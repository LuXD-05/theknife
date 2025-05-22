package uni.insubria.theknife.repository;

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

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private static final String REVIEWS_JSON;

    static {
        if (ReviewsRepository.class.getClassLoader().getResource("data/reviews.json") != null) {
            REVIEWS_JSON = Objects.requireNonNull(ReviewsRepository.class.getClassLoader().getResource("data/reviews.json")).getFile();
        } else {
            REVIEWS_JSON = new File(Objects.requireNonNull(ReviewsRepository.class.getClassLoader().getResource("data")).getFile(), "reviews.json").getAbsolutePath();
        }
    }

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }

    //TODO
    public static List<Review> reviewsByRestaurant(Restaurant restaurant) {
        return new ArrayList<>();
    }

    public static void saveReviews(List<Review> reviews) throws IOException {
        FileWriter fileWriter = new FileWriter(REVIEWS_JSON, false); // true to append
        fileWriter.write(objectMapper.writeValueAsString(reviews));
        fileWriter.close();
    }

    ///
    public static List<Review> loadReviews() {
        try {
            FileInputStream inputStream = new FileInputStream(REVIEWS_JSON);
            return Arrays.asList(objectMapper.readValue(inputStream, Review[].class));
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento delle recensioni", e);
        }
    }

    //TODO
    public static ERROR_CODE addReview(Review review) {
        return ERROR_CODE.NONE;
    }

    //TODO
    public static ERROR_CODE editReview(Review review) {
        return ERROR_CODE.NONE;
    }

    //TODO
    public static ERROR_CODE deleteReview(Review review) {
        return ERROR_CODE.NONE;
    }
}
