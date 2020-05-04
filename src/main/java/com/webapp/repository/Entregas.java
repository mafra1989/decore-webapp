package com.webapp.repository;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Entrega;
import com.webapp.model.Venda;
import com.webapp.util.jpa.Transacional;

public class Entregas implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Entrega porId(Long id) {
		return this.manager.find(Entrega.class, id);
	}
	
	@Transacional
	public Entrega save(Entrega entrega) {
		return this.manager.merge(entrega);
	}

	@Transacional
	public void remove(Entrega entrega) {
		Entrega entregaTemp = new Entrega();
		entregaTemp = this.manager.merge(entrega);

		this.manager.remove(entregaTemp);
	}
	
	public Entrega porVenda(Venda venda) {
		
		try {
			return this.manager.createQuery("from Entrega e where e.venda.id = :id", Entrega.class)
					.setParameter("id", venda.getId()).getSingleResult();
		} catch (NoResultException e) {

		}

		return new Entrega();
	}
	
}