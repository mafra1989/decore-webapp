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

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Lancamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Lancamento porId(Long id) {
		return this.manager.find(Lancamento.class, id);
	}

	@Transacional
	public Lancamento save(Lancamento despesa) {
		return this.manager.merge(despesa);
	}

	@Transacional
	public void remove(Lancamento despesa) {
		Lancamento despesaTemp = new Lancamento();
		despesaTemp = this.manager.merge(despesa);

		this.manager.remove(despesaTemp);
	}
	
	public Lancamento porNumeroLancamento(Long condigoOperacao, String empresa) {

		try {
			return this.manager.createQuery("from Lancamento e where e.empresa = :empresa AND e.numeroLancamento = :numeroLancamento", Lancamento.class)
					.setParameter("empresa", empresa).setParameter("numeroLancamento", condigoOperacao).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	public Lancamento ultimoNLancamento(String empresa) {

		try {
			return this.manager.createQuery("from Lancamento e WHERE e.empresa = :empresa order by e.numeroLancamento desc", Lancamento.class)
					.setParameter("empresa", empresa).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public List<Lancamento> lancamentosFiltrados(Long numeroLancamento, Date dateStart, Date dateStop,
			OrigemLancamento[] origemLancamento, CategoriaLancamento categoriaLancamento,
			DestinoLancamento destinoLancamento, Usuario usuario, String[] categorias, String empresa) {

		String conditionOrigem = "";
		String conditionCategoria = "";
		String conditionDestino = "";
		String conditionNumeroLancamento = "";
		String conditionUsuario = "";

		if (origemLancamento.length > 0) {
			conditionOrigem = "AND i.categoriaLancamento.tipoLancamento.origem in (:origemLancamento) ";
		}

		if (categorias != null && categorias.length > 0) {
			conditionCategoria = "AND i.categoriaLancamento.nome in (:categorias) ";
		}
		
		if (usuario != null && usuario.getId() != null) { 
			 conditionUsuario += "AND i.usuario.id = :idUsuario ";
		}

		if (destinoLancamento != null && destinoLancamento.getId() != null) {
			conditionDestino = "AND i.destinoLancamento.id = :destinoLancamento ";
		}

		if (numeroLancamento != null) {
			conditionNumeroLancamento = "AND i.numeroLancamento = :numeroLancamento ";
		}

		String jpql = "SELECT i FROM Lancamento i WHERE i.empresa = :empresa AND i.dataLancamento between :dateStart and :dateStop "
				+ conditionOrigem + conditionCategoria + conditionUsuario + conditionDestino + conditionNumeroLancamento
				+ "order by i.numeroLancamento desc";

		System.out.println(jpql);

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (origemLancamento.length > 0) {
			q.setParameter("origemLancamento", Arrays.asList(origemLancamento));
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}
		
		if (usuario != null && usuario.getId() != null) { 
			 q.setParameter("idUsuario", usuario.getId());
		}

		if (destinoLancamento != null && destinoLancamento.getId() != null) {
			q.setParameter("destinoLancamento", destinoLancamento.getId());
		}

		if (numeroLancamento != null) {
			q.setParameter("numeroLancamento", numeroLancamento);
		}

		return q.getResultList();
	}

	/* DEPRECIADO */
	public Number totalDebitos() {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO);

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

	/* DEPRECIADO */
	public Number totalCreditos() {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO);

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

	/* DEPRECIADO */
	public Number totalDeReceitasPorDia(Long dia, Long mes, Long ano) {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.dia = :dia AND i.mes = :mes AND i.ano = :ano AND UPPER(i.categoriaLancamento.tipoLancamento.descricao) = 'RECEITAS'";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO)
				.setParameter("dia", dia).setParameter("mes", mes).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	/* DEPRECIADO */
	public Number totalDeReceitasPorSemana(Long semana, Long ano) {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.semana = :semana AND i.ano = :ano AND UPPER(i.categoriaLancamento.tipoLancamento.descricao) = 'RECEITAS'";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO)
				.setParameter("semana", semana).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	/* DEPRECIADO */
	public Number totalDeReceitasPorAno(Long ano) {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.ano = :ano AND UPPER(i.categoriaLancamento.tipoLancamento.descricao) = 'RECEITAS'";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO)
				.setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	/* DEPRECIADO */
	public Number totalDeReceitasPorMes(Long mes, Long ano) {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.mes = :mes AND i.ano = :ano AND UPPER(i.categoriaLancamento.tipoLancamento.descricao) = 'RECEITAS'";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO)
				.setParameter("mes", mes).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	/* DEPRECIADO */
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorData(Calendar calendarStart, Calendar calendarStop,
			CategoriaLancamento categoriaLancamento, boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			condition = "AND i.categoriaLancamento.nome = :categoriaLancamento ";
		}

		if (chartCondition != false) {
			select_Condition = "i.dia, i.mes, i.ano, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.dia, i.mes, i.ano ";
			orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.dia BETWEEN :diaInicio AND :diaFim " + "AND i.mes BETWEEN :mesInicio AND :mesFim "
				+ "AND i.ano BETWEEN :anoInicio AND :anoFim "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
				.setParameter("diaInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.DAY_OF_MONTH))))
				.setParameter("diaFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.DAY_OF_MONTH))))
				.setParameter("mesInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.MONTH) + 1)))
				.setParameter("mesFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.MONTH) + 1)))
				.setParameter("anoInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.YEAR))))
				.setParameter("anoFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.YEAR))))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		if (chartCondition != false) {
			for (Object[] object : result) {
				if ((long) object[0] < 10) {
					object[0] = "0" + object[0];
				}

				if ((long) object[1] < 10) {
					object[1] = "0" + object[1];
				}

			}
		}

		return result;
	}

	/* DEPRECIADO */
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorSemana(String ano, String semana01, String semana02,
			CategoriaLancamento categoriaLancamento, boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			condition = "AND i.categoriaLancamento.nome = :categoriaLancamento ";
		}

		if (chartCondition != false) {
			select_Condition = "i.semana, i.ano, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.semana, i.ano";
			orderBy_Condition = "i.semana asc, i.ano asc";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.semana BETWEEN :semanaInicio AND :semanaFim " + "AND i.ano = :ano "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano)).setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	/* DEPRECIADO */
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorMes(String ano, String mes01, String mes02,
			CategoriaLancamento categoriaLancamento, boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			condition = "AND i.categoriaLancamento.nome = :categoriaLancamento ";
		}

		if (chartCondition != false) {
			select_Condition = "i.mes, i.ano, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.mes, i.ano";
			orderBy_Condition = "i.mes asc, i.ano asc";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.mes BETWEEN :mesInicio AND :mesFim " + "AND i.ano = :ano "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	/* DEPRECIADO */
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorAno(String ano01, String ano02, CategoriaLancamento categoriaLancamento,
			boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			condition = "AND i.categoriaLancamento.nome = :categoriaLancamento ";
		}

		if (chartCondition != false) {
			select_Condition = "i.ano, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.ano";
			orderBy_Condition = "i.ano asc";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.ano BETWEEN :anoInicio AND :anoFim "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("anoInicio", Long.parseLong(ano01))
				.setParameter("anoFim", Long.parseLong(ano02))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	/* DEPRECIADO */
	public Number totalDespesasPorData(Number dia, Number mes, Number ano) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE i.dia = :dia "
				+ "AND i.mes = :mes " + "AND i.ano = :ano "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("dia", Long.parseLong(String.valueOf(dia)))
				.setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

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

	/* DEPRECIADO */
	public Number totalDespesasPorSemana(Number semana, Number ano) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.semana, i.ano ";
		orderBy_Condition = "i.semana asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE "
				+ "i.semana = :semana " + "AND i.ano = :ano "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("semana", Long.parseLong(String.valueOf(semana)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

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

	/* DEPRECIADO */
	public Number totalDespesasPorMes(Number mes, Number ano) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.mes, i.ano ";
		orderBy_Condition = "i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE " + "i.mes = :mes "
				+ "AND i.ano = :ano " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

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

	/* DEPRECIADO */
	public Number totalDespesasPorAno(Number ano) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.ano ";
		orderBy_Condition = "i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE " + "i.ano = :ano "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("ano", Long.parseLong(String.valueOf(ano)))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

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

}
