package com.webapp.model;

import java.io.Serializable;

public class ItemEspelhoVendaParcelamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	private String parcela;
	private String valor;
	private String vencimento;
	private String status;
	
	public String getParcela() {
		return parcela;
	}
	public void setParcela(String parcela) {
		this.parcela = parcela;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String getVencimento() {
		return vencimento;
	}
	public void setVencimento(String vencimento) {
		this.vencimento = vencimento;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}