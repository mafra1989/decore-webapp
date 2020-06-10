package com.webapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListaProduto implements Serializable {

	private static final long serialVersionUID = 203356217547759664L;

	/* Dados da Lista */
	private String categoria;
	private String valorEmEstoque;
	private String totalDeItens;

	/* Produtos */
	private List<Object> listaDeItens = new ArrayList<Object>();

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getValorEmEstoque() {
		return valorEmEstoque;
	}

	public void setValorEmEstoque(String valorEmEstoque) {
		this.valorEmEstoque = valorEmEstoque;
	}

	public String getTotalDeItens() {
		return totalDeItens;
	}

	public void setTotalDeItens(String totalDeItens) {
		this.totalDeItens = totalDeItens;
	}

	public List<Object> getListaDeItens() {
		return listaDeItens;
	}

	public void setListaDeItens(List<Object> listaDeItens) {
		this.listaDeItens = listaDeItens;
	}

}