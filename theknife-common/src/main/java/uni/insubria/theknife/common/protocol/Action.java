/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.protocol;

/**
 * Every operation supported by the WebSocket API, plus the server-initiated
 * broadcast events. REQUEST actions are sent by the client; EVENT actions are
 * pushed by the server to the other connected clients after a successful change.
 */
public enum Action {
    // --- Auth / session (per connection) ---
    LOGIN,
    REGISTER,
    LOGOUT,

    // --- Reference / bootstrap data ---
    LIST_RESTAURANTS,
    LIST_MY_RESTAURANTS,
    GET_RESTAURANT,
    GET_LOCATIONS,
    GET_CUISINES,

    // --- Restaurant CRUD (RISTORATORE) ---
    ADD_RESTAURANT,
    EDIT_RESTAURANT,
    DELETE_RESTAURANT,

    // --- Reviews ---
    ADD_REVIEW,
    EDIT_REVIEW,
    DELETE_REVIEW,

    // --- Favorites (CLIENTE) ---
    TOGGLE_FAVORITE,
    GET_FAVORITES,

    // --- Broadcast events (server -> clients) ---
    RESTAURANT_ADDED,
    RESTAURANT_EDITED,
    RESTAURANT_DELETED,
    REVIEW_ADDED,
    REVIEW_EDITED,
    REVIEW_DELETED
}
