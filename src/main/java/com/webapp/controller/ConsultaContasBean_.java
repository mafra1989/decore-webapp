package com.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.google.common.io.ByteSource;
import com.webapp.model.Cliente;
import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.Entrega;
import com.webapp.model.EspelhoVenda;
import com.webapp.model.FormaPagamento;
import com.webapp.model.ItemEspelhoVendaPagamento;
import com.webapp.model.ItemEspelhoVendaPagamentos;
import com.webapp.model.ItemEspelhoVendaParcelamentos;
import com.webapp.model.ItemEspelhoVendaProdutos;
import com.webapp.model.ItemVenda;
import com.webapp.model.Lancamento;
import com.webapp.model.Log;
import com.webapp.model.OrigemConta;
import com.webapp.model.Pagamento;
import com.webapp.model.PagamentoConta;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoConta;
import com.webapp.model.TipoOperacao;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Clientes;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas_;
import com.webapp.repository.Entregas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.Pagamentos;
import com.webapp.repository.PagamentosContas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class ConsultaContasBean_ implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Contas_ contas;
	
	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private Compras compras;
	
	@Inject
	private Vendas vendas;
	
	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalContas = "0,00";

	private Long codigo;

	private Date vencimento = new Date();

	private boolean contasPagas;

	private OrigemConta[] origemConta;

	private TipoOperacao tipoOperacao;

	private List<Conta> contasFiltradas = new ArrayList<>();

	private Conta contaSelecionada;
	
	private Conta contaSelecionada_;

	private Date mes = new Date();
	
	@Inject
	private Logs logs;
	
	private Date dateStart = new Date();
	
	private Date dateStop = new Date();
	
	private String hoje;
	
	
	private Number totalAPagarValor;
	
	private Number totalAReceberValor;
	
	private Number totalAPagarEmAtrasoValor;
	
	private Number totalPagoContasAPagarEmAtrasoValor;
	
	private Number totalPagoContasAReceberEmAtrasoValor;
	
	private Number totalAReceberEmAtrasoValor;
	
	
	private Number totalReceitasPagasHojeValor;
	
	private Number totalVendasPagasHojeValor;
	
	private Number totalContasRecebidasHojeValor;
	
	
	private Number totalLancamentosReceitasHojeValor;
	
	private Number totalReceitasHojeValor;
	
	
	private Number totalLancamentosDespesasHojeValor;
	
	private Number totalDespesasHojeValor;
	
	
	private Number totalDespesasPagasHojeValor;
	
	private Number totalComprasPagasHojeValor;
	
	//private Number totalPagamentosParcialComprasHojeValor;
	
	private Number totalContasPagasHojeValor;
	
	private Number totalPagoHojeContasAPagarEmAtrasoValor;
	
	private Number totalPagoHojeContasAReceberEmAtrasoValor;
	
	
	private String tipoContas = "";
	
	private String labelFiltroContas = "";
	
	@Inject
	private Entregas entregas;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private Pagamentos pagamentos;
	
	@Inject
	private Usuario usuarioSelecionado;
	
	@Inject
	private Cliente clienteSelecionado;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private PagamentoConta pagamentoConta;
	
	@Inject
	private PagamentoConta pagamentoSelecionado;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	private List<FormaPagamento> todasFormasPagamentos = new ArrayList<FormaPagamento>();
	
	@Inject
	private PagamentosContas pagamentosContas;
	
	private List<PagamentoConta> pagamentosAdicionados = new ArrayList<PagamentoConta>();
	
	private boolean somenteVendas = false;
	
	private List<Cliente> todosClientes;
	
	private boolean receitas = false;
	
	private boolean filtroTodasContas = false;
	
	private String totalComissoes = "0,00";
	
	private Number totalAPagarHojeValor;
	
	private Number totalAReceberHojeValor;
	
	private String fitrarContasPagas;
	
	@Inject
	private Usuario usuario_;
	
	private List<Cliente> todosClientes_ = new ArrayList<Cliente>();
	
	@Inject
	private Cliente cliente;
	
	@Inject
	private Clientes clientes;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			// todosUsuarios = usuarios.todos();
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	
			usuario = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario.getEmpresa());
			
			todasFormasPagamentos = formasPagamentos.todos(usuario.getEmpresa());
			
			todosClientes_ = clientes.todos(usuario.getEmpresa());
			
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(dateStart);
			calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
			
			totalAPagarValor = contas.totalAPagarValor(usuario.getEmpresa());
			
			totalAReceberValor = contas.totalAReceberValor(usuario.getEmpresa());
			
			totalPagoContasAPagarEmAtrasoValor = contas.totalPagoContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
			totalAPagarEmAtrasoValor = (contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAPagarEmAtrasoValor.doubleValue();
			
			totalPagoContasAReceberEmAtrasoValor = contas.totalPagoContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);			
			totalAReceberEmAtrasoValor = (contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAReceberEmAtrasoValor.doubleValue();
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
			hoje = sdf.format(calendarStart.getTime());
			
			
			Calendar calendarStop = Calendar.getInstance();
			calendarStop.setTime(dateStop);
			calendarStop.add(Calendar.DAY_OF_MONTH, 1);
			calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);
			
			
			Number totalContasVendasPagasParcialmenteDataValor = contas.totalContasVendasPagas(usuario.getEmpresa(), calendarStart, calendarStop, null).doubleValue();					
			totalVendasPagasHojeValor = contas.totalVendasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa()).doubleValue() + totalContasVendasPagasParcialmenteDataValor.doubleValue();
				
			Number totalContasReceitasPagasParcialmenteDataValor = contas.totalContasReceitasPagas(usuario.getEmpresa(), calendarStart, calendarStop, null).doubleValue();			
			totalReceitasPagasHojeValor = contas.totalReceitasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa()).doubleValue() + totalContasReceitasPagasParcialmenteDataValor.doubleValue();	
			
			totalContasRecebidasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalVendasPagasHojeValor.doubleValue();
			
			
	
			//Number totalReceitasPagasParcialmenteHojeValor = contas.totalReceitasPagasParcialmente(usuario.getEmpresa(), null, null, null);
			
			totalLancamentosReceitasHojeValor = lancamentos.totalLancamentosReceitasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());			
			totalReceitasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalLancamentosReceitasHojeValor.doubleValue();// + totalReceitasPagasParcialmenteHojeValor.doubleValue();
			
			
			
			totalComprasPagasHojeValor = contas.totalComprasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());			
			totalDespesasPagasHojeValor = contas.totalDespesasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			
			Number totalDespesasPagasParcialmenteHojeValor = contas.totalDespesasPagasParcialmente(usuario.getEmpresa(), null, null);
			
			totalLancamentosDespesasHojeValor = lancamentos.totalLancamentosDespesasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalDespesasHojeValor = totalDespesasPagasHojeValor.doubleValue() + totalLancamentosDespesasHojeValor.doubleValue() + totalDespesasPagasParcialmenteHojeValor.doubleValue();
			
			
			
			//totalPagamentosParcialComprasHojeValor  = contas.totalPagamentosParcialComprasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());		
			totalPagoHojeContasAPagarEmAtrasoValor = contas.totalPagoHojeContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart, calendarStop);					
			totalContasPagasHojeValor = (totalDespesasPagasHojeValor.doubleValue() + totalComprasPagasHojeValor.doubleValue()) + totalPagoHojeContasAPagarEmAtrasoValor.doubleValue();
			
			
			//totalPagoHojeContasAReceberEmAtrasoValor = contas.totalPagoHojeContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart, calendarStop);					
			totalContasRecebidasHojeValor = (totalVendasPagasHojeValor.doubleValue() + totalReceitasPagasHojeValor.doubleValue());// + totalPagoHojeContasAReceberEmAtrasoValor.doubleValue();
		
		
			Number totalAReceberHojeValorTemp = contas.totalAReceberPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			Number totalAReceberHojeValorPago = contas.totalAReceberPorDiaValor_(calendarStart, calendarStop, usuario.getEmpresa());
			totalAReceberHojeValor = totalAReceberHojeValorTemp.doubleValue() - totalAReceberHojeValorPago.doubleValue();
			
			
			Number totalAPagarHojeValorTemp = contas.totalAPagarPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			Number totalAPagarHojeValorPago = contas.totalAPagarPorDiaValor_(calendarStart, calendarStop, usuario.getEmpresa());
			totalAPagarHojeValor = totalAPagarHojeValorTemp.doubleValue() - totalAPagarHojeValorPago.doubleValue();
		}
	}


	public void pesquisar() {
		
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.set(Calendar.HOUR, 23);
		calendarStop.set(Calendar.MINUTE, 59);
		calendarStop.set(Calendar.SECOND, 59);	

		contasFiltradas = new ArrayList<>();
		contasFiltradas = contas.contasFiltradas(codigo, tipoOperacao, calendarStart.getTime(), calendarStop.getTime(), origemConta,
				fitrarContasPagas, cliente, usuario_, usuario.getEmpresa());

		double totalContasTemp = 0;
		totalContasTemp = getValorTotalAndSettingDescricaoAndVendedor(totalContasTemp);

		totalContas = nf.format(totalContasTemp);
		/*
		 * if(origemConta.length > 0 ) { for (Conta conta : contasFiltradas) {
		 * contas.remove(conta); } }
		 */

		contaSelecionada = null;
	}

	
	private double getValorTotalAndSettingDescricaoAndVendedor(double totalContasTemp) {
		todosClientes = new ArrayList<Cliente>();
		BigDecimal saldoDeComissao = BigDecimal.ZERO;
		
		for (Conta conta : contasFiltradas) {
			totalContasTemp += conta.getValor().doubleValue();
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
				conta.setVendedor(lancamento.getUsuario() != null ? lancamento.getUsuario().getNome() : null);
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
				conta.setVendedor(compra.getUsuario().getNome());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(venda.getObservacao());
				conta.setCliente(venda.getCliente().getNome());
				conta.setVendedor(venda.getUsuario().getNome());
				conta.setTaxaDeComissao(venda.getTaxaDeComissao());
				
				if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
					if(venda.getTaxaDeComissao().doubleValue() > 0) {
						if((conta.getValor().doubleValue() - conta.getSaldo().doubleValue()) > 0) {
							saldoDeComissao = new BigDecimal(saldoDeComissao.doubleValue() + ((venda.getTaxaDeComissao().doubleValue() * (conta.getValor().doubleValue() - conta.getSaldo().doubleValue()))/100));
						}
					}
				} else {
					if(venda.getTaxaDeComissao().doubleValue() > 0) {
						if(conta.getSaldo().doubleValue() > 0) {
							saldoDeComissao = new BigDecimal(saldoDeComissao.doubleValue() + ((venda.getTaxaDeComissao().doubleValue() * conta.getSaldo().doubleValue())/100));
						}
					}
				}
								
				if(!todosClientes.contains(venda.getCliente())) {
					todosClientes.add(venda.getCliente());
				}
			}		
		}
		
		totalComissoes = nf.format(saldoDeComissao);
		
		return totalContasTemp;
	}
	
	
	public void preparaDataVencimento() {
		contaSelecionada.setVencimento(new Date());
	}
	
	public void preparaPagamentoConta() {
		contaSelecionada = contas.porId(contaSelecionada.getId());
		
		pagamentoConta = new PagamentoConta();
		pagamentoConta.setValorPago(contaSelecionada.getSaldo());
		pagamentoConta.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario.getEmpresa()));
		
		if(contaSelecionada.getOperacao().equals("VENDA")) {
			Venda venda = vendas.porNumeroVenda(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
			pagamentoConta.setFormaPagamento(venda.getFormaPagamento());
		}	
		
		pagamentoConta.setConta(contaSelecionada);
		
	}
	
	public void visualizar() {
		ExternalContext ec = FacesContext.getCurrentInstance()
		        .getExternalContext();
		try {
			if(contaSelecionada.getOperacao().equals("VENDA")) {
				Venda venda = vendas.porNumeroVenda(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());				
				ec.redirect(ec.getRequestContextPath()
			            + "/operacoes/RegistroVendas.xhtml?id=" + venda.getId());
			}
			if(contaSelecionada.getOperacao().equals("COMPRA")) {
				Compra compra = compras.porNumeroCompra(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());				
				ec.redirect(ec.getRequestContextPath()
			            + "/operacoes/RegistroCompras.xhtml?id=" + compra.getId());
			}
			if(contaSelecionada.getOperacao().equals("LANCAMENTO")) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());				
				ec.redirect(ec.getRequestContextPath()
			            + "/operacoes/RegistroLancamentos.xhtml?id=" + lancamento.getId());
			}
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}

	public void pagar() {
		contaSelecionada_ = contas.porId(contaSelecionada.getId());
		realizarPagamento();
		
		pesquisar();
		contaSelecionada = contas.porId(contaSelecionada_.getId());

		/*PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Pagamento realizado com sucesso!' });");*/
	}
	
	public void realizarPagamento() {		
		
		if(pagamentoConta.getValorPago().doubleValue() > 0) {
			if(pagamentoConta.getValorPago().doubleValue() == contaSelecionada.getSaldo().doubleValue()) {
	
				Conta conta = contas.porId(contaSelecionada.getId());
		
				conta.setSaldo(new BigDecimal(conta.getSaldo().doubleValue() - pagamentoConta.getValorPago().doubleValue()));
				conta.setPagamento(pagamentoConta.getDataPagamento());
				conta.setStatus(true);
		
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getPagamento());
		
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
		
				conta = contas.save(conta);
						
				
				/* Ajuste informações de pagamento */
				Calendar calendario = Calendar.getInstance();
				calendario.setTime(pagamentoConta.getDataPagamento());
				
				pagamentoConta.setDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
				pagamentoConta.setNomeDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
				pagamentoConta.setSemanaPagamento(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
				pagamentoConta.setMesPagamento(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
				pagamentoConta.setAnoPagamento(Long.valueOf((calendario.get(Calendar.YEAR))));
				/* fim ajuste */

				pagamentosContas.save(pagamentoConta);
							
				RegistrarLog(conta, true);
				
				
				 
				PrimeFaces.current().executeScript(
						"PF('pagamento-dialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Pagamento realizado com sucesso!' });");
				
			} else if(pagamentoConta.getValorPago().doubleValue() < contaSelecionada.getSaldo().doubleValue()) {
				
				Conta conta = contas.porId(contaSelecionada.getId());				
				conta.setSaldo(new BigDecimal(conta.getSaldo().doubleValue() - pagamentoConta.getValorPago().doubleValue()));		
				conta = contas.save(conta);
				
				/* Ajuste informações de pagamento */
				Calendar calendario = Calendar.getInstance();
				calendario.setTime(pagamentoConta.getDataPagamento());
				
				pagamentoConta.setDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
				pagamentoConta.setNomeDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
				pagamentoConta.setSemanaPagamento(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
				pagamentoConta.setMesPagamento(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
				pagamentoConta.setAnoPagamento(Long.valueOf((calendario.get(Calendar.YEAR))));
				/* fim ajuste */
				
				pagamentosContas.save(pagamentoConta);
				
				RegistrarLog(conta, false);				
				
				PrimeFaces.current().executeScript(
						"PF('pagamento-dialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Pagamento realizado com sucesso!' });");
				
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor menor ou igual ao saldo!', timer: 3000 });");
			}
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor maior que R$ 0,00!', timer: 3000 });");
		}
		
	}

	private void RegistrarLog(Conta conta, boolean pagouTudo) {
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(conta.getCodigoOperacao()));

		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		String msg = "";
		if(conta.getOperacao().equals(TipoAtividade.VENDA.name())) {
			msg = "Recebeu venda";
			log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			
			/*if(pagouTudo) {
				if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
					Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
					venda.setVendaPaga(true);
					venda.setConta(false);
					vendas.save(venda);
				}
			}*/	
			
			Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
			if(pagouTudo) {
				venda.setDataPagamento(new Date());
				
				if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
					venda.setConta(false);
					venda.setVendaPaga(true);				
					
				} else {				
					venda.setVendaPaga(true);
					List<Conta> listaDeContas = contas.porCodigoOperacao(conta.getCodigoOperacao(), "VENDA", usuario.getEmpresa());
					for (Conta contaTemp : listaDeContas) {
						if(!contaTemp.isStatus()) {
							venda.setVendaPaga(false);
						}
					}			
				}
				setInformacoesDataPagamento(venda);
				vendas.save(venda);
				
			} else {
				venda.setDataPagamento(new Date());
				setInformacoesDataPagamento(venda);
				vendas.save(venda);
			}
		}
		
		if(conta.getOperacao().equals(TipoAtividade.COMPRA.name())) {
			msg = "Pagou compra";
			log.setOperacao(TipoAtividade.PAGAMENTO.name());
			
			/*if(pagouTudo) {
				if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
					Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
					compra.setCompraPaga(true);
					compra.setConta(false);
					compras.save(compra);
				}
			}*/
			
			Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
			if(pagouTudo) {
				compra.setDataPagamento(new Date());
				
				if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
					compra.setConta(false);
					compra.setCompraPaga(true);				
					
				} else {				
					compra.setCompraPaga(true);
					List<Conta> listaDeContas = contas.porCodigoOperacao(conta.getCodigoOperacao(), "COMPRA", usuario.getEmpresa());
					for (Conta contaTemp : listaDeContas) {
						if(!contaTemp.isStatus()) {
							compra.setCompraPaga(false);
						}
					}			
				}
				
				compras.save(compra);
				
			} else {
				compra.setDataPagamento(new Date());
				compras.save(compra);
			}
		}
		
		if(conta.getOperacao().equals(TipoAtividade.LANCAMENTO.name())) {
			if(conta.getTipo().equals(TipoConta.ARECEBER.name())) {
				msg = "Recebeu receita";
				log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			} else {
				msg = "Pagou despesa";
				log.setOperacao(TipoAtividade.PAGAMENTO.name());
			}
			
			Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
			if(pagouTudo) {
				lancamento.setDataPagamento(new Date());
				
				if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
					lancamento.setConta(false);
					lancamento.setLancamentoPago(true);				
					
				} else {				
					lancamento.setLancamentoPago(true);
					List<Conta> listaDeContas = contas.porCodigoOperacao(conta.getCodigoOperacao(), "LANCAMENTO", usuario.getEmpresa());
					for (Conta contaTemp : listaDeContas) {
						if(!contaTemp.isStatus()) {
							lancamento.setLancamentoPago(false);
						}
					}			
					//lancamentos.save(lancamento);
				}
				
				lancamentos.save(lancamento);
				
			} else {
				lancamento.setDataPagamento(new Date());
				lancamentos.save(lancamento);
			}
		}
		
		log.setDescricao(msg + ", Nº " + conta.getCodigoOperacao() + ", valor total R$ " + nf.format(pagamentoConta.getValorPago()));
		log.setUsuario(usuario);		
		logs.save(log);
	}
	
	private void setInformacoesDataPagamento(Venda venda) {
		
		Calendar calendario = Calendar.getInstance();

		calendario.setTime(venda.getDataPagamento());
		
		venda.setDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
		venda.setNomeDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
		venda.setSemanaPagamento(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
		venda.setMesPagamento(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
		venda.setAnoPagamento(Long.valueOf((calendario.get(Calendar.YEAR))));
	}
	
	public void pagarTelaResumoFinanceiro() {
		
		boolean atualizar = false;
		if(pagamentoConta.getValorPago().doubleValue() > 0) {
			if(pagamentoConta.getValorPago().doubleValue() == contaSelecionada.getSaldo().doubleValue()) {
				atualizar = true;
			} else if(pagamentoConta.getValorPago().doubleValue() < contaSelecionada.getSaldo().doubleValue()) {
				atualizar = true;
			}
		}
		
		realizarPagamento();
		
		if(atualizar) {
			atualizarDadosResumoFinanceiro();
		}

	}

	private void atualizarDadosResumoFinanceiro() {
		
		if(contaSelecionada.getTipo().equals("DEBITO")) {

			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(dateStart);
			calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
			
			//totalAPagarEmAtrasoValor = contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);	
			
			Calendar calendarStop = Calendar.getInstance();
			calendarStop.setTime(dateStop);
			calendarStop.add(Calendar.DAY_OF_MONTH, 1);
			calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);	
			
			if(tipoContas.equals("Contas à pagar em atraso")) {	
				buscarContasAPagarEmAtraso();
				
			}else if(tipoContas.equals("Contas pagas Hoje " + hoje)) {	
				buscarContasPagasHoje();
				totalPagoContasAPagarEmAtrasoValor = contas.totalPagoContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
				totalAPagarEmAtrasoValor = (contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAPagarEmAtrasoValor.doubleValue();				
			
			} else if(tipoContas.equals("Total contas à pagar")) {
				buscarContasAPagar();
				totalPagoContasAReceberEmAtrasoValor = contas.totalPagoContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);
				totalAReceberEmAtrasoValor = (contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAReceberEmAtrasoValor.doubleValue();	
			
				totalPagoContasAPagarEmAtrasoValor = contas.totalPagoContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
				totalAPagarEmAtrasoValor = (contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAPagarEmAtrasoValor.doubleValue();			
			}
			
			totalComprasPagasHojeValor = contas.totalComprasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());								
			totalDespesasPagasHojeValor = contas.totalDespesasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());

			//totalPagamentosParcialComprasHojeValor  = contas.totalPagamentosParcialComprasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			totalPagoHojeContasAPagarEmAtrasoValor = contas.totalPagoHojeContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart, calendarStop);					
			totalContasPagasHojeValor = (totalDespesasPagasHojeValor.doubleValue() + totalComprasPagasHojeValor.doubleValue()) + totalPagoHojeContasAPagarEmAtrasoValor.doubleValue();
			
			Number totalDespesasPagasParcialmenteHojeValor = contas.totalDespesasPagasParcialmente(usuario.getEmpresa(), null, null);
			
			totalLancamentosDespesasHojeValor = lancamentos.totalLancamentosDespesasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());		
			totalDespesasHojeValor = totalDespesasPagasHojeValor.doubleValue() + totalLancamentosDespesasHojeValor.doubleValue() + totalDespesasPagasParcialmenteHojeValor.doubleValue();
			
			Number totalAPagarHojeValorTemp = contas.totalAPagarPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			Number totalAPagarHojeValorPago = contas.totalAPagarPorDiaValor_(calendarStart, calendarStop, usuario.getEmpresa());
			totalAPagarHojeValor = totalAPagarHojeValorTemp.doubleValue() - totalAPagarHojeValorPago.doubleValue();
			
		} else if(contaSelecionada.getTipo().equals("CREDITO")) {
			
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(dateStart);
			calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
			
			Calendar calendarStop = Calendar.getInstance();
			calendarStop.setTime(dateStop);
			calendarStop.add(Calendar.DAY_OF_MONTH, 1);
			calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);	
			
			if(tipoContas.equals("Contas à receber em atraso")) {	
				buscarContasAReceberEmAtraso();
				
			} else if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
				buscarContasRecebidasHoje();				
				totalPagoContasAReceberEmAtrasoValor = contas.totalPagoContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);
				totalAReceberEmAtrasoValor = (contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAReceberEmAtrasoValor.doubleValue();	
			
			} else if(tipoContas.equals("Total contas à receber")) {
				buscarContasAReceber();
				totalPagoContasAReceberEmAtrasoValor = contas.totalPagoContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);
				totalAReceberEmAtrasoValor = (contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAReceberEmAtrasoValor.doubleValue();	
			
				totalPagoContasAPagarEmAtrasoValor = contas.totalPagoContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
				totalAPagarEmAtrasoValor = (contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAPagarEmAtrasoValor.doubleValue();			
			}
			
			Number totalContasVendasPagasParcialmenteDataValor = contas.totalContasVendasPagas(usuario.getEmpresa(), calendarStart, calendarStop, null).doubleValue();					
			totalVendasPagasHojeValor = contas.totalVendasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa()).doubleValue() + totalContasVendasPagasParcialmenteDataValor.doubleValue();		
			//totalVendasPagasHojeValor = contas.totalVendasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());			
			
			//totalReceitasPagasHojeValor = contas.totalReceitasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());			
			Number totalContasReceitasPagasParcialmenteDataValor = contas.totalContasReceitasPagas(usuario.getEmpresa(), calendarStart, calendarStop, null).doubleValue();			
			totalReceitasPagasHojeValor = contas.totalReceitasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa()).doubleValue() + totalContasReceitasPagasParcialmenteDataValor.doubleValue();	
			
			totalPagoHojeContasAReceberEmAtrasoValor = contas.totalPagoHojeContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart, calendarStop);					
			//totalContasRecebidasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalVendasPagasHojeValor.doubleValue() + totalPagoHojeContasAReceberEmAtrasoValor.doubleValue();	
			totalContasRecebidasHojeValor = (totalVendasPagasHojeValor.doubleValue() + totalReceitasPagasHojeValor.doubleValue());// + totalPagoHojeContasAReceberEmAtrasoValor.doubleValue();
			
			//Number totalReceitasPagasParcialmenteHojeValor = contas.totalReceitasPagasParcialmente(usuario.getEmpresa(), null, null, null);
			
			totalLancamentosReceitasHojeValor = lancamentos.totalLancamentosReceitasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());		
			totalReceitasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalLancamentosReceitasHojeValor.doubleValue();// + totalReceitasPagasParcialmenteHojeValor.doubleValue();					
			

			
			Number totalAReceberHojeValorTemp = contas.totalAReceberPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			Number totalAReceberHojeValorPago = contas.totalAReceberPorDiaValor_(calendarStart, calendarStop, usuario.getEmpresa());
			totalAReceberHojeValor = totalAReceberHojeValorTemp.doubleValue() - totalAReceberHojeValorPago.doubleValue();
		}
	}

	public void estornar() {
		contaSelecionada_ = contas.porId(contaSelecionada.getId());
		realizarEstorno();
		
		pesquisar();
		contaSelecionada = contas.porId(contaSelecionada_.getId());
		
		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Pagamento estornado com sucesso!' });");

	}
	
	public void realizarEstorno() {
		
		pagamentosAdicionados.remove(pagamentoSelecionado);
		pagamentosContas.remove(pagamentoSelecionado);
		
		contaSelecionada = contas.porId(contaSelecionada.getId());
		
		if(pagamentosAdicionados.size() == 0) {
			
			contaSelecionada.setSaldo(contaSelecionada.getValor());
			contaSelecionada.setPagamento(null);
			contaSelecionada.setStatus(false);

			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(contaSelecionada.getVencimento());

			contaSelecionada.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			contaSelecionada.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			contaSelecionada.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			contaSelecionada.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			contaSelecionada.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			contas.save(contaSelecionada);
		} else {
			
			contaSelecionada.setPagamento(null);
			contaSelecionada.setStatus(false);
			
			contaSelecionada.setSaldo(new BigDecimal(contaSelecionada.getSaldo().doubleValue() + pagamentoSelecionado.getValorPago().doubleValue()));
			contas.save(contaSelecionada);			
		}
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(contaSelecionada.getCodigoOperacao()));
		
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		String msg = "";
		if(contaSelecionada.getOperacao().equals(TipoAtividade.VENDA.name())) {
			msg = "Desfez Recebimento, venda Nº ";
			log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			
			//if(pagamentosAdicionados.size() == 0) {
				if(contaSelecionada.getParcela().equals(TipoPagamento.AVISTA.name())) {
					Venda venda = vendas.porNumeroVenda(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
					venda.setVendaPaga(false);
					venda.setConta(true);
					
					List<Conta> listaDeContas = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario.getEmpresa());
					
					if(listaDeContas.size() > 0) {
						for (Conta conta : listaDeContas) {
							if(conta.getPagamento() != null) {
								venda.setDataPagamento(conta.getPagamento());
								setInformacoesDataPagamento(venda);
							}
							
							List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
							for (PagamentoConta pagamentoConta : pagamentosContaTemp) {								
								venda.setDataPagamento(pagamentoConta.getDataPagamento());
								setInformacoesDataPagamento(venda);
							}
						}
					}
					
					vendas.save(venda);
				} else {
					Venda venda = vendas.porNumeroVenda(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
					venda.setDataPagamento(null);
					
					venda.setDiaPagamento(null);
					venda.setNomeDiaPagamento(null);
					venda.setSemanaPagamento(null);
					venda.setMesPagamento(null);
					venda.setAnoPagamento(null);
					
					venda.setVendaPaga(false);
					
					List<Conta> listaDeContas = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario.getEmpresa());
					
					if(listaDeContas.size() > 0) {
						for (Conta conta : listaDeContas) {
							if(conta.getPagamento() != null) {
								venda.setDataPagamento(conta.getPagamento());
								setInformacoesDataPagamento(venda);
							}
							
							List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
							for (PagamentoConta pagamentoConta : pagamentosContaTemp) {								
								venda.setDataPagamento(pagamentoConta.getDataPagamento());
								setInformacoesDataPagamento(venda);
							}
						}
					}
					
					vendas.save(venda);
				}
			//}
		}
		
		if(contaSelecionada.getOperacao().equals(TipoAtividade.COMPRA.name())) {
			msg = "Desfez Pagamento, compra Nº ";
			log.setOperacao(TipoAtividade.PAGAMENTO.name());
			
			//if(pagamentosAdicionados.size() == 0) {
				if(contaSelecionada.getParcela().equals(TipoPagamento.AVISTA.name())) {
					Compra compra = compras.porNumeroCompra(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
					compra.setCompraPaga(false);
					compra.setConta(true);
					
					List<Conta> listaDeContas = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA", usuario.getEmpresa());
					
					if(listaDeContas.size() > 0) {
						for (Conta conta : listaDeContas) {
							if(conta.getPagamento() != null) {
								compra.setDataPagamento(conta.getPagamento());
							}
							
							List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
							for (PagamentoConta pagamentoConta : pagamentosContaTemp) {								
								compra.setDataPagamento(pagamentoConta.getDataPagamento());
							}
						}
					}
					
					compras.save(compra);
				} else {
					Compra compra = compras.porNumeroCompra(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
					compra.setDataPagamento(null);
					compra.setCompraPaga(false);
					
					List<Conta> listaDeContas = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA", usuario.getEmpresa());
					
					if(listaDeContas.size() > 0) {
						for (Conta conta : listaDeContas) {
							if(conta.getPagamento() != null) {
								compra.setDataPagamento(conta.getPagamento());
							}
							
							List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
							for (PagamentoConta pagamentoConta : pagamentosContaTemp) {								
								compra.setDataPagamento(pagamentoConta.getDataPagamento());
							}
						}
					}
					
					compras.save(compra);
				}
			//}
		}
		
		if(contaSelecionada.getOperacao().equals(TipoAtividade.LANCAMENTO.name())) {
			if(contaSelecionada.getTipo().equals(TipoConta.ARECEBER.name())) {
				msg = "Desfez Recebimento, receita Nº ";
				log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			} else {
				msg = "Desfez Pagamento, despesa Nº ";
				log.setOperacao(TipoAtividade.PAGAMENTO.name());
			}
			
			if(contaSelecionada.getParcela().equals(TipoPagamento.AVISTA.name())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
				lancamento.setDataPagamento(null);
				lancamento.setLancamentoPago(false);
				//lancamento.setConta(true);	
				
				List<Conta> listaDeContas = contas.porCodigoOperacao(lancamento.getNumeroLancamento(), "LANCAMENTO", usuario.getEmpresa());
				
				if(listaDeContas.size() > 0) {
					for (Conta conta : listaDeContas) {
						if(conta.getPagamento() != null) {
							lancamento.setDataPagamento(conta.getPagamento());
						}
						
						List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
						for (PagamentoConta pagamentoConta : pagamentosContaTemp) {								
							lancamento.setDataPagamento(pagamentoConta.getDataPagamento());
						}
					}
				}
				
				lancamentos.save(lancamento);
			} else {
				Lancamento lancamento = lancamentos.porNumeroLancamento(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
				lancamento.setDataPagamento(null);
				lancamento.setLancamentoPago(false);
				
				List<Conta> listaDeContas = contas.porCodigoOperacao(lancamento.getNumeroLancamento(), "LANCAMENTO", usuario.getEmpresa());
				
				if(listaDeContas.size() > 0) {
					for (Conta conta : listaDeContas) {
						if(conta.getPagamento() != null) {
							lancamento.setDataPagamento(conta.getPagamento());
						}
						
						List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
						for (PagamentoConta pagamentoConta : pagamentosContaTemp) {								
							lancamento.setDataPagamento(pagamentoConta.getDataPagamento());
						}
					}
				}
				
				lancamentos.save(lancamento);
			}
		}
		
		log.setDescricao(msg + contaSelecionada.getCodigoOperacao() + ", valor total R$ " + nf.format(pagamentoSelecionado.getValorPago()));
		log.setUsuario(usuario);		
		logs.save(log);
		
		contaSelecionada.setSaldo(new BigDecimal(contaSelecionada.getSaldo().doubleValue() + pagamentoSelecionado.getValorPago().doubleValue()));
		pagamentoSelecionado = null;
	}
	
	public void estornarTelaResumoFinanceiro() {

		realizarEstorno();
		
		if(contaSelecionada.getTipo().equals("DEBITO")) {
			buscarContasPagasHoje();
			
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(dateStart);
			calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
			
			totalPagoContasAPagarEmAtrasoValor = contas.totalPagoContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
			totalAPagarEmAtrasoValor = (contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAPagarEmAtrasoValor.doubleValue();
			
		} else if(contaSelecionada.getTipo().equals("CREDITO")) {
			buscarContasRecebidasHoje();
			
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(dateStart);
			calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
			
			totalPagoContasAReceberEmAtrasoValor = contas.totalPagoContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);
			totalAReceberEmAtrasoValor = (contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAReceberEmAtrasoValor.doubleValue();
		
		}
		
		PrimeFaces.current().executeScript(
				"PF('pagamentos-dialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Pagamento estornado com sucesso!' });");

	}

	public String nameMes(int mes) {

		switch (mes) {
		case 1:
			return "Janeiro";
		case 2:
			return "Fevereiro";
		case 3:
			return "Março";
		case 4:
			return "Abril";
		case 5:
			return "Maio";
		case 6:
			return "Junho";
		case 7:
			return "Julho";
		case 8:
			return "Agosto";
		case 9:
			return "Setembro";
		case 10:
			return "Outubro";
		case 11:
			return "Novembro";
		case 12:
			return "Dezembro";
		}

		return "";
	}

	public OrigemConta[] getOrigensContas() {
		return OrigemConta.values();
	}

	public List<TipoOperacao> getTiposOperacoes() {
		return com.webapp.model.TipoOperacao.filtrarOperacoesContasPagarReceber();
	}

	public OrigemConta[] getOrigemConta() {
		return origemConta;
	}

	public void setOrigemConta(OrigemConta[] origemConta) {
		this.origemConta = origemConta;
	}

	public String getTotalContas() {
		return totalContas;
	}

	public TipoOperacao getTipoOperacao() {
		return tipoOperacao;
	}

	public void setTipoOperacao(TipoOperacao tipoOperacao) {
		this.tipoOperacao = tipoOperacao;
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public boolean isContasPagas() {
		return contasPagas;
	}

	public void setContasPagas(boolean contasPagas) {
		this.contasPagas = contasPagas;
	}

	public List<Conta> getContasFiltradas() {
		return contasFiltradas;
	}

	public Date getMes() {
		return mes;
	}

	public void setMes(Date mes) {
		this.mes = mes;
	}

	public int getContasFiltradasSize() {
		return contasFiltradas.size();
	}

	public Conta getContaSelecionada() {
		return contaSelecionada;
	}

	public void setContaSelecionada(Conta contaSelecionada) {
		this.contaSelecionada = contaSelecionada;
	}
	
	public Number getTotalAPagarValor() {
		return totalAPagarValor;
	}

	public Number getTotalAReceberValor() {
		return totalAReceberValor;
	}

	public Number getTotalAPagarEmAtrasoValor() {
		return totalAPagarEmAtrasoValor;
	}

	public Number getTotalAReceberEmAtrasoValor() {
		return totalAReceberEmAtrasoValor;
	}

	public Number getTotalContasRecebidasHojeValor() {
		return totalContasRecebidasHojeValor;
	}

	public Number getTotalContasPagasHojeValor() {
		return totalContasPagasHojeValor;
	}

	public String getTipoContas() {
		return tipoContas;
	}

	public String getLabelFiltroContas() {
		return labelFiltroContas;
	}

	public String getHoje() {
		return hoje;
	}

	public void buscarContasAPagarEmAtraso() {
		tipoContas = "Contas à pagar em atraso";
		
		labelFiltroContas = null;
		
		receitas = false;
				
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		contasFiltradas = contas.contasAPagarEmAtraso(usuario.getEmpresa(), calendarStart);		
		
		getValorTotalAndSettingDescricaoAndVendedor(0);
		/*for (Conta conta : contasFiltradas) {
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				//Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao("Cliente: " + venda.getCliente().getNome());
			}
		}*/
		
		totalPagoContasAPagarEmAtrasoValor = contas.totalPagoContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
		totalAPagarEmAtrasoValor = (contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAPagarEmAtrasoValor.doubleValue();
		totalContas = nf.format(totalAPagarEmAtrasoValor);
		
		totalAPagarValor = contas.totalAPagarValor(usuario.getEmpresa());
		
		contaSelecionada = null;
		
		usuarioSelecionado = null;
	}
	
	public void buscarContasAReceberEmAtraso() {
		tipoContas = "Contas à receber em atraso";
		
		labelFiltroContas = null;
		
		somenteVendas = false;
		receitas = true;
				
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		contasFiltradas = contas.contasAReceberEmAtraso(usuario.getEmpresa(), calendarStart);
		
		getValorTotalAndSettingDescricaoAndVendedor(0);
		/*
		for (Conta conta : contasFiltradas) {
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				//Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao("Cliente: " + venda.getCliente().getNome());
			}		
		}*/
		
		totalPagoContasAReceberEmAtrasoValor = contas.totalPagoContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);
		totalAReceberEmAtrasoValor = (contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart)).doubleValue() - totalPagoContasAReceberEmAtrasoValor.doubleValue();
		totalContas = nf.format(totalAReceberEmAtrasoValor);
		
		totalAReceberValor = contas.totalAReceberValor(usuario.getEmpresa());
		
		contaSelecionada = null;
		
		usuarioSelecionado = null;

	}
	

	public void buscarContasRecebidasHoje() {
		tipoContas = "Contas recebidas Hoje " + hoje;
		
		labelFiltroContas = null;
		
		somenteVendas = false;
		receitas = true;
				
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);
		
		Number totalContasVendasPagasParcialmenteDataValor = contas.totalContasVendasPagas(usuario.getEmpresa(), calendarStart, calendarStop, null).doubleValue();					
		totalVendasPagasHojeValor = contas.totalVendasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa()).doubleValue() + totalContasVendasPagasParcialmenteDataValor.doubleValue();
		
		//totalVendasPagasHojeValor = contas.totalVendasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());	
		//totalReceitasPagasHojeValor = contas.totalReceitasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		
		Number totalContasReceitasPagasParcialmenteDataValor = contas.totalContasReceitasPagas(usuario.getEmpresa(), calendarStart, calendarStop, null).doubleValue();			
		totalReceitasPagasHojeValor = contas.totalReceitasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa()).doubleValue() + totalContasReceitasPagasParcialmenteDataValor.doubleValue();	
		
		totalPagoHojeContasAReceberEmAtrasoValor = contas.totalPagoHojeContasAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart, calendarStop);						
		//totalContasRecebidasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalVendasPagasHojeValor.doubleValue() + totalPagoHojeContasAReceberEmAtrasoValor.doubleValue();
		totalContasRecebidasHojeValor = (totalVendasPagasHojeValor.doubleValue() + totalReceitasPagasHojeValor.doubleValue());// + totalPagoHojeContasAReceberEmAtrasoValor.doubleValue();
		
		//Number totalReceitasPagasParcialmenteHojeValor = contas.totalReceitasPagasParcialmente(usuario.getEmpresa(), null, null, null);	
		totalLancamentosReceitasHojeValor = lancamentos.totalLancamentosReceitasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());		
		//totalReceitasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalLancamentosReceitasHojeValor.doubleValue() + totalReceitasPagasParcialmenteHojeValor.doubleValue();
		totalReceitasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalLancamentosReceitasHojeValor.doubleValue();// + totalReceitasPagasParcialmenteHojeValor.doubleValue();

		
		Number totalAReceberHojeValorTemp = contas.totalAReceberPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		Number totalAReceberHojeValorPago = contas.totalAReceberPorDiaValor_(calendarStart, calendarStop, usuario.getEmpresa());
		totalAReceberHojeValor = totalAReceberHojeValorTemp.doubleValue() - totalAReceberHojeValorPago.doubleValue();
		
		
		contasFiltradas = new ArrayList<Conta>();
		contasFiltradas = contas.contasRecebidasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		
		List<PagamentoConta> listaPagamentosHeoje = contas.pagamentosHojeContasAReceber(usuario.getEmpresa(), calendarStart, calendarStop);
		for (PagamentoConta pagamentoConta : listaPagamentosHeoje) {
			Conta conta = pagamentoConta.getConta();
			
			if(!contasFiltradas.contains(conta)) {
				contasFiltradas.add(conta);
			}
		}
		
		getValorTotalAndSettingDescricaoAndVendedor(0);
		/*
		for (Conta conta : contasFiltradas) {
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				//Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao("Cliente: " + venda.getCliente().getNome());
			}		
		}
		*/
		
		totalContas = nf.format(totalContasRecebidasHojeValor);
		
		totalAReceberValor = contas.totalAReceberValor(usuario.getEmpresa());
					
		contaSelecionada = null;
		
		usuarioSelecionado = null;
		
	}
	
	
	public void buscarContasAReceber() {
		tipoContas = "Total contas à receber";
		
		filtroTodasContas = false;
		labelFiltroContas = "Mostrar contas à receber";
		
		somenteVendas = false;
		receitas = true;
	
		contasFiltradas = contas.contasAReceber(usuario.getEmpresa());
		
		getValorTotalAndSettingDescricaoAndVendedor(0);
		
		totalAReceberValor = contas.totalAReceberValor(usuario.getEmpresa());
		
		totalContas = nf.format(totalAReceberValor);
					
		contaSelecionada = null;
		
		usuarioSelecionado = null;
	}
	
	public void buscarContasAPagar() {
		tipoContas = "Total contas à pagar";
		
		filtroTodasContas = false;
		labelFiltroContas = "Mostrar contas à pagar";
		
		receitas = false;
	
		contasFiltradas = contas.contasAPagar(usuario.getEmpresa());
		
		getValorTotalAndSettingDescricaoAndVendedor(0);
		
		totalAPagarValor = contas.totalAPagarValor(usuario.getEmpresa());
		
		totalContas = nf.format(totalAPagarValor);
					
		contaSelecionada = null;
		
		usuarioSelecionado = null;
	}
	
	
	public void buscarContasPagasHoje() {
		tipoContas = "Contas pagas Hoje " + hoje;
		
		labelFiltroContas = null;
		
		receitas = false;
				
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);
		
		
		totalComprasPagasHojeValor = contas.totalComprasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());		
		totalDespesasPagasHojeValor = contas.totalDespesasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		
		Number totalDespesasPagasParcialmenteHojeValor = contas.totalDespesasPagasParcialmente(usuario.getEmpresa(), null, null);
		
		totalLancamentosDespesasHojeValor = lancamentos.totalLancamentosDespesasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());		
		totalDespesasHojeValor = totalDespesasPagasHojeValor.doubleValue() + totalLancamentosDespesasHojeValor.doubleValue() + totalDespesasPagasParcialmenteHojeValor.doubleValue();
		
		totalPagoHojeContasAPagarEmAtrasoValor = contas.totalPagoHojeContasAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart, calendarStop);		
		
		totalContasPagasHojeValor = totalDespesasPagasHojeValor.doubleValue() + totalComprasPagasHojeValor.doubleValue() + totalPagoHojeContasAPagarEmAtrasoValor.doubleValue();
		
		
		Number totalAPagarHojeValorTemp = contas.totalAPagarPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		Number totalAPagarHojeValorPago = contas.totalAPagarPorDiaValor_(calendarStart, calendarStop, usuario.getEmpresa());
		totalAPagarHojeValor = totalAPagarHojeValorTemp.doubleValue() - totalAPagarHojeValorPago.doubleValue();
		
		
		contasFiltradas = new ArrayList<Conta>();
		contasFiltradas = contas.contasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		
		List<PagamentoConta> listaPagamentosHeoje = contas.pagamentosHojeContasAPagar(usuario.getEmpresa(), calendarStart, calendarStop);
		for (PagamentoConta pagamentoConta : listaPagamentosHeoje) {
			Conta conta = pagamentoConta.getConta();
			
			if(!contasFiltradas.contains(conta)) {
				contasFiltradas.add(conta);
			}
		}
		
		//contasFiltradas = contas.contasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
		
		getValorTotalAndSettingDescricaoAndVendedor(0);
		/*
		for (Conta conta : contasFiltradas) {
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				//Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao("Cliente: " + venda.getCliente().getNome());
			}			
		}
		*/
		
		totalContas = nf.format(totalContasPagasHojeValor);
		
		totalAPagarValor = contas.totalAPagarValor(usuario.getEmpresa());
					
		contaSelecionada = null;
		
		usuarioSelecionado = null;
	}
	

	public Usuario getUsuarioSelecionado() {
		return usuarioSelecionado;
	}

	public void setUsuarioSelecionado(Usuario usuarioSelecionado) {
		this.usuarioSelecionado = usuarioSelecionado;
	}

	public Cliente getClienteSelecionado() {
		return clienteSelecionado;
	}

	public void setClienteSelecionado(Cliente clienteSelecionado) {
		this.clienteSelecionado = clienteSelecionado;
	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public void emitirPedido() {

		Venda vendaSelecionada = vendas.porNumeroVenda(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		EspelhoVenda pedido = new EspelhoVenda();
		pedido.setVendaNum(vendaSelecionada.getNumeroVenda() + "");
		pedido.setTipoVenda(vendaSelecionada.getTipoVenda().getDescricao().toUpperCase());
		pedido.setBairro(vendaSelecionada.getBairro().getNome().toUpperCase());
		pedido.setDataVenda(sdf.format(vendaSelecionada.getDataVenda()));
		
		String vendedor = vendaSelecionada.getUsuario().getNome().split(" ")[0].toUpperCase();
		pedido.setVendedor(vendedor);
		
		if(vendaSelecionada.getCliente().getNome().toUpperCase().equals("NAO INFORMADO") || vendaSelecionada.getCliente().getNome().toUpperCase().equals("NÃO INFORMADO")) {
			Entrega entrega = entregas.porVenda(vendaSelecionada);
			if (entrega != null) {
				if(!vendaSelecionada.getBairro().getNome().toUpperCase().equals("NAO INFORMADO") && !vendaSelecionada.getBairro().getNome().toUpperCase().equals("NÃO INFORMADO")) {
					if(entrega.getNome() == null || entrega.getNome() != null && entrega.getNome().equals("")) {
						pedido.setCliente("Não Informado");
					} else {
						pedido.setCliente(entrega.getNome());
					}
					
					pedido.setEnderecoCliente(entrega.getLocalizacao() != null ? entrega.getLocalizacao() : "");
					//implementar observação/contato
					pedido.setTelefone(entrega.getContato() != null ? entrega.getContato() : "");
					
				} else {
					pedido.setCliente("Não Informado");
					pedido.setEnderecoCliente("");
					//implementar observação/contato
					pedido.setTelefone("");
				}						
			} else {
				pedido.setCliente("Não Informado");
				pedido.setEnderecoCliente("");	
				pedido.setTelefone("");
			}
			
			pedido.setCpfCnpj("");			
			pedido.setCodigoCliente("");
			pedido.setBairroCliente(vendaSelecionada.getBairro().getNome());
			
		} else {
			if(vendaSelecionada.getBairro().getNome().toUpperCase().equals("NAO INFORMADO") || vendaSelecionada.getBairro().getNome().toUpperCase().equals("NÃO INFORMADO")) {
				pedido.setCliente(vendaSelecionada.getCliente().getNome());
				pedido.setCpfCnpj(vendaSelecionada.getCliente().getDocumento());
				pedido.setTelefone(vendaSelecionada.getCliente().getContato());
				pedido.setCodigoCliente(String.valueOf(vendaSelecionada.getCliente().getId().longValue()));
				pedido.setEnderecoCliente(vendaSelecionada.getCliente().getEndereco());
				pedido.setBairroCliente(vendaSelecionada.getCliente().getBairro().getNome());
			} else {
				Entrega entrega = entregas.porVenda(vendaSelecionada);
				if (entrega != null) {
					if(entrega.getNome() == null || entrega.getNome() != null && entrega.getNome().equals("")) {
						pedido.setCliente(vendaSelecionada.getCliente().getNome());
					} else {
						pedido.setCliente(entrega.getNome());
					}
					
					pedido.setEnderecoCliente(entrega.getLocalizacao() != null ? entrega.getLocalizacao() : "");
					
					//implementar observação/contato
					if(entrega.getContato() == null || entrega.getContato() != null && entrega.getContato().equals("")) {
						pedido.setTelefone(vendaSelecionada.getCliente().getContato());
					} else {
						pedido.setTelefone(entrega.getContato());
					}
				}
								
				pedido.setCpfCnpj(vendaSelecionada.getCliente().getDocumento());
				pedido.setCodigoCliente(String.valueOf(vendaSelecionada.getCliente().getId().longValue()));
				pedido.setBairroCliente(vendaSelecionada.getBairro().getNome());
			}
		}

		/*
		Entrega entrega = entregas.porVenda(vendaSelecionada);
		if (entrega != null) {
			pedido.setResponsavel(entrega.getNome());
			pedido.setLocalizacao(entrega.getLocalizacao());
			pedido.setObservacao(entrega.getObservacao());
			pedido.setTelefone(entrega.getContato());
			
			if(entrega.getStatus() == StatusPedido.ENTREGUE.name()) {
				pedido.setEntrega("Y");
			}
		}*/
		
		NumberFormat nf_ = new DecimalFormat("###,##0.000", REAL);
		NumberFormat nf__ = new DecimalFormat("###,##0", REAL);
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemEspelhoVendaProdutos itemPedido = new ItemEspelhoVendaProdutos();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			itemPedido.setDescricao(itemVenda.getProduto().getDescricao().trim());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().doubleValue()));
			
			String quantidade = "0";
			if(itemVenda.getProduto().getUnidadeMedida().equals("Kg") || itemVenda.getProduto().getUnidadeMedida().equals("Lt"))  {
				quantidade = nf_.format(itemVenda.getQuantidade().doubleValue());
			} else {
				quantidade = nf__.format(itemVenda.getQuantidade());
			}
			
			itemPedido.setQuantidade(quantidade);
			
			itemPedido.setUN(itemVenda.getProduto().getUnidadeMedida());
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}
		
		Double troco = 0D;
		List<Pagamento> listaPagamentos = pagamentos.todosPorVenda(vendaSelecionada, usuario.getEmpresa());
		for (Pagamento pagamento : listaPagamentos) {
			
			troco += pagamento.getTroco().doubleValue();
				
			ItemEspelhoVendaPagamentos pagamentosPedido = new ItemEspelhoVendaPagamentos();
			pagamentosPedido.setFormaPagamento(pagamento.getFormaPagamento().getNome());
			pagamentosPedido.setValor(nf.format(pagamento.getValor().doubleValue()));
			
			pedido.getItensPagamentos().add(pagamentosPedido);
			
		}
		
		pedido.setConta(false);
		List<Conta> listaDeContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario.getEmpresa());
		if(listaDeContas.size() > 0) {
			pedido.setConta(true);
		}
		
		if(listaPagamentos.size() == 0) {
			
			if(pedido.getConta() && vendaSelecionada.getTipoPagamento() == TipoPagamento.AVISTA) {
				List<Conta> listaContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario.getEmpresa());
				
				Optional<Conta> conta = listaContas.stream().findFirst();
				
				ItemEspelhoVendaPagamento pagamentosPedido = new ItemEspelhoVendaPagamento();
				pagamentosPedido.setValorPagar(nf.format(conta.get().getValor().doubleValue()));
				pagamentosPedido.setVencimento(sdf.format(conta.get().getVencimento()));
				
				if(conta.isPresent()) {
					if(conta.get().isStatus()) {
						pagamentosPedido.setStatus("PAGO");
					}
				}
				
				pedido.getItensPagamento().add(pagamentosPedido);
				
			} else if(pedido.getConta() && vendaSelecionada.getTipoPagamento() == TipoPagamento.PARCELADO) {
				List<Conta> listaContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario.getEmpresa());
				
				listaContas.forEach(f -> {
					ItemEspelhoVendaParcelamentos parcelamentosPedido = new ItemEspelhoVendaParcelamentos();
					parcelamentosPedido.setParcela(f.getParcela());
					parcelamentosPedido.setValor(nf.format(f.getValor().doubleValue()));
					parcelamentosPedido.setVencimento(sdf.format(f.getVencimento()));
					
					if(f.isStatus()) {
						parcelamentosPedido.setStatus("PAGO");
					}
		            
		            pedido.getItensParcelamentos().add(parcelamentosPedido);
		        });
				
			}
		}	
		
		pedido.setTipoPagamento(vendaSelecionada.getTipoPagamento().name());
				
		pedido.setTroco(nf.format(troco.doubleValue()));
		
		if(usuario.getEmpresa().getLogoRelatorio() != null) {
			
			InputStream targetStream;
			try {
				targetStream = ByteSource.wrap(usuario.getEmpresa().getLogoRelatorio()).openStream();
				pedido.setLogo(targetStream);
				
			} catch (IOException e1) {
			}	
		}
		
		pedido.setxNome(usuario.getEmpresa().getNome() != null ? usuario.getEmpresa().getNome() : "");
		pedido.setCNPJ(usuario.getEmpresa().getCnpj() != null ? usuario.getEmpresa().getCnpj() : "");
		pedido.setxLgr(usuario.getEmpresa().getEndereco() != null ? usuario.getEmpresa().getEndereco().trim() : "");
		pedido.setNro(usuario.getEmpresa().getNumero() != null ? usuario.getEmpresa().getNumero().trim() : "");
		pedido.setxBairro(usuario.getEmpresa().getBairro() != null ? usuario.getEmpresa().getBairro().trim() : "");
		pedido.setxMun(usuario.getEmpresa().getCidade() != null ? usuario.getEmpresa().getCidade() : "");
		pedido.setUF(usuario.getEmpresa().getUf() != null ? usuario.getEmpresa().getUf() : "");
		pedido.setContato(usuario.getEmpresa().getContato() != null ? usuario.getEmpresa().getContato() : "");
		
		pedido.setObservacoes(vendaSelecionada.getObservacao());
		
		pedido.setTotalVenda(nf.format(new BigDecimal(vendaSelecionada.getValorTotal().doubleValue()- vendaSelecionada.getDesconto().doubleValue())));
		
		pedido.setFrete(nf.format(vendaSelecionada.getTaxaDeEntrega()));
		
		if(vendaSelecionada.getDesconto() != null) {
			//aleteração do subtotal para somar com a taxa de entrega
			pedido.setSubTotal(nf.format(vendaSelecionada.getValorTotal().doubleValue()/* - vendaSelecionada.getTaxaDeEntrega().doubleValue()*/));
			pedido.setDesconto(nf.format(vendaSelecionada.getDesconto()));
		} else {
			pedido.setSubTotal(nf.format(vendaSelecionada.getValorTotal().doubleValue()/* - vendaSelecionada.getTaxaDeEntrega().doubleValue()*/));
		}

		List<EspelhoVenda> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
		try {
			
			String path = "";
			if(vendaSelecionada.getEmpresa().getId().equals(7111L)) {
				path = "/relatorios/decore-vendas_RECIBO_DECORE.jasper";
			} else {
				path = "/relatorios/decore-vendas_RECIBO_OURO_DA_AMAZONIA.jasper";
			}
			
			report.getRelatorio_(pedidos, "Venda-N" + vendaSelecionada.getNumeroVenda().longValue(), path);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void filtrarContasPorUsuario() {
		 
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);
		
		double totalContasTemp = 0;
		this.contasFiltradas = new ArrayList<Conta>();
		
		List<Conta> contasFiltradas = new ArrayList<Conta>();
		
		if(tipoContas.equals("Contas à pagar em atraso")) {	
			contasFiltradas = contas.contasAPagarEmAtraso(usuario.getEmpresa(), calendarStart);	
			
		} else if(tipoContas.equals("Contas pagas Hoje " + hoje)) {					
			contasFiltradas = contas.contasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			List<PagamentoConta> listaPagamentosHoje = contas.pagamentosHojeContasAPagar(usuario.getEmpresa(), calendarStart, calendarStop);
			for (PagamentoConta pagamentoConta : listaPagamentosHoje) {
				Conta conta = pagamentoConta.getConta();
				
				if(!contasFiltradas.contains(conta)) {
					contasFiltradas.add(conta);
				}
			}
		}
		
		if(tipoContas.equals("Contas à receber em atraso")) {	
			contasFiltradas = contas.contasAReceberEmAtraso(usuario.getEmpresa(), calendarStart);	
			
		} else if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {					
			contasFiltradas = contas.contasRecebidasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			List<PagamentoConta> listaPagamentosHeoje = contas.pagamentosHojeContasAReceber(usuario.getEmpresa(), calendarStart, calendarStop);
			for (PagamentoConta pagamentoConta : listaPagamentosHeoje) {
				Conta conta = pagamentoConta.getConta();
				
				if(!contasFiltradas.contains(conta)) {
					contasFiltradas.add(conta);
				}
			}
		}
		
		
		if(tipoContas.equals("Total contas à receber")) {
			if(!filtroTodasContas) {
				contasFiltradas = contas.contasAReceber(usuario.getEmpresa());
			} else {
				contasFiltradas = contas.contasAReceberPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());				
			}			
		}
		
		if(tipoContas.equals("Total contas à pagar")) {	
			if(!filtroTodasContas) {
				contasFiltradas = contas.contasAPagar(usuario.getEmpresa());
			} else {
				contasFiltradas = contas.contasAPagarPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());				
			}
		}
		
		
		
		for (Conta conta : contasFiltradas) {				
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
				//conta.setVendedor(lancamento.getUsuario().getNome());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
				conta.setVendedor(compra.getUsuario().getNome());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Cliente: " + venda.getCliente().getNome());
				conta.setCliente(venda.getCliente().getNome());
				conta.setVendedor(venda.getUsuario().getNome());
				conta.setTaxaDeComissao(venda.getTaxaDeComissao());
			}
			
			if(usuarioSelecionado == null) {
				if(!somenteVendas)  {	
					this.contasFiltradas.add(conta);
					
					if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
						totalContasTemp += conta.getSaldo().doubleValue();
						
					}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
						totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
					
						if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
							if(conta.getParcela().equals("Entrada")) {
								totalContasTemp = totalContasTemp + conta.getValor().doubleValue();
							}
						}
					}
				} else {
					if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
						Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
						if(clienteSelecionado == null)  {
							this.contasFiltradas.add(conta);
	
							if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
								totalContasTemp += conta.getSaldo().doubleValue();
								
							}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
								totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();							
								
								if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
									if(conta.getParcela().equals("Entrada")) {
										totalContasTemp = totalContasTemp + conta.getValor().doubleValue();
									}
								}
							}
						} else if(venda.getCliente().getId() == clienteSelecionado.getId()) {
							this.contasFiltradas.add(conta);
							
							if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
								totalContasTemp += conta.getSaldo().doubleValue();
								
							}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
								totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
							
								if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
									if(conta.getParcela().equals("Entrada")) {
										totalContasTemp = totalContasTemp + conta.getValor().doubleValue();
									}
								}
							}
						}						
					}
				}
				
			} else {
				if(!somenteVendas) {
					if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
						Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
						
						if(lancamento.getUsuario() != null) {
							if(usuarioSelecionado.getId() == lancamento.getUsuario().getId()) {							
								
								this.contasFiltradas.add(conta);			
								
								if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
									totalContasTemp += conta.getSaldo().doubleValue();
									
								}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
									totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
								}																
							}
						}					
					}
					
					if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
						Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
						if(usuarioSelecionado.getId() == compra.getUsuario().getId()) {
							
							this.contasFiltradas.add(conta);
	
							if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
								totalContasTemp += conta.getSaldo().doubleValue();
								
							}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
								totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
							}
						}
					}
					
					if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
						Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
						if(usuarioSelecionado.getId() == venda.getUsuario().getId()) {
														
							this.contasFiltradas.add(conta);
	
							if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
								totalContasTemp += conta.getSaldo().doubleValue();
								
							}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
								totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
							
								if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
									if(conta.getParcela().equals("Entrada")) {
										totalContasTemp = totalContasTemp + conta.getValor().doubleValue();
									}
								}
							}							
						}
					}
					
				} else {
					
					if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
						Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
						if(usuarioSelecionado.getId() == venda.getUsuario().getId()) {
							
							if(clienteSelecionado == null)  {
								this.contasFiltradas.add(conta);
		
								if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
									totalContasTemp += conta.getSaldo().doubleValue();
									
								}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
									totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
								
									if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
										if(conta.getParcela().equals("Entrada")) {
											totalContasTemp = totalContasTemp + conta.getValor().doubleValue();
										}
									}
								}
							} else if(venda.getCliente().getId() == clienteSelecionado.getId()) {
								this.contasFiltradas.add(conta);
								
								if(tipoContas.equals("Contas à pagar em atraso") || tipoContas.equals("Contas à receber em atraso") || tipoContas.equals("Total contas à receber") || tipoContas.equals("Total contas à pagar")) {	
									totalContasTemp += conta.getSaldo().doubleValue();
									
								}else if(tipoContas.equals("Contas pagas Hoje " + hoje) || tipoContas.equals("Contas recebidas Hoje " + hoje)) {	
									totalContasTemp += pagamentosContas.totalPagoPorContaHoje(conta, usuario.getEmpresa(), calendarStart, calendarStop).doubleValue();
								
									if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
										if(conta.getParcela().equals("Entrada")) {
											totalContasTemp = totalContasTemp + conta.getValor().doubleValue();
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		BigDecimal saldoDeComissao = BigDecimal.ZERO;
		for (Conta conta : this.contasFiltradas) {
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				if(tipoContas.equals("Contas recebidas Hoje " + hoje)) {
					if(conta.getTaxaDeComissao().doubleValue() > 0) {
						if((conta.getValor().doubleValue() - conta.getSaldo().doubleValue()) > 0) {
							saldoDeComissao = new BigDecimal(saldoDeComissao.doubleValue() + ((conta.getTaxaDeComissao().doubleValue() * (conta.getValor().doubleValue() - conta.getSaldo().doubleValue()))/100));
						}
					}
				} else {
					if(conta.getTaxaDeComissao().doubleValue() > 0) {
						if(conta.getSaldo().doubleValue() > 0) {
							saldoDeComissao = new BigDecimal(saldoDeComissao.doubleValue() + ((conta.getTaxaDeComissao().doubleValue() * conta.getSaldo().doubleValue())/100));
						}
					}
				}
			}		
		}
		
		totalComissoes = nf.format(saldoDeComissao);
		
		totalContas = nf.format(totalContasTemp);
		
	}

	public PagamentoConta getPagamentoConta() {
		return pagamentoConta;
	}

	public void setPagamentoConta(PagamentoConta pagamentoConta) {
		this.pagamentoConta = pagamentoConta;
	}
	
	public List<FormaPagamento> getTodasFormasPagamentos() {
		return todasFormasPagamentos;
	}

	public List<PagamentoConta> getPagamentosAdicionados() {
		return pagamentosAdicionados;
	}
	
	public void prepararPagamentosAdicionados() {
		contaSelecionada = contas.porId(contaSelecionada.getId());
		pagamentosAdicionados = pagamentosContas.todosPorConta(contaSelecionada, usuario.getEmpresa());
		pagamentoSelecionado = null;
	}
	
	public void prepararPagamentosAdicionadosHoje() {
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
		
		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);
		
		if(contaSelecionada.getTipo().equals("DEBITO")) {
			pagamentosAdicionados = contas.pagamentosHojeContasAPagarPorConta(usuario.getEmpresa(), contaSelecionada, calendarStart, calendarStop);			
		} else if(contaSelecionada.getTipo().equals("CREDITO")) {
			pagamentosAdicionados = contas.pagamentosHojeContasAReceberPorConta(usuario.getEmpresa(), contaSelecionada, calendarStart, calendarStop);			
		}
		
		pagamentoSelecionado = null;
	}

	public PagamentoConta getPagamentoSelecionado() {
		return pagamentoSelecionado;
	}

	public void setPagamentoSelecionado(PagamentoConta pagamentoSelecionado) {
		this.pagamentoSelecionado = pagamentoSelecionado;
	}

	public boolean isSomenteVendas() {
		return somenteVendas;
	}

	public void setSomenteVendas(boolean somenteVendas) {
		this.somenteVendas = somenteVendas;
	}

	public List<Cliente> getTodosClientes() {
		return todosClientes;
	}
	
	public List<Cliente> getTodosClientes_() {
		return todosClientes_;
	}
	
	public void mostrarFiltroCliente() {
		if(receitas) {
			if(somenteVendas) {
				PrimeFaces.current().executeScript("mostrarFiltroCliente();");
			} else {
				PrimeFaces.current().executeScript("ocultarFiltroCliente();");
			}
		}
	}

	public boolean isReceitas() {
		return receitas;
	}

	public boolean getFiltroTodasContas() {
		return filtroTodasContas;
	}

	public void setFiltroTodasContas(boolean filtroTodasContas) {
		this.filtroTodasContas = filtroTodasContas;
	}

	public String getTotalComissoes() {
		return totalComissoes;
	}

	public Number getTotalReceitasHojeValor() {
		return totalReceitasHojeValor;
	}

	public Number getTotalLancamentosReceitasHojeValor() {
		return totalLancamentosReceitasHojeValor;
	}

	public Number getTotalLancamentosDespesasHojeValor() {
		return totalLancamentosDespesasHojeValor;
	}

	public Number getTotalDespesasHojeValor() {
		return totalDespesasHojeValor;
	}
	
	public Number getTotalPagoHojeContasAReceberEmAtrasoValor() {
		return totalPagoHojeContasAReceberEmAtrasoValor;
	}


	public Number getTotalPagoHojeContasAPagarEmAtrasoValor() {
		return totalPagoHojeContasAPagarEmAtrasoValor;
	}


	public Number getTotalAPagarHojeValor() {
		return totalAPagarHojeValor;
	}


	public Number getTotalAReceberHojeValor() {
		return totalAReceberHojeValor;
	}


	public String getFitrarContasPagas() {
		return fitrarContasPagas;
	}


	public void setFitrarContasPagas(String fitrarContasPagas) {
		this.fitrarContasPagas = fitrarContasPagas;
	}


	public Date getDateStart() {
		return dateStart;
	}


	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}


	public Date getDateStop() {
		return dateStop;
	}


	public void setDateStop(Date dateStop) {
		this.dateStop = dateStop;
	}


	public Usuario getUsuario_() {
		return usuario_;
	}


	public void setUsuario_(Usuario usuario_) {
		this.usuario_ = usuario_;
	}


	public Cliente getCliente() {
		return cliente;
	}


	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

}
