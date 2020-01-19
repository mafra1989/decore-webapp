package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.webapp.model.Parcela;
import com.webapp.util.jpa.Transacional;

public class Parcelas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Parcela porId(Long id) {
		return this.manager.find(Parcela.class, id);
	}

	@Transacional
	public Parcela save(Parcela parcela) {
		return this.manager.merge(parcela);
	}

	@Transacional
	public void remove(Parcela emprestimo) {
		Parcela emprestimoTemp = new Parcela();
		emprestimoTemp = this.manager.merge(emprestimo);

		this.manager.remove(emprestimoTemp);
	}
	
	public List<Parcela> todas() {
		return this.manager.createQuery("from Parcela order by id", Parcela.class).getResultList();
	}

	public List<Parcela> todasParcelas(Long id) {

		TypedQuery<Parcela> typedQuery;

		typedQuery = manager
				.createQuery("select p from Parcela p join fetch p.emprestimo e where e.id = :id order by p.id",
						Parcela.class)
				.setParameter("id", id);

		return typedQuery.getResultList();

	}

}