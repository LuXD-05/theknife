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
 * This class provides methods to interact with the repository of Restaurant objects.
 */
public class RestaurantRepository {
    //TODO GITHUB TASK #9
    //Aggiungere/Modificare/Eliminare ristoranti preferiti
    //TODO GITHUB TASK #11
    //transform .CSV to JSON and add/edit/delete review
    private static final String RESTAURANTS_CSV = "/data/michelin_my_maps.csv";
    private static final String RESTAURANTS_JSON;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        if (ReviewsRepository.class.getClassLoader().getResource("data/restaurants.json") != null) {
            RESTAURANTS_JSON = Objects.requireNonNull(ReviewsRepository.class.getClassLoader().getResource("data/restaurants.json")).getFile();
        } else {
            RESTAURANTS_JSON = new File(Objects.requireNonNull(ReviewsRepository.class.getClassLoader().getResource("data")).getFile(), "restaurants.json").getAbsolutePath();
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
        try (InputStream is = RestaurantRepository.class.getResourceAsStream(RESTAURANTS_CSV)) {
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

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }
}
