package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Empresa;
import com.webapp.repository.Empresas;
import com.webapp.repository.filter.EmpresaFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEmpresaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Empresas empresas;

	@Inject
	private Empresa empresa;

	private List<Empresa> todasEmpresas;

	private Empresa empresaSelecionada;

	private EmpresaFilter filtro = new EmpresaFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		empresa = new Empresa();
	}

	public void prepararEditarCadastro() {
		empresa = empresaSelecionada;
	}

	public void salvar() {

		empresas.save(empresa);

		empresaSelecionada = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Empresa salva com sucesso!' });");
	}

	public void excluir() {

		empresas.remove(empresaSelecionada);

		empresaSelecionada = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Empresa excluída com sucesso!' });");

	}

	public void pesquisar() {
		todasEmpresas = empresas.filtrados(filtro);
		empresaSelecionada = null;
	}

	private void listarTodos() {
		todasEmpresas = empresas.todos();
	}

	public List<Empresa> getTodasEmpresas() {
		return todasEmpresas;
	}

	public EmpresaFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(EmpresaFilter filtro) {
		this.filtro = filtro;
	}

	public Empresa getEmpresaSelecionada() {
		return empresaSelecionada;
	}

	public void setEmpresaSelecionada(Empresa empresaSelecionada) {
		this.empresaSelecionada = empresaSelecionada;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}