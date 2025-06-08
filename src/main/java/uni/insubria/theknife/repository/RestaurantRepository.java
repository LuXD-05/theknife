package uni.insubria.theknife.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;

/**
 * This class provides methods to interact with the repository of Restaurant objects.
 */
public class RestaurantRepository {

    /**
     * Adds a new restaurant to the repository if it does not already exist.
     *
     * @param restaurant the restaurant to add
     * @return ERROR_CODE an error code indicating the result of the operation:
     *                   - DUPLICATED if the restaurant already exists in the repository
     *                   - SERVICE_ERROR if an error occurs during saving the repository
     *                   - NONE if the restaurant is successfully added
     */
    public static ERROR_CODE addRestaurant(Restaurant restaurant) {
        List<Restaurant> restaurants = loadRestaurants();
        for (Restaurant existingRestaurant : restaurants) {
            if (existingRestaurant.equals(restaurant)) {
                return ERROR_CODE.DUPLICATED;
            }
        }
        restaurants.add(restaurant);
        try {
            saveRestaurants(restaurants.stream().collect(Collectors.toMap(Restaurant::getName, Function.identity())));
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
        return ERROR_CODE.NONE;
    }

    /**
     * Path to the CSV file containing restaurant data.
     * Note that the path should start with '/', otherwise the InputStream will be null.
     */
    private static final String RESTAURANTS_CSV = "/data/michelin_my_maps.csv";
    /**
     * Represents the file path of the JSON data for restaurants.
     */
    private static final String RESTAURANTS_JSON = "/data/restaurants.json";

    /**
     * Private static final instance of ObjectMapper used for JSON serialization and deserialization.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();




    /**
     * Saves the provided map of restaurants to a JSON file.
     *
     * @param restaurants a map containing restaurant names as keys and corresponding Restaurant object values
     * @throws IOException if an I/O error occurs during file writing
     */
    public static void saveRestaurants(Map<String, Restaurant> restaurants) throws IOException {

        //TODO - Verify if the path to the file (all folders) exist --> IF NOT, create make them

        // Writes the json file
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
     * @return A list of Restaurant objects representing the restaurants data loaded from JSON file
     */
    public static List<Restaurant> loadRestaurants() {
        try {
            FileInputStream inputStream = new FileInputStream(RESTAURANTS_JSON);
            return Arrays.asList(objectMapper.readValue(inputStream, Restaurant[].class));
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
        List<Restaurant> restaurants = loadRestaurants();
        for (int i = 0; i < restaurants.size(); i++) {
            if (restaurants.get(i).equals(restaurant)) {
                restaurants.set(i, restaurant);
                try {
                    saveRestaurants(restaurants.stream().collect(Collectors.toMap(Restaurant::getName, Function.identity())));
                } catch (IOException e) {
                    return ERROR_CODE.SERVICE_ERROR;
                }
        return ERROR_CODE.NONE;
    }
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
        List<Restaurant> restaurants = loadRestaurants();
        boolean removed = restaurants.removeIf(existingRestaurant -> existingRestaurant.equals(restaurant));

        if (removed) {
            try {
                saveRestaurants(restaurants.stream().collect(Collectors.toMap(Restaurant::getName, Function.identity())));
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
