package com.fast_food_frontend.entity;

import com.fast_food_frontend.enums.DroneStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drones")
public class Drone implements Serializable {
    private static final long serialVersionUID = 7980135901401334589L;
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private DroneStatus status;

    @Column(name = "current_lat", precision = 10, scale = 6)
    private BigDecimal currentLat;

    @Column(name = "current_lng", precision = 10, scale = 6)
    private BigDecimal currentLng;

    @ColumnDefault("100.00")
    @Column(name = "battery_level", nullable = false, precision = 5, scale = 2)
    private BigDecimal batteryLevel;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

}