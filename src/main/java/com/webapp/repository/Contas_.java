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

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;
import com.webapp.model.Conta;
import com.webapp.model.Empresa;
import com.webapp.model.OrigemConta;
import com.webapp.model.PagamentoConta;
import com.webapp.model.TipoOperacao;
import com.webapp.util.jpa.Transacional;

public class Contas_ implements Serializable {

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

	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorCategoriaMesAtual() {
		Calendar calendar = Calendar.getInstance();
		String jpql = "SELECT i.codigoOperacao, sum(i.valor) FROM Conta i WHERE i.operacao = 'LANCAMENTO' and i.tipo = 'DEBITO' and i.status = 'Y' and i.mes = :mesAtual and i.ano = :anoAtual AND i.ajuste = 'N' and i.exclusao = 'N' GROUP BY i.codigoOperacao order by sum(i.valor) desc";
		Query q = manager.createQuery(jpql)
				.setParameter("mesAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.MONTH) + 1)))
				.setParameter("anoAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.YEAR))));
		List<Object[]> result = q.getResultList();

		return result;
	}
	
	
	public Number vendasAPagarPagas(String tipo, String operacao, Empresa empresa) {
		//modificado de subtotal para valor
		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.parcela <> 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

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
	
	public Number porContasPagas(String tipo, String operacao, Empresa empresa) {

		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.parcela <> 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

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
	
	public Number lucroEmVendasAPagarPagas(String tipo, String operacao, Empresa empresa) {

		String jpql = "SELECT sum(c.lucro) FROM Conta c WHERE c.parcela <> 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

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
	
	@SuppressWarnings("unchecked")
	public List<Conta> comissaoEmVendasAPagarPagas(String tipo, String operacao, Empresa empresa) {

		String jpql = "FROM Conta c WHERE (c.parcela = 'AVISTA' and c.status = 'N') OR c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.ajuste = 'N' and c.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		return q.getResultList();
	}

	public Number porContasPagas(String tipo, Empresa empresa) {

		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.empresa.id = :empresa AND c.tipo = :tipo AND c.status = 'Y' AND c.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("empresa", empresa.getId());

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
				"from Conta i where i.codigoOperacao = :codigoOperacao and operacao = :operacao and i.status = 'Y' AND i.ajuste = 'N'",
				Conta.class).setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao)
				.getResultList();
	}

	public List<Conta> porCodigoOperacao(Long codigoOperacao, String operacao, Empresa empresa) {
		return this.manager
				.createQuery("from Conta i where i.empresa.id = :empresa AND i.codigoOperacao = :codigoOperacao and operacao = :operacao AND i.ajuste = 'N' order by i.id asc",
						Conta.class)
				.setParameter("empresa", empresa.getId())
				.setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Conta> contasFiltradas(Long codigo, TipoOperacao tipoOperacao, Date mes, Calendar vencimento,
			OrigemConta[] origemConta, Date vencimento2, boolean contasPagas, Empresa empresa) {

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

		if (origemConta != null && origemConta.length > 0) {
			conditionOrigemConta = "AND c.tipo in (:origemConta) ";
		}

		if (contasPagas) {
			conditionContasPagas = "AND (c.status = 'Y' OR c.status = 'N') ";
		} else {
			conditionContasPagas = "AND c.status = 'N' ";
		}

		Calendar calendario = Calendar.getInstance();
		calendario.setTime(mes);

		String jpql = "SELECT c FROM Conta c " + "WHERE c.empresa.id = :empresa AND c.id > 0 AND c.mes = :mes AND c.ano = :ano " + conditionCodigo
				+ conditionTipoOperacao + conditionOrigemConta + conditionContasPagas
				+ "order by c.vencimento ASC, c.id asc";//c.vencimento ASC
		
		System.out.println(jpql);

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mes", (long) calendario.get(Calendar.MONTH) + 1).setParameter("ano",
				(long) calendario.get(Calendar.YEAR));/* setParameter("vencimento", vencimento.getTime()); */

		if (codigo != null) {
			q.setParameter("codigo", codigo);
		}

		if (tipoOperacao != null) {
			q.setParameter("tipoOperacao", tipoOperacao.name());
		}

		if (origemConta != null && origemConta.length > 0) {
			q.setParameter("origemConta",
					Arrays.asList(Arrays.toString(origemConta).replace("[", "").replace("]", "").replace(" ", "").trim().split(",")));		
		}

		return q.getResultList();
	}

	public Number totalDeReceitasPorDia(Long dia, Long mes, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.dia = :dia AND i.mes = :mes AND i.ano = :ano";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dia", dia).setParameter("mes", mes).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalComprasPorData(Number dia, Number mes, Number ano) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i " + "WHERE i.dia = :dia "
				+ "AND i.mes = :mes " + "AND i.ano = :ano "
				+ "AND i.tipo = 'DEBITO' AND i.operacao = 'COMPRA' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("dia", Long.parseLong(String.valueOf(dia)))
				.setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

		Number value = 0;
		try {
			value = (Number) q.getSingleResult();
		} catch (NoResultException e) {

		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

	public Number totalDespesasPorData(Number dia, Number mes, Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.empresa.id = :empresa AND i.dia = :dia "
				+ "AND i.mes = :mes " + "AND i.ano = :ano "
				+ "AND i.tipo = 'DEBITO' AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dia", Long.parseLong(String.valueOf(dia)))
				.setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

		Number value = 0;
		try {
			value = (Number) q.getSingleResult();
		} catch (NoResultException e) {

		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

	public Number totalDeReceitasPorSemana(Long semana, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.semana = :semana AND i.ano = :ano AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semana", semana).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalDespesasPorSemana(Number semana, Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.semana, i.ano ";
		orderBy_Condition = "i.semana asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.empresa.id = :empresa AND i.semana = :semana "
				+ "AND i.ano = :ano AND i.tipo = 'DEBITO' AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semana", Long.parseLong(String.valueOf(semana)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

		Number value = 0;
		try {
			value = (Number) q.getSingleResult();
		} catch (NoResultException e) {

		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

	public Number totalDeReceitasPorMes(Long mes, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.operacao = 'LANCAMENTO' AND i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.mes = :mes AND i.ano = :ano AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mes", mes).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalDespesasPorMes(Number mes, Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.mes, i.ano ";
		orderBy_Condition = "i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.operacao = 'LANCAMENTO' AND i.empresa.id = :empresa AND i.mes = :mes "
				+ "AND i.ano = :ano AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mes", Long.parseLong(String.valueOf(mes))).setParameter("ano",
				Long.parseLong(String.valueOf(ano)));

		Number value = 0;
		try {
			value = (Number) q.getSingleResult();
		} catch (NoResultException e) {

		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

	public Number totalDeReceitasPorAno(Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ano = :ano AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalDespesasPorAno(Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.ano ";
		orderBy_Condition = "i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.empresa.id = :empresa AND i.ano = :ano "
				+ "AND i.tipo = 'DEBITO' AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("ano", Long.parseLong(String.valueOf(ano)));

		Number value = 0;
		try {
			value = (Number) q.getSingleResult();
		} catch (NoResultException e) {

		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.tipo";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());
/*
		List<Object[]> result = q.getResultList();

		for (Object[] object : result) {
			if ((long) object[0] < 10) {
				object[0] = "0" + object[0];
			}

			if ((long) object[1] < 10) {
				object[1] = "0" + object[1];
			}
		}
*/
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorSemana(String ano, String semana01, String semana02,
			boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.semana, i.ano, i.codigoOperacao, i.tipo, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.semana, i.ano, i.codigoOperacao, i.tipo ";
			orderBy_Condition = "i.semana asc, i.ano asc, i.tipo";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.semana = :semanaInicio " + "AND i.ano = :ano "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));

		List<Object[]> result = q.getResultList();

		return result;
	}
	

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorMes(String ano, String mes01, String mes02, boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.mes, i.ano, i.codigoOperacao, i.tipo, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.mes, i.ano, i.codigoOperacao, i.tipo ";
			orderBy_Condition = "i.mes asc, i.ano asc, i.tipo";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.mes BETWEEN :mesInicio AND :mesFim " + "AND i.ano = :ano "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano));

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorAno(String ano01, String ano02, boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.ano, i.codigoOperacao, i.tipo, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.ano, i.codigoOperacao, i.tipo";
			orderBy_Condition = "i.ano asc, i.tipo";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i " 
				+ "WHERE i.empresa.id = :empresa AND i.ano = :anoInicio "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'  " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("anoInicio", Long.parseLong(ano01));

		List<Object[]> result = q.getResultList();

		return result;
	}
	
	
	public Number totalDespesasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	
	public Number totalComprasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		//AND i.operacao = 'LANCAMENTO' 
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'COMPRA' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	
	public Number totalPagamentosParcialComprasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		//AND i.operacao = 'LANCAMENTO' 
		String jpql = "SELECT sum(i.valorPago) FROM PagamentoConta i "
				+ "WHERE i.conta.empresa.id = :empresa AND i.conta.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.conta.operacao = 'COMPRA' AND i.conta.tipo = 'DEBITO' AND i.conta.status = 'N' AND i.conta.ajuste = 'N' and i.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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

	
	@SuppressWarnings("unchecked")
	public List<Conta> contasRecebidasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND (i.operacao = 'LANCAMENTO' OR i.operacao = 'VENDA') AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND (i.operacao = 'LANCAMENTO' OR i.operacao = 'COMPRA') AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	
	public Number totalVendasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		//AND i.operacao = 'LANCAMENTO' 
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalVendasPagasParcialmente(Empresa empresa) {
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalComprasPagasParcialmente(Empresa empresa) {
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'COMPRA' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalDespesasPagasParcialmente(Empresa empresa) {
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalReceitasPagasParcialmente(Empresa empresa) {
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalReceitasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAReceberValor(Empresa empresa) {

		String jpql = "SELECT sum(i.saldo) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalAReceberPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAReceberEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAReceberEmAtraso(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	public Number totalDespesasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		//String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
		//		+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
		//		+ "AND c.conta.operacao = 'LANCAMENTO' AND c.conta.tipo = 'DEBITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	
	public Number totalAPagarPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAPagarPorDiaValor_(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valorPago) FROM PagamentoConta i "
				+ "WHERE i.conta.empresa.id = :empresa AND i.conta.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.conta.tipo = 'DEBITO' AND i.conta.status = 'N' AND i.conta.ajuste = 'N' and i.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAReceberPorDiaValor_(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valorPago) FROM PagamentoConta i "
				+ "WHERE i.conta.empresa.id = :empresa AND i.conta.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.conta.tipo = 'CREDITO' AND i.conta.status = 'N' AND i.conta.ajuste = 'N' and i.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAPagarValor(Empresa empresa) {

		String jpql = "SELECT sum(i.saldo) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAPagar(Empresa empresa) {

		String jpql = "FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAPagarPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAReceber(Empresa empresa) {

		String jpql = "FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAReceberPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAPagarEmAtraso(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

		List<Conta> contas = q.getResultList();

		return contas;
	}
	
	public Number totalAPagarEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	
	public Number totalPagoContasAPagarEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.vencimento < :dataInicio "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	
	public Number totalPagoContasAReceberEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.vencimento < :dataInicio "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAPagar(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAPagarPorConta(Empresa empresa, Conta conta, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.id = :contaId AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("contaId", conta.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAReceberPorConta(Empresa empresa, Conta conta, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.id = :contaId AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("contaId", conta.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAReceber(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	public Number totalPagoHojeContasAPagarEmAtrasoValor(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

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
	
	public Number totalPagoHojeContasAReceberEmAtrasoValor(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

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
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPagasPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.tipo";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalReceitasPagasPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.tipo";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		return q.getResultList();
	}
}