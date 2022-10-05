package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Bairro;
import com.webapp.model.Empresa;
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
	
	public List<Bairro> todos(Empresa empresa) {
		return this.manager.createQuery("from Bairro b where b.empresa.id = :id order by nome", Bairro.class)
				.setParameter("id", empresa.getId()).getResultList();
	}
	
	public Bairro porNome(String nome, Empresa empresa) {
		Bairro bairro = null;
		
		try {
			bairro = this.manager.createQuery("from Bairro i where i.empresa.id = :id and i.nome = :nome", Bairro.class)
					.setParameter("nome", nome)
					.setParameter("id", empresa.getId())
					.getSingleResult();
			
		} catch (NoResultException e) {
			System.out.println("nenhum bairro encontrado com o nome informado");
		}
		
		return bairro;
	}
	
	public List<Bairro> filtrados(BairroFilter filter, Empresa empresa) {
		return this.manager.createQuery("from Bairro i where i.empresa.id = :id and upper(i.nome) like :nome order by nome", Bairro.class)
				.setParameter("nome", "%" + filter.getNome().toUpperCase() + "%")
				.setParameter("id", empresa.getId()).getResultList();
	}
	
}