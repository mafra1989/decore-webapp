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
		return this.manager.createQuery("from FormaPagamento order by nome", FormaPagamento.class).getResultList();
	}

	public List<FormaPagamento> filtrados(FormaPagamentoFilter filter) {
		return this.manager.createQuery("from FormaPagamento i where lower(i.nome) like lower(:nome) order by nome", FormaPagamento.class)
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}
	
	public FormaPagamento porNome(String nome) {
		FormaPagamento formaPagamento = null;
		try {
			formaPagamento = this.manager.createQuery("from FormaPagamento i where lower(i.nome) like lower(:nome) order by nome", FormaPagamento.class)
					.setParameter("nome", "%" + nome + "%").getSingleResult();
			return formaPagamento;
			
		} catch (NoResultException e) {
			return null;
		}
	}
	
}