package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Deposito;
import com.webapp.model.Investidor;
import com.webapp.repository.Depositos;
import com.webapp.repository.Investidores;
import com.webapp.repository.filter.DepositoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class LancamentoDepositoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Depositos depositos;
	
	@Inject
	private Investidores investidores;

	@Inject
	private Deposito deposito;

	private List<Deposito> todosLacamentos;
	
	private List<Investidor> todosInvestidores;

	private Deposito depositoSelecionado;

	private DepositoFilter filtro = new DepositoFilter();
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);
	
	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalDepositos;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			pesquisar();
			todosInvestidores();
		}
	}

	public void prepararNovoLancamento() {
		deposito = new Deposito();
	}

	public void prepararEditarLancamento() {
		deposito = depositoSelecionado;
	}

	public void salvar() {

		depositos.save(deposito);

		depositoSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Depósito registrado com sucesso!' });");
	}

	public void excluir() {

		depositos.remove(depositoSelecionado);

		depositoSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído', text: 'Despesa excluída com sucesso!' });");

	}

	public void pesquisar() {
		todosLacamentos = depositos.filtrados(filtro);
		
		for (Deposito deposito : todosLacamentos) {
			deposito.setValorTemp(nf.format(deposito.getValor().doubleValue()));
		}
		
		depositoSelecionado = null;
		
		totalDepositos();
	}

	/*
	private void listarTodos() {
		todosLacamentos = despesas.filtrados(filtro);
	}
	*/
	
	private void todosInvestidores() {
		todosInvestidores = investidores.todos();
	}
	
	public void totalDepositos() {
		/*
		if(depositos.totalDepositos() != null) {
			totalDepositos = nf.format(depositos.totalDepositos().doubleValue());
			
		} else {
			totalDepositos = "0,00";
		}
		*/
		
		Double totalDepositado = 0.0;
		
		for (Deposito deposito : todosLacamentos) {
			totalDepositado += deposito.getValor().doubleValue();
		}
		
		totalDepositos = nf.format(totalDepositado);
	}

	public List<Deposito> getTodosLancamentos() {
		return todosLacamentos;
	}

	public DepositoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(DepositoFilter filtro) {
		this.filtro = filtro;
	}

	public Deposito getDepositoSelecionado() {
		return depositoSelecionado;
	}

	public void setDepositoSelecionado(Deposito depositoSelecionado) {
		this.depositoSelecionado = depositoSelecionado;
	}

	public Deposito getDeposito() {
		return deposito;
	}

	public void setDeposito(Deposito deposito) {
		this.deposito = deposito;
	}

	public String getTotalDepositos() {
		return totalDepositos;
	}

	public List<Investidor> getTodosInvestidores() {
		return todosInvestidores;
	}



}