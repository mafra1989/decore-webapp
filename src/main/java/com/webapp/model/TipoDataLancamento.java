package com.webapp.model;

public enum TipoDataLancamento {

	LANCAMENTO("Lançamento"), PAGAMENTO("Pagamento");

	private String descricao;

	TipoDataLancamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
