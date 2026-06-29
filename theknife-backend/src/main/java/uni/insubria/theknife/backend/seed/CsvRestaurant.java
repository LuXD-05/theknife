/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.seed;

import com.opencsv.bean.CsvBindByName;

import java.util.Objects;

/**
 * OpenCSV bean mirroring {@code michelin_my_maps.csv}. Used only by {@link DataImporter}.
 * <p>
 * Latitude/longitude are kept as {@link Float} on purpose: the legacy restaurant id is
 * {@code Objects.hash(name, latitude, longitude)} computed on {@code Float} values, and
 * the existing reviews/favorites reference those exact ids — so the id must be reproduced
 * with identical semantics here.
 */
public class CsvRestaurant {

    @CsvBindByName(column = "Name")
    public String name;

    @CsvBindByName(column = "Address")
    public String address;

    @CsvBindByName(column = "Location")
    public String location;

    @CsvBindByName(column = "Price")
    public String price;

    @CsvBindByName(column = "Cuisine")
    public String cuisine;

    @CsvBindByName(column = "Longitude")
    public Float longitude;

    @CsvBindByName(column = "Latitude")
    public Float latitude;

    @CsvBindByName(column = "PhoneNumber")
    public String phone;

    @CsvBindByName(column = "Url")
    public String michelinUrl;

    @CsvBindByName(column = "WebsiteUrl")
    public String websiteUrl;

    @CsvBindByName(column = "Award")
    public String award;

    @CsvBindByName(column = "GreenStar")
    public Integer greenStar;

    @CsvBindByName(column = "FacilitiesAndServices")
    public String facilities;

    @CsvBindByName(column = "Description")
    public String description;

    /**
     * Reproduces the legacy id: {@code String.valueOf(Objects.hash(name, latitude, longitude))}.
     */
    public String legacyId() {
        return String.valueOf(Objects.hash(name, latitude, longitude));
    }
}
