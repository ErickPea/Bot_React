package botinteractions.models;

import botinteractions.services.DatabaseService; // Importar DatabaseService
import java.util.List; // Importar List

public class Cuenta {
    private String email;
    private String contrasena;
    private String id; // Añadir ID para la cuenta

    // Constructor existente
    public Cuenta(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }

    // Nuevo constructor con ID
    public Cuenta(String id, String email, String contrasena) {
        this.id = id;
        this.email = email;
        this.contrasena = contrasena;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getId() {
        return id;
    }

    // Método para obtener todas las cuentas desde la base de datos
    public List<Cuenta> getCuentas() {
        DatabaseService dbService = new DatabaseService();
        return dbService.obtenerTodasLasCuentas();
    }
}
