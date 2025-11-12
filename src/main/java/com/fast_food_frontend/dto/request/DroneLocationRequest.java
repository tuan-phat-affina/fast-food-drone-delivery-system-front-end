package com.fast_food_frontend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DroneLocationRequest {
    @NotNull(message = "lat is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double lat;

    @NotNull(message = "lng is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double lng;

    private Double altitude;
    private Double speed;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private Double battery;

    private Instant timestamp;
}
