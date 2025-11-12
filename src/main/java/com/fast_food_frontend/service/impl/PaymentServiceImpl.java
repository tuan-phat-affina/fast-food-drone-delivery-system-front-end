package com.fast_food_frontend.service.impl;

import com.fast_food_frontend.common.IdGenerator;
import com.fast_food_frontend.common.JsonUtil;
import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.dto.model.PaymentEvent;
import com.fast_food_frontend.dto.request.PaymentCreateRequest;
import com.fast_food_frontend.dto.response.PaymentResponse;
import com.fast_food_frontend.entity.Order;
import com.fast_food_frontend.entity.Payment;
import com.fast_food_frontend.enums.OrderStatus;
import com.fast_food_frontend.enums.PaymentMethod;
import com.fast_food_frontend.enums.PaymentStatus;
import com.fast_food_frontend.exception.AppException;
import com.fast_food_frontend.exception.ErrorCode;
import com.fast_food_frontend.mapper.PaymentMapper;
import com.fast_food_frontend.repository.OrderRepository;
import com.fast_food_frontend.repository.PaymentRepository;
import com.fast_food_frontend.service.EventPublisher;
import com.fast_food_frontend.service.IOrderService;
import com.fast_food_frontend.service.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentServiceImpl implements IPaymentService {
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;
    EventPublisher eventPublisher;
    VnPayService vnPayService;
    PaymentMapper paymentMapper;
    IOrderService orderService;

    @NonFinal
    @Value("${vnpay.secretKey}")
    String secretKey;

    @Transactional
    @Override
    public RestResponse<PaymentResponse> createPayment(String ipAddress, Long customerId, PaymentCreateRequest req) {
        log.info("Start creating payment for orderId={} by customer={}", req.orderId(), customerId);

        Order order = orderRepository.findById(req.orderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_TO_UPDATE_THIS_RESOURCE);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.BUSINESS_INVALID_SEQUENCE);
        }

        // 1. Tạo payment record
        Payment payment = Payment.builder()
                .id(IdGenerator.generateRandomId())
                .order(order)
                .method(req.method())
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .transactionTime(Instant.now())
                .build();

        paymentRepository.save(payment);

        log.info("Created payment record id={} for orderId={}", payment.getId(), req.orderId());

        // 2. Nếu ONLINE, giả lập gọi payment gateway (asynchronous)
        if (req.method() == PaymentMethod.ONLINE) {
            String locale = "vn";
            String orderInfo = "Thanh toan don hang: " + order.getId();
            String paymentUrl = vnPayService.buildPaymentUrl(payment.getId(),
                    order.getTotalAmount(), ipAddress, locale, orderInfo);

            payment.setStatus(PaymentStatus.PENDING);
            payment.setTransactionId(null);
            paymentRepository.save(payment);

            return RestResponse.ok(PaymentResponse.builder()
                    .id(payment.getId())
                    .orderId(order.getId())
                    .method(payment.getMethod())
                    .status(payment.getStatus())
                    .amount(payment.getAmount())
                    .transactionId(payment.getTransactionId())
                    .transactionTime(payment.getTransactionTime())
                    .paymentUrl(paymentUrl)
                    .build());
        } else {
            // COD -> mark order as confirmed but unpaid
            order.setStatus(OrderStatus.PREPARING);
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);
        }

        eventPublisher.publish(PaymentEvent.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .occurredAt(Instant.now())
                .metadata(Map.of(
                        "amount", payment.getAmount(),
                        "method", payment.getMethod()
                ))
                .build());

        return RestResponse.ok(paymentMapper.toPaymentResponse(payment));
    }

    @Override
    public RestResponse<String> handleVnPayIpn(HttpServletRequest request) {
        Map<String, String> params = extractVnpParams(request);
        log.info("Received VNPAY IPN callback: {}", params);

        //Kiểm tra chữ ký bảo mật
        String vnpSecureHash = params.remove("vnp_SecureHash");
        String signData = vnPayService.buildHashData(params);
        String expectedHash = vnPayService.hmacSHA512(secretKey, signData);

        if (!expectedHash.equalsIgnoreCase(vnpSecureHash)) {
            log.warn("VNPAY invalid signature: {}", vnpSecureHash);
            return RestResponse.badRequest("Invalid signature",  vnpSecureHash);
        }

        Long paymentId = Long.valueOf(params.get("vnp_TxnRef"));
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();

        // 3️⃣ Check idempotency (đã xử lý rồi thì bỏ qua)
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return RestResponse.ok("{\"RspCode\":\"00\",\"Message\":\"Already confirmed\"}");
        }

        // Xử lý logic cập nhật đơn hàng & thanh toán
        if ("00".equals(responseCode)) {
            log.info("vào");
//            handlePaymentSuccess(payment, order, params);
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(transactionNo);
            payment.setTransactionTime(Instant.now());
            payment.setVnpayResp(JsonUtil.toJson(params));

            order.setStatus(OrderStatus.PREPARING);
            order.setUpdatedAt(Instant.now());
        } else {
            //todo
//            handlePaymentFailure(payment, order);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setTransactionTime(Instant.now());
            payment.setTransactionId(transactionNo);

            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(Instant.now());
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        publishPaymentEvent(payment, order, params);

        return RestResponse.ok("Thanh toán " + ("00".equals(responseCode) ? "thành công" : "thất bại"));
    }

    private Map<String, String> extractVnpParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            if (name.startsWith("vnp_")) {
                params.put(name, request.getParameter(name));
            }
        }
        return params;
    }

    private void handlePaymentSuccess(Payment payment, Order order, Map<String, String> params) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(params.get("vnp_TransactionNo"));
        payment.setTransactionTime(Instant.now());

        order.setStatus(OrderStatus.PREPARING);
        order.setUpdatedAt(Instant.now());
    }

    private void handlePaymentFailure(Payment payment, Order order) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setTransactionTime(Instant.now());

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
    }

    private void publishPaymentEvent(Payment payment, Order order, Map<String, String> params) {
        eventPublisher.publish(
                PaymentEvent.builder()
                        .paymentId(payment.getId())
                        .orderId(order.getId())
                        .status(payment.getStatus())
                        .occurredAt(Instant.now())
                        .metadata(Map.of(
                                "transactionNo", params.get("vnp_TransactionNo")
                        ))
                        .build()
        );
    }

}
