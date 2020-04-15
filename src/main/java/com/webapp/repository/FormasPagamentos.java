package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.FormaPagamento;
import com.webapp.repository.filter.FormaPagamentoFilter;
import com.webapp.util.jpa.Transacional;

public class FormasPagamentos implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public FormaPagamento porId(Long id) {
		return this.manager.find(FormaPagamento.class, id);
	}
	
	@Transacional
	public FormaPagamento save(FormaPagamento formaPagamento) {
		return this.manager.merge(formaPagamento);
	}

	@Transacional
	public void remove(FormaPagamento formaPagamento) {
		FormaPagamento formaPagamentoTemp = new FormaPagamento();
		formaPagamentoTemp = this.manager.merge(formaPagamento);

		this.manager.remove(formaPagamentoTemp);
	}
	
	public List<FormaPagamento> todos() {
		return this.manager.createQuery("from FormaPagamento order by id", FormaPagamento.class).getResultList();
	}
	
	public List<FormaPagamento> filtrados(FormaPagamentoFilter filter) {
		return this.manager.createQuery("from FormaPagamento i where i.descricao like :descricao order by descricao", FormaPagamento.class)
				.setParameter("descricao", "%" + filter.getDescricao() + "%").getResultList();
	}
	
	public FormaPagamento porDescricao(FormaPagamentoFilter filter) {
		try {
			return this.manager.createQuery("from FormaPagamento i where i.descricao = :descricao", FormaPagamento.class)
				.setParameter("descricao", filter.getDescricao()).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	public FormaPagamento porDescricao(FormaPagamento formaPagamento) {
		try {
			return this.manager.createQuery("from FormaPagamento i where i.descricao = :descricao and i.id != :id", FormaPagamento.class)
					.setParameter("descricao", formaPagamento.getDescricao()).setParameter("id", formaPagamento.getId()).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
}