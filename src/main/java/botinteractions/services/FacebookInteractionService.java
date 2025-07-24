package botinteractions.services;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import botinteractions.models.CookieData;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Random;

public class FacebookInteractionService {

    private static final String FACEBOOK_URL = "https://www.facebook.com";

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

  

public void iniciarSesionConCredenciales(BrowserContext context, String email, String contrasena) {
    Page page = context.newPage();
    page.navigate(FACEBOOK_URL + "/login");

    Random random = new Random();

    // Simular una pausa breve antes de escribir
    page.waitForTimeout(1000 + random.nextInt(1000));

    // Escribir el email carácter por carácter
    page.click("input#email");
    escribirHumanizado(page, email, 100, 200);

    page.waitForTimeout(500 + random.nextInt(500));

    // Escribir la contraseña carácter por carácter
    page.click("input#pass");
    escribirHumanizado(page, contrasena, 120, 250);

    page.waitForTimeout(500 + random.nextInt(1000));

    // Clic en el botón de login
    page.click("button[name='login']");
    page.waitForTimeout(230000);
    // Esperar hasta que el campo de búsqueda aparezca o 15s
    try {
        page.waitForSelector("input[placeholder='Buscar en Facebook']", 
            new Page.WaitForSelectorOptions().setTimeout(15000));
        System.out.println("✅ Sesión iniciada de forma humanizada.");
    } catch (PlaywrightException e) {
        System.out.println("⚠️ El inicio de sesión podría no haber sido exitoso: " + e.getMessage());
    }

    page.close();
}

// Método helper para escribir simulando comportamiento humano
private void escribirHumanizado(Page page, String texto, int minDelay, int maxDelay) {
    Random random = new Random();
    for (char c : texto.toCharArray()) {
        page.keyboard().press(String.valueOf(c));
        page.waitForTimeout(minDelay + random.nextInt(maxDelay - minDelay));
       
    }
}




public void aceptarSolicitudes(BrowserContext context) {
    Page page = context.newPage();
    page.navigate(FACEBOOK_URL + "/friends/requests");

    

    try {
        page.waitForSelector("text=Confirmar", new Page.WaitForSelectorOptions().setTimeout(10000));
        page.locator("text=Confirm").first().click();
        System.out.println("✅ Solicitudes de amistad aceptadas.");
    } catch (PlaywrightException e) {
        System.out.println("⚠️ No se encontró el botón de Confirmar: " + e.getMessage());
    }

    page.close();
}


    public void reaccionarComentarCompartir(BrowserContext context, String publicacionUrl, String comentario, String tipoReaccion) {
        Page page = context.newPage();
        page.navigate(publicacionUrl);
        page.waitForTimeout(3000);

        reaccionarAPublicacion(page, tipoReaccion);

        page.locator("div[aria-label='Escribe un comentario...']").click();
        page.keyboard().type(comentario);
        page.keyboard().press("Enter");

        page.locator("text=Compartir").click();
        page.waitForTimeout(1000);
        page.locator("text=Compartir ahora (Amigos)").click();

        System.out.println("Reaccionó, comentó y compartió en: " + publicacionUrl);
        page.close();
    }

    private void reaccionarAPublicacion(Page page, String tipoReaccion) {
        Locator reaccionBtn = page.locator("div[aria-label='Me gusta']");
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
            default:
                selectorReaccion = "div[aria-label='Me gusta']";
                break;
        }

        page.locator(selectorReaccion).click();
    }
}
