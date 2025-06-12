package uni.insubria.theknife.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import uni.insubria.theknife.model.User;

public class UserRepository {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_JSON = "data/users.json";

    public enum ERROR_CODE {
        DUPLICATED,
        SERVICE_ERROR,
        NONE
    }

    public static Map<String, User> loadUsers() {
        File file = new File(USERS_JSON);
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, User> users = objectMapper.readValue(fis, Map.class);
            users.keySet().forEach(key -> users.put(key,objectMapper.convertValue(users.get(key), User.class)));
            return users;
        } catch (Exception e) {
            System.out.println("Invalid users file.");
            return new HashMap<>();
        }
    }

    public static void saveUsers(Map<String, User> users) throws IOException {
        FileWriter fileWriter = new FileWriter(USERS_JSON, false); // true to append
        fileWriter.write(objectMapper.writeValueAsString(users));
        fileWriter.close();
    }

    public static User getUser(String username) {
        return loadUsers().get(username);
    }

    public static ERROR_CODE addUser(User user) {
        Map<String, User> users = loadUsers();
        if (users.containsKey(user.getUsername())) return ERROR_CODE.DUPLICATED;
        users.put(user.getUsername(), user);
        try {
            saveUsers(users);
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }
        return ERROR_CODE.NONE;
    }
}
