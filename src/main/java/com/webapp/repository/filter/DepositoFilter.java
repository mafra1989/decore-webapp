package com.webapp.repository.filter;

import java.io.Serializable;

public class DepositoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nomeInvestidor;

	public String getNomeInvestidor() {
		return nomeInvestidor;
	}

	public void setNomeInvestidor(String nomeInvestidor) {
		this.nomeInvestidor = nomeInvestidor;
	}

}