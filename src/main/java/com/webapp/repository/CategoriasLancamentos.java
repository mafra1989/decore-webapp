package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.repository.filter.CategoriaLancamentoFilter;
import com.webapp.util.jpa.Transacional;

public class CategoriasLancamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public CategoriaLancamento porId(Long id) {
		return this.manager.find(CategoriaLancamento.class, id);
	}

	@Transacional
	public CategoriaLancamento save(CategoriaLancamento categoriaLancamento) {
		return this.manager.merge(categoriaLancamento);
	}

	@Transacional
	public void remove(CategoriaLancamento categoriaLancamento) {
		CategoriaLancamento categoriaLancamentoTemp = new CategoriaLancamento();
		categoriaLancamentoTemp = this.manager.merge(categoriaLancamento);

		this.manager.remove(categoriaLancamentoTemp);
	}

	public CategoriaLancamento porNome(String nome, String empresa) {
		CategoriaLancamento categoriaLancamento = null;
		try {
			categoriaLancamento = this.manager.createQuery("from CategoriaLancamento i where (i.empresa is null OR i.empresa = :empresa) AND lower(i.nome) like lower(:nome) order by nome", CategoriaLancamento.class)
					.setParameter("empresa", empresa).setParameter("nome", "%" + nome + "%").getSingleResult();
			return categoriaLancamento;
			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public List<CategoriaLancamento> todos(String empresa) {
		return this.manager.createQuery("from CategoriaLancamento where (empresa is null OR empresa = :empresa) order by nome", CategoriaLancamento.class)
				.setParameter("empresa", empresa).getResultList();
	}

	public List<CategoriaLancamento> todasDespesas(String empresa) {
		return this.manager
				.createQuery("from CategoriaLancamento c where (c.empresa is null OR c.empresa = :empresa) AND c.tipoLancamento.origem = :origem order by nome",
						CategoriaLancamento.class)
				.setParameter("empresa", empresa)
				.setParameter("origem", OrigemLancamento.DEBITO).getResultList();
	}

	public List<CategoriaLancamento> todasReceitas(String empresa) {
		return this.manager
				.createQuery("from CategoriaLancamento c where (c.empresa is null OR c.empresa = :empresa) AND c.tipoLancamento.origem = :origem order by nome",
						CategoriaLancamento.class)
				.setParameter("empresa", empresa)
				.setParameter("origem", OrigemLancamento.CREDITO).getResultList();
	}

	public List<CategoriaLancamento> porOrigem(OrigemLancamento origem, String empresa) {
		return this.manager.createQuery("from CategoriaLancamento c where (c.empresa is null OR c.empresa = :empresa) AND c.origem = :origem order by c.nome",
				CategoriaLancamento.class)
				.setParameter("empresa", empresa)
				.setParameter("origem", origem).getResultList();
	}

	public List<CategoriaLancamento> filtrados(CategoriaLancamentoFilter filter) {
		return this.manager.createQuery("from CategoriaLancamento i where (i.empresa is null OR i.empresa = :empresa) AND i.nome like :nome order by nome",
				CategoriaLancamento.class)
				.setParameter("empresa", filter.getEmpresa())
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}

}