/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.dto;

/**
 * User roles shared between backend and frontend over the WebSocket protocol.
 * <ul>
 *   <li>{@code CLIENTE}: customer who can browse, favorite and review restaurants</li>
 *   <li>{@code RISTORATORE}: restaurant owner who manages restaurants and replies to reviews</li>
 * </ul>
 */
public enum Role {
    CLIENTE,
    RISTORATORE
}
