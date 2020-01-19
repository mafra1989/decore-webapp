package com.webapp.model;

public enum OrigemLancamento {

	CREDITO("Crédito"), DEBITO("Débito");

	private String descricao;

	OrigemLancamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
