package com.webapp.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Compras implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Compra porId(Long id) {
		return this.manager.find(Compra.class, id);
	}

	public Compra porNumeroCompra(Long numeroCompra, String empresa) {
		try {
			return this.manager.createQuery("from Compra e where e.empresa = :empresa AND e.numeroCompra = :numeroCompra", Compra.class)
					.setParameter("empresa", empresa).setParameter("numeroCompra", numeroCompra).getSingleResult();
		} catch (NoResultException e) {

		}

		return null;
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

	public List<Compra> todas() {
		return this.manager.createQuery("from Compra order by numeroCompra desc", Compra.class).getResultList();
	}

	public Compra ultimoNCompra(String empresa) {

		try {
			return this.manager.createQuery("from Compra e WHERE e.empresa = :empresa order by e.numeroCompra desc", Compra.class).setMaxResults(1)
					.setParameter("empresa", empresa).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	public List<Compra> porComprador(Usuario usuario) {
		return this.manager.createQuery("from Compra e where e.usuario.nome = :nome order by id", Compra.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}

	public Number totalCompras(String empresa) {
		String jpql = "SELECT sum(i.valorTotal) FROM Compra i Where i.empresa = :empresa AND i.ajuste = 'N'";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa);

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
	public List<Compra> comprasFiltradas(Long numeroCompra, Date dateStart, Date dateStop, Usuario usuario, String empresa) {

		String condition = "";
		if (usuario != null && usuario.getId() != null) {
			condition = "AND i.usuario.id = :idUsuario ";
		}

		String conditionNumeroCompra = "";
		if (numeroCompra != null) {
			conditionNumeroCompra = "AND i.numeroCompra = :numeroCompra ";
		}

		String jpql = "SELECT i FROM Compra i " + "WHERE i.empresa = :empresa AND i.dataCompra between :dateStart and :dateStop " + condition
				+ conditionNumeroCompra + " order by i.numeroCompra desc";
		Query q = manager.createQuery(jpql).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("idUsuario", usuario.getId());
		}
		
		if (numeroCompra != null) {
			q.setParameter("numeroCompra", numeroCompra);
		}
		
		q.setParameter("empresa", empresa);

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
			groupBy_Condition = "p.mes, p.ano ";
			orderBy_Condition = "p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.mes = :mes AND p.ano = :ano AND p.ajuste = 'N'" + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mes", Long.parseLong(String.valueOf(mes))).setParameter("ano",
				Long.parseLong(String.valueOf(ano)));

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
