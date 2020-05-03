package com.webapp.model;

public enum StatusPedido {

	PENDENTE("Pendente"), ENTREGUE("Entregue"), CANCELADA("Cancelada");

	private String descricao;

	StatusPedido(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
