/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import uni.insubria.theknife.backend.entity.UserEntity;

/**
 * Panache repository for {@link UserEntity} (primary key: username).
 */
@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, String> {
}
