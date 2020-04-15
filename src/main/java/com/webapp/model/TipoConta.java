package com.webapp.model;

public enum TipoConta {

	APAGAR("à pagar"), ARECEBER("à receber");

	private String descricao;

	TipoConta(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
