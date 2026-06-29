/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.ws;

import uni.insubria.theknife.common.dto.Role;

/**
 * Per-connection authentication state. Mutable: {@code LOGIN} sets it, {@code LOGOUT}
 * and connection close clear it.
 */
public class ConnectionState {

    private volatile String username;
    private volatile Role role;

    public boolean isAuthenticated() {
        return username != null;
    }

    public String username() {
        return username;
    }

    public Role role() {
        return role;
    }

    public void login(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    public void logout() {
        this.username = null;
        this.role = null;
    }
}
