package botinteractions.models;

import java.time.LocalDateTime;

public class RegistroAccion {
    private String tipoAccion;
    private String detalle;
    private LocalDateTime fecha;

    public RegistroAccion(String tipoAccion, String detalle) {
        this.tipoAccion = tipoAccion;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public String getDetalle() {
        return detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
