package com.webapp.model;

public enum OrigemConta {

	DEBITO("à pagar"), CREDITO("à receber");

	private String descricao;

	OrigemConta(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
