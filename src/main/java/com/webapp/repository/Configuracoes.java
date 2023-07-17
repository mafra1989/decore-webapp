package com.webapp.repository;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Configuracao;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Configuracoes implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Configuracao porId(Long id) {
		return this.manager.find(Configuracao.class, id);
	}
	
	@Transacional
	public Configuracao save(Configuracao configuracao) {
		return this.manager.merge(configuracao);
	}
	
	@Transacional
	public void remove(Configuracao configuracao) {
		Configuracao configuracaoTemp = new Configuracao();
		configuracaoTemp = this.manager.merge(configuracao);

		this.manager.remove(configuracaoTemp);
	}
	
	public Configuracao porUsuario(Usuario usuario) {
		Configuracao configuracao = null;
		
		try {
			configuracao = this.manager.createQuery("from Configuracao c where c.usuario.id = :id", Configuracao.class)
					.setParameter("id", usuario.getId())
					.getSingleResult();
			
		} catch (NoResultException e) {
			System.out.println("nenhuma configuracao encontrada para o usuario");
		}
		
		return configuracao;
	}
	
}