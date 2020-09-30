package com.webapp.model;

public enum TipoOperacao {

	COMPRA("Compra"), VENDA("Venda"), LANCAMENTO("Lançamento"), DEVOLUCAO("Devolução/Troca");

	private String descricao;

	TipoOperacao(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
