/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped to the {@code reviews} table. References the author and the
 * reviewed restaurant by id (the FKs are enforced at the DB level by Flyway).
 */
@Entity
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    public String id;

    @Column(name = "restaurant_id")
    public String restaurantId;

    public String username;

    @Column(columnDefinition = "text")
    public String content;

    public Integer stars;

    @Column(columnDefinition = "text")
    public String answer;
}
