package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Empresa;
import com.webapp.model.Grupo;
import com.webapp.util.jpa.Transacional;

public class Grupos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Grupo porId(Long id) {
		return this.manager.find(Grupo.class, id);
	}

	@Transacional
	public Grupo save(Grupo usuario) {
		return this.manager.merge(usuario);
	}

	public List<Grupo> todos(Empresa empresa) {
		return this.manager.createQuery("from Grupo g where g.empresa.id = :id order by g.nome", Grupo.class)
				.setParameter("id", empresa.getId()).getResultList();
	}
	
	@Transacional
	public void saveUsuarioGrupo(String insert) {
		//EntityTransaction et = manager.getTransaction();
		//et.begin();
		manager.createNativeQuery(insert).executeUpdate();
		//et.commit();
	}

}