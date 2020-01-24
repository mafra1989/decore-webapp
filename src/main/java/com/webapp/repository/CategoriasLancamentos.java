package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;
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

	public List<CategoriaLancamento> todos() {
		return this.manager.createQuery("from CategoriaLancamento order by nome", CategoriaLancamento.class).getResultList();
	}
	
	public List<CategoriaLancamento> porOrigem(OrigemLancamento origem) {
		return this.manager.createQuery("from CategoriaLancamento c where c.origem = :origem order by c.nome", CategoriaLancamento.class).setParameter("origem", origem).getResultList();
	}

	public List<CategoriaLancamento> filtrados(CategoriaLancamentoFilter filter) {
		return this.manager.createQuery("from CategoriaLancamento i where i.nome like :nome order by nome", CategoriaLancamento.class)
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}
	
}