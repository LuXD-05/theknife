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
 * JPA entity mapped to the {@code restaurants} table. The owner is kept as a plain
 * {@code owner_username} column (the FK is enforced at the DB level by Flyway), which
 * keeps mapping trivial and avoids lazy-proxy concerns.
 */
@Entity
@Table(name = "restaurants")
public class RestaurantEntity {

    @Id
    public String id;

    public String name;

    public String address;

    public String location;

    public String price;

    public String cuisine;

    public Double longitude;

    public Double latitude;

    public String phone;

    @Column(name = "michelin_url")
    public String michelinUrl;

    @Column(name = "website_url")
    public String websiteUrl;

    public String award;

    @Column(name = "green_star")
    public Integer greenStar;

    @Column(columnDefinition = "text")
    public String facilities;

    @Column(columnDefinition = "text")
    public String description;

    @Column(name = "owner_username")
    public String ownerUsername;
}
