package com.fast_food_frontend.repository;

import com.fast_food_frontend.entity.DeliveryTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, Long> {
    Optional<DeliveryTask> findByOrder_Id(Long orderId);
}
