package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Usuario;
import com.webapp.repository.filter.UsuarioFilter;
import com.webapp.util.jpa.Transacional;

public class Usuarios implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Usuario porId(Long id) {
		return this.manager.find(Usuario.class, id);
	}
	
	@Transacional
	public Usuario save(Usuario usuario) {
		return this.manager.merge(usuario);
	}

	@Transacional
	public void remove(Usuario usuario) {
		Usuario usuarioTemp = new Usuario();
		usuarioTemp = this.manager.merge(usuario);

		this.manager.remove(usuarioTemp);
	}
	
	public List<Usuario> todos() {
		return this.manager.createQuery("from Usuario order by nome", Usuario.class).getResultList();
	}

	public List<Usuario> filtrados(UsuarioFilter filter) {
		return this.manager.createQuery("from Usuario i where i.nome like :nome order by nome", Usuario.class)
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}

	public Usuario porLogin(String login) {
		Usuario usuario = null;
		
		System.out.println("buscando usuário . . .");
		try {
			usuario = this.manager.createQuery("from Usuario where lower(login) = :login", Usuario.class)
				.setParameter("login", login.toLowerCase()).getSingleResult();
		} catch (NoResultException e) {
			System.out.println("nenhum usuário encontrado com o login informado");
		}
		
		return usuario;
	}
	
}