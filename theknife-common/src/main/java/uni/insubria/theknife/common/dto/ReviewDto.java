/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.dto;

/**
 * Wire representation of a review. Acyclic by construction: it references the
 * author and the reviewed restaurant only by their identifiers, mirroring the
 * "simplified review" shape that the legacy file-based repository used to persist.
 *
 * @param id           unique review id
 * @param username     author username
 * @param restaurantId reviewed restaurant id
 * @param content      free-text review body
 * @param stars        1-5 rating
 * @param answer       optional reply written by the restaurant owner
 */
public record ReviewDto(
        String id,
        String username,
        String restaurantId,
        String content,
        Integer stars,
        String answer
) {
}
