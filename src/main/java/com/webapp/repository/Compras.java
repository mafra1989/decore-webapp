package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Emprestimo;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.filter.EmprestimoFilter;
import com.webapp.util.jpa.Transacional;

public class Compras implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Compra porId(Long id) {
		return this.manager.find(Compra.class, id);
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
		return this.manager.createQuery("from Compra order by id", Compra.class).getResultList();
	}

	public List<Compra> porComprador(Usuario usuario) {
		return this.manager.createQuery("from Compra e where e.usuario.nome = :nome order by id", Compra.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}

	public List<Emprestimo> filtrados(EmprestimoFilter filter) {

		TypedQuery<Emprestimo> typedQuery;

		if (StringUtils.isNotBlank(filter.getNome())) {
			typedQuery = manager.createQuery(
					"select e from Emprestimo e join fetch e.cliente c where c.nome like :nome order by e.id",
					Emprestimo.class).setParameter("nome", "%" + filter.getNome() + "%");

		} else {
			typedQuery = manager.createQuery("select e from Emprestimo e", Emprestimo.class);
		}

		return typedQuery.getResultList();

	}

	public List<Emprestimo> porCliente(Long id) {

		TypedQuery<Emprestimo> typedQuery;

		typedQuery = manager
				.createQuery("select e from Emprestimo e join fetch e.cliente c where c.id = :id order by e.id",
						Emprestimo.class)
				.setParameter("id", id);

		return typedQuery.getResultList();

	}

	public Number totalCompras() {
		String jpql = "SELECT sum(i.valorTotal) FROM Compra i";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
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
				+ "WHERE p.dia = :dia " + "AND p.mes = :mes " + "AND p.ano = :ano " + condition + "group by "
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
			
		} catch(NoResultException e) {
			
		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

	public Number totalComprasPorMes(Number mes, Number ano, CategoriaProduto categoriaProduto,
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
			condition += "AND i.produto.id = :id "; System.out.println(produto.getId());
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
				+ "WHERE p.mes = :mes AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
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
			
		}catch(NoResultException e) {
			
		}
		
		if (value == null) {
			value = 0;
		}

		return value;
	}
	
	public Number totalComprasPorSemana(Number semana, Number ano, CategoriaProduto categoriaProduto,
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
			groupBy_Condition = "p.semana, p.ano ";
			orderBy_Condition = "p.semana asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.semana = :semana AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
				.setParameter("semana", Long.parseLong(String.valueOf(semana)))
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
			
		} catch(NoResultException e) {
			
		}

		if (value == null) {
			value = 0;
		}

		return value;
	}
	
	public Number totalComprasPorAno(Number ano, CategoriaProduto categoriaProduto,
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
			groupBy_Condition = "p.ano ";
			orderBy_Condition = "p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemCompra i join i.compra p "
				+ "WHERE p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql)
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
			
		} catch(NoResultException e) {
			
		}

		if (value == null) {
			value = 0;
		}

		return value;
	}

}