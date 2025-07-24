package botinteractions.orquestador;

import com.microsoft.playwright.*;
import botinteractions.models.CookieData;
import botinteractions.models.Cuenta;
import botinteractions.services.FacebookInteractionService;

import java.util.List;

public class InteractionManager {

    private final FacebookInteractionService fbService;
    private final NstBrowserService nstBrowserService;

    public InteractionManager() {
        this.fbService = new FacebookInteractionService();
        this.nstBrowserService = new NstBrowserService();
    }

    public void iniciarBots(Cuenta cuenta, List<CookieData> cookies, String publicacionUrl, String comentario, String tipoReaccion, String profileId) {
        try (Playwright playwright = Playwright.create()) {

            // 1. Obtener lista de perfiles
            String perfilesJson = nstBrowserService.obtenerPerfiles();
            if (perfilesJson == null || !nstBrowserService.perfilExiste(perfilesJson, profileId)) {
                System.out.println("⚠️ El perfil con ID " + profileId + " no existe en NSTBrowser.");
                return;
            }

            // 2. Obtener WebSocket endpoint
            String wsEndpoint = nstBrowserService.obtenerWsEndpoint(profileId);
            if (wsEndpoint == null) {
                System.out.println("No se pudo obtener el WebSocket endpoint de NSTBrowser.");
                return;
            }

            Browser browser = playwright.chromium().connectOverCDP(wsEndpoint);
            BrowserContext context = browser.contexts().get(0); // Primer contexto disponible

            if (cookies == null || cookies.isEmpty()) {
                System.out.println("No se encontraron cookies. Iniciar sesión con email y contraseña.");
                fbService.iniciarSesionConCredenciales(context, cuenta.getEmail(), cuenta.getContrasena());
            } else {
                fbService.iniciarSesionConCookies(context, cookies);
            }

            // Humanizar espera inicial
            System.out.println("Esperando antes de aceptar solicitudes...");
            context.pages().get(0).waitForTimeout(180000); // 3 minutos

            fbService.aceptarSolicitudes(context);
            fbService.reaccionarComentarCompartir(context, publicacionUrl, comentario, tipoReaccion);

            System.out.println("Proceso completado para el perfil: " + cuenta.getEmail());
        }
    }
}
