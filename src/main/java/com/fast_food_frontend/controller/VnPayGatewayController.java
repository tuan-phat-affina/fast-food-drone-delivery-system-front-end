package com.fast_food_frontend.controller;

import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.service.impl.VnPayService;
import com.fast_food_frontend.repository.OrderRepository;
import com.fast_food_frontend.repository.PaymentRepository;
import com.fast_food_frontend.service.EventPublisher;
import com.fast_food_frontend.service.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class VnPayGatewayController {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VnPayService vnPayService;
    private final IPaymentService paymentService;
    private final EventPublisher eventPublisher;

    @GetMapping("/vnpay/return")
    @Transactional
    public ResponseEntity<RestResponse<String>> vnpayReturn(HttpServletRequest request) {
        RestResponse<String> response = paymentService.handleVnPayIpn(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/vnpay/ipn")
    @Transactional
    public ResponseEntity<RestResponse<String>> vnpayHandleIpn(HttpServletRequest request) {
        RestResponse<String> response = paymentService.handleVnPayIpn(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
