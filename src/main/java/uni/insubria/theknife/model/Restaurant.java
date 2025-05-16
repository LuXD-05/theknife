package uni.insubria.theknife.model;

import com.opencsv.bean.CsvBindByName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Restaurant {
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
    List<Review> reviews = new ArrayList<>();
    User user = new User();
}





