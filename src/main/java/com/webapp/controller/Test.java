package com.webapp.controller;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;

public class Test {

	public static void main(String[] args) {
		@SuppressWarnings("unchecked")
				
		
	 EntityManagerFactory factory;

	  
	        factory = Persistence.createEntityManagerFactory("SmartBankingPU");
	        EntityManager manager = factory.createEntityManager();
	        String jpql = "SELECT i.categoriaLancamento, sum(i.valor) FROM Despesa i WHERE i.origemLancamento = :origem GROUP BY i.categoriaLancamento order by sum(i.valor) desc";
			Query q = manager.createQuery(jpql).setParameter("origem", OrigemLancamento.DEBITO).setMaxResults(5);
			List<Object[]> result = q.getResultList();
			for (Object[] object : result) {
				System.out.println(((CategoriaLancamento) object[0]).getNome() + " - " + object[1]);
			}
			
			
	}
}
