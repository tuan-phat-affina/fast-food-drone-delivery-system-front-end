package com.fast_food_frontend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RestaurantRequest {
    private String name;
    private AddressRequest address;
    private String phone;
    private String description;
}
