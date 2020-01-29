package com.webapp.repository;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
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
	
	@SuppressWarnings("unchecked")
	public List<Lancamento> lancamentosFiltrados(Date dateStart, Date dateStop, OrigemLancamento origemLancamento) {

		String condition = "";
		/*
		if (usuario != null && usuario.getId() != null) {
			condition = "AND i.usuario.id = :idUsuario";
		}*/
		
		if (origemLancamento != null) {
			condition = "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento";
		}

		String jpql = "SELECT i FROM Lancamento i "
				+ "WHERE i.dataLancamento between :dateStart and :dateStop " + condition
				+ " order by i.dataLancamento desc";
		Query q = manager.createQuery(jpql).setParameter("dateStart", dateStart)
				.setParameter("dateStop", dateStop);

		if (origemLancamento != null) {
			q.setParameter("origemLancamento", origemLancamento);
		}

		return q.getResultList();
	}

	public Number totalDebitos() {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO);

		Number count = 0;
		try {
		    count = (Number) q.getSingleResult();
			
		} catch(NoResultException e) {
			
		}

                if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalCreditos() {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO);

		Number count = 0;
		try {
		    count = (Number) q.getSingleResult();
			
		} catch(NoResultException e) {
			
		}

                if (count == null) {
			count = 0;
		}

		return count;
	}

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

	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorCategoriaMesAtual() {
		Calendar calendar = Calendar.getInstance();
		String jpql = "SELECT c.nome, sum(i.valor) FROM Lancamento i join i.categoriaLancamento c WHERE i.categoriaLancamento.tipoLancamento.origem = :origem and i.mes = :mesAtual GROUP BY c.nome order by sum(i.valor) desc";
		Query q = manager.createQuery(jpql).setParameter("origem", OrigemLancamento.DEBITO)
				.setParameter("mesAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.MONTH) + 1)))
				.setMaxResults(5);
		List<Object[]> result = q.getResultList();

		return result;
	}

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
				+ "AND i.ano BETWEEN :anoInicio AND :anoFim " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
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
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano)).setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

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
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

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
				+ "WHERE i.ano BETWEEN :anoInicio AND :anoFim " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("anoInicio", Long.parseLong(ano01))
				.setParameter("anoFim", Long.parseLong(ano02))
				.setParameter("origemLancamento", OrigemLancamento.DEBITO);

		if (categoriaLancamento != null && categoriaLancamento.getId() != null) {
			q.setParameter("categoriaLancamento", categoriaLancamento.getNome());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

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
				+ "AND i.mes = :mes " + "AND i.ano = :ano " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
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
				+ "i.semana = :semana " + "AND i.ano = :ano " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
				.setParameter("semana", Long.parseLong(String.valueOf(semana)))
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

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE "
				+ "i.mes = :mes " + "AND i.ano = :ano " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
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

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE "
				+ "i.ano = :ano " + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
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

}
