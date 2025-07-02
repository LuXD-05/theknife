package uni.insubria.theknife.model;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * Represents the active set of filters applied by the user
 * to the list of restaurants in the application.
 *
 * <p>
 * Each field corresponds to a filterable attribute such as location,
 * cuisine type, price range, minimum star rating, and availability of services.
 * </p>
 *
 * <p>
 * This class also provides a method to check if a given {@link Restaurant}
 * satisfies all the active filter criteria.
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class FilterOptions {

    /**
     * The selected type of cuisine to filter by.
     * If null or equal to "Tutte le tipologie", all cuisines are allowed.
     */
    private String cuisine;

    /**
     * The selected location or city to filter by.
     * If null, all locations are allowed.
     */
    private String location;

    /**
     * The restaurant's privce.
     */
    private String price;

    /**
     * The star rating required.
     * If null or zero, all ratings are allowed.
     */
    private String stars;

    /**
     * Whether only restaurants with delivAery service should be included.
     */
    private boolean deliveryAvailable;

    /**
     * Whether only restaurants with online booking should be included.
     */
    private boolean onlineBookingAvailable;

    public FilterOptions() {
        cuisine = null;
        location = null;
        price = null;
        stars = null;
        deliveryAvailable = false;
        onlineBookingAvailable = false;
    }

    /**
     * Checks whether a given restaurant matches all the active filter conditions.
     *
     * @param r The restaurant to check
     * @return true if the restaurant matches all filters; false otherwise
     */
    public boolean matches(Restaurant r) {

        // Location (case-insensitive)
        if (location != null && !location.isBlank() && !r.getLocation().equalsIgnoreCase(location)) {
            return false;
        }

        // Tipologia cucina
        if (cuisine != null && !cuisine.isBlank() && !r.getCuisine().equalsIgnoreCase(cuisine)) {
            return false;
        }

        // Fascia di prezzo — match diretto su simbolo
        if (price != null && !price.equals("Qualsiasi")
                && !price.equalsIgnoreCase(r.getPrice())) {
            return false;
        }

        // Rating — se vuoi implementarlo: parse "3★" in 3, ecc.
        if (stars != null && !stars.equals("Qualsiasi")
                && r.getReviews() != null && !r.getReviews().isEmpty()) {
            int selectedStars = parseStars(stars);
            double avg = r.getReviews().stream().mapToDouble(Review::getStars).average().orElse(0);
            int flooredAvg = (int) Math.floor(avg);
            if (flooredAvg != selectedStars) {
                return false;
            }
        }

        // Delivery
        if (deliveryAvailable && !containsIgnoreCase(r.getFacilities(), "delivery")) {
            return false;
        }

        // Prenotazione online
        if (onlineBookingAvailable && !containsIgnoreCase(r.getFacilities(), "prenotazione")) {
            return false;
        }

        return true;
    }

    private int parseStars(String starsStr) {
        // Esempio: "3★" → 3
        try {
            return Integer.parseInt(starsStr.replace("★", "").trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Case-insensitive search for a keyword inside a string.
     *
     * @param text     The full text
     * @param keyword  The word to search for
     * @return true if found, false otherwise
     */
    private boolean containsIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) return false;
        return text.toLowerCase().contains(keyword.toLowerCase());
    }
    
}