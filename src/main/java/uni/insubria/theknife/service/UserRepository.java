package uni.insubria.theknife.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uni.insubria.theknife.model.User;

public class UserRepository {

    //TODO - Decide the final users.json path
    //! NOT WORKING if path does not start with '/' (InputStream is null)
    private static final String USERS_JSON = "/data/users.json";

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 
     * @return
     * @throws IOException
     */
    public ArrayList<User> loadUsers() throws IOException {

        InputStream is = UserRepository.class.getResourceAsStream(USERS_JSON);

        // If file doesn't exist --> (creates new json file + returns empty Map)
        if (is == null) {
            return new ArrayList<>();
        }

        try {

            return objectMapper.readValue(is, new TypeReference<ArrayList<User>>() {});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 
     * @param users
     * @throws IOException
     */
    public void saveUsers(Map<String, User> users) throws IOException {

        //TODO - Verify if the path to the file (all folders) exist --> IF NOT, create make them

        // Writes the json file
        objectMapper.writeValue(new File(USERS_JSON), users);

    }

}
