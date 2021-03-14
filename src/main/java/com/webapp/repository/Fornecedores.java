package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Empresa;
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

	public List<Fornecedor> todos(Empresa empresa) {
		return this.manager.createQuery("from Fornecedor f WHERE f.empresa.id = :empresa order by f.nome", Fornecedor.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public List<Fornecedor> filtrados(FornecedorFilter filter) {
		return this.manager.createQuery("from Fornecedor i where i.nome like :nome AND i.empresa.id = :empresa order by i.nome", Fornecedor.class)
				.setParameter("nome", "%" + filter.getNome() + "%")
				.setParameter("empresa", filter.getEmpresa().getId()).getResultList();
	}
	
	
	public Fornecedor porNome(String nome, Empresa empresa) {
		
		try {
			return this.manager.createQuery("from Fornecedor i where i.nome = :nome and i.empresa.id = :empresa order by nome", Fornecedor.class)
					.setParameter("nome", nome).setParameter("empresa", empresa.getId()).getSingleResult();
		} catch(NoResultException e) {		
		}
		
		return null;
	}
}