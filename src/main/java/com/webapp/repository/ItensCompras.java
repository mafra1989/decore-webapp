package com.webapp.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
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
	
	
	public List<ItemCompra> porCompra(Compra compra) {
		return this.manager
				.createQuery("from ItemCompra e join fetch e.compra c where c.id = :id order by e.id asc", ItemCompra.class)
				.setParameter("id", compra.getId()).getResultList();
	}
	
	public Number saldoPorProduto(Produto produto) { 
		
		Number count = (Number) this.manager
				.createQuery("select sum(e.quantidadeDisponivel) from ItemCompra e where e.produto.id = :id and e.quantidadeDisponivel > 0")
				.setParameter("id", produto.getId()).getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}
	
	public List<ItemCompra> porProduto(Produto produto) { 
		return this.manager //and e.quantidadeDisponivel > 0
				.createQuery("from ItemCompra e join fetch e.compra c where e.produto.id = :id order by e.compra.dataCompra asc", ItemCompra.class)
				.setParameter("id", produto.getId()).getResultList();
	}
	
	public List<ItemCompra> porProduto(Produto produto, boolean ajuste) { 
		return this.manager
				.createQuery("from ItemCompra e join fetch e.compra c where c.ajuste = :ajuste AND e.produto.id = :id order by e.compra.dataCompra asc", ItemCompra.class)
				.setParameter("id", produto.getId())
				.setParameter("ajuste", ajuste).getResultList();
	}
	
	
	public ItemCompra porCompra(Compra compra, Produto produto) {
		
		try {
			return this.manager
				.createQuery("from ItemCompra e join fetch e.compra c where e.produto.id = :idProduto and c.id = :idCompra", ItemCompra.class)
				.setParameter("idProduto", produto.getId()).setParameter("idCompra", compra.getId()).getSingleResult();
		} catch(NoResultException e) {
			
		}
		
		return null;
	}

	public List<ItemCompra> porCategoria(Compra compra, String[] categorias) {
	
		return this.manager
				.createQuery("from ItemCompra e join fetch e.compra c where e.produto.categoriaProduto.nome in (:categorias) and c.id = :idCompra", ItemCompra.class)
				.setParameter("categorias", Arrays.asList(categorias)).setParameter("idCompra", compra.getId()).getResultList();
	}
	
	
	public ItemCompra porProdutoDisponivel(Produto produto) {
		
		try {
			return this.manager
				.createQuery("from ItemCompra e join fetch e.compra c where e.produto.id = :id and e.quantidadeDisponivel > 0 order by c.dataCompra asc", ItemCompra.class)
				.setParameter("id", produto.getId()).setMaxResults(1).getSingleResult();
		} catch(NoResultException e) {
			
		}
		
		return null;
	}
	
	
	public Number aVender(Produto produto) {
		Number count = (Number) this.manager
				.createQuery("SELECT sum(e.valorUnitario * e.quantidadeDisponivel) from ItemCompra e where e.quantidadeDisponivel > 0 and e.produto.id = :id")
				.setParameter("id", produto.getId()).getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}


	public List<Compra> todas() {
		return this.manager.createQuery("from Compra order by id", Compra.class).getResultList();
	}

	public List<Compra> porComprador(Usuario usuario) {
		return this.manager
				.createQuery("from Compra e where e.usuario.nome = :nome order by id", Compra.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}

}