package botinteractions.orquestador;

import botinteractions.models.Profile;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileManager {

    private final List<Profile> profiles;

    public ProfileManager() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("profiles.json");
        if (inputStream == null) {
            throw new FileNotFoundException("profiles.json no encontrado en resources");
        }

        String jsonContent = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));

        Gson gson = new Gson();
        ProfileDataWrapper wrapper = gson.fromJson(jsonContent, ProfileDataWrapper.class);
        if (wrapper == null || wrapper.getData() == null) {
            throw new IllegalStateException("Estructura de JSON incorrecta: se esperaba un array dentro de 'data'");
        }

        profiles = wrapper.getData();
        System.out.println("Profiles loaded: " + profiles.size());
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public Profile getProfileByName(String name) {
        return profiles.stream()
                .filter(profile -> profile.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private static class ProfileDataWrapper {
        @SerializedName("data")
        private List<Profile> data;

        public List<Profile> getData() {
            return data;
        }
    }
}
