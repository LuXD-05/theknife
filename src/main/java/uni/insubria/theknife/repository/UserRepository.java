package uni.insubria.theknife.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.ObjectWriter;
import uni.insubria.theknife.model.User;

public class UserRepository {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    private static final String USERS_JSON;

    static {
        if (UserRepository.class.getClassLoader().getResource("data/users.json") != null) {
            USERS_JSON = Objects.requireNonNull(UserRepository.class.getClassLoader().getResource("data/users.json")).getFile();
        } else {
            USERS_JSON = new File(Objects.requireNonNull(UserRepository.class.getClassLoader().getResource("data")).getFile(), "users.json").getAbsolutePath();
        }
    }

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
