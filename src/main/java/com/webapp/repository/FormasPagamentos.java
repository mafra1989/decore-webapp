package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Empresa;
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

	public List<FormaPagamento> todos(Empresa empresa) {
		return this.manager.createQuery("from FormaPagamento f where f.empresa.id = :id order by f.nome", FormaPagamento.class)
				.setParameter("id", empresa.getId()).getResultList();
	}

	public List<FormaPagamento> filtrados(FormaPagamentoFilter filter, Empresa empresa) {
		return this.manager.createQuery("from FormaPagamento i where i.empresa.id = :id and lower(i.nome) like lower(:nome) order by nome", FormaPagamento.class)
				.setParameter("nome", "%" + filter.getNome() + "%")
				.setParameter("id", empresa.getId()).getResultList();
	}
	
	public FormaPagamento porNome(String nome, Empresa empresa) {
		FormaPagamento formaPagamento = null;
		try {
			formaPagamento = this.manager.createQuery("from FormaPagamento i where i.empresa.id = :id and lower(i.nome) like lower(:nome) order by nome", FormaPagamento.class)
					.setParameter("id", empresa.getId())
					.setParameter("nome", "%" + nome + "%").getSingleResult();
			return formaPagamento;
			
		} catch (NoResultException e) {
			return null;
		}
	}
	
}