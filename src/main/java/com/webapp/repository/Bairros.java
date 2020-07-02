package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Bairro;
import com.webapp.repository.filter.BairroFilter;
import com.webapp.util.jpa.Transacional;

public class Bairros implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Bairro porId(Long id) {
		return this.manager.find(Bairro.class, id);
	}
	
	@Transacional
	public Bairro save(Bairro bairro) {
		return this.manager.merge(bairro);
	}

	@Transacional
	public void remove(Bairro bairro) {
		Bairro bairroTemp = new Bairro();
		bairroTemp = this.manager.merge(bairro);

		this.manager.remove(bairroTemp);
	}
	
	public List<Bairro> todos() {
		return this.manager.createQuery("from Bairro order by nome", Bairro.class).getResultList();
	}
	
	public Bairro porNome(String nome) {
		return this.manager.createQuery("from Bairro i where i.nome = :nome", Bairro.class)
				.setParameter("nome", nome).getSingleResult();
	}
	
	public List<Bairro> filtrados(BairroFilter filter) {
		return this.manager.createQuery("from Bairro i where upper(i.nome) like :nome order by nome", Bairro.class)
				.setParameter("nome", "%" + filter.getNome().toUpperCase() + "%").getResultList();
	}
	
}