package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.CategoriaProduto;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.filter.CategoriaProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroCategoriaProdutoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CategoriasProdutos categoriasProdutos;

	@Inject
	private CategoriaProduto categoriaProduto;

	private List<CategoriaProduto> todasCategoriasProduto;

	private CategoriaProduto categoriaProdutoSelecionado;

	private CategoriaProdutoFilter filtro = new CategoriaProdutoFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodasCategoriasProdutos();
		}
	}

	public void prepararNovoCadastro() {
		categoriaProduto = new CategoriaProduto();
	}

	public void prepararEditarCadastro() {
		categoriaProduto = categoriaProdutoSelecionado;
	}

	public void salvar() {

		categoriasProdutos.save(categoriaProduto);

		categoriaProdutoSelecionado = null;

		listarTodasCategoriasProdutos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Categoria de produto salvo com sucesso!' });");
	}

	public void excluir() {

		categoriasProdutos.remove(categoriaProdutoSelecionado);

		categoriaProdutoSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Categoria de produto excluído com sucesso!' });");

	}

	public void pesquisar() {
		todasCategoriasProduto = categoriasProdutos.filtrados(filtro);
		categoriaProdutoSelecionado = null;
	}

	private void listarTodasCategoriasProdutos() {
		todasCategoriasProduto = categoriasProdutos.todos();
	}

	public List<CategoriaProduto> getTodasCategoriasProdutos() {
		return todasCategoriasProduto;
	}

	public CategoriaProdutoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(CategoriaProdutoFilter filtro) {
		this.filtro = filtro;
	}

	public CategoriaProduto getCategoriaProdutoSelecionado() {
		return categoriaProdutoSelecionado;
	}

	public void setCategoriaProdutoSelecionado(CategoriaProduto categoriaProdutoSelecionado) {
		this.categoriaProdutoSelecionado = categoriaProdutoSelecionado;
	}

	public CategoriaProduto getCategoriaProduto() {
		return categoriaProduto;
	}

	public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
		this.categoriaProduto = categoriaProduto;
	}

}