package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Empresa;
import com.webapp.repository.filter.CategoriaProdutoFilter;
import com.webapp.util.jpa.Transacional;

public class CategoriasProdutos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public CategoriaProduto porId(Long id) {
		return this.manager.find(CategoriaProduto.class, id);
	}

	@Transacional
	public CategoriaProduto save(CategoriaProduto categoriaProduto) {
		return this.manager.merge(categoriaProduto);
	}

	@Transacional
	public void remove(CategoriaProduto categoriaProduto) {
		CategoriaProduto categoriaProdutoTemp = new CategoriaProduto();
		categoriaProdutoTemp = this.manager.merge(categoriaProduto);

		this.manager.remove(categoriaProdutoTemp);
	}

	public List<CategoriaProduto> todos(Empresa empresa) {
		return this.manager.createQuery("from CategoriaProduto c where c.empresa.id = :empresa order by c.nome", CategoriaProduto.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}
	
	public List<CategoriaProduto> todosEmDestaque(Empresa empresa) {
		return this.manager.createQuery("from CategoriaProduto c where c.empresa.id = :empresa order by c.id", CategoriaProduto.class)
				.setParameter("empresa", 1L).getResultList();
	}
	
	public CategoriaProduto porNome(String nome) {
		
		try {
			return this.manager.createQuery("from CategoriaProduto i where i.nome = :nome order by nome", CategoriaProduto.class)
					.setParameter("nome", nome).getSingleResult();
		} catch(NoResultException e) {		
		}
		
		return null;
	}
	
	public CategoriaProduto porNome(String nome, Empresa empresa) {
		
		try {
			return this.manager.createQuery("from CategoriaProduto i where i.nome = :nome and i.empresa.id = :empresa order by nome", CategoriaProduto.class)
					.setParameter("nome", nome).setParameter("empresa", empresa.getId()).getSingleResult();
		} catch(NoResultException e) {		
		}
		
		return null;
	}

	public List<CategoriaProduto> filtrados(CategoriaProdutoFilter filter) {
		
		return this.manager.createQuery("from CategoriaProduto i where i.nome like :nome and i.empresa.id = :empresa order by nome", CategoriaProduto.class)
				.setParameter("nome", "%" + filter.getNome() + "%")
				.setParameter("empresa", filter.getEmpresa().getId()).getResultList();	
	}

}