package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Emprestimo;
import com.webapp.model.Produto;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jpa.Transacional;

public class Produtos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Produto porId(Long id) {
		return this.manager.find(Produto.class, id);
	}

	@Transacional
	public Produto save(Produto produto) {
		return this.manager.merge(produto);
	}

	@Transacional
	public void remove(Produto produto) {
		Produto produtoTemp = new Produto();
		produtoTemp = this.manager.merge(produto);

		this.manager.remove(produtoTemp);
	}
	

	public List<Produto> porCategoria(CategoriaProduto categoriaProduto) {
		return this.manager
				.createQuery("from Produto e where e.categoriaProduto.nome = :nome order by e.categoriaProduto.nome asc", Produto.class)
				.setParameter("nome", categoriaProduto.getNome()).getResultList();
	}


	public List<Produto> filtrados(ProdutoFilter filter) {

		TypedQuery<Produto> typedQuery;
		
		String condition = "";
		
		if (StringUtils.isNotBlank(filter.getDescricao())) {
			
			if(filter.getCategoriaProduto() != null) {
				condition = "AND e.categoriaProduto.id = :id";
			}
			
			typedQuery = manager.createQuery(
					"select e from Produto e where " + condition + " e.descricao like :descricao or e.codigo = :codigo order by e.codigo",
					Produto.class).setParameter("descricao", "%" + filter.getDescricao() + "%").setParameter("codigo", filter.getDescricao());
			
		} else {
			
			if(filter.getCategoriaProduto() != null) {
				condition = "WHERE e.categoriaProduto.id = :id";
			}
			
			typedQuery = manager.createQuery("select e from Produto e " + condition, Produto.class);
		}
		
		if(filter.getCategoriaProduto() != null) {
			typedQuery.setParameter("id", filter.getCategoriaProduto().getId());
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
	
	
	
	public Number totalAVender() {		
		String jpql = "SELECT sum(i.precoVenda * i.quantidadeAtual) FROM Produto i";
		Query q = manager.createQuery(jpql);
		Number count = (Number) q.getSingleResult();
		
		if(count == null) {
			count = 0;
		}
		
		return count;
	}

}