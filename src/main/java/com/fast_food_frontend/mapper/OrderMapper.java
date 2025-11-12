package com.fast_food_frontend.mapper;

import com.fast_food_frontend.dto.response.OrderResponse;
import com.fast_food_frontend.entity.Order;
import com.fast_food_frontend.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "restaurant.id", target = "restaurantId")
    @Mapping(source = "items", target = "items")
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse.OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    @Mapping(source = "dish.id", target = "dishId")
    @Mapping(source = "dish.name", target = "dishName")
    @Mapping(source = "quantity", target = "qty")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(source = "subtotal", target = "subtotal")
    OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item);

    // Default method for DeliveryTask mapping (can return null if not implemented)
//    default OrderResponse.DeliveryTaskSummary mapDeliveryTask(DeliveryTask task) {
//        if (task == null) {
//            return null;
//        }
//        // Example mapping, adapt when DeliveryTask entity exists
//        return new OrderResponse.DeliveryTaskSummary(
//                task.getId(),
//                task.getDrone() != null ? task.getDrone().getId() : null,
//                task.getStatus(),
//                task.getPickupLat(),
//                task.getPickupLng(),
//                task.getDropoffLat(),
//                task.getDropoffLng()
//        );
//    }
//
//    // Helper to map Order.items using MapStruct
//    default List<OrderResponse.OrderItemResponse> mapItems(List<OrderItem> items) {
//        if (items == null) return List.of();
//        return items.stream()
//                .map(this::toOrderItemResponse)
//                .toList();
//    }
}
