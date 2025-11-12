package com.fast_food_frontend.service;

import com.fast_food_frontend.dto.model.OrderEvent;
import com.fast_food_frontend.dto.model.OrderPendingCheckEvent;
import com.fast_food_frontend.dto.model.PaymentEvent;

public interface EventPublisher {
    void publish(OrderEvent event);

    void publish(PaymentEvent event);

    void pulishEvent(OrderPendingCheckEvent event);
}
