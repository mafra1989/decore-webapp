package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Empresa;
import com.webapp.repository.filter.EmpresaFilter;
import com.webapp.util.jpa.Transacional;

public class Empresas implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	
	@Transacional
	public Empresa save(Empresa empresa) {
		return this.manager.merge(empresa);
	}

	@Transacional
	public void remove(Empresa empresa) {
		Empresa empresaTemp = new Empresa();
		empresaTemp = this.manager.merge(empresa);

		this.manager.remove(empresaTemp);
	}
	
	public Empresa porId(Long id) {
		return this.manager.find(Empresa.class, id);
	}
	
	public List<Empresa> todos() {
		return this.manager.createQuery("from Empresa order by nome", Empresa.class).getResultList();
	}
	
	public Empresa porNome(String nome) {
		return this.manager.createQuery("from Empresa i where i.nome = :nome", Empresa.class)
				.setParameter("nome", nome).getSingleResult();
	}
	
	public List<Empresa> filtrados(EmpresaFilter filter) {
		return this.manager.createQuery("from Empresa i where upper(i.nome) like :nome order by nome", Empresa.class)
				.setParameter("nome", "%" + filter.getNome().toUpperCase() + "%").getResultList();
	}
}