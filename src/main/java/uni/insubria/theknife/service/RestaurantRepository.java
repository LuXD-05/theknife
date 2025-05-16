package uni.insubria.theknife.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import uni.insubria.theknife.model.Restaurant;

public class RestaurantRepository {

    //! NOT WORKING if path does not start with '/' (InputStream is null)
    private static final String RESTAURANTS_CSV = "/data/michelin_my_maps.csv";
    private static final String RESTAURANTS_JSON = "/data/restaurants.json";

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 
     * @return
     * @throws IOException
     */
    public Map<String, Restaurant> loadRestaurants() throws IOException {

        File file = new File(RESTAURANTS_JSON);

        // If file doesn't exist --> (creates new json file + returns empty Map)
        if (!file.exists()) {
            
            //TODO - create empty file 'restaurants.json' to its location ???
            
            return new HashMap<>();
            
        }

        try (FileInputStream fis = new FileInputStream(file)) {

            return objectMapper.readValue(fis, new TypeReference<Map<String, Restaurant>>() {});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 
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
     * @return A list of Restaurant objects
     */
    static public List<Restaurant> loadRestaurantsCSV() {

        List<Restaurant> restaurants;
        // InputStream is = RestaurantRepository.class.getClassLoader().getResourceAsStream(RESTAURANT_CSV); // --> NOT WORKING with getClassLoader() 
        InputStream is = RestaurantRepository.class.getResourceAsStream(RESTAURANTS_CSV);

        try (Reader reader = new StringReader(new String(is.readAllBytes()))) {
            
            CsvToBean<Restaurant> cb = new CsvToBeanBuilder<Restaurant>(reader)
                .withType(Restaurant.class)
                .build();
            restaurants = new ArrayList<>(cb.parse());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return restaurants;

    }
}
