package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.TipoLancamento;
import com.webapp.repository.filter.TipoLancamentoFilter;
import com.webapp.util.jpa.Transacional;

public class TiposDespesas implements Serializable {

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

	public List<TipoLancamento> todos() {
		return this.manager.createQuery("from TipoLancamento order by descricao", TipoLancamento.class).getResultList();
	}

	public List<TipoLancamento> filtrados(TipoLancamentoFilter filter) {
		return this.manager.createQuery("from TipoLancamento i where i.descricao like :descricao order by descricao", TipoLancamento.class)
				.setParameter("descricao", "%" + filter.getDescricao() + "%").getResultList();
	}
	
}