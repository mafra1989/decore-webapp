package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.FormaPagamento;
import com.webapp.model.Usuario;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.Usuarios;
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

	private FormaPagamento formaPagamentoSelecionada;

	private FormaPagamentoFilter filtro = new FormaPagamentoFilter();
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();			
			usuario = usuarios.porLogin(user.getUsername());
			
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		formaPagamento = new FormaPagamento();
	}

	public void prepararEditarCadastro() {
		formaPagamento = formaPagamentoSelecionada;
	}

	public void salvar() {

		if(formaPagamento.getAcrescimo().doubleValue() >= 0) {
			
			formaPagamento.setEmpresa(usuario.getEmpresa());
			formasPagamentos.save(formaPagamento);

			formaPagamentoSelecionada = null;

			listarTodos();

			PrimeFaces.current().executeScript(
					"PF('formapagamento-dialog').hide();PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Forma de Pagamento salva com sucesso!' });");
		} else {
			
			PrimeFaces.current().executeScript(
					"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Ops!', text: 'Taxa de Pagamento não pode ser menor que zero!' });");
		}
	}

	public void excluir() {

		formasPagamentos.remove(formaPagamentoSelecionada);

		formaPagamentoSelecionada = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Forma de Pagamento excluída com sucesso!' });");

	}

	public void pesquisar() {
		todasFormasPagamentos = formasPagamentos.filtrados(filtro, usuario.getEmpresa());
		formaPagamentoSelecionada = null;
	}

	private void listarTodos() {
		todasFormasPagamentos = formasPagamentos.todos(usuario.getEmpresa());
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

	public FormaPagamento getFormaPagamentoSelecionada() {
		return formaPagamentoSelecionada;
	}

	public void setFormaPagamentoSelecionada(FormaPagamento formaPagamentoSelecionada) {
		this.formaPagamentoSelecionada = formaPagamentoSelecionada;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

}