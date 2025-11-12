package com.fast_food_frontend.controller;

import com.fast_food_frontend.common.RestResponse;
import com.fast_food_frontend.dto.request.OrderCreateRequest;
import com.fast_food_frontend.dto.request.UpdateOrderStatusRequest;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.dto.response.OrderResponse;
import com.fast_food_frontend.service.IOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final IOrderService orderService;
    @PostMapping
    public ResponseEntity<RestResponse<OrderResponse>> createOrder(
            @RequestBody @Valid OrderCreateRequest req,
            HttpServletRequest httpReq) {
        Long customerId = extractUserIdFromRequest(httpReq);
        RestResponse<OrderResponse> created = orderService.createOrder(customerId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<RestResponse<ListResponse<OrderResponse>>> getListOrdersByFilter(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean all) {
        RestResponse<ListResponse<OrderResponse>> listOrders= orderService.getListOrdersByFilter(page, size, sort, filter, search, all);
        return ResponseEntity.status(HttpStatus.OK).body(listOrders);
    }

    @PostMapping("/{orderId}/status")
    public ResponseEntity<RestResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody @Valid UpdateOrderStatusRequest request,
            HttpServletRequest httpReq
    ) {
        Long customerId = extractUserIdFromRequest(httpReq);
        RestResponse<OrderResponse> orderUpdated = orderService.restaurantHandleOrderStatus(httpReq.getRemoteAddr(), customerId, orderId, request);
        return ResponseEntity.status(HttpStatus.OK).body(orderUpdated);
    }

    @PostMapping("/{orderId}/pick-up")
    public ResponseEntity<RestResponse<OrderResponse>> restaurantPickUpOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpReq) {
        Long customerId = extractUserIdFromRequest(httpReq);
        RestResponse<OrderResponse> orderUpdated = orderService.restaurantReadyForPickup(customerId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orderUpdated);
    }

    @PostMapping("/{orderId}/confirmed")
    public ResponseEntity<RestResponse<OrderResponse>> customerConfirmOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpReq) {
        Long customerId = extractUserIdFromRequest(httpReq);
        RestResponse<OrderResponse> orderUpdated = orderService.confirmDelivered(customerId, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(orderUpdated);
    }
}
