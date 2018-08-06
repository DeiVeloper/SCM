package mx.com.desoft.hidrogas.scm.dto;

import java.io.Serializable;

public class Data implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2958726625432040690L;
	private String numeroCelular;
	private String mensaje;
	private String nombreArchivo;

	public Data(String numeroCelular, String mensaje, String nombreArchivo) {
		this.setNumeroCelular(numeroCelular);
		this.setMensaje(mensaje);
		this.setNombreArchivo(nombreArchivo);
	}
   	   
	public String getNumeroCelular() {
		return numeroCelular;
	}

	public void setNumeroCelular(String numeroCelular) {
		this.numeroCelular = numeroCelular;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}
	
	@Override
	public String toString() {
		return "Data [numeroCelular=" + getNumeroCelular() + ", mensaje=" + getMensaje()+ "]";
	}
}
