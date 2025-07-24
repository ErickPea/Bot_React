// Main.java
package botinteractions;

import botinteractions.orquestador.NstBrowserService;

public class Main {
    public static void main(String[] args) {
        String[] profileIds = {
            "2ade7b9c-2a9f-4f59-a102-3a9e641c92a2",
            "afed5411-d9c9-48c7-a54b-1095538e0aa3",
            "34755b4d-0e3a-4e54-8c3f-8751e3548054",
            "15d30eef-c4c3-4fb4-91ee-c05cfa1a4c8b"
        };

        NstBrowserService nstService = new NstBrowserService();

        for (String profileId : profileIds) {
            System.out.println("Ejecutando bot con perfil: " + profileId);
            nstService.ejecutarConPerfil(profileId);
        }
    }
}
