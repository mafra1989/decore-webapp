package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Empresa;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;
import com.webapp.repository.filter.TipoLancamentoFilter;
import com.webapp.util.jpa.Transacional;

public class TiposLancamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public TipoLancamento porId(Long id) {
		return this.manager.find(TipoLancamento.class, id);
	}

	@Transacional
	public TipoLancamento save(TipoLancamento tipoLancamento) {
		return this.manager.merge(tipoLancamento);
	}

	@Transacional
	public void remove(TipoLancamento tipoLancamento) {
		TipoLancamento tipoLancamentoTemp = new TipoLancamento();
		tipoLancamentoTemp = this.manager.merge(tipoLancamento);

		this.manager.remove(tipoLancamentoTemp);
	}

	public List<TipoLancamento> todos(Empresa empresa) {
		return this.manager.createQuery("from TipoLancamento t where t.empresa.id = :id and t.id != 7 order by t.descricao", TipoLancamento.class)
				.setParameter("id", empresa.getId()).getResultList();
	}
	
	public List<TipoLancamento> porOrigem(OrigemLancamento origem, Empresa empresa) {
		return this.manager.createQuery("from TipoLancamento t where t.empresa.id = :empresa and t.origem = :origem order by t.descricao", TipoLancamento.class)
				.setParameter("empresa", empresa.getId()).setParameter("origem", origem).getResultList();
	}

	public List<TipoLancamento> filtrados(TipoLancamentoFilter filter, Empresa empresa) {
		return this.manager.createQuery("from TipoLancamento i where i.empresa.id = :id and i.id != 7 AND i.descricao like :descricao order by descricao", TipoLancamento.class)
				.setParameter("id", empresa.getId()).setParameter("descricao", "%" + filter.getDescricao() + "%").getResultList();
	}
	
}