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

        // Verificar si la URL actual NO es la página de login o checkpoint.
        // Si no es ninguna de esas, asumimos que la sesión está activa.
        if (!page.url().contains("facebook.com/login") && !page.url().contains("facebook.com/checkpoint")) {
            System.out.println("✅ Sesión activa con perfil persistente para: " + email);
            // Si la sesión ya está activa, no se hace nada más aquí, se continúa con las interacciones.
            return; // Salir del método ya que no se necesita iniciar sesión
        }

        // Si llegamos aquí, la sesión no está activa o requiere validación.
        System.out.println("⚠️ Sesión no activa o requiere validación para: " + email + ". Redirigiendo e iniciando sesión con credenciales.");
        // Navegar directamente a la página de login si no estamos logueados
        page.navigate(FACEBOOK_URL + "/login");
        page.waitForLoadState(LoadState.NETWORKIDLE); // Espera a que la página de login cargue
        iniciarSesionConCredenciales(page, email, contrasena);
    }


    // The Page instance is passed directly to interact with the login form
    public void iniciarSesionConCredenciales(Page page, String email, String contrasena) {
        // Asegurarse de que la página esté completamente cargada antes de interactuar con los elementos
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Random random = new Random();
        // Aumentar el tiempo de espera inicial
        page.waitForTimeout(2000 + random.nextInt(2000)); // De 2-4 segundos

        // **NUEVO: Esperar explícitamente por el campo de email antes de hacer clic**
        page.waitForSelector("input[placeholder='Correo electrónico o número de teléfono']", new Page.WaitForSelectorOptions().setTimeout(30000)); // Espera hasta 30 segundos

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
            // Intento de verificar si ya se inició sesión buscando la barra de búsqueda
            page.waitForSelector("input[placeholder='Buscar en Facebook']",
                new Page.WaitForSelectorOptions().setTimeout(30000)); // Darle 30 segundos para cargar
            loggedIn = true; // Si se encuentra, el login fue exitoso
        } catch (PlaywrightException e) {
            // Si la barra de búsqueda no se encuentra, entonces verificamos el anti-bot
            System.out.println("Barra de búsqueda no encontrada después del intento de login. Verificando desafío anti-bot...");
            try {
                // Espera corta para el texto anti-bot
                page.waitForSelector(ANTI_BOT_TEXT_SELECTOR, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
                manejarAntiBotChallenge(page); // Llama a la función para manejar el desafío

                // Después de intentar manejar el desafío, intentar verificar el inicio de sesión nuevamente
                page.waitForSelector("input[placeholder='Buscar en Facebook']",
                    new Page.WaitForSelectorOptions().setTimeout(30000)); // 30 segundos para confirmar login después del desafío
                loggedIn = true; // Login confirmado después de anti-bot
            } catch (PlaywrightException antiBotException) {
                System.out.println("⚠️ Desafío anti-bot no detectado o el inicio de sesión falló después de intentar manejar el desafío: " + antiBotException.getMessage());
                // Si el anti-bot no estaba o falló, loggedIn permanece false.
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
        page.keyboard().type(String.valueOf(c));
        page.waitForTimeout(minDelay + random.nextInt(maxDelay - minDelay));
    }
}

    public void aceptarSolicitudes(Page page) { // Recibe Page directamente
        page.navigate(FACEBOOK_URL + "/friends/requests");
        page.waitForLoadState(LoadState.NETWORKIDLE); // Esperar a que la página cargue completamente

        // Realizar un scroll para cargar posibles solicitudes que no estén visibles al inicio
        page.evaluate("window.scrollBy(0, document.body.scrollHeight)");
        page.waitForTimeout(2000); // Pequeña espera después del scroll

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


    public void reaccionarComentarCompartir(Page page, String publicacionUrl, String comentario, String tipoReaccion) {
    page.navigate(publicacionUrl);
    page.waitForLoadState(LoadState.NETWORKIDLE);
    page.waitForTimeout(4000); // Espera extra para carga completa

    page.evaluate("window.scrollBy(0, 1000)");
    page.waitForTimeout(2000); // Espera extra para que cargue el área de comentarios

    reaccionarAPublicacion(page, tipoReaccion);

    // Selector robusto para el botón "Comentar"/"Comment"
    Locator commentButton = null;
if (page.locator("div[aria-label='Comentar']").count() > 0) {
    commentButton = page.locator("div[aria-label='Comentar']").first();
} else if (page.locator("div[aria-label='Comment']").count() > 0) {
    commentButton = page.locator("div[aria-label='Comment']").first();
} else if (page.locator("span:has-text('Comentar')").count() > 0) {
    commentButton = page.locator("span:has-text('Comentar')").first();
} else if (page.locator("span:has-text('Comment')").count() > 0) {
    commentButton = page.locator("span:has-text('Comment')").first();
}

if (commentButton != null) {
    try {
        commentButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(40000));
        commentButton.click(new Locator.ClickOptions().setForce(true));
        System.out.println("✅ Botón de comentar presionado.");
        page.waitForTimeout(1500); // Espera tras abrir el modal

        // NO hagas scroll aquí

        // Selector específico para el campo de comentario dentro del modal
        Locator commentBox = page.locator("div[contenteditable='true'][role='textbox'][aria-label^='Write a comment']").first();
        commentBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

        if (commentBox.isVisible()) {
            commentBox.click();
            page.waitForTimeout(500); // Espera para asegurar el foco
            System.out.println("Intentando escribir el comentario: " + comentario);
            page.keyboard().type(comentario, new Keyboard.TypeOptions().setDelay(100));
            page.keyboard().press("Enter");
            System.out.println("✅ Comentario realizado.");
            page.waitForTimeout(2000); // Espera para asegurar que el comentario se publique
        } else {
            System.out.println("⚠️ El campo de comentario no está visible en el modal.");
            // Buscar y cerrar el modal
            Locator closeModalBtn = page.locator("div[aria-label='Close'][role='button']").first();
            if (closeModalBtn.isVisible()) {
                closeModalBtn.click();
                System.out.println("✅ Modal de comentario cerrado.");
                page.waitForTimeout(1000);
            } else {
                System.out.println("⚠️ No se encontró el botón para cerrar el modal.");
            }
        }
    } catch (PlaywrightException e) {
        System.out.println("⚠️ No se pudo comentar: " + e.getMessage());
        // Intentar cerrar el modal si ocurre un error
        Locator closeModalBtn = page.locator("div[aria-label='Close'][role='button']").first();
        if (closeModalBtn.isVisible()) {
            closeModalBtn.click();
            System.out.println("✅ Modal de comentario cerrado tras error.");
            page.waitForTimeout(1000);
        } else {
            System.out.println("⚠️ No se encontró el botón para cerrar el modal tras error.");
        }
    }
} else {
    System.out.println("⚠️ No se encontró ningún botón de comentar.");
}

// Espera tras abrir el modal de comentarios
page.waitForTimeout(1500);

// Selector específico para el campo de comentario en el modal
Locator commentBox = page.locator("div[contenteditable='true'][role='textbox'][aria-label^='Write a comment']").first();
try {
    commentBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
    if (commentBox.isVisible()) {
        commentBox.click();
        page.waitForTimeout(500); // Espera para asegurar el foco
        System.out.println("Intentando escribir el comentario: " + comentario);
        page.keyboard().type(comentario, new Keyboard.TypeOptions().setDelay(100));
        page.keyboard().press("Enter");
        System.out.println("✅ Comentario realizado.");
        page.waitForTimeout(2000); // Espera para asegurar que el comentario se publique
    } else {
        System.out.println("⚠️ El campo de comentario no está visible en el modal.");
        // Buscar y cerrar el modal
        Locator closeModalBtn = page.locator("div[aria-label='Close'][role='button']").first();
        if (closeModalBtn.isVisible()) {
            closeModalBtn.click();
            System.out.println("✅ Modal de comentario cerrado.");
            page.waitForTimeout(1000);
        } else {
            System.out.println("⚠️ No se encontró el botón para cerrar el modal.");
        }
    }
} catch (PlaywrightException e) {
    System.out.println("⚠️ No se pudo comentar: " + e.getMessage());
    // Intentar cerrar el modal si ocurre un error
    Locator closeModalBtn = page.locator("div[aria-label='Close'][role='button']").first();
    if (closeModalBtn.isVisible()) {
        closeModalBtn.click();
        System.out.println("✅ Modal de comentario cerrado tras error.");
        page.waitForTimeout(1000);
    } else {
        System.out.println("⚠️ No se encontró el botón para cerrar el modal tras error.");
    }
}

// ...compartir como ya tienes...
    System.out.println("Reaccionó, comentó y compartió en: " + publicacionUrl);
    page.waitForTimeout(2000); // Espera final antes de cerrar la página

    // Compartir
    Locator shareButton = page.locator("text=Compartir").first();
    if (shareButton.isVisible()) {
            shareButton.click();
            page.waitForTimeout(1000);
            Locator shareNowButton = page.locator("text=Compartir ahora (Amigos)").first();
            if (shareNowButton.isVisible()) {
                shareNowButton.click();
                System.out.println("✅ Publicación compartida.");
            } else {
                System.out.println("⚠️ No se encontró el botón 'Compartir ahora (Amigos)'.");
            }
        } else {
            System.out.println("⚠️ No se encontró el botón 'Compartir'.");
        }

        System.out.println("Reaccionó, comentó y compartió en: " + publicacionUrl);

        // No cerramos la página aquí, InteractionManager la cerrará
    }

    private void reaccionarAPublicacion(Page page, String tipoReaccion) {
    // Selector robusto para el botón "Me gusta"/"Like"
    Locator reaccionBtn = page.locator("div[aria-label='Me gusta'], div[aria-label='Like']").first();
    try {
        reaccionBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(30000));
        reaccionBtn.hover();
        page.waitForTimeout(1000);

        String selectorReaccion;
        switch (tipoReaccion.toLowerCase()) {
            case "me encanta":
                selectorReaccion = "div[aria-label='Me encanta'], div[aria-label='Love']";
                break;
            case "me divierte":
                selectorReaccion = "div[aria-label='Me divierte'], div[aria-label='Haha']";
                break;
            case "me asombra":
                selectorReaccion = "div[aria-label='Me asombra'], div[aria-label='Wow']";
                break;
            case "me entristece":
                selectorReaccion = "div[aria-label='Me entristece'], div[aria-label='Sad']";
                break;
            case "me enoja":
                selectorReaccion = "div[aria-label='Me enoja'], div[aria-label='Angry']";
                break;
            default:
                selectorReaccion = "div[aria-label='Me gusta'], div[aria-label='Like']";
                break;
        }

        Locator reaccionFinal = page.locator(selectorReaccion).first();
        reaccionFinal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        reaccionFinal.click();
        System.out.println("✅ Reacción '" + tipoReaccion + "' realizada.");
    } catch (PlaywrightException e) {
        System.out.println("⚠️ No se encontró el botón de reacción: " + e.getMessage());
    }
}

    // Nuevo método para manejar el desafío anti-bot
    private void manejarAntiBotChallenge(Page page) {
        System.out.println("⏳ Detected anti-bot security control. Waiting up to " + (ANTI_BOT_TIMEOUT_MS / 60000) + " minutes for manual completion.");
        try {
            // Wait for the anti-bot text to disappear (indicating the challenge is complete)
            page.waitForSelector(ANTI_BOT_TEXT_SELECTOR, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(ANTI_BOT_TIMEOUT_MS));
            System.out.println("✅ Anti-bot security control completed or challenge page closed.");
        } catch (PlaywrightException e) {
            System.out.println("⚠️ Anti-bot security control not completed in " + (ANTI_BOT_TIMEOUT_MS / 60000) + " minutes.");
        }
    }
}
