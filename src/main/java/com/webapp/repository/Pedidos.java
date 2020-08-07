package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Pedido;
import com.webapp.util.jpa.Transacional;

public class Pedidos implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Pedido porId(Long id) {
		return this.manager.find(Pedido.class, id);
	}
	
	@Transacional
	public Pedido save(Pedido pedido) {
		return this.manager.merge(pedido);
	}

	@Transacional
	public void remove(Pedido pedido) {
		Pedido pedidoTemp = new Pedido();
		pedidoTemp = this.manager.merge(pedido);

		this.manager.remove(pedidoTemp);
	}
	
	public List<Pedido> todos() {
		return this.manager.createQuery("from Pedido order by nome", Pedido.class).getResultList();
	}
	
}