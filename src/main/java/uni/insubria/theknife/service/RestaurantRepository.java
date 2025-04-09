package uni.insubria.theknife.service;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import uni.insubria.theknife.model.Restaurant;

public class RestaurantRepository {

    //! NOT WORKING if path does not start with '/' (InputStream is null)
    private static final String RESTAURANTS_CSV = "/data/michelin_my_maps.csv";

    /**
     * Loads the restaurants from the .csv file
     * @return A list of Restaurant objects
     */
    static public List<Restaurant> loadRestaurants() {

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
