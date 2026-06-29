/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.repository;

import uni.insubria.theknife.client.BackendClient;
import uni.insubria.theknife.client.DtoMapper;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.common.dto.LoginRequest;
import uni.insubria.theknife.common.dto.RegisterRequest;
import uni.insubria.theknife.common.dto.UserDto;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

/**
 * Client-side user repository. Operations are forwarded to the backend over the
 * WebSocket; the original static method signatures are preserved so the controllers
 * are essentially unchanged.
 */
public class UserRepository {

    public UserRepository() {
        // Not meant to be instantiated
    }

    /**
     * Outcome codes, kept identical to the original so controller branches still compile.
     */
    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }

    /**
     * Authenticates against the backend (password validated server-side).
     *
     * @return the logged-in {@link User} on success, or null on failure
     */
    public static User login(String username, String password) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.LOGIN, new LoginRequest(username, password));
            if (response.error() == ErrorCode.NONE && response.payload() != null) {
                UserDto dto = BackendClient.get().mapper().convertValue(response.payload(), UserDto.class);
                return DtoMapper.toModel(dto, UserRepository::resolveRestaurant);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Registers a new user. The plaintext password is sent and hashed server-side.
     */
    public static ERROR_CODE addUser(User user) {
        try {
            RegisterRequest request = new RegisterRequest(
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPassword(),
                    user.getBirthDate(),
                    user.getCity(),
                    user.getRole() == null ? null
                            : uni.insubria.theknife.common.dto.Role.valueOf(user.getRole().name()));
            Envelope response = BackendClient.get().sendAndWait(Action.REGISTER, request);
            return map(response.error());
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    /**
     * Adds/removes a restaurant from the current user's favorites. Keeps the in-memory
     * user's favorite set in sync with the server response.
     */
    public static ERROR_CODE toggleFavoriteRestaurant(User user, Restaurant restaurant) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.TOGGLE_FAVORITE,
                    java.util.Map.of("restaurantId", restaurant.getId()));
            if (response.error() != ErrorCode.NONE) {
                return map(response.error());
            }
            boolean favorited = response.payload() != null && response.payload().path("favorited").asBoolean(false);
            if (favorited) {
                user.getRestaurants().add(restaurant);
            } else {
                user.getRestaurants().remove(restaurant);
            }
            return ERROR_CODE.NONE;
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    private static Restaurant resolveRestaurant(String id) {
        return SessionService.getRestaurants().stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst()
                .orElse(null);
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
