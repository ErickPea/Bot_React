package botinteractions.models;

public class Cuenta {
    private String email;
    private String contrasena;

    public Cuenta(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }
}

