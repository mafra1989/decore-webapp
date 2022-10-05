package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.DestinoLancamento;
import com.webapp.model.Empresa;
import com.webapp.repository.filter.DestinoLancamentoFilter;
import com.webapp.util.jpa.Transacional;

public class DestinosLancamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public DestinoLancamento porId(Long id) {
		return this.manager.find(DestinoLancamento.class, id);
	}

	@Transacional
	public DestinoLancamento save(DestinoLancamento destinoLancamento) {
		return this.manager.merge(destinoLancamento);
	}

	@Transacional
	public void remove(DestinoLancamento destinoLancamento) {
		DestinoLancamento destinoLancamentoTemp = new DestinoLancamento();
		destinoLancamentoTemp = this.manager.merge(destinoLancamento);

		this.manager.remove(destinoLancamentoTemp);
	}
	
	public DestinoLancamento porDescricao(String descricao) {
		DestinoLancamento destinoLancamento = null;
		try {
			destinoLancamento = this.manager.createQuery("from DestinoLancamento i where lower(i.descricao) like lower(:descricao) order by descricao", DestinoLancamento.class)
					.setParameter("descricao", "%" + descricao + "%").getSingleResult();
			return destinoLancamento;
			
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<DestinoLancamento> todos(Empresa empresa) {
		return this.manager.createQuery("from DestinoLancamento d where d.empresa.id = :empresa order by d.descricao", DestinoLancamento.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public List<DestinoLancamento> filtrados(DestinoLancamentoFilter filter, Empresa empresa) {
		return this.manager.createQuery("from DestinoLancamento i where i.empresa.id = :id and i.descricao like :descricao order by descricao", DestinoLancamento.class)
				.setParameter("descricao", "%" + filter.getDescricao() + "%")
				.setParameter("id", empresa.getId()).getResultList();
	}
	
}