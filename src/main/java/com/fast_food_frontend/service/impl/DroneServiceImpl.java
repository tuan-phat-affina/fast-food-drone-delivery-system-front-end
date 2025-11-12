package com.fast_food_frontend.service.impl;

import com.fast_food_frontend.common.IdGenerator;
import com.fast_food_frontend.common.SearchHelper;
import com.fast_food_frontend.dto.request.DroneCreateRequest;
import com.fast_food_frontend.dto.request.DroneLocationRequest;
import com.fast_food_frontend.dto.request.DroneUpdateRequest;
import com.fast_food_frontend.dto.response.DroneResponse;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.entity.Drone;
import com.fast_food_frontend.enums.DroneStatus;
import com.fast_food_frontend.exception.AppException;
import com.fast_food_frontend.exception.ErrorCode;
import com.fast_food_frontend.mapper.DroneMapper;
import com.fast_food_frontend.repository.DroneRepository;
import com.fast_food_frontend.service.IDroneService;
import io.github.perplexhub.rsql.RSQLJPASupport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DroneServiceImpl implements IDroneService {

    DroneRepository droneRepository;
    DroneMapper droneMapper;

    private static final List<String> SEARCH_FIELDS = List.of("code", "status");

    @Override
    public ListResponse<DroneResponse> getListDronesResponseByStatus(int page, int size, String sort, String filter, String search, boolean all) {
        Specification<Drone> sortable = RSQLJPASupport.toSort(sort);
        Specification<Drone> filterable = RSQLJPASupport.toSpecification(filter);
        Specification<Drone> searchable = SearchHelper.parseSearchToken(search, SEARCH_FIELDS);
        Pageable pageable = all ? Pageable.unpaged() : PageRequest.of(page - 1, size);
        Page<DroneResponse> responses = droneRepository
                .findAll(sortable.and(filterable).and(searchable), pageable)
                .map(droneMapper::toDroneResponse);

        return ListResponse.of(responses);
    }

    @Override
    public DroneResponse getDroneDetail(Long id) {
        Drone drone = droneRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATASOURCE_NOT_FOUND));
        return droneMapper.toDroneResponse(drone);
    }

    @Override
    public DroneResponse createDrone(DroneCreateRequest request) {
        if (droneRepository.findByCode(request.getCode().trim()).isPresent()) {
            throw new AppException(ErrorCode.DATASOURCE_ALREADY_EXISTS);
        }

        DroneStatus status;
        try {
            status = DroneStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.DATASOURCE_ENUM_NOT_FOUND);
        }

        if (request.getBatteryLevel() != null && (request.getBatteryLevel() < 0.0 || request.getBatteryLevel() > 100.0)) {
            throw new AppException(ErrorCode.INVALID_BUSINESS_FLOW);
        }

        Drone drone = droneMapper.toDrone(request);
        drone.setId(IdGenerator.generateRandomId());
        drone.setCode(request.getCode().trim());
        drone.setStatus(status);
        drone.setLastUpdated(Instant.now());

        Drone savedDrone = droneRepository.save(drone);
        return droneMapper.toDroneResponse(savedDrone);
    }

    @Override
    public DroneResponse updateDrone(String code, DroneUpdateRequest request) {
        Drone drone = droneRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.DATASOURCE_NOT_FOUND));

        // Nếu có status truyền vào, kiểm tra business rule
        if (request.getStatus() != null) {
            DroneStatus status;
            try {
                status = DroneStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.DATASOURCE_ENUM_NOT_FOUND);
            }

            // Business rule: không cho set trạng thái AVAILABLE khi pin < 10%
            if (status == DroneStatus.AVAILABLE && drone.getBatteryLevel() != null && drone.getBatteryLevel().compareTo(BigDecimal.valueOf(10.0)) < 0) {
                throw new AppException(ErrorCode.INVALID_BUSINESS_FLOW);
            }
            drone.setStatus(status);
        }

        // Cập nhật các trường khác nếu có
        if (request.getCurrentLat() != null) drone.setCurrentLat(request.getCurrentLat());
        if (request.getCurrentLng() != null) drone.setCurrentLng(request.getCurrentLng());
        BigDecimal batteryLevel = request.getBatteryLevel();
        if (batteryLevel != null) {
            if (batteryLevel.compareTo(BigDecimal.ZERO) < 0 || batteryLevel.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new AppException(ErrorCode.INVALID_BUSINESS_FLOW);
            }
            drone.setBatteryLevel(request.getBatteryLevel());
        }

        drone.setLastUpdated(Instant.now());
        Drone updatedDrone = droneRepository.save(drone);
        return droneMapper.toDroneResponse(updatedDrone);
    }

    @Override
    public DroneResponse updateDroneLocation(Long id, DroneLocationRequest req) {
        Drone drone = droneRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATASOURCE_NOT_FOUND));

        if (req.getLat() == null || req.getLng() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        drone.setCurrentLat(BigDecimal.valueOf(req.getLat()));
        drone.setCurrentLng(BigDecimal.valueOf(req.getLng()));

        if (req.getBattery() != null) {
            if (req.getBattery() < 0.0 || req.getBattery() > 100.0) {
                throw new AppException(ErrorCode.INVALID_BUSINESS_FLOW);
            }
            drone.setBatteryLevel(BigDecimal.valueOf(req.getBattery()));
        }

        drone.setLastUpdated(req.getTimestamp() != null ? req.getTimestamp() : Instant.now());

        Drone savedDrone = droneRepository.save(drone);

        // TODO: publish update event to websocket/message broker

        return droneMapper.toDroneResponse(savedDrone);
    }
}
