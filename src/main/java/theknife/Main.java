package theknife;

import theknife.model.Restaurant;
import theknife.service.RestaurantRepository;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Restaurant> restaurants = RestaurantRepository.loadRestaurants();
        System.out.println("loaded " + restaurants.size() + " restaurants");
    }
}