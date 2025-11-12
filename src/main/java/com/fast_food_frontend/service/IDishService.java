package com.fast_food_frontend.service;

import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.dto.request.DishRequest;
import com.fast_food_frontend.dto.response.DishResponse;
import com.fast_food_frontend.dto.response.ListResponse;

public interface IDishService {
    RestResponse<DishResponse> createDish(Long restaurantId, DishRequest req, Long ownerId);

    RestResponse<ListResponse<DishResponse>> getListDishes(int page, int size, String sort, String filter, String search, boolean all);

    RestResponse<DishResponse> updateDish(Long dishId, DishRequest req, Long ownerId);
}
