package com.webapp.repository.filter;

import java.io.Serializable;

import com.webapp.model.CategoriaProduto;

public class ProdutoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String descricao = "";
	
	private CategoriaProduto categoriaProduto;

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public CategoriaProduto getCategoriaProduto() {
		return categoriaProduto;
	}

	public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
		this.categoriaProduto = categoriaProduto;
	}

}