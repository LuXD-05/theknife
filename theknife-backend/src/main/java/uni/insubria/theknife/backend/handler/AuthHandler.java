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
import uni.insubria.theknife.backend.entity.UserEntity;
import uni.insubria.theknife.backend.mapper.UserMapper;
import uni.insubria.theknife.backend.repository.FavoriteRepository;
import uni.insubria.theknife.backend.repository.UserRepository;
import uni.insubria.theknife.backend.service.SecurityService;
import uni.insubria.theknife.backend.ws.ConnectionState;
import uni.insubria.theknife.backend.ws.HandlerResult;
import uni.insubria.theknife.common.dto.LoginRequest;
import uni.insubria.theknife.common.dto.RegisterRequest;
import uni.insubria.theknife.common.dto.Role;
import uni.insubria.theknife.common.dto.UserDto;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

/**
 * Handles {@code LOGIN}, {@code REGISTER} and {@code LOGOUT}. Establishes the
 * per-connection authentication state used by the other handlers for authorization.
 */
@ApplicationScoped
public class AuthHandler {

    @Inject
    UserRepository users;
    @Inject
    FavoriteRepository favorites;
    @Inject
    SecurityService security;
    @Inject
    ObjectMapper mapper;

    @Transactional
    public HandlerResult login(Envelope req, ConnectionState state) {
        LoginRequest in = mapper.convertValue(req.payload(), LoginRequest.class);
        if (in == null || in.username() == null) {
            return HandlerResult.of(Envelope.error(Action.LOGIN, req.correlationId(),
                    ErrorCode.VALIDATION, "Username mancante"));
        }
        UserEntity user = users.findById(in.username());
        if (user == null) {
            return HandlerResult.of(Envelope.error(Action.LOGIN, req.correlationId(),
                    ErrorCode.NOT_FOUND, "Utente non trovato"));
        }
        if (!security.validate(in.password(), user.password)) {
            return HandlerResult.of(Envelope.error(Action.LOGIN, req.correlationId(),
                    ErrorCode.UNAUTHORIZED, "Credenziali non valide"));
        }
        state.login(user.username, user.role);
        UserDto dto = UserMapper.toDto(user, favorites.favoriteRestaurantIds(user.username));
        return HandlerResult.of(Envelope.ok(Action.LOGIN, req.correlationId(), mapper.valueToTree(dto)));
    }

    @Transactional
    public HandlerResult register(Envelope req, ConnectionState state) {
        RegisterRequest in = mapper.convertValue(req.payload(), RegisterRequest.class);
        if (in == null || in.username() == null || in.username().isBlank()
                || in.password() == null || in.password().isBlank()) {
            return HandlerResult.of(Envelope.error(Action.REGISTER, req.correlationId(),
                    ErrorCode.VALIDATION, "Dati di registrazione incompleti"));
        }
        if (users.findById(in.username()) != null) {
            return HandlerResult.of(Envelope.error(Action.REGISTER, req.correlationId(),
                    ErrorCode.DUPLICATED, "Username già in uso"));
        }
        UserEntity user = new UserEntity();
        user.username = in.username();
        user.firstName = in.firstName();
        user.lastName = in.lastName();
        user.password = security.encode(in.password());
        user.birthDate = in.birthDate();
        user.city = in.city();
        user.role = in.role() == null ? Role.CLIENTE : in.role();
        users.persist(user);

        state.login(user.username, user.role);
        UserDto dto = UserMapper.toDto(user, java.util.Set.of());
        return HandlerResult.of(Envelope.ok(Action.REGISTER, req.correlationId(), mapper.valueToTree(dto)));
    }

    public HandlerResult logout(Envelope req, ConnectionState state) {
        state.logout();
        return HandlerResult.of(Envelope.ok(Action.LOGOUT, req.correlationId(), null));
    }
}
