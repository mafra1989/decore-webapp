package com.webapp.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Entrega;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.util.jpa.Transacional;

public class Vendas implements Serializable {

	private static final long serialVersionUID = 1L;
	

	@Inject
	private EntityManager manager;
	

	public Venda porId(Long id) {
		return this.manager.find(Venda.class, id);
	}

	@Transacional
	public Venda save(Venda venda) {
		return this.manager.merge(venda);
	}

	@Transacional
	public void remove(Venda venda) {
		Venda vendaTemp = new Venda();
		vendaTemp = this.manager.merge(venda);

		this.manager.remove(vendaTemp);
	}

	public List<Venda> todas() {
		return this.manager.createQuery("from Venda order by numeroVenda desc", Venda.class).getResultList();
	}

	public Venda ultimoNVenda(String empresa) {

		try {
			return this.manager.createQuery("from Venda e WHERE e.empresa = :empresa order by e.numeroVenda desc", Venda.class).setMaxResults(1)
					.setParameter("empresa", empresa).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	public Venda porNumeroVenda(Long numeroVenda) {
		try {
			return this.manager.createQuery("from Venda e where e.numeroVenda = :numeroVenda", Venda.class)
					.setParameter("numeroVenda", numeroVenda).getSingleResult();
		} catch (NoResultException e) {

		}

		return null;
	}

	public List<Venda> porVendedor(Usuario usuario) {
		return this.manager.createQuery("from Venda e where e.usuario.nome = :nome order by id", Venda.class)
				.setParameter("nome", usuario.getNome()).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Venda> vendasFiltradas(Long numeroVenda, Date dateStart, Date dateStop, boolean vendasNormais,
			StatusPedido[] statusPedido, Usuario usuario, String empresa) {

		List<Venda> vendas = new ArrayList<>();

		String condition = "";
		if (usuario != null && usuario.getId() != null) {
			condition = "AND i.usuario.id = :idUsuario ";
		}

		String conditionNumeroVenda = "";
		if (numeroVenda != null) {
			conditionNumeroVenda = "AND i.numeroVenda = :numeroVenda ";
		}
		
		String orderBy = " order by i.numeroVenda desc";
		if (!vendasNormais & statusPedido.length == 1) {			
			if (statusPedido[0] == StatusPedido.PENDENTE) {
				orderBy = " order by i.numeroVenda asc";
			}
		}

		String jpql = "SELECT i, (select e from Entrega e where e.venda.id = i.id) FROM Venda i "
				+ "WHERE i.empresa = :empresa AND i.dataVenda between :dateStart and :dateStop " + condition + conditionNumeroVenda
				+ orderBy;

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("idUsuario", usuario.getId());
		}

		if (numeroVenda != null) {
			q.setParameter("numeroVenda", numeroVenda);
		}

		List<Object[]> objects = q.getResultList();
		System.out.println(objects.size() + " - " + jpql);

		for (Object[] objectTemp : objects) {
			System.out.println(Arrays.toString(objectTemp));

			if (vendasNormais & statusPedido.length == 2) {
				vendas.add((Venda) objectTemp[0]);

			} else if (vendasNormais & statusPedido.length == 1) {

				if (vendasNormais & statusPedido[0] == StatusPedido.ENTREGUE) {

					if (objectTemp[1] == null
							|| ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.ENTREGUE.name())) {
						vendas.add((Venda) objectTemp[0]);
					}

				} else if (vendasNormais & statusPedido[0] == StatusPedido.PENDENTE) {

					if (objectTemp[1] == null
							|| ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.PENDENTE.name())) {
						vendas.add((Venda) objectTemp[0]);
					}
				}

			} if (!vendasNormais & statusPedido.length == 2) {
				if(objectTemp[1] != null) {
					vendas.add((Venda) objectTemp[0]);
				}
				
			} else if (!vendasNormais & statusPedido.length == 1) {
				
				if (!vendasNormais & statusPedido[0] == StatusPedido.ENTREGUE) {

					if (objectTemp[1] != null
							&& ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.ENTREGUE.name())) {
						vendas.add((Venda) objectTemp[0]);
					}

				} else if (!vendasNormais & statusPedido[0] == StatusPedido.PENDENTE) {

					if (objectTemp[1] != null
							&& ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.PENDENTE.name())) {
						vendas.add((Venda) objectTemp[0]);
					}
				}
				
			} else if(vendasNormais & statusPedido.length == 0) {
				if(objectTemp[1] == null) {
					vendas.add((Venda) objectTemp[0]);
				}
				
			} else if(!vendasNormais & statusPedido.length == 0) {
				vendas.add((Venda) objectTemp[0]);
			}
		}

		return vendas;

	}
	// i.status = 'Y' AND 
	public Number totalVendas(String empresa) {
		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.empresa = :empresa AND i.status = 'Y' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa);

		Number count = 0;
		try {
			count = (Number) q.getSingleResult();

		} catch (NoResultException e) {

		}

		if (count == null) {
			count = 0;
		}

		return count;
	}
	// i.status = 'Y' AND 
	public Number totalVendasPorDiaDaSemana(Long nomeDia, String empresa) {

		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.empresa = :empresa AND i.nomeDia = :nomeDia AND i.status = 'Y' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("nomeDia", nomeDia).setParameter("empresa", empresa);

		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorCategoria(String empresa) {
		//  i.venda.status = 'Y' AND
		String jpql = "SELECT p.categoriaProduto.nome, SUM(i.total), SUM(i.quantidade) FROM ItemVenda i JOIN i.produto p WHERE i.venda.empresa = :empresa AND i.venda.status = 'Y' AND i.venda.ajuste = 'N' GROUP BY p.categoriaProduto.nome ORDER BY SUM(i.total) DESC";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa);
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorProduto(String categoriaProduto, String empresa) {
		//  i.venda.status = 'Y' AND
		String jpql = "SELECT p.descricao, SUM(i.total), SUM(i.quantidade), p.codigo FROM ItemVenda i JOIN i.produto p where i.venda.empresa = :empresa AND p.categoriaProduto.nome = :categoriaProduto AND i.venda.status = 'Y' AND i.venda.ajuste = 'N' GROUP BY p.codigo, p.descricao ORDER BY SUM(i.total) DESC";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("categoriaProduto", categoriaProduto);
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalParaVendasPorCategoria(String empresa) {

		String jpql = "SELECT i.produto.categoriaProduto.nome, sum(i.quantidadeDisponivel * i.valorUnitario * (1+(i.produto.margemLucro/100))), SUM(i.quantidadeDisponivel) from ItemCompra i where i.compra.empresa = :empresa AND i.quantidadeDisponivel > 0 group by i.produto.categoriaProduto.nome order by sum(i.quantidadeDisponivel * i.valorUnitario * (1+(i.produto.margemLucro/100))) desc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa);
		List<Object[]> result = q.getResultList();
		
		System.out.println(jpql);

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalParaVendasPorProduto(String categoriaProduto, String empresa) {

		String jpql = "SELECT i.produto.descricao, sum(i.quantidadeDisponivel * i.valorUnitario * (1+(i.produto.margemLucro/100))), SUM(i.quantidadeDisponivel), i.produto.codigo from ItemCompra i where i.compra.empresa = :empresa AND i.quantidadeDisponivel > 0 AND i.produto.categoriaProduto.nome = :categoriaProduto group by i.produto.codigo, i.produto.descricao order by sum(i.quantidadeDisponivel * i.valorUnitario * (1+(i.produto.margemLucro/100))) desc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("categoriaProduto", categoriaProduto);
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorData(Calendar calendarStart, Calendar calendarStop,
			CategoriaProduto categoriaProduto, String[] categorias, Produto produto, Usuario usuario,
			boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}

		if (chartCondition != false) {
			select_Condition = "p.dia, p.mes, p.ano, ";
			sum_Condition = "sum(i.total)";
			groupBy_Condition = "p.dia, p.mes, p.ano ";
			orderBy_Condition = "p.ano asc,p.mes asc,p.dia asc ";
		} else {
			select_Condition = "i.produto.descricao, ";
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.dataVenda BETWEEN :dataInicio AND :dataFim AND p.status = 'Y' AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		if (chartCondition != false) {
			/*for (Object[] object : result) {
				if ((long) object[0] < 10) {
					object[0] = "0" + object[0];
				}

				if ((long) object[1] < 10) {
					object[1] = "0" + object[1];
				}
			}*/
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorSemana(String ano, String semana01, String semana02,
			CategoriaProduto categoriaProduto, String[] categorias, Produto produto, Usuario usuario,
			boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}

		if (chartCondition != false) {
			select_Condition = "p.semana, p.ano, ";
			sum_Condition = "sum(i.total)";
			groupBy_Condition = "p.semana, p.ano";
			orderBy_Condition = "p.semana asc, p.ano asc";
		} else {
			select_Condition = "i.produto.descricao, ";
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.semana BETWEEN :semanaInicio AND :semanaFim AND p.status = 'Y' AND p.ajuste = 'N' " + "AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorMes(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}

		if (chartCondition != false) {
			select_Condition = "p.mes, p.ano, ";
			sum_Condition = "sum(i.total)";
			groupBy_Condition = "p.mes, p.ano";
			orderBy_Condition = "p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.descricao, ";
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.mes BETWEEN :mesInicio AND :mesFim AND p.status = 'Y' AND p.ajuste = 'N' " + "AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorAno(String ano01, String ano02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}

		if (chartCondition != false) {
			select_Condition = "p.ano, ";
			sum_Condition = "sum(i.total)";
			groupBy_Condition = "p.ano";
			orderBy_Condition = "p.ano asc";
		} else {
			select_Condition = "i.produto.descricao, ";
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.ano BETWEEN :anoInicio AND :anoFim AND p.status = 'Y' AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("anoInicio", Long.parseLong(ano01)).setParameter("anoFim",
				Long.parseLong(ano02));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucrosPorData(Calendar calendarStart, Calendar calendarStop,
			CategoriaProduto categoriaProduto, String[] categorias, Produto produto, Usuario usuario, 
			boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}

		if (chartCondition != false) {
			select_Condition = "p.dia, p.mes, p.ano, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.total)";
			groupBy_Condition = "p.dia, p.mes, p.ano ";
			orderBy_Condition = "p.dataVenda asc";
			orderBy_Condition = "p.ano asc, p.mes asc, p.dia asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		// AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.dataVenda BETWEEN :dataInicio AND :dataFim AND p.status = 'Y' AND p.ajuste = 'N' "
				/*
				 * + "WHERE p.ano BETWEEN :anoInicio AND :anoFim " +
				 * "AND p.mes BETWEEN :mesInicio AND :mesFim " +
				 * "AND p.dia BETWEEN :diaInicio AND :diaFim "
				 */
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("dataInicio", calendarStart.getTime())// calendarStart.getTime()
				.setParameter("dataFim", calendarStop.getTime());// calendarStop.getTime()
		/*
		 * .setParameter("diaInicio",
		 * Long.parseLong(String.valueOf(calendarStart.get(Calendar.DAY_OF_MONTH))))
		 * .setParameter("diaFim",
		 * Long.parseLong(String.valueOf(calendarStop.get(Calendar.DAY_OF_MONTH))))
		 * .setParameter("mesInicio",
		 * Long.parseLong(String.valueOf(calendarStart.get(Calendar.MONTH) + 1)))
		 * .setParameter("mesFim",
		 * Long.parseLong(String.valueOf(calendarStop.get(Calendar.MONTH) + 1)))
		 * .setParameter("anoInicio",
		 * Long.parseLong(String.valueOf(calendarStart.get(Calendar.YEAR))))
		 * .setParameter("anoFim",
		 * Long.parseLong(String.valueOf(calendarStop.get(Calendar.YEAR))));
		 */

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		if (chartCondition != false) {
			for (Object[] object : result) {
				if ((long) object[0] < 10) {
					object[0] = "0" + object[0];
				}

				if ((long) object[1] < 10) {
					object[1] = "0" + object[1];
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucrosPorSemana(String ano, String semana01, String semana02,
			CategoriaProduto categoriaProduto, String[] categorias, Produto produto, Usuario usuario, 
			boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}
		

		if (chartCondition != false) {
			select_Condition = "p.semana, p.ano, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.total)";
			groupBy_Condition = "p.semana, p.ano";
			orderBy_Condition = "p.semana asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		// AND p.status = 'Y' 
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.semana = :semanaInicio " + "AND p.ano = :ano AND p.status = 'Y'  AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}
		
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucrosPorMes(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}
		

		if (chartCondition != false) {
			select_Condition = "p.mes, p.ano, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.total)";
			groupBy_Condition = "p.mes, p.ano";
			orderBy_Condition = "p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		// AND p.status = 'Y' 
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.mes = :mesInicio " + "AND p.ano = :ano AND p.status = 'Y' AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("mesInicio", Long.parseLong(mes01)).setParameter("ano",
				Long.parseLong(ano));

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucrosPorLote(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (chartCondition != false) {
			select_Condition = "i.compra.mes, p.ano, ";// p.mes
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), i.compra.mes, sum(i.total)";
			groupBy_Condition = "i.compra.mes, p.ano "; // i.produto.categoriaProduto.nome
			orderBy_Condition = "i.compra.mes asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = i.compra.empresa AND p.empresa = :empresa AND i.compra.mes BETWEEN :mesInicio AND :mesFim " + "AND i.compra.ano = :ano AND p.status = 'Y' AND p.ajuste = 'N' " + condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucrosPorAno(String ano01, String ano02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, String empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (categorias != null && categorias.length > 0) {
			condition = "AND i.produto.categoriaProduto.nome in (:categorias) ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}
		
		if (usuario != null && usuario.getId() != null) {
			condition += "AND p.usuario.id = :userID ";
		}
		

		if (chartCondition != false) {
			select_Condition = "p.ano, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.total)";
			groupBy_Condition = "p.ano";
			orderBy_Condition = "p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.lucro), sum(i.valorCompra), sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}
		// AND p.status = 'Y' 
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa = :empresa AND p.ano = :anoInicio AND p.status = 'Y' AND p.ajuste = 'N' " + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa).setParameter("anoInicio", Long.parseLong(ano01));

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (categorias != null && categorias.length > 0) {
			q.setParameter("categorias", Arrays.asList(categorias));
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}
		
		if (usuario != null && usuario.getId() != null) {
			q.setParameter("userID", usuario.getId());
		}

		List<Object[]> result = q.getResultList();

		return result;
	}
	
}
