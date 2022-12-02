package com.webapp.model;

import java.io.Serializable;

public class ItemEspelhoVendaPagamento implements Serializable {

	private static final long serialVersionUID = 1L;

	private String valorPagar;
	private String vencimento;
	
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
	
}