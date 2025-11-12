package com.fast_food_frontend.repository;

import com.fast_food_frontend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByCustomer_Id(Long customerId, Pageable p);
    Page<Order> findByRestaurant_Id(Long restaurantId, Pageable p);
}
