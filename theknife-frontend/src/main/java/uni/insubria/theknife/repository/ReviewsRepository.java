/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.repository;

import uni.insubria.theknife.client.BackendClient;
import uni.insubria.theknife.client.DtoMapper;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.common.dto.ReviewDto;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side review repository, backed by the WebSocket backend. Reads are served
 * from the in-memory catalog (reviews are embedded in each restaurant); writes are
 * forwarded to the backend.
 */
public class ReviewsRepository {

    public ReviewsRepository() {
        // Not meant to be instantiated
    }

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }

    /**
     * All reviews currently known to the client, flattened from the cached restaurants.
     */
    public static Map<String, Review> loadReviews() {
        Map<String, Review> reviews = new LinkedHashMap<>();
        for (Restaurant r : SessionService.getRestaurants()) {
            if (r.getReviews() != null) {
                for (Review review : r.getReviews()) {
                    reviews.put(review.getId(), review);
                }
            }
        }
        return reviews;
    }

    /**
     * Reviews for a given restaurant (embedded in the restaurant model).
     */
    public static List<Review> reviewsByRestaurant(Restaurant restaurant) {
        if (restaurant == null || restaurant.getReviews() == null) {
            return new ArrayList<>();
        }
        return restaurant.getReviews();
    }

    /**
     * Adds a review. The server assigns the id (written back) and the review is appended
     * to its restaurant's in-memory review list.
     */
    public static ERROR_CODE addReview(final Review review) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.ADD_REVIEW, DtoMapper.toDto(review));
            if (response.error() != ErrorCode.NONE) {
                return map(response.error());
            }
            if (response.payload() != null) {
                ReviewDto created = BackendClient.get().mapper().convertValue(response.payload(), ReviewDto.class);
                review.setId(created.id());
            }
            Restaurant restaurant = review.getRestaurant();
            if (restaurant != null) {
                if (restaurant.getReviews() == null) {
                    restaurant.setReviews(new ArrayList<>());
                }
                restaurant.getReviews().add(review);
            }
            return ERROR_CODE.NONE;
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    /**
     * Updates a review (author edits content/stars, owner sets the answer).
     */
    public static ERROR_CODE editReview(Review review) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.EDIT_REVIEW, DtoMapper.toDto(review));
            return map(response.error());
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    /**
     * Deletes a review.
     */
    public static ERROR_CODE deleteReview(Review review) {
        try {
            Envelope response = BackendClient.get().sendAndWait(Action.DELETE_REVIEW, DtoMapper.toDto(review));
            return map(response.error());
        } catch (Exception e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
    }

    private static ERROR_CODE map(ErrorCode code) {
        if (code == null) {
            return ERROR_CODE.SERVICE_ERROR;
        }
        return switch (code) {
            case NONE -> ERROR_CODE.NONE;
            case DUPLICATED -> ERROR_CODE.DUPLICATED;
            default -> ERROR_CODE.SERVICE_ERROR;
        };
    }
}
