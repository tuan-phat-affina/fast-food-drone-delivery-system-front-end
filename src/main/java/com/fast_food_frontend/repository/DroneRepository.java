package com.fast_food_frontend.repository;

import com.fast_food_frontend.entity.Drone;
import com.fast_food_frontend.enums.DroneStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drone, Long>, JpaSpecificationExecutor<Drone> {
    List<Drone> findByStatus(DroneStatus status);

    Optional<Drone> findByCode(@NotBlank(message = "code is required") String code);

    @Query("select d from Drone d where d.status = :status order by (d.currentLat - :pickupLat)*(d.currentLat - :pickupLat) + (d.currentLng - :pickupLng)*(d.currentLng - :pickupLng)")
    List<Drone> findAvailableDronesForUpdate(DroneStatus status, BigDecimal pickupLat, BigDecimal pickupLng);
}
