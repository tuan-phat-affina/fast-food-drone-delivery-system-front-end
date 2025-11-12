package com.fast_food_frontend.controller;

import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.dto.request.DishRequest;
import com.fast_food_frontend.dto.response.DishResponse;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.service.IDishService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController extends BaseController {
    private final IDishService dishService;

    @PostMapping
    public ResponseEntity<RestResponse<DishResponse>> createDish(
            @RequestParam Long restaurantId,
            @RequestBody DishRequest request,
            HttpServletRequest httpReq) {
        Long ownerId = extractUserIdFromRequest(httpReq);
        return ResponseEntity.ok(dishService.createDish(restaurantId, request, ownerId));
    }

    @GetMapping
    public ResponseEntity<RestResponse<ListResponse<DishResponse>>> getListDishes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "code,desc") String sort,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean all) {
        return ResponseEntity.ok(dishService.getListDishes(page, size, sort, filter, search, all));
    }

    @PutMapping("/{dishId}")
    public ResponseEntity<RestResponse<DishResponse>> updateDish(
            @PathVariable Long dishId,
            @RequestHeader("X-Owner-Id") Long ownerId,
            @RequestBody DishRequest request) {
        return ResponseEntity.ok(dishService.updateDish(dishId, request, ownerId));
    }
}
