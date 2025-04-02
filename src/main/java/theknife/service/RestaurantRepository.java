package theknife.service;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;
import theknife.model.Restaurant;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestaurantRepository {
    private static final String RESTAURANT_CSV = "michelin_my_maps.csv";

    static public List<Restaurant> loadRestaurants() {
        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        InputStream is = RestaurantRepository.class.getClassLoader().getResourceAsStream(RESTAURANT_CSV);
        try (Reader reader = new StringReader(new String(is.readAllBytes()))) {
            CsvToBean<Restaurant> cb = new CsvToBeanBuilder<Restaurant>(reader)
                    .withType(Restaurant.class)
                    .build();
            cb.parse().forEach(restaurant -> restaurants.add(restaurant));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return restaurants;
    }
}
