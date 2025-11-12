package com.fast_food_frontend.controller;

import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.dto.request.PaymentCreateRequest;
import com.fast_food_frontend.dto.response.PaymentResponse;
import com.fast_food_frontend.service.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController extends BaseController{
    private final IPaymentService paymentService;

    @PostMapping
    public ResponseEntity<RestResponse<PaymentResponse>> createPayment(
            @RequestBody PaymentCreateRequest request,
            HttpServletRequest httpReq) {
        Long customerId = extractUserIdFromRequest(httpReq);
        RestResponse<PaymentResponse> response = paymentService
                .createPayment(httpReq.getRemoteAddr(), customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
