package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

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

	public List<Grupo> todos() {
		return this.manager.createQuery("from Grupo order by nome", Grupo.class).getResultList();
	}

}