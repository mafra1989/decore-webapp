package com.webapp.repository.filter;

import java.io.Serializable;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Empresa;

public class ProdutoFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String codigo = "";

	private String descricao = "";
	
	private String tamanho = "";
	
	private String unidade = "";
	
	private CategoriaProduto categoriaProduto;
	
	private Empresa empresa;

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

	public String getTamanho() {
		return tamanho;
	}

	public void setTamanho(String tamanho) {
		this.tamanho = tamanho;
	}

	public String getUnidade() {
		return unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public CategoriaProduto getCategoriaProduto() {
		return categoriaProduto;
	}

	public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
		this.categoriaProduto = categoriaProduto;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}