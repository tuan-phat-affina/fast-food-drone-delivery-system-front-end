package com.fast_food_frontend.service;

import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.dto.request.PaymentCreateRequest;
import com.fast_food_frontend.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface IPaymentService {
    RestResponse<PaymentResponse> createPayment(String ipAddress, Long customerId, PaymentCreateRequest request);

    RestResponse<String> handleVnPayIpn(HttpServletRequest request);
}
