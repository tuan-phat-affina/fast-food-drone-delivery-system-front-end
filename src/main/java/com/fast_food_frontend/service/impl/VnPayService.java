package com.fast_food_frontend.service.impl;


import com.fast_food_frontend.entity.Payment;
import com.fast_food_frontend.exception.AppException;
import com.fast_food_frontend.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {
    @Value("${vnpay.tmnCode}")
    private String tmnCode;
    @Value("${vnpay.secretKey}")
    private String secretKey;
    @Value("${vnpay.paymentUrl}")
    private String paymentUrl;
    @Value("${vnpay.returnUrl}")
    private String returnUrl;
    @Value("${vnpay.ipnUrl}")
    private String ipnUrl;
    @Value("${vnpay.refundUrl}")
    private String refundUrl;

    private ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate =  new RestTemplate();

    public String buildPaymentUrl(Long paymentId, BigDecimal amount, String clientIp, String locale, String orderInfo) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue())); // multiply 100 per doc. :contentReference[oaicite:2]{index=2}
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(paymentId));
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", locale != null ? locale : "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
//        vnpParams.put("vnp_IpnUrl", ipnUrl);
        vnpParams.put("vnp_IpAddr", clientIp);

        String createDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(Instant.now());
        vnpParams.put("vnp_CreateDate", createDate);

        // Sort params and generate hash
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = vnpParams.get(fieldName);
            if (value != null && value.length() > 0) {
                hashData.append(fieldName).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append("&");
                query.append(fieldName).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII)).append("&");
            }
        }
        // remove trailing &
        if (hashData.length() > 0) hashData.setLength(hashData.length() - 1);
        if (query.length() > 0) query.setLength(query.length() - 1);

        String secureHash = hmacSHA512(secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return paymentUrl + "?" + query.toString();
    }

    @Transactional
    public void refund(Payment payment, long amountToRefund, String clientIp) throws JsonProcessingException {
        // chuẩn hóa số tiền: VNPay yêu cầu đơn vị là VNĐ *100 nếu yêu cầu theo tài liệu (tùy độ hiểu ứng dụng bạn)
        long vnpAmount = amountToRefund * 100;
        Map<String, Object> vnpayRespParams = objectMapper.readValue(payment.getVnpayResp(), Map.class);

        String requestId = UUID.randomUUID().toString();
        String version = "2.1.0";
        String command = "refund";
        String createDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("GMT+7"))
                .format(Instant.now());
        String transactionTime = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("GMT+7"))
                .format(payment.getTransactionTime());
        String transactionType = "02"; // “02” = hoàn tiền toàn phần, “03” = hoàn tiền một phần
        String txnRef = String.valueOf(payment.getOrder().getId());
        String transactionNo = payment.getTransactionId();
        String orderInfo = "Thanh toan don hang: " + payment.getOrder().getId();

        // build map tham số
        Map<String, String> params = new HashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_TransactionType", transactionType);
        params.put("vnp_TxnRef", vnpayRespParams.get("vnp_TxnRef").toString());
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_TransactionNo", vnpayRespParams.get("vnp_TransactionNo").toString());
        params.put("vnp_TransactionDate", vnpayRespParams.get("vnp_PayDate").toString());
        params.put("vnp_CreateBy", "system"); // hoặc userId
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", clientIp);

        // Tạo SecureHash
        String secureHash = buildSecureHash(params, secretKey);
        params.put("vnp_SecureHash", secureHash);

        log.info("checksum: {}", secureHash);

        // Gửi POST request – dạng JSON hoặc form, tùy cấu hình cổng
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(refundUrl, entity, String.class);

        log.info("responseEntity={}", responseEntity);

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new AppException(ErrorCode.REFUND_FAILED);
        }

        // parse kết quả JSON về object (tùy cấu trúc)
        String body = responseEntity.getBody();
        // Ví dụ: parse JSON, kiểm tra vnp_ResponseCode == "00"
        Map<String, String> respMap = parseResponse(body);

        String respCode = respMap.get("vnp_ResponseCode");
        if (!"00".equals(respCode)) {
            throw new AppException(ErrorCode.REFUND_FAILED);
        }

        // optional: xác minh secureHash phản hồi từ VNPay – nếu API cung cấp secureHash trả về
        // nếu ok → cập nhật trạng thái payment = REFUNDED
    }

    private String buildSecureHash(Map<String, String> params, String secret) {
//        // sắp xếp params theo key asc (ASCII) và nối theo dạng: key=value&key2=value2...
//        SortedMap<String, String> sorted = new TreeMap<>(params);
//        StringBuilder sb = new StringBuilder();
//        // thường không bao gồm trường vnp_SecureHash khi tính
//        for (Map.Entry<String, String> entry : sorted.entrySet()) {
//            if ("vnp_SecureHash".equals(entry.getKey())) {
//                continue;
//            }
//            if (sb.length() > 0) {
//                sb.append("|");
//            }
//            sb.append(entry.getValue());
//        }
//        String data = sb.toString();
//        // dùng HMACSHA512
//        try {
//            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
//            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
//            sha512Hmac.init(secretKey);
//            byte[] hashBytes = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            return bytesToHex(hashBytes);
//        } catch (GeneralSecurityException e) {
//            throw new RuntimeException("Error when generating secure hash for VNPay", e);
//        }
        String[] fields = {
                "vnp_RequestId",
                "vnp_Version",
                "vnp_Command",
                "vnp_TmnCode",
                "vnp_TransactionType",
                "vnp_TxnRef",
                "vnp_Amount",
                "vnp_TransactionNo",
                "vnp_TransactionDate",
                "vnp_CreateBy",
                "vnp_CreateDate",
                "vnp_IpAddr",
                "vnp_OrderInfo",
        };

        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            if (params.containsKey(field)) {
                if (sb.length() > 0) sb.append("|");
                sb.append(params.get(field));
            }
        }

        String data = sb.toString();
        log.info("data: {}", data);
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);
            byte[] hashBytes = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error when generating secure hash for VNPay", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Map<String, String> parseResponse(String body) {
        // parse JSON string thành Map – bạn có thể dùng Jackson, Gson
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(body, new TypeReference<Map<String, String>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse VNPay refund response", e);
        }
    }

    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.US_ASCII));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC SHA512", e);
        }
    }

    public static String buildHashData(Map<String, String> params) {
        // 1. Sắp xếp theo key alphabet
        SortedMap<String, String> sortedParams = new TreeMap<>(params);

        // 2. Duyệt map và encode
        List<String> list = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    // URLEncoder.encode chỉ encode giá trị, không encode key
                    String value = URLEncoder.encode(entry.getValue(), "UTF-8");
                    list.add(entry.getKey() + "=" + value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding VNPAY params", e);
        }

        // 3. Nối các key=value bằng &
        return String.join("&", list);
    }

    public String getSecretKey() {
        return this.secretKey;
    }
}
