package uni.insubria.theknife.model;

/**
 * Represents the roles that users can have in the TheKnife application.
 * <p>
 * This enum defines the different types of users in the system, each with
 * different permissions and capabilities:
 * </p>
 * <ul>
 *   <li>CLIENTE (Customer): Regular users who can browse restaurants and write reviews</li>
 *   <li>RISTORATORE (Restaurant Owner): Users who own or manage restaurants and can
 *       respond to reviews for their establishments</li>
 * </ul>
 */
public enum Role {
    /**
     * Represents a customer user who can browse restaurants and write reviews.
     */
    CLIENTE,

    /**
     * Represents a restaurant owner who can manage restaurant information and respond to reviews.
     */
    RISTORATORE
}
