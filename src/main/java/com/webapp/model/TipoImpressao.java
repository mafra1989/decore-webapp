package com.webapp.model;

public enum TipoImpressao {

	IMPRESSORA01("IMPRESSORA 01"), IMPRESSORA02("IMPRESSORA 02"), IMPRESSORA03("IMPRESSORA 03"),;

	private String descricao;

	TipoImpressao(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
