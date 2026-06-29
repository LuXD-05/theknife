/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.mapper;

import uni.insubria.theknife.backend.entity.ReviewEntity;
import uni.insubria.theknife.common.dto.ReviewDto;

/**
 * Converts {@link ReviewEntity} to the wire {@link ReviewDto}.
 */
public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewDto toDto(ReviewEntity e) {
        return new ReviewDto(e.id, e.username, e.restaurantId, e.content, e.stars, e.answer);
    }
}
