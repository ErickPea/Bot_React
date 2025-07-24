package botinteractions.services;

import botinteractions.models.Cuenta;
import botinteractions.models.CookieData;
import botinteractions.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    public Cuenta obtenerCuentaPorId(String idCuenta) {
        String query = "SELECT email, contrasena FROM cuentas WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, idCuenta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                String contrasena = rs.getString("contrasena");
                return new Cuenta(email, contrasena);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // NUEVO MÉTODO PARA OBTENER COOKIES
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
