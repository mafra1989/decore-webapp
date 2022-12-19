package com.webapp.repository;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Conta;
import com.webapp.model.Empresa;
import com.webapp.model.PagamentoConta;
import com.webapp.util.jpa.Transacional;

public class PagamentosContas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public PagamentoConta porId(Long id) {
		return this.manager.find(PagamentoConta.class, id);
	}

	@Transacional
	public PagamentoConta save(PagamentoConta pagamentoConta) {
		return this.manager.merge(pagamentoConta);
	}

	@Transacional
	public void remove(PagamentoConta pagamentoConta) {
		PagamentoConta pagamentoContaTemp = new PagamentoConta();
		pagamentoContaTemp = this.manager.merge(pagamentoConta);

		this.manager.remove(pagamentoContaTemp);
	}
	
	public List<PagamentoConta> todosPorConta(Conta conta, Empresa empresa) {
		
		return this.manager
					.createQuery("select e from PagamentoConta e where e.conta.id = :idConta and e.conta.empresa.id = :empresa", PagamentoConta.class)
					.setParameter("idConta", conta.getId())
					.setParameter("empresa", empresa.getId()).getResultList();
	}
	
	public Number totalPagoPorConta(Conta conta, Empresa empresa) {
		
		Number count = (Number) this.manager
					.createQuery("select sum(e.valorPago) from PagamentoConta e where e.conta.id = :idConta and e.conta.empresa.id = :empresa")
					.setParameter("idConta", conta.getId())
					.setParameter("empresa", empresa.getId()).getSingleResult();	

		if (count == null) {
			count = 0;
		}

		return count;
	}
	
	public Number totalPagoPorContaHoje(Conta conta, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		Number count = (Number) this.manager
					.createQuery("select sum(e.valorPago) from PagamentoConta e where e.conta.id = :idConta and e.conta.empresa.id = :empresa and e.dataPagamento BETWEEN :dataInicio AND :dataFim")
					.setParameter("idConta", conta.getId())
					.setParameter("empresa", empresa.getId())
					.setParameter("dataInicio", calendarStart.getTime())
					.setParameter("dataFim", calendarStop.getTime())
					.getSingleResult();	

		if (count == null) {
			count = 0;
		}

		return count;
	}
}