/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.model;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a restaurant in the TheKnife application.
 * <p>
 * This class contains all the information related to a restaurant, including its
 * basic details (name, address, location), cuisine information, geographical coordinates,
 * contact information, and Michelin-specific data such as awards and green stars.
 * </p>
 * <p>
 * Restaurants can be loaded from CSV files and are the central entity that users
 * can browse, review, and save as favorites.
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurant {
    /**
     * Default constructor for the Restaurant class.
     * <p>
     * Creates a new Restaurant instance with default values for all fields.
     * The Lombok @Setter and @Accessors(chain = true) annotations allow for
     * fluent setting of properties after construction.
     * </p>
     */
    public Restaurant() {
        // Default constructor - fields will be initialized with default values
    }
    /**
     * The unique identifier for this restaurant.
     * This field is used for equality checks.
     */
    @EqualsAndHashCode.Include
    String id;

    /**
     * The name of the restaurant.
     */
    @CsvBindByName(column = "Name")
    String name;

    /**
     * The physical address of the restaurant.
     */
    @CsvBindByName(column = "Address")
    String address;

    /**
     * The city or location where the restaurant is situated.
     */
    @CsvBindByName(column = "Location")
    String location;

    /**
     * The price range or category of the restaurant.
     */
    @CsvBindByName(column = "Price")
    String price;

    /**
     * The type of cuisine offered by the restaurant.
     */
    @CsvBindByName(column = "Cuisine")
    String cuisine;

    /**
     * The longitude coordinate of the restaurant's location.
     */
    @CsvBindByName(column = "Longitude")
    Float longitude;

    /**
     * The latitude coordinate of the restaurant's location.
     */
    @CsvBindByName(column = "Latitude")
    Float latitude;

    /**
     * The contact phone number of the restaurant.
     */
    @CsvBindByName(column = "PhoneNumber")
    String phone;

    /**
     * The URL to the restaurant's page on the Michelin website.
     */
    @CsvBindByName(column = "Url")
    String michelinUrl;

    /**
     * The URL to the restaurant's official website.
     */
    @CsvBindByName(column = "WebsiteUrl")
    String websiteUrl;

    /**
     * The Michelin award or recognition received by the restaurant.
     */
    @CsvBindByName(column = "Award")
    String award;

    /**
     * The number of Michelin Green Stars awarded to the restaurant for sustainability.
     */
    @CsvBindByName(column = "GreenStar")
    Integer greenStar;

    /**
     * The facilities and services offered by the restaurant.
     */
    @CsvBindByName(column = "FacilitiesAndServices")
    String facilities;

    /**
     * A detailed description of the restaurant.
     */
    @CsvBindByName(column = "Description")
    String description;

    /**
     * The calculated distance from a reference point to this restaurant in kilometers.
     */
    Double distance;

    /**
     * The list of reviews associated with this restaurant.
     */
    List<Review> reviews = new ArrayList<>();

    /**
     * The user who owns or manages this restaurant, if applicable.
     */
    User user = null;

    /**
     * Represents geographical coordinates for a restaurant location.
     * <p>
     * This nested class is used to store and manipulate longitude and latitude
     * coordinates, particularly for distance calculations between restaurants.
     * </p>
     * <p>
     * The class uses Lombok's @AllArgsConstructor to generate a constructor that
     * takes longitude and latitude parameters.
     * </p>
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    public static class Coordinate {
        /**
         * The longitude coordinate (east-west position).
         */
        private Float longitude;

        /**
         * The latitude coordinate (north-south position).
         */
        private Float latitude;
    }

}
