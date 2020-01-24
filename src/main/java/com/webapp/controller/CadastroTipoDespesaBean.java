package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;
import com.webapp.repository.TiposDespesas;
import com.webapp.repository.filter.TipoLancamentoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroTipoDespesaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private TiposDespesas tiposDespesas;

	@Inject
	private TipoLancamento tipoDespesa;

	private List<TipoLancamento> todosTiposDespesas;

	private TipoLancamento tipoDespesaSelecionado;

	private TipoLancamentoFilter filtro = new TipoLancamentoFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		tipoDespesa = new TipoLancamento();
	}

	public void prepararEditarCadastro() {
		tipoDespesa = tipoDespesaSelecionado;
	}

	public void salvar() {

		tiposDespesas.save(tipoDespesa);

		tipoDespesaSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Tipo de lançamento salvo com sucesso!' });");
	}

	public void excluir() {

		tiposDespesas.remove(tipoDespesaSelecionado);

		tipoDespesaSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Tipo de lançamento excluído com sucesso!' });");

	}

	public void pesquisar() {
		todosTiposDespesas = tiposDespesas.filtrados(filtro);
		tipoDespesaSelecionado = null;
	}

	private void listarTodos() {
		todosTiposDespesas = tiposDespesas.todos();
	}

	public List<TipoLancamento> getTodosTiposDespesas() {
		return todosTiposDespesas;
	}

	public TipoLancamentoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(TipoLancamentoFilter filtro) {
		this.filtro = filtro;
	}

	public TipoLancamento getTipoDespesaSelecionado() {
		return tipoDespesaSelecionado;
	}

	public void setTipoDespesaSelecionado(TipoLancamento tipoVendaSelecionado) {
		this.tipoDespesaSelecionado = tipoVendaSelecionado;
	}

	public TipoLancamento getTipoDespesa() {
		return tipoDespesa;
	}

	public void setTipoDespesa(TipoLancamento tipoDespesa) {
		this.tipoDespesa = tipoDespesa;
	}

	public OrigemLancamento[] getOrigensLancamentos() {
		return OrigemLancamento.values();
	}
}