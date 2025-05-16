package uni.insubria.theknife.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Review {
    User user;
    Restaurant restaurant;
    String content;
    Integer stars;
    String answer;
}





