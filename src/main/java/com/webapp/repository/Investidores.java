package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webapp.model.Investidor;
import com.webapp.repository.filter.InvestidorFilter;
import com.webapp.util.jpa.Transacional;

public class Investidores implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Investidor porId(Long id) {
		return this.manager.find(Investidor.class, id);
	}

	@Transacional
	public Investidor save(Investidor investidor) {
		return this.manager.merge(investidor);
	}

	@Transacional
	public void remove(Investidor investidor) {
		Investidor investidorTemp = new Investidor();
		investidorTemp = this.manager.merge(investidor);

		this.manager.remove(investidorTemp);
	}

	public List<Investidor> todos() {
		return this.manager.createQuery("from Investidor order by nome", Investidor.class).getResultList();
	}

	public List<Investidor> filtrados(InvestidorFilter filter) {
		return this.manager.createQuery("from Investidor i where i.nome like :nome order by nome", Investidor.class)
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}
	
	public Number totalInvestidores() {		
		String jpql = "SELECT count(i) FROM Investidor i";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		return count;
	}
}