package com.webapp.model;

public enum TipoPagamento {

	AVISTA("À vista"), PARCELADO("Parcelado");

	private String descricao;

	TipoPagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
