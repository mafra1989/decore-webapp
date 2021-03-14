package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Cliente;
import com.webapp.repository.filter.ClienteFilter;
import com.webapp.util.jpa.Transacional;

public class Clientes implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Cliente porId(Long id) {
		return this.manager.find(Cliente.class, id);
	}
	
	@Transacional
	public Cliente save(Cliente cliente) {
		return this.manager.merge(cliente);
	}

	@Transacional
	public void remove(Cliente cliente) {
		Cliente clienteTemp = new Cliente();
		clienteTemp = this.manager.merge(cliente);

		this.manager.remove(clienteTemp);
	}
	
	public List<Cliente> todos() {
		return this.manager.createQuery("from Cliente order by nome", Cliente.class).getResultList();
	}
	
	public Cliente porNome(String nome) {
		return this.manager.createQuery("from Cliente i where i.nome = :nome", Cliente.class)
				.setParameter("nome", nome).getSingleResult();
	}
	
	public List<Cliente> filtrados(ClienteFilter filter) {
		return this.manager.createQuery("from Cliente i where upper(i.nome) like :nome order by nome", Cliente.class)
				.setParameter("nome", "%" + filter.getNome().toUpperCase() + "%").getResultList();
	}
	
	public List<Cliente> filtrados_(ClienteFilter filter) {
		return this.manager.createQuery("from Cliente i where upper(i.nome) like :nome or i.contato like :contato order by nome", Cliente.class)
				.setParameter("nome", "%" + filter.getNome().toUpperCase() + "%")
				.setParameter("contato", "%" + filter.getNome().toUpperCase() + "%").getResultList();
	}
	
}