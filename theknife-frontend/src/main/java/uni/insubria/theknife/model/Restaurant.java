/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.model;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a restaurant in the TheKnife client.
 * <p>
 * In the client/server architecture this is the in-memory model used by the JavaFX
 * views; it is populated from the {@code RestaurantDto} received over the WebSocket.
 * Equality is based on the id, so favorites ({@code HashSet<Restaurant>}) and lookups
 * keep working across DTO round-trips.
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurant {
    /**
     * Default constructor for the Restaurant class.
     */
    public Restaurant() {
        // Default constructor - fields will be initialized with default values
    }

    /**
     * The unique identifier for this restaurant. Used for equality checks.
     */
    @EqualsAndHashCode.Include
    String id;

    /** The name of the restaurant. */
    String name;

    /** The physical address of the restaurant. */
    String address;

    /** The city or location where the restaurant is situated. */
    String location;

    /** The price range or category of the restaurant. */
    String price;

    /** The type of cuisine offered by the restaurant. */
    String cuisine;

    /** The longitude coordinate of the restaurant's location. */
    Float longitude;

    /** The latitude coordinate of the restaurant's location. */
    Float latitude;

    /** The contact phone number of the restaurant. */
    String phone;

    /** The URL to the restaurant's page on the Michelin website. */
    String michelinUrl;

    /** The URL to the restaurant's official website. */
    String websiteUrl;

    /** The Michelin award or recognition received by the restaurant. */
    String award;

    /** The number of Michelin Green Stars awarded to the restaurant for sustainability. */
    Integer greenStar;

    /** The facilities and services offered by the restaurant. */
    String facilities;

    /** A detailed description of the restaurant. */
    String description;

    /** The calculated distance from a reference point to this restaurant in kilometers. */
    Double distance;

    /** The list of reviews associated with this restaurant. */
    List<Review> reviews = new ArrayList<>();

    /** The user who owns or manages this restaurant, if applicable. */
    User user = null;

    /**
     * Represents geographical coordinates for a restaurant location.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    public static class Coordinate {
        /** The longitude coordinate (east-west position). */
        private Float longitude;

        /** The latitude coordinate (north-south position). */
        private Float latitude;
    }

}
