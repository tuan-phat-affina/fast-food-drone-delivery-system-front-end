package com.fast_food_frontend.repository;

import com.fast_food_frontend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
