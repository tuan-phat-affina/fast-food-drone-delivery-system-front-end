package com.fast_food_frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FastFoodDroneDeliverySystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastFoodDroneDeliverySystemApplication.class, args);
	}

}
