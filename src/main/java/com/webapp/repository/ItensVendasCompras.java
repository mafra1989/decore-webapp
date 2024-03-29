package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Produto;
import com.webapp.util.jpa.Transacional;

public class ItensVendasCompras implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ItemVendaCompra porId(Long id) {
		return this.manager.find(ItemVendaCompra.class, id);
	}

	@Transacional
	public ItemVendaCompra save(ItemVendaCompra itemVendaCompra) {
		return this.manager.merge(itemVendaCompra);
	}

	@Transacional
	public void remove(ItemVendaCompra itemVendaCompra) {
		ItemVendaCompra itemVendaCompraTemp = new ItemVendaCompra();
		itemVendaCompraTemp = this.manager.merge(itemVendaCompra);

		this.manager.remove(itemVendaCompraTemp);
	}
	
	
	public List<ItemVendaCompra> porItemVenda(ItemVenda itemVenda) {
		return this.manager
				.createQuery("from ItemVendaCompra e join fetch e.itemVenda c where c.id = :id order by e.id asc", ItemVendaCompra.class)
				.setParameter("id", itemVenda.getId()).getResultList();
	} 
	
	public List<ItemVendaCompra> porItemVenda_(ItemVenda itemVenda) { // c.venda.empresa.id = :empresa and 
		return this.manager
				.createQuery("from ItemVendaCompra e join fetch e.compra c where e.itemVenda.id = :id and c.empresa.id = :empresa order by e.id asc", ItemVendaCompra.class)
				.setParameter("id", itemVenda.getId())
				.setParameter("empresa", itemVenda.getVenda().getEmpresa().getId())
				.getResultList();
	}
	
	public List<ItemVendaCompra> porCompra(Compra compra, Produto produto) {
		return this.manager
				.createQuery("from ItemVendaCompra e join fetch e.compra c where c.id = :id AND e.itemVenda.produto.id = :produtoID order by e.id asc", ItemVendaCompra.class)
				.setParameter("id", compra.getId()).setParameter("produtoID", produto.getId()).getResultList();
	}
	
	public List<ItemVendaCompra> porCompra_(Compra compra) {
		return this.manager
				.createQuery("from ItemVendaCompra e join fetch e.compra c where c.id = :id order by e.id asc", ItemVendaCompra.class)
				.setParameter("id", compra.getId()).getResultList();
	}
	
	public List<ItemVendaCompra> porItemCompra(ItemCompra itemCompra) {
		return this.manager
				.createQuery("from ItemVendaCompra e join fetch e.itemCompra c where c.id = :id order by e.id asc", ItemVendaCompra.class)
				.setParameter("id", itemCompra.getId()).getResultList();
	} 

}