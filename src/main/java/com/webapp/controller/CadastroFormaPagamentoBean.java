package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;

import com.webapp.model.FormaPagamento;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.filter.FormaPagamentoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroFormaPagamentoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FormasPagamentos formasPagamentos;

	@Inject
	private FormaPagamento formaPagamento;

	private List<FormaPagamento> todasFormasPagamentos;

	private FormaPagamento formaPagamentoSelecionado;

	private FormaPagamentoFilter filtro = new FormaPagamentoFilter();

	@NotNull
	private Long parcelas;

	@NotNull
	private Long dias;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		formaPagamento = new FormaPagamento();
		parcelas = null;
		dias = null;
	}

	public void prepararEditarCadastro() {
		formaPagamento = formaPagamentoSelecionado;
		parcelas = formaPagamento.getParcelas();
		dias = formaPagamento.getDias();
	}

	public void salvar() {
		
		FormaPagamento formaPagamentoTemp = null;

		if (formaPagamento.getId() == null) {

			FormaPagamentoFilter filtroTemp = new FormaPagamentoFilter();
			filtroTemp.setDescricao(formaPagamento.getDescricao());
			formaPagamentoTemp = formasPagamentos.porDescricao(filtroTemp);
			
		} else {
			formaPagamentoTemp = formasPagamentos.porDescricao(formaPagamento);
		}

		if (formaPagamentoTemp == null) {

			formaPagamento.setParcelas(parcelas);
			formaPagamento.setDias(dias);

			formasPagamentos.save(formaPagamento);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Forma de Pagamento salva com sucesso!' });");
		} else {

			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Forma de Pagamento já existente!' });");
		}
		
		
		formaPagamentoSelecionado = null;

		parcelas = null;
		dias = null;

		listarTodos();
	}

	public void excluir() {

		formasPagamentos.remove(formaPagamentoSelecionado);

		formaPagamentoSelecionado = null;

		parcelas = null;
		dias = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Forma de Pagamento excluída com sucesso!' });");

	}

	public void pesquisar() {
		todasFormasPagamentos = formasPagamentos.filtrados(filtro);
		formaPagamentoSelecionado = null;
	}

	public void pagamentoAvista() {

		if (formaPagamento.isAvista()) {
			formaPagamento.setDescricao("À vista");
			formaPagamento.setEntrada(false);
			parcelas = null;
			dias = null;
		} else {
			formaPagamento.setDescricao("");
			formaPagamento.setEntrada(false);
			parcelas = null;
			dias = null;
		}

	}

	public void buildDescription() {

		if (parcelas != null && dias != null) {

			String descricao = "";

			if (formaPagamento.isEntrada()) {
				descricao = "1 entrada + ";
			}

			descricao += dias;
			Long diasTemp = 0L;

			for (int i = 0; i < parcelas; i++) {

				diasTemp += dias;
				if (i > 0) {
					descricao += "/" + diasTemp;
				}
			}

			formaPagamento.setDescricao(descricao);
		}

	}

	private void listarTodos() {
		todasFormasPagamentos = formasPagamentos.todos();
	}

	public List<FormaPagamento> getTodasFormasPagamentos() {
		return todasFormasPagamentos;
	}

	public FormaPagamentoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(FormaPagamentoFilter filtro) {
		this.filtro = filtro;
	}

	public FormaPagamento getFormaPagamentoSelecionado() {
		return formaPagamentoSelecionado;
	}

	public void setFormaPagamentoSelecionado(FormaPagamento formaPagamentoSelecionado) {
		this.formaPagamentoSelecionado = formaPagamentoSelecionado;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public Long getParcelas() {
		return parcelas;
	}

	public void setParcelas(Long parcelas) {
		this.parcelas = parcelas;
	}

	public Long getDias() {
		return dias;
	}

	public void setDias(Long dias) {
		this.dias = dias;
	}

}