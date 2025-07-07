/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.util;

import lombok.experimental.UtilityClass;
import uni.insubria.theknife.model.Restaurant;

/**
 * Utility class for calculating geographical distances between locations.
 * Uses the Haversine formula to calculate the great-circle distance between two points on a sphere.
 * <p>
 * This class is annotated with Lombok's @UtilityClass which automatically makes the class final,
 * adds a private constructor, and makes all methods static.
 * </p>
 */
@UtilityClass
public class DistanceCalculator {

    /**
     * Earth's radius in kilometers, used for distance calculations.
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates the distance between a restaurant and a reference coordinate in kilometers.
     *
     * @param restaurant           The restaurant whose distance needs to be calculated
     * @param referenceCoordinates The reference coordinates to calculate distance from
     * @return The distance in kilometers
     */
    public static double calculateDistanceInKm(Restaurant restaurant, Restaurant.Coordinate referenceCoordinates) {
        if (restaurant == null || referenceCoordinates == null) {
            return Double.MAX_VALUE;
        }

        double startLat = Math.toRadians(restaurant.getLatitude());
        double startLong = Math.toRadians(restaurant.getLongitude());
        double endLat = Math.toRadians(referenceCoordinates.getLatitude());
        double endLong = Math.toRadians(referenceCoordinates.getLongitude());

        double a = calculateHaversineFormula(startLat, startLong, endLat, endLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS_KM * c);
    }

    /**
     * Calculates the Haversine formula component for spherical distance calculation.
     *
     * @param startLat The latitude of the starting point in radians
     * @param startLong The longitude of the starting point in radians
     * @param endLat The latitude of the ending point in radians
     * @param endLong The longitude of the ending point in radians
     * @return The Haversine formula component (a value between 0 and 1)
     */
    private static double calculateHaversineFormula(double startLat, double startLong,
                                                    double endLat, double endLong) {
        double latDistance = endLat - startLat;
        double longDistance = endLong - startLong;

        return Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(startLat) * Math.cos(endLat) *
                        Math.sin(longDistance / 2) * Math.sin(longDistance / 2);
    }
}
