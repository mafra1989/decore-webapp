package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.ItemPedido;
import com.webapp.model.Pedido;
import com.webapp.util.jpa.Transacional;

public class ItensPedidos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ItemPedido porId(Long id) {
		return this.manager.find(ItemPedido.class, id);
	}

	@Transacional
	public ItemPedido save(ItemPedido itemPedido) {
		return this.manager.merge(itemPedido);
	}

	@Transacional
	public void remove(ItemPedido itemPedido) {
		ItemPedido itemPedidoTemp = new ItemPedido();
		itemPedidoTemp = this.manager.merge(itemPedido);

		this.manager.remove(itemPedidoTemp);
	}

	public List<ItemPedido> porPedido(Pedido pedido) {
		return this.manager.createQuery("from ItemPedido e join fetch e.pedido c where c.id = :id order by e.id asc",
				ItemPedido.class).setParameter("id", pedido.getId()).getResultList();
	}

}