package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webapp.model.Fornecedor;
import com.webapp.repository.filter.FornecedorFilter;
import com.webapp.util.jpa.Transacional;

public class Fornecedores implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Fornecedor porId(Long id) {
		return this.manager.find(Fornecedor.class, id);
	}

	@Transacional
	public Fornecedor save(Fornecedor fornecedor) {
		return this.manager.merge(fornecedor);
	}

	@Transacional
	public void remove(Fornecedor fornecedor) {
		Fornecedor fornecedorTemp = new Fornecedor();
		fornecedorTemp = this.manager.merge(fornecedor);

		this.manager.remove(fornecedorTemp);
	}

	public List<Fornecedor> todos() {
		return this.manager.createQuery("from Fornecedor order by nome", Fornecedor.class).getResultList();
	}

	public List<Fornecedor> filtrados(FornecedorFilter filter) {
		return this.manager.createQuery("from Fornecedor i where i.nome like :nome order by nome", Fornecedor.class)
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}
	
	
	
	
	
	public Number totalInvestidores() {		
		String jpql = "SELECT count(i) FROM Investidor i";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		return count;
	}
}