package uni.insubria.theknife.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import uni.insubria.theknife.model.Restaurant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantRepository {
    private static final String RESTAURANT_CSV = "data/michelin_my_maps.csv";

    static public List<Restaurant> loadRestaurants() {
        List<Restaurant> restaurants;
        InputStream is = RestaurantRepository.class.getClassLoader().getResourceAsStream(RESTAURANT_CSV);
        try (Reader reader = new StringReader(new String(is.readAllBytes()))) {
            CsvToBean<Restaurant> cb = new CsvToBeanBuilder<Restaurant>(reader)
                    .withType(Restaurant.class)
                    .build();
            restaurants = new ArrayList<>(cb.parse());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return restaurants;
    }
}
