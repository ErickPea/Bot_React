package botinteractions.orquestador;

import botinteractions.models.Profile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    public List<Profile> obtenerPerfiles(String perfilesJson) {
        List<Profile> perfiles = new ArrayList<>();
        try {
            JSONObject respuesta = new JSONObject(perfilesJson);
            JSONObject data = respuesta.getJSONObject("data");
            JSONArray docs = data.getJSONArray("docs");

            for (int i = 0; i < docs.length(); i++) {
                JSONObject perfil = docs.getJSONObject(i);
                String name = perfil.getString("name");
                String id = perfil.getString("profileId");
                perfiles.add(new Profile(name, id));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return perfiles;
    }

    public boolean perfilExiste(String perfilesJson, String profileId) {
        List<Profile> perfiles = obtenerPerfiles(perfilesJson);
        for (Profile perfil : perfiles) {
            if (perfil.getProfileId().equals(profileId) || perfil.getName().equals(profileId)) {
                System.out.println("Perfil encontrado: " + perfil);
                return true;
            }
        }
        return false;
    }
}
