package com.webapp.model;

public enum StatusEmprestimo {

	ABERTO("ABERTO"), FECHADO("FECHADO"), VENCIDO("VENCIDO");

	private String descricao;

	StatusEmprestimo(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
