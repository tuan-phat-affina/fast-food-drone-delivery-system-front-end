package com.fast_food_frontend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DroneCreateRequest {
    String status;
    String code;
   Double currentLat;
   Double currentLng;
   Double batteryLevel;
}
