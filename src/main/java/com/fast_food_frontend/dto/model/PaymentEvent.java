package com.fast_food_frontend.dto.model;

import com.fast_food_frontend.enums.PaymentMethod;
import com.fast_food_frontend.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEvent {
    Long paymentId;
    Long orderId;
    BigDecimal amount;
    PaymentMethod method;
    PaymentStatus status;
    Instant occurredAt;
    Map<String, Object> metadata;
}
