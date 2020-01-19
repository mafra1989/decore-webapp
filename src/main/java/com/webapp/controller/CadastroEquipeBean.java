package com.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.webapp.model.Grupo;
import com.webapp.model.Usuario;
import com.webapp.repository.Grupos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.UsuarioFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Grupos grupos;
	
	@Inject
	private Grupo grupo;

	private List<Grupo> todosGrupos;

	private Usuario membroSelecionado;

	private UsuarioFilter filtro = new UsuarioFilter();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		usuario = new Usuario();
		grupo = new Grupo();
	}

	public void prepararEditarCadastro() {
		usuario = membroSelecionado;
		usuario.setSenha("");
		Usuario usuarioTemp = usuarios.porId(usuario.getId());
		grupo = usuarioTemp.getGrupos().get(0);
	}

	public void salvar() {

		try {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String hashedPassword = passwordEncoder.encode(usuario.getSenha());

			usuario.setSenha(hashedPassword);

			List<Grupo> listaDeGrupos = new ArrayList<Grupo>();
			listaDeGrupos.add(grupo);
			usuario.setGrupos(listaDeGrupos);

			usuarios.save(usuario);

			membroSelecionado = null;

			listarTodos();

			PrimeFaces.current().executeScript(
					"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe salvo com sucesso!' });");


		} catch (Exception e) {
			FacesUtil.addErrorMessage("Erro ao cadastrar membro da equipe!");
		}

	}

	public void excluir() {

		usuarios.remove(membroSelecionado);

		membroSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Conclu�do!', text: 'Membro de equipe exclu�do com sucesso!' });");

	}

	public void pesquisar() {
		todosUsuarios = usuarios.filtrados(filtro);
		membroSelecionado = null;
	}

	private void listarTodos() {
		todosUsuarios = usuarios.todos();
		todosGrupos = grupos.todos();
	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public List<Grupo> getTodosGrupos() {
		return todosGrupos;
	}

	public UsuarioFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(UsuarioFilter filtro) {
		this.filtro = filtro;
	}

	public Usuario getMembroSelecionado() {
		return membroSelecionado;
	}

	public void setMembroSelecionado(Usuario membroSelecionado) {
		this.membroSelecionado = membroSelecionado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Grupo getGrupo() {
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}
	
	public String getGrupo(Usuario usuario) {
		usuario = usuarios.porLogin(usuario.getLogin());

		if (usuario.getGrupos().size() != 0) {
			return usuario.getGrupos().get(0).getNome();
		}

		return "";
	}

}