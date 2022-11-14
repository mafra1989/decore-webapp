package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

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
	
	public List<Pagamento> todosPorVenda(Venda venda, Empresa empresa) {
		
		return this.manager
					.createQuery("select e from Pagamento e where e.venda.id = :idVenda and e.venda.empresa.id = :empresa", Pagamento.class)
					.setParameter("idVenda", venda.getId())
					.setParameter("empresa", empresa.getId()).getResultList();
	}
	
	public Number totalPagoPorVenda(Venda venda, Empresa empresa) {
		
		Number count = (Number) this.manager
					.createQuery("select sum(e.valor) from Pagamento e where e.venda.id = :idVenda and e.venda.empresa.id = :empresa")
					.setParameter("idVenda", venda.getId())
					.setParameter("empresa", empresa.getId()).getSingleResult();	

		if (count == null) {
			count = 0;
		}

		return count;
	}
}