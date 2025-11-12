package com.fast_food_frontend.common;

import com.fast_food_frontend.dto.model.OrderEvent;
import com.fast_food_frontend.dto.model.OrderPendingCheckEvent;
import com.fast_food_frontend.enums.OrderStatus;
import com.fast_food_frontend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPendingCheckListener {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    @Transactional
    public void handleOrderPendingCheckEvent(OrderPendingCheckEvent event) {
        Duration delay = Duration.between(Instant.now(), event.getScheduledTime());
        if (!delay.isNegative()) {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException ignored) {}
        }

        // Re-check order status
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(Instant.now());
                orderRepository.save(order);

                log.info("Auto-cancelled order {} after pending timeout", order.getId());

                eventPublisher.publishEvent(OrderEvent.builder()
                        .orderId(order.getId())
                        .emailTo(List.of(order.getCustomer().getEmail()))
                        .status(order.getStatus())
                        .occurredAt(Instant.now())
                        .metadata(Map.of("reason", "Auto-cancelled after 4 minutes"))
                        .build());
            }
        });
    }
}
