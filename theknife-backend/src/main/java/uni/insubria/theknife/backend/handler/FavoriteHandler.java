/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.handler;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uni.insubria.theknife.backend.entity.FavoriteEntity;
import uni.insubria.theknife.backend.entity.FavoriteId;
import uni.insubria.theknife.backend.entity.RestaurantEntity;
import uni.insubria.theknife.backend.repository.FavoriteRepository;
import uni.insubria.theknife.backend.repository.RestaurantRepository;
import uni.insubria.theknife.backend.ws.ConnectionState;
import uni.insubria.theknife.backend.ws.HandlerResult;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

/**
 * Toggles a restaurant in the current user's favorites. Idempotent and
 * concurrency-safe thanks to the composite primary key. Private to the user, so
 * no broadcast is emitted. Returns the updated favorite-id set so the client can sync.
 */
@ApplicationScoped
public class FavoriteHandler {

    @Inject
    FavoriteRepository favorites;
    @Inject
    RestaurantRepository restaurants;
    @Inject
    ObjectMapper mapper;

    @Transactional
    public HandlerResult toggle(Envelope req, ConnectionState state) {
        if (!state.isAuthenticated()) {
            return HandlerResult.of(Envelope.error(Action.TOGGLE_FAVORITE, req.correlationId(),
                    ErrorCode.UNAUTHORIZED, "Devi effettuare l'accesso"));
        }
        String restaurantId = req.payload() == null ? null : req.payload().path("restaurantId").asText(null);
        if (restaurantId == null) {
            return HandlerResult.of(Envelope.error(Action.TOGGLE_FAVORITE, req.correlationId(),
                    ErrorCode.VALIDATION, "Id ristorante mancante"));
        }
        if (restaurants.findById(restaurantId) == null) {
            return HandlerResult.of(Envelope.error(Action.TOGGLE_FAVORITE, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Ristorante non trovato"));
        }
        String username = state.username();
        FavoriteEntity existing = favorites.findById(new FavoriteId(username, restaurantId));
        boolean favorited;
        if (existing != null) {
            favorites.delete(existing);
            favorited = false;
        } else {
            favorites.persist(new FavoriteEntity(username, restaurantId));
            favorited = true;
        }

        ObjectNode payload = mapper.createObjectNode();
        payload.put("favorited", favorited);
        payload.set("favoriteRestaurantIds",
                mapper.valueToTree(favorites.favoriteRestaurantIds(username)));
        return HandlerResult.of(Envelope.ok(Action.TOGGLE_FAVORITE, req.correlationId(), payload));
    }

    public HandlerResult getFavorites(Envelope req, ConnectionState state) {
        if (!state.isAuthenticated()) {
            return HandlerResult.of(Envelope.error(Action.TOGGLE_FAVORITE, req.correlationId(),
                    ErrorCode.UNAUTHORIZED, "Devi effettuare l'accesso"));
        }
        String username = req.payload() == null ? null : req.payload().path("username").asText(null);
        if (username == null) {
            return HandlerResult.of(Envelope.error(Action.TOGGLE_FAVORITE, req.correlationId(),
                    ErrorCode.VALIDATION, "Nome utente mancante"));
        }
        
        List<RestaurantEntity> favs = favorites.favoriteRestaurantIds(username).stream().map(id -> {
            return restaurants.findById(id);
        }).toList();

        ObjectNode payload = mapper.createObjectNode();
        payload.set("favoriteRestaurants", mapper.valueToTree(favs));
        return HandlerResult.of(Envelope.ok(Action.GET_FAVORITES, req.correlationId(), payload));
    }

}
