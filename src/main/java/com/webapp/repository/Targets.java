package com.webapp.repository;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Target;
import com.webapp.util.jpa.Transacional;

public class Targets implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Target porId(Long id) {
		return this.manager.find(Target.class, id);
	}
	
	@Transacional
	public Target save(Target target) {
		return this.manager.merge(target);
	}

	@Transacional
	public void remove(Target target) {
		Target targetTemp = new Target();
		targetTemp = this.manager.merge(target);

		this.manager.remove(targetTemp);
	}
	
	public Target porPeriodoTipo(String periodo, String tipo, String empresa) {
		try {
			return this.manager
					.createQuery("from Target e where e.empresa = :empresa AND e.periodo = :periodo and e.tipo = :tipo", Target.class)
					.setParameter("empresa", empresa).setParameter("periodo", periodo).setParameter("tipo", tipo).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
}