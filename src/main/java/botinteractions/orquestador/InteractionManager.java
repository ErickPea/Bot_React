// InteractionManager.java
package botinteractions.orquestador;

import botinteractions.models.Cuenta;
import botinteractions.services.DatabaseService;
import botinteractions.services.FacebookInteractionService;
import botinteractions.models.CookieData; // Importar CookieData
import com.microsoft.playwright.*;
import java.nio.file.Paths; // Para manejar rutas de archivos
import java.io.File; // Para crear directorios

import java.util.List;
import java.util.Arrays; // Importar Arrays
import com.microsoft.playwright.options.LoadState; // Importar LoadState

public class InteractionManager {

    // La URL de Facebook ya no se define aquí.

    public void ejecutarInteracciones() throws Exception {
        Cuenta cuentaManager = new Cuenta("temp", "temp", "temp"); // Instancia temporal para acceder a getCuentas
        List<Cuenta> cuentas = cuentaManager.getCuentas(); // Obtener todas las cuentas
        DatabaseService dbService = new DatabaseService(); // Instancia de DatabaseService

        try (Playwright playwright = Playwright.create()) {
            for (Cuenta cuenta : cuentas) {
                // Define un directorio de datos de usuario único para cada cuenta
                String userDataDirPath = "playwright_profiles/" + cuenta.getId();
                File userDataDir = new File(userDataDirPath);
                if (!userDataDir.exists()) {
                    userDataDir.mkdirs(); // Crea el directorio si no existe
                }

                BrowserContext context = playwright.chromium().launchPersistentContext(
                    Paths.get(userDataDirPath),
                    new BrowserType.LaunchPersistentContextOptions()
                        .setHeadless(false) // Mostrar el navegador
                        .setViewportSize(null) // Sin tamaño de viewport fijo
                        .setArgs(Arrays.asList("--start-maximized"))
                );

                FacebookInteractionService servicioFacebook = new FacebookInteractionService();
                Page page = context.newPage(); // Crear una única página para esta cuenta

                System.out.println("Intentando iniciar sesión para: " + cuenta.getEmail());
                servicioFacebook.iniciarSesion(page, cuenta.getEmail(), cuenta.getContrasena());

                // La lógica de navegación inicial y verificación de sesión se maneja completamente en FacebookInteractionService.
                servicioFacebook.iniciarSesionConCredenciales(page, cuenta.getEmail(), cuenta.getContrasena());

                

                // Aquí puedes llamar a más métodos como aceptarSolicitudes, reaccionarComentarCompartir, etc.
                // Pasando la misma instancia de 'page'
                servicioFacebook.aceptarSolicitudes(page);
                // Asegúrate de reemplazar "URL_DE_PUBLICACION" con una URL de publicación real de Facebook
                servicioFacebook.reaccionarComentarCompartir(page, "https://www.facebook.com/leonel.parrales.35", "jaja", "me gusta");

                page.close(); // Cerrar la página al finalizar las interacciones de la cuenta
                context.close(); // Cerrar el contexto persistente para esta cuenta
            }
        }
    }
}