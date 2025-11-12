package com.fast_food_frontend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DishResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Boolean available;
    private Long restaurantId;
}
