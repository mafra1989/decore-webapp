package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.Empresa;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.Empresas;
import com.webapp.repository.TiposDespesas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.CategoriaLancamentoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroCategoriaLancamentoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CategoriasLancamentos categoriasDespesas;
	
	@Inject
	private TiposDespesas tiposLancamentos;
	
	@Inject
	private Empresas empresas;

	private List<TipoLancamento> todosTiposLancamentos;
	
	@Inject
	private DestinosLancamentos destinosLancamentos;

	private List<DestinoLancamento> todosDestinosLancamentos;
	
	private List<Empresa> todasEmpresas;

	@Inject
	private CategoriaLancamento categoriaDespesa;

	private List<CategoriaLancamento> todasCategoriasDespesas;

	private CategoriaLancamento categoriaDespesaSelecionada;

	private CategoriaLancamentoFilter filtro = new CategoriaLancamentoFilter();
	
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
		categoriaDespesa = new CategoriaLancamento();
	}

	public void prepararEditarCadastro() {
		categoriaDespesa = categoriaDespesaSelecionada;
	}

	public void salvar() {

		categoriaDespesa.setEmpresa(usuario.getEmpresa());
		categoriaDespesa.setDestinoLancamento(null);
		categoriasDespesas.save(categoriaDespesa);

		categoriaDespesaSelecionada = null;

		//listarTodos();
		pesquisar();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Categoria de lançamento salva com sucesso!' });");
	}

	public void excluir() {

		categoriasDespesas.remove(categoriaDespesaSelecionada);

		categoriaDespesaSelecionada = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: Categoria de lançamento excluída com sucesso!' });");

	}

	public void pesquisar() {
		filtro.setEmpresa(usuario.getEmpresa());
		todasCategoriasDespesas = categoriasDespesas.filtrados(filtro);
		categoriaDespesaSelecionada = null;
	}

	private void listarTodos() {
		todasCategoriasDespesas = categoriasDespesas.todos(usuario.getEmpresa());
		todosDestinosLancamentos = destinosLancamentos.todos(usuario.getEmpresa());
		todosTiposLancamentos = tiposLancamentos.todos(usuario.getEmpresa());
		todasEmpresas = empresas.todos(usuario.getEmpresa());
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
	
	public OrigemLancamento[] getOrigensLancamentos() {
		return OrigemLancamento.values();
	}

	public List<DestinoLancamento> getTodosDestinosLancamentos() {
		return todosDestinosLancamentos;
	}

	public List<TipoLancamento> getTodosTiposLancamentos() {
		return todosTiposLancamentos;
	}

	public List<Empresa> getTodasEmpresas() {
		return todasEmpresas;
	}

}