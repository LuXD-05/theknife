/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import uni.insubria.theknife.client.BackendClient;
import uni.insubria.theknife.client.DtoMapper;
import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.common.dto.RestaurantDto;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Client-side restaurant repository, backed by the WebSocket backend. Method
 * signatures are preserved; searching/filtering remains client-side on the cached list.
 */
public class RestaurantRepository {

    public RestaurantRepository() {
        // Not meant to be instantiated
    }

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }

    /**
     * Loads the whole restaurant catalog (with embedded reviews) from the backend.
     */
    public static Map<String, Restaurant> loadRestaurants() {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.LIST_RESTAURANTS, null);
            if (response.error() != ErrorCode.NONE || response.payload() == null) {
                throw new RuntimeException("Errore nel caricamento dei ristoranti");
            }
            List<RestaurantDto> dtos = BackendClient.get().mapper()
                    .convertValue(response.payload(), new TypeReference<List<RestaurantDto>>() {
                    });
            Map<String, Restaurant> map = new LinkedHashMap<>();
            for (Restaurant r : DtoMapper.toModelList(dtos)) {
                map.put(r.getId(), r);
            }
            return map;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Errore nel caricamento dei ristoranti", e);
        }
    }

    /**
     * Filters a list of restaurants by name (case-insensitive) and the active filters.
     * Unchanged from the original: this stays client-side on the in-memory list.
     */
    public static List<Restaurant> searchRestaurants(List<Restaurant> restaurants, String searchQuery) {
        if (restaurants == null || restaurants.isEmpty()) {
            return List.of();
        }
        FilterOptions filters = SessionService.getFilters();
        String queryLower = (searchQuery != null) ? searchQuery.toLowerCase().trim() : "";
        return restaurants.stream()
                .filter(r -> queryLower.isEmpty() || r.getName().toLowerCase().contains(queryLower))
                .filter(r -> filters == null || filters.matches(r))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new restaurant (RISTORATORE). The server assigns the id, which is written
     * back onto the supplied object, and the cache is updated.
     */
    public static ERROR_CODE addRestaurant(Restaurant restaurant) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.ADD_RESTAURANT, DtoMapper.toDto(restaurant));
            if (response.error() != ErrorCode.NONE) {
                return map(response.error());
            }
            if (response.payload() != null) {
                RestaurantDto created = BackendClient.get().mapper().convertValue(response.payload(), RestaurantDto.class);
                restaurant.setId(created.id());
            }
            SessionService.getRestaurants().add(restaurant);
            return ERROR_CODE.NONE;
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    /**
     * Persists edits to an existing restaurant (owner only).
     */
    public static ERROR_CODE editRestaurant(Restaurant restaurant) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.EDIT_RESTAURANT, DtoMapper.toDto(restaurant));
            return map(response.error());
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    /**
     * Deletes a restaurant (owner only) and removes it from the cache.
     */
    public static ERROR_CODE deleteRestaurant(Restaurant restaurant) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.DELETE_RESTAURANT, DtoMapper.toDto(restaurant));
            ERROR_CODE code = map(response.error());
            if (code == ERROR_CODE.NONE) {
                SessionService.getRestaurants().removeIf(r -> r.getId().equals(restaurant.getId()));
            }
            return code;
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    /**
     * Legacy id helper kept for compatibility; the server now assigns ids for new
     * restaurants, so this value is only a local placeholder.
     */
    public static String generateUniqueId(Restaurant restaurant) {
        return String.valueOf(Objects.hash(restaurant.getName(), restaurant.getLatitude(), restaurant.getLongitude()));
    }

    private static ERROR_CODE map(ErrorCode code) {
        if (code == null) {
            return ERROR_CODE.SERVICE_ERROR;
        }
        return switch (code) {
            case NONE -> ERROR_CODE.NONE;
            case DUPLICATED -> ERROR_CODE.DUPLICATED;
            default -> ERROR_CODE.SERVICE_ERROR;
        };
    }
}
