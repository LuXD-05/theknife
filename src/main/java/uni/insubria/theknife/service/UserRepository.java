package uni.insubria.theknife.service;

import uni.insubria.theknife.model.User;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;

public class UserRepository {

    private ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, User> loadUsers() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/utenti.json")) {
            if (inputStream == null) {
                throw new IOException("File non trovato");
            }
            return objectMapper.readValue(inputStream, new TypeReference<Map<String, User>>() {});
        }
    }
    public void saveUsers(Map<String, User> users) throws IOException {
        objectMapper.writeValue(new File("utenti.json"), users);
    }
}
