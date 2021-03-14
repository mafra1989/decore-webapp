package com.webapp.repository;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Empresa;
import com.webapp.model.Pagamento;
import com.webapp.model.Venda;
import com.webapp.util.jpa.Transacional;

public class Pagamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Pagamento porId(Long id) {
		return this.manager.find(Pagamento.class, id);
	}

	@Transacional
	public Pagamento save(Pagamento pagamento) {
		return this.manager.merge(pagamento);
	}

	@Transacional
	public void remove(Pagamento pagamento) {
		Pagamento pagamentoTemp = new Pagamento();
		pagamentoTemp = this.manager.merge(pagamento);

		this.manager.remove(pagamentoTemp);
	}

	public Pagamento porVenda(Venda venda, Empresa empresa) {
		try {
			return this.manager
					.createQuery("select e from Pagamento e where e.venda.id = :idVenda and e.venda.empresa.id = :empresa", Pagamento.class)
					.setParameter("idVenda", venda.getId())
					.setParameter("empresa", empresa.getId()).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
}