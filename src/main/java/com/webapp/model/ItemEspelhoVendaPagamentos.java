package com.webapp.model;

import java.io.Serializable;

public class ItemEspelhoVendaPagamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	private String formaPagamento;
	private String valor;
	
	public String getFormaPagamento() {
		return formaPagamento;
	}
	public void setFormaPagamento(String formaPagamento) {
		this.formaPagamento = formaPagamento;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	
}