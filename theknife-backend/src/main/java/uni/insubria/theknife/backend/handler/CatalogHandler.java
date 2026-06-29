/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uni.insubria.theknife.backend.entity.RestaurantEntity;
import uni.insubria.theknife.backend.entity.ReviewEntity;
import uni.insubria.theknife.backend.mapper.RestaurantMapper;
import uni.insubria.theknife.backend.mapper.ReviewMapper;
import uni.insubria.theknife.backend.repository.RestaurantRepository;
import uni.insubria.theknife.backend.repository.ReviewRepository;
import uni.insubria.theknife.backend.ws.ConnectionState;
import uni.insubria.theknife.backend.ws.HandlerResult;
import uni.insubria.theknife.common.dto.RestaurantDto;
import uni.insubria.theknife.common.dto.ReviewDto;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only catalog queries: the full restaurant list (with embedded reviews), a
 * single restaurant, and the distinct locations / cuisines used by the client filters.
 */
@ApplicationScoped
public class CatalogHandler {

    @Inject
    RestaurantRepository restaurants;
    @Inject
    ReviewRepository reviews;
    @Inject
    ObjectMapper mapper;

    @Transactional
    public HandlerResult listRestaurants(Envelope req) {
        // The client can only list restaurants filtered by city/location: this keeps the
        // payload small (a single city) instead of shipping the whole ~17k catalog (~18 MB).
        String location = req.payload() == null ? null : req.payload().path("location").asText(null);
        List<RestaurantEntity> entities = (location == null || location.isBlank())
                ? List.of()
                : restaurants.findByLocation(location);

        Map<String, List<ReviewDto>> byRestaurant = reviews.listAll().stream()
                .map(ReviewMapper::toDto)
                .collect(Collectors.groupingBy(ReviewDto::restaurantId));

        List<RestaurantDto> result = new ArrayList<>();
        for (RestaurantEntity e : entities) {
            result.add(RestaurantMapper.toDto(e, byRestaurant.getOrDefault(e.id, List.of())));
        }
        return HandlerResult.of(Envelope.ok(Action.LIST_RESTAURANTS, req.correlationId(),
                mapper.valueToTree(result)));
    }

    @Transactional
    public HandlerResult listMyRestaurants(Envelope req, ConnectionState state) {
        // A RISTORATORE sees the restaurants they own, regardless of city.
        if (!state.isAuthenticated()) {
            return HandlerResult.of(Envelope.error(Action.LIST_MY_RESTAURANTS, req.correlationId(),
                    ErrorCode.UNAUTHORIZED, "Non autenticato"));
        }
        Map<String, List<ReviewDto>> byRestaurant = reviews.listAll().stream()
                .map(ReviewMapper::toDto)
                .collect(Collectors.groupingBy(ReviewDto::restaurantId));

        List<RestaurantDto> result = new ArrayList<>();
        for (RestaurantEntity e : restaurants.findByOwner(state.username())) {
            result.add(RestaurantMapper.toDto(e, byRestaurant.getOrDefault(e.id, List.of())));
        }
        return HandlerResult.of(Envelope.ok(Action.LIST_MY_RESTAURANTS, req.correlationId(),
                mapper.valueToTree(result)));
    }

    @Transactional
    public HandlerResult getRestaurant(Envelope req) {
        String id = req.payload() == null ? null : req.payload().path("id").asText(null);
        if (id == null) {
            return HandlerResult.of(Envelope.error(Action.GET_RESTAURANT, req.correlationId(),
                    ErrorCode.VALIDATION, "Id mancante"));
        }
        RestaurantEntity e = restaurants.findById(id);
        if (e == null) {
            return HandlerResult.of(Envelope.error(Action.GET_RESTAURANT, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Ristorante non trovato"));
        }
        List<ReviewDto> rv = reviews.findByRestaurant(id).stream().map(ReviewMapper::toDto).toList();
        return HandlerResult.of(Envelope.ok(Action.GET_RESTAURANT, req.correlationId(),
                mapper.valueToTree(RestaurantMapper.toDto(e, rv))));
    }

    @Transactional
    public HandlerResult getLocations(Envelope req) {
        return HandlerResult.of(Envelope.ok(Action.GET_LOCATIONS, req.correlationId(),
                mapper.valueToTree(restaurants.distinctLocations())));
    }

    @Transactional
    public HandlerResult getCuisines(Envelope req) {
        return HandlerResult.of(Envelope.ok(Action.GET_CUISINES, req.correlationId(),
                mapper.valueToTree(restaurants.distinctCuisines())));
    }
}
