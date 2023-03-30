package com.webapp.repository;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Cliente;
import com.webapp.model.Empresa;
import com.webapp.model.Entrega;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.TipoDataLancamento;
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

	public List<Venda> todas(Empresa empresa) {
		return this.manager.createQuery("from Venda where empresa.id = :empresa order by numeroVenda desc", Venda.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}
	
	public List<Venda> todas_(Empresa empresa) {
		return this.manager.createQuery("from Venda where empresa.id = :empresa and quantidadeItens > 0 order by numeroVenda desc", Venda.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public Venda ultimoNVenda(Empresa empresa) {

		try {
			return this.manager.createQuery("from Venda e WHERE e.empresa.id = :empresa order by e.numeroVenda desc", Venda.class).setMaxResults(1)
					.setParameter("empresa", empresa.getId()).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}

	}

	public Venda porNumeroVenda(Long numeroVenda, Empresa empresa) {
		try {
			return this.manager.createQuery("from Venda e where e.empresa.id = :empresa AND e.numeroVenda = :numeroVenda", Venda.class)
					.setParameter("empresa", empresa.getId()).setParameter("numeroVenda", numeroVenda).getSingleResult();
		} catch (NoResultException e) {

		}

		return null;
	}
	
	public Venda porStatusMesa(String numeroMesa, String statusMesa, Empresa empresa) {
		try {
			return this.manager.createQuery("from Venda e where e.empresa.id = :empresa AND e.mesa = :numeroMesa AND e.statusMesa IS NULL", Venda.class)
					.setParameter("empresa", empresa.getId()).setParameter("numeroMesa", numeroMesa).getSingleResult();
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
			StatusPedido[] statusPedido, Usuario usuario, Cliente cliente, Usuario entregador, boolean vendasPagas, 
			Empresa empresa, TipoDataLancamento tipoData) {

		List<Venda> vendas = new ArrayList<>();

		String condition = "";
		if (usuario != null && usuario.getId() != null) {
			condition += "AND i.usuario.id = :idUsuario ";
		}
		
		if (cliente != null && cliente.getId() != null) {
			condition += "AND i.cliente.id = :idCliente ";
		}
		
		if (entregador != null && entregador.getId() != null) {
			condition += "AND i.entregador.id = :idEntregador ";
		}
		
		String conditionVendasPagas = "";
		if (vendasPagas) {
			conditionVendasPagas = "AND i.status = 'Y' ";
		} else {
			conditionVendasPagas = "AND (i.status = 'N' OR i.status = 'Y') ";
		}

		String conditionNumeroVenda = "";		
		if (numeroVenda != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroVenda))) {
				if(!String.valueOf(numeroVenda).trim().equals("0")) {
					System.out.println(numeroVenda + " - " + String.valueOf(numeroVenda));
					conditionNumeroVenda = "AND i.numeroVenda = :numeroVenda ";
				}			
			}
		}
		
		String data = "";
		if(tipoData == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento";
		} else if(tipoData == TipoDataLancamento.LANCAMENTO) {
			data = "AND i.dataVenda";
		}
		
		String orderBy = " order by i.numeroVenda desc";
		if (!vendasNormais & statusPedido.length == 1) {			
			if (statusPedido[0] == StatusPedido.PENDENTE) {
				orderBy = " order by i.numeroVenda asc";
			}
		}

		String jpql = "SELECT i, (select e from Entrega e where e.venda.id = i.id and e.exclusao = 'N') FROM Venda i "
				+ "WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.ajuste = 'N' and i.exclusao = 'N' " + data + " between :dateStart and :dateStop " + condition 
				+ conditionNumeroVenda + conditionVendasPagas
				+ orderBy;

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("idUsuario", usuario.getId());
		}
		
		if (cliente != null && cliente.getId() != null) {
			q.setParameter("idCliente", cliente.getId());
		}
		
		if (entregador != null && entregador.getId() != null) {
			q.setParameter("idEntregador", entregador.getId());
		}

		if (numeroVenda != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroVenda))) {
				if(!String.valueOf(numeroVenda).trim().equals("0")) {
					System.out.println(numeroVenda + " - " + String.valueOf(numeroVenda));
					q.setParameter("numeroVenda", Long.parseLong(String.valueOf(numeroVenda).trim()));
				}
			}
		}

		List<Object[]> objects = q.getResultList();
		System.out.println(objects.size() + " - " + jpql);

		for (Object[] objectTemp : objects) {
			System.out.println(Arrays.toString(objectTemp));

			if(vendasPagas && ((Venda) objectTemp[0]).isVendaPaga()) {
				vendas.add((Venda) objectTemp[0]);
				
			} else if(!vendasPagas) {
								
				if (vendasNormais && statusPedido.length == 2) {
					vendas.add((Venda) objectTemp[0]);
	
				} else if (vendasNormais && statusPedido.length == 1) {
	
					if (vendasNormais && statusPedido[0] == StatusPedido.ENTREGUE) {
	
						if (objectTemp[1] == null
								|| ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.ENTREGUE.name())) {
							vendas.add((Venda) objectTemp[0]);
						}
	
					} else if (vendasNormais && statusPedido[0] == StatusPedido.PENDENTE) {
	
						if (objectTemp[1] == null
								|| ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.PENDENTE.name())) {
							vendas.add((Venda) objectTemp[0]);
						}
					}
	
				} if (!vendasNormais && statusPedido.length == 2) {
					if(objectTemp[1] != null) {
						vendas.add((Venda) objectTemp[0]);
					}
					
				} else if (!vendasNormais && statusPedido.length == 1) {
					
					if (!vendasNormais && statusPedido[0] == StatusPedido.ENTREGUE) {
	
						if (objectTemp[1] != null
								&& ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.ENTREGUE.name())) {
							vendas.add((Venda) objectTemp[0]);
						}
	
					} else if (!vendasNormais && statusPedido[0] == StatusPedido.PENDENTE) {
	
						if (objectTemp[1] != null
								&& ((Entrega) objectTemp[1]).getStatus().equalsIgnoreCase(StatusPedido.PENDENTE.name())) {
							vendas.add((Venda) objectTemp[0]);
						}
					}
					
				} else if(vendasNormais && statusPedido.length == 0) {
					if(objectTemp[1] == null) {
						vendas.add((Venda) objectTemp[0]);
					}
					
				} else if(!vendasNormais && statusPedido.length == 0) {
					vendas.add((Venda) objectTemp[0]);
				}
			}
		}

		return vendas;

	}
	
	
	
	@SuppressWarnings("unchecked")
	public List<Venda> orcamentosFiltradas(Long numeroVenda, Date dateStart, Date dateStop, Usuario usuario, Empresa empresa) {

		List<Venda> vendas = new ArrayList<>();

		String condition = "";
		if (usuario != null && usuario.getId() != null) {
			condition += "AND i.usuario.id = :idUsuario ";
		}

		String conditionNumeroVenda = "";		
		if (numeroVenda != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroVenda))) {
				if(!String.valueOf(numeroVenda).trim().equals("0")) {
					System.out.println(numeroVenda + " - " + String.valueOf(numeroVenda));
					conditionNumeroVenda = "AND i.numeroVenda = :numeroVenda ";
				}			
			}
		}
		
		String orderBy = " order by i.numeroVenda desc";

		String jpql = "SELECT i, (select e from Entrega e where e.venda.id = i.id) FROM Venda i "
				+ "WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.ajuste = 'Y' AND i.conta = 'Y' AND i.dataVenda between :dateStart and :dateStop " + condition 
				+ conditionNumeroVenda
				+ orderBy;

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("idUsuario", usuario.getId());
		}

		if (numeroVenda != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroVenda))) {
				if(!String.valueOf(numeroVenda).trim().equals("0")) {
					System.out.println(numeroVenda + " - " + String.valueOf(numeroVenda));
					q.setParameter("numeroVenda", Long.parseLong(String.valueOf(numeroVenda).trim()));
				}
			}
		}

		List<Object[]> objects = q.getResultList();
		System.out.println(objects.size() + " - " + jpql);

		for (Object[] objectTemp : objects) {
			
			vendas.add((Venda) objectTemp[0]);
			
		}

		return vendas;

	}
	
	
	@SuppressWarnings("unchecked")
	public List<Venda> vendasFiltradasPDV(Long numeroVenda, Date dateStart, Date dateStop, Usuario usuario, Empresa empresa) {

		List<Venda> vendas = new ArrayList<>();

		String condition = "";
		if (usuario != null && usuario.getId() != null) {
			condition = "AND i.usuario.id = :idUsuario ";
		}

		String conditionNumeroVenda = "";
		if (numeroVenda != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroVenda))) {
				if(!String.valueOf(numeroVenda).trim().equals("0")) {
					System.out.println(numeroVenda + " - " + String.valueOf(numeroVenda));
					conditionNumeroVenda = "AND i.numeroVenda = :numeroVenda ";
				}			
			}
		}
		
		String orderBy = " order by i.numeroVenda desc";

		String jpql = "SELECT i, (select e from Entrega e where e.venda.id = i.id) FROM Venda i "
				+ "WHERE i.empresa.id = :empresa AND i.pdv = 'Y' AND i.ajuste = 'N' AND (i.conta = 'N' OR (i.conta = 'Y' AND i.mesa is not null)) AND i.status = 'Y' AND i.quantidadeItens > 0 AND i.dataVenda between :dateStart and :dateStop  " + condition + conditionNumeroVenda + orderBy;

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (usuario != null && usuario.getId() != null) {
			q.setParameter("idUsuario", usuario.getId());
		}

		if (numeroVenda != null) {
			if (StringUtils.isNotBlank(String.valueOf(numeroVenda))) {
				if(!String.valueOf(numeroVenda).trim().equals("0")) {
					System.out.println(numeroVenda + " - " + String.valueOf(numeroVenda));
					q.setParameter("numeroVenda", Long.parseLong(String.valueOf(numeroVenda).trim()));
				}
			}
		}

		List<Object[]> objects = q.getResultList();
		System.out.println(objects.size() + " - " + jpql);

		for (Object[] objectTemp : objects) {
			System.out.println(Arrays.toString(objectTemp));
			
			vendas.add((Venda) objectTemp[0]);
		}

		return vendas;

	}
	
	
	// i.status = 'Y' AND 
	public Number totalVendas(Empresa empresa) { 
		String jpql = "SELECT sum(i.total) FROM ItemVenda i WHERE i.venda.empresa.id = :empresa AND i.venda.vendaPaga = 'Y' AND i.venda.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	
	public Number totalComissao(Empresa empresa, Usuario usuario) {
		String jpql = "SELECT sum(((i.valorTotal - i.desconto) * i.taxaDeComissao)/100) FROM Venda i WHERE i.empresa.id = :empresa "
				+ "AND i.conta = 'N' AND i.vendaPaga = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "and i.exclusao = 'N' and i.usuario.id = :idUsuario "
				+ "AND i.taxaDeComissao > 0";
		Query q = manager.createQuery(jpql)
				.setParameter("empresa", empresa.getId())
				.setParameter("idUsuario", usuario.getId());

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
	
	public Number totalLucros(Empresa empresa) {
		String jpql = "SELECT sum(i.lucro) FROM ItemVenda i WHERE i.venda.empresa.id = :empresa AND i.venda.conta = 'N' AND i.venda.vendaPaga = 'Y' AND i.venda.ajuste = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalDescontos(Empresa empresa) {
		String jpql = "SELECT sum(v.desconto) FROM Venda v WHERE v.empresa.id = :empresa AND v.conta = 'N' AND v.vendaPaga = 'Y' AND v.ajuste = 'N' and v.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	public Number totalEmTaxas(Empresa empresa) {
		String jpql = "SELECT sum((v.valorTotal - v.desconto) * v.taxaDeAcrescimo/100) FROM Venda v WHERE v.empresa.id = :empresa AND v.conta = 'N' AND v.vendaPaga = 'Y' AND v.ajuste = 'N' AND v.clientePagouTaxa = 'N' AND v.taxaDeAcrescimo > 0 and v.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	public Number totalComprasVendidas(Empresa empresa) {
		String jpql = "SELECT sum(i.valorCompra) FROM ItemVenda i WHERE i.venda.empresa.id = :empresa AND i.venda.vendaPaga = 'Y' AND i.venda.ajuste = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

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
	
	
	public Number vendasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.venda.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(i.total) FROM ItemVenda i Where i.venda.empresa.id = :empresa AND i.venda.ajuste = 'N' AND i.venda.vendaPaga = 'Y' AND i.venda.conta = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

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
	
	public Number vendasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, List<BigInteger> contas) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.venda.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String listaDeContas = "";
		if(contas.size() > 0) {
			listaDeContas = "AND i.venda.numeroVenda not in (:contas)";
		}
		
		String jpql = "SELECT sum(i.total) FROM ItemVenda i Where i.venda.empresa.id = :empresa " + listaDeContas + " AND i.venda.ajuste = 'N' AND i.venda.vendaPaga = 'Y' AND i.venda.conta = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(contas.size() > 0) {
			q.setParameter("contas", contas);
		}

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
	
	@SuppressWarnings("unchecked")
	public List<ItemVenda> vendasAvistaPagas_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.venda.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT i FROM ItemVenda i Where i.venda.empresa.id = :empresa AND i.venda.ajuste = 'N' AND i.venda.vendaPaga = 'Y' AND i.venda.conta = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	
	public Number descontoVendasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i Where i.empresa.id = :empresa AND i.ajuste = 'N' AND i.vendaPaga = 'Y' AND i.conta = 'N' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

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
	
	public Number descontoVendasAvistaPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, List<BigInteger> contas) {
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String listaDeContas = "";
		if(contas.size() > 0) {
			listaDeContas = "AND i.numeroVenda not in (:contas)";
		}
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i Where i.empresa.id = :empresa " + listaDeContas + " AND i.ajuste = 'N' AND i.vendaPaga = 'Y' AND i.conta = 'N' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(contas.size() > 0) {
			q.setParameter("contas", contas);
		}

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
	
	@SuppressWarnings("unchecked")
	public List<Venda> descontoVendasAvistaPagas_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT i FROM Venda i Where i.empresa.id = :empresa AND i.ajuste = 'N' AND i.vendaPaga = 'Y' AND i.conta = 'N' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	
	public Number descontoVendasAvistaAPagar(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i Where i.empresa.id = :empresa AND i.ajuste = 'N' AND i.vendaPaga = 'N' AND i.conta = 'Y' and i.exclusao = 'N' " + periodo;
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

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
	public Number totalVendasPorDiaDaSemana(Long nomeDia, Empresa empresa) {

		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.empresa.id = :empresa AND i.nomeDia = :nomeDia AND i.vendaPaga = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("nomeDia", nomeDia).setParameter("empresa", empresa.getId());

		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorCategoria(Empresa empresa) {
		//  i.venda.status = 'Y' AND //AND ((i.venda.vendaPaga = 'Y' AND i.venda.conta = 'N') OR (i.venda.vendaPaga = 'N' AND i.venda.conta = 'Y'))
		String jpql = "SELECT p.categoriaProduto.nome, SUM(i.total), SUM(i.quantidade) FROM ItemVenda i JOIN i.produto p WHERE i.venda.empresa.id = :empresa AND i.venda.ajuste = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N' GROUP BY p.categoriaProduto.nome ORDER BY SUM(i.total) DESC";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorProduto(String categoriaProduto, Empresa empresa) {
		//  i.venda.status = 'Y' AND //AND ((i.venda.vendaPaga = 'Y' AND i.venda.conta = 'N') OR (i.venda.vendaPaga = 'N' AND i.venda.conta = 'Y'))
		String jpql = "SELECT p.descricao, SUM(i.total), SUM(i.quantidade), p.codigo, p.unidadeMedida FROM ItemVenda i JOIN i.produto p where i.venda.empresa.id = :empresa AND p.categoriaProduto.nome = :categoriaProduto AND i.venda.ajuste = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N' GROUP BY p.codigo, p.descricao, p.unidadeMedida ORDER BY SUM(i.total) DESC";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("categoriaProduto", categoriaProduto);
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalParaVendasPorCategoria(Empresa empresa) {

		String jpql = "SELECT i.produto.categoriaProduto.nome, sum(i.quantidadeDisponivel * i.produto.precoDeVenda), SUM(i.quantidadeDisponivel) from ItemCompra i where i.produto.estoque = 'Y' AND i.compra.empresa.id = :empresa AND i.quantidadeDisponivel >= 0 group by i.produto.categoriaProduto.nome order by sum(i.quantidadeDisponivel * i.valorUnitario * (1+(i.produto.margemLucro/100))) desc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
		List<Object[]> result = q.getResultList();
		
		System.out.println(jpql);

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalParaVendasPorProduto(String categoriaProduto, Empresa empresa) {

		String jpql = "SELECT i.produto.descricao, sum(i.quantidadeDisponivel * i.produto.precoDeVenda), SUM(i.quantidadeDisponivel), i.produto.codigo, i.produto.unidadeMedida from ItemCompra i where i.produto.estoque = 'Y' AND i.compra.empresa.id = :empresa AND i.quantidadeDisponivel >= 0 AND i.produto.categoriaProduto.nome = :categoriaProduto group by i.produto.codigo, i.produto.descricao, i.produto.unidadeMedida order by sum(i.quantidadeDisponivel * i.valorUnitario * (1+(i.produto.margemLucro/100))) desc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("categoriaProduto", categoriaProduto);
		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalVendasPorData(Calendar calendarStart, Calendar calendarStop,
			CategoriaProduto categoriaProduto, String[] categorias, Produto produto, Usuario usuario,
			boolean chartCondition, Empresa empresa, TipoDataLancamento tipoDataLancamento) {

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
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo, i.produto.unidadeMedida";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao, i.produto.unidadeMedida ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		
		String data = "AND p.dataVenda BETWEEN :dataInicio AND :dataFim";
		if(tipoDataLancamento == TipoDataLancamento.PAGAMENTO) {
			data = "AND p.dataPagamento BETWEEN :dataInicio AND :dataFim AND p.vendaPaga = 'Y'";
		}
		
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa " + data + " AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
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
			boolean chartCondition, Empresa empresa) {

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
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo, i.produto.unidadeMedida";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao, i.produto.unidadeMedida ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa AND p.semana BETWEEN :semanaInicio AND :semanaFim AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + "AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
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
	public List<Object[]> totalVendasPorMes_(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, Empresa empresa) {

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
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo, i.produto.unidadeMedida";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao, i.produto.unidadeMedida ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes BETWEEN :mesInicio AND :mesFim AND p.conta = 'N' AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + "AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01))
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
	public List<Object[]> totalVendasPorMesRelatorio(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, Empresa empresa) {

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
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo, i.produto.unidadeMedida";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao, i.produto.unidadeMedida ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes BETWEEN :mesInicio AND :mesFim AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + "AND p.ano = :ano " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01))
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
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, Empresa empresa) {

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
			sum_Condition = "sum(i.total), sum(i.quantidade), i.produto.codigo, i.produto.unidadeMedida";
			groupBy_Condition = "i.produto.id, i.produto.codigo, i.produto.descricao, i.produto.unidadeMedida ";
			orderBy_Condition = "sum(i.quantidade) desc, sum(i.total) desc";
		}
		//  AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa AND p.ano BETWEEN :anoInicio AND :anoFim AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("anoInicio", Long.parseLong(ano01)).setParameter("anoFim",
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
			boolean chartCondition, Empresa empresa, TipoDataLancamento tipoDataLancamento) {

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
		
		String data = "AND p.dataVenda BETWEEN :dataInicio AND :dataFim";
		if(tipoDataLancamento == TipoDataLancamento.PAGAMENTO) {
			data = "AND p.dataVenda BETWEEN :dataInicio AND :dataFim AND p.vendaPaga = 'Y'";
		}
		
		// AND p.status = 'Y'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa " + data + " AND p.ajuste = 'N' and i.exclusao = 'N' and p.exclusao = 'N' "
				/*
				 * + "WHERE p.ano BETWEEN :anoInicio AND :anoFim " +
				 * "AND p.mes BETWEEN :mesInicio AND :mesFim " +
				 * "AND p.dia BETWEEN :diaInicio AND :diaFim "
				 */
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime())// calendarStart.getTime()
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
			boolean chartCondition, Empresa empresa) {

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
				+ "WHERE p.empresa.id = :empresa AND p.semana = :semanaInicio " + "AND p.ano = :ano AND p.vendaPaga = 'Y'  AND p.ajuste = 'N' and i.exclusao = 'N' and p.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
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
	public List<Object[]> totalLucrosPorMes__(String ano, String mes01, String mes02, CategoriaProduto categoriaProduto,
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, Empresa empresa) {

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
		// AND p.status = 'Y' // AND p.conta = 'N'
		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mesInicio " + "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01)).setParameter("ano",
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
			String[] categorias, Produto produto, boolean chartCondition, Empresa empresa) {

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
				+ "WHERE p.empresa.id = i.compra.empresa.id AND p.empresa.id = :empresa AND i.compra.mes BETWEEN :mesInicio AND :mesFim " + "AND i.compra.ano = :ano AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + condition
				+ "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01))
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
			String[] categorias, Produto produto, Usuario usuario, boolean chartCondition, Empresa empresa) {

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
				+ "WHERE p.empresa.id = :empresa AND p.ano = :anoInicio AND p.vendaPaga = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("anoInicio", Long.parseLong(ano01));

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
	
	
	public Number totalVendasPorDiaValor(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataVenda between :dateStart and :dateStop AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

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
	
	public Number totalVendasParceladasPorDiaValor(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataVenda between :dateStart and :dateStop AND i.vendaPaga = 'N' AND i.conta = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

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

	public Number totalDescontosPorDiaValor(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataPagamento between :dateStart and :dateStop AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	public Number totalDescontosVendasParceladasPorDiaValor(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataPagamento between :dateStart and :dateStop AND i.vendaPaga = 'Y' AND i.conta = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	public Number totalTaxasPorDiaValor(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum((i.valorTotal - i.desconto) * i.taxaDeAcrescimo/100) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataVenda between :dateStart and :dateStop AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' AND i.clientePagouTaxa = 'N' AND i.taxaDeAcrescimo > 0 and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	
	public Number totalTaxasValor(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum((i.valorTotal - i.desconto) * i.taxaDeAcrescimo/100) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' AND i.clientePagouTaxa = 'N' AND i.taxaDeAcrescimo > 0 and i.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
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
	
	public Number totalTaxasValor(Empresa empresa, Calendar calendarStart, Calendar calendarStop, List<BigInteger> contas) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String listaDeContas = "";
		if(contas.size() > 0) {
			listaDeContas = "AND i.numeroVenda not in (:contas)";
		}
		
		String jpql = "SELECT sum((i.valorTotal - i.desconto) * i.taxaDeAcrescimo/100) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa " + listaDeContas + " AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' AND i.clientePagouTaxa = 'N' AND i.taxaDeAcrescimo > 0 and i.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(contas.size() > 0) {
			q.setParameter("contas", contas);
		}
		
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
	
	@SuppressWarnings("unchecked")
	public List<Venda> totalTaxasValor_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND i.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT i FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' AND i.clientePagouTaxa = 'N' AND i.taxaDeAcrescimo > 0 and i.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
	
		return q.getResultList();
	}
	
	
	public Number totalVendasPorDiaQuantidade(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT count(i.id) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataVenda between :dateStart and :dateStop AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

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
	
	public Number totalVendasParceladasPorDiaQuantidade(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT count(i.id) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.dataVenda between :dateStart and :dateStop AND i.vendaPaga = 'N' AND i.conta = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

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

	public Number totalLucrosPorDia(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.lucro) FROM ItemVenda i WHERE i.venda.quantidadeItens > 0 AND i.venda.empresa.id = :empresa AND i.venda.dataVenda between :dateStart and :dateStop AND i.venda.vendaPaga = 'Y' AND i.venda.conta = 'N' AND i.venda.ajuste = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	public Number totalLucrosVendasParceladasPorDia(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.lucro) FROM ItemVenda i WHERE i.venda.quantidadeItens > 0 AND i.venda.empresa.id = :empresa AND i.venda.dataVenda between :dateStart and :dateStop AND i.venda.vendaPaga = 'N' AND i.venda.conta = 'Y' AND i.venda.ajuste = 'N' and i.venda.exclusao = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	
	public Number totalEstornosPorDia(Date dateStart, Date dateStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.estorno) FROM Venda i WHERE i.empresa.id = :empresa AND i.dataVenda between :dateStart and :dateStop AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	
	public Number totalDescontosPorDia(Date dateStart, Date dateStop, Empresa empresa, TipoDataLancamento tipoDataLancamento) {
		
		String data = "AND i.dataVenda between :dateStart and :dateStop";
		if(tipoDataLancamento == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento BETWEEN :dateStart AND :dateStop AND i.vendaPaga = 'Y'";
		}
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i WHERE i.empresa.id = :empresa " + data + " AND i.conta = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	
	
	public Number totalDescontosPorDiaVendaParcelada(Date dateStart, Date dateStop, Empresa empresa, TipoDataLancamento tipoDataLancamento) {
		
		String data = "AND i.dataVenda between :dateStart and :dateStop";
		if(tipoDataLancamento == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento BETWEEN :dateStart AND :dateStop AND i.vendaPaga = 'Y'";
		}
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i WHERE i.empresa.id = :empresa " + data + " AND i.conta = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	
	public Number totalTaxasEntregaPorDia(Date dateStart, Date dateStop, Empresa empresa, TipoDataLancamento tipoDataLancamento) {
		
		String data = "AND i.dataVenda between :dateStart and :dateStop";
		if(tipoDataLancamento == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento BETWEEN :dateStart AND :dateStop AND i.vendaPaga = 'Y'";
		}
		
		String jpql = "SELECT sum(i.taxaDeEntrega) FROM Venda i WHERE i.empresa.id = :empresa " + data + " AND i.conta = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	public Number totalTaxasEntregaPorDiaVendaParcelada(Date dateStart, Date dateStop, Empresa empresa, TipoDataLancamento tipoDataLancamento) {
		
		String data = "AND i.dataVenda between :dateStart and :dateStop";
		if(tipoDataLancamento == TipoDataLancamento.PAGAMENTO) {
			data = "AND i.dataPagamento BETWEEN :dateStart AND :dateStop AND i.vendaPaga = 'Y'";
		}
		
		String jpql = "SELECT sum(i.taxaDeEntrega) FROM Venda i WHERE i.empresa.id = :empresa " + data + " AND i.conta = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
	
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
	
	
	
	
	public Number totalEstornosPorSemana(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.estorno) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.semana = :semanaInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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

	public Number totalDescontosPorSemana(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.desconto) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.semana = :semanaInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	
	public Number totalTaxasEntregaPorSemana(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.taxaDeEntrega) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.semana = :semanaInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalTaxasEntregaPorSemanaVendasParceladas(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.taxaDeEntrega) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.semana = :semanaInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	
	public Number totalDescontosPorSemanaVendaParcelada(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.desconto) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.semana = :semanaInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	
	public Number totalEstornosPorMes(String ano, String mes01, String mes02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.estorno) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mesInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("ano", Long.parseLong(ano));
		
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

	public Number totalDescontosPorMes(String ano, String mes01, String mes02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.desconto) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mesInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalDescontosPorMesVendasParceladas(String ano, String mes01, String mes02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.desconto) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mesInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalTaxasEntregasPorMes(String ano, String mes01, String mes02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.taxaDeEntrega) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mesInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalTaxasEntregasPorMesVendasParceladas(String ano, String mes01, String mes02, Empresa empresa) {
		
		String jpql = "SELECT sum(p.taxaDeEntrega) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mesInicio " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	
	public Number totalEstornosPorAno(String ano, Empresa empresa) {
		
		String jpql = "SELECT sum(p.estorno) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));
		
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

	public Number totalDescontosPorAno(String ano, Empresa empresa) {
		
		String jpql = "SELECT sum(p.desconto) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalDescontosPorAnoVendasParceladas(String ano, Empresa empresa) {
		
		String jpql = "SELECT sum(p.desconto) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalTaxasEntregasPorAno(String ano, Empresa empresa) {
		
		String jpql = "SELECT sum(p.taxaDeEntrega) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));
		
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
	
	public Number totalTaxasEntregasPorAnoVendasParceladas(String ano, Empresa empresa) {
		
		String jpql = "SELECT sum(p.taxaDeEntrega) FROM Venda p "
				+ "WHERE p.empresa.id = :empresa " 
				+ "AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'Y' AND p.ajuste = 'N' and p.exclusao = 'N' ";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));
		
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

	
	public Number totalEntregasPendentesValor(Empresa empresa) {
		
		String jpql = "SELECT sum(i.valorTotal) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.statusMesa = 'PAGO' AND i.vendaPaga = 'N' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
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

	public Number totalDescontosPendentesValor(Empresa empresa) {
		
		String jpql = "SELECT sum(i.desconto) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.vendaPaga = 'N' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
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
	
	public Number totalEntregasPendentesQuantidade(Empresa empresa) {
		
		String jpql = "SELECT count(i.id) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.statusMesa = 'PAGO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
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
	
	public Number totalEntregasPendentesAReceber(Empresa empresa) {
		
		String jpql = "SELECT count(i.id) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.statusMesa = 'PAGO' AND i.vendaPaga = 'N' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
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
	
	public Number totalEntregasPendentesPagas(Empresa empresa) {
		
		String jpql = "SELECT count(i.id) FROM Venda i WHERE i.quantidadeItens > 0 AND i.empresa.id = :empresa AND i.statusMesa = 'PAGO' AND i.vendaPaga = 'Y' AND i.conta = 'N' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
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
	
	public Number totalVendasPorMes(Number mes, Number ano, CategoriaProduto categoriaProduto, Produto produto,
			boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";
		
		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			condition = "AND i.produto.categoriaProduto.nome = :categoriaProduto ";
		}

		if (produto != null && produto.getId() != null) {
			condition += "AND i.produto.id = :id ";
		}

		if (chartCondition != false) {
			select_Condition = "";
			sum_Condition = "sum(i.total)";//sum(i.valorTotal)
			groupBy_Condition = "p.mes, p.ano ";
			orderBy_Condition = "p.mes asc, p.ano asc";
		} else {
			select_Condition = "i.produto.nome, ";
			sum_Condition = "sum(i.quantidade)";
			groupBy_Condition = "i.produto.nome ";
			orderBy_Condition = "sum(i.quantidade) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM ItemVenda i join i.venda p "
				+ "WHERE p.empresa.id = :empresa AND p.mes = :mes AND p.ano = :ano AND p.vendaPaga = 'Y' AND p.conta = 'N' AND p.ajuste = 'N' and p.exclusao = 'N' and i.exclusao = 'N'" + condition + "group by " + groupBy_Condition + " order by "
				+ orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("mes", Long.parseLong(String.valueOf(mes))).setParameter("ano",
				Long.parseLong(String.valueOf(ano))).setParameter("empresa", empresa.getId());

		if (categoriaProduto != null && categoriaProduto.getId() != null) {
			q.setParameter("categoriaProduto", categoriaProduto.getNome());
		}

		if (produto != null && produto.getId() != null) {
			q.setParameter("id", produto.getId());
		}

		Number value = 0;
		try {
			value = (Number) q.getSingleResult();

		} catch (NoResultException e) {

		}

		if (value == null) {
			value = 0;
		}

		return value;
	}
}
