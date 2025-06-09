package uni.insubria.theknife.model;

import com.opencsv.bean.CsvBindByName;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurant {
    @EqualsAndHashCode.Include
    private int id;
    @EqualsAndHashCode.Include
    @CsvBindByName(column = "Name")
    String name;
    @CsvBindByName(column = "Address")
    String address;
    @CsvBindByName(column = "Location")
    String location;
    @CsvBindByName(column = "Price")
    String price;
    @CsvBindByName(column = "Cuisine")
    String cuisine;
    @EqualsAndHashCode.Include
    @CsvBindByName(column = "Longitude")
    float longitude;
    @EqualsAndHashCode.Include
    @CsvBindByName(column = "Latitude")
    float latitude;
    @CsvBindByName(column = "PhoneNumber")
    String phone;
    @CsvBindByName(column = "Url")
    String michelinUrl;
    @CsvBindByName(column = "WebsiteUrl")
    String websiteUrl;
    @CsvBindByName(column = "Award")
    String award;
    @CsvBindByName(column = "GreenStar")
    int greenStar;
    @CsvBindByName(column = "FacilitiesAndServices")
    String facilities;
    @CsvBindByName(column = "Description")
    String description;
    double distance;
    List<Review> reviews = new ArrayList<>();
    User user = new User();

    public Restaurant() {
        this.id = Objects.hash(name, longitude, latitude);
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    public static class Coordinate {
        private float longitude;
        private float latitude;
    }

}