package com.webapp.model;

import java.io.Serializable;

public class ItemEspelhoVendaPagamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	private String tipo;
	private String valor;
	
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	
}