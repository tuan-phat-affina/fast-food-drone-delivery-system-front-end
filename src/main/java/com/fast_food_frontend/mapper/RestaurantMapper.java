package com.fast_food_frontend.mapper;

import com.fast_food_frontend.dto.request.RestaurantRequest;
import com.fast_food_frontend.dto.response.RestaurantResponse;
import com.fast_food_frontend.entity.Restaurant;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(target = "address", ignore = true)
    Restaurant toRestaurant(RestaurantRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    RestaurantResponse toRestaurantResponse(Restaurant restaurant);

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRestaurant(@MappingTarget Restaurant entity, RestaurantRequest request);
}
