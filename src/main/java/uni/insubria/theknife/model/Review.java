package uni.insubria.theknife.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Represents a review for a restaurant in the TheKnife application.
 * <p>
 * This class contains all the information related to a user's review of a restaurant,
 * including the review content, star rating, and any response from the restaurant owner.
 * </p>
 * <p>
 * Reviews are associated with both a user (who wrote the review) and a restaurant
 * (that is being reviewed), creating a relationship between these entities.
 * </p>
 * <p>
 * This class uses Lombok annotations:
 * - @Getter: Generates getter methods for all fields
 * - @Setter: Generates setter methods for all fields
 * - @Accessors(chain = true): Enables method chaining for setters
 * - @NoArgsConstructor: Generates a no-args constructor that initializes all fields to default values
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Review {
    /**
     * The unique identifier for this review.
     * This field is used for equality checks.
     */
    @EqualsAndHashCode.Include
    String id;

    /**
     * The user who wrote this review.
     */
    User user;

    /**
     * The restaurant that this review is about.
     */
    Restaurant restaurant;

    /**
     * The text content of the review.
     */
    String content;

    /**
     * The star rating given in the review, typically on a scale of 1-5.
     */
    Integer stars;

    /**
     * The response or answer from the restaurant owner to this review, if any.
     */
    String answer;
}
