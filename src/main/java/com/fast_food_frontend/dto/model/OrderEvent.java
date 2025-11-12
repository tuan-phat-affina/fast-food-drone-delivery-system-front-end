package com.fast_food_frontend.dto.model;

import com.fast_food_frontend.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderEvent {
    private Long orderId;
    private List<String> emailTo;
    private OrderStatus status;
    private Instant occurredAt;
    private Map<String, Object> metadata;
}