package uni.insubria.theknife.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class Review {
    @EqualsAndHashCode.Include
    String id;
    User user;
    Restaurant restaurant;
    String content;
    Integer stars;
    String answer;
}





