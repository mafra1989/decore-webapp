package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Fornecedor;
import com.webapp.repository.Fornecedores;
import com.webapp.repository.filter.FornecedorFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroFornecedorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Fornecedores fornecedores;

	@Inject
	private Fornecedor fornecedor;

	private List<Fornecedor> todosFornecedores;

	private Fornecedor fornecedorSelecionado;

	private FornecedorFilter filtro = new FornecedorFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		fornecedor = new Fornecedor();
	}

	public void prepararEditarCadastro() {
		fornecedor = fornecedorSelecionado;
	}

	public void salvar() {

		fornecedores.save(fornecedor);

		fornecedorSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Fornecedor salvo com sucesso!' });");
	}

	public void excluir() {

		fornecedores.remove(fornecedorSelecionado);

		fornecedorSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Fornecedor excluído com sucesso!' });");

	}

	public void pesquisar() {
		todosFornecedores = fornecedores.filtrados(filtro);
		fornecedorSelecionado = null;
	}

	private void listarTodos() {
		todosFornecedores = fornecedores.todos();
	}

	public List<Fornecedor> getTodosFornecedores() {
		return todosFornecedores;
	}

	public FornecedorFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(FornecedorFilter filtro) {
		this.filtro = filtro;
	}

	public Fornecedor getFornecedorSelecionado() {
		return fornecedorSelecionado;
	}

	public void setFornecedorSelecionado(Fornecedor fornecedorSelecionado) {
		this.fornecedorSelecionado = fornecedorSelecionado;
	}

	public Fornecedor getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(Fornecedor fonecedor) {
		this.fornecedor = fonecedor;
	}

}