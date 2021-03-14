package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webapp.model.Empresa;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.util.jpa.Transacional;

public class ItensDevolucoes implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ItemDevolucao porId(Long id) {
		return this.manager.find(ItemDevolucao.class, id);
	}

	@Transacional
	public ItemDevolucao save(ItemDevolucao itemDevolucao) {
		return this.manager.merge(itemDevolucao);
	}

	@Transacional
	public void remove(ItemDevolucao itemDevolucao) {
		ItemDevolucao itemDevolucaoTemp = new ItemDevolucao();
		itemDevolucaoTemp = this.manager.merge(itemDevolucao);

		this.manager.remove(itemDevolucaoTemp);
	}

	public List<ItemDevolucao> todos(Empresa empresa) {
		return this.manager.createQuery("from ItemDevolucao i where i.devolucao.empresa.id = :empresa order by i.id", ItemDevolucao.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public List<ItemDevolucao> porVenda(Venda venda) {
		return this.manager
				.createQuery("from ItemDevolucao e join fetch e.venda c where c.id = :id order by e.id asc", ItemDevolucao.class)
				.setParameter("id",venda.getId()).getResultList();
	}
	
	public Object[] saldoParaTroca(Usuario usuario, Empresa empresa) {
		
		String jpql = "SELECT sum(i.quantidade), sum(i.valorTotal) FROM ItemDevolucao i WHERE i.devolucao.tipo = 'T' and i.devolucao.status = 'N'"
				+ " AND i.devolucao.usuario.id = :id AND i.devolucao.empresa.id = :empresa";
		Query q = manager.createQuery(jpql).setParameter("id", usuario.getId()).setParameter("empresa", empresa.getId());
		
		Object[] result = (Object[]) q.getSingleResult();
		
		return result;
	}
	
	public List<ItemDevolucao> listaDevolucao() {
		return this.manager
				.createQuery("from ItemDevolucao i where i.devolucao.tipo = 'T' and i.devolucao.status = 'N' order by i.id asc", ItemDevolucao.class)
				.getResultList();
	}
}