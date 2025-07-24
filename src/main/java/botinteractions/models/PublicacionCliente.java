package botinteractions.models;

public class PublicacionCliente {
	 private String enlace;
	    private String textoComentario;
	    private String tipoReaccion;

	    public PublicacionCliente(String enlace, String textoComentario, String tipoReaccion) {
	        this.enlace = enlace;
	        this.textoComentario = textoComentario;
	        this.tipoReaccion = tipoReaccion;
	    }

	    public String getEnlace() {
	        return enlace;
	    }

	    public String getTextoComentario() {
	        return textoComentario;
	    }

	    public String getTipoReaccion() {
	        return tipoReaccion;
	    }
}
