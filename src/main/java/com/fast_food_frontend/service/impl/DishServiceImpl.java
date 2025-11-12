package com.fast_food_frontend.service.impl;

import com.fast_food_frontend.common.IdGenerator;
import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.common.SearchHelper;
import com.fast_food_frontend.dto.request.DishRequest;
import com.fast_food_frontend.dto.response.DishResponse;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.entity.Dish;
import com.fast_food_frontend.entity.Restaurant;
import com.fast_food_frontend.enums.DishStatus;
import com.fast_food_frontend.exception.AppException;
import com.fast_food_frontend.exception.ErrorCode;
import com.fast_food_frontend.mapper.DishMapper;
import com.fast_food_frontend.repository.DishRepository;
import com.fast_food_frontend.repository.RestaurantRepository;
import com.fast_food_frontend.service.IDishService;
import io.github.perplexhub.rsql.RSQLJPASupport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DishServiceImpl implements IDishService {
    DishRepository dishRepository;
    RestaurantRepository restaurantRepository;
    DishMapper dishMapper;

    private static final List<String> SEARCH_FIELDS = List.of("name", "status", "price", "restaurant_id");

    @Override
    public RestResponse<DishResponse> createDish(Long restaurantId, DishRequest req, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(ErrorCode.DATASOURCE_NOT_FOUND));

        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized to add dish to this restaurant");
        }

        Dish dish = dishMapper.toDish(req);
        dish.setId(IdGenerator.generateRandomId());
        dish.setRestaurant(restaurant);
        dish.setStatus(DishStatus.AVAILABLE.name());
        dish.setCreatedAt(Instant.now());
        return RestResponse.ok(dishMapper.toDishResponse(dishRepository.save(dish)));
    }

    @Override
    public RestResponse<ListResponse<DishResponse>> getListDishes(int page, int size, String sort, String filter, String search, boolean all) {
        Specification<Dish> sortable = RSQLJPASupport.toSort(sort);
        Specification<Dish> filterable = RSQLJPASupport.toSpecification(filter);
        Specification<Dish> searchable = SearchHelper.parseSearchToken(search, SEARCH_FIELDS);
        Pageable pageable = all ? Pageable.unpaged() : PageRequest.of(page - 1, size);
        Page<DishResponse> responses = dishRepository
                .findAll(sortable.and(filterable).and(searchable), pageable)
                .map(dishMapper::toDishResponse);

        return RestResponse.ok(ListResponse.of(responses));
    }

    @Override
    public RestResponse<DishResponse> updateDish(Long dishId, DishRequest req, Long ownerId) {
        return null;
    }
}
