package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.Compra;
import com.webapp.model.Emprestimo;
import com.webapp.model.ItemCompra;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.filter.EmprestimoFilter;
import com.webapp.util.jpa.Transacional;

public class ItensCompras implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public ItemCompra porId(Long id) {
		return this.manager.find(ItemCompra.class, id);
	}

	@Transacional
	public ItemCompra save(ItemCompra itemCompra) {
		return this.manager.merge(itemCompra);
	}

	@Transacional
	public void remove(ItemCompra itemCompra) {
		ItemCompra itemCompraTemp = new ItemCompra();
		itemCompraTemp = this.manager.merge(itemCompra);

		this.manager.remove(itemCompraTemp);
	}
	
	public List<ItemCompra> porCompra(Produto produto) {
		return this.manager
				.createQuery("from ItemCompra e join fetch e.compra c where e.produto.id = :id order by e.compra.dataCompra desc", ItemCompra.class)
				.setParameter("id", produto.getId()).getResultList();
	}
	
	
	
	
	
	

	public List<Compra> todas() {
		return this.manager.createQuery("from Compra order by id", Compra.class).getResultList();
	}

	public List<Compra> porComprador(Usuario usuario) {
		return this.manager
				.createQuery("from Compra e where e.usuario.nome = :nome order by id", Compra.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}

	public List<Emprestimo> filtrados(EmprestimoFilter filter) {

		TypedQuery<Emprestimo> typedQuery;

		if (StringUtils.isNotBlank(filter.getNome())) {
			typedQuery = manager.createQuery(
					"select e from Emprestimo e join fetch e.cliente c where c.nome like :nome order by e.id",
					Emprestimo.class).setParameter("nome", "%" + filter.getNome() + "%");

		} else {
			typedQuery = manager.createQuery("select e from Emprestimo e", Emprestimo.class);
		}

		return typedQuery.getResultList();

	}

	public List<Emprestimo> porCliente(Long id) {

		TypedQuery<Emprestimo> typedQuery;

		typedQuery = manager
				.createQuery("select e from Emprestimo e join fetch e.cliente c where c.id = :id order by e.id",
						Emprestimo.class)
				.setParameter("id", id);

		return typedQuery.getResultList();

	}

}