package com.fast_food_frontend.dto.response;

import com.fast_food_frontend.enums.DeliveryStatus;
import com.fast_food_frontend.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse (
        Long id,
        Long customerId,
        Long restaurantId,
        OrderStatus status,
        Double totalAmount,
        Double shippingFee,
        List<OrderItemResponse> items,
        DeliveryTaskSummary deliveryTask,
        Instant createdAt,
        Instant updatedAt
) {
    public record OrderItemResponse(
            Long dishId,
            String dishName,
            Integer qty,
            Double unitPrice,
            Double subtotal
    ) {}

    public record DeliveryTaskSummary(
            Long id,
            Long droneId,
            DeliveryStatus status,
            BigDecimal pickupLat,
            BigDecimal pickupLng,
            BigDecimal dropoffLat,
            BigDecimal dropoffLng
    ) {}
}
