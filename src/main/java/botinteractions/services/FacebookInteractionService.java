// FacebookInteractionService.java
package botinteractions.services;

import botinteractions.models.CookieData;
import botinteractions.models.Cuenta;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState; // Importar LoadState
import com.microsoft.playwright.options.WaitForSelectorState; // Importar WaitForSelectorState

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class FacebookInteractionService {

    private static final String FACEBOOK_URL = "https://www.facebook.com";
    private DatabaseService databaseService = new DatabaseService(); // Instanciar DatabaseService

    // Selector y tiempo de espera para el desafío anti-bot
    private static final String ANTI_BOT_TEXT_SELECTOR = "p:has-text('Esto nos ayuda a combatir conductas dañinas, detectar y prevenir el spam y mantener la integridad de nuestros productos. Hemos utilizado MatchKey de Arkose Labs para proporcionar este control de seguridad.')";
    private static final int ANTI_BOT_TIMEOUT_MS = 360000; // 6 minutos

    public void ejecutar(BrowserContext context, Cuenta profile) {
        // Este método ya no es necesario si inicias sesión con credenciales directamente
        // o con cookies desde InteractionManager. Puedes eliminarlo si no lo usas.
        // iniciarSesionConCredenciales(context, profile.getEmail(), profile.getContrasena());
    }

    public void iniciarSesionConCookies(BrowserContext context, List<CookieData> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            System.out.println("No se encontraron cookies para esta cuenta.");
            return;
        }

        List<Cookie> playwrightCookies = cookies.stream()
            .map(CookieData::toPlaywrightCookie)
            .collect(Collectors.toList());

        context.addCookies(playwrightCookies);
        System.out.println("Cookies cargadas exitosamente en el navegador.");
    }

    // Nuevo método que encapsula la lógica de inicio de sesión, incluyendo la navegación
    public void iniciarSesion(Page page, String email, String contrasena) {
        // Navega a la URL principal de Facebook para verificar el estado de la sesión
        page.navigate(FACEBOOK_URL);
        page.waitForLoadState(LoadState.NETWORKIDLE); // Espera a que la red esté inactiva

        // Verifica si la página actual es la de login o checkpoint.
        // Si la URL actual ya contiene "facebook.com/login" o "facebook.com/checkpoint",
        // significa que ya estamos en una página de inicio de sesión o validación.
        // No es necesario navegar de nuevo a FACEBOOK_URL + "/login".
        if (page.url().contains("facebook.com/login") || page.url().contains("facebook.com/checkpoint")) {
            System.out.println("⚠️ Sesión no activa o requiere validación para: " + email + ". Procediendo con inicio de sesión con credenciales.");
            // No se navega de nuevo aquí, ya estamos en la página de login/checkpoint.
            iniciarSesionConCredenciales(page, email, contrasena);
        } else {
            System.out.println("✅ Sesión activa con perfil persistente para: " + email);
            // Si la sesión ya está activa, no se hace nada más aquí, se continúa con las interacciones.
        }
    }


    // The Page instance is passed directly to interact with the login form
    public void iniciarSesionConCredenciales(Page page, String email, String contrasena) {
        // Asegurarse de que la página esté completamente cargada antes de interactuar con los elementos
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Random random = new Random();
        // Aumentar el tiempo de espera inicial
        page.waitForTimeout(2000 + random.nextInt(2000)); // De 2-4 segundos

        // Localizar el campo de email usando el atributo placeholder
        page.click("input[placeholder='Correo electrónico o número de teléfono']");
        escribirHumanizado(page, email, 150, 300); // Aumentar el rango de delay por caracter

        // Aumentar el tiempo de espera entre campos
        page.waitForTimeout(1000 + random.nextInt(1000)); // De 1-2 segundos

        // Localizar el campo de contraseña usando el atributo placeholder
        page.click("input[placeholder='Contraseña']");
        escribirHumanizado(page, contrasena, 180, 350); // Aumentar el rango de delay por caracter

        // Aumentar el tiempo de espera antes de hacer clic en el botón de login
        page.waitForTimeout(1000 + random.nextInt(2000)); // De 1-3 segundos

        page.click("button[name='login']");

        boolean loggedIn = false;
        try {
            // Intento rápido de verificar si ya se inició sesión
            page.waitForSelector("input[placeholder='Buscar en Facebook']",
                new Page.WaitForSelectorOptions().setTimeout(15000)); // 15 segundos
            loggedIn = true;
        } catch (PlaywrightException e) {
            System.out.println("No se detectó el inicio de sesión directo, verificando anti-bot...");
            // Si el login directo falla, verificar si es un desafío anti-bot
            try {
                // Espera corta para el texto anti-bot
                page.waitForSelector(ANTI_BOT_TEXT_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
                manejarAntiBotChallenge(page); // Llama a la función para manejar el desafío

                // Después de intentar manejar el desafío, intentar verificar el inicio de sesión nuevamente
                page.waitForSelector("input[placeholder='Buscar en Facebook']",
                    new Page.WaitForSelectorOptions().setTimeout(30000)); // 30 segundos para confirmar login después del desafío
                loggedIn = true;
            } catch (PlaywrightException antiBotException) {
                System.out.println("⚠️ No se detectó desafío anti-bot o el inicio de sesión falló después del desafío: " + antiBotException.getMessage());
            }
        }

        if (loggedIn) {
            System.out.println("✅ Sesión iniciada de forma humanizada.");
            List<Cookie> currentCookies = page.context().cookies();
            databaseService.guardarCookies(email, currentCookies);
        } else {
            System.out.println("⚠️ El inicio de sesión no fue exitoso después de todos los intentos.");
        }
        // No cerramos la página aquí, InteractionManager la cerrará
    }

    private void escribirHumanizado(Page page, String texto, int minDelay, int maxDelay) {
        Random random = new Random();
        for (char c : texto.toCharArray()) {
            page.keyboard().press(String.valueOf(c));
            page.waitForTimeout(minDelay + random.nextInt(maxDelay - minDelay));
        }
    }

    public void aceptarSolicitudes(Page page) { // Recibe Page directamente
        page.navigate(FACEBOOK_URL + "/friends/requests");
        page.waitForLoadState(LoadState.NETWORKIDLE); // Esperar a que la página cargue completamente

        try {
            // Aumentar el timeout para el selector de Confirmar/Confirm
            page.waitForSelector("span:has-text('Confirmar'), span:has-text('Confirm')", new Page.WaitForSelectorOptions().setTimeout(30000)); // 30 segundos

            Locator confirmButtons = page.locator("span:has-text('Confirmar'), span:has-text('Confirm')");
            int count = 0;
            while (confirmButtons.count() > 0) {
                confirmButtons.first().click();
                count++;
                page.waitForTimeout(500 + new Random().nextInt(500)); // Pequeña espera entre clics
                confirmButtons = page.locator("span:has-text('Confirmar'), span:has-text('Confirm')"); // Actualizado el selector
            }
            System.out.println("✅ " + count + " solicitudes de amistad aceptadas.");
        } catch (PlaywrightException e) {
            System.out.println("⚠️ No se encontraron solicitudes de amistad o el botón de Confirmar: " + e.getMessage());
        }

        // No cerramos la página aquí, InteractionManager la cerrará
    }


    public void reaccionarComentarCompartir(Page page, String publicacionUrl, String comentario, String tipoReaccion) { // Recibe Page directamente
        page.navigate(publicacionUrl); // Usar la URL de la publicación
        page.waitForLoadState(LoadState.NETWORKIDLE); // Esperar a que la página cargue completamente
        page.waitForTimeout(3000);

        // Añadir scroll hacia abajo para asegurar que los elementos sean visibles
        page.evaluate("window.scrollBy(0, 500)");
        page.waitForTimeout(1000);

        reaccionarAPublicacion(page, tipoReaccion);

        // Primero, haz clic en el botón de "Comment"
        Locator commentButton = page.locator("text=Comment");
        commentButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000)); // Aumentar timeout
        commentButton.click();
        page.waitForTimeout(1000);

        // Ahora, asegúrate de que el campo de comentario esté visible y sea interactuable
        Locator commentBox = page.locator("div[aria-label='Escribe un comentario...']");
        commentBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000)); // Aumentar timeout
        commentBox.click();
        escribirHumanizado(page, comentario, 50, 150);
        page.keyboard().press("Enter");

        // Compartir
        Locator shareButton = page.locator("text=Compartir").first();
        if (shareButton.isVisible()) {
            shareButton.click();
            page.waitForTimeout(1000);
            Locator shareNowButton = page.locator("text=Compartir ahora (Amigos)").first();
            if (shareNowButton.isVisible()) {
                shareNowButton.click();
                System.out.println("Reaccionó, comentó y compartió en: " + publicacionUrl);
            } else {
                System.out.println("No se encontró el botón 'Compartir ahora (Amigos)'.");
            }
        } else {
            System.out.println("No se encontró el botón 'Compartir'.");
        }

        // No cerramos la página aquí, InteractionManager la cerrará
    }

    private void reaccionarAPublicacion(Page page, String tipoReaccion) { // Recibe Page directamente
        // Usar el selector de texto para el botón "Like"
        Locator reaccionBtn = page.locator("text=Like");
        reaccionBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000)); // Aumentar timeout
        reaccionBtn.hover();
        page.waitForTimeout(1000);

        String selectorReaccion;
        switch (tipoReaccion.toLowerCase()) {
            case "me encanta":
                selectorReaccion = "div[aria-label='Me encanta']";
                break;
            case "me divierte":
                selectorReaccion = "div[aria-label='Me divierte']";
                break;
            case "me asombra":
                selectorReaccion = "div[aria-label='Me asombra']";
                break;
            case "me entristece":
                selectorReaccion = "div[aria-label='Me entristece']";
                break;
            case "me enoja":
                selectorReaccion = "div[aria-label='Me enoja']";
                break;
            default:
                selectorReaccion = "div[aria-label='Me gusta']";
                break;
        }

        page.locator(selectorReaccion).click();
    }

    // Nuevo método para manejar el desafío anti-bot
    private void manejarAntiBotChallenge(Page page) {
        System.out.println("⏳ Detectado control de seguridad anti-bot. Esperando hasta " + (ANTI_BOT_TIMEOUT_MS / 60000) + " minutos para que se complete manualmente.");
        try {
            // Esperar a que el texto del anti-bot desaparezca (indicando que el desafío se completó)
            page.waitForSelector(ANTI_BOT_TEXT_SELECTOR, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(ANTI_BOT_TIMEOUT_MS));
            System.out.println("✅ Control de seguridad anti-bot completado o página de desafío cerrada.");
        } catch (PlaywrightException e) {
            System.out.println("⚠️ El control de seguridad anti-bot no se completó en " + (ANTI_BOT_TIMEOUT_MS / 60000) + " minutos.");
        }
    }
}