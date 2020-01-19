package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.Deposito;
import com.webapp.model.Investidor;
import com.webapp.repository.filter.DepositoFilter;
import com.webapp.util.jpa.Transacional;

public class Depositos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Deposito porId(Long id) {
		return this.manager.find(Deposito.class, id);
	}

	@Transacional
	public Deposito save(Deposito deposito) {
		return this.manager.merge(deposito);
	}

	@Transacional
	public void remove(Deposito deposito) {
		Deposito depositoTemp = new Deposito();
		depositoTemp = this.manager.merge(deposito);

		this.manager.remove(depositoTemp);
	}

	public List<Deposito> todos() {
		return this.manager.createQuery("from Deposito order by id", Deposito.class).getResultList();
	}

	public List<Deposito> filtrados(DepositoFilter filter) {

		TypedQuery<Deposito> typedQuery;

		if (StringUtils.isNotBlank(filter.getNomeInvestidor())) {
			typedQuery = manager.createQuery(
					"from Deposito d join fetch d.investidor i where i.nome like :nome order by d.data desc",
					Deposito.class).setParameter("nome", "%" + filter.getNomeInvestidor() + "%");

		} else {
			typedQuery = manager.createQuery("select d from Deposito d order by d.data desc", Deposito.class);
		}

		return typedQuery.getResultList();
	}

	public Number porInvestidor(Investidor investidor) {
		String jpql = "SELECT sum(d.valor) FROM Deposito d join d.investidor i WHERE i.id = :id";
		Query q = manager.createQuery(jpql).setParameter("id", investidor.getId());
		Number count = (Number) q.getSingleResult();

		return count;
	}

	public Number totalDepositos() {		
		String jpql = "SELECT sum(d.valor) FROM Deposito d";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		return count;
	}
}