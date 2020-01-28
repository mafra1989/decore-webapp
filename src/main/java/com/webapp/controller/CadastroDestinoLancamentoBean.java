package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.DestinoLancamento;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.filter.DestinoLancamentoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroDestinoLancamentoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private DestinosLancamentos destinosLancamentos;

	@Inject
	private DestinoLancamento destinoLancamento;

	private List<DestinoLancamento> todosDestinosLancamentos;

	private DestinoLancamento destinoLancamentoSelecionado;

	private DestinoLancamentoFilter filtro = new DestinoLancamentoFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		destinoLancamento = new DestinoLancamento();
	}

	public void prepararEditarCadastro() {
		destinoLancamento = destinoLancamentoSelecionado;
	}

	public void salvar() {

		destinosLancamentos.save(destinoLancamento);

		destinoLancamentoSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Destino de lançamento salvo com sucesso!' });");
	}

	public void excluir() {

		destinosLancamentos.remove(destinoLancamentoSelecionado);

		destinoLancamentoSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Conclu�do!', text: 'Destino de lançamento excluído com sucesso!' });");

	}

	public void pesquisar() {
		todosDestinosLancamentos = destinosLancamentos.filtrados(filtro);
		destinoLancamentoSelecionado = null;
	}

	private void listarTodos() {
		todosDestinosLancamentos = destinosLancamentos.todos();
	}

	public List<DestinoLancamento> getTodosDestinosLancamentos() {
		return todosDestinosLancamentos;
	}

	public DestinoLancamentoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(DestinoLancamentoFilter filtro) {
		this.filtro = filtro;
	}

	public DestinoLancamento getDestinoLancamentoSelecionado() {
		return destinoLancamentoSelecionado;
	}

	public void setDestinoLancamentoSelecionado(DestinoLancamento destinoLancamentoSelecionado) {
		this.destinoLancamentoSelecionado = destinoLancamentoSelecionado;
	}

	public DestinoLancamento getDestinoLancamento() {
		return destinoLancamento;
	}

	public void setDestinoLancamento(DestinoLancamento DestinoLancamento) {
		this.destinoLancamento = DestinoLancamento;
	}

}