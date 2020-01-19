package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.CategoriaLancamento;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.filter.CategoriaLancamentoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroCategoriaDespesaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CategoriasLancamentos categoriasDespesas;

	@Inject
	private CategoriaLancamento categoriaDespesa;

	private List<CategoriaLancamento> todasCategoriasDespesas;

	private CategoriaLancamento categoriaDespesaSelecionada;

	private CategoriaLancamentoFilter filtro = new CategoriaLancamentoFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		categoriaDespesa = new CategoriaLancamento();
	}

	public void prepararEditarCadastro() {
		categoriaDespesa = categoriaDespesaSelecionada;
	}

	public void salvar() {

		categoriasDespesas.save(categoriaDespesa);

		categoriaDespesaSelecionada = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Categoria de despesa salva com sucesso!' });");
	}

	public void excluir() {

		categoriasDespesas.remove(categoriaDespesaSelecionada);

		categoriaDespesaSelecionada = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: Categoria de despesa excluída com sucesso!' });");

	}

	public void pesquisar() {
		todasCategoriasDespesas = categoriasDespesas.filtrados(filtro);
		categoriaDespesaSelecionada = null;
	}

	private void listarTodos() {
		todasCategoriasDespesas = categoriasDespesas.todos();
	}

	public List<CategoriaLancamento> getTodasCategoriasDespesas() {
		return todasCategoriasDespesas;
	}

	public CategoriaLancamentoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(CategoriaLancamentoFilter filtro) {
		this.filtro = filtro;
	}

	public CategoriaLancamento getCategoriaDespesaSelecionada() {
		return categoriaDespesaSelecionada;
	}

	public void setCategoriaDespesaSelecionada(CategoriaLancamento categoriaDespesaSelecionada) {
		this.categoriaDespesaSelecionada = categoriaDespesaSelecionada;
	}

	public CategoriaLancamento getCategoriaDespesa() {
		return categoriaDespesa;
	}

	public void setCategoriaDespesa(CategoriaLancamento categoriaDespesa) {
		this.categoriaDespesa = categoriaDespesa;
	}

}