package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.Caixa;
import com.webapp.model.Empresa;
import com.webapp.model.ItemCaixa;
import com.webapp.model.TipoOperacao;
import com.webapp.util.jpa.Transacional;

public class ItensCaixas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ItemCaixa porId(Long id) {
		return this.manager.find(ItemCaixa.class, id);
	}

	@Transacional
	public ItemCaixa save(ItemCaixa itemCaixa) {
		return this.manager.merge(itemCaixa);
	}

	@Transacional
	public void remove(ItemCaixa itemCaixa) {
		ItemCaixa itemCaixaTemp = new ItemCaixa();
		itemCaixaTemp = this.manager.merge(itemCaixa);

		this.manager.remove(itemCaixaTemp);
	}

	public List<ItemCaixa> todos(Empresa empresa) {
		return this.manager.createQuery("from ItemCaixa i where i.caixa.empresa.id = :empresa order by i.id", ItemCaixa.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public List<ItemCaixa> porCaixa(Caixa caixa) {
		return this.manager
				.createQuery("from ItemCaixa e join fetch e.caixa c where c.id = :id order by e.id asc", ItemCaixa.class)
				.setParameter("id",caixa.getId()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<ItemCaixa> porCodigoOperacao(Long codigoOperacao, TipoOperacao operacao, Empresa empresa) {

		String jpql = "SELECT i FROM ItemCaixa i WHERE i.caixa.empresa.id = :empresa AND i.operacao = :operacao AND i.codigoOperacao = :codigoOperacao";
		Query q = manager.createQuery(jpql).setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		return q.getResultList();
	}
	
}