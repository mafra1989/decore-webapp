package com.webapp.model;

public enum TipoFiltroVenda {

	LANCAMENTO("Lançadas"), PAGAMENTO("Pagas");

	private String descricao;

	TipoFiltroVenda(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
