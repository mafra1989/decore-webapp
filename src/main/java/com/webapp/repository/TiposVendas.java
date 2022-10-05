package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Empresa;
import com.webapp.model.TipoVenda;
import com.webapp.repository.filter.TipoVendaFilter;
import com.webapp.util.jpa.Transacional;

public class TiposVendas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public TipoVenda porId(Long id) {
		return this.manager.find(TipoVenda.class, id);
	}

	@Transacional
	public TipoVenda save(TipoVenda tipoVenda) {
		return this.manager.merge(tipoVenda);
	}

	@Transacional
	public void remove(TipoVenda tipoVenda) {
		TipoVenda tipoVendaTemp = new TipoVenda();
		tipoVendaTemp = this.manager.merge(tipoVenda);

		this.manager.remove(tipoVendaTemp);
	}

	public List<TipoVenda> todos(Empresa empresa) {
		return this.manager.createQuery("from TipoVenda t where t.empresa.id = :id order by t.descricao", TipoVenda.class)
				.setParameter("id", empresa.getId()).getResultList();
	}
	
	public List<TipoVenda> filtrados(TipoVendaFilter filter, Empresa empresa) {
		return this.manager.createQuery("from TipoVenda i where i.empresa.id = :id and lower(i.descricao) like lower(:descricao) order by descricao", TipoVenda.class)
				.setParameter("descricao", "%" + filter.getDescricao() + "%")
				.setParameter("id", empresa.getId()).getResultList();
	}
	
	public TipoVenda porDescricao(String descricao, Empresa empresa) {
		TipoVenda tipoVenda = null;
		try {
			tipoVenda = this.manager.createQuery("from TipoVenda i where i.empresa.id = :id and lower(i.descricao) like lower(:descricao) order by descricao", TipoVenda.class)
					.setParameter("descricao", "%" + descricao + "%")
					.setParameter("id", empresa.getId())
					.getSingleResult();
			return tipoVenda;
			
		} catch (NoResultException e) {
			return null;
		}
	}
	
}