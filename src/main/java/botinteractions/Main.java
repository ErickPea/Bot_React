package botinteractions;

import botinteractions.orquestador.NstBrowserService;

public class Main {
    public static void main(String[] args) {
        // Primero intentemos obtener los perfiles disponibles
        NstBrowserService nstService = new NstBrowserService();
        String allProfilesJson = nstService.obtenerPerfiles();

        System.out.println("\n=== INFORMACIÓN DE NstBrowser ===");
        System.out.println("Estado de la conexión: " + 
            (allProfilesJson != null && !"[]".equals(allProfilesJson) ? "Conectado" : "Desconectado"));

        if (allProfilesJson != null && !"[]".equals(allProfilesJson)) {
            System.out.println("\n=== PERFILES DISPONIBLES ===");
            try {
                org.json.JSONObject json = new org.json.JSONObject(allProfilesJson);
                if (json.has("data")) {
                    org.json.JSONArray profiles = json.getJSONArray("data");
                    if (profiles.length() > 0) {
                        System.out.println("ID\t\t\t\t\tNombre");
                        for (int i = 0; i < profiles.length(); i++) {
                            org.json.JSONObject profile = profiles.getJSONObject(i);
                            String id = profile.getString("profileId");
                            String name = profile.getString("name");
                            System.out.println(id + "\t" + name);
                        }
                    } else {
                        System.out.println("No se encontraron perfiles disponibles");
                    }
                } else {
                    System.out.println("Formato de respuesta no válido");
                }
            } catch (Exception e) {
                System.out.println("Error al procesar los perfiles: " + e.getMessage());
            }
        }

        // Lista de perfiles que intentaremos usar
        String[] profileIds = {
            "2ade7b9c-2a9f-4f59-a102-3a9e641c92a2",
            "afed5411-d9c9-48c7-a54b-1095538e0aa3",
            "34755b4d-0e3a-4e54-8c3f-8751e3548054",
            "15d30eef-c4c3-4fb4-91ee-c05cfa1a4c8b"
        };

        System.out.println("\n=== VALIDACIÓN DE PERFILES ===");
        for (String profileId : profileIds) {
            if (nstService.perfilExiste(allProfilesJson, profileId)) {
                System.out.println("✓ Perfil válido: " + profileId);
            } else {
                System.out.println("✗ Perfil no encontrado: " + profileId);
            }
        }

        System.out.println("\n=== EJECUTANDO BOTS ===");
        for (String profileId : profileIds) {
            if (nstService.perfilExiste(allProfilesJson, profileId)) {
                System.out.println("\nEjecutando bot con perfil: " + profileId);
                nstService.ejecutarConPerfil(profileId);
            } else {
                System.out.println("Omitiendo perfil no encontrado: " + profileId);
            }
        }
    }
}
