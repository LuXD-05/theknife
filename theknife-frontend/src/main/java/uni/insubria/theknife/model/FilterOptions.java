/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
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

        // Ristorante non presente
        if (r == null) {
            return false;
        }

        // Location (case-insensitive)
        if (!isBlank(location) && !equalsIgnoreCase(r.getLocation(), location)) {
            return false;
        }

        // Tipologia cucina
        if (!isBlank(cuisine)) {
            return false;
        }

        // Fascia di prezzo — match diretto su simbolo
        if (!isBlank(price) && !"Qualsiasi".equalsIgnoreCase(price)
                && !matchesPrice(r, price)
                && !price.equalsIgnoreCase(r.getPrice())) {
            return false;
        }

        // Rating — se vuoi implementarlo: parse "3★" in 3, ecc.
        if (!isBlank(stars) && !"Qualsiasi".equalsIgnoreCase(stars)
                && !matchesMinimumStars(r, stars)) {
            return false;
        }

        // Delivery
        if (deliveryAvailable && !hasDelivery(r)) {
            return false;
        }

        // Prenotazione online
        if (onlineBookingAvailable && !hasOnlineBooking(r)) {
            return false;
        }

        return true;
    }

    /**
     * Checks whether the restaurant price range matches the selected filter.
     * The comparison is based on the price level, not on the specific currency symbol.
     *
     * @param r The restaurant to evaluate
     * @param selectedPrice The price range selected in the filter
     * @return true if the restaurant matches the selected price range, false otherwise
     */
    private boolean matchesPrice(Restaurant r, String selectedPrice) {
        int selectedLevel = priceLevel(selectedPrice);
        int restaurantLevel = priceLevel(r.getPrice());

        if (restaurantLevel == 0) {
            restaurantLevel = priceLevel(r.getDescription());
        }

        return selectedLevel == 0 || selectedLevel == restaurantLevel;
    }

    /**
     * Calculates the price level by counting currency symbols in the given text.
     * For example, "€", "$" or "£" are level 1, while "€€€" or "$$$" are level 3.
     *
     * @param value The text containing the price information
     * @return The detected price level, from 0 to 4
     */
    private int priceLevel(String value) {
        if (isBlank(value)) {
            return 0;
        }

        int level = 0;
        for (char c : value.toCharArray()) {
            if (isCurrencySymbol(c)) {
                level++;
            }
        }

        return Math.min(level, 4);
    }

    /**
     * Takes the correct currencySymbol in the given text
     * @param c the char to cast to currencySymbol
     * @return correct currency
     */
    private boolean isCurrencySymbol(char c) {
    return c == '€'
            || c == '$'
            || c == '£'
            || c == '¥'
            || c == '₩'
            || c == '₹'
            || c == '₽'
            || c == '₺'
            || c == '₴'
            || c == '₫'
            || Character.getType(c) == Character.CURRENCY_SYMBOL;
    }

    /**
     * Checks whether the restaurant average review score is at least the selected value.
     * Restaurants without reviews do not match when a minimum rating is selected.
     *
     * @param r The restaurant to evaluate
     * @param selectedStars The minimum rating selected in the filter
     * @return true if the average rating is high enough, false otherwise
     */
    private boolean matchesMinimumStars(Restaurant r, String selectedStars) {
        int minStars = parseInteger(selectedStars);
        if (minStars <= 0) {
            return true;
        }

        if (r.getReviews() == null || r.getReviews().isEmpty()) {
            return false;
        }

        double average = r.getReviews().stream()
                .mapToInt(Review::getStars)
                .average()
                .orElse(0);

        return average >= minStars;
    }

    /**
     * Checks whether the restaurant appears to offer delivery or takeaway services.
     * The check is based on keywords found in the restaurant searchable text.
     *
     * @param r The restaurant to evaluate
     * @return true if delivery-related keywords are found, false otherwise
     */
    private boolean hasDelivery(Restaurant r) {
        String text = searchableText(r);

        return containsAny(text,
                "delivery",
                "home delivery",
                "takeaway",
                "take away",
                "to go",
                "asporto",
                "consegna",
                "consegna a domicilio",
                "da asporto");
    }

    /**
     * Checks whether the restaurant appears to support online booking.
     * If no booking keyword is found, the presence of a website is used as a fallback.
     *
     * @param r The restaurant to evaluate
     * @return true if online booking is detected or inferred, false otherwise
     */
    private boolean hasOnlineBooking(Restaurant r) {
        String text = searchableText(r);

        if (containsAny(text,
                "online booking",
                "book online",
                "booking online",
                "book a table",
                "reserve online",
                "reservation",
                "reservations",
                "prenotazione",
                "prenotazioni",
                "prenota online")) {
            return true;
        }

        return !isBlank(r.getWebsiteUrl());
    }

    /**
     * Builds a single searchable text from the restaurant fields useful for service filters.
     * This allows delivery and booking checks to inspect facilities, description and URLs together.
     *
     * @param r The restaurant whose fields are combined
     * @return A single text containing the searchable restaurant information
     */
    private String searchableText(Restaurant r) {
        return String.join(" ",
                safe(r.getFacilities()),
                safe(r.getDescription()),
                safe(r.getWebsiteUrl()),
                safe(r.getMichelinUrl()));
    }

    /**
     * Checks whether the given text contains at least one of the provided keywords.
     * Matching is case-insensitive.
     *
     * @param text The text to inspect
     * @param keywords The keywords to search for
     * @return true if at least one keyword is found, false otherwise
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (containsIgnoreCase(text, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares two strings ignoring letter case.
     * Null values are treated as non-matching.
     *
     * @param a The first string
     * @param b The second string
     * @return true if both strings are non-null and equal ignoring case, false otherwise
     */
    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    /**
     * Parses an integer value from a string.
     * Returns -1 when the text is not a valid number.
     *
     * @param value The string to parse
     * @return The parsed integer, or -1 if parsing fails
     */
    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Checks whether a string is null, empty or made only of whitespace.
     *
     * @param value The string to check
     * @return true if the string is null or blank, false otherwise
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Converts a possibly null string into a non-null value.
     * Useful when building searchable text from optional restaurant fields.
     *
     * @param value The original string
     * @return The original value, or an empty string if it is null
     */
    private String safe(String value) {
        return value == null ? "" : value;
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
