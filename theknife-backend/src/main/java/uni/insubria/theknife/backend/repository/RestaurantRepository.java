/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import uni.insubria.theknife.backend.entity.RestaurantEntity;

import java.util.List;

/**
 * Panache repository for {@link RestaurantEntity} (primary key: id).
 */
@ApplicationScoped
public class RestaurantRepository implements PanacheRepositoryBase<RestaurantEntity, String> {

    /** Distinct, sorted list of restaurant locations. */
    public List<String> distinctLocations() {
        return getEntityManager()
                .createQuery("select distinct r.location from RestaurantEntity r "
                        + "where r.location is not null order by r.location", String.class)
                .getResultList();
    }

    /** Distinct, sorted list of cuisines. */
    public List<String> distinctCuisines() {
        return getEntityManager()
                .createQuery("select distinct r.cuisine from RestaurantEntity r "
                        + "where r.cuisine is not null order by r.cuisine", String.class)
                .getResultList();
    }
}
