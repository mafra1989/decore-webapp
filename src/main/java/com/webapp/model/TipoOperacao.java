package com.webapp.model;

public enum TipoOperacao {

	COMPRA("Compra"), LANCAMENTO("Lançamento");

	private String descricao;

	TipoOperacao(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
