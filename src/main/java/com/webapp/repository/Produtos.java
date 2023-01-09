package com.webapp.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Empresa;
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

	public Produto porCodigo(String codigo, Empresa empresa) {
		try {
			return this.manager
					.createQuery("from Produto e where e.codigo = :codigo AND e.categoriaProduto.empresa.id = :empresa",
							Produto.class)
					.setParameter("codigo", codigo).setParameter("empresa", empresa.getId()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Produto porCodigoDeBarras(String codigoDeBarras, Empresa empresa) {
		try {
			return this.manager.createQuery(
					"from Produto e where e.codigoDeBarras = :codigoDeBarras AND e.categoriaProduto.empresa.id = :empresa",
					Produto.class).setParameter("codigoDeBarras", codigoDeBarras)
					.setParameter("empresa", empresa.getId()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Produto porCodigoDeBarras_(String codigoDeBarras, Empresa empresa) {
		try {
			return this.manager.createQuery(
					"from Produto e where e.codigoDeBarras = :codigoDeBarras AND e.categoriaProduto.empresa.id = :empresa AND e.estoque = 'Y'",
					Produto.class).setParameter("codigoDeBarras", codigoDeBarras)
					.setParameter("empresa", empresa.getId()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Produto porCodigoCadastrado(Produto produto) {
		try {
			return this.manager.createQuery(
					"from Produto e where e.codigo = :codigo and e.id != :id and e.categoriaProduto.empresa = :empresa",
					Produto.class).setParameter("codigo", produto.getCodigo()).setParameter("id", produto.getId())
					.setParameter("empresa", produto.getCategoriaProduto().getEmpresa()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Produto porCodigoDeBarrasCadastrado(Produto produto) {
		try {
			return this.manager.createQuery(
					"from Produto e where e.codigoDeBarras = :codigoDeBarras and e.id != :id and e.categoriaProduto.empresa = :empresa",
					Produto.class).setParameter("codigoDeBarras", produto.getCodigoDeBarras())
					.setParameter("id", produto.getId())
					.setParameter("empresa", produto.getCategoriaProduto().getEmpresa()).getSingleResult();
		} catch (NoResultException e) {
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

	public List<Produto> todos(Empresa empresa) {
		return this.manager.createQuery("from Produto p where p.categoriaProduto.empresa.id = :empresa order by p.id",
				Produto.class).setParameter("empresa", empresa.getId()).getResultList();
	}

	public List<Produto> porCategoria_(CategoriaProduto categoriaProduto) {
		return this.manager.createQuery(
				"from Produto e where e.categoriaProduto.nome = :nome order by e.categoriaProduto.nome asc",
				Produto.class).setParameter("nome", categoriaProduto.getNome()).getResultList();
	}

	public List<Produto> porCategoria(CategoriaProduto categoriaProduto, Produto produto) {
		return this.manager.createQuery(
				"from Produto e where e.categoriaProduto.nome = :nome AND e.id != :id AND e.precoDeVenda > 0 AND e.numeracao = :tamanho order by e.categoriaProduto.nome asc",
				Produto.class).setParameter("nome", categoriaProduto.getNome()).setParameter("id", produto.getId())
				.setParameter("tamanho", produto.getNumeracao()).getResultList();
	}

	public List<Produto> porCategoria(String[] categorias) {
		return this.manager.createQuery(
				"from Produto e where e.categoriaProduto.nome in (:categorias) order by e.categoriaProduto.nome asc",
				Produto.class).setParameter("categorias", Arrays.asList(categorias)).getResultList();
	}

	public List<Produto> filtrados(ProdutoFilter filter) {

		TypedQuery<Produto> typedQuery;

		String condition = "";

		if (StringUtils.isNotBlank(filter.getDescricao())) {

			if (filter.getCategoriaProduto() != null) {
				condition = "e.categoriaProduto.id = :id AND";
			}

			typedQuery = manager.createQuery("select e from Produto e where " + condition
					+ " (upper(e.nome) like :nome or upper(e.descricao) like :descricao or upper(e.codigo) = :codigo) AND e.categoriaProduto.empresa = :empresa order by e.codigo",
					Produto.class).setParameter("nome", "%" + filter.getDescricao().toUpperCase() + "%")
					.setParameter("descricao", "%" + filter.getDescricao().toUpperCase() + "%")
					.setParameter("codigo", filter.getDescricao().toUpperCase())
					.setParameter("empresa", filter.getEmpresa());

			if (filter.getCategoriaProduto() != null) {
				typedQuery.setParameter("id", filter.getCategoriaProduto().getId());
			}

		} else {

			if (filter.getCategoriaProduto() != null) {
				condition = "AND upper(e.categoriaProduto.nome) = :nome ";
			}

			String tamanho = "";
			String unidade = "";

			if (StringUtils.isNotBlank(filter.getTamanho())) {
				tamanho = "AND upper(e.numeracao) = :tamanho ";
			}

			if (StringUtils.isNotBlank(filter.getUnidade())) {
				unidade = "AND upper(e.unidadeMedida) = :unidade ";
			}

			typedQuery = manager.createQuery("select e from Produto e Where e.categoriaProduto.empresa = :empresa "
					+ condition + tamanho + unidade, Produto.class);
			typedQuery.setParameter("empresa", filter.getEmpresa());

			if (filter.getCategoriaProduto() != null) {
				typedQuery.setParameter("nome", filter.getCategoriaProduto().getNome().toUpperCase());
			}

			if (StringUtils.isNotBlank(filter.getTamanho())) {
				typedQuery.setParameter("tamanho", filter.getTamanho().toUpperCase());
			}

			if (StringUtils.isNotBlank(filter.getUnidade())) {
				typedQuery.setParameter("unidade", filter.getUnidade().toUpperCase());
			}
		}

		return typedQuery.getResultList();

	}

	public List<Produto> filtrados_(ProdutoFilter filter) {

		TypedQuery<Produto> typedQuery;

		String condition = "";

		if (StringUtils.isNotBlank(filter.getDescricao())) {

			if (filter.getCategoriaProduto() != null) {
				condition = "e.categoriaProduto.id = :id AND";
			}

			typedQuery = manager.createQuery("select e from Produto e where e.estoque = 'Y' AND " + condition
					+ " (upper(e.nome) like :nome or upper(e.descricao) like :descricao or upper(e.codigo) = :codigo) AND e.categoriaProduto.empresa = :empresa order by e.codigo",
					Produto.class).setParameter("nome", "%" + filter.getDescricao().toUpperCase() + "%")
					.setParameter("descricao", "%" + filter.getDescricao().toUpperCase() + "%")
					.setParameter("codigo", filter.getDescricao().toUpperCase())
					.setParameter("empresa", filter.getEmpresa());

		} else {

			if (filter.getCategoriaProduto() != null) {
				condition = "AND upper(e.categoriaProduto.nome) = :nome";
			}

			typedQuery = manager.createQuery(
					"select e from Produto e Where e.estoque = 'Y' AND e.categoriaProduto.empresa = :empresa "
							+ condition,
					Produto.class);
			typedQuery.setParameter("empresa", filter.getEmpresa());
		}

		if (filter.getCategoriaProduto() != null) {
			typedQuery.setParameter("nome", filter.getCategoriaProduto().getNome().toUpperCase());
		}

		return typedQuery.getResultList();

	}

	public List<Produto> produtosEmDestaque(ProdutoFilter filter) {

		TypedQuery<Produto> typedQuery = manager.createQuery(
				"select e from Produto e where e.destaque = 'Y' AND e.categoriaProduto.empresa = :empresa order by e.codigo",
				Produto.class).setParameter("empresa", filter.getEmpresa());

		return typedQuery.getResultList();

	}

	@SuppressWarnings("unchecked")
	public List<String> tamanhos(Empresa empresa, CategoriaProduto categoriaProduto) {

		List<String> tamanhos = new ArrayList<String>();

		String condition = "";

		if (categoriaProduto != null) {
			condition = "AND upper(p.categoriaProduto.nome) = :nome";
		}

		String jpql = "SELECT p.numeracao from Produto p where p.categoriaProduto.empresa.id = :empresa " + condition
				+ " group by p.numeracao";

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if (categoriaProduto != null) {
			q.setParameter("nome", categoriaProduto.getNome().toUpperCase());
		}

		for (String tamanho : (List<String>) q.getResultList()) {
			if (StringUtils.isNotBlank(tamanho)) {
				tamanhos.add(tamanho);
			}
		}

		return tamanhos;
	}

	@SuppressWarnings("unchecked")
	public List<String> unidades(Empresa empresa, CategoriaProduto categoriaProduto) {

		List<String> unidades = new ArrayList<String>();

		String condition = "";

		if (categoriaProduto != null) {
			condition = "AND upper(p.categoriaProduto.nome) = :nome";
		}

		String jpql = "SELECT p.unidadeMedida from Produto p where p.categoriaProduto.empresa.id = :empresa "
				+ condition + " group by p.unidadeMedida";

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if (categoriaProduto != null) {
			q.setParameter("nome", categoriaProduto.getNome().toUpperCase());
		}

		for (String unidade : (List<String>) q.getResultList()) {
			if (StringUtils.isNotBlank(unidade)) {
				unidades.add(unidade);
			}
		}

		return unidades;
	}

	public Number totalAVender(Empresa empresa) {

		Number count = 0;

		try {
			count = (Number) this.manager.createQuery(
					"SELECT sum(e.quantidadeDisponivel * e.produto.precoDeVenda) from ItemCompra e where e.produto.estoque = 'Y' AND e.compra.empresa.id = :empresa AND e.quantidadeDisponivel > 0")
					.setParameter("empresa", empresa.getId()).getSingleResult();

		} catch (NoResultException e) {

		}

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number saldoEstorno(Empresa empresa) {

		Number count = 0;

		try {
			count = (Number) this.manager
					.createQuery("SELECT sum(e.estorno) from Produto e where e.categoriaProduto.empresa.id = :empresa")
					.setParameter("empresa", empresa.getId()).getSingleResult();

		} catch (NoResultException e) {

		}

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalCustoMedio(Empresa empresa) {

		Number count = 0;

		try {
			count = (Number) this.manager.createQuery(//e.produto.custoMedioUnitario
					"SELECT sum(e.quantidadeDisponivel * e.valorUnitario) from ItemCompra e where e.produto.estoque = 'Y' AND e.compra.empresa.id = :empresa AND e.quantidadeDisponivel > 0")
					.setParameter("empresa", empresa.getId()).getSingleResult();

		} catch (NoResultException e) {

		}

		if (count == null) {
			count = 0;
		}

		return count;
	}

}
