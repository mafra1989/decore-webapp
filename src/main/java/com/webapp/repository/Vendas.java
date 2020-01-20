package com.webapp.repository;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.util.jpa.Transacional;

public class Vendas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Venda porId(Long id) {
		return this.manager.find(Venda.class, id);
	}

	@Transacional
	public Venda save(Venda venda) {
		return this.manager.merge(venda);
	}

	@Transacional
	public void remove(Venda venda) {
		Venda vendaTemp = new Venda();
		vendaTemp = this.manager.merge(venda);

		this.manager.remove(vendaTemp);
	}

	public List<Venda> todas() {
		return this.manager.createQuery("from Venda order by id", Venda.class).getResultList();
	}

	public List<Venda> porVendedor(Usuario usuario) {
		return this.manager
				.createQuery("from Venda e where e.usuario.nome = :nome order by id", Venda.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}
	
	
	public Number totalVendas() {		
		String jpql = "SELECT sum(i.valorTotal) FROM Venda i";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}
	
	
	public Number totalVendasPorDiaDaSemana(Long nomeDia) {		
		
		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.nomeDia = :nomeDia";
		Query q = manager.createQuery(jpql).setParameter("nomeDia", nomeDia);
		
		Number count = (Number) q.getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorCategoria() {		
		
		String jpql = "SELECT p.categoriaProduto.nome, SUM(i.total) FROM ItemVenda i JOIN i.produto p GROUP BY p.categoriaProduto.nome ORDER BY p.categoriaProduto.nome ASC";
		Query q = manager.createQuery(jpql);
		List<Object[]> result = q.getResultList();
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalParaVendasPorCategoria() {		
		
		String jpql = "SELECT p.categoriaProduto.nome, sum(p.quantidadeAtual * p.precoVenda) FROM Produto p group by p.categoriaProduto.nome order by p.categoriaProduto.nome asc";
		Query q = manager.createQuery(jpql);
		List<Object[]> result = q.getResultList();
		
		return result;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorData(Calendar calendarStart, Calendar calendarStop, CategoriaProduto categoriaProduto, Produto produto, boolean chartCondition) {		
		
		String condition = ""; String select_Condition = ""; String sum_Condition = ""; String groupBy_Condition = ""; String orderBy_Condition = ""; 
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}
		
		if(produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if(chartCondition != false) {
			select_Condition = "p.dia, p.mes, p.ano, ";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.dia, p.mes, p.ano ";
			orderBy_Condition = "p.dia asc, p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.dia BETWEEN :diaInicio AND :diaFim "
				+ "AND p.mes BETWEEN :mesInicio AND :mesFim "
				+ "AND p.ano BETWEEN :anoInicio AND :anoFim "
				+ condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("diaInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.DAY_OF_MONTH))))
				.setParameter("diaFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.DAY_OF_MONTH))))
				.setParameter("mesInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.MONTH) + 1)))
				.setParameter("mesFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.MONTH) + 1)))
				.setParameter("anoInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.YEAR))))
				.setParameter("anoFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.YEAR))));
		
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}
		
		if(produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		List<Object[]> result = q.getResultList();
		
		if(chartCondition != false) {
			for (Object[] object : result) {
				if((long)object[0] < 10) {
					object[0] = "0" + object[0];
				}
				
				if((long)object[1] < 10) {
					object[1] = "0" + object[1];
				}
			}
		}
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorSemana(String ano, String semana01, String semana02, CategoriaProduto categoriaProduto, Produto produto, boolean chartCondition) {		
		
		String condition = ""; String select_Condition = ""; String sum_Condition = ""; String groupBy_Condition = ""; String orderBy_Condition = "";
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}
		
		if(produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if(chartCondition != false) {
			select_Condition = "p.semana, p.ano, ";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.semana, p.ano";
			orderBy_Condition = "p.semana asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.semana BETWEEN :semanaInicio AND :semanaFim "
				+ "AND p.ano = :ano "
				+ condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}
		
		if(produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		List<Object[]> result = q.getResultList();
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorMes(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto, Produto produto, boolean chartCondition) {		
		
		String condition = ""; String select_Condition = ""; String sum_Condition = ""; String groupBy_Condition = ""; String orderBy_Condition = "";
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}
		
		if(produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if(chartCondition != false) {
			select_Condition = "p.mes, p.ano, ";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.mes, p.ano";
			orderBy_Condition = "p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.mes BETWEEN :mesInicio AND :mesFim "
				+ "AND p.ano = :ano "
				+ condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02))
				.setParameter("ano", Long.parseLong(ano));
		
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}
		
		if(produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		List<Object[]> result = q.getResultList();
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorLote(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto, Produto produto, boolean chartCondition) {		
		
		String condition = ""; String select_Condition = ""; String sum_Condition = ""; String groupBy_Condition = ""; String orderBy_Condition = "";
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}
		
		if(produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if(chartCondition != false) {
			select_Condition = "p.mes, p.ano, ";
			sum_Condition = "sum(p.valorTotal), i.compra.mes";
			groupBy_Condition = "i.compra.mes, i.produto.categoriaProduto.nome ";
			orderBy_Condition = "i.compra.mes asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE i.compra.mes BETWEEN :mesInicio AND :mesFim "
				+ "AND i.compra.ano = :ano "
				+ condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02))
				.setParameter("ano", Long.parseLong(ano));
		
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}
		
		if(produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		List<Object[]> result = q.getResultList();
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorAno(String ano01, String ano02, CategoriaProduto categoriaProduto, Produto produto, boolean chartCondition) {		
		
		String condition = ""; String select_Condition = ""; String sum_Condition = ""; String groupBy_Condition = ""; String orderBy_Condition = "";
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}
		
		if(produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if(chartCondition != false) {
			select_Condition = "p.ano, ";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.ano";
			orderBy_Condition = "p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.ano BETWEEN :anoInicio AND :anoFim "
				+ condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("anoInicio", Long.parseLong(ano01))
				.setParameter("anoFim", Long.parseLong(ano02));
		
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}
		
		if(produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		List<Object[]> result = q.getResultList();
		
		return result;
	}
	

	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucrosPorData(Calendar calendarStart, Calendar calendarStop, CategoriaProduto categoriaProduto, Produto produto, boolean chartCondition) {		
		
		String condition = ""; String select_Condition = ""; String sum_Condition = ""; String groupBy_Condition = ""; String orderBy_Condition = ""; 
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}
		
		if(produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if(chartCondition != false) {
			select_Condition = "p.dia, p.mes, p.ano, ";
			sum_Condition = "sum(p.valorTotal)";
			groupBy_Condition = "p.dia, p.mes, p.ano ";
			orderBy_Condition = "p.dia asc, p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.dia BETWEEN :diaInicio AND :diaFim "
				+ "AND p.mes BETWEEN :mesInicio AND :mesFim "
				+ "AND p.ano BETWEEN :anoInicio AND :anoFim "
				+ condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("diaInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.DAY_OF_MONTH))))
				.setParameter("diaFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.DAY_OF_MONTH))))
				.setParameter("mesInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.MONTH) + 1)))
				.setParameter("mesFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.MONTH) + 1)))
				.setParameter("anoInicio", Long.parseLong(String.valueOf(calendarStart.get(Calendar.YEAR))))
				.setParameter("anoFim", Long.parseLong(String.valueOf(calendarStop.get(Calendar.YEAR))));
		
		if(categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}
		
		if(produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		List<Object[]> result = q.getResultList();
		
		if(chartCondition != false) {
			for (Object[] object : result) {
				if((long)object[0] < 10) {
					object[0] = "0" + object[0];
				}
				
				if((long)object[1] < 10) {
					object[1] = "0" + object[1];
				}
			}
		} 
		
		return result;
	}

}