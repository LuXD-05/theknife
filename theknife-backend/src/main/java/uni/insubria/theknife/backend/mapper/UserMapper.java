/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.mapper;

import uni.insubria.theknife.backend.entity.UserEntity;
import uni.insubria.theknife.common.dto.UserDto;

import java.util.Set;

/**
 * Converts {@link UserEntity} to the wire {@link UserDto}. The password hash is
 * never copied across.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(UserEntity e, Set<String> favoriteRestaurantIds) {
        return new UserDto(
                e.username,
                e.firstName,
                e.lastName,
                e.birthDate,
                e.city,
                e.role,
                favoriteRestaurantIds
        );
    }
}
