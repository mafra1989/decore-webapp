package com.webapp.model;

public enum TipoAtividade {

	ESTOQUE("Estoque"), COMPRA("Compra"), VENDA("Venda"), LANCAMENTO("Lançamento"), PAGAMENTO("Pagamento"), RECEBIMENTO("Recebimento");

	private String descricao;

	TipoAtividade(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
