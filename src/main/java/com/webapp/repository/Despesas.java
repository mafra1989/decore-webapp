package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Despesa;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;
import com.webapp.util.jpa.Transacional;

public class Despesas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Despesa porId(Long id) {
		return this.manager.find(Despesa.class, id);
	}

	@Transacional
	public Despesa save(Despesa despesa) {
		return this.manager.merge(despesa);
	}

	@Transacional
	public void remove(Despesa despesa) {
		Despesa despesaTemp = new Despesa();
		despesaTemp = this.manager.merge(despesa);

		this.manager.remove(despesaTemp);
	}
	
	
	public Number totalDebitos() {		
		String jpql = "SELECT sum(i.valor) FROM Despesa i WHERE i.origemLancamento = :origemLancamento";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO);
		Number count = (Number) q.getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}
	
	public Number totalCreditos() {		
		String jpql = "SELECT sum(i.valor) FROM Despesa i WHERE i.origemLancamento = :origemLancamento";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO);
		Number count = (Number) q.getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorCategoria() {		
		
		String jpql = "SELECT i.categoriaLancamento, sum(i.valor) FROM Despesa i WHERE i.origemLancamento = :origem GROUP BY i.categoriaLancamento order by sum(i.valor) desc";
		Query q = manager.createQuery(jpql).setParameter("origem", OrigemLancamento.DEBITO).setMaxResults(5);
		List<Object[]> result = q.getResultList();
		for (Object[] object : result) {
			System.out.println(((CategoriaLancamento) object[0]).getNome() + " - " + object[1]);
		}
		
		return result;
	}

}