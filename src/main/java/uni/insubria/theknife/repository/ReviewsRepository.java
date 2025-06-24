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

/**
 * Repository for managing review data in the TheKnife application.
 * <p>
 * This class provides methods to interact with the repository of Review objects,
 * including loading reviews from JSON files, and performing CRUD operations
 * (Create, Read, Update, Delete) on review data.
 * </p>
 * <p>
 * The repository handles data persistence and serves as the data access layer
 * for review-related operations in the application. It also manages the relationship
 * between reviews, users, and restaurants.
 * </p>
 */
public class ReviewsRepository {
    /**
     * Default constructor for the ReviewsRepository class.
     * <p>
     * This constructor is not meant to be used directly as this class only provides
     * static methods. The class is not designed to be instantiated.
     * </p>
     */
    public ReviewsRepository() {
        // Default constructor - not meant to be used
    }



    /**
     * Jackson ObjectMapper instance configured to exclude null and empty values during serialization.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    /**
     * Jackson ObjectWriter instance configured for pretty printing JSON output.
     */
    private static final ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();

    /**
     * Path to the JSON file used for storing and retrieving review data.
     */
    private static final String REVIEWS_JSON = "data/reviews.json";

    /**
     * Enumeration of possible error codes returned by repository operations.
     */
    public enum ERROR_CODE {
        /**
         * Indicates that the operation failed because the entity already exists.
         */
        DUPLICATED,

        /**
         * Indicates that a service-level error occurred during the operation.
         */
        SERVICE_ERROR,

        /**
         * Indicates that the operation completed successfully with no errors.
         */
        NONE
    }



    /**
     * Retrieves all reviews for a specific restaurant.
     *
     * @param restaurant The restaurant for which to retrieve reviews
     * @return A list of Review objects associated with the specified restaurant,
     *         or an empty list if the restaurant is null
     */
    public static List<Review> reviewsByRestaurant(Restaurant restaurant) {
        if (restaurant != null) {
            return loadReviews().values().stream().filter(review -> review.getRestaurant().getId().equals(restaurant.getId())).toList();
        }
        return new ArrayList<>();
    }

    /**
     * Saves the provided map of reviews to a JSON file.
     * <p>
     * This method simplifies the review objects before saving to reduce JSON file size
     * by removing unnecessary information and keeping only essential references.
     * </p>
     *
     * @param reviews A map containing review IDs as keys and corresponding Review objects as values
     * @throws IOException If an I/O error occurs during file writing
     */
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

    /**
     * Loads all reviews from the JSON file.
     * <p>
     * If the reviews file doesn't exist, creates a new empty file.
     * </p>
     *
     * @return A map containing review IDs as keys and corresponding Review objects as values
     */
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

    //#region Review CRUD

    /**
     * Adds a new review to the repository.
     * <p>
     * This method adds a review to the repository and updates the associated restaurant's
     * review list. If the review already exists (based on ID), the operation fails.
     * </p>
     *
     * @param review The Review object to add
     * @return An ERROR_CODE indicating the result of the operation:
     *         - DUPLICATED if the review already exists
     *         - SERVICE_ERROR if an error occurs during saving
     *         - NONE if the review is successfully added
     */
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

    /**
     * Deletes a review from the repository.
     * <p>
     * This method removes the specified review from the repository.
     * </p>
     *
     * @param review The Review object to delete
     * @return An ERROR_CODE indicating the result of the operation:
     *         - SERVICE_ERROR if an error occurs during saving
     *         - NONE if the review is successfully deleted
     */
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

    /**
     * Updates an existing review in the repository.
     * <p>
     * This method updates the specified review in the repository if it exists.
     * </p>
     *
     * @param review The Review object with updated information
     * @return An ERROR_CODE indicating the result of the operation:
     *         - SERVICE_ERROR if the review doesn't exist or an error occurs during saving
     *         - NONE if the review is successfully updated
     */
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

    //#endregion

}
