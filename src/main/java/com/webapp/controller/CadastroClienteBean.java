package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Bairro;
import com.webapp.model.Cliente;
import com.webapp.model.Usuario;
import com.webapp.repository.Bairros;
import com.webapp.repository.Clientes;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.ClienteFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroClienteBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Clientes clientes;

	@Inject
	private Cliente cliente;

	private List<Cliente> todosClientes;

	private Cliente clienteSelecionado;

	private ClienteFilter filtro = new ClienteFilter();
	
	@Inject
	private Bairros bairros;
	
	private List<Bairro> todosBairros;
	
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
		cliente = new Cliente();
	}

	public void prepararEditarCadastro() {
		cliente = clienteSelecionado;
	}

	public void salvar() {

		cliente.setEmpresa(usuario.getEmpresa());
		clientes.save(cliente);

		clienteSelecionado = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Cliente salvo com sucesso!' });");
	}

	public void excluir() {

		clientes.remove(clienteSelecionado);

		clienteSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Cliente excluído com sucesso!' });");

	}

	public void pesquisar() {
		todosClientes = clientes.filtrados(filtro, usuario.getEmpresa());
		clienteSelecionado = null;
	}

	private void listarTodos() {
		todosClientes = clientes.todos(usuario.getEmpresa());
		todosBairros = bairros.todos(usuario.getEmpresa());
	}

	public List<Cliente> getTodosClientes() {
		return todosClientes;
	}

	public ClienteFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(ClienteFilter filtro) {
		this.filtro = filtro;
	}

	public Cliente getClienteSelecionado() {
		return clienteSelecionado;
	}

	public void setClienteSelecionado(Cliente clienteSelecionado) {
		this.clienteSelecionado = clienteSelecionado;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<Bairro> getTodosBairros() {
		return todosBairros;
	}

}