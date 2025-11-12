package com.fast_food_frontend.controller;

import com.fast_food_frontend.dto.request.DroneCreateRequest;
import com.fast_food_frontend.dto.request.DroneLocationRequest;
import com.fast_food_frontend.dto.request.DroneUpdateRequest;
import com.fast_food_frontend.dto.response.DroneResponse;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.service.IDroneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController {
    private final IDroneService droneService;

    @GetMapping
    public ResponseEntity<ListResponse<DroneResponse>> getListDrones(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "code,desc") String sort,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) boolean all) {
        ListResponse<DroneResponse> list = droneService.getListDronesResponseByStatus(page, size, sort, filter, search, all);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DroneResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(droneService.getDroneDetail(id));
    }

    // @PreAuthorize("hasRole('RESTAURANT') or hasRole('ADMIN')")  // enable in real app
    @PostMapping
    public ResponseEntity<DroneResponse> create(@Valid @RequestBody DroneCreateRequest req) {
        DroneResponse created = droneService.createDrone(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // @PreAuthorize("hasRole('RESTAURANT') or hasRole('ADMIN')")
    @PutMapping("/{code}")
    public ResponseEntity<DroneResponse> update(@PathVariable String code, @Valid @RequestBody DroneUpdateRequest req) {
        DroneResponse updated = droneService.updateDrone(code, req);
        return ResponseEntity.ok(updated);
    }

    // @PreAuthorize("hasRole('DRONE_SYSTEM') or hasRole('ADMIN')")
    @PostMapping("/{id}/location")
    public ResponseEntity<DroneResponse> updateLocation(@PathVariable Long id, @Valid @RequestBody DroneLocationRequest req) {
        DroneResponse res = droneService.updateDroneLocation(id, req);
        return ResponseEntity.ok(res);
    }
}
