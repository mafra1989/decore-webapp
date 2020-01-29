package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

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
	
	public List<ItemVenda> porVenda(Venda venda) {
		return this.manager
				.createQuery("from ItemVenda e join fetch e.venda c where c.id = :id order by e.id asc", ItemVenda.class)
				.setParameter("id", venda.getId()).getResultList();
	}
	
	public List<ItemVenda> porProduto(Produto produto) {
		return this.manager
				.createQuery("from ItemVenda e join fetch e.venda c where e.produto.id = :id order by e.venda.dataVenda desc", ItemVenda.class)
				.setParameter("id", produto.getId()).getResultList();
	}

}