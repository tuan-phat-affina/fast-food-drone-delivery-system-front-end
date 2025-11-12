package com.fast_food_frontend.dto.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Builder
@Getter
public class OrderPendingCheckEvent extends ApplicationEvent {
    private final Long orderId;
    private final Instant scheduledTime;

    public OrderPendingCheckEvent(Long orderId, Instant scheduledTime) {
        super(orderId);
        this.orderId = orderId;
        this.scheduledTime = scheduledTime;
    }
}
