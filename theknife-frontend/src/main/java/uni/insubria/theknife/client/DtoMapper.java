/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.client;

import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.common.dto.RestaurantDto;
import uni.insubria.theknife.common.dto.ReviewDto;
import uni.insubria.theknife.common.dto.UserDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * Converts the wire DTOs (shared module) into the JavaFX client model objects and back.
 * Keeps the controllers and FXML working on the existing {@code model.*} classes while
 * the transport speaks DTOs.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    // --- Restaurant ---

    public static Restaurant toModel(RestaurantDto dto) {
        Restaurant r = new Restaurant()
                .setId(dto.id())
                .setName(dto.name())
                .setAddress(dto.address())
                .setLocation(dto.location())
                .setPrice(dto.price())
                .setCuisine(dto.cuisine())
                .setLongitude(dto.longitude() == null ? null : dto.longitude().floatValue())
                .setLatitude(dto.latitude() == null ? null : dto.latitude().floatValue())
                .setPhone(dto.phone())
                .setMichelinUrl(dto.michelinUrl())
                .setWebsiteUrl(dto.websiteUrl())
                .setAward(dto.award())
                .setGreenStar(dto.greenStar())
                .setFacilities(dto.facilities())
                .setDescription(dto.description());
        if (dto.ownerUsername() != null) {
            r.setUser(new User().setUsername(dto.ownerUsername()));
        }
        List<Review> reviews = new ArrayList<>();
        if (dto.reviews() != null) {
            for (ReviewDto rv : dto.reviews()) {
                reviews.add(toModel(rv, r));
            }
        }
        r.setReviews(reviews);
        return r;
    }

    public static List<Restaurant> toModelList(List<RestaurantDto> dtos) {
        List<Restaurant> list = new ArrayList<>(dtos.size());
        for (RestaurantDto dto : dtos) {
            list.add(toModel(dto));
        }
        return list;
    }

    public static RestaurantDto toDto(Restaurant r) {
        return new RestaurantDto(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getLocation(),
                r.getPrice(),
                r.getCuisine(),
                r.getLongitude() == null ? null : r.getLongitude().doubleValue(),
                r.getLatitude() == null ? null : r.getLatitude().doubleValue(),
                r.getPhone(),
                r.getMichelinUrl(),
                r.getWebsiteUrl(),
                r.getAward(),
                r.getGreenStar(),
                r.getFacilities(),
                r.getDescription(),
                r.getUser() == null ? null : r.getUser().getUsername(),
                List.of()
        );
    }

    // --- Review ---

    public static Review toModel(ReviewDto dto, Restaurant restaurant) {
        return new Review()
                .setId(dto.id())
                .setUser(new User().setUsername(dto.username()))
                .setRestaurant(restaurant)
                .setContent(dto.content())
                .setStars(dto.stars())
                .setAnswer(dto.answer());
    }

    public static ReviewDto toDto(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getUser() == null ? null : r.getUser().getUsername(),
                r.getRestaurant() == null ? null : r.getRestaurant().getId(),
                r.getContent(),
                r.getStars(),
                r.getAnswer()
        );
    }

    // --- User ---

    /**
     * @param dto      the user DTO
     * @param resolver maps a restaurant id to the full cached Restaurant (may return null)
     */
    public static User toModel(UserDto dto, Function<String, Restaurant> resolver) {
        User user = new User()
                .setUsername(dto.username())
                .setFirstName(dto.firstName())
                .setLastName(dto.lastName())
                .setBirthDate(dto.birthDate())
                .setCity(dto.city())
                .setRole(dto.role() == null ? null : Role.valueOf(dto.role().name()));
        HashSet<Restaurant> favorites = new HashSet<>();
        if (dto.favoriteRestaurantIds() != null) {
            for (String id : dto.favoriteRestaurantIds()) {
                Restaurant resolved = resolver == null ? null : resolver.apply(id);
                favorites.add(resolved != null ? resolved : new Restaurant().setId(id));
            }
        }
        user.setRestaurants(favorites);
        return user;
    }
}
