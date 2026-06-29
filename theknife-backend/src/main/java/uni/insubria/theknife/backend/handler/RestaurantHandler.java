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
import uni.insubria.theknife.backend.mapper.RestaurantMapper;
import uni.insubria.theknife.backend.repository.RestaurantRepository;
import uni.insubria.theknife.backend.ws.ConnectionState;
import uni.insubria.theknife.backend.ws.HandlerResult;
import uni.insubria.theknife.common.dto.RestaurantDto;
import uni.insubria.theknife.common.dto.Role;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

import java.util.List;
import java.util.UUID;

/**
 * Restaurant CRUD, restricted to RISTORATORE owners. Every successful change is
 * broadcast so the other clients update their lists in real time. Ownership is
 * re-checked server-side (the client-side checks are only for UX).
 */
@ApplicationScoped
public class RestaurantHandler {

    @Inject
    RestaurantRepository restaurants;
    @Inject
    ObjectMapper mapper;

    @Transactional
    public HandlerResult add(Envelope req, ConnectionState state) {
        if (!state.isAuthenticated() || state.role() != Role.RISTORATORE) {
            return unauthorized(Action.ADD_RESTAURANT, req);
        }
        RestaurantDto in = mapper.convertValue(req.payload(), RestaurantDto.class);
        if (in == null || in.name() == null || in.name().isBlank()) {
            return HandlerResult.of(Envelope.error(Action.ADD_RESTAURANT, req.correlationId(),
                    ErrorCode.VALIDATION, "Nome ristorante mancante"));
        }
        RestaurantEntity e = new RestaurantEntity();
        e.id = UUID.randomUUID().toString();
        RestaurantMapper.applyEditable(e, in);
        e.ownerUsername = state.username();
        restaurants.persist(e);

        RestaurantDto created = RestaurantMapper.toDto(e, List.of());
        Envelope response = Envelope.ok(Action.ADD_RESTAURANT, req.correlationId(), mapper.valueToTree(created));
        Envelope event = Envelope.event(Action.RESTAURANT_ADDED, mapper.valueToTree(created));
        return HandlerResult.of(response, event);
    }

    @Transactional
    public HandlerResult edit(Envelope req, ConnectionState state) {
        RestaurantDto in = mapper.convertValue(req.payload(), RestaurantDto.class);
        if (in == null || in.id() == null) {
            return HandlerResult.of(Envelope.error(Action.EDIT_RESTAURANT, req.correlationId(),
                    ErrorCode.VALIDATION, "Id mancante"));
        }
        RestaurantEntity e = restaurants.findById(in.id());
        if (e == null) {
            return HandlerResult.of(Envelope.error(Action.EDIT_RESTAURANT, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Ristorante non trovato"));
        }
        if (!isOwner(e, state)) {
            return unauthorized(Action.EDIT_RESTAURANT, req);
        }
        RestaurantMapper.applyEditable(e, in);

        RestaurantDto updated = RestaurantMapper.toDto(e, List.of());
        Envelope response = Envelope.ok(Action.EDIT_RESTAURANT, req.correlationId(), null);
        Envelope event = Envelope.event(Action.RESTAURANT_EDITED, mapper.valueToTree(updated));
        return HandlerResult.of(response, event);
    }

    @Transactional
    public HandlerResult delete(Envelope req, ConnectionState state) {
        RestaurantDto in = mapper.convertValue(req.payload(), RestaurantDto.class);
        if (in == null || in.id() == null) {
            return HandlerResult.of(Envelope.error(Action.DELETE_RESTAURANT, req.correlationId(),
                    ErrorCode.VALIDATION, "Id mancante"));
        }
        RestaurantEntity e = restaurants.findById(in.id());
        if (e == null) {
            return HandlerResult.of(Envelope.error(Action.DELETE_RESTAURANT, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Ristorante non trovato"));
        }
        if (!isOwner(e, state)) {
            return unauthorized(Action.DELETE_RESTAURANT, req);
        }
        String id = e.id;
        restaurants.delete(e); // reviews/favorites cascade at the DB level

        Envelope response = Envelope.ok(Action.DELETE_RESTAURANT, req.correlationId(), null);
        Envelope event = Envelope.event(Action.RESTAURANT_DELETED, mapper.createObjectNode().put("id", id));
        return HandlerResult.of(response, event);
    }

    private boolean isOwner(RestaurantEntity e, ConnectionState state) {
        return state.isAuthenticated()
                && state.role() == Role.RISTORATORE
                && state.username().equals(e.ownerUsername);
    }

    private HandlerResult unauthorized(Action action, Envelope req) {
        return HandlerResult.of(Envelope.error(action, req.correlationId(),
                ErrorCode.UNAUTHORIZED, "Operazione non consentita"));
    }
}
