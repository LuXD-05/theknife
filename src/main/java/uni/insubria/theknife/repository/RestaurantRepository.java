package uni.insubria.theknife.repository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;

public class RestaurantRepository {

    //! NOT WORKING if path does not start with '/' (InputStream is null)
    private static final String RESTAURANTS_CSV = "/data/michelin_my_maps.csv";
    private static final String RESTAURANTS_JSON = "/data/restaurants.json";

    private static final ObjectMapper objectMapper = new ObjectMapper();




    /**
     * @param restaurants
     * @throws IOException
     */
    public void saveRestaurants(Map<String, Restaurant> restaurants) throws IOException {

        //TODO - Verify if the path to the file (all folders) exist --> IF NOT, create make them

        // Writes the json file
        objectMapper.writeValue(new File(RESTAURANTS_JSON), restaurants);

    }

    /**
     * Loads the restaurants from the .csv file
     *
     * @return A list of Restaurant objects
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

    //TODO
    public static ERROR_CODE addRestaurant(Restaurant restaurant) {
        return ERROR_CODE.NONE;
    }

    //TODO
    public static ERROR_CODE editRestaurant(Restaurant restaurant) {
        return ERROR_CODE.NONE;
    }

    //TODO
    public static ERROR_CODE deleteRestaurant(Restaurant restaurant) {
        return ERROR_CODE.NONE;
    }

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }
}
