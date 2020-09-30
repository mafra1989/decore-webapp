package com.webapp.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.Venda;
import com.webapp.util.jpa.Transacional;

public class ItensVendas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ItemVenda porId(Long id) {
		return this.manager.find(ItemVenda.class, id);
	}

	@Transacional
	public ItemVenda save(ItemVenda itemVenda) {
		return this.manager.merge(itemVenda);
	}

	@Transacional
	public void remove(ItemVenda itemVenda) {
		ItemVenda itemVendaTemp = new ItemVenda();
		itemVendaTemp = this.manager.merge(itemVenda);

		this.manager.remove(itemVendaTemp);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> porVenda(Venda venda, Produto produto) {
		
		List<Object[]> objects = new ArrayList<Object[]>();
		
		try {
			return this.manager
					.createQuery("SELECT sum(e.quantidade), sum(e.quantidade * e.valorUnitario), sum(e.total) from ItemVenda e where e.venda.id = :id and e.produto.id = :produto group by e.id order by e.id desc")
					.setParameter("id", venda.getId())
					.setParameter("produto", produto.getId()).getResultList();
			
		} catch(NoResultException e) {
			
		}

		return objects;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<ItemVenda> porVendaProduto(Venda venda, Produto produto) {
		
		List<ItemVenda> objects = new ArrayList<ItemVenda>();
		
		try {
			return this.manager
					.createQuery("SELECT e from ItemVenda e where e.venda.id = :id and e.produto.id = :produto order by e.id desc")
					.setParameter("id", venda.getId())
					.setParameter("produto", produto.getId()).getResultList();
			
		} catch(NoResultException e) {
			
		}

		return objects;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<ItemVenda> porProduto(Venda venda, Produto produto) {
		
		return this.manager
				.createQuery("SELECT e from ItemVenda e where e.venda.id = :id and e.produto.id = :produto order by e.id desc")
				.setParameter("id", venda.getId())
				.setParameter("produto", produto.getId()).getResultList();
			
	}
	
	

	public List<ItemVenda> porVenda(Venda venda) {
		return this.manager.createQuery("from ItemVenda e join fetch e.venda c where c.id = :id order by e.id asc",
				ItemVenda.class).setParameter("id", venda.getId()).getResultList();
	}

	public List<ItemVenda> porCompra(Compra compra) {
		return this.manager.createQuery("from ItemVenda e join fetch e.compra c where c.id = :id order by e.id asc",
				ItemVenda.class).setParameter("id", compra.getId()).getResultList();
	}

	public Number vendasPorCompra(Compra compra, Produto produto) {
		
		String jpql = "SELECT sum(i.quantidade) FROM ItemVenda i WHERE i.compra.id = :idCompra and i.produto.id = :idProduto";
		Query q = manager.createQuery(jpql).setParameter("idCompra", compra.getId()).setParameter("idProduto", produto.getId());
		
		Number count = (Number) q.getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}

	public List<ItemVenda> porCompra(Compra compra, ItemCompra itemCompra) {
		return this.manager.createQuery(
				"from ItemVenda e join fetch e.compra c where c.id = :id and e.produto.id = :itemID order by e.id asc",
				ItemVenda.class).setParameter("id", compra.getId())
				.setParameter("itemID", itemCompra.getProduto().getId()).getResultList();
	}
	
	public List<ItemVenda> porProduto(Produto produto) {
		return this.manager.createQuery(
				"from ItemVenda e join fetch e.venda c where e.produto.id = :id order by e.venda.dataVenda desc",
				ItemVenda.class).setParameter("id", produto.getId()).getResultList();
	}

	public List<ItemVenda> porProduto(Produto produto, boolean ajuste) {
		
		String statusCondition = "";
		
		if(!ajuste) {
			//statusCondition = "c.status = 'Y' AND";
		}
		
		return this.manager.createQuery(
				"from ItemVenda e join fetch e.venda c where " + statusCondition + " c.ajuste = :ajuste AND e.produto.id = :id order by e.venda.dataVenda desc",
				ItemVenda.class).setParameter("id", produto.getId())
				.setParameter("ajuste", ajuste).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> maisVendidos(String ano, Produto produto, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		select_Condition = "p.ano, i.produto.id, ";
		sum_Condition = "sum(i.quantidade)";
		groupBy_Condition = "p.ano, i.produto.id";
		orderBy_Condition = "sum(i.quantidade) desc"; 
		
		// AND p.status = 'Y' 
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.ano = :anoInicio AND p.vendaPaga = 'Y' AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("anoInicio", Long.parseLong(ano));

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		System.out.println(jpql);

		
		List<Object[]> result = q.setMaxResults(10).getResultList();

		return result;
	}

}