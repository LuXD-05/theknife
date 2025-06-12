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
    String id;
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
    @CsvBindByName(column = "Longitude")
    Float longitude;
    @CsvBindByName(column = "Latitude")
    Float latitude;
    @CsvBindByName(column = "PhoneNumber")
    String phone;
    @CsvBindByName(column = "Url")
    String michelinUrl;
    @CsvBindByName(column = "WebsiteUrl")
    String websiteUrl;
    @CsvBindByName(column = "Award")
    String award;
    @CsvBindByName(column = "GreenStar")
    Integer greenStar;
    @CsvBindByName(column = "FacilitiesAndServices")
    String facilities;
    @CsvBindByName(column = "Description")
    String description;
    Double distance;
    List<Review> reviews = new ArrayList<>();
    User user = null;

    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    public static class Coordinate {
        private Float longitude;
        private Float latitude;
    }

}