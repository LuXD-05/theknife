package uni.insubria.theknife.repository;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import uni.insubria.theknife.model.Restaurant;

/**
 * Repository for managing restaurant data in the TheKnife application.
 * <p>
 * This class provides methods to interact with the repository of Restaurant objects,
 * including loading restaurants from CSV and JSON files, and performing CRUD operations
 * (Create, Read, Update, Delete) on restaurant data.
 * </p>
 * <p>
 * The repository handles data persistence and serves as the data access layer
 * for restaurant-related operations in the application.
 * </p>
 */
public class RestaurantRepository {
    /**
     * Default constructor for the RestaurantRepository class.
     * <p>
     * This constructor is not meant to be used directly as this class only provides
     * static methods. The class is not designed to be instantiated.
     * </p>
     */
    public RestaurantRepository() {
        // Default constructor - not meant to be used
    }
    //TODO GITHUB TASK #9
    //Aggiungere/Modificare/Eliminare ristoranti preferiti
    //TODO GITHUB TASK #11
    //transform .CSV to JSON and add/edit/delete review

    /**
     * Path to the CSV file containing the initial restaurant data.
     */
    private static final String RESTAURANTS_CSV = "data/michelin_my_maps.csv";

    /**
     * Path to the JSON file used for storing and retrieving restaurant data.
     */
    private static final String RESTAURANTS_JSON = "data/restaurants.json";

    /**
     * Jackson ObjectMapper instance used for JSON serialization and deserialization.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Saves the provided map of restaurants to a JSON file.
     *
     * @param restaurants a map containing restaurant names as keys and corresponding Restaurant object values
     * @throws IOException if an I/O error occurs during file writing
     */
    public static void saveRestaurants(Map<String, Restaurant> restaurants) throws IOException {
        objectMapper.writeValue(new File(RESTAURANTS_JSON), restaurants);

    }

    /**
     * Loads a list of restaurants from a CSV file.
     *
     * @return A list of Restaurant objects parsed from the CSV file.
     */
    static public List<Restaurant> loadRestaurantsCSV() {
        try (InputStream is = new FileInputStream(RESTAURANTS_CSV)) {
            Reader reader = new StringReader(new String(Objects.requireNonNull(is).readAllBytes()));
            CsvToBean<Restaurant> cb = new CsvToBeanBuilder<Restaurant>(reader)
                    .withType(Restaurant.class)
                    .build();
            return new ArrayList<>(cb.parse());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the restaurants from the .json file
     *
     * @return A list of Restaurant objects representing the restaurant data loaded from the JSON file
     */
    public static Map<String, Restaurant> loadRestaurants() {
        try {
            File file = new File(RESTAURANTS_JSON);
            if (!file.exists()) {
                Map<String, Restaurant> restaurants = loadRestaurantsCSV().stream().map(restaurant -> {
                    String id = String.valueOf(Objects.hash(restaurant.getName(), restaurant.getLatitude(), restaurant.getLongitude()));
                    return restaurant
                            .setId(id)
                            .setReviews(ReviewsRepository.reviewsByRestaurant(restaurant));

                }).collect(Collectors.toMap(Restaurant::getId, Function.identity()));
                saveRestaurants(restaurants);
                return restaurants;
            }
            Map<String, Restaurant> restaurants = objectMapper.readValue(new FileInputStream(file), new TypeReference<>() {
            });
            restaurants.keySet().forEach(key -> {
                Restaurant restaurant = objectMapper.convertValue(restaurants.get(key), Restaurant.class);
                restaurant.setReviews(ReviewsRepository.reviewsByRestaurant(restaurant));
                restaurants.put(key, restaurant);
            });
            return restaurants;
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento delle recensioni", e);
        }
    }


    /**
     * Adds a new restaurant to the repository if it does not already exist.
     *
     * @param restaurant the restaurant to add
     * @return ERROR_CODE an error code indicating the result of the operation:
     * - DUPLICATED if the restaurant already exists in the repository
     * - SERVICE_ERROR if an error occurs during saving the repository
     * - NONE if the restaurant is successfully added
     */
    public static ERROR_CODE addRestaurant(Restaurant restaurant) {
        Map<String, Restaurant> restaurants = loadRestaurants();
        String id = String.valueOf(Objects.hash(restaurant.getName(), restaurant.getLatitude(), restaurant.getLongitude()));
        if (restaurants.containsKey(id)) {
            return ERROR_CODE.DUPLICATED;
        }
        restaurants.put(id, restaurant.setId(id));
        try {
            saveRestaurants(restaurants);
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
        return ERROR_CODE.NONE;
    }


    /**
     * Edits the provided Restaurant object within the list of restaurants.
     *
     * @param restaurant The Restaurant object to edit.
     * @return ERROR_CODE.NONE if the restaurant was successfully edited, ERROR_CODE.SERVICE_ERROR if an error occurred.
     */
    public static ERROR_CODE editRestaurant(Restaurant restaurant) {
        Map<String, Restaurant> restaurants = loadRestaurants();
        if (restaurants.containsKey(restaurant.getId())) {
            restaurants.put(restaurant.getId(), restaurant);
            try {
                saveRestaurants(restaurants);
            } catch (IOException e) {
                return ERROR_CODE.SERVICE_ERROR;
            }
            return ERROR_CODE.NONE;
        }
        return ERROR_CODE.SERVICE_ERROR;
    }

    /**
     * Deletes the specified restaurant from the list of restaurants.
     *
     * @param restaurant the restaurant to delete
     * @return ERROR_CODE representing the outcome of the deletion operation:
     * - DUPLICATED if the restaurant is duplicated
     * - SERVICE_ERROR if an error occurred during saving the changes
     * - NONE if the operation was successful
     */
    public static ERROR_CODE deleteRestaurant(Restaurant restaurant) {
        Map<String, Restaurant> restaurants = loadRestaurants();
        if (restaurants.remove(restaurant.getId()) != null) {
            try {
                saveRestaurants(restaurants);
            } catch (IOException e) {
                return ERROR_CODE.SERVICE_ERROR;
            }
            return ERROR_CODE.NONE;
        }
        return ERROR_CODE.SERVICE_ERROR;
    }

    /**
     * Enumeration of possible error codes returned by repository operations.
     */
    public enum ERROR_CODE {
        /**
         * Indicates that the operation failed because the entity already exists.
         */
        DUPLICATED,

        /**
         * Indicates that a service-level error occurred during the operation.
         */
        SERVICE_ERROR,

        /**
         * Indicates that the operation completed successfully with no errors.
         */
        NONE
    }
}
