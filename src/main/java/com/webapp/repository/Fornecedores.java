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

	public List<Fornecedor> todos(String empresa) {
		return this.manager.createQuery("from Fornecedor f WHERE f.empresa = :empresa order by f.nome", Fornecedor.class)
				.setParameter("empresa", empresa).getResultList();
	}

	public List<Fornecedor> filtrados(FornecedorFilter filter) {
		return this.manager.createQuery("from Fornecedor i where i.nome like :nome AND i.empresa = :empresa order by i.nome", Fornecedor.class)
				.setParameter("nome", "%" + filter.getNome() + "%")
				.setParameter("empresa", filter.getEmpresa()).getResultList();
	}
	
	
	
	
	
	public Number totalInvestidores() {		
		String jpql = "SELECT count(i) FROM Investidor i";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		return count;
	}
}