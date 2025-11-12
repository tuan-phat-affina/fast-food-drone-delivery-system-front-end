package com.fast_food_frontend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DroneUpdateRequest {
    private String status; // should be one of DroneStatus values
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private BigDecimal batteryLevel;
}
