package com.fast_food_frontend.dto.response;

import com.fast_food_frontend.enums.AddressTypeStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressResponse {
    private Integer id;
    private Integer ownerId;
    private String street;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private AddressTypeStatus type;

}
