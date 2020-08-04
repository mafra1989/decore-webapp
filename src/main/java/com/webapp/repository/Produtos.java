package com.webapp.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaProduto;
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
	
	public Produto porCodigo(String codigo, String empresa) {
		try {
			return this.manager
					.createQuery("from Produto e where e.codigo = :codigo AND e.categoriaProduto.empresa = :empresa", Produto.class)
					.setParameter("codigo", codigo)
					.setParameter("empresa", empresa).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	public Produto porCodigoDeBarras(String codigoDeBarras, String empresa) {
		try {
			return this.manager
					.createQuery("from Produto e where e.codigoDeBarras = :codigoDeBarras AND e.categoriaProduto.empresa = :empresa", Produto.class)
					.setParameter("codigoDeBarras", codigoDeBarras)
					.setParameter("empresa", empresa).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	public Produto porCodigoCadastrado(Produto produto) {
		try {
			return this.manager
					.createQuery("from Produto e where e.codigo = :codigo and e.id != :id and e.categoriaProduto.empresa = :empresa", Produto.class)
					.setParameter("codigo", produto.getCodigo())
					.setParameter("id", produto.getId())
					.setParameter("empresa", produto.getCategoriaProduto().getEmpresa()).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	
	public Produto porCodigoDeBarrasCadastrado(Produto produto) {
		try {
			return this.manager
					.createQuery("from Produto e where e.codigoDeBarras = :codigoDeBarras and e.id != :id and e.categoriaProduto.empresa = :empresa", Produto.class)
					.setParameter("codigoDeBarras", produto.getCodigoDeBarras())
					.setParameter("id", produto.getId())
					.setParameter("empresa", produto.getCategoriaProduto().getEmpresa()).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
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
	

	public List<Produto> porCategoria_(CategoriaProduto categoriaProduto) {
		return this.manager
				.createQuery("from Produto e where e.categoriaProduto.nome = :nome order by e.categoriaProduto.nome asc", Produto.class)
				.setParameter("nome", categoriaProduto.getNome()).getResultList();
	}
	
	
	public List<Produto> porCategoria(String[] categorias) {
		return this.manager
				.createQuery("from Produto e where e.categoriaProduto.nome in (:categorias) order by e.categoriaProduto.nome asc", Produto.class)
				.setParameter("categorias", Arrays.asList(categorias)).getResultList();
	}


	public List<Produto> filtrados(ProdutoFilter filter) {

		TypedQuery<Produto> typedQuery;
		
		String condition = "";
		
		if (StringUtils.isNotBlank(filter.getDescricao())) {
			
			if(filter.getCategoriaProduto() != null) {
				condition = "e.categoriaProduto.id = :id AND";
			}

			typedQuery = manager.createQuery(
					"select e from Produto e where " + condition + " (upper(e.nome) like :nome or upper(e.descricao) like :descricao or upper(e.codigo) = :codigo) AND e.categoriaProduto.empresa = :empresa order by e.codigo",
					Produto.class)
					.setParameter("nome", "%" + filter.getDescricao().toUpperCase() + "%")
					.setParameter("descricao", "%" + filter.getDescricao().toUpperCase() + "%")
					.setParameter("codigo", filter.getDescricao().toUpperCase())
					.setParameter("empresa", filter.getEmpresa());
			
		} else {
			
			if(filter.getCategoriaProduto() != null) {
				condition = "AND upper(e.categoriaProduto.nome) = :nome";
			}
			
			typedQuery = manager.createQuery("select e from Produto e Where e.categoriaProduto.empresa = :empresa " + condition, Produto.class);
			typedQuery.setParameter("empresa", filter.getEmpresa());
		}
		
		if(filter.getCategoriaProduto() != null) {
			typedQuery.setParameter("nome", filter.getCategoriaProduto().getNome().toUpperCase());
		}

		return typedQuery.getResultList();

	}
	
	
	public List<Produto> produtosEmDestaque(ProdutoFilter filter) {
		
		TypedQuery<Produto> typedQuery = manager.createQuery(
				"select e from Produto e where e.destaque = 'Y' AND e.categoriaProduto.empresa = :empresa order by e.codigo",
				Produto.class)
				.setParameter("empresa", filter.getEmpresa());

		return typedQuery.getResultList();

	}
	
	
	public Number totalAVender(String empresa) {

                Number count = 0;
		
                try {
		    count = (Number) this.manager
				.createQuery("SELECT sum((e.valorUnitario * e.quantidadeDisponivel) * (1 + (e.produto.margemLucro/100))) from ItemCompra e where e.compra.empresa = :empresa AND e.quantidadeDisponivel > 0")
				.setParameter("empresa", empresa).getSingleResult();

		} catch(NoResultException e) {
			
		}
                
		if(count == null) {
			count = 0;
		}
		
		return count;
	}

}
