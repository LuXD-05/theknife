package uni.insubria.theknife.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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
    public Map<String, User> loadUsers() throws IOException {

        File file = new File(USERS_JSON);

        // If file doesn't exist --> (creates new json file + returns empty Map)
        if (!file.exists()) {
            
            //TODO - create empty file 'users.json' to its location (to decide, see line 17) ???
            
            return new HashMap<>();
            
        }

        try (FileInputStream fis = new FileInputStream(file)) {

            return objectMapper.readValue(fis, new TypeReference<Map<String, User>>() {});

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
