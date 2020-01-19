package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Produto;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Produtos;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class EstoqueBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<CategoriaProduto> todasCategoriasProdutos;
	
	@Inject
	private CategoriasProdutos categoriasProdutos;
	
	private List<Produto> produtosFiltrados;
	
	@Inject
	private Produtos produtos;
	
	@Inject
	private Produto produtoSelecionado;
	
	private ProdutoFilter filter = new ProdutoFilter();
	
	private String estoqueTotal = "0";

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todasCategoriasProdutos();
		}
	}
	
	public void pesquisar() {
		produtosFiltrados = produtos.filtrados(filter);
		System.out.println(produtosFiltrados.size());
		
		long value = 0;
		for (Produto produto : produtosFiltrados) {
			value += produto.getQuantidadeAtual();
		}
		
		estoqueTotal = String.valueOf(value);
	}
	
	private void todasCategoriasProdutos() {
		todasCategoriasProdutos = categoriasProdutos.todos();
	}

	public ProdutoFilter getFilter() {
		return filter;
	}

	public void setFilter(ProdutoFilter filter) {
		this.filter = filter;
	}

	public List<Produto> getProdutosFiltrados() {
		return produtosFiltrados;
	}

	public List<CategoriaProduto> getTodasCategoriasProdutos() {
		return todasCategoriasProdutos;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
	}

	public String getEstoqueTotal() {
		return estoqueTotal;
	}

}
