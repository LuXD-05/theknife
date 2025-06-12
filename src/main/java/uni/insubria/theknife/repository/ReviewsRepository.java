package uni.insubria.theknife.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ReviewsRepository {

    //TODO GITHUB TASK #7
    //finalise repository and pre-load some review for restaurants (demo purposes)
    //TODO GITHUB TASK #10
    //add/edit/delete review
    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private static final ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

    private static final String REVIEWS_JSON = "data/reviews.json";

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }

    public static List<Review> reviewsByRestaurant(Restaurant restaurant) {
        if (restaurant != null) {
            return loadReviews().values().stream().filter(review -> review.getRestaurant().getId().equals(restaurant.getId())).toList();
        }
        return new ArrayList<>();
    }

    public static void saveReviews(Map<String, Review> reviews) throws IOException {
        Map<String, Review> simplifiedReviews = new HashMap<>();

        //Reduce json size by removing useless information
        for (Map.Entry<String, Review> entry : reviews.entrySet()) {
            Review review = entry.getValue();
            User user = new User().setUsername(review.getUser().getUsername());
            Restaurant restaurant = new Restaurant().setId(review.getRestaurant().getId());

            Review simplifiedReview = new Review()
                    .setUser(user)
                    .setRestaurant(restaurant)
                    .setId(review.getId())
                    .setContent(review.getContent())
                    .setStars(review.getStars())
                    .setAnswer(review.getAnswer());

            simplifiedReviews.put(entry.getKey(), simplifiedReview);
        }

        FileWriter fileWriter = new FileWriter(REVIEWS_JSON, false);
        objectWriter.writeValue(fileWriter, simplifiedReviews);
        fileWriter.close();
    }

    ///
    public static Map<String, Review> loadReviews() {
        try {
            File file = new File(REVIEWS_JSON);
            if (!file.exists()) {
                file.createNewFile();
                objectMapper.writeValue(file, new HashMap<>());
            }
            Map<String, Review> reviews = objectMapper.readValue(new FileInputStream(file), Map.class);
            reviews.keySet().forEach(key -> reviews.put(key, objectMapper.convertValue(reviews.get(key), Review.class)));
            return reviews;
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public static ERROR_CODE addReview(final Review review) {
        try {
            Map<String, Review> reviews = loadReviews();

            if (reviews.containsKey(review.getId())) return ERROR_CODE.DUPLICATED;

            reviews.put(review.getId(), review);

            try {
                saveReviews(reviews);
            } catch (IOException e) {
                return ERROR_CODE.SERVICE_ERROR;
            }

            if (review.getRestaurant().getReviews() == null) {
                review.getRestaurant().setReviews(new ArrayList<>());
            } else {
                List<Review> mutableReviews = new ArrayList<>(review.getRestaurant().getReviews());
                review.getRestaurant().setReviews(mutableReviews);
            }
            review.getRestaurant().getReviews().add(review);

            return ERROR_CODE.NONE;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    public static ERROR_CODE deleteReview(Review review) {
        try {
            Map<String, Review> reviews = loadReviews();
            reviews.remove(review.getId());
            saveReviews(reviews);
            return ERROR_CODE.NONE;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    public static ERROR_CODE editReview(Review review) {
        try {
            Map<String, Review> reviews = loadReviews();
            if (reviews.containsKey(review.getId())) {
                reviews.put(review.getId(), review);
                saveReviews(reviews);
                return ERROR_CODE.NONE;
            }
            return ERROR_CODE.SERVICE_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_CODE.SERVICE_ERROR;
        }
    }
}