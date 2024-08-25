package com.webapp.model;

import java.io.Serializable;

public class ItemEspelhoCompraPagamento implements Serializable {

	private static final long serialVersionUID = 1L;

	private String valorPagar;
	private String vencimento;
	private String status;
	
	public String getValorPagar() {
		return valorPagar;
	}
	public void setValorPagar(String valorPagar) {
		this.valorPagar = valorPagar;
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