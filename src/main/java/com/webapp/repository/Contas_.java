package com.webapp.repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.Cliente;
import com.webapp.model.Conta;
import com.webapp.model.Empresa;
import com.webapp.model.OrigemConta;
import com.webapp.model.PagamentoConta;
import com.webapp.model.TipoOperacao;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Contas_ implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Conta porId(Long id) {
		return this.manager.find(Conta.class, id);
	}

	@Transacional
	public Conta save(Conta conta) {
		return this.manager.merge(conta);
	}

	@Transacional
	public void remove(Conta conta) {
		Conta contaTemp = new Conta();
		contaTemp = this.manager.merge(conta);

		this.manager.remove(contaTemp);
	}

	public List<Conta> todas() {
		return this.manager.createQuery("from Conta order by id", Conta.class).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalDespesasPorCategoriaMesAtual(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String jpql = "SELECT i.codigoOperacao, sum(i.valor) FROM Conta i WHERE i.operacao = 'LANCAMENTO' and i.tipo = 'DEBITO' and i.status = 'Y' and i.mes = :mesAtual and i.ano = :anoAtual AND i.ajuste = 'N' and i.exclusao = 'N' GROUP BY i.codigoOperacao order by sum(i.valor) desc";
		Query q = manager.createQuery(jpql)
				.setParameter("mesAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.MONTH) + 1)))
				.setParameter("anoAtual", Long.parseLong(String.valueOf(calendar.get(Calendar.YEAR))));
		List<Object[]> result = q.getResultList();

		return result;
	}
	
	
	public Number vendasAPagarPagas(String tipo, String operacao, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.pagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		//modificado de subtotal para valor
		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.parcela <> 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

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
	
	@SuppressWarnings("unchecked")
	public List<Conta> vendasAVistaAPagarPagas(String tipo, String operacao, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.pagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		//modificado de subtotal para valor
		String jpql = "FROM Conta c WHERE c.parcela = 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		return q.getResultList();
	}
	
	public Number porContasPagas(String tipo, String operacao, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.pagamento BETWEEN :dataInicio AND :dataFim";
		}
		//c.parcela <> 'AVISTA' AND 
		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

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
	
	
	@SuppressWarnings("unchecked")
	public List<Conta> despesasAVistaAPagarPagas(String tipo, String operacao, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.pagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "FROM Conta c WHERE c.parcela = 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> receitasAVistaAPagarPagas(String tipo, String operacao, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.pagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "FROM Conta c WHERE c.parcela = 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> comprasAVistaAPagarPagas(String tipo, String operacao, Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.pagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "FROM Conta c WHERE c.parcela = 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N' " + periodo;
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		return q.getResultList();
	}
	
	public Number lucroEmVendasAPagarPagas(String tipo, String operacao, Empresa empresa) {

		String jpql = "SELECT sum(c.lucro) FROM Conta c WHERE c.parcela <> 'AVISTA' AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.status = 'Y' AND c.ajuste = 'N' and c.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

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
	public List<Conta> comissaoEmVendasAPagarPagas(String tipo, String operacao, Empresa empresa) {

		String jpql = "FROM Conta c WHERE (c.parcela = 'AVISTA' and c.status = 'N') AND c.empresa.id = :empresa AND c.tipo = :tipo AND c.operacao = :operacao AND c.ajuste = 'N' and c.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("operacao", operacao)
				.setParameter("empresa", empresa.getId());

		return q.getResultList();
	}

	public Number porContasPagas(String tipo, Empresa empresa) {

		String jpql = "SELECT sum(c.valor) FROM Conta c WHERE c.empresa.id = :empresa AND c.tipo = :tipo AND c.status = 'Y' AND c.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("tipo", tipo).setParameter("empresa", empresa.getId());

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

	public List<Conta> porContasPagas(Long codigoOperacao, String operacao) {
		return this.manager.createQuery(
				"from Conta i where i.codigoOperacao = :codigoOperacao and operacao = :operacao and i.status = 'Y' AND i.ajuste = 'N'",
				Conta.class).setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao)
				.getResultList();
	}

	public List<Conta> porCodigoOperacao(Long codigoOperacao, String operacao, Empresa empresa) {
		return this.manager
				.createQuery("from Conta i where i.empresa.id = :empresa AND i.codigoOperacao = :codigoOperacao and operacao = :operacao AND i.ajuste = 'N' order by i.id asc",
						Conta.class)
				.setParameter("empresa", empresa.getId())
				.setParameter("codigoOperacao", codigoOperacao).setParameter("operacao", operacao).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Conta> contasFiltradas(Long codigo, TipoOperacao tipoOperacao, Date dateStart, Date dateStop,
			OrigemConta[] origemConta, String contasPagas, Cliente cliente, Usuario funcionario, Empresa empresa) {

		String conditionCodigo = "";
		String conditionOrigemConta = "";
		String conditionContasPagas = "";
		
		String conditionFuncionarioVenda = "";
		String conditionFuncionarioCompra = "";
		String conditionFuncionarioLancamento = "";
		
		String conditionCliente = "";

		if (codigo != null) {
			conditionCodigo = "AND c.codigoOperacao = :codigo ";		
		}
		
		if (origemConta != null && origemConta.length > 0) {
			conditionOrigemConta = "AND c.tipo in (:origemConta) ";		
		}

		if(contasPagas.equals("1")) {
			conditionContasPagas = "AND c.status = 'Y' ";
		} else if(contasPagas.equals("2")) {
			conditionContasPagas = "AND c.status = 'N' ";
		}
		
		if (funcionario != null) {
			if(cliente != null) {
				conditionFuncionarioVenda = "AND v.usuario.id = :funcionario ";
				conditionFuncionarioCompra = "AND cp.usuario.id = :funcionario ";
				conditionFuncionarioLancamento = "AND l.usuario.id = :funcionario ";
				
			} else {
				if (tipoOperacao != null) {	
					conditionFuncionarioVenda = "AND v.usuario.id = :funcionario ";
					conditionFuncionarioCompra = "AND cp.usuario.id = :funcionario ";
					conditionFuncionarioLancamento = "AND l.usuario.id = :funcionario ";
					
				} else {
					conditionFuncionarioVenda = "AND v.usuario_id = :funcionario ";
					conditionFuncionarioCompra = "AND cp.usuario_id = :funcionario ";
					conditionFuncionarioLancamento = "AND l.usuario_id = :funcionario ";
				}	
			}			
		}
		
		if(cliente != null) {
			conditionCliente = "AND v.cliente.id = :cliente ";
		}


		String jpql = ""
				+ "select * from ("
					
					+ "select c.id, v.numerovenda, c.operacao, c.parcela, "
					+ "c.valor, c.saldo, c.vencimento, c.pagamento, c.status, c.tipo, c.empresa_id from contas c "
					+ "join vendas v ON c.codigooperacao = v.numerovenda "
					+ "WHERE c.operacao = 'VENDA' AND c.vencimento BETWEEN :dateStart AND :dateStop "
					+ "and v.empresa_id = c.empresa_id and c.empresa_id = :empresa "
					+ conditionCodigo
					+ conditionOrigemConta
					+ conditionContasPagas
					+ conditionFuncionarioVenda
					
					+ "UNION "
				
					+ "select c.id, cp.numerocompra, c.operacao, c.parcela, "
					+ "c.valor, c.saldo, c.vencimento, c.pagamento, c.status, c.tipo, c.empresa_id from contas c "
					+ "join compras cp ON c.codigooperacao = cp.numerocompra "
					+ "WHERE c.operacao = 'COMPRA' AND c.vencimento BETWEEN :dateStart AND :dateStop "
					+ "and cp.empresa_id = c.empresa_id and c.empresa_id = :empresa "
					+ conditionCodigo
					+ conditionOrigemConta
					+ conditionContasPagas
					+ conditionFuncionarioCompra
					
					+ "UNION "

					+ "select c.id, l.numerolancamento, c.operacao, c.parcela, "
					+ "c.valor, c.saldo, c.vencimento, c.pagamento, c.status, c.tipo, c.empresa_id from contas c "
					+ "join lancamentos l ON c.codigooperacao = l.numerolancamento "
					+ "WHERE c.operacao = 'LANCAMENTO' AND c.vencimento BETWEEN :dateStart AND :dateStop "
					+ "and l.empresa_id = c.empresa_id and c.empresa_id = :empresa "
					+ conditionCodigo
					+ conditionOrigemConta
					+ conditionContasPagas
					+ conditionFuncionarioLancamento
					
				+ ") table_temp order by vencimento asc, id asc";
				
		
		if(cliente != null) {
			if (tipoOperacao != null) {			
				if(tipoOperacao == TipoOperacao.VENDA) {
					jpql = "SELECT c FROM Conta c join Venda v ON v.numeroVenda = c.codigoOperacao " 
							+ "WHERE c.empresa.id = v.empresa.id AND c.empresa.id = :empresa AND c.id > 0 "
							+ "AND c.operacao = 'VENDA' "
							+ "AND c.vencimento BETWEEN :dateStart AND :dateStop " 
							+ conditionCodigo
							+ conditionOrigemConta 
							+ conditionContasPagas
							+ conditionFuncionarioVenda
							+ conditionCliente
							+ "order by c.vencimento ASC, c.id asc";
				} else {
					List<Conta> contas = new ArrayList<>();
					return contas;
				}
			} else {
				jpql = "SELECT c FROM Conta c join Venda v ON v.numeroVenda = c.codigoOperacao " 
						+ "WHERE c.empresa.id = v.empresa.id AND c.empresa.id = :empresa AND c.id > 0 "
						+ "AND c.operacao = 'VENDA' "
						+ "AND c.vencimento BETWEEN :dateStart AND :dateStop " 
						+ conditionCodigo
						+ conditionOrigemConta 
						+ conditionContasPagas
						+ conditionFuncionarioVenda
						+ conditionCliente
						+ "order by c.vencimento ASC, c.id asc";
			}			
		} else {
			
			if (tipoOperacao != null) {			
				if(tipoOperacao == TipoOperacao.VENDA) {
					jpql = "SELECT c FROM Conta c join Venda v ON v.numeroVenda = c.codigoOperacao " 
							+ "WHERE c.empresa.id = v.empresa.id AND c.empresa.id = :empresa AND c.id > 0 "
							+ "AND c.operacao = 'VENDA' "
							+ "AND c.vencimento BETWEEN :dateStart AND :dateStop " 
							+ conditionCodigo
							+ conditionOrigemConta 
							+ conditionContasPagas
							+ conditionFuncionarioVenda	
							+ "order by c.vencimento ASC, c.id asc";
				}
				
				if(tipoOperacao == TipoOperacao.COMPRA) {
					jpql = "SELECT c FROM Conta c join Compra cp ON cp.numeroCompra = c.codigoOperacao " 
							+ "WHERE c.empresa.id = cp.empresa.id AND c.empresa.id = :empresa AND c.id > 0 "
							+ "AND c.operacao = 'COMPRA' "
							+ "AND c.vencimento BETWEEN :dateStart AND :dateStop " 
							+ conditionCodigo
							+ conditionOrigemConta 
							+ conditionContasPagas
							+ conditionFuncionarioCompra
							+ "order by c.vencimento ASC, c.id asc";
				}
				
				if(tipoOperacao == TipoOperacao.LANCAMENTO) {
					jpql = "SELECT c FROM Conta c join Lancamento l ON l.numeroLancamento = c.codigoOperacao " 
							+ "WHERE c.empresa.id = l.empresa.id AND c.empresa.id = :empresa AND c.id > 0 "
							+ "AND c.operacao = 'LANCAMENTO' "
							+ "AND c.vencimento BETWEEN :dateStart AND :dateStop " 
							+ conditionCodigo
							+ conditionOrigemConta 
							+ conditionContasPagas
							+ conditionFuncionarioLancamento
							+ "order by c.vencimento ASC, c.id asc";
				}
				
			} 
		}

		System.out.println(jpql);
		
		Query q = null;		
		if(cliente != null) {
			q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
					.setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
		} else {
		
			if (tipoOperacao != null) {	
				q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
						.setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
			} else {
				 q = manager.createNativeQuery(jpql).setParameter("empresa", empresa.getId())
						.setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);
			}
		}
		
		

		if (codigo != null) {
			q.setParameter("codigo", codigo);
		}

		if (origemConta != null && origemConta.length > 0) {
			q.setParameter("origemConta",
					Arrays.asList(Arrays.toString(origemConta).replace("[", "").replace("]", "").replace(" ", "").trim().split(",")));		
		}
		
		if(funcionario != null) {
			q.setParameter("funcionario", funcionario.getId());
		}
		
		if(cliente != null) {
			q.setParameter("cliente", cliente.getId());
		}
		
		if(cliente != null) {
			return q.getResultList();			
		}
		
		if (tipoOperacao != null) {	
			return q.getResultList();
			
		} else {
				
			List<Conta> contas = new ArrayList<>();
			
			List<Object[]> rows = new ArrayList<>();
			rows = q.getResultList();
			
			for (Object[] objects : rows) {
				Conta conta = new Conta();

				conta.setId(Long.parseLong(objects[0].toString()));
				conta.setCodigoOperacao(Long.parseLong(objects[1].toString()));			
				conta.setOperacao(objects[2].toString());
				conta.setParcela(objects[3].toString());
				conta.setValor(new BigDecimal(objects[4].toString()));	
				conta.setSaldo(new BigDecimal(objects[5].toString()));
				conta.setVencimento((Date) objects[6]);		
				conta.setPagamento((Date) objects[7]);		
				conta.setStatus(objects[8].toString().equals("Y") ? true : false);	
				conta.setTipo(objects[9].toString());
				conta.setEmpresa(empresa);
				
				contas.add(conta);
			}
			
			return contas;
		}
	}

	public Number totalDeReceitasPorDia(Long dia, Long mes, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.dia = :dia AND i.mes = :mes AND i.ano = :ano";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dia", dia).setParameter("mes", mes).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalComprasPorData(Number dia, Number mes, Number ano) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i " + "WHERE i.dia = :dia "
				+ "AND i.mes = :mes " + "AND i.ano = :ano "
				+ "AND i.tipo = 'DEBITO' AND i.operacao = 'COMPRA' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("dia", Long.parseLong(String.valueOf(dia)))
				.setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

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

	public Number totalDespesasPorData(Number dia, Number mes, Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.empresa.id = :empresa AND i.dia = :dia "
				+ "AND i.mes = :mes " + "AND i.ano = :ano "
				+ "AND i.tipo = 'DEBITO' AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dia", Long.parseLong(String.valueOf(dia)))
				.setParameter("mes", Long.parseLong(String.valueOf(mes)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

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

	public Number totalDeReceitasPorSemana(Long semana, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.semana = :semana AND i.ano = :ano AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semana", semana).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalDespesasPorSemana(Number semana, Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.semana, i.ano ";
		orderBy_Condition = "i.semana asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.empresa.id = :empresa AND i.semana = :semana "
				+ "AND i.ano = :ano AND i.tipo = 'DEBITO' AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semana", Long.parseLong(String.valueOf(semana)))
				.setParameter("ano", Long.parseLong(String.valueOf(ano)));

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

	public Number totalDeReceitasPorMes(Long mes, Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.operacao = 'LANCAMENTO' AND i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.mes = :mes AND i.ano = :ano AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mes", mes).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalDespesasPorMes(Number mes, Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.mes, i.ano ";
		orderBy_Condition = "i.mes asc, i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.operacao = 'LANCAMENTO' AND i.empresa.id = :empresa AND i.mes = :mes "
				+ "AND i.ano = :ano AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' "
				+ condition + "group by " + groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mes", Long.parseLong(String.valueOf(mes))).setParameter("ano",
				Long.parseLong(String.valueOf(ano)));

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

	public Number totalDeReceitasPorAno(Long ano, Empresa empresa) {
		String jpql = "SELECT sum(i.valor) FROM Conta i WHERE i.empresa.id = :empresa AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ano = :ano AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("ano", ano);
		Number count = (Number) q.getSingleResult();

		if (count == null) {
			count = 0;
		}

		return count;
	}

	public Number totalDespesasPorAno(Number ano, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.ano ";
		orderBy_Condition = "i.ano asc";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i WHERE i.empresa.id = :empresa AND i.ano = :ano "
				+ "AND i.tipo = 'DEBITO' AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by "
				+ groupBy_Condition + " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("ano", Long.parseLong(String.valueOf(ano)));

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

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.tipo";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());
/*
		List<Object[]> result = q.getResultList();

		for (Object[] object : result) {
			if ((long) object[0] < 10) {
				object[0] = "0" + object[0];
			}

			if ((long) object[1] < 10) {
				object[1] = "0" + object[1];
			}
		}
*/
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorSemana(String ano, String semana01, String semana02,
			boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.semana, i.ano, i.codigoOperacao, i.tipo, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.semana, i.ano, i.codigoOperacao, i.tipo ";
			orderBy_Condition = "i.semana asc, i.ano asc, i.tipo";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.semana = :semanaInicio " + "AND i.ano = :ano "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));

		List<Object[]> result = q.getResultList();

		return result;
	}
	

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorMes(String ano, String mes01, String mes02, boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.mes, i.ano, i.codigoOperacao, i.tipo, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.mes, i.ano, i.codigoOperacao, i.tipo ";
			orderBy_Condition = "i.mes asc, i.ano asc, i.tipo";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.mes BETWEEN :mesInicio AND :mesFim " + "AND i.ano = :ano "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02)).setParameter("ano", Long.parseLong(ano));

		List<Object[]> result = q.getResultList();

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> totalLancamentosPorAno(String ano01, String ano02, boolean chartCondition, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		if (chartCondition != false) {
			select_Condition = "i.ano, i.codigoOperacao, i.tipo, ";
			sum_Condition = "sum(i.valor)";
			groupBy_Condition = "i.ano, i.codigoOperacao, i.tipo";
			orderBy_Condition = "i.ano asc, i.tipo";
		} else {
			select_Condition = "i.categoriaLancamento.tipoLancamento.descricao, ";
			sum_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao)";
			groupBy_Condition = "i.categoriaLancamento.tipoLancamento.descricao ";
			orderBy_Condition = "count(i.categoriaLancamento.tipoLancamento.descricao) asc";
		}

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i " 
				+ "WHERE i.empresa.id = :empresa AND i.ano = :anoInicio "
				+ "AND i.operacao = 'LANCAMENTO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'  " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("anoInicio", Long.parseLong(ano01));

		List<Object[]> result = q.getResultList();

		return result;
	}
	
	
	public Number totalDespesasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	
	public Number totalComprasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		//AND i.operacao = 'LANCAMENTO' 
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'COMPRA' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	
	public Number totalPagamentosParcialComprasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		//AND i.operacao = 'LANCAMENTO' 
		String jpql = "SELECT sum(i.valorPago) FROM PagamentoConta i "
				+ "WHERE i.conta.empresa.id = :empresa AND i.conta.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.conta.operacao = 'COMPRA' AND i.conta.tipo = 'DEBITO' AND i.conta.status = 'N' AND i.conta.ajuste = 'N' and i.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	public List<Conta> contasRecebidasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND (i.operacao = 'LANCAMENTO' OR i.operacao = 'VENDA') AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND (i.operacao = 'LANCAMENTO' OR i.operacao = 'COMPRA') AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	
	public Number totalVendasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		//AND i.operacao = 'LANCAMENTO' 
		/*String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		*/
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalVendasPagasParcialmente(Empresa empresa, Calendar calendarStart, Calendar calendarStop, Conta conta) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String filtroConta = "";
		if(conta != null) {
			filtroConta = " AND c.conta.id = :contaId";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo + filtroConta;
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
	
	public Number totalVendasPagasParcialmente_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo;
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
	
	public Number totalComprasPagasParcialmente(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'COMPRA' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo;
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

	public Number totalComprasPagasParcialmente_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'COMPRA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo;
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
	
	public Number totalDespesasPagasParcialmente(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo;
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
	
	public Number totalDespesasPagasParcialmente_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo;
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
	
	public Number totalReceitasPagasParcialmente(Empresa empresa, Calendar calendarStart, Calendar calendarStop, Conta conta) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String filtroConta = "";
		if(conta != null) {
			filtroConta = " AND c.conta.id = :contaId";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo + filtroConta;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	
	public Number totalReceitasPagasParcialmente_(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo;
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

	public Number totalContasReceitasPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, Conta conta) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String filtroConta = "";
		if(conta != null) {
			filtroConta = " AND c.conta.id = :contaId";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo + filtroConta;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	
	public Number totalContasComprasPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, Conta conta) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String filtroConta = "";
		if(conta != null) {
			filtroConta = " AND c.conta.id = :contaId";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'COMPRA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo + filtroConta;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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

	public Number totalContasVendasPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, Conta conta) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String filtroConta = "";
		if(conta != null) {
			filtroConta = " AND c.conta.id = :contaId";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo + filtroConta;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	public List<Object[]> totalContasVendasPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String jpql = "SELECT c.conta.id, sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo
				+ "GROUP BY c.conta.id";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucroContasVendasPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String jpql = "SELECT c.conta.id, ((sum(c.valorPago)*100/c.conta.valor)/100)*c.conta.lucro, c.conta.custoMedio, c.conta.valor, c.conta.taxaEntrega FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo
				+ "GROUP BY c.conta.id, c.conta.custoMedio, c.conta.valor, c.conta.taxaEntrega, c.conta.lucro";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucroContasVendasPagas_Semanal(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT c.conta.id, ((sum(c.valorPago)*100/c.conta.valor)/100)*c.conta.lucro, c.conta.custoMedio, c.conta.valor, c.conta.taxaEntrega FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " 
				+ "AND c.semanaPagamento BETWEEN :semanaInicio AND :semanaFim AND c.anoPagamento = :ano "
				+ "GROUP BY c.conta.id, c.conta.custoMedio, c.conta.valor, c.conta.taxaEntrega, c.conta.lucro";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));

		return q.getResultList();
	} 
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucroContasVendasPagas_Anual(String ano, Empresa empresa) {
		
		String jpql = "SELECT c.conta.id, ((sum(c.valorPago)*100/c.conta.valor)/100)*c.conta.lucro, c.conta.custoMedio, c.conta.valor, c.conta.taxaEntrega FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " 
				+ "AND c.anoPagamento = :ano "
				+ "GROUP BY c.conta.id, c.conta.custoMedio, c.conta.valor, c.conta.taxaEntrega, c.conta.lucro";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalContasVendasPagas_Semanal(String ano, String semana01, String semana02, Empresa empresa) {	
		
		String jpql = "SELECT c.conta.id, sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' "
				+ "AND c.semanaPagamento BETWEEN :semanaInicio AND :semanaFim AND c.anoPagamento = :ano "
				+ "GROUP BY c.conta.id";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
	
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalContasVendasPagas_Mensal(String ano, String mes01, String mes02, Empresa empresa) {	
		
		String jpql = "SELECT c.conta.id, sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' "
				+ "AND c.mesPagamento BETWEEN :mesInicio AND :mesFim AND c.anoPagamento = :ano "
				+ "GROUP BY c.conta.id";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02))
				.setParameter("ano", Long.parseLong(ano));
	
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalContasVendasPagas_Anual(String ano01, String ano02, Empresa empresa) {	
		
		String jpql = "SELECT c.conta.id, sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.operacao = 'VENDA' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' "
				+ "AND c.anoPagamento BETWEEN :anoInicio AND :anoFim "
				+ "GROUP BY c.conta.id";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("anoInicio", Long.parseLong(ano01))
				.setParameter("anoFim", Long.parseLong(ano02));
	
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalEntradaVendasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo = "AND i.pagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String jpql = "SELECT i.id, sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa " + periodo
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucroEntradaVendasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo = "AND i.pagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String jpql = "SELECT i.id, sum(i.lucro), i.custoMedio, i.valor, i.taxaEntrega FROM Conta i "
				+ "WHERE i.empresa.id = :empresa " + periodo
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id, i.custoMedio, i.valor, i.taxaEntrega";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucroEntradaVendasPagasPorSemanaValor(String ano, String semana01, String semana02, Empresa empresa) {
		
		String jpql = "SELECT i.id, sum(i.lucro), i.custoMedio, i.valor, i.taxaEntrega FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.semana BETWEEN :semanaInicio AND :semanaFim AND i.ano = :ano "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id, i.custoMedio, i.valor, i.taxaEntrega";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalLucroEntradaVendasPagasPorAnoValor(String ano, Empresa empresa) {
		
		String jpql = "SELECT i.id, sum(i.lucro), i.custoMedio, i.valor, i.taxaEntrega FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.ano = :ano "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id, i.custoMedio, i.valor, i.taxaEntrega";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("ano", Long.parseLong(ano));
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalEntradaVendasPagasPorSemanaValor(String ano, String semana01, String semana02, Empresa empresa) {		 
		
		String jpql = "SELECT i.id, sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.semana BETWEEN :semanaInicio AND :semanaFim AND i.ano = :ano "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' "
				+ "AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("semanaInicio", Long.parseLong(semana01.replace("W", "")))
				.setParameter("semanaFim", Long.parseLong(semana02.replace("W", "")))
				.setParameter("ano", Long.parseLong(ano));
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalEntradaVendasPagasPorMesValor(String ano, String mes01, String mes02, Empresa empresa) {		 
		
		String jpql = "SELECT i.id, sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.mes BETWEEN :mesInicio AND :mesFim AND i.ano = :ano "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' "
				+ "AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("mesInicio", Long.parseLong(mes01))
				.setParameter("mesFim", Long.parseLong(mes02))
				.setParameter("ano", Long.parseLong(ano));
		
		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalEntradaVendasPagasPorAnoValor(String ano01, String ano02, Empresa empresa) {		 
		
		String jpql = "SELECT i.id, sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.ano BETWEEN :anoInicio AND :anoFim "
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' "
				+ "AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' "
				+ "GROUP BY i.id";
		
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("anoInicio", Long.parseLong(ano01))
				.setParameter("anoFim", Long.parseLong(ano02));
		
		return q.getResultList();
	}
	
	public Number totalContasDespesasPagas(Empresa empresa, Calendar calendarStart, Calendar calendarStop, Conta conta) {
		
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo += "AND c.dataPagamento BETWEEN :dataInicio AND :dataFim";
		}
		
		String filtroConta = "";
		if(conta != null) {
			filtroConta = " AND c.conta.id = :contaId";
		}
		
		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.operacao = 'LANCAMENTO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' " + periodo + filtroConta;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());
	
		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	public List<BigInteger> codigoOperacoesCredito(Empresa empresa, String operacao) {
			
		String jpql = "SELECT i.codigoOperacao FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.operacao = :operacao and i.parcela = 'AVISTA' and i.tipo = 'CREDITO' group by i.codigoOperacao order by i.codigoOperacao";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("operacao", operacao);

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<BigInteger> codigoOperacoesDebito(Empresa empresa, String operacao) {
			
		String jpql = "SELECT i.codigoOperacao FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.operacao = :operacao and i.parcela = 'AVISTA' and i.tipo = 'DEBITO' group by i.codigoOperacao order by i.codigoOperacao";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("operacao", operacao);

		return q.getResultList();
	}
	
	public Number totalReceitasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalEntradaVendasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa, Conta conta) {
		 
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo = "AND i.pagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String contaParametro = "";
		if(conta != null) {
			contaParametro = "AND i.id = :contaId";
		}
		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa " + periodo
				+ "AND i.operacao = 'VENDA' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + contaParametro;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	
	
	public Number totalEntradaComprasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa, Conta conta) {
		 
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo = "AND i.pagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String contaParametro = "";
		if(conta != null) {
			contaParametro = "AND i.id = :contaId";
		}
		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa " + periodo
				+ "AND i.operacao = 'COMPRA' AND i.tipo = 'DEBITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + contaParametro;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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

	public Number totalEntradaReceitasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa, Conta conta) {
		 
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo = "AND i.pagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String contaParametro = "";
		if(conta != null) {
			contaParametro = "AND i.id = :contaId";
		}
		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa " + periodo
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'CREDITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + contaParametro;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	
	public Number totalEntradaDespesasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa, Conta conta) {
		 
		String periodo = "";
		if(calendarStart != null && calendarStop != null) {
			periodo = "AND i.pagamento BETWEEN :dataInicio AND :dataFim ";
		}
		
		String contaParametro = "";
		if(conta != null) {
			contaParametro = "AND i.id = :contaId";
		}
		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa " + periodo
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.parcela = 'Entrada' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + contaParametro;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		if(calendarStart != null && calendarStop != null) {
			q.setParameter("dataInicio", calendarStart.getTime());
			q.setParameter("dataFim", calendarStop.getTime());
		}
		
		if(conta != null) {
			q.setParameter("contaId", conta.getId());
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
	
	public Number totalAReceberValor(Empresa empresa) {

		String jpql = "SELECT sum(i.saldo) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
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
	
	public Number totalAReceberPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAReceberEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	public List<Conta> contasAReceberEmAtraso(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	public Number totalDespesasPagasPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		//String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
		//		+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
		//		+ "AND c.conta.operacao = 'LANCAMENTO' AND c.conta.tipo = 'DEBITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";		
		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	
	public Number totalAPagarPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAPagarPorDiaValor_(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valorPago) FROM PagamentoConta i "
				+ "WHERE i.conta.empresa.id = :empresa AND i.conta.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.conta.tipo = 'DEBITO' AND i.conta.status = 'N' AND i.conta.ajuste = 'N' and i.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAReceberPorDiaValor_(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String jpql = "SELECT sum(i.valorPago) FROM PagamentoConta i "
				+ "WHERE i.conta.empresa.id = :empresa AND i.conta.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.conta.tipo = 'CREDITO' AND i.conta.status = 'N' AND i.conta.ajuste = 'N' and i.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

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
	
	public Number totalAPagarValor(Empresa empresa) {

		String jpql = "SELECT sum(i.saldo) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
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
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAPagar(Empresa empresa) {

		String jpql = "FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAPagarPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAReceber(Empresa empresa) {

		String jpql = "FROM Conta i "
				+ "WHERE i.empresa.id = :empresa "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAReceberPorDiaValor(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {
		 
		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.tipo = 'CREDITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		List<Conta> contas = q.getResultList();
		
		return contas;
	}
	
	@SuppressWarnings("unchecked")
	public List<Conta> contasAPagarEmAtraso(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT i FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N' order by i.vencimento ASC, i.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

		List<Conta> contas = q.getResultList();

		return contas;
	}
	
	public Number totalAPagarEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(i.valor) FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.vencimento < :dataInicio "
				+ "AND i.tipo = 'DEBITO' AND i.status = 'N' AND i.ajuste = 'N' and i.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	
	public Number totalPagoContasAPagarEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.vencimento < :dataInicio "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	
	public Number totalPagoContasAReceberEmAtrasoValor(Empresa empresa, Calendar calendarStart) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.vencimento < :dataInicio "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime());

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
	public List<PagamentoConta> pagamentosHojeContasAPagar(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAPagarPorConta(Empresa empresa, Conta conta, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.id = :contaId AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("contaId", conta.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAReceberPorConta(Empresa empresa, Conta conta, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.conta.id = :contaId AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("contaId", conta.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<PagamentoConta> pagamentosHojeContasAReceber(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {
		// AND c.conta.status = 'N'
		String jpql = "FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N' order by c.id asc";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

		return q.getResultList();
	}
	
	public Number totalPagoHojeContasAPagarEmAtrasoValor(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'DEBITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

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
	
	public Number totalPagoHojeContasAReceberEmAtrasoValor(Empresa empresa, Calendar calendarStart, Calendar calendarStop) {

		String jpql = "SELECT sum(c.valorPago) FROM PagamentoConta c "
				+ "WHERE c.conta.empresa.id = :empresa AND c.dataPagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND c.conta.tipo = 'CREDITO' AND c.conta.status = 'N' AND c.conta.ajuste = 'N' and c.conta.exclusao = 'N'";
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId())
				.setParameter("dataInicio", calendarStart.getTime())
				.setParameter("dataFim", calendarStop.getTime());

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
	public List<Object[]> totalDespesasPagasPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.tipo";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'DEBITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> totalReceitasPagasPorData(Calendar calendarStart, Calendar calendarStop, Empresa empresa) {

		String condition = "";
		String select_Condition = "";
		String sum_Condition = "";
		String groupBy_Condition = "";
		String orderBy_Condition = "";

		select_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo, ";
		sum_Condition = "sum(i.valor)";
		groupBy_Condition = "i.dia, i.mes, i.ano, i.codigoOperacao, i.tipo ";
		orderBy_Condition = "i.dia asc, i.mes asc, i.ano asc, i.tipo";

		String jpql = "SELECT " + select_Condition + sum_Condition + " FROM Conta i "
				+ "WHERE i.empresa.id = :empresa AND i.pagamento BETWEEN :dataInicio AND :dataFim "
				+ "AND i.operacao = 'LANCAMENTO' AND i.tipo = 'CREDITO' AND i.status = 'Y' AND i.ajuste = 'N' and i.exclusao = 'N' " + condition + "group by " + groupBy_Condition
				+ " order by " + orderBy_Condition;
		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dataInicio", calendarStart.getTime()).setParameter("dataFim",
				calendarStop.getTime());

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> extratoMovimentacoes(String dateStart, String dateStop, Empresa empresa) {
		
		String query = ""
				+ "select \"Código\" \"Cód. Lançamento\", \"Operação\", \"Data Operação\", \"Valor Total\", \"Tipo\", to_char(\"Data Pagto\", 'DD-MM-YYYY HH24:MI:SS') \"Data/Hora Pagto\", \"Pago Nesta Data\" \"Valor Pago Nesta Data\", \"Saldo\" from ("
				+ "		\r\n"
				+ "	(select cp.numerocompra \"Código\", to_char(cp.datacompra, 'DD-MM-YYYY') \"Data Operação\", cp.valortotal \"Valor Total\", \r\n"
				+ "		ct.operacao \"Operação\", pc.datapagamento \"Data Pagto\", 'D' \"Tipo\", pc.valorpago \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		compras cp left join contas ct ON cp.numerocompra = ct.codigooperacao\r\n"
				+ "		join pagamentos_conta pc ON ct.id = pc.conta_id\r\n"
				+ "	where \r\n"
				+ "		pc.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ " 	and ct.operacao = 'COMPRA"	
				+ "	order by pc.datapagamento asc, cp.numerocompra asc)\r\n"
				+ "		\r\n"
				+ "	UNION\r\n"
				+ "		\r\n"
				+ "	(select cp.numerocompra \"Código\", to_char(cp.datacompra, 'DD-MM-YYYY') \"Data Operação\", cp.valortotal \"Valor Total\", \r\n"
				+ "		ct.operacao \"Operação\", ct.pagamento \"Data Pagto\", 'D' \"Tipo\", ct.valor \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		compras cp left join contas ct ON cp.numerocompra = ct.codigooperacao\r\n"
				+ "	where\r\n"
				+ "	 	ct.parcela = 'Entrada' and\r\n"
				+ "		cp.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ "	order by ct.pagamento asc, cp.numerocompra asc)\r\n"
				+ "	\r\n"
				+ "	UNION\r\n"
				+ "	\r\n"
				+ "	(select cp.numerocompra \"Código\", to_char(cp.datacompra, 'DD-MM-YYYY') \"Data Operação\", cp.valortotal \"Valor Total\", \r\n"
				+ "		'COMPRA' \"Operação\", cp.datapagamento \"Data Pagto\", 'D' \"Tipo\", cp.valortotal \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		compras cp \r\n"
				+ "	where\r\n"
				+ "		not exists(select 1 from contas where codigooperacao = cp.numerocompra)\r\n"
				+ "		and cp.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ "\r\n"
				+ "	order by cp.numerocompra asc)\r\n"
				+ "	--) as tbl order by \"Data Pagto\";\r\n"
				+ "	\r\n"
				+ "	\r\n"
				+ "	\r\n"
				+ "	UNION\r\n"
				+ "	\r\n"
				+ "	\r\n"
				+ "	\r\n"
				+ "	--select * from (\r\n"
				+ "	(select cp.numerovenda \"Código\", to_char(cp.datavenda, 'DD-MM-YYYY') \"Data Operação\", cp.valortotal \"Valor Total\", \r\n"
				+ "		ct.operacao \"Operação\", pc.datapagamento \"Data Pagto\", 'C' \"Tipo\", pc.valorpago \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		vendas cp left join contas ct ON cp.numerovenda = ct.codigooperacao\r\n"
				+ "		join pagamentos_conta pc ON ct.id = pc.conta_id\r\n"
				+ "	where \r\n"
				+ "		pc.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"
				+ " 	and ct.operacao = 'VENDA"	
				+ "	order by pc.datapagamento asc, cp.numerovenda asc)\r\n"
				+ "		\r\n"
				+ "	UNION\r\n"
				+ "		\r\n"
				+ "	(select cp.numerovenda \"Código\", to_char(cp.datavenda, 'DD-MM-YYYY') \"Data Operação\", cp.valortotal \"Valor Total\", \r\n"
				+ "		ct.operacao \"Operação\", ct.pagamento \"Data Pagto\", 'C' \"Tipo\", ct.valor \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		vendas cp left join contas ct ON cp.numerovenda = ct.codigooperacao\r\n"
				+ "	where\r\n"
				+ "	 	ct.parcela = 'Entrada' and\r\n"
				+ "		cp.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ "	order by ct.pagamento asc, cp.numerovenda asc)\r\n"
				+ "	\r\n"
				+ "	UNION\r\n"
				+ "	\r\n"
				+ "	(select cp.numerovenda \"Código\", to_char(cp.datavenda, 'DD-MM-YYYY') \"Data Operação\", cp.valortotal \"Valor Total\", \r\n"
				+ "		'VENDA' \"Operação\", cp.datapagamento \"Data Pagto\", 'C' \"Tipo\", cp.valortotal \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		vendas cp \r\n"
				+ "	where\r\n"
				+ "		not exists(select 1 from contas where codigooperacao = cp.numerovenda)\r\n"
				+ "		and cp.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ "\r\n"
				+ "	order by cp.numerovenda asc)\r\n"
				+ "		\r\n"
				+ "		\r\n"
				+ "		\r\n"
				+ "	UNION\r\n"
				+ "		\r\n"
				+ "		\r\n"
				+ "	\r\n"
				+ "	(select cp.numeroLancamento \"Código\", to_char(cp.dataLancamento, 'DD-MM-YYYY') \"Data Operação\", cp.valor \"Valor Total\", \r\n"
				+ "		case ct.tipo when 'DEBITO' then 'DESPESA' else 'RECEITA' end \"Operação\", pc.datapagamento \"Data Pagto\", case ct.tipo when 'DEBITO' then 'D' else 'C' end,\r\n"
				+ "	 	pc.valorpago \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		lancamentos cp left join contas ct ON cp.numeroLancamento = ct.codigooperacao\r\n"
				+ "		join pagamentos_conta pc ON ct.id = pc.conta_id\r\n"
				+ "	where \r\n"
				+ "		pc.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ " 	and ct.operacao = 'LANCAMENTO"	
				+ "	order by pc.datapagamento asc, cp.numeroLancamento asc)\r\n"
				+ "		\r\n"
				+ "	UNION\r\n"
				+ "		\r\n"
				+ "	(select cp.numeroLancamento \"Código\", to_char(cp.dataLancamento, 'DD-MM-YYYY') \"Data Operação\", cp.valor \"Valor Total\", \r\n"
				+ "		case ct.tipo when 'DEBITO' then 'DESPESA' else 'RECEITA' end \"Operação\", ct.pagamento \"Data Pagto\", case ct.tipo when 'DEBITO' then 'D' else 'C' end, \r\n"
				+ "	 	ct.valor \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		lancamentos cp left join contas ct ON cp.numeroLancamento = ct.codigooperacao\r\n"
				+ "	where\r\n"
				+ "	 	ct.parcela = 'Entrada' and\r\n"
				+ "		cp.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ "	order by ct.pagamento asc, cp.numeroLancamento asc)\r\n"
				+ "	\r\n"
				+ "	UNION\r\n"
				+ "	\r\n"
				+ "	(select cp.numeroLancamento \"Código\", to_char(cp.dataLancamento, 'DD-MM-YYYY') \"Data Operação\", cp.valor \"Valor Total\", \r\n"
				+ "		case tl.origem when 0 then 'DESPESA' else 'RECEITA' end \"Operação\", cp.datapagamento \"Data Pagto\", case tl.origem when 0 then 'D' else 'C' end, \r\n"
				+ "	 	cp.valor \"Pago Nesta Data\", 0 \"Saldo\"\r\n"
				+ "	from \r\n"
				+ "		lancamentos cp, categoria_lancamentos cl, tipos_lancamentos tl\r\n"
				+ "	where\r\n"
				+ "	 	cp.categorialancamento_id = cl.id\r\n"
				+ "	 	and cl.tipolancamento_id = tl.id\r\n"
				+ "	 	and not exists(select 1 from contas where codigooperacao = cp.numeroLancamento)\r\n"
				+ "		and cp.datapagamento between '" + dateStart + "' and '" + dateStop + "'\r\n"
				+ "		and cp.empresa_id = " + empresa.getId() + "\r\n"	
				+ "	order by cp.numeroLancamento asc)\r\n"
				+ "	--) as tbl order by \"Data Pagto\";\r\n"
				+ "		\r\n"
				+ "	) as tbl order by \"Data Pagto\";"
				+ "";
		
		Query q = manager.createNativeQuery(query);
		
		List<Object[]> result = q.getResultList();

		return result;
	}
}