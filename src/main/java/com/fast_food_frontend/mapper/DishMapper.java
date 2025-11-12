package com.fast_food_frontend.mapper;

import com.fast_food_frontend.dto.request.DishRequest;
import com.fast_food_frontend.dto.response.DishResponse;
import com.fast_food_frontend.entity.Dish;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DishMapper {

    Dish toDish(DishRequest request);

    DishResponse toDishResponse(Dish request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDish(@MappingTarget Dish entity, DishRequest request);
}
