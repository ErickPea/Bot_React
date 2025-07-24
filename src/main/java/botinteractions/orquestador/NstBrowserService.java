// NstBrowserService.java
package botinteractions.orquestador;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class NstBrowserService {

    private static final String API_KEY = "85b9a76c-191b-4916-a408-83ed21de8c5c";
    private static final String BASE_URL = "http://localhost:8888";
    private static final String API_URL_PERFILES = BASE_URL + "/api/v2/profiles/";
    private static final String API_URL_WS = BASE_URL + "/api/v2/cdpEndpoints/connect?profileId=";
    private static final String API_URL_START_PROFILE = BASE_URL + "/api/v2/profiles/start/";

    private final OkHttpClient client = new OkHttpClient();

    public String obtenerPerfiles() {
        Request request = new Request.Builder()
                .url(API_URL_PERFILES)
                .addHeader("x-api-key", API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Error al obtener perfiles: " + response);
                return "[]";
            }
            String body = response.body().string();
            System.out.println("Respuesta cruda de perfiles: " + body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public boolean perfilExiste(String perfilesJson, String profileId) {
        try {
            JSONObject jsonObject = new JSONObject(perfilesJson);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray docs = data.getJSONArray("docs");

            for (int i = 0; i < docs.length(); i++) {
                JSONObject perfil = docs.getJSONObject(i);
                if (profileId.equals(perfil.getString("profileId"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error al procesar perfiles JSON: " + perfilesJson);
            e.printStackTrace();
        }
        return false;
    }

    public String obtenerWsEndpoint(String profileId) {
        String url = API_URL_WS + profileId;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Error al obtener el wsEndpoint: " + response);
                return null;
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getString("webSocketDebuggerUrl");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean abrirPerfil(String profileId) {
        String url = API_URL_START_PROFILE + profileId;
        System.out.println("Intentando abrir perfil en URL: " + url);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Perfil abierto correctamente: " + profileId);
                return true;
            } else {
                System.out.println("Error al abrir perfil (status " + response.code() + "): " + response.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void conectarConPlaywright(String wsEndpoint) {
        try {
            System.out.println("Conectando a Playwright con el wsEndpoint: " + wsEndpoint);
            // Aquí deberías usar un cliente WebSocket o librería externa si lo deseas.
            // En este ejemplo solo mostramos el endpoint.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ejecutarConPerfil(String profileId) {
        String perfilesJson = obtenerPerfiles();

        if (!perfilExiste(perfilesJson, profileId)) {
            System.out.println("El perfil con ID " + profileId + " no existe.");
            return;
        }

        if (!abrirPerfil(profileId)) {
            System.out.println("No se pudo abrir el perfil.");
            return;
        }

        String wsEndpoint = obtenerWsEndpoint(profileId);
        if (wsEndpoint == null || wsEndpoint.isEmpty()) {
            System.out.println("No se pudo obtener el WebSocket Endpoint.");
            return;
        }

        conectarConPlaywright(wsEndpoint);
    }
}
