package com.fast_food_frontend.controller;

import com.fast_food_frontend.dto.request.RestaurantRequest;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.dto.response.RestaurantResponse;
import com.fast_food_frontend.service.IRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final IRestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @RequestBody RestaurantRequest request,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        return ResponseEntity.ok(restaurantService.createRestaurant(ownerId, request));
    }

    @GetMapping
    public ResponseEntity<ListResponse<RestaurantResponse>> getListRestaurants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "rating,desc") String sort,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean all) {
        return ResponseEntity.ok(restaurantService.getListRestaurants(page, size, sort, filter, search, all));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") Long ownerId,
            @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, ownerId, request));
    }
}
