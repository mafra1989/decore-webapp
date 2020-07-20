package com.webapp.repository.filter;

import java.io.Serializable;

import com.webapp.model.CategoriaProduto;

public class ProdutoFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String codigo = "";

	private String descricao = "";
	
	private CategoriaProduto categoriaProduto;
	
	private String empresa = "";

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

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

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

}