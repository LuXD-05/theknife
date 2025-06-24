package uni.insubria.theknife.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.User;

/**
 * Repository for managing user data in the TheKnife application.
 * <p>
 * This class provides methods to interact with the repository of User objects,
 * including loading users from JSON files, and performing operations such as
 * adding users and retrieving user information.
 * </p>
 * <p>
 * The repository handles data persistence and serves as the data access layer
 * for user-related operations in the application.
 * </p>
 */
public class UserRepository {
    /**
     * Default constructor for the UserRepository class.
     * <p>
     * This constructor is not meant to be used directly as this class only provides
     * static methods. The class is not designed to be instantiated.
     * </p>
     */
    public UserRepository() {
        // Default constructor - not meant to be used
    }

    /**
     * Jackson ObjectMapper instance used for JSON serialization and deserialization.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Path to the JSON file used for storing and retrieving user data.
     */
    private static final String USERS_JSON = "data/users.json";

    /**
     * Enumeration of possible error codes returned by repository operations.
     */
    public enum ERROR_CODE {
        /**
         * Indicates that the operation failed because the entity already exists.
         */
        DUPLICATED,

        /**
         * Indicates that a service-level error occurred during the operation.
         */
        SERVICE_ERROR,

        /**
         * Indicates that the operation completed successfully with no errors.
         */
        NONE
    }

    /**
     * Loads all users from the JSON file.
     * <p>
     * If an error occurs during loading, returns an empty map.
     * </p>
     *
     * @return A map containing usernames as keys and corresponding User objects as values
     */
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

    /**
     * Saves the provided map of users to a JSON file.
     *
     * @param users A map containing usernames as keys and corresponding User objects as values
     * @throws IOException If an I/O error occurs during file writing
     */
    public static void saveUsers(Map<String, User> users) throws IOException {
        FileWriter fileWriter = new FileWriter(USERS_JSON, false); // true to append
        fileWriter.write(objectMapper.writeValueAsString(users));
        fileWriter.close();
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve
     * @return The User object with the specified username, or null if not found
     */
    public static User getUser(String username) {
        return loadUsers().get(username);
    }

    /**
     * Adds a new user to the repository if they do not already exist.
     *
     * @param user The User object to add
     * @return An ERROR_CODE indicating the result of the operation:
     *         - DUPLICATED if a user with the same username already exists
     *         - SERVICE_ERROR if an error occurs during saving
     *         - NONE if the user is successfully added
     */
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

    //TODO GITHUB TASK #9:    
    //#region Favorites CRUD

    /**
     * Adds a restaurant to the current user's favorite list.
     *
     * @param restaurant the restaurant to add to favorites
     * @return ERROR_CODE an error code indicating the result of the operation:
     * - DUPLICATED if the restaurant already exists in the repository
     * - SERVICE_ERROR if an error occurs during saving the repository
     * - NONE if the restaurant is successfully added
     */
    public static ERROR_CODE toggleFavoriteRestaurant(User user, Restaurant restaurant) {

        Map<String, User> users = loadUsers();
        HashSet<Restaurant> favorites = users.get(user.getUsername()).getRestaurants();

        if (favorites.contains(restaurant))
            favorites.remove(restaurant);
        else
            favorites.add(restaurant);

        // Replaces old user with the new one with updated favorites
        users.put(user.getUsername(), user);

        try {
            UserRepository.saveUsers(users);
        } catch (IOException e) {
            return ERROR_CODE.SERVICE_ERROR;
        }

        return ERROR_CODE.NONE;

    }


    
    //#endregion

}
