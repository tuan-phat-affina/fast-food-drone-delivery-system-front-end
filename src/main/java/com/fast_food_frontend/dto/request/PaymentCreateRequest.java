package com.fast_food_frontend.dto.request;

import com.fast_food_frontend.enums.PaymentMethod;

public record PaymentCreateRequest(
        Long orderId,
        PaymentMethod method
) {}
