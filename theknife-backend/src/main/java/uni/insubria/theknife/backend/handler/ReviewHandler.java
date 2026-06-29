/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import uni.insubria.theknife.backend.entity.RestaurantEntity;
import uni.insubria.theknife.backend.entity.ReviewEntity;
import uni.insubria.theknife.backend.mapper.ReviewMapper;
import uni.insubria.theknife.backend.repository.RestaurantRepository;
import uni.insubria.theknife.backend.repository.ReviewRepository;
import uni.insubria.theknife.backend.ws.ConnectionState;
import uni.insubria.theknife.backend.ws.HandlerResult;
import uni.insubria.theknife.common.dto.ReviewDto;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

import java.util.UUID;

/**
 * Review operations. A customer creates/edits/deletes their own review; the owner of
 * the reviewed restaurant may reply (set the answer). All changes are broadcast so
 * other clients viewing the same restaurant update live.
 */
@ApplicationScoped
public class ReviewHandler {

    @Inject
    ReviewRepository reviews;
    @Inject
    RestaurantRepository restaurants;
    @Inject
    ObjectMapper mapper;

    @Transactional
    public HandlerResult add(Envelope req, ConnectionState state) {
        if (!state.isAuthenticated()) {
            return unauthorized(Action.ADD_REVIEW, req);
        }
        ReviewDto in = mapper.convertValue(req.payload(), ReviewDto.class);
        if (in == null || in.restaurantId() == null) {
            return HandlerResult.of(Envelope.error(Action.ADD_REVIEW, req.correlationId(),
                    ErrorCode.VALIDATION, "Dati recensione incompleti"));
        }
        if (restaurants.findById(in.restaurantId()) == null) {
            return HandlerResult.of(Envelope.error(Action.ADD_REVIEW, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Ristorante non trovato"));
        }
        if (reviews.existsForUserAndRestaurant(state.username(), in.restaurantId())) {
            return HandlerResult.of(Envelope.error(Action.ADD_REVIEW, req.correlationId(),
                    ErrorCode.DUPLICATED, "Hai già recensito questo ristorante"));
        }
        ReviewEntity e = new ReviewEntity();
        e.id = UUID.randomUUID().toString();
        e.restaurantId = in.restaurantId();
        e.username = state.username(); // never trust a client-supplied author
        e.content = in.content();
        e.stars = in.stars();
        e.answer = null;
        reviews.persist(e);

        ReviewDto created = ReviewMapper.toDto(e);
        Envelope response = Envelope.ok(Action.ADD_REVIEW, req.correlationId(), mapper.valueToTree(created));
        Envelope event = Envelope.event(Action.REVIEW_ADDED, mapper.valueToTree(created));
        return HandlerResult.of(response, event);
    }

    @Transactional
    public HandlerResult edit(Envelope req, ConnectionState state) {
        if (!state.isAuthenticated()) {
            return unauthorized(Action.EDIT_REVIEW, req);
        }
        ReviewDto in = mapper.convertValue(req.payload(), ReviewDto.class);
        if (in == null || in.id() == null) {
            return HandlerResult.of(Envelope.error(Action.EDIT_REVIEW, req.correlationId(),
                    ErrorCode.VALIDATION, "Id recensione mancante"));
        }
        ReviewEntity e = reviews.findById(in.id());
        if (e == null) {
            return HandlerResult.of(Envelope.error(Action.EDIT_REVIEW, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Recensione non trovata"));
        }
        boolean isAuthor = state.username().equals(e.username);
        boolean isOwner = isRestaurantOwner(e.restaurantId, state);
        if (isAuthor) {
            // Customer updates their own review body / rating
            e.content = in.content();
            e.stars = in.stars();
        } else if (isOwner) {
            // Restaurant owner replies
            e.answer = in.answer();
        } else {
            return unauthorized(Action.EDIT_REVIEW, req);
        }

        ReviewDto updated = ReviewMapper.toDto(e);
        Envelope response = Envelope.ok(Action.EDIT_REVIEW, req.correlationId(), null);
        Envelope event = Envelope.event(Action.REVIEW_EDITED, mapper.valueToTree(updated));
        return HandlerResult.of(response, event);
    }

    @Transactional
    public HandlerResult delete(Envelope req, ConnectionState state) {
        if (!state.isAuthenticated()) {
            return unauthorized(Action.DELETE_REVIEW, req);
        }
        ReviewDto in = mapper.convertValue(req.payload(), ReviewDto.class);
        if (in == null || in.id() == null) {
            return HandlerResult.of(Envelope.error(Action.DELETE_REVIEW, req.correlationId(),
                    ErrorCode.VALIDATION, "Id recensione mancante"));
        }
        ReviewEntity e = reviews.findById(in.id());
        if (e == null) {
            return HandlerResult.of(Envelope.error(Action.DELETE_REVIEW, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Recensione non trovata"));
        }
        if (!state.username().equals(e.username) && !isRestaurantOwner(e.restaurantId, state)) {
            return unauthorized(Action.DELETE_REVIEW, req);
        }
        ReviewDto removed = ReviewMapper.toDto(e);
        reviews.delete(e);

        Envelope response = Envelope.ok(Action.DELETE_REVIEW, req.correlationId(), null);
        Envelope event = Envelope.event(Action.REVIEW_DELETED, mapper.valueToTree(removed));
        return HandlerResult.of(response, event);
    }

    private boolean isRestaurantOwner(String restaurantId, ConnectionState state) {
        RestaurantEntity r = restaurants.findById(restaurantId);
        return r != null && state.username().equals(r.ownerUsername);
    }

    private HandlerResult unauthorized(Action action, Envelope req) {
        return HandlerResult.of(Envelope.error(action, req.correlationId(),
                ErrorCode.UNAUTHORIZED, "Operazione non consentita"));
    }
}
