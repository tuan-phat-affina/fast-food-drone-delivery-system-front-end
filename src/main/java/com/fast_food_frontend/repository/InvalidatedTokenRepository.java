package com.fast_food_frontend.repository;

import com.fast_food_frontend.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository  extends JpaRepository<InvalidatedToken, String> {
}
