package com.fast_food_frontend.service.impl;

import com.fast_food_frontend.common.IdGenerator;
import com.fast_food_frontend.common.SearchHelper;
import com.fast_food_frontend.dto.request.RestaurantRequest;
import com.fast_food_frontend.dto.response.ListResponse;
import com.fast_food_frontend.dto.response.RestaurantResponse;
import com.fast_food_frontend.entity.Address;
import com.fast_food_frontend.entity.Restaurant;
import com.fast_food_frontend.entity.User;
import com.fast_food_frontend.enums.AddressTypeStatus;
import com.fast_food_frontend.enums.RestaurantStatus;
import com.fast_food_frontend.exception.AppException;
import com.fast_food_frontend.exception.ErrorCode;
import com.fast_food_frontend.mapper.RestaurantMapper;
import com.fast_food_frontend.repository.RestaurantRepository;
import com.fast_food_frontend.repository.UserRepository;
import com.fast_food_frontend.service.IRestaurantService;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RestaurantServiceImpl implements IRestaurantService {
    RestaurantRepository restaurantRepository;
    UserRepository userRepository;
    RestaurantMapper restaurantMapper;

    private static final List<String> SEARCH_FIELDS = List.of("name", "address_id", "rating", "status");

    @Override
    public RestaurantResponse createRestaurant(Long ownerId, RestaurantRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.DATASOURCE_NOT_FOUND));

        Address address = Address.builder()
                .id(IdGenerator.generateRandomId())
                .user(owner)
                .street(request.getAddress().getStreet())
                .city(request.getAddress().getCity())
                .latitude(request.getAddress().getLatitude())
                .longitude(request.getAddress().getLongitude())
                .type(AddressTypeStatus.RESTAURANT.name())
                .build();

        Restaurant restaurant = restaurantMapper.toRestaurant(request);
        restaurant.setId(IdGenerator.generateRandomId());
        restaurant.setOwner(owner);
        restaurant.setRating(BigDecimal.ONE);
        restaurant.setStatus(RestaurantStatus.OPEN.name());
        restaurant.setAddress(address);
        restaurant.setCreatedAt(Instant.now());
        restaurant.setUpdatedAt(Instant.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toRestaurantResponse(savedRestaurant);

    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, Long ownerId, RestaurantRequest req) {
        log.info("request: {}, {}, {}", id, ownerId, req);
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATASOURCE_NOT_FOUND));

        if (!Objects.equals(restaurant.getOwner().getId(), ownerId)) {
            log.error("restaurant owner: {} and ownerId: {}", restaurant.getOwner().getId(), ownerId);
            throw new AppException(ErrorCode.UNAUTHORIZED_TO_UPDATE_THIS_RESOURCE);
        }
        restaurantMapper.updateRestaurant(restaurant, req);
        restaurant.setUpdatedAt(Instant.now());
        restaurantRepository.save(restaurant);

        return restaurantMapper.toRestaurantResponse(restaurant);
    }

    @Override
    public ListResponse<RestaurantResponse> getListRestaurants(int page, int size, String sort, String filter, String search, boolean all) {
        Specification<Restaurant> sortable = RSQLJPASupport.toSort(sort);
        Specification<Restaurant> filterable = RSQLJPASupport.toSpecification(filter);
        Specification<Restaurant> searchable = SearchHelper.parseSearchToken(search, SEARCH_FIELDS);
        Pageable pageable = all ? Pageable.unpaged() : PageRequest.of(page - 1, size);
        Page<RestaurantResponse> responses = restaurantRepository
                .findAll(sortable.and(filterable).and(searchable), pageable)
                .map(restaurantMapper::toRestaurantResponse);

        return ListResponse.of(responses);
    }
}
