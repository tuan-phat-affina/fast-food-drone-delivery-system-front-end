package com.fast_food_frontend.mapper;

import com.fast_food_frontend.dto.response.PaymentResponse;
import com.fast_food_frontend.entity.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toPaymentResponse(Payment request);
}
