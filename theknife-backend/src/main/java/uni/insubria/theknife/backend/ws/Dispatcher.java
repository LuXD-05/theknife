/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uni.insubria.theknife.backend.handler.AuthHandler;
import uni.insubria.theknife.backend.handler.CatalogHandler;
import uni.insubria.theknife.backend.handler.FavoriteHandler;
import uni.insubria.theknife.backend.handler.RestaurantHandler;
import uni.insubria.theknife.backend.handler.ReviewHandler;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

/**
 * Routes a parsed request {@link Envelope} to the handler responsible for its action.
 * Handlers run their DB work in a transaction; any broadcast event they return is
 * dispatched by the endpoint <em>after</em> the transaction commits.
 */
@ApplicationScoped
public class Dispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(Dispatcher.class);

    @Inject
    AuthHandler auth;
    @Inject
    CatalogHandler catalog;
    @Inject
    RestaurantHandler restaurant;
    @Inject
    ReviewHandler review;
    @Inject
    FavoriteHandler favorite;

    public HandlerResult dispatch(Envelope req, ConnectionState state) {
        if (req == null || req.action() == null) {
            return HandlerResult.of(Envelope.error(null, req == null ? null : req.correlationId(),
                    ErrorCode.VALIDATION, "Azione mancante"));
        }
        try {
            return switch (req.action()) {
                case LOGIN -> auth.login(req, state);
                case REGISTER -> auth.register(req, state);
                case LOGOUT -> auth.logout(req, state);
                case LIST_RESTAURANTS -> catalog.listRestaurants(req);
                case LIST_MY_RESTAURANTS -> catalog.listMyRestaurants(req, state);
                case GET_RESTAURANT -> catalog.getRestaurant(req);
                case GET_LOCATIONS -> catalog.getLocations(req);
                case GET_CUISINES -> catalog.getCuisines(req);
                case ADD_RESTAURANT -> restaurant.add(req, state);
                case EDIT_RESTAURANT -> restaurant.edit(req, state);
                case DELETE_RESTAURANT -> restaurant.delete(req, state);
                case ADD_REVIEW -> review.add(req, state);
                case EDIT_REVIEW -> review.edit(req, state);
                case DELETE_REVIEW -> review.delete(req, state);
                case TOGGLE_FAVORITE -> favorite.toggle(req, state);
                default -> HandlerResult.of(Envelope.error(req.action(), req.correlationId(),
                        ErrorCode.VALIDATION, "Azione non supportata: " + req.action()));
            };
        } catch (Exception e) {
            LOG.error("Error handling action {}", req.action(), e);
            return HandlerResult.of(Envelope.error(req.action(), req.correlationId(),
                    ErrorCode.SERVICE_ERROR, "Errore interno del server"));
        }
    }
}
