package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Investidor;
import com.webapp.repository.Investidores;
import com.webapp.repository.filter.InvestidorFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroInvestidorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Investidores investidores;

	@Inject
	private Investidor investidor;

	private List<Investidor> todosInvestidores;

	private Investidor investidorSelecionado;

	private InvestidorFilter filtro = new InvestidorFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		investidor = new Investidor();
	}

	public void prepararEditarCadastro() {
		investidor = investidorSelecionado;
	}

	public void salvar() {

		investidores.save(investidor);

		investidorSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Investidor salvo com sucesso!' });");
	}

	public void excluir() {

		investidores.remove(investidorSelecionado);

		investidorSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Investidor excluído com sucesso!' });");

	}

	public void pesquisar() {
		todosInvestidores = investidores.filtrados(filtro);
		investidorSelecionado = null;
	}

	private void listarTodos() {
		todosInvestidores = investidores.todos();
	}

	public List<Investidor> getTodosInvestidores() {
		return todosInvestidores;
	}

	public InvestidorFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(InvestidorFilter filtro) {
		this.filtro = filtro;
	}

	public Investidor getInvestidorSelecionado() {
		return investidorSelecionado;
	}

	public void setInvestidorSelecionado(Investidor investidorSelecionado) {
		this.investidorSelecionado = investidorSelecionado;
	}

	public Investidor getInvestidor() {
		return investidor;
	}

	public void setInvestidor(Investidor investidor) {
		this.investidor = investidor;
	}

}