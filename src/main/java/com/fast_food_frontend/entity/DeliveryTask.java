package com.fast_food_frontend.entity;

import com.fast_food_frontend.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "delivery_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryTask implements Serializable {
    private static final long serialVersionUID = 2851050709728106410L;
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @Column(name = "pickup_lat", nullable = false, precision = 10, scale = 6)
    private BigDecimal pickupLat;

    @Column(name = "pickup_lng", nullable = false, precision = 10, scale = 6)
    private BigDecimal pickupLng;

    @Column(name = "dropoff_lat", nullable = false, precision = 10, scale = 6)
    private BigDecimal dropoffLat;

    @Column(name = "dropoff_lng", nullable = false, precision = 10, scale = 6)
    private BigDecimal dropoffLng;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}