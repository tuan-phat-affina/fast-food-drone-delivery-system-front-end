package com.fast_food_frontend.dto.request;

import com.fast_food_frontend.enums.RestaurantOrderAction;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull RestaurantOrderAction action,
        Long paymentId,
        String reason // optional, required for REJECT maybe
) {}
