package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

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

	public List<Cliente> filtrados(ClienteFilter filter) {
		return this.manager.createQuery("from Cliente c where c.nome like :nome order by nome", Cliente.class)
				.setParameter("nome", "%" + filter.getNome() + "%").getResultList();
	}
	
	public Number totalClientes() {		
		String jpql = "SELECT count(c) FROM Cliente c";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		return count;
	}
}