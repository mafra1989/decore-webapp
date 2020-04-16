package com.webapp.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.Conta;
import com.webapp.model.OrigemConta;
import com.webapp.model.TipoOperacao;
import com.webapp.util.jpa.Transacional;

public class Contas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Conta porId(Long id) {
		return this.manager.find(Conta.class, id);
	}

	@Transacional
	public Conta save(Conta conta) {
		return this.manager.merge(conta);
	}

	@Transacional
	public void remove(Conta conta) {
		Conta contaTemp = new Conta();
		contaTemp = this.manager.merge(conta);

		this.manager.remove(contaTemp);
	}

	public List<Conta> todas() {
		return this.manager.createQuery("from Conta order by id", Conta.class).getResultList();
	}

	public Number porContasPagas(String tipo, String operacao) {

		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao);

		Number count = 0;
		try {
			count = (Number) q.getSingleResult();

		} catch (NoResultException e) {

		}

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number porContasPagas(String tipo) {

		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.tipo = :tipo AND c.status = 'Y'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo);

		Number count = 0;
		try {
			count = (Number) q.getSingleResult();

		} catch (NoResultException e) {

		}

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public List<Conta> porContasPagas(Long codigoOperacao, String operacao) {
		return this.manager.createQuery(
				"from Conta i where i.codigoOperacao = :codigoOperacao and operacao = :operacao and i.status = 'Y'",
				Conta.class).setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao)
				.getResultList();
	}

	public List<Conta> porCodigoOperacao(Long codigoOperacao, String operacao) {
		return this.manager
				.createQuery("from Conta i where i.codigoOperacao = :codigoOperacao and operacao = :operacao",
						Conta.class)
				.setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Conta> contasFiltradas(Long codigo, TipoOperacao tipoOperacao, Calendar vencimento,
			OrigemConta[] origemConta, Date vencimento2, boolean contasPagas) {

		String conditionCodigo = "";
		String conditionTipoOperacao = "";
		String conditionOrigemConta = "";
		String conditionContasPagas = "";

		if (codigo != null) {
			conditionCodigo = "AND c.codigoOperacao = :codigo ";
		}

		if (tipoOperacao != null) {
			conditionTipoOperacao = "AND c.operacao = :tipoOperacao ";
		}

		if (origemConta.length > 0) {
			conditionOrigemConta = "AND c.tipo in (:origemConta) ";
		}

		if (contasPagas) {
			conditionContasPagas = "AND (c.status = 'Y' OR c.status = 'N') ";
		} else {
			conditionContasPagas = "AND c.status = 'N' ";
		}

		String jpql = "SELECT c FROM Conta c " + "WHERE c.id > 0 " + conditionCodigo + conditionTipoOperacao
				+ conditionOrigemConta + conditionContasPagas + "order by c.vencimento ASC, c.id asc";

		Query q = manager.createQuery(jpql);/* setParameter("vencimento", vencimento.getTime()); */

		if (codigo != null) {
			q.setParameter("codigo", codigo);
		}

		if (tipoOperacao != null) {
			q.setParameter("tipoOperacao", tipoOperacao.name());
		}

		if (origemConta.length > 0) {
			conditionOrigemConta = "AND c.tipo = :tipoOperacao in (:origemConta) ";
			q.setParameter("origemConta",
					Arrays.asList(Arrays.toString(origemConta).replace("[", "").replace("]", "").trim().split(",")));
		}

		return q.getResultList();
	}
}