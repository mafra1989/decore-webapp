package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Anexo;
import com.webapp.model.Cliente;
import com.webapp.util.jpa.Transacional;

public class Anexos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Anexo porId(Long id) {
		return this.manager.find(Anexo.class, id);
	}

	@Transacional
	public Anexo save(Anexo anexo) {
		return this.manager.merge(anexo);
	}

	@Transacional
	public void remove(Anexo anexo) {
		Anexo anexoTemp = new Anexo();
		anexoTemp = this.manager.merge(anexo);

		this.manager.remove(anexoTemp);
	}

	public List<Anexo> todos(Cliente cliente) {
		return manager.createQuery(
				"from Anexo a join fetch a.cliente c where c.id = :id order by c.id desc",
				Anexo.class).setParameter("id", cliente.getId()).getResultList();
	}

}