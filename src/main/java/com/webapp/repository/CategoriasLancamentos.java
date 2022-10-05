package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Empresa;
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

	public CategoriaLancamento porNome(String nome, Empresa empresa) {
		CategoriaLancamento categoriaLancamento = null;
		try {
			categoriaLancamento = this.manager.createQuery("from CategoriaLancamento i where i.empresa.id = :empresa and lower(i.nome) like :nome order by nome", CategoriaLancamento.class)
					.setParameter("empresa", empresa.getId())
					.setParameter("nome", "%" + nome.toLowerCase() + "%").getSingleResult();
			return categoriaLancamento;
			
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public List<CategoriaLancamento> todos(Empresa empresa) {//where id != 36
		return this.manager.createQuery("from CategoriaLancamento c where c.empresa.id = :empresa order by nome", CategoriaLancamento.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}
	
	public List<CategoriaLancamento> todasDespesas(Empresa empresa) {
		return this.manager
				.createQuery("from CategoriaLancamento c where c.empresa.id = :empresa AND c.nome != 'Retirada de lucro' AND c.tipoLancamento.origem = :origem order by nome",
						CategoriaLancamento.class).setParameter("empresa", empresa.getId())
				.setParameter("origem", OrigemLancamento.DEBITO).getResultList();
	}

	public List<CategoriaLancamento> todasReceitas(Empresa empresa) {
		return this.manager
				.createQuery("from CategoriaLancamento c where c.empresa.id = :empresa AND c.tipoLancamento.origem = :origem order by nome",
						CategoriaLancamento.class).setParameter("empresa", empresa.getId())
				.setParameter("origem", OrigemLancamento.CREDITO).getResultList();
	}

	/*
	public List<CategoriaLancamento> todasDespesas(Empresa empresa) {
		return this.manager
				.createQuery("from CategoriaLancamento c where (c.empresa.id = :empresa OR c.empresa.id = null) AND c.id != 36 AND c.tipoLancamento.origem = :origem order by nome",
						CategoriaLancamento.class).setParameter("empresa", empresa.getId())
				.setParameter("origem", OrigemLancamento.DEBITO).getResultList();
	}

	public List<CategoriaLancamento> todasReceitas(Empresa empresa) {
		return this.manager
				.createQuery("from CategoriaLancamento c where (c.empresa.id = :empresa OR c.empresa.id = null) AND c.tipoLancamento.origem = :origem order by nome",
						CategoriaLancamento.class).setParameter("empresa", empresa.getId())
				.setParameter("origem", OrigemLancamento.CREDITO).getResultList();
	}
	*/

	public List<CategoriaLancamento> porOrigem(OrigemLancamento origem, Empresa empresa) {
		return this.manager.createQuery("from CategoriaLancamento c where c.empresa.id = :empresa AND c.origem = :origem order by c.nome",
				CategoriaLancamento.class).setParameter("empresa", empresa.getId())
				.setParameter("origem", origem).getResultList();
	}

	public List<CategoriaLancamento> filtrados(CategoriaLancamentoFilter filter) {
		return this.manager.createQuery("from CategoriaLancamento i where i.empresa.id = :id and i.nome != 'Retirada de lucro' AND lower(i.nome) like :nome order by nome",
				CategoriaLancamento.class).setParameter("id", filter.getEmpresa().getId())
				.setParameter("nome", "%" + filter.getNome().toLowerCase() + "%").getResultList();
	}

}