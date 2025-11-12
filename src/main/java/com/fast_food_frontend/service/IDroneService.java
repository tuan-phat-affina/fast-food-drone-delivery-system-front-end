package com.fast_food_frontend.service;

import com.fast_food_frontend.dto.request.DroneCreateRequest;
import com.fast_food_frontend.dto.request.DroneLocationRequest;
import com.fast_food_frontend.dto.request.DroneUpdateRequest;
import com.fast_food_frontend.dto.response.DroneResponse;
import com.fast_food_frontend.dto.response.ListResponse;
import jakarta.validation.Valid;

public interface IDroneService {
    ListResponse<DroneResponse> getListDronesResponseByStatus(int page, int size, String sort, String filter, String search, boolean all);

    DroneResponse getDroneDetail(Long id);

    DroneResponse createDrone(@Valid DroneCreateRequest req);

    DroneResponse updateDrone(String code, DroneUpdateRequest req);

    DroneResponse updateDroneLocation(Long id, @Valid DroneLocationRequest req);
//    List<DroneResponse> getListDronesResponseByStatus(String status);
}
