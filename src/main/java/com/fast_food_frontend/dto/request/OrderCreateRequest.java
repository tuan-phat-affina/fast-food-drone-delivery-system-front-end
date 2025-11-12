package com.fast_food_frontend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull Long restaurantId,
        Long deliveryAddressId,
        //option
        AddressRequest deliveryAddress,
        @NotEmpty List<OrderItemRequest> items,
        @NotBlank String paymentMethod
) {
    public record OrderItemRequest(
            @NotNull Long dishId,
            @Min(1) Integer quantity
    ) {}
}
