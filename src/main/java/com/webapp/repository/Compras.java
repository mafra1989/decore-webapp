package com.webapp.repository;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Empresa;
import com.webapp.model.Produto;
import com.webapp.model.TipoDataLancamento;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Compras implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Compra porId(Long id) {
		return this.manager.find(Compra.class, id);
	}

	public Compra porNumeroCompra(Long numeroCompra, Empresa empresa) {
		try {
			return this.manager.createQuery("from Compra e where e.empresa.id = :empresa AND e.numeroCompra = :numeroCompra", Compra.class)
					.setParameter("empresa", empresa.getId()).setParameter("numeroCompra", numeroCompra).getSingleResult();
		} catch (NoResultException e) {

		}

		return null;
	}
	
	public List<Compra> porUsuario(Usuario usuario) {
		return this.manager.createQuery("from Compra e where e.usuario.nome = :nome order by id", Compra.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}

	@Transacional
	public Compra save(Compra compra) {
		return this.manager.merge(compra);
	}

	@Transacional
	public void remove(Compra compra) {
		Compra compraTemp = new Compra();
		compraTemp = this.manager.merge(compra);

		this.manager.remove(compraTemp);
	}

	public List<Compra> todas(Empresa empresa) {
		return this.manager.createQuery("from Compra where empresa.id = :empresa order by numeroCompra desc", Compra.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public Compra ultimoNCompra(Empresa empresa) {

		try {
			return this.manager.createQuery("from Compra e WHERE e.empresa.id = :empresa order by e.numeroCompra desc", Compra.class).setMaxResults(1)
					.setParameter("empresa", empresa.getId()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	/*public List<Compra> porComprador(Usuario usuario) {
		return this.manager.createQuery("from Compra e where e.usuario.nome = :nome order by id", Compra.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}*/

	public Number totalCompras(Empresa empresa) {
		String jpql = "SELECT sum(i.valorTotal) FROM Compra i Where i.empresa.id = :empresa AND i.ajuste = 'N'";
		
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
	
	
	public Number comprasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataCompra BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(i.valorTotal) FROM Compra i Where i.empresa.id = :empresa AND i.ajuste = 'N' AND i.conta = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number comprasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, List<BigInteger> contas) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String listaDeContas = "";
		if(contas.size() > 0) {
			listaDeContas = "AND i.numeroCompra not in ( :contas )";
		}
		
		String jpql = "SELECT sum(i.valorTotal) FROM Compra i Where i.empresa.id = :empresa " + listaDeContas + " AND i.ajuste = 'N' AND i.compraPaga = 'Y' AND i.conta = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	public List<Compra> comprasAvistaPagas_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT i FROM Compra i Where i.empresa.id = :empresa AND i.ajuste = 'N' AND i.compraPaga = 'Y' AND i.conta = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		return q.getResultList();
	}
	

	@SuppressWarnings("unchecked")
	public List<Compra> comprasFiltradas(Long numeroCompra, Date dateStart, Date dateStop, Usuario usuario, boolean comprasPagas, 
			Empresa empresa, TipoDataLancamento tipoData) {

		String condition = "";
		if (usuario != null && usuario.getId() != null) {
			condition = "AND i.usuario.id = :idUsuario ";
		}
		
		String conditionComprasPagas = "";
		if (comprasPagas) {
			conditionComprasPagas = "AND i.compraPaga = 'Y' ";
		} else {
			conditionComprasPagas = "AND (i.compraPaga = 'N' OR  i.compraPaga = 'Y') ";
		}

		String conditionNumeroCompra = "";
		if (numeroCompra != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroCompra))) {
				if(!String.valueOf(numeroCompra).trim().equals("0")) {
					System.out.println(numeroCompra + " - " + String.valueOf(numeroCompra));
					conditionNumeroCompra = "AND i.numeroCompra = :numeroCompra ";
				}			
			}
		}
		
		String data = "";
		if(tipoData == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento";
		} else if(tipoData == TipoDataLancamento.LANCAMENTO) {
			data = "AND i.dataCompra";
		}

		String jpql = "SELECT i FROM Compra i " + "WHERE i.empresa.id = :empresa AND i.ajuste = 'N' " + data + " between :dateStart and :dateStop " + condition
				+ conditionNumeroCompra + conditionComprasPagas+ " order by i.numeroCompra desc";
		Query q = manager.createQuery(jpql).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("idUsuario", usuario.getId());
		}
		
		if (numeroCompra != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroCompra))) {
				if(!String.valueOf(numeroCompra).trim().equals("0")) {
					System.out.println(numeroCompra + " - " + String.valueOf(numeroCompra));
					q.setParameter("numeroCompra", numeroCompra);
				}			
			}
		}
		
		q.setParameter("empresa", empresa.getId());

		return q.getResultList();
	}

	public Number totalComprasPorData(Number dia, Number mes, Number ano, CategoriaProduto categoriaProduto,
			Produto produto, boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (chartCondition != false) {
			select_Condition = "";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.dia, p.mes, p.ano ";
			orderBy_Condition = "p.dia asc, p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.dia = :dia " + "AND p.mes = :mes " + "AND p.ano = :ano AND p.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("dia", Long.parseLong(String.valueOf(dia)))
				.setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

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

	public Number totalComprasPorMes(Number mes, Number ano, CategoriaProduto categoriaProduto, Produto produto,
			boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (chartCondition != false) {
			select_Condition = "";
			sum_Condition = "sum(i.total)";//sum(p.valorTotal)
			groupBy_Condition = "p.mes, p.ano ";
			orderBy_Condition = "p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mes AND p.ano = :ano AND p.compraPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N'" + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mes", Long.parseLong(String.valueOf(mes))).setParameter("ano",
				Long.parseLong(String.valueOf(ano))).setParameter("empresa", empresa.getId());

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

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

	public Number totalComprasPorSemana(Number semana, Number ano, CategoriaProduto categoriaProduto, Produto produto,
			boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (chartCondition != false) {
			select_Condition = "";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.semana, p.ano ";
			orderBy_Condition = "p.semana asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.semana = :semana AND p.ano = :ano AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("semana", Long.parseLong(String.valueOf(semana)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

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

	public Number totalComprasPorAno(Number ano, CategoriaProduto categoriaProduto, Produto produto,
			boolean chartCondition) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (chartCondition != false) {
			select_Condition = "";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.ano ";
			orderBy_Condition = "p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.ano = :ano AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("ano", Long.parseLong(String.valueOf(ano)));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

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
