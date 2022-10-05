package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.TipoVendaFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroTipoVendaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private TiposVendas tiposVendas;

	@Inject
	private TipoVenda tipoVenda;

	private List<TipoVenda> todosTiposVendas;

	private TipoVenda tipoVendaSelecionado;

	private TipoVendaFilter filtro = new TipoVendaFilter();
	
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
		tipoVenda = new TipoVenda();
	}

	public void prepararEditarCadastro() {
		tipoVenda = tipoVendaSelecionado;
	}

	public void salvar() {

		tipoVenda.setEmpresa(usuario.getEmpresa());
		tiposVendas.save(tipoVenda);

		tipoVendaSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Tipo de venda salvo com sucesso!' });");
	}

	public void excluir() {

		tiposVendas.remove(tipoVendaSelecionado);

		tipoVendaSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Tipo de venda excluído com sucesso!' });");

	}

	public void pesquisar() {
		todosTiposVendas = tiposVendas.filtrados(filtro, usuario.getEmpresa());
		tipoVendaSelecionado = null;
	}

	private void listarTodos() {
		todosTiposVendas = tiposVendas.todos(usuario.getEmpresa());
	}

	public List<TipoVenda> getTodosTiposVendas() {
		return todosTiposVendas;
	}

	public TipoVendaFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(TipoVendaFilter filtro) {
		this.filtro = filtro;
	}

	public TipoVenda getTipoVendaSelecionado() {
		return tipoVendaSelecionado;
	}

	public void setTipoVendaSelecionado(TipoVenda tipoVendaSelecionado) {
		this.tipoVendaSelecionado = tipoVendaSelecionado;
	}

	public TipoVenda getTipoVenda() {
		return tipoVenda;
	}

	public void setTipoVenda(TipoVenda tipoVenda) {
		this.tipoVenda = tipoVenda;
	}

}