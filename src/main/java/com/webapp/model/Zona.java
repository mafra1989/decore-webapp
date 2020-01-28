package com.webapp.model;

public enum Zona {

	NORTE("Norte"), SUL("Sul"), LESTE("Leste"), OESTE("Oeste"), CENTRO_SUL("Centro-Sul"), CENTRO_OESTE("Centro-Oeste");

	private String descricao;

	Zona(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
