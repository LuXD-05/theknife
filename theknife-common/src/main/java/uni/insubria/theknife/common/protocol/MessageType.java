/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.protocol;

/**
 * The kind of message carried by an {@link Envelope}.
 * <ul>
 *   <li>{@code REQUEST}: client to server, carries a correlationId</li>
 *   <li>{@code RESPONSE}: server to client, echoes the request correlationId</li>
 *   <li>{@code EVENT}: server-initiated broadcast, has no correlationId</li>
 * </ul>
 */
public enum MessageType {
    REQUEST,
    RESPONSE,
    EVENT
}
