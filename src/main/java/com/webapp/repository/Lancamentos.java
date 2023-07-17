package com.webapp.repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.Empresa;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoDataLancamento;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Lancamentos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Lancamento porId(Long id) {
		return this.manager.find(Lancamento.class, id);
	}
	
	public List<Lancamento> porUsuario(Usuario usuario) {
		return this.manager.createQuery("from Lancamento e where e.usuario.nome = :nome order by id", Lancamento.class)
				.setParameter("nome", usuario.getNome()).getResultList();
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
	
	public List<Lancamento> todos(Empresa empresa, CategoriaLancamento categoriaLancamento) {
		return this.manager.createQuery("from Lancamento where empresa.id = :empresa AND categoriaLancamento.id = :id order by id", Lancamento.class)
				.setParameter("empresa", empresa.getId()).setParameter("id", categoriaLancamento.getId()).getResultList();
	}
	
	public Lancamento porNumeroLancamento(Long condigoOperacao, Empresa empresa) {

		try {
			return this.manager.createQuery("from Lancamento e where e.empresa.id = :empresa AND e.numeroLancamento = :numeroLancamento", Lancamento.class)
					.setParameter("empresa", empresa.getId()).setParameter("numeroLancamento", condigoOperacao).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}
	
	public Lancamento porValor(BigDecimal valor, Empresa empresa) {

		try {
			return this.manager.createQuery("from Lancamento e where e.empresa.id = :empresa AND e.valor = :valor AND e.categoriaLancamento.nome = 'Retirada de lucro'", Lancamento.class)
					.setParameter("empresa", empresa.getId()).setParameter("valor", valor.doubleValue()).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	public Lancamento ultimoNLancamento(Empresa empresa) {

		try {
			return this.manager.createQuery("from Lancamento e WHERE e.empresa.id = :empresa order by e.numeroLancamento desc", Lancamento.class)
					.setParameter("empresa", empresa.getId()).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public List<Lancamento> lancamentosFiltrados(Long numeroLancamento, Date dateStart, Date dateStop,
			OrigemLancamento[] origemLancamento, CategoriaLancamento categoriaLancamento,
			DestinoLancamento destinoLancamento, Usuario usuario, String[] categorias, 
			String lancamentosPagos, Empresa empresa, Usuario vendedor, TipoDataLancamento tipoData, String gerouContas) {

		String conditionOrigem = "";
		String conditionCategoria = "";
		String conditionDestino = "";
		String conditionNumeroLancamento = "";
		String conditionUsuario = "";
		String conditionVendedor = "";
		String conditionContas = "";
		
		String conditionLancamentosPagos = "";
		if(lancamentosPagos.equals("1")) {
			conditionLancamentosPagos = "AND i.lancamentoPago = 'Y' ";
		} else if(lancamentosPagos.equals("2")) {
			conditionLancamentosPagos = "AND i.lancamentoPago = 'N' ";
		}


		if (origemLancamento.length > 0) {
			conditionOrigem = "AND i.categoriaLancamento.tipoLancamento.origem in (:origemLancamento) ";
		}

		if (categorias != null && categorias.length > 0) {
			conditionCategoria = "AND i.categoriaLancamento.nome in (:categorias) ";
		} else {
			//conditionCategoria = "AND i.categoriaLancamento.nome != 'Retirada de lucro' ";
		}
		
		if (usuario != null && usuario.getId() != null) { 
			 conditionUsuario += "AND i.usuario.id = :idUsuario ";
		}

		if (destinoLancamento != null && destinoLancamento.getId() != null) {
			conditionDestino = "AND i.destinoLancamento.id = :destinoLancamento ";
		}

		if (numeroLancamento != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroLancamento))) {
				if(!String.valueOf(numeroLancamento).trim().equals("0")) {
					System.out.println(numeroLancamento + " - " + String.valueOf(numeroLancamento));
					conditionNumeroLancamento = "AND i.numeroLancamento = :numeroLancamento ";
				}			
			}
		}
		
		if (vendedor != null && vendedor.getId() != null) { 
			 conditionVendedor += "AND i.usuario.id = :idVendedor ";
		}
		
		String data = "";
		if(tipoData == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento";
		} else if(tipoData == TipoDataLancamento.LANCAMENTO) {
			data = "AND i.dataLancamento";
		}

		if(gerouContas != null) {
			if(gerouContas.equals("1")) {
				conditionContas = "AND i.conta = 'Y' ";
			} else if(gerouContas.equals("2")) {
				conditionContas = "AND i.conta = 'N' ";
			}
		}
		
		String jpql = "SELECT i FROM Lancamento i WHERE i.empresa.id = :empresa " + data + " between :dateStart and :dateStop "
				+ conditionOrigem + conditionCategoria + conditionUsuario + conditionVendedor
				+ conditionDestino + conditionNumeroLancamento + conditionLancamentosPagos + conditionContas
				+ "order by i.numeroLancamento desc";

		System.out.println(jpql);

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (origemLancamento.length > 0) {
			q.setParameter("origemLancamento", Arrays.asList(origemLancamento));
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}
		
		if (usuario != null && usuario.getId() != null) { 
			 q.setParameter("idUsuario", usuario.getId());
		}
		
		if (vendedor != null && vendedor.getId() != null) { 
			 q.setParameter("idVendedor", vendedor.getId());
		}

		if (destinoLancamento != null && destinoLancamento.getId() != null) {
			q.setParameter("destinoLancamento", destinoLancamento.getId());
		}

		if (numeroLancamento != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroLancamento))) {
				if(!String.valueOf(numeroLancamento).trim().equals("0")) {
					System.out.println(numeroLancamento + " - " + String.valueOf(numeroLancamento));
					q.setParameter("numeroLancamento", numeroLancamento);
				}			
			}
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
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorCategoriaMesAtual(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String jpql = "SELECT i.numeroLancamento, sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento and i.conta = 'N' and i.mes = :mesAtual and i.ano = :anoAtual AND i.ajuste = 'N' GROUP BY i.numeroLancamento order by sum(i.valor) desc";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO)
				.setParameter("mesAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.MONTH) + 1)))
				.setParameter("anoAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.YEAR))));
		List<Object[]> result = q.getResultList();

		return result;
	}
	
	public Number totalDeRetiradas(Empresa empresa, CategoriaLancamento categoriaLancamento, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataLancamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.id = :categoriaLancamento " + periodo;
		Query q = manager.createQuery(jpql).setParameter("categoriaLancamento", categoriaLancamento.getId())
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
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
	
	
	public Number totalDeRetiradaComissoes(Empresa empresa, CategoriaLancamento categoriaLancamento, Usuario usuario) {
		
		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.id = :categoriaLancamento and c.usuario.id = :idUsuario";
		Query q = manager.createQuery(jpql).setParameter("categoriaLancamento", categoriaLancamento.getId())
				.setParameter("empresa", empresa.getId())
				.setParameter("idUsuario", usuario.getId());

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
	
	
	public Number totalDespesasAvistaPagasSemRetirada(Empresa empresa) {

		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND c.conta = 'N' AND c.ajuste = 'N' "
				+ "AND c.categoriaLancamento.nome != 'Retirada de lucro'";
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO)
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
	
	
	public Number totalDespesasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {

		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.categoriaLancamento.nome != 'Retirada de lucro' AND c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND c.conta = 'N' AND c.ajuste = 'N' "
				+ periodo;
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
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
	
	
	public Number totalDespesasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, List<BigInteger> contas) {

		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String listaDeContas = "";
		if(contas.size() > 0) {
			listaDeContas = "AND c.numeroLancamento not in ( :contas )";
		}
		
		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.categoriaLancamento.nome != 'Retirada de lucro' AND c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + listaDeContas + " AND c.lancamentoPago = 'Y' and c.conta = 'N' AND c.ajuste = 'N' "
				+ periodo;
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.DEBITO)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(contas.size() > 0) {
			q.setParameter("contas", contas);
		}
		
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
	
	
	public Number totalReceitasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {

		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND c.conta = 'N' AND c.ajuste = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
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
	
	public Number totalReceitasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, List<BigInteger> contas) {

		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String listaDeContas = "";
		if(contas.size() > 0) {
			listaDeContas = "AND c.numeroLancamento not in ( :contas )";
		}
		
		String jpql = "SELECT sum(c.valor) FROM Lancamento c WHERE c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.tipoLancamento.origem = :origemLancamento " + listaDeContas + " AND c.lancamentoPago = 'Y' AND c.conta = 'N' AND c.ajuste = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", OrigemLancamento.CREDITO)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(contas.size() > 0) {
			q.setParameter("contas", contas);
		}
		
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
	public List<Lancamento> lancamentosAvistaPagos(Empresa empresa, Calendar calendarStart, Calendar calendarStop, OrigemLancamento origemLancamento) {

		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT c FROM Lancamento c WHERE c.empresa.id = :empresa AND "
				+ "c.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND c.lancamentoPago = 'Y' AND c.conta = 'N' AND c.ajuste = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("origemLancamento", origemLancamento)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	

	/* DEPRECIADO */
	public Number totalReceitas() {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.conta = 'N'";
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
	public Number totalDeReceitasPorMes(Long mes, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Lancamento i WHERE i.empresa.id = :empresa AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.conta = 'N' AND i.ajuste = 'N' AND i.mes = :mes AND i.ano = :ano";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("origemLancamento", OrigemLancamento.CREDITO)
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

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorSemana(String ano, String semana01, String semana02,
			boolean chartCondition, Empresa empresa) {

		String condition = "AND i.conta = 'N' AND i.ajuste = 'N' AND i.empresa.id = :empresa AND i.categoriaLancamento.nome != 'Retirada de lucro' ";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.semana, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.semana, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem ";
			orderBy_Condition = "i.semana asc, i.ano asc, i.categoriaLancamento.tipoLancamento.origem";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.semana BETWEEN :semanaInicio AND :semanaFim " + "AND i.ano = :ano "
				+ condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano)).setParameter("empresa", empresa.getId());

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
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorMes(String ano, String mes01, String mes02, boolean chartCondition, Empresa empresa) {

		String condition = "AND i.conta = 'N' AND i.ajuste = 'N' AND i.empresa.id = :empresa AND i.categoriaLancamento.nome != 'Retirada de lucro' ";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.mes, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.mes, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem ";
			orderBy_Condition = "i.mes asc, i.ano asc, i.categoriaLancamento.tipoLancamento.origem";
			
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.mes BETWEEN :mesInicio AND :mesFim " + "AND i.ano = :ano "
				+ condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano))
				.setParameter("empresa", empresa.getId());

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
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorAno(String ano01, String ano02, boolean chartCondition, Empresa empresa) {
		
		String condition = "AND i.conta = 'N' AND i.ajuste = 'N' AND i.empresa.id = :empresa AND i.categoriaLancamento.nome != 'Retirada de lucro' ";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem";
			orderBy_Condition = "i.ano asc, i.categoriaLancamento.tipoLancamento.origem";
			
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.ano BETWEEN :anoInicio AND :anoFim "
				+ condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("anoInicio", Long.parseLong(ano01))
				.setParameter("anoFim", Long.parseLong(ano02)).setParameter("empresa", empresa.getId());


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
		
		String condition_retiradas = "";
		if(empresa.getId() == 7111 || empresa.getId() == 7112) {
			condition_retiradas = "AND i.categoriaLancamento.id != 44903 ";
		} else {
			condition_retiradas = "AND i.categoriaLancamento.id != 36 ";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i " + "WHERE i.empresa.id = :empresa AND  " + "i.mes = :mes "
				+ "AND i.ano = :ano " + "AND i.conta = 'N' AND i.ajuste = 'N' " + condition_retiradas + "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mes", Long.parseLong(String.valueOf(mes)))
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
	
	
	public Number totalLancamentosReceitasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Lancamento i "
				+ "WHERE i.empresa.id = :empresa AND i.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.conta = 'N' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime()).setParameter("origemLancamento", OrigemLancamento.CREDITO);

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

	
	public Number totalLancamentosDespesasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Lancamento i "
				+ "WHERE i.empresa.id = :empresa AND i.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.categoriaLancamento.nome != 'Retirada de lucro' AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.conta = 'N' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime()).setParameter("origemLancamento", OrigemLancamento.DEBITO);

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
	public List<Object[]> totalLancamentosDespesasPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.categoriaLancamento.tipoLancamento.origem";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.empresa.id = :empresa AND i.dataLancamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.categoriaLancamento.nome != 'Retirada de lucro' AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.conta = 'N' AND i.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime()).setParameter("origemLancamento", OrigemLancamento.DEBITO);
		
		List<Object[]> result = q.getResultList();
		
		for (Object[] object : result) {
			if ((OrigemLancamento) object[4] == OrigemLancamento.CREDITO) {
				object[4] = "CREDITO";
			} else {
				object[4] = "DEBITO";
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosReceitasPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.numeroLancamento, i.categoriaLancamento.tipoLancamento.origem ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.categoriaLancamento.tipoLancamento.origem";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Lancamento i "
				+ "WHERE i.empresa.id = :empresa AND i.dataLancamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.categoriaLancamento.tipoLancamento.origem = :origemLancamento AND i.conta = 'N' AND i.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime()).setParameter("origemLancamento", OrigemLancamento.CREDITO);
		
		List<Object[]> result = q.getResultList();
		
		for (Object[] object : result) {
			if ((OrigemLancamento) object[4] == OrigemLancamento.CREDITO) {
				object[4] = "CREDITO";
			} else {
				object[4] = "DEBITO";
			}
		}

		return result;
	}
}
