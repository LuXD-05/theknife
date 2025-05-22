package uni.insubria.theknife.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.User;

public class RestaurantRepository {

    //! NOT WORKING if path does not start with '/' (InputStream is null)
    private static final String RESTAURANTS_CSV = "/data/michelin_my_maps.csv";
    private static final String RESTAURANTS_JSON = "/data/restaurants.json";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Saves restaurants in json file (sorts them by id before that --> needed to get last id on add)
     * @param restaurants list of restaurants to save
     * @throws IOException
     */
    public static void saveRestaurants(List<Restaurant> restaurants) throws IOException {
        // Sorts collection before saving
        Collections.sort(restaurants, Comparator.comparing(Restaurant::getId));
        // Saves
        FileWriter fw = new FileWriter(RESTAURANTS_JSON, false);
        fw.write(objectMapper.writeValueAsString(restaurants));
        fw.close();
    }

    /**
     * Loads the restaurants from the .csv file
     *
     * @return A list of Restaurant objects
     */
    public static List<Restaurant> loadRestaurantsCSV() {
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
     * @return A list of Restaurant objects
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
     * Adds a restaurant in json db
     * @param restaurant the restaurant object to add
     * @return
     */
    public static ERROR_CODE addRestaurant(Restaurant restaurant) {
        
        // Get all restaurants
        List<Restaurant> restaurants = loadRestaurants();

        // Check for duplicates in list
        if (restaurants.stream().anyMatch(x -> x.equals(restaurant))) 
            return ERROR_CODE.DUPLICATED;

        // Gets last restaurant id (in order)
        int lastId = restaurants.isEmpty() ? 0 : restaurants.get(restaurants.size() - 1).getId();

        // Sets incremented id + adds to list
        restaurant.setId(lastId + 1);
        restaurants.add(restaurant);

        // Updates restaurants db or error
        try {
            saveRestaurants(restaurants);
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }

        return ERROR_CODE.NONE;

    }

    /**
     * Edits a restaurant
     * @param restaurant the restaurant object that will be edited
     * @return
     */
    public static ERROR_CODE editRestaurant(Restaurant restaurant) {

        // Get all restaurants
        List<Restaurant> restaurants = loadRestaurants();

        // Removes old restaurant (if id matches)
        if (!restaurants.removeIf(r -> r.getId() == restaurant.getId()))
            return ERROR_CODE.SERVICE_ERROR; //? ERROR_CODE.NOT_FOUND meglio ???

        // Adds updated restaurant
        restaurants.add(restaurant);

        // Updates restaurants db or error
        try {
            saveRestaurants(restaurants);
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }

        return ERROR_CODE.NONE;

    }

    /**
     * Deletes a restaurant
     * @param restaurant the restaurant object that will be deleted
     * @return
     */
    public static ERROR_CODE deleteRestaurant(Restaurant restaurant) {
        
        // Get all restaurants
        List<Restaurant> restaurants = loadRestaurants();

        // Removes old restaurant (if id matches)
        if (!restaurants.removeIf(r -> r.getId() == restaurant.getId()))
            return ERROR_CODE.SERVICE_ERROR; //? ERROR_CODE.NOT_FOUND meglio ???

        // Updates restaurants db or error
        try {
            saveRestaurants(restaurants);
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }

        return ERROR_CODE.NONE;

    }

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }
}
