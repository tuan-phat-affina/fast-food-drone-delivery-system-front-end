package com.fast_food_frontend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private AddressResponse address;
    private String phone;
    private String description;
    private Long ownerId;
}
