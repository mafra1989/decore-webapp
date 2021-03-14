package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Devolucao;
import com.webapp.model.Empresa;
import com.webapp.util.jpa.Transacional;

public class Devolucoes implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Devolucao porId(Long id) {
		return this.manager.find(Devolucao.class, id);
	}

	@Transacional
	public Devolucao save(Devolucao devolucao) {
		return this.manager.merge(devolucao);
	}

	@Transacional
	public void remove(Devolucao devolucao) {
		Devolucao devolucaoTemp = new Devolucao();
		devolucaoTemp = this.manager.merge(devolucao);

		this.manager.remove(devolucaoTemp);
	}

	public List<Devolucao> todas(Empresa empresa) {
		return this.manager.createQuery("from Devolucao where empresa.id = :empresa order by id", Devolucao.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}
}