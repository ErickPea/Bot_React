package botinteractions.services;

import botinteractions.models.Cuenta;
import botinteractions.models.CookieData;
import botinteractions.persistence.DatabaseConnection;
import com.microsoft.playwright.options.Cookie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date; // Importar Date

public class DatabaseService {

    public Cuenta obtenerCuentaPorId(String idCuenta) {
        String query = "SELECT id, email, contrasena FROM cuentas WHERE id = ?"; // Seleccionar también el ID
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, idCuenta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String id = rs.getString("id"); // Obtener el ID
                String email = rs.getString("email");
                String contrasena = rs.getString("contrasena");
                return new Cuenta(id, email, contrasena); // Pasar el ID al constructor
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // NUEVO MÉTODO PARA OBTENER TODAS LAS CUENTAS
    public List<Cuenta> obtenerTodasLasCuentas() {
        List<Cuenta> cuentas = new ArrayList<>();
        String query = "SELECT id, email, contrasena FROM cuentas"; // Seleccionar también el ID
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("id"); // Obtener el ID
                String email = rs.getString("email");
                String contrasena = rs.getString("contrasena");
                cuentas.add(new Cuenta(id, email, contrasena)); // Pasar el ID al constructor
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cuentas;
    }

    // Método para guardar cookies
    public void guardarCookies(String cuentaId, List<Cookie> cookies) {
        String deleteQuery = "DELETE FROM cookies WHERE cuenta_id = ?";
        String insertQuery = "INSERT INTO cookies (cuenta_id, nombre, valor, dominio, path, expiracion) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // Eliminar cookies existentes para la cuenta
            deleteStmt.setString(1, cuentaId);
            deleteStmt.executeUpdate();

            // Insertar nuevas cookies
            for (Cookie cookie : cookies) {
                insertStmt.setString(1, cuentaId);
                insertStmt.setString(2, cookie.name);
                insertStmt.setString(3, cookie.value);
                insertStmt.setString(4, cookie.domain);
                insertStmt.setString(5, cookie.path);
                // Playwright ya devuelve cookie.expires como un double en segundos Unix
                insertStmt.setDouble(6, cookie.expires); // Corregido: Usar directamente cookie.expires
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
            System.out.println("Cookies guardadas exitosamente para la cuenta: " + cuentaId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener cookies por cuenta
    public List<CookieData> obtenerCookiesPorCuenta(String cuentaId) {
        List<CookieData> cookies = new ArrayList<>();
        String query = "SELECT nombre, valor, dominio, path, expiracion FROM cookies WHERE cuenta_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cuentaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CookieData cookie = new CookieData(
                    rs.getString("nombre"),
                    rs.getString("valor"),
                    rs.getString("dominio"),
                    rs.getString("path"),
                    rs.getDouble("expiracion")
                );
                cookies.add(cookie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cookies;
    }
}
