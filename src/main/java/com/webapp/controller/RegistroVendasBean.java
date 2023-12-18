package com.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.google.common.io.ByteSource;
import com.webapp.model.Bairro;
import com.webapp.model.Caixa;
import com.webapp.model.Cliente;
import com.webapp.model.Configuracao;
import com.webapp.model.Conta;
import com.webapp.model.Devolucao;
import com.webapp.model.Entrega;
import com.webapp.model.EspelhoVenda;
import com.webapp.model.FormaPagamento;
import com.webapp.model.ItemCaixa;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.ItemEspelhoVendaPagamento;
import com.webapp.model.ItemEspelhoVendaPagamentos;
import com.webapp.model.ItemEspelhoVendaParcelamentos;
import com.webapp.model.ItemEspelhoVendaProdutos;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Log;
import com.webapp.model.Mesa;
import com.webapp.model.Pagamento;
import com.webapp.model.PagamentoConta;
import com.webapp.model.PeriodoPagamento;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoOperacao;
import com.webapp.model.TipoPagamento;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Bairros;
import com.webapp.repository.Clientes;
import com.webapp.repository.Configuracoes;
import com.webapp.repository.Contas;
import com.webapp.repository.Entregas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Logs;
import com.webapp.repository.Pagamentos;
import com.webapp.repository.PagamentosContas;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.repository.filter.BairroFilter;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RegistroVendasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Venda venda;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Bairros bairros;

	@Inject
	private Produtos produtos;

	@Inject
	private TiposVendas tiposVendas;

	@Inject
	private Vendas vendas;

	@Inject
	private ItensVendas itensVendas;

	@Inject
	private Entregas entregas;

	@Inject
	private ItensCompras itensCompras;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;
	
	@Inject
	private Contas contas;

	private List<Usuario> todosUsuarios;

	private List<Bairro> todosBairros;
	
	private List<Cliente> todosClientes = new ArrayList<Cliente>();

	private List<TipoVenda> todosTiposVendas;

	private List<Produto> produtosFiltrados;

	@Inject
	private ItemVenda itemVenda;

	//@NotNull
	@Inject
	private ItemCompra itemCompra;

	private List<ItemVenda> itensVenda = new ArrayList<ItemVenda>();

	private List<ItemCompra> itensCompra = new ArrayList<ItemCompra>();

	private ProdutoFilter filter = new ProdutoFilter();

	private ItemVenda itemSelecionado;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private boolean entrega;

	@Inject
	private Entrega entregaVenda;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Clientes clientes;
	
	private boolean disableAjuste = false;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	
	@Inject
	private Logs logs;
	
	/* Novas variáveis */	
	private Double valorRecebido;
	
	private Double troco;
	
	private Double trocoParcial = 0D;
	
	private Double faltando;

	private boolean trocaPendente = false;
	
	
	private Double saldoParaTroca = 0D;
	private String saldoParaTrocaEmString = "0,00";
	
	private boolean vendaPaga = true;
	private Date pagamentoPara;
	private String pagamentoParaEmString;

	@Inject
	private Pagamentos pagamentos;
	
	@Inject
	private PagamentosContas pagamentosContas;
	
	@Inject
	private Pagamento pagamento;
	
	private List<Pagamento> pagamentosAdicionados = new ArrayList<Pagamento>();

	private BigDecimal totalDeAcrescimo = BigDecimal.ZERO;
	
	
	private TipoPagamento tipoPagamento = TipoPagamento.AVISTA;
	private boolean parcelasConfirmadas;
	private List<Conta> todasContas = new ArrayList<>();
	private Double valorEntrada;
	
	private List<Conta> entradas = new ArrayList<>();
	
	@Inject
	private ItensDevolucoes itensDevolucoes;
	
	
	private boolean contaAPagar;

	@NotNull
	private Long parcelas = 2L;

	private PeriodoPagamento periodoPagamento = PeriodoPagamento.MESES;

	private Date primeiraParcela;
		
	private String primeiraParcelaEmString;
	
	private List<FormaPagamento> todasFormasPagamentos = new ArrayList<FormaPagamento>();
	
	private boolean imprimirCupom = true;
	
	private boolean gerarPDF = false;
	
	@Inject
	private Configuracoes configuracoes;
	
	@Inject
	private Configuracao configuracao;
	
	private BigDecimal totalPagamentos = BigDecimal.ZERO;
	private BigDecimal totalTroco = BigDecimal.ZERO;
	
	private Venda vendaTemp_ = new Venda();
	
	private Mesa mesa;	
	private List<Mesa> mesas = new ArrayList<Mesa>();
	
	@Inject
	private Produto produto;
	
	private boolean edit;
	
	private BigDecimal margemDeLucro = new BigDecimal(0.00);
	
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario.getEmpresa());
			todosTiposVendas = tiposVendas.todos(usuario.getEmpresa());
			todosBairros = bairros.todos(usuario.getEmpresa());
			todosClientes = clientes.todos(usuario.getEmpresa());
			todasFormasPagamentos = formasPagamentos.todos(usuario.getEmpresa());
			
			configuracao = configuracoes.porUsuario(usuario);
			imprimirCupom = configuracao.isCupomAtivado();
			
			venda.setUsuario(usuario);
			venda.setStatusMesa("PAGO");
			
			Cliente cliente = clientes.porNome("Nao Informado", usuario.getEmpresa());
			venda.setCliente(cliente);
			
			if(usuario.getEmpresa().getId() == 7111 || usuario.getEmpresa().getId() == 7112) {
				venda.setTipoVenda(tiposVendas.porId(34L));
				FormaPagamento formaPagamento = formasPagamentos.porId(13987L);
				venda.setFormaPagamento(formaPagamento);
				venda.setBairro(bairros.porId(3008L));
			} else {
				venda.setTipoVenda(tiposVendas.porDescricao("Nao Informado", usuario.getEmpresa()));
				FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro", usuario.getEmpresa());
				venda.setFormaPagamento(formaPagamento);
				venda.setBairro(bairros.porNome("Não Informado", usuario.getEmpresa()));
			}
		}
	}

	public void pesquisar() {
		filter.setEmpresa(usuario.getEmpresa());
		produtosFiltrados = produtos.filtrados(filter);
		System.out.println(produtosFiltrados.size());
	}
	
	public List<Bairro> completeText(String query) {
		
        BairroFilter filtro = new BairroFilter();
        filtro.setNome(query);
        
        List<Bairro> listaDeBairros = bairros.filtrados(filtro, usuario.getEmpresa());       
        
        return listaDeBairros;
    }

	public void buscar() {
		venda = vendas.porId(venda.getId());
		itensVenda = itensVendas.porVenda(venda);
		
		venda.setValorTotal(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getTaxaDeEntrega().doubleValue()));

		for (ItemVenda itemVenda : itensVenda) {
			
			/*if(itemVenda.getLucro().doubleValue() < 0) {
				disableAjuste = true;
			}*/
			
			List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda_(itemVenda);
			itemVenda.setItensVendaCompra(itensVendaCompra);
			
			try {
				Thread.sleep(100);
				itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
			} catch (InterruptedException e) {
			}
		}
		
		entregaVenda = entregas.porVenda(venda);
		entrega = entregaVenda.getId() != null;
		
		edit = true;
		
		
		
		
		this.margemDeLucro = BigDecimal.ZERO;
		
		if(itensVenda.size() > 0) {
			
			Double valorTotalDeCompra = 0D;
			Double valorTotalDeVenda = 0D;
			for (ItemVenda itemVenda : itensVenda) {
				valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
				valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
			}
			
			if(venda.getDesconto() != null) {
				valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
			}
			
			Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
			BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
			this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			System.out.println("Margem de Lucro: " + margemDeLucro);
			System.out.println("Lucro Real: " + lucroReal);
			
		}
	}
	
	public void calculaSubtotal() {
		itemVenda.setTotal(new BigDecimal(itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue()));
	}

	public void salvar() {

		if (itensVenda.size() > 0) {

			long totalDeItens = 0L;
			double valorTotal = 0;
			double lucro = 0;
			//double percentualLucro = 0;
			double valorCompra = 0;

			setInformacoesDataLancamento();

			venda.setTipoPagamento(TipoPagamento.AVISTA);

			FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro", usuario.getEmpresa());
			venda.setFormaPagamento(formaPagamento);
			
			
			
			venda.setValorRecebido(venda.getValorTotal());
			venda.setFaltando(BigDecimal.ZERO);
			venda.setTroco(BigDecimal.ZERO);
			
			
			if(venda.getDesconto() == null) {
				venda.setDesconto(BigDecimal.ZERO);
			}
			
			
			
			if(!venda.isAjuste()) {
				venda.setRecuperarValores(false);
			}

			boolean edit = false;
			if (venda.getId() != null) {
				edit = true;

				

				//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
				//for (ItemCompra itemCompraTemp : itensCompraTemp) {
				//	itemCompraTemp.getCompra()
				//			.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
						
				//for (ItemVenda itemVenda : itensVenda) {
					
				List<ItemVenda> itensVendaOld = itensVendas.porVenda(venda);
				for (ItemVenda itemVendaOld : itensVendaOld) {
					
					List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVendaOld.getProduto());
					for (ItemCompra itemCompraTemp : itensCompraTemp) {
						if (itemCompraTemp.getCompra().getId().longValue() == itemVendaOld.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVendaOld.getProduto().getId()
									.longValue()) {
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaOld.getQuantidade().doubleValue()));
				
							}
						}
						
						itensCompras.save(itemCompraTemp);
					}	
				}

				for (ItemVenda itemVenda : itensVenda) {									
					
					List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompraTemp : itensCompraTemp) {
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
				
							}
						}
						
						itensCompras.save(itemCompraTemp);
					}
	
				}
				
					
			
						/*if(itemVenda.isNovo()) {
							
							if(itemVenda.getId() != null) {
								ItemVenda itemVendaTemp = itensVendas.porId(itemVenda.getId());
								if (itemCompraTemp.getCompra().getId().longValue() == itemVendaTemp.getCompra().getId()
										.longValue()) {
									if (itemCompraTemp.getProduto().getId().longValue() == itemVendaTemp.getProduto().getId()
											.longValue()) {
										itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
												itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaTemp.getQuantidade().doubleValue()));
						
									}
								}							
							}
							
							if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
									.longValue()) {
								if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
										.longValue()) {
									
									itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
								}
							}
							
						}*/

						
				//}
						
				//}
				

				
				List<ItemVenda> itemVendaTemp_ = itensVendas.porVenda(venda);

				for (ItemVenda itemVenda : itemVendaTemp_) {
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()));
					produtos.save(produto);

					itensVendas.remove(itemVenda);
				}
				

				
				for (ItemVenda itemVenda : itensVenda) {
					
					itemVenda.setId(null);
					
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					
					Venda vendaTemp = vendas.porId(venda.getId());
					System.out.println("Ajuste: " + (venda.isAjuste() != vendaTemp.isAjuste()));	
					
					
	
					if(vendaTemp.isAjuste()) {
						
						if(!vendaTemp.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								//if(!itemVendaCompra.getCompra().isAjuste()) {
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());										
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
								//}
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								//if(!itemVendaCompra.getCompra().isAjuste()) {					
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());
									
									BigDecimal total = itemVenda.getTotal();					
									//BigDecimal total = new BigDecimal(subtotal.doubleValue() - itemVenda.getLucro().doubleValue());		
	
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + total.doubleValue()));					
								//}
							//}											
						}
					}

					
					
					if(venda.isAjuste()) {
						
						if(!venda.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								//if(!itemVendaCompra.getCompra().isAjuste()) {
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());										
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
								//}
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								//if(!itemVendaCompra.getCompra().isAjuste()) {					
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());
									
									BigDecimal total = itemVenda.getTotal();					
									//BigDecimal total = new BigDecimal(subtotal.doubleValue() - itemVenda.getLucro().doubleValue());		
	
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - total.doubleValue()));					
								//}
							//}											
						}
					}
					
					
					System.out.println(produto.getCustoTotal());
					produtos.save(produto);
									
				}
			}
			
			
			if (venda.getId() == null) {			
	
				Venda vendaTemp = vendas.ultimoNVenda(usuario.getEmpresa());
	
				if (vendaTemp == null) {
					venda.setNumeroVenda(1L);
				} else {
					if (venda.getId() == null) {
						venda.setNumeroVenda(vendaTemp.getNumeroVenda() + 1);
					}
				}
	
				venda.setEmpresa(usuario.getEmpresa());
				venda = vendas.save(venda);
	
				for (ItemVenda itemVenda : itensVenda) {
	
					itemVenda.setVenda(venda);
					itensVendas.save(itemVenda);
	
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - itemVenda.getQuantidade().doubleValue()));
					
					if(venda.isAjuste()) {
								
						if(!venda.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								//if(!itemVendaCompra.getCompra().isAjuste()) {
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());										
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
								//}
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								//if(!itemVendaCompra.getCompra().isAjuste()) {					
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());
									
									BigDecimal total = itemVenda.getTotal();					
									//BigDecimal total = new BigDecimal(subtotal.doubleValue() - itemVenda.getLucro().doubleValue());		
	
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - total.doubleValue()));					
								//}
							//}											
						}
					}
						
						
					produtos.save(produto);
	
					if(!produto.getUnidadeMedida().equals("Kg") && !produto.getUnidadeMedida().equals("Lt") && !produto.getUnidadeMedida().equals("Pt")) {
						totalDeItens += itemVenda.getQuantidade().doubleValue();				
					} else {
						totalDeItens += 1;
					}
					
					valorTotal += itemVenda.getTotal().doubleValue();
					valorCompra += itemVenda.getValorCompra().doubleValue();
	
					lucro += itemVenda.getLucro().doubleValue();
					//percentualLucro += itemVenda.getPercentualLucro().doubleValue();
	
					List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompraTemp : itensCompraTemp) {
	
						System.out.println(itemCompraTemp.getCompra().getId() + " == " + itemVenda.getCompra().getId());
						System.out.println(itemCompraTemp.getProduto().getId() + " == " + itemVenda.getProduto().getId());
	
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								// if(itemVenda.getId() == null) {
								System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
								System.out.println("itemCompraTemp.getQuantidadeDisponivel(): "
										+ itemCompraTemp.getQuantidadeDisponivel());
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
								System.out
										.println("Nova QuantidadeDisponivel: " + itemCompraTemp.getQuantidadeDisponivel());
								itensCompras.save(itemCompraTemp);
								// }
							}
						}
					}
					
				}
			}


			if (!edit) {
				
				venda.setStatus(!entrega);
				
				venda.setVendaPaga(false); //venda.setVendaPaga(!entrega);

				if (entrega) {
					entregaVenda.setStatus("PENDENTE");
					entregaVenda.setVenda(venda);
					entregaVenda = entregas.save(entregaVenda);
				}
				
				venda.setValorCompra(BigDecimal.valueOf(valorCompra));				
				//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
				venda.setQuantidadeItens(totalDeItens);				
				//venda.setLucro(BigDecimal.valueOf(lucro));
				venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));
				
				
				
				

				
				
				
				
				
				BigDecimal taxaDeEntrega = BigDecimal.ZERO;
				if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
				} else {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
				}

				venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));
				
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
				if(venda.getDesconto() != null) {
					venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
				}

				venda.setLucro(BigDecimal.valueOf(lucro));
				if(venda.getDesconto() != null) {
					venda.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
				}
				
				if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
					venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
				}
		
				venda.setPercentualLucro(new BigDecimal(((venda.getValorTotalComDesconto().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotalComDesconto().doubleValue())*100));
				
				
				
				
				
				
				
				
				
				
				
				venda = vendas.save(venda);
				
				

				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
						+ venda.getNumeroVenda() + " registrada com sucesso!' });");
				
				
				
				
				
				
				Log log = new Log();
				log.setDataLog(new Date());
				log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
				log.setOperacao(TipoAtividade.VENDA.name());
				
				NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
				
				log.setDescricao("Registrou venda, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
				log.setUsuario(usuario);		
				logs.save(log);
				
				

				Venda vendaTemp_ = new Venda();
				vendaTemp_.setNumeroVenda(null);
				vendaTemp_.setTipoVenda(venda.getTipoVenda());
				vendaTemp_.setBairro(venda.getBairro());
				vendaTemp_.setUsuario(venda.getUsuario());	
				vendaTemp_.setStatusMesa("PAGO");
				Cliente cliente = clientes.porNome("Nao Informado", usuario.getEmpresa());
				vendaTemp_.setCliente(cliente);
				
				venda = new Venda();
				itensVenda = new ArrayList<ItemVenda>();
				itemVenda = new ItemVenda();
				itemSelecionado = null;

				itensCompra = new ArrayList<>();
				itemCompra = new ItemCompra();

				entregaVenda = new Entrega();
				entrega = false;
				
				venda = vendaTemp_;

			} else {

				if (entrega) {
					if(entregaVenda.getId() == null) {
						venda.setStatus(!entrega);
						venda.setVendaPaga(false); //venda.setVendaPaga(!entrega);
						
						entregaVenda.setStatus("PENDENTE");
						entregaVenda.setVenda(venda);
						entregaVenda = entregas.save(entregaVenda);
					} else {
						entregaVenda = entregas.save(entregaVenda);
					}
				} else {
					if(entregaVenda.getId() != null) {
						entregas.remove(entregaVenda);
						entregaVenda = new Entrega();
						
						venda.setStatus(!entrega);
						venda.setVendaPaga(false); //venda.setVendaPaga(!entrega);
					}
				}
				
				
				venda = vendas.save(venda);
				
				for (ItemVenda itemVenda : itensVenda) {
					
					itemVenda.setVenda(venda);
					itensVendas.save(itemVenda);
	
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - itemVenda.getQuantidade().doubleValue()));
					
					if(venda.isAjuste()) {
								
						if(!venda.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								//if(!itemVendaCompra.getCompra().isAjuste()) {
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());										
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
								//}
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								//if(!itemVendaCompra.getCompra().isAjuste()) {					
									//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());
									
									BigDecimal total = itemVenda.getTotal();					
									//BigDecimal total = new BigDecimal(subtotal.doubleValue() - itemVenda.getLucro().doubleValue());		
	
									produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - total.doubleValue()));					
								//}
							//}											
						}
					}
						
						
					produtos.save(produto);
	
					if(!produto.getUnidadeMedida().equals("Kg") && !produto.getUnidadeMedida().equals("Lt") && !produto.getUnidadeMedida().equals("Pt")) {
						totalDeItens += itemVenda.getQuantidade().doubleValue();				
					} else {
						totalDeItens += 1;
					}
					
					valorTotal += itemVenda.getTotal().doubleValue();
					valorCompra += itemVenda.getValorCompra().doubleValue();
	
					lucro += itemVenda.getLucro().doubleValue();
					//percentualLucro += itemVenda.getPercentualLucro().doubleValue();
	
					/*
					List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompraTemp : itensCompraTemp) {
	
						System.out.println(itemCompraTemp.getCompra().getId() + " == " + itemVenda.getCompra().getId());
						System.out.println(itemCompraTemp.getProduto().getId() + " == " + itemVenda.getProduto().getId());
	
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								// if(itemVenda.getId() == null) {
								System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
								System.out.println("itemCompraTemp.getQuantidadeDisponivel(): "
										+ itemCompraTemp.getQuantidadeDisponivel());
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
								System.out
										.println("Nova QuantidadeDisponivel: " + itemCompraTemp.getQuantidadeDisponivel());
								itensCompras.save(itemCompraTemp);
								// }
							}
						}
					}
					*/
					
				}
				
				

				venda.setValorCompra(BigDecimal.valueOf(valorCompra));
				//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
				venda.setQuantidadeItens(totalDeItens);
				//venda.setLucro(BigDecimal.valueOf(lucro));
				/*venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));*/			
				//venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));	
				
				
				
				
				
				
				
				
				
				
				BigDecimal taxaDeEntrega = BigDecimal.ZERO;
				if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
				} else {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
				}

				venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));
				
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
				if(venda.getDesconto() != null) {
					venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
				}	

				venda.setLucro(BigDecimal.valueOf(lucro));
				if(venda.getDesconto() != null) {
					venda.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
				}
				
				if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
					venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
				}
		
				venda.setPercentualLucro(new BigDecimal(((venda.getValorTotalComDesconto().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotalComDesconto().doubleValue())*100));
				
				
				
				
				
				
				
				venda = vendas.save(venda);
				
				
				buscar();
				
				
				
				
				

				
				
				
				Log log = new Log();
				log.setDataLog(new Date());
				log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
				log.setOperacao(TipoAtividade.VENDA.name());
				
				NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
				
				log.setDescricao("Alterou venda, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
				log.setUsuario(usuario);		
				logs.save(log);
				
				
				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
						+ venda.getNumeroVenda() + " atualizada com sucesso!' });");
			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!' });");
		}

	}

	public void selecionarProduto(Produto produto) {
		itemVenda = new ItemVenda();
		itemVenda.setProduto(produto);
		itemVenda.setCode(produto.getCodigo().concat("_" + new Date().getTime()));
		System.out.println(itemVenda.getCode());

		itensCompra = new ArrayList<ItemCompra>();
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);

		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			itemCompraTemp.getCompra().setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
			
			
			
			if(venda.getId() != null) {
				List<ItemVenda> itensVendaTemp = itensVendas.porVenda(venda);
				for (ItemVenda itemVenda : itensVendaTemp) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
							.longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
							itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
									itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVenda.getQuantidade().doubleValue()));
			
						}
					}
				}
				
				for (ItemVenda itemVenda : itensVenda) {									
						
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
							}
						}
	
				}
				
			} else {

				boolean produtoNaLista = false;
				for (ItemVenda itemVenda : itensVenda) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
	
							produtoNaLista = true;
							if (itemVenda.getId() == null && venda.getId() == null) {
								
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
						
							}
	
						}
					}
				}
	
				if (produtoNaLista != false) {
					if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}
	
				if (produtoNaLista != true) {
					if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}
			}
			
			
			
			
			if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
				nf = new DecimalFormat("###,##0.000", REAL);
				itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
						itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));

			} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
				nf = new DecimalFormat("###,##0.0", REAL);
				itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
						itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
			
			} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
				nf = new DecimalFormat("###,##0", REAL);
				itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
						itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
			}
		}
		
		if(venda.getId() != null) {
			itensCompra.addAll(itensCompraTemp);
		}

		if (itensCompra.size() == 0) {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!' });");
		} else {

			nf = new DecimalFormat("###,##0.00", REAL);

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				
				//itensCompraTemp.add(itensCompra.get(i));
				
				if (itensCompra.get(i).getQuantidadeDisponivel().doubleValue() > 0) {
					itensCompraTemp.add(itensCompra.get(i));
					itemCompra = itensCompra.get(i);
				}				
				
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);
		}
	}
	

	public void adicionarItem() {

		if(itemVenda.getQuantidade().doubleValue() > 0) {
			
			if(itemVenda.getValorUnitario().doubleValue() >= 0) {
		
				if (venda.getId() == null) {
		
					Double quantidadeDisponivel = itemCompra.getQuantidadeDisponivel().doubleValue();
		
					for (ItemVenda itemVenda : itensVenda) {
						if (itemCompra.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
							if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
								if (itemVenda.getId() == null && venda.getId() == null) {
									quantidadeDisponivel -= itemVenda.getQuantidade().doubleValue();
								}
							}
						}
					}
		
					System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
					System.out.println("quantidadeDisponivel: " + quantidadeDisponivel);
		
					if (itemVenda.getQuantidade().doubleValue() <= quantidadeDisponivel.doubleValue()) {
						
							if (itemVenda.getValorUnitario().doubleValue() <= itemCompra.getValorUnitario().doubleValue()) {					
								PrimeFaces.current().executeScript(
										"swal({ type: 'warning', title: 'Atenção!', text: 'Produto adicionado com valor unitário menor ou igual ao valor de compra.' });");
							}
						
							itemVenda.setTotal(BigDecimal.valueOf(
									itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue()));
							itemVenda.setVenda(venda);
							itemVenda.setCompra(itemCompra.getCompra());
							
							itemVenda.setItemCompra(itemCompra);
		
							/* Calculo do Lucro em valor e percentual */
							Double valorDeCustoUnitario = itemCompra.getValorUnitario().doubleValue(); //itemVenda.getProduto().getCustoMedioUnitario().doubleValue();	
							
							/*itemVenda.setLucro(BigDecimal.valueOf((itemVenda.getQuantidade().doubleValue()
									* itemVenda.getValorUnitario().doubleValue())
									- (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getCustoMedioUnitario().doubleValue())));
							
							itemVenda.setPercentualLucro(BigDecimal.valueOf((itemVenda.getLucro().doubleValue()
									/ (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getCustoMedioUnitario().doubleValue()))
									* 100));*/
							
							itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
									* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().intValue())
									- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * itemVenda.getDesconto().doubleValue() / 100));
		
							itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100)));													
							itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));

							
							
							
							List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();	
							
							ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
							itemVendaCompra.setItemVenda(itemVenda);
							itemVendaCompra.setItemCompra(itemCompra);
							itemVendaCompra.setCompra(itemCompra.getCompra());
							itemVendaCompra.setQuantidade(itemVenda.getQuantidade());
							
							itensVendaCompra.add(itemVendaCompra);
							itemVenda.setItensVendaCompra(itensVendaCompra);
							
							
							
							System.out.println(itemVenda.getLucro());
							System.out.println(itemVenda.getPercentualLucro());
		
							venda.setValorTotal(BigDecimal
									.valueOf(venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));
		
							itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
							itensVenda.add(itemVenda);
		
							String code = itemVenda.getCode();
							Produto produto = itemVenda.getProduto();
		
							itemVenda = new ItemVenda();
							itemVenda.setCode(code);
							itemVenda.setProduto(produto);
		
							itensCompra = new ArrayList<ItemCompra>();
		
							List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
							for (ItemCompra itemCompraTemp : itensCompraTemp) {
								itemCompraTemp.getCompra()
										.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
		
								boolean produtoNaLista = false;
								for (ItemVenda itemVenda : itensVenda) {
									if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
											.longValue()) {
										if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
												.longValue()) {
		
											produtoNaLista = true;
											if (itemVenda.getId() == null && venda.getId() == null) {
												
												itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
														itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
								
											}
		
										}
									}
								}
		
								if (produtoNaLista != false) {
									if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
										itensCompra.add(itemCompraTemp);
									}
								}
		
								if (produtoNaLista != true) {
									if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
										itensCompra.add(itemCompraTemp);
									}
								}
								
								if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
									nf = new DecimalFormat("###,##0.000", REAL);
									itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
					
								} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
									nf = new DecimalFormat("###,##0.0", REAL);
									itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
								
								} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
									nf = new DecimalFormat("###,##0", REAL);
									itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
								}
							}
							
							nf = new DecimalFormat("###,##0.00", REAL);
		
							itensCompraTemp = new ArrayList<>();
							for (int i = itensCompra.size() - 1; i >= 0; i--) {
								itensCompra.get(i).setValorUnitarioFormatado(
										"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
								itensCompraTemp.add(itensCompra.get(i));
							}
							
		
							itensCompra = new ArrayList<>();
							itensCompra.addAll(itensCompraTemp);
							
							int index = itensCompra.indexOf(itemCompra);							
							if(index < 0) {								
								for (int i = 0; i < itensCompra.size(); i++) {
									this.itemCompra = itensCompra.get(i);
								}
							}

							
							
							this.margemDeLucro = BigDecimal.ZERO;
							venda.setTaxaDeComissao(BigDecimal.ZERO);
							
							if(itensVenda.size() > 0) {
								
								Double valorTotalDeCompra = 0D;
								Double valorTotalDeVenda = 0D;
								for (ItemVenda itemVenda : itensVenda) {
									valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
									valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
								}
								
								if(venda.getDesconto() != null) {
									valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
								}
								
								Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
								BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
								this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
								System.out.println("Margem de Lucro: " + margemDeLucro);
								System.out.println("Lucro Real: " + lucroReal);
								
								if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
									venda.setTaxaDeComissao(new BigDecimal(1));
								}	
								
								if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
									venda.setTaxaDeComissao(new BigDecimal(1.5));
								}
								
								if(this.margemDeLucro.doubleValue() > 29) {
									venda.setTaxaDeComissao(new BigDecimal(2));
								}
								
							} else {
								this.margemDeLucro = BigDecimal.ZERO;
								venda.setTaxaDeComissao(BigDecimal.ZERO);
							}
							
					} else {
						PrimeFaces.current().executeScript(
								"swal({ type: 'error', title: 'Erro!', text: 'Quantidade maior que a disponível!' });");
					}
		
				} else {
					//PrimeFaces.current().executeScript(
					//		"swal({ type: 'error', title: 'Erro!', text: 'Não é possível adicionar itens à esta venda!' });");

					Double quantidadeDisponivel = itensCompra.get(itensCompra.indexOf(itemCompra)).getQuantidadeDisponivel().doubleValue();					
						
			        //quantidadeDisponivel += novaQuantidadeAdicionada.doubleValue();
			        
					System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
					System.out.println("quantidadeDisponivel: " + quantidadeDisponivel);
		
					if (itemVenda.getQuantidade().doubleValue() <= quantidadeDisponivel.doubleValue()) {
						
							if (itemVenda.getValorUnitario().doubleValue() <= itemCompra.getValorUnitario().doubleValue()) {					
								PrimeFaces.current().executeScript(
										"swal({ type: 'warning', title: 'Atenção!', text: 'Produto adicionado com valor unitário menor ou igual ao valor de compra.' });");
							}
							
							itemVenda.setNovo(true);
							
							itemVenda.setTotal(BigDecimal.valueOf(
									itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue()));
							itemVenda.setVenda(venda);
							itemVenda.setCompra(itemCompra.getCompra());
		
							/* Calculo do Lucro em valor e percentual */
							Double valorDeCustoUnitario = itemCompra.getValorUnitario().doubleValue(); //itemVenda.getProduto().getCustoMedioUnitario().doubleValue();	
							
							/*itemVenda.setLucro(BigDecimal.valueOf((itemVenda.getQuantidade().doubleValue()
									* itemVenda.getValorUnitario().doubleValue())
									- (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getCustoMedioUnitario().doubleValue())));
							
							itemVenda.setPercentualLucro(BigDecimal.valueOf((itemVenda.getLucro().doubleValue()
									/ (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getCustoMedioUnitario().doubleValue()))
									* 100));*/
							
							itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
									* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().intValue())
									- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * itemVenda.getDesconto().doubleValue() / 100));
		
							itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100)));													
							itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));

							
							
							
							List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();	
							
							ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
							itemVendaCompra.setItemVenda(itemVenda);
							itemVendaCompra.setItemCompra(itemCompra);
							itemVendaCompra.setCompra(itemCompra.getCompra());
							itemVendaCompra.setQuantidade(itemVenda.getQuantidade());
							
							itensVendaCompra.add(itemVendaCompra);
							itemVenda.setItensVendaCompra(itensVendaCompra);
							
							
							
							System.out.println(itemVenda.getLucro());
							System.out.println(itemVenda.getPercentualLucro());
		
							venda.setValorTotal(BigDecimal
									.valueOf(venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));
		
							itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
							itensVenda.add(itemVenda);
		
							String code = itemVenda.getCode();
							Produto produto = itemVenda.getProduto();
							
							
							List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
							
							//ItemCompra itemCompra = itensCompras.porCompra(this.itemCompra.getCompra(), this.itemCompra.getProduto());
							//itensCompraTemp.get(itensCompraTemp.indexOf(itemCompra)).setQuantidadeDisponivel(new BigDecimal(quantidadeDisponivel.doubleValue()));
							
		
							itemVenda = new ItemVenda();
							itemVenda.setCode(code);
							itemVenda.setProduto(produto);
		
							itensCompra = new ArrayList<ItemCompra>();
		
							
							// filter
					       // Predicate<? super ItemVenda> novo = name -> name.isNovo();			        
					        //BigDecimal novaQuantidadeAdicionada = itensVenda.stream().filter(novo).map((x) -> x.getQuantidade()).reduce((x, y) -> x.add(y)).get();
			
							
							for (ItemCompra itemCompraTemp : itensCompraTemp) {
								itemCompraTemp.getCompra()
										.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

								
								
								
								List<ItemVenda> itensVendaTemp = itensVendas.porVenda(venda);
								for (ItemVenda itemVenda : itensVendaTemp) {
									if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
											.longValue()) {
										if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
												.longValue()) {
											itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
													itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVenda.getQuantidade().doubleValue()));
							
										}
									}
								}
								
								for (ItemVenda itemVenda : itensVenda) {									
										
										if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
												.longValue()) {
											if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
													.longValue()) {
												
												itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
														itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
											}
										}

								}
								
								
								
								
								if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
									nf = new DecimalFormat("###,##0.000", REAL);
									itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
					
								} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
									nf = new DecimalFormat("###,##0.0", REAL);
									itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
								
								} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
									nf = new DecimalFormat("###,##0", REAL);
									itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
								}
							}
							
							nf = new DecimalFormat("###,##0.00", REAL);
							
							itensCompra.addAll(itensCompraTemp);
		
							itensCompraTemp = new ArrayList<>();
							for (int i = itensCompra.size() - 1; i >= 0; i--) {
								itensCompra.get(i).setValorUnitarioFormatado(
										"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
								//itensCompraTemp.add(itensCompra.get(i));
								
								if (itensCompra.get(i).getQuantidadeDisponivel().doubleValue() > 0) {
									itensCompraTemp.add(itensCompra.get(i));														
								}
							}
							
		
							itensCompra = new ArrayList<>();
							itensCompra.addAll(itensCompraTemp);	
							
							int index = itensCompra.indexOf(itemCompra);							
							if(index < 0) {								
								for (int i = 0; i < itensCompra.size(); i++) {
									this.itemCompra = itensCompra.get(i);
								}
							}
							
							
							this.margemDeLucro = BigDecimal.ZERO;
							venda.setTaxaDeComissao(BigDecimal.ZERO);
							
							if(itensVenda.size() > 0) {
								
								Double valorTotalDeCompra = 0D;
								Double valorTotalDeVenda = 0D;
								for (ItemVenda itemVenda : itensVenda) {
									valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
									valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
								}
								
								if(venda.getDesconto() != null) {
									valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
								}
								
								Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
								BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
								this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
								System.out.println("Margem de Lucro: " + margemDeLucro);
								System.out.println("Lucro Real: " + lucroReal);
								
								if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
									venda.setTaxaDeComissao(new BigDecimal(1));
								}	
								
								if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
									venda.setTaxaDeComissao(new BigDecimal(1.5));
								}
								
								if(this.margemDeLucro.doubleValue() > 29) {
									venda.setTaxaDeComissao(new BigDecimal(2));
								}
								
							} else {
								this.margemDeLucro = BigDecimal.ZERO;
								venda.setTaxaDeComissao(BigDecimal.ZERO);
							}
							
					} else {
						PrimeFaces.current().executeScript(
								"swal({ type: 'error', title: 'Erro!', text: 'Quantidade maior que a disponível!' });");
					}
				}
				
			} else {
				
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Valor unitário não pode ser menor que zero!' });");
			}
			
		} else {
			PrimeFaces.current()
				.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor ou igual a zero!' });");
		}
		
		Long totalDeItens = 0L;
		for (ItemVenda itemVenda : itensVenda) {
			
			if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt") && !itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {
				totalDeItens += itemVenda.getQuantidade().longValue();				
			} else {
				totalDeItens += 1;
			}
		}	
		
		venda.setTaxaDeEntrega(venda.getTaxaDeEntrega() != null ? venda.getTaxaDeEntrega() : BigDecimal.ZERO);
		venda.setQuantidadeItens(totalDeItens);
		
	}

	public void removeItem() {

		if (venda.getId() == null) {

			// itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);

			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());

			itensCompra = new ArrayList<ItemCompra>();

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

				boolean produtoNaLista = false;
				for (ItemVenda itemVenda : itensVenda) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {

							produtoNaLista = true;
							if (itemVenda.getId() == null && venda.getId() == null) {
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
								
							}

						}
					}
				}

				if (produtoNaLista != false) {
					if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}

				if (produtoNaLista != true) {
					if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}
				
				if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
					nf = new DecimalFormat("###,##0.000", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
	
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
					nf = new DecimalFormat("###,##0.0", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
				
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
					nf = new DecimalFormat("###,##0", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
				}
			}
			
			nf = new DecimalFormat("###,##0.00", REAL);

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);

			itemSelecionado = null;

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();
			
			this.margemDeLucro = BigDecimal.ZERO;
			venda.setTaxaDeComissao(BigDecimal.ZERO);
			
			if(itensVenda.size() > 0) {
				
				Double valorTotalDeCompra = 0D;
				Double valorTotalDeVenda = 0D;
				for (ItemVenda itemVenda : itensVenda) {
					valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
					valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
				}
				
				if(venda.getDesconto() != null) {
					valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
				}
				
				Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
				BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
				this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				System.out.println("Margem de Lucro: " + margemDeLucro);
				System.out.println("Lucro Real: " + lucroReal);
				
				if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
					venda.setTaxaDeComissao(new BigDecimal(1));
				}	
				
				if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
					venda.setTaxaDeComissao(new BigDecimal(1.5));
				}
				
				if(this.margemDeLucro.doubleValue() > 29) {
					venda.setTaxaDeComissao(new BigDecimal(2));
				}
				
			} else {
				this.margemDeLucro = BigDecimal.ZERO;
				venda.setTaxaDeComissao(BigDecimal.ZERO);
			}

		} else {
			//PrimeFaces.current().executeScript(
					//"swal({ type: 'error', title: 'Erro!', text: 'Não é possível remover os itens desta venda!' });");
			
			//itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);

			//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
			/*
			ItemCompra itemCompra = itensCompras.porCompra(itemSelecionado.getCompra(), itemSelecionado.getProduto());
			if(itemSelecionado.getId() != null) {
				ItemVenda itemVenda__ = itensVendas.porId(itemSelecionado.getId());
				itensCompraTemp.get(itensCompraTemp.indexOf(itemCompra)).setQuantidadeDisponivel(new BigDecimal(itemCompra.getQuantidadeDisponivel().doubleValue() + itemVenda__.getQuantidade().doubleValue()));
			} else {
				//itensCompraTemp.get(itensCompraTemp.indexOf(itemCompra)).setQuantidadeDisponivel(new BigDecimal(itemCompra.getQuantidadeDisponivel().doubleValue() + itemSelecionado.getQuantidade().doubleValue()));
			}*/

			if(itemVenda.getProduto().getId() != null && itemVenda.getProduto().equals(itemSelecionado.getProduto())) {
			
				List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
				
				itensCompra = new ArrayList<ItemCompra>();
	
				for (ItemCompra itemCompraTemp : itensCompraTemp) {
					itemCompraTemp.getCompra()
							.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
					
					List<ItemVenda> itensVendaTemp = itensVendas.porVenda(venda);
					for (ItemVenda itemVenda : itensVendaTemp) {
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVenda.getQuantidade().doubleValue()));
				
							}
						}
					}
					
					for (ItemVenda itemVenda : itensVenda) {
						
						//if(itemVenda.isNovo()) {
							
							/*if(itemVenda.getId() != null) {
								ItemVenda itemVendaTemp = itensVendas.porId(itemVenda.getId());
								if (itemCompraTemp.getCompra().getId().longValue() == itemVendaTemp.getCompra().getId()
										.longValue()) {
									if (itemCompraTemp.getProduto().getId().longValue() == itemVendaTemp.getProduto().getId()
											.longValue()) {
										itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
												itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaTemp.getQuantidade().doubleValue()));
						
									}
								}							
							}*/
							
							if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
									.longValue()) {
								if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
										.longValue()) {
									
									itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
								}
							}
							
						//}
					}
					
					
					if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
						nf = new DecimalFormat("###,##0.000", REAL);
						itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
								itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
		
					} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
						nf = new DecimalFormat("###,##0.0", REAL);
						itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
								itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
					
					} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
						nf = new DecimalFormat("###,##0", REAL);
						itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
								itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
					}
				}
				
				nf = new DecimalFormat("###,##0.00", REAL);
	
				itensCompra.addAll(itensCompraTemp);
				
				itensCompraTemp = new ArrayList<>();
				for (int i = itensCompra.size() - 1; i >= 0; i--) {
					itensCompra.get(i).setValorUnitarioFormatado(
							"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
					
					//itensCompraTemp.add(itensCompra.get(i));
					if (itensCompra.get(i).getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompraTemp.add(itensCompra.get(i));
					}
				}
	
				itensCompra = new ArrayList<>();
				
				itensCompra.addAll(itensCompraTemp);	
	
				itemSelecionado = null;
			}
			
			
			
			
			this.margemDeLucro = BigDecimal.ZERO;
			venda.setTaxaDeComissao(BigDecimal.ZERO);
			
			if(itensVenda.size() > 0) {
				
				Double valorTotalDeCompra = 0D;
				Double valorTotalDeVenda = 0D;
				for (ItemVenda itemVenda : itensVenda) {
					valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
					valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
				}
				
				if(venda.getDesconto() != null) {
					valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
				}
				
				Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
				BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
				this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				System.out.println("Margem de Lucro: " + margemDeLucro);
				System.out.println("Lucro Real: " + lucroReal);
				
				if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
					venda.setTaxaDeComissao(new BigDecimal(1));
				}	
				
				if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
					venda.setTaxaDeComissao(new BigDecimal(1.5));
				}
				
				if(this.margemDeLucro.doubleValue() > 29) {
					venda.setTaxaDeComissao(new BigDecimal(2));
				}
				
			} else {
				this.margemDeLucro = BigDecimal.ZERO;
				venda.setTaxaDeComissao(BigDecimal.ZERO);
			}
		}
		
		Long totalDeItens = 0L;
		for (ItemVenda itemVenda : itensVenda) {
			
			if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt") && !itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {
				totalDeItens += itemVenda.getQuantidade().longValue();				
			} else {
				totalDeItens += 1;
			}
		}	
		
		venda.setTaxaDeEntrega(venda.getTaxaDeEntrega() != null ? venda.getTaxaDeEntrega() : BigDecimal.ZERO);
		venda.setQuantidadeItens(totalDeItens);
	}

	public void editarItem() {

		if (venda.getId() == null) {

			itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);

			ItemCompra itemCompra__ = itensCompras.porCompra(itemSelecionado.getCompra(), itemSelecionado.getProduto());
			
			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());

			itensCompra = new ArrayList<ItemCompra>();

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

				boolean produtoNaLista = false;
				for (ItemVenda itemVenda : itensVenda) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {

							produtoNaLista = true;
							if (itemVenda.getId() == null && venda.getId() == null) {
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
							
							}

						}
					}
				}

				if (produtoNaLista != false) {
					if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}

				if (produtoNaLista != true) {
					if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}
				
				if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
					nf = new DecimalFormat("###,##0.000", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
	
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
					nf = new DecimalFormat("###,##0.0", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
				
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
					nf = new DecimalFormat("###,##0", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
				}
			}
			
			nf = new DecimalFormat("###,##0.00", REAL);

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);

			itemSelecionado = null;
			
			this.itemCompra = itensCompra.get(itensCompra.indexOf(itemCompra__));

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();
			
			
			this.margemDeLucro = BigDecimal.ZERO;
			venda.setTaxaDeComissao(BigDecimal.ZERO);
			
			if(itensVenda.size() > 0) {
				
				Double valorTotalDeCompra = 0D;
				Double valorTotalDeVenda = 0D;
				for (ItemVenda itemVenda : itensVenda) {
					valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
					valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
				}
				
				if(venda.getDesconto() != null) {
					valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
				}
				
				Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
				BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
				this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				System.out.println("Margem de Lucro: " + margemDeLucro);
				System.out.println("Lucro Real: " + lucroReal);
				
				if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
					venda.setTaxaDeComissao(new BigDecimal(1));
				}	
				
				if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
					venda.setTaxaDeComissao(new BigDecimal(1.5));
				}
				
				if(this.margemDeLucro.doubleValue() > 29) {
					venda.setTaxaDeComissao(new BigDecimal(2));
				}
				
			} else {
				this.margemDeLucro = BigDecimal.ZERO;
				venda.setTaxaDeComissao(BigDecimal.ZERO);
			}

		} else {
			//PrimeFaces.current().executeScript(
			//		"swal({ type: 'error', title: 'Erro!', text: 'Não é possível editar os itens desta venda!' });");
		
		
			itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);
						
			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
			
			ItemCompra itemCompra = itensCompras.porCompra(itemSelecionado.getCompra(), itemSelecionado.getProduto());
			/*if(itemSelecionado.getId() != null) {
				ItemVenda itemVenda__ = itensVendas.porId(itemSelecionado.getId());
				itensCompraTemp.get(itensCompraTemp.indexOf(itemCompra)).setQuantidadeDisponivel(new BigDecimal(itemCompra.getQuantidadeDisponivel().doubleValue() + itemVenda__.getQuantidade().doubleValue()));
			} else {
				//itensCompraTemp.get(itensCompraTemp.indexOf(itemCompra)).setQuantidadeDisponivel(new BigDecimal(itemCompra.getQuantidadeDisponivel().doubleValue() + itemSelecionado.getQuantidade().doubleValue()));
			}*/
			
			
			itensCompra = new ArrayList<ItemCompra>();

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
				
				
				
				List<ItemVenda> itensVendaTemp = itensVendas.porVenda(venda);
				for (ItemVenda itemVenda : itensVendaTemp) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
							.longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
							itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
									itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVenda.getQuantidade().doubleValue()));
			
						}
					}
				}
				
				for (ItemVenda itemVenda : itensVenda) {									
						
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
							}
						}

				}
				
				
				
				/*
				for (ItemVenda itemVenda : itensVenda) {
					
					if(itemVenda.isNovo()) {
						
						if(itemVenda.getId() != null) {
							ItemVenda itemVendaTemp = itensVendas.porId(itemVenda.getId());
							if (itemCompraTemp.getCompra().getId().longValue() == itemVendaTemp.getCompra().getId()
									.longValue()) {
								if (itemCompraTemp.getProduto().getId().longValue() == itemVendaTemp.getProduto().getId()
										.longValue()) {
									itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaTemp.getQuantidade().doubleValue()));
					
								}
							}							
						}
						
						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
							}
						}
						
					}
				}*/
				
				
				if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Kg") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Lt")) {
					nf = new DecimalFormat("###,##0.000", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
	
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Pt")) {
					nf = new DecimalFormat("###,##0.0", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
				
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx") || itemCompraTemp.getProduto().getUnidadeMedida().equals("FARDO")) {
					nf = new DecimalFormat("###,##0", REAL);
					itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
							itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
				}
			}
			
			itensCompra.addAll(itensCompraTemp);
			
			nf = new DecimalFormat("###,##0.00", REAL);

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				//itensCompraTemp.add(itensCompra.get(i));
				
				if (itensCompra.get(i).getQuantidadeDisponivel().doubleValue() > 0) {
					itensCompraTemp.add(itensCompra.get(i));
				}
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);

			itemSelecionado = null;
			
			this.itemCompra = itensCompra.get(itensCompra.indexOf(itemCompra));
			
			
			
			
			this.margemDeLucro = BigDecimal.ZERO;
			venda.setTaxaDeComissao(BigDecimal.ZERO);
			
			if(itensVenda.size() > 0) {
				
				Double valorTotalDeCompra = 0D;
				Double valorTotalDeVenda = 0D;
				for (ItemVenda itemVenda : itensVenda) {
					valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
					valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
				}
				
				if(venda.getDesconto() != null) {
					valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
				}
				
				Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
				BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
				this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				System.out.println("Margem de Lucro: " + margemDeLucro);
				System.out.println("Lucro Real: " + lucroReal);
				
				if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
					venda.setTaxaDeComissao(new BigDecimal(1));
				}	
				
				if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
					venda.setTaxaDeComissao(new BigDecimal(1.5));
				}
				
				if(this.margemDeLucro.doubleValue() > 29) {
					venda.setTaxaDeComissao(new BigDecimal(2));
				}
				
			} else {
				this.margemDeLucro = BigDecimal.ZERO;
				venda.setTaxaDeComissao(BigDecimal.ZERO);
			}
			
		}
		
		Long totalDeItens = 0L;
		for (ItemVenda itemVenda : itensVenda) {
			
			if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt") && !itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {
				totalDeItens += itemVenda.getQuantidade().longValue();				
			} else {
				totalDeItens += 1;
			}
		}	
		
		venda.setTaxaDeEntrega(venda.getTaxaDeEntrega() != null ? venda.getTaxaDeEntrega() : BigDecimal.ZERO);
		venda.setQuantidadeItens(totalDeItens);
	}
	
	
	

	public void aplicarDescontoVenda() throws IOException {
		
		if(venda.getDesconto() != null) {
			
			this.margemDeLucro = BigDecimal.ZERO;
			venda.setTaxaDeComissao(BigDecimal.ZERO);
			
			if(itensVenda.size() > 0) {
				
				Double valorTotalDeCompra = 0D;
				Double valorTotalDeVenda = 0D;
				for (ItemVenda itemVenda : itensVenda) {
					valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
					valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
				}
				
				if(venda.getDesconto() != null) {
					valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
				}
				
				Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
				BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
				this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				System.out.println("Margem de Lucro: " + margemDeLucro);
				System.out.println("Lucro Real: " + lucroReal);
				
				if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
					venda.setTaxaDeComissao(new BigDecimal(1));
				}	
				
				if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
					venda.setTaxaDeComissao(new BigDecimal(1.5));
				}
				
				if(this.margemDeLucro.doubleValue() > 29) {
					venda.setTaxaDeComissao(new BigDecimal(2));
				}
				
			} else {
				this.margemDeLucro = BigDecimal.ZERO;
				venda.setTaxaDeComissao(BigDecimal.ZERO);
			}
			
			
			
			if(venda.getDesconto().doubleValue() >= 0) {
				if(venda.getDesconto().doubleValue() <= venda.getValorTotal().doubleValue()) {
					
					//venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
					finalizar();
					
				} else {
					venda.setDesconto(null);
					//venda.setValorTotalComDesconto(venda.getValorTotal());
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor menor ou igual ao valor da venda!', timer: 3000 });");
				}
			} else {
				venda.setDesconto(null);
				//venda.setValorTotalComDesconto(venda.getValorTotal());
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor maior ou igual a R$ 0,00!', timer: 3000 });");
			}					
		} else {
			
			
			
			this.margemDeLucro = BigDecimal.ZERO;
			venda.setTaxaDeComissao(BigDecimal.ZERO);
			
			if(itensVenda.size() > 0) {
				
				Double valorTotalDeCompra = 0D;
				Double valorTotalDeVenda = 0D;
				for (ItemVenda itemVenda : itensVenda) {
					valorTotalDeCompra += itemVenda.getValorCompra().doubleValue();
					valorTotalDeVenda += itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue();
				}
				
				if(venda.getDesconto() != null) {
					valorTotalDeVenda = valorTotalDeVenda - venda.getDesconto().doubleValue();
				}
				
				Double margemDeLucro = (valorTotalDeVenda.doubleValue() - valorTotalDeCompra.doubleValue()) / valorTotalDeVenda.doubleValue();
				BigDecimal lucroReal = new BigDecimal(margemDeLucro.doubleValue() * valorTotalDeVenda.doubleValue()).setScale(4, BigDecimal.ROUND_HALF_EVEN);
				this.margemDeLucro = new BigDecimal(margemDeLucro.doubleValue() * 100).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				System.out.println("Margem de Lucro: " + margemDeLucro);
				System.out.println("Lucro Real: " + lucroReal);
				
				if(this.margemDeLucro.doubleValue() > 9 && this.margemDeLucro.doubleValue() < 20) {
					venda.setTaxaDeComissao(new BigDecimal(1));
				}	
				
				if(this.margemDeLucro.doubleValue() > 19 && this.margemDeLucro.doubleValue() < 30) {
					venda.setTaxaDeComissao(new BigDecimal(1.5));
				}
				
				if(this.margemDeLucro.doubleValue() > 29) {
					venda.setTaxaDeComissao(new BigDecimal(2));
				}
				
			} else {
				this.margemDeLucro = BigDecimal.ZERO;
				venda.setTaxaDeComissao(BigDecimal.ZERO);
			}
			
			
			
	
			finalizar();	
			
		}	
		
		calculaAcrescimo();
	}
	
	
	public void aplicarDescontoVenda_() throws IOException {
		
		if(venda.getDesconto() != null) {
			
			if(venda.getDesconto().doubleValue() >= 0) {
				if(venda.getDesconto().doubleValue() <= venda.getValorTotal().doubleValue()) {
					
					//venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
					salvar();
					
				} else {
					venda.setDesconto(null);
					//venda.setValorTotalComDesconto(venda.getValorTotal());
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor menor ou igual ao valor da venda!', timer: 3000 });");
				}
			} else {
				venda.setDesconto(null);
				//venda.setValorTotalComDesconto(venda.getValorTotal());
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor maior ou igual a R$ 0,00!', timer: 3000 });");
			}						
		} else {
	
			salvar();	
			
		}	
	}
	
	
	
	public void calculaAcrescimo() {
		
		Double subtotal = 0D;
		
		totalDeAcrescimo = BigDecimal.ZERO;
		
		Double lucro = 0D;
		
		for (ItemVenda itemVenda : itensVenda) {			
			subtotal += itemVenda.getTotal().doubleValue();
			lucro += itemVenda.getLucro().doubleValue();
		}
		
		
		
		if(venda.getId() != null) {
			venda.setLucro(BigDecimal.valueOf(lucro));
			if(venda.getDesconto() != null) {
				venda.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
			}
		}
		
		
		
		
		
		
		if(!entrega) {
			venda.setEntregador(null);
			venda.setTaxaDeEntrega(BigDecimal.ZERO);
			changeStatusPago();
		}
		
		BigDecimal taxaDeEntrega = BigDecimal.ZERO;
		
		if(venda.getCliente().getId() != null) {
			taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;
			
		} 
		
		if(venda.getTaxaDeEntrega().doubleValue() > 0) {
			taxaDeEntrega = venda.getTaxaDeEntrega();
		}
		
		subtotal += taxaDeEntrega.doubleValue();
		
		
		
		System.out.println(subtotal + " - " + venda.getFormaPagamento().getAcrescimo());
		
		//venda.setValorTotal(new BigDecimal(subtotal.doubleValue() + totalDeAcrescimo.doubleValue()));
		//venda.setValorTotalComDesconto(venda.getValorTotal());
		
		//if(venda.getId() != null) {
			if(venda.getDesconto() != null) {
				venda.setValorTotalComDesconto(new BigDecimal(subtotal.doubleValue() - venda.getDesconto().doubleValue()));
			} else {
				venda.setValorTotalComDesconto(new BigDecimal(subtotal.doubleValue()));
			}			
		//}
			
			if(venda.getFormaPagamento().getAcrescimo().doubleValue() > 0 && venda.getFormaPagamento().isClientePaga()) {
				totalDeAcrescimo = new BigDecimal(venda.getValorTotalComDesconto().doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100));		
			}
			
		if(tipoPagamento == TipoPagamento.PARCELADO) {
			
			if(venda.getFormaPagamento().isClientePaga()) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotalComDesconto().doubleValue() + venda.getValorTotalComDesconto().doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100)));				
			}
			
			
			if(venda.getId() != null) {
				if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
					venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
				}
			}
			
			zerarParcelas();
		}	
		
		venda.setAcrescimo(venda.getFormaPagamento().getAcrescimo());
		
		venda.setTaxaDeAcrescimo(venda.getFormaPagamento().getAcrescimo());
		
		venda.setTaxaDeEntrega(taxaDeEntrega);
		
		if(!trocaPendente) {
			
			//faltando = venda.getValorTotal().doubleValue();
			
		}
		
		pagamento = new Pagamento();
		
		
		changeFormaPagamento();
	}
	
	
	

	public void changeFormaPagamento() {
		
		try {
			totalPagamentos = pagamentosAdicionados.stream().map((x) -> x.getValor()).reduce((x, y) -> x.add(y)).get();
			totalTroco = pagamentosAdicionados.stream().map((x) -> x.getTroco()).reduce((x, y) -> x.add(y)).get();
			totalPagamentos = new BigDecimal(totalPagamentos.doubleValue() - totalTroco.doubleValue());
		} catch (NoSuchElementException e) {}
		
		if(trocaPendente) {
			
			Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
			faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
			
			if(faltando < 0) {
				troco = -1 * faltando.doubleValue();
				faltando = 0D;			
			}	
		}
		
		valorRecebido = null;
		faltando = venda.getValorTotalComDesconto().doubleValue() - totalPagamentos.doubleValue();
		troco = totalTroco.doubleValue();
		
		/*
		vendaPaga = true;
		
		if (!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
			//PrimeFaces.current().executeScript("ocultarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		} else {		
			//PrimeFaces.current().executeScript("mostrarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		}
		*/
	}
	
	
	

	public void changeStatusPago() {
		
		if(/*venda.getFormaPagamento().getNome().equals("Dinheiro") &&*/entrega != true) {
			
			if(tipoPagamento == TipoPagamento.AVISTA) {
				
				if (!vendaPaga) {
					
					if(valorRecebido == null) {
						//Comentado para correção
						//valorRecebido = 0D;
					}
					
					PrimeFaces.current().executeScript("ocultarValores();");
					PrimeFaces.current().executeScript("mostrarParcelaUnica();");
					
					if(pagamentoPara == null) {
						pagamentoPara = venda.getDataVenda();
						pagamentoParaEmString = sdf.format(pagamentoPara);
					}				
					
				} else {
		
					PrimeFaces.current().executeScript("mostrarValores();");
					PrimeFaces.current().executeScript("ocultarParcelaUnica();");
				}
			}
	
		}
		
		try {
			totalPagamentos = pagamentosAdicionados.stream().map((x) -> x.getValor()).reduce((x, y) -> x.add(y)).get();
			totalTroco = pagamentosAdicionados.stream().map((x) -> x.getTroco()).reduce((x, y) -> x.add(y)).get();
			totalPagamentos = new BigDecimal(totalPagamentos.doubleValue() - totalTroco.doubleValue());
		} catch (NoSuchElementException e) {}
	}
	
	

	public void finalizar() throws IOException { 
		
		if(!venda.isPrevenda()) {
			PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Ops!', text: 'Não é possível alterar esta venda, venda finalizada !' });");
			return;
		}
		
		if (itensVenda.size() > 0) {						
									
			if(venda.isAjuste()) {
				
				tipoPagamento = null;
				valorRecebido = 0D;
				troco = 0D;
				vendaPaga = true;
				
				//salvar_(true);
				
			} else {		
				
				pagamento = new Pagamento();
				
				if(venda.getId() == null) {
					tipoPagamento = TipoPagamento.AVISTA;
					parcelasConfirmadas = true;
					
					valorRecebido = null;
					troco = 0D;
					vendaPaga = true;
					
					if(!trocaPendente) {
						faltando = venda.getValorTotalComDesconto().doubleValue();
					} else {
						faltando = venda.getValorTotalComDesconto().doubleValue() - saldoParaTroca.doubleValue();
						
						if(faltando < 0) {
							troco = -1 * faltando.doubleValue();
							faltando = 0D;			
						}	
					}
					
					pagamentosAdicionados = new ArrayList<Pagamento>();
					totalPagamentos = BigDecimal.ZERO;
					
					todasContas = new ArrayList<>();

					valorEntrada = null;
					
					if(entrega) {
						vendaPaga = false;
					}					
					
					PrimeFaces.current().executeScript("PF('confirmDialog').show();");							
						
				} else {
					 
					Venda vendaTemp = vendas.porId(venda.getId());
					if(!vendaTemp.getFormaPagamento().getNome().equals(venda.getFormaPagamento().getNome()) && !venda.getFormaPagamento().getNome().equals("Dinheiro")) {
						
						tipoPagamento = TipoPagamento.AVISTA;
						parcelasConfirmadas = false;
						valorRecebido = null;
						troco = 0D;
						vendaPaga = true;
						
						faltando = venda.getValorTotalComDesconto().doubleValue();
						
						
						//faltando = venda.getValorTotalComDesconto().doubleValue() - saldoParaTroca.doubleValue() - valorRecebido - (pagamento.getId() != null ? pagamento.getValor().doubleValue() : 0);
						
						todasContas = new ArrayList<>();

						valorEntrada = null;
						
						if(entrega) {
							vendaPaga = false;
						}
						
						parcelasConfirmadas = true;
						
						PrimeFaces.current().executeScript("PF('confirmDialog').show();");
						
					 } else if(venda.getTipoPagamento() == null) {
							
							tipoPagamento = TipoPagamento.AVISTA;
							parcelasConfirmadas = false;
							valorRecebido = null;
							troco = 0D;
							vendaPaga = true;
							
							faltando = venda.getValorTotalComDesconto().doubleValue() - saldoParaTroca.doubleValue();
							
							todasContas = new ArrayList<>();

							valorEntrada = null;
							
							if(entrega) {
								vendaPaga = false;
							}
							
							parcelasConfirmadas = true;
							
							PrimeFaces.current().executeScript("PF('confirmDialog').show();");
							
						 } else {
							 
							tipoPagamento = TipoPagamento.AVISTA;
							 
							faltando = venda.getValorTotalComDesconto().doubleValue();//vendaTemp.getFaltando().doubleValue();
							troco = 0D;//vendaTemp.getTroco().doubleValue();
							
							pagamentosAdicionados = new ArrayList<Pagamento>();
							totalPagamentos = BigDecimal.ZERO;
							
							FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro", usuario.getEmpresa());
							venda.setFormaPagamento(formaPagamento);
							
							vendaPaga = true;
							
							pagamentoPara = venda.getDataVenda();
							pagamentoParaEmString = sdf.format(pagamentoPara);
							
							parcelasConfirmadas = true;
						 
							List<ItemDevolucao> listaDevolucao = itensDevolucoes.porVenda(venda);
							if(listaDevolucao.size() == 0) {
								
								PrimeFaces.current().executeScript("PF('confirmDialog').show();");
							
							} else {
								
								PrimeFaces.current().executeScript(
										"swal({ type: 'error', title: 'Ops!', text: 'Não é possível alterar esta venda, já existem devoluções registradas!' });");
							}
					 }
				}				
							
			}
		
		} else {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!', timer: 1500 });");
		}
		
	}
	
	
	
	public void preparaPagamento() {
		
		if(valorRecebido != null) {
			
			if(valorRecebido.doubleValue() > 0) {
				PrimeFaces.current().executeScript(
						"PF('pagamento-dialog').show();");
			} else {
				PrimeFaces.current().executeScript(
						"Toast.fire({ " +
						  "icon: 'error', " +
						  "title: 'Valor recebido deve ser maior que 0.'" +
						"}) ");
			}
			
		} else {
			
			if(entrega != true) {
								
				PrimeFaces.current().executeScript(
						"Toast.fire({ " +
						  "icon: 'error', " +
						  "title: 'Valor recebido deve ser informado.'" +
						"}) ");
							
			} else {
				
				if(!vendaPaga) {
					PrimeFaces.current().executeScript(
							"Toast.fire({ " +
							  "icon: 'error', " +
							  "title: 'Valor a receber deve ser informado.'" +
							"}) ");
				} else {
					PrimeFaces.current().executeScript(
							"Toast.fire({ " +
							  "icon: 'error', " +
							  "title: 'Valor recebido deve ser informado.'" +
							"}) ");
				}
			}
		}
	}

	
	
	public void calculaTroco() throws IOException {
		
 		totalPagamentos = BigDecimal.ZERO;				
		try {
			totalPagamentos = pagamentosAdicionados.stream().map((x) -> x.getValor()).reduce((x, y) -> x.add(y)).get();
		} catch (NoSuchElementException e) {
			
		}
		
		trocoParcial = 0D;
		
		totalTroco = BigDecimal.ZERO;
		try {
			totalTroco = pagamentosAdicionados.stream().map((x) -> x.getTroco()).reduce((x, y) -> x.add(y)).get();
		} catch (NoSuchElementException e) {
			
		}
		
		if((totalPagamentos.doubleValue() - totalTroco.doubleValue()) == venda.getValorTotalComDesconto().doubleValue()) {
			if(valorRecebido != null) {
				adicionarPagamento(true);
			} 
		} else {
		
			Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
			faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + (totalPagamentos.doubleValue() - totalTroco.doubleValue()));			
			
			if(valorRecebidoTemp.doubleValue() > faltando.doubleValue()) {
				trocoParcial = valorRecebidoTemp - faltando;
			} else {
				trocoParcial = 0D;
			}			
			
			faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue() + (totalPagamentos.doubleValue() - totalTroco.doubleValue()));
			troco = totalTroco.doubleValue() + trocoParcial.doubleValue();			
	
			if(faltando < 0) { 
				 //troco = (-1 * faltando.doubleValue()) + totalTroco.doubleValue();
				faltando = 0D; 
			}			
			
			/*faltando = venda.getValorTotal().doubleValue() - valorRecebido.doubleValue();
			
			if(faltando < 0D) {
				faltando = 0D;
			}
			
			
			troco = valorRecebido.doubleValue() - venda.getValorTotal().doubleValue();
			
			if(troco < 0D) {
				troco = 0D;
			}*/
		}
				
	}
	
	

	public void changeTipoPagamento() {
		
		/*valorEntrada = 0D;
		valorRecebido = 0D;
		troco = 0D;
		faltando = 0D;*/
		
		if (tipoPagamento == TipoPagamento.AVISTA) {
			
			calculaAcrescimo();
			
			contaAPagar = false;
			vendaPaga = true;
			parcelasConfirmadas = true;
			
			if(!trocaPendente) {
				//faltando = venda.getValorTotalComDesconto().doubleValue();
			} else {
							
				Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
				faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
				
				if(faltando < 0) {
					troco = -1 * faltando.doubleValue();
					faltando = 0D;			
				}	
			}
			
			try {
				totalPagamentos = pagamentosAdicionados.stream().map((x) -> x.getValor()).reduce((x, y) -> x.add(y)).get();
				totalTroco = pagamentosAdicionados.stream().map((x) -> x.getTroco()).reduce((x, y) -> x.add(y)).get();
				totalPagamentos = new BigDecimal(totalPagamentos.doubleValue() - totalTroco.doubleValue());
			} catch (NoSuchElementException e) {}
			
			PrimeFaces.current().executeScript("ocultar();");
			PrimeFaces.current().executeScript("mostrarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
			
		} else {
			
			venda.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario.getEmpresa()));
			
			if(todasContas.size() > 0) {
				parcelasConfirmadas = true;
			} else {
				parcelasConfirmadas = false;
			}
									
			//calculaAcrescimo();
			//-------confirmaCliente();
			contaAPagar = true;
			parcelas = 2L;
			periodoPagamento = PeriodoPagamento.MESES;
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(venda.getDataVenda());
			calendar.add(Calendar.MONTH, 1);
			
			primeiraParcela = calendar.getTime();
			
			primeiraParcelaEmString = sdf.format(calendar.getTime());
					
			PrimeFaces.current().executeScript("mostrar();");
			PrimeFaces.current().executeScript("ocultarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		}
	}
	
	
	
	
	public void gerarParcelas() throws ParseException {
		
		parcelasConfirmadas = false;

		todasContas = new ArrayList<>();

		entradas = new ArrayList<>();
		
		Calendar calendario = Calendar.getInstance();
		Calendar vencimento = Calendar.getInstance();
		vencimento.setTime(primeiraParcela);
		vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
		vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
		vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
		

		
		
		Double subTotal = 0D;
		Double custoMedio = 0D;
		Double lucro = 0D;		
		
		
		for(ItemVenda itemVenda : itensVenda) {
			
			if(venda.getId() == null) {
				//Comentado para correção
				/*
				Produto produto = produtos.porId(itemVenda.getProduto().getId());
				Double valorDeCustoUnitario = produto.getCustoMedioUnitario().doubleValue();
				itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));					
			
				if(produto.getQuantidadeAtual().doubleValue() <= 0 && valorDeCustoUnitario.doubleValue() <= 0) {
					valorDeCustoUnitario = (itemVenda.getProduto().getQuantidadeAtual().doubleValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()) / itemVenda.getProduto().getQuantidadeAtual().doubleValue();
				}
				
				if(produto.getQuantidadeAtual().doubleValue() <= 0) {
					if(!produto.isEstoque()) {
						valorDeCustoUnitario = 0D;
					}
				}
				
				double desconto = 0;				
				if(itemVenda.getDesconto() != null) {
					desconto = itemVenda.getDesconto().doubleValue() / 100;
				} else {
					itemVenda.setDesconto(BigDecimal.ZERO);
				}
				
				BigDecimal subtotal = BigDecimal.valueOf(
					itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue());					
				itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * desconto)));
	
				//Calculo do Lucro em valor e percentual 										
				Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
						* itemVenda.getQuantidade().doubleValue())
				.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
				//
				
			
				itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
						* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue())
						- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));
	
				itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100)));
				itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));
				*/
				
				subTotal += itemVenda.getTotal().doubleValue();
				custoMedio += itemVenda.getValorCompra().doubleValue();
				lucro += itemVenda.getLucro().doubleValue();
				
			} else {
				
				subTotal += itemVenda.getTotal().doubleValue();
				custoMedio += itemVenda.getValorCompra().doubleValue();
				lucro += itemVenda.getLucro().doubleValue();
			}
		}
		
		
		
		Double descontos = 0D;
		
		if(venda.getDesconto() != null) {
			descontos = venda.getDesconto().doubleValue();
		}
		
		subTotal -= descontos.doubleValue();
		lucro -= descontos.doubleValue();
		
		Double valorVenda = venda.getValorTotalComDesconto().doubleValue();//venda.getValorTotal().doubleValue() - descontos;
		Double taxaEntrega = venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue();
		
		if (valorEntrada != null && valorEntrada > 0) {
			valorVenda = venda.getValorTotalComDesconto().doubleValue() /*venda.getValorTotal().doubleValue()*/ - valorEntrada;

			Conta conta = new Conta();
			conta.setParcela("Entrada");
			conta.setValor(new BigDecimal(valorEntrada));
			
			conta.setVencimento(venda.getDataVenda());			
			conta.setPagamento(venda.getDataVenda());
			 
			Calendar calendarioTemp = Calendar.getInstance();
			//calendarioTemp.setTime(conta.getVencimento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			entradas.add(conta);
		}

		Double valorParcela = valorVenda / parcelas;
		
		Double subTotalParcela = subTotal / parcelas;
		Double custoMedioParcela = custoMedio / parcelas;
		Double lucroParcela = lucro / parcelas;
		
		Double taxaEntregaParcela = taxaEntrega / parcelas;
		
		
		
		

		if (valorParcela > 0) {
			for (int i = 0; i < parcelas; i++) {
				
				if(i > 0) {
					
					long dias = parcelas;
					if (periodoPagamento == PeriodoPagamento.MESES) {
						// dias = 30;
						// vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
						vencimento.add(Calendar.MONTH, 1);
					}

					if (periodoPagamento == PeriodoPagamento.QUINZENAS) {
						dias = 15;
						vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					}

					if (periodoPagamento == PeriodoPagamento.SEMANAS) {
						dias = 7;
						vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					}

					/*if (periodoPagamento == PeriodoPagamento.DIAS) {
						dias = i + 1;
						vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					}*/
				}

				

				Conta conta = new Conta();
				conta.setParcela((i + 1) + "/" + parcelas);
				conta.setStatus(false);

				if (i == parcelas - 1) {

					Double valorTemp = 0D;
					
					Double subTotalTemp = 0D;
					Double custoMedioTemp = 0D;
					Double lucroTemp = 0D;
					Double taxaEntregaTemp = 0D;
					
					DecimalFormat fmt = new DecimalFormat("0.00");
					for (int j = 0; j < i; j++) {
						valorTemp += Double.parseDouble(fmt.format(valorParcela).replace(",", "."));
						
						subTotalTemp += Double.parseDouble(fmt.format(subTotalParcela).replace(",", "."));
						custoMedioTemp += Double.parseDouble(fmt.format(custoMedioParcela).replace(",", "."));
						lucroTemp += Double.parseDouble(fmt.format(lucroParcela).replace(",", "."));
						
						taxaEntregaTemp += Double.parseDouble(fmt.format(taxaEntregaParcela).replace(",", "."));
					}

					conta.setValor(new BigDecimal(valorVenda - valorTemp));
					
					conta.setSubTotal(new BigDecimal(subTotal - subTotalTemp));
					conta.setCustoMedio(new BigDecimal(custoMedio - custoMedioTemp));
					conta.setLucro(new BigDecimal(lucro - lucroTemp));
					
					conta.setTaxaEntrega(new BigDecimal(taxaEntrega - taxaEntregaTemp));

				} else {
					conta.setValor(new BigDecimal(valorParcela));
					
					conta.setSubTotal(new BigDecimal(subTotalParcela));
					conta.setCustoMedio(new BigDecimal(custoMedioParcela));
					conta.setLucro(new BigDecimal(lucroParcela));
					
					conta.setTaxaEntrega(new BigDecimal(taxaEntrega));
				}

				conta.setVencimento(vencimento.getTime());
				
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getVencimento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				todasContas.add(conta);
			}
		}
	}
	
	
	public void adicionarPagamento(boolean salvar) throws IOException {	
		
		Number pagamentosRealizados = pagamentos.totalPagoPorVenda(venda, usuario.getEmpresa());

		if((totalPagamentos.doubleValue() - totalTroco.doubleValue()) == venda.getValorTotalComDesconto().doubleValue()) {
			
			if(valorRecebido != null) {
				
				valorRecebido = null;
				
				calculaTroco();
				
				PrimeFaces.current().executeScript(
						"Toast_.fire({ " +
						  "icon: 'error', " +
						  "title: 'Não é possível adicionar pagamento!'" +
						"}) ");
			} else {

				if(salvar) {
					finalizarVenda();
				}
			}		
		
		} else {
			
			valorRecebido = valorRecebido != null ? valorRecebido : 0;
			pagamento.setValor(new BigDecimal(valorRecebido));
			
			if(valorRecebido > 0) {				
				
				if(venda.getFormaPagamento().getNome().equals("Dinheiro")) {
					
					valorRecebido = null;
					
					Pagamento pagamentoTemp = new Pagamento();
					pagamentoTemp.setValor(new BigDecimal(pagamento.getValor().doubleValue()));
					
					try {
						Thread.sleep(100);
						pagamentoTemp.setCode("" + new Date().getTime());
					} catch (InterruptedException e) {
					}

					pagamentoTemp.setTroco(new BigDecimal(trocoParcial));
					pagamentoTemp.setAcrescimo(BigDecimal.ZERO);
					pagamentoTemp.setTaxaDeAcrescimo(BigDecimal.ZERO);
					pagamentoTemp.setValorDeAcrescimo(BigDecimal.ZERO);
					
					pagamentoTemp.setVenda(venda);
					pagamentoTemp.setFormaPagamento(venda.getFormaPagamento());
					pagamentosAdicionados.add(pagamentoTemp);
										
					PrimeFaces.current().executeScript(
							"Toast_.fire({ " +
							  "icon: 'success', " +
							  "title: 'Pagamento adicionado!'" +
							"}) ");	
							
				} else {

					//valorRecebido = null;
					
					if(trocoParcial.doubleValue() > 0) {
						
						calculaTroco();
						
						PrimeFaces.current().executeScript(
								"Toast_.fire({ " +
								  "icon: 'error', " +
								  "title: 'O valor informado é maior que o valor que está faltando!'" +
								"}) ");
						
					} else {
						
						Pagamento pagamentoTemp = new Pagamento();
						pagamentoTemp.setValor(new BigDecimal(pagamento.getValor().doubleValue()));
						
						try {
							Thread.sleep(100);
							pagamentoTemp.setCode("" + new Date().getTime());
						} catch (InterruptedException e) {
						}
						
						pagamentoTemp.setTroco(BigDecimal.ZERO);
						pagamentoTemp.setAcrescimo(BigDecimal.ZERO);
						pagamentoTemp.setTaxaDeAcrescimo(BigDecimal.ZERO);
						pagamentoTemp.setValorDeAcrescimo(BigDecimal.ZERO);
						
						pagamentoTemp.setVenda(venda);
						pagamentoTemp.setFormaPagamento(venda.getFormaPagamento());
						pagamentosAdicionados.add(pagamentoTemp);
						
						valorRecebido = null;
						
						PrimeFaces.current().executeScript(
								"Toast_.fire({ " +
								  "icon: 'success', " +
								  "title: 'Pagamento adicionado!'" +
								"}) ");
					}					
				}		
				
			} else {
				
				PrimeFaces.current().executeScript(
						"Toast_.fire({ " +
						  "icon: 'error', " +
						  "title: 'O valor informado deve ser maior que R$ 0,00!'" +
						"}) ");
				
				valorRecebido = null;
				pagamento.setVenda(null);				
			}
		}
		
		try {
			totalPagamentos = pagamentosAdicionados.stream().map((x) -> x.getValor()).reduce((x, y) -> x.add(y)).get();
			totalTroco = pagamentosAdicionados.stream().map((x) -> x.getTroco()).reduce((x, y) -> x.add(y)).get();
			totalPagamentos = new BigDecimal(totalPagamentos.doubleValue() - totalTroco.doubleValue());
		} catch (NoSuchElementException e) {}
		
	}


	public void removePagamento(Pagamento pagamento) throws IOException {
			
		pagamentosAdicionados.remove(pagamento);		
		valorRecebido = null;
		
		calculaTroco();
		
		try {
			totalPagamentos = pagamentosAdicionados.stream().map((x) -> x.getValor()).reduce((x, y) -> x.add(y)).get();
			totalTroco = pagamentosAdicionados.stream().map((x) -> x.getTroco()).reduce((x, y) -> x.add(y)).get();
			totalPagamentos = new BigDecimal(totalPagamentos.doubleValue() - totalTroco.doubleValue());
		} catch (NoSuchElementException e) {}
	}
	
	
	
	public void atualizaPrimeiraParcela() {
		
		zerarParcelas();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(venda.getDataVenda());
		
		if (periodoPagamento == PeriodoPagamento.MESES) {
			calendar.add(Calendar.MONTH, 1);
		}

		if (periodoPagamento == PeriodoPagamento.QUINZENAS) {
			calendar.add(Calendar.DAY_OF_MONTH, (int) 15);
		}

		if (periodoPagamento == PeriodoPagamento.SEMANAS) {
			calendar.add(Calendar.DAY_OF_MONTH, (int) 7);
		}
			
		primeiraParcela = calendar.getTime();
		
		primeiraParcelaEmString = sdf.format(calendar.getTime());
		
	}
	
	

	public void finalizarVenda() throws IOException {		

		if(tipoPagamento == TipoPagamento.AVISTA) {
			
			if(vendaPaga || entrega) {
				
				if(faltando.doubleValue() == 0D) {

					adicionarPagamento(false);
					
					if(venda.getFormaPagamento().getNome().equals("Dinheiro") || (!venda.getFormaPagamento().getNome().equals("Dinheiro") && trocoParcial == 0)) {
						
						salvar(true); 
						/*
						PrimeFaces.current().executeScript(
							"Toast_.fire({ " +
							  "icon: 'success', " +
							  "title: 'Finalizando venda . . .'" +
							"}) ");
						*/			
					}			
					
				} else {
						
					PrimeFaces.current().executeScript(
						"Toast_.fire({ " +
						  "icon: 'error', " +
						  "title: 'Não foi possível salvar a venda. Está faltando R$ " + nf.format(faltando) + "!'" +
						"}) ");
				}
				
			} else {
				
				salvar(true); 
			}
			
			
		} else if(tipoPagamento == TipoPagamento.PARCELADO) {
			
			/* Salva venda e gera conta a receber. */
			System.out.println("Salva venda e gera conta a receber.");
			
			salvar(true);
			/*
			PrimeFaces.current().executeScript(
				"Toast_.fire({ " +
				  "icon: 'success', " +
				  "title: 'Pagamento adicionado!'" +
				"}) ");
			*/
		}

	}
	
	
	public void salvar(boolean pagarConta) throws IOException {
		
		if (venda.getId() != null) {
			
			List<Conta> contasTemp = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario.getEmpresa());
			for (Conta conta : contasTemp) {
				
				List<PagamentoConta> pagamentosConta = pagamentosContas.todosPorConta(conta, usuario.getEmpresa());
				for (PagamentoConta pagamentoConta : pagamentosConta) {
					pagamentosContas.remove(pagamentoConta);
				}
				
				contas.remove(conta);
			}
					
			List<Pagamento> pagamentosTemp = pagamentos.todosPorVenda(venda, usuario.getEmpresa());
			for (Pagamento pagamento : pagamentosTemp) {
				pagamentos.remove(pagamento);
			}
		}
		
		if(pagarConta) {
			venda.setStatusMesa("PAGO");
			mesa = new Mesa();
		}
		
		List<ItemVenda> itensVenda_ = new ArrayList<ItemVenda>();
		
		List<ItemVendaCompra> itensVendaCompraDefault = new ArrayList<ItemVendaCompra>();
		
		if(venda.getId() != null) {
			
			//List<ItemVenda> itensVenda = itensVendas.porVenda(venda, usuario.getEmpresa());
			
			//itensVenda_.addAll(itensVenda);
			
			
			List<ItemVenda> itensVendaOld = itensVendas.porVenda(venda);
			for (ItemVenda itemVendaOld : itensVendaOld) {
				
				List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVendaOld.getProduto());
				for (ItemCompra itemCompraTemp : itensCompraTemp) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVendaOld.getCompra().getId()
							.longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVendaOld.getProduto().getId()
								.longValue()) {
							itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
									itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaOld.getQuantidade().doubleValue()));
			
						}
					}
					
					itensCompras.save(itemCompraTemp);
				}	
			}

			/*for (ItemVenda itemVenda : itensVenda) {									
				
				List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
				for (ItemCompra itemCompraTemp : itensCompraTemp) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
							.longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
							itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
									itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
			
						}
					}
					
					itensCompras.save(itemCompraTemp);
				}

			}*/
			
			
			for (ItemVenda itemVenda : itensVendaOld) {
				
				//if(itemVenda.isEstoque()) {
					
						/*
					boolean contains = false;
					for (ItemVenda itemVenda_ : this.itensVenda) {
						
						if(itemVenda_.getId() != null) {
							if(itemVenda_.getId().longValue() == itemVenda.getId().longValue()) {
								contains = true;
							}
						}				
					}
					*/
					
					
					
					Produto produto = itemVenda.getProduto();
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()));
					
					if(venda.isAjuste()) {
						
						if(!venda.isRecuperarValores()) {
							
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
						/*if(itemVenda.getLucro().doubleValue() >= 0) {
							
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
							
						} else {
							
							BigDecimal total = itemVenda.getTotal();
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + total.doubleValue()));					
		
						}*/
						
						
					}
				
					produtos.save(produto);
					
					
					
					
					List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda_(itemVenda);
					
					/*for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {	
						
						ItemCompra itemCompra = itensCompras.porId(itemVendaCompra.getItemCompra().getId());
						
						System.out.println("Quantidade Disponivel: "+itemCompra.getQuantidadeDisponivel().doubleValue());
						System.out.println("Quantidade Retornada: "+itemVendaCompra.getQuantidade().doubleValue());
						
						itemCompra.setQuantidadeDisponivel(new BigDecimal(
								itemCompra.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));
						itensCompras.save(itemCompra);
					}*/
				
					
					
					
					//itensVendaCompraDefault = itensVendasCompras.porItemVenda_(itemVenda);
					
					//itemVenda.setItensVendaCompra(itensVendaCompraDefault);
					
					for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {
						itensVendasCompras.remove(itemVendaCompra);
					}
	
					//itemVenda.setItensVendaCompra(itensVendaCompra); 
					itensVendas.remove(itemVenda);				
					
					//produtos.save(produto);
					
				//} else {
					
					//itensVendas.remove(itemVenda);	
				//}
			}
			
			
			Entrega entrega = entregas.porVenda(venda);
			if (entrega.getId() != null) {
				entregas.remove(entrega);
			}
			

			//vendas.remove(venda);
		}
		
		
		
		
		long totalDeItens = 0L;
		double valorTotal = 0;
		double lucro = 0;
		//double percentualLucro = 0;
		double valorCompra = 0;
		
		setInformacoesDataLancamento();
		
		venda.setVendaPaga(vendaPaga);
		if(!pagarConta) {
			venda.setVendaPaga(pagarConta);
		}
		
		venda.setTipoPagamento(tipoPagamento);
		
		System.out.println(venda.getTipoPagamento());
		
		if(venda.getTipoPagamento() == TipoPagamento.AVISTA) {
			
			if(vendaPaga) {
				venda.setValorRecebido(totalPagamentos);
				venda.setFaltando(new BigDecimal(faltando));
				venda.setTroco(new BigDecimal(troco));
			} else {
				venda.setValorRecebido(BigDecimal.ZERO);
				venda.setFaltando(BigDecimal.ZERO);
				venda.setTroco(BigDecimal.ZERO);
			}
			
		} else {		
			venda.setValorRecebido(BigDecimal.ZERO);
			venda.setFaltando(BigDecimal.ZERO);
			venda.setTroco(BigDecimal.ZERO);
		}
			
		//venda.setTaxaDeEntrega(entrega ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO);
		if(entrega) {
			
			BigDecimal taxaDeEntrega = BigDecimal.ZERO;
			if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
				taxaDeEntrega = venda.getCliente().getBairro().getTaxaDeEntrega();			
				venda.setTaxaDeEntrega(taxaDeEntrega);
			} else {
				taxaDeEntrega = venda.getTaxaDeEntrega();	
				venda.setTaxaDeEntrega(taxaDeEntrega);
			}
			
		} else {
			venda.setTaxaDeEntrega(BigDecimal.ZERO);
		}
		
		if(!venda.isAjuste()) {
			venda.setRecuperarValores(false);
		}
	

		if (venda.getId() == null) {

			Venda vendaTemp = vendas.ultimoNVenda(usuario.getEmpresa());				
	
			if (vendaTemp == null) {
				venda.setNumeroVenda(1L);
			} else {
				if (venda.getId() == null) {
					venda.setNumeroVenda(vendaTemp.getNumeroVenda() + 1);
				}
			}
		}
		
		
		if (tipoPagamento == TipoPagamento.PARCELADO) {
			venda.setConta(true);
			venda.setVendaPaga(false);
		} else {
			venda.setConta((vendaPaga != true && !entrega) ? true : false);
			venda.setVendaPaga(vendaPaga);
			if(!pagarConta) {
				venda.setVendaPaga(pagarConta);
			}
		}
		
		if(!venda.isConta()) {
			//if(venda.getId() == null) {
				venda.setDataPagamento(new Date());
				setInformacoesDataPagamento();
			//}			
		} else {
			venda.setDataPagamento(null);
			
			venda.setDiaPagamento(null);
			venda.setNomeDiaPagamento(null);
			venda.setSemanaPagamento(null);
			venda.setMesPagamento(null);
			venda.setAnoPagamento(null);
		}
		
		if (tipoPagamento == TipoPagamento.PARCELADO) {
			if(entradas.size() > 0) {
				venda.setDataPagamento(new Date());
				setInformacoesDataPagamento();
			}
		}

		if(venda.getId() == null) {
			if(venda.isVendaPaga()) {
				venda.setDataPagamento(new Date());
				setInformacoesDataPagamento();
			}
		}
		
		venda.setPrevenda(false);
		venda.setPdv(false);
		venda.setEmpresa(usuario.getEmpresa());
		venda = vendas.save(venda);
		
		//List<ItemVenda> itensVendaTemp = new ArrayList<ItemVenda>();
		//itensVendaTemp.addAll(itensVenda);

		for (ItemVenda itemVenda : itensVenda) {
			
			itemVenda.setId(null); //correção
			
			if(itemVenda.getId() == null) {
				
				List<ItemVendaCompra> itensVendaCompra = itemVenda.getItensVendaCompra();
				
				itemVenda.setVenda(venda);
				itemVenda = itensVendas.save(itemVenda);
					

				for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
	
					itemVendaCompra.setItemVenda(itemVenda);
					itensVendasCompras.save(itemVendaCompra);
					
					ItemCompra itemCompra = itensCompras.porId(itemVendaCompra.getItemCompra().getId());
					itemCompra.setQuantidadeDisponivel(new BigDecimal(itemCompra.getQuantidadeDisponivel().doubleValue() - itemVenda.getQuantidade().doubleValue()));
					itensCompras.save(itemCompra);
				}
				
				
	
				Produto produto = produtos.porId(itemVenda.getProduto().getId());
								
				
				if(venda.isAjuste()) {
					
					if(!venda.isRecuperarValores()) {
						//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));								
						//}
					}
											
				} else {					
				
					/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
					 * serão somados 
					 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
					 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
					 * */
					//if(itemVenda.getLucro().doubleValue() >= 0) {
						
						//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
					if(itemVenda.getProduto().isEstoque()) {	
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));	
					}
						//}
						
					//} else {
						
						//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
							
							/*BigDecimal subtotal = BigDecimal.valueOf( 
										itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().longValue());					
							BigDecimal total = new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * (itemVenda.getDesconto().doubleValue() / 100)));		
							*/	
							/*BigDecimal total = itemVenda.getTotal();
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - total.doubleValue()));					
							
						//}											
					}*/
				}
					
				if(itemVenda.getProduto().isEstoque()) {	
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - itemVenda.getQuantidade().doubleValue()));				
				}
				
				if(!produto.getUnidadeMedida().equals("Kg") && !produto.getUnidadeMedida().equals("Lt") && !produto.getUnidadeMedida().equals("Pt")) {
					totalDeItens += itemVenda.getQuantidade().doubleValue();				
				} else {
					totalDeItens += 1;
				}
				
				
				valorTotal += itemVenda.getTotal().doubleValue();
				valorCompra += itemVenda.getValorCompra().doubleValue();
	
				lucro += itemVenda.getLucro().doubleValue();
				//percentualLucro += itemVenda.getPercentualLucro().doubleValue();

				
			} else {
				
				if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt") && !itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {
					totalDeItens += itemVenda.getQuantidade().doubleValue();				
				} else {
					totalDeItens += 1;
				}
				
				valorTotal += itemVenda.getTotal().doubleValue();
				valorCompra += itemVenda.getValorCompra().doubleValue();
	
				lucro += itemVenda.getLucro().doubleValue();
			}
		}
			
		
		
		/*for (ItemVenda itemVenda : itensVenda) {
			
			Produto produto = itemVenda.getProduto();
			Object[] result = itensCompras.porQuantidadeDisponivel(produto);
			
			System.out.println(Arrays.asList(result));
			
			produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / ((Long) result[0]).longValue()));
			produto.setCustoTotal((BigDecimal) result[1]);

			produtos.save(produto);
			
		}*/
		
		
		if (tipoPagamento == TipoPagamento.AVISTA) {

			if(venda.isConta()) {
				
				Conta conta = new Conta();
				conta.setCodigoOperacao(venda.getNumeroVenda());
				conta.setOperacao("VENDA");
				conta.setParcela(tipoPagamento.name());
				conta.setTipo("CREDITO");
				conta.setStatus(vendaPaga != true ? false : true);
				
				
				
				BigDecimal taxaDeEntrega = BigDecimal.ZERO;
				if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
				} else {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
				}	
				
				venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));
				venda.setValorTotalComDesconto(venda.getValorTotal());
				
				if(venda.getDesconto() != null) {
					venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
				}
				
				conta.setValor(venda.getValorTotalComDesconto());
				
				conta.setSaldo(conta.getValor());
				
				conta.setSubTotal(venda.getValorTotalComDesconto());
				conta.setCustoMedio(new BigDecimal(valorCompra));
				
				
				if(venda.getDesconto() != null) {
					conta.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
				} else {
					conta.setLucro(new BigDecimal(lucro));
				}
				
				
				//conta.setLucro(new BigDecimal(lucro));
				conta.setTaxaEntrega(venda.getTaxaDeEntrega());
				
				Calendar calendario = Calendar.getInstance();
				Calendar vencimento = Calendar.getInstance();
				vencimento.setTime(pagamentoPara);
				vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
				vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
				vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
				
				conta.setVencimento(vencimento.getTime());

				conta.setPagamento(null);
				//conta.setPagamento(vendaPaga != true ? null : vencimento.getTime());
				
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(venda.getDataVenda());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				conta.setEmpresa(usuario.getEmpresa());
				contas.save(conta);
				
			} else {
				
				for (Pagamento pagamento : pagamentosAdicionados) {		
					pagamento.setVenda(venda);
					pagamentos.save(pagamento);
				}
			}

		} else {
			
			for (Conta conta : entradas) {

				conta.setCodigoOperacao(venda.getNumeroVenda());
				conta.setOperacao("VENDA");
				conta.setTipo("CREDITO");
				conta.setStatus(true);
				conta.setPagamento(new Date());
				
				conta.setSaldo(BigDecimal.ZERO);

				conta.setEmpresa(usuario.getEmpresa());
				contas.save(conta);
			}
			
			for (Conta conta : todasContas) {
				conta.setCodigoOperacao(venda.getNumeroVenda());
				conta.setOperacao("VENDA");
				conta.setTipo("CREDITO");
				conta.setPagamento(null);
				
				conta.setSaldo(conta.getValor());
				
				conta.setEmpresa(usuario.getEmpresa());
				conta = contas.save(conta);
			}
		}
		

			
		//venda.setStatus(true); 
		//venda.setStatus(entrega);
		
		String download = "", cupom = "";
			
		if (edit != true) {
			
			venda.setStatus(!entrega); 

			if (entrega) {
				entregaVenda.setStatus("PENDENTE");
				entregaVenda.setVenda(venda);
				entregaVenda = entregas.save(entregaVenda);
				if(!venda.isVendaPaga()) {
					venda.setDataPagamento(null);
					
					venda.setDiaPagamento(null);
					venda.setNomeDiaPagamento(null);
					venda.setSemanaPagamento(null);
					venda.setMesPagamento(null);
					venda.setAnoPagamento(null);
				}			
			}
			
			venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			
			//BigDecimal taxaDeEntrega = (venda.getBairro() != null && entrega) ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;		
			BigDecimal taxaDeEntrega = BigDecimal.ZERO;
			if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
			} else {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
			}
			
			
			
			venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));
			
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
			if(venda.getDesconto() != null) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
			}	

		
			venda.setQuantidadeItens(totalDeItens);
			venda.setLucro(BigDecimal.valueOf(lucro));
			if(venda.getDesconto() != null) {
				venda.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
			}
			
			if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
				venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
			}
			
			
			venda.setPercentualLucro(new BigDecimal(((venda.getValorTotalComDesconto().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotalComDesconto().doubleValue())*100));
			
			
			if(venda.getTipoPagamento() == TipoPagamento.AVISTA) {
				
				if(trocaPendente && vendaPaga) {
				
					// Comentado para futura correção
					/*
					List<ItemDevolucao> listaDevolucao = itensDevolucoes.listaDevolucao(usuario.getEmpresa());
					for (ItemDevolucao itemDevolucao : listaDevolucao) {				
						Devolucao devolucao = itemDevolucao.getDevolucao();
						devolucao.setStatus(true);
						devolucoes.save(devolucao);
					}
					*/
					
					venda.setSaldoParaTroca(new BigDecimal(saldoParaTroca));
					
					trocaPendente = false;
					saldoParaTroca = 0D;
					saldoParaTrocaEmString = "0,00";
				}
			}
			
			
			if(venda.isVendaPaga() != true) {
				
				venda.setValorRecebido(BigDecimal.ZERO);
				venda.setFaltando(BigDecimal.ZERO);
				venda.setTroco(BigDecimal.ZERO);		
				
			} else {
				venda.setValorRecebido(totalPagamentos);
				venda.setFaltando(new BigDecimal(faltando));
				venda.setTroco(new BigDecimal(troco));				
			}
	
			
			venda.setClientePagouTaxa(venda.getFormaPagamento().isClientePaga());
			
			if(venda.getDesconto() == null) {
				venda.setDesconto(BigDecimal.ZERO);
			}
			
			venda = vendas.save(venda);
			
			
			

			vendaTemp_ = new Venda();
			vendaTemp_.setNumeroVenda(null);
			vendaTemp_.setTipoVenda(venda.getTipoVenda());
			//vendaTemp_.setBairro(venda.getBairro());
			vendaTemp_.setBairro(bairros.porNome("Não Informado", usuario.getEmpresa()));
			vendaTemp_.setUsuario(venda.getUsuario());	
			
			valorRecebido = null;
			troco = 0D;
			totalTroco = BigDecimal.ZERO;
			pagamentosAdicionados = new ArrayList<Pagamento>();
			totalPagamentos = BigDecimal.ZERO;
				
			
			//Cliente cliente = clientes.porId(1L);
			//vendaTemp_.setCliente(cliente);
			
			if(imprimirCupom && !venda.isAjuste()) {
				cupom += "downloadCupom();";
				vendaTemp_.setId(venda.getId());
				if(pagarConta) {
					//Implementar depois essa parte
					//emitirCupom();
				} else {
					mesa = new Mesa();
					vendaTemp_.setId(null);
				}
				//imprimirCupom(itensVenda, venda);
			}
			
			//imprimirCupom = false;
			
			if(gerarPDF && !venda.isAjuste()) {
				if(pagarConta) {
					//Implementar depois essa parte
					//emitirPedido(venda);
					download += "downloadPDF();";
				}
			}
			
			gerarPDF = false;
			
			
			
			//PF('confirmDialog').hide();
			PrimeFaces.current().executeScript(download + "PF('confirmDialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
					+ venda.getNumeroVenda() + " registrada com sucesso!', timer: 5000 });");
			
			/*emitirCupom(venda);*/
			//imprimirCupom(itensVenda, venda);
			
			
			Log log = new Log();
			log.setDataLog(new Date());
			log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
			log.setOperacao(TipoAtividade.VENDA.name());
			
			NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
			
			log.setDescricao("Registrou venda, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
			log.setUsuario(usuario);		
			logs.save(log);
			

			
			venda = new Venda();
			itensVenda = new ArrayList<ItemVenda>();
			itemVenda = new ItemVenda();
			itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
			itemSelecionado = null;

			itensCompra = new ArrayList<>();
			itemCompra = new ItemCompra();

			entregaVenda = new Entrega();
			entrega = false;
			
			venda = vendaTemp_;
			FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro", usuario.getEmpresa());
			venda.setFormaPagamento(formaPagamento);
			venda.setId(null);
			
			filter = new ProdutoFilter();
			produto = new Produto();
			
			pagamentoPara = venda.getDataVenda();
			pagamentoParaEmString = sdf.format(pagamentoPara);
			
			totalDeAcrescimo = BigDecimal.ZERO;
			
			pagamento = new Pagamento();
			
			venda.setCliente(clientes.porNome("Nao Informado", usuario.getEmpresa()));

		} else {

			if (entrega) {
				if(entregaVenda.getId() == null) {
					venda.setStatus(!entrega);
					
					entregaVenda.setStatus("PENDENTE");
					entregaVenda.setVenda(venda);
					entregaVenda = entregas.save(entregaVenda);

				} else {
					venda.setStatus(!entrega); 
					entregaVenda.setStatus("PENDENTE");
					entregaVenda = entregas.save(entregaVenda);
				}
				
				if(!venda.isVendaPaga()) {
					venda.setDataPagamento(null);
					
					venda.setDiaPagamento(null);
					venda.setNomeDiaPagamento(null);
					venda.setSemanaPagamento(null);
					venda.setMesPagamento(null);
					venda.setAnoPagamento(null);
				}
				
				
			} else {
				if(entregaVenda.getId() != null) {
					entregas.remove(entregaVenda);
					entregaVenda = new Entrega();
					
					venda.setDataPagamento(new Date());
					setInformacoesDataPagamento();
					
					venda.setStatus(!entrega);
									
				}
			}
			

							
			/*
			venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			BigDecimal taxaDeEntrega = (venda.getBairro() != null && entrega) ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;
			venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));

			
			venda.setQuantidadeItens(totalDeItens);
			venda.setLucro(BigDecimal.valueOf(lucro));
			
			venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));
			*/
			

			
			
			
			venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			
			//BigDecimal taxaDeEntrega = (venda.getBairro() != null && entrega) ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;		
			BigDecimal taxaDeEntrega = BigDecimal.ZERO;
			if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
			} else {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
			}
			
			
			
			venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));
			
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
			if(venda.getDesconto() != null) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
			}	

		
			venda.setQuantidadeItens(totalDeItens);
			venda.setLucro(BigDecimal.valueOf(lucro));
			if(venda.getDesconto() != null) {
				venda.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
			}
			
			if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
				venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
			}
			
			
			venda.setPercentualLucro(new BigDecimal(((venda.getValorTotalComDesconto().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotalComDesconto().doubleValue())*100));
			
			
			
			/*
			BigDecimal taxaDeEntrega = BigDecimal.ZERO;
			if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
			} else {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
			}
			
			venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));
			
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
			if(venda.getDesconto() != null) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
			}
			
			
			if(venda.getDesconto() != null) {
				venda.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
			} else {
				venda.setLucro(new BigDecimal(lucro));
			}
			
			if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
				venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
			}*/
						
			
			venda.setClientePagouTaxa(venda.getFormaPagamento().isClientePaga());
			
			if(venda.getDesconto() == null) {
				venda.setDesconto(BigDecimal.ZERO);
			}
			
			venda = vendas.save(venda);
			
			
			
				
			if(imprimirCupom && !venda.isAjuste()) {
				cupom += "downloadCupom();";
				vendaTemp_.setId(venda.getId());					
				
				if(pagarConta) {
					//Implementar depois essa parte
					//emitirCupom();
					//imprimirCupom(itensVenda, venda);
				} else {
					mesa = new Mesa();
					vendaTemp_.setId(null);
				}
			}
			
			//imprimirCupom = false;
			
			if(gerarPDF && !venda.isAjuste()) {
				if(pagarConta) {
					//Implementar depois essa parte
					//emitirPedido(venda);
					download += "downloadPDF();";
				}
			}
			
			gerarPDF = false;
				
			
			buscar();

			//venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			//venda.setQuantidadeItens(totalDeItens);
			//venda.setLucro(BigDecimal.valueOf(lucro));
			//venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));
			//venda = vendas.save(venda);
			
			
			Log log = new Log();
			log.setDataLog(new Date());
			log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
			log.setOperacao(TipoAtividade.VENDA.name());
			
			NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
			
			log.setDescricao("Alterou venda, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
			log.setUsuario(usuario);		
			logs.save(log);
				
				
			//PF('confirmDialog').hide();
			PrimeFaces.current().executeScript(download + "PF('confirmDialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
					+ venda.getNumeroVenda() + " atualizada com sucesso!', timer: 5000 });");

		}
	}
	

	private void setInformacoesDataLancamento() {
		
		Calendar calendario = Calendar.getInstance();
		Calendar calendarioTemp = Calendar.getInstance();

		calendarioTemp.setTime(venda.getDataVenda());
		calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
		calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
		calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
		venda.setDataVenda(calendarioTemp.getTime());
		
		venda.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
		venda.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
		venda.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
		venda.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
		venda.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
	}
	
	
	private void setInformacoesDataPagamento() {
		
		Calendar calendario = Calendar.getInstance();

		calendario.setTime(venda.getDataPagamento());
		
		venda.setDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
		venda.setNomeDiaPagamento(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
		venda.setSemanaPagamento(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
		venda.setMesPagamento(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
		venda.setAnoPagamento(Long.valueOf((calendario.get(Calendar.YEAR))));
	}
	
	
	public void changeBairro() {		
		
		venda.setTaxaDeEntrega(BigDecimal.ZERO);
		if(entrega) {
			venda.setTaxaDeEntrega(venda.getBairro().getTaxaDeEntrega());
		}	
	}
	
	public void changeTaxaEntrega() {		
		
		venda.setTaxaDeEntrega(BigDecimal.ZERO);
		venda.setBairro(venda.getCliente().getBairro());
		if(entrega) {
			venda.setTaxaDeEntrega(venda.getBairro().getTaxaDeEntrega());
		}	
	}

	
	public void exibirTaxaEntrega() {	
		venda.setTaxaDeEntrega(BigDecimal.ZERO);
		
		if(!entrega) {
			PrimeFaces.current().executeScript("ocultarTaxaEntrega();");
		} else {
			PrimeFaces.current().executeScript("mostrarTaxaEntrega();");
			venda.setTaxaDeEntrega(venda.getBairro().getTaxaDeEntrega());
		}		
	}
	
	
	
	public void ativarCupom() {
		configuracao.setCupomAtivado(imprimirCupom);
		configuracao = configuracoes.save(configuracao);
	}
	
	public void confirmarParcelas() {
		
		parcelasConfirmadas = true;
	}

	public void confirmarPrimeiraParcela() {
		
		primeiraParcelaEmString = sdf.format(primeiraParcela);
		zerarParcelas();
	}

	public void preparaPrimeiraParcela() throws ParseException {
		
		primeiraParcela = sdf.parse(primeiraParcelaEmString);
	}
	
	public void preparaPagamentoPara() throws ParseException {
		
		pagamentoPara = sdf.parse(pagamentoParaEmString);
	}
	
	public void confirmarPagamentoPara() {
		
		pagamentoParaEmString = sdf.format(pagamentoPara);
	}
	

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public List<Bairro> getTodosBairros() {
		return todosBairros;
	}

	public List<ItemVenda> getItensVenda() {
		return itensVenda;
	}

	public List<ItemCompra> getItensCompra() {
		return itensCompra;
	}

	public Venda getVenda() {
		return venda;
	}

	public ItemVenda getItemVenda() {
		return itemVenda;
	}

	public void setItemVenda(ItemVenda itemVenda) {
		this.itemVenda = itemVenda;
	}

	public ItemCompra getItemCompra() {
		return itemCompra;
	}

	public void setItemCompra(ItemCompra itemCompra) {
		this.itemCompra = itemCompra;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public ProdutoFilter getFilter() {
		return filter;
	}

	public void setFilter(ProdutoFilter filter) {
		this.filter = filter;
	}

	public List<Produto> getProdutosFiltrados() {
		return produtosFiltrados;
	}

	public ItemVenda getItemSelecionado() {
		return itemSelecionado;
	}

	public void setItemSelecionado(ItemVenda itemSelecionado) {
		this.itemSelecionado = itemSelecionado;
	}

	public int getItensVendaSize() {
		return itensVenda.size();
	}

	public int getItensCompraSize() {
		return itensCompra.size();
	}

	public List<TipoVenda> getTodosTiposVendas() {
		return todosTiposVendas;
	}

	public boolean isEntrega() {
		return entrega;
	}

	public void setEntrega(boolean entrega) {
		this.entrega = entrega;
	}

	public Entrega getEntregaVenda() {
		return entregaVenda;
	}

	public void setEntregaVenda(Entrega entregaVenda) {
		this.entregaVenda = entregaVenda;
	}

	public boolean isDisableAjuste() {
		return disableAjuste;
	}
	
	public List<Cliente> getTodosClientes() {
		return todosClientes;
	}
	
	public TipoPagamento[] getTiposPagamentos() {
		return TipoPagamento.values();
	}
	
	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(TipoPagamento tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}
	
	public List<FormaPagamento> getTodasFormasPagamentos() {
		return todasFormasPagamentos;
	}
	
	public void zerarParcelas() {
		todasContas = new ArrayList<>();
		parcelasConfirmadas = false;
	}
	
	public boolean isParcelasConfirmadas() {
		return parcelasConfirmadas;
	}
	
	public Double getValorEntrada() {
		return valorEntrada;
	}

	public void setValorEntrada(Double valorEntrada) {
		this.valorEntrada = valorEntrada;
	}
	
	public boolean isVendaPaga() {
		return vendaPaga;
	}

	public void setVendaPaga(boolean vendaPaga) {
		this.vendaPaga = vendaPaga;
	}
	
	public String getSaldoParaTrocaEmString() {
		return saldoParaTrocaEmString;
	}
	
	public boolean isContaAPagar() {
		return contaAPagar;
	}

	public void setContaAPagar(boolean contaAPagar) {
		this.contaAPagar = contaAPagar;
	}
	
	public Pagamento getPagamento() {
		return pagamento;
	}

	public void setPagamento(Pagamento pagamento) {
		this.pagamento = pagamento;
	}
	
	public Double getValorRecebido() {
		return valorRecebido;
	}

	public void setValorRecebido(Double valorRecebido) {
		this.valorRecebido = valorRecebido;
	}
	
	public Double getTroco() {
		return troco;
	}

	public void setTroco(Double troco) {
		this.troco = troco;
	}
	
	public Double getFaltando() {
		return faltando;
	}

	public void setFaltando(Double faltando) {
		this.faltando = faltando;
	}
	
	public String getPrimeiraParcelaEmString() {
		return primeiraParcelaEmString;
	}
	
	public String getPagamentoParaEmString() {
		return pagamentoParaEmString;
	}
	
	public Date getPrimeiraParcela() {
		return primeiraParcela;
	}

	public void setPrimeiraParcela(Date primeiraParcela) {
		this.primeiraParcela = primeiraParcela;
	}
	
	public Date getPagamentoPara() {
		return pagamentoPara;
	}

	public void setPagamentoPara(Date pagamentoPara) {
		this.pagamentoPara = pagamentoPara;
	}
	
	public Long getParcelas() {
		return parcelas;
	}

	public void setParcelas(Long parcelas) {
		this.parcelas = parcelas;
	}
	
	public PeriodoPagamento getPeriodoPagamento() {
		return periodoPagamento;
	}

	public void setPeriodoPagamento(PeriodoPagamento periodoPagamento) {
		this.periodoPagamento = periodoPagamento;
	}

	public PeriodoPagamento[] getPeriodosPagamentos() {
		return PeriodoPagamento.values();
	}
	
	public List<Conta> getTodasContas() {
		return todasContas;
	}
	
	public Integer getTodasContasSize() {
		return todasContas.size();
	}
	
	public boolean isImprimirCupom() {
		return imprimirCupom;
	}

	public void setImprimirCupom(boolean imprimirCupom) {
		this.imprimirCupom = imprimirCupom;
	}

	public boolean isGerarPDF() {
		return gerarPDF;
	}

	public void setGerarPDF(boolean gerarPDF) {
		this.gerarPDF = gerarPDF;
	}

	public List<Pagamento> getPagamentosAdicionados() {
		return pagamentosAdicionados;
	}

	public BigDecimal getTotalPagamentos() {
		return totalPagamentos;
	}
	
	public void entregarVenda() {
		
		entregaVenda = entregas.porVenda(venda);
		entregaVenda.setStatus(StatusPedido.ENTREGUE.name());
		entregaVenda = entregas.save(entregaVenda);

		Venda venda = entregaVenda.getVenda();
		venda.setStatus(true);
		
		if(venda.getTipoPagamento() == TipoPagamento.AVISTA && !venda.isConta()) {
			venda.setVendaPaga(true);
		}
		
		venda = vendas.save(venda);
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
		log.setOperacao(TipoAtividade.VENDA.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		log.setDescricao("Entregou venda, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
		log.setUsuario(usuario);		
		logs.save(log);

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
				+ venda.getNumeroVenda() + " entregue com sucesso!' });");
	}

	public void desfazerEntrega() {
		entregaVenda = entregas.porVenda(venda);
		entregaVenda.setStatus(StatusPedido.PENDENTE.name());
		entregaVenda = entregas.save(entregaVenda);

		Venda venda = entregaVenda.getVenda();
		venda.setStatus(false);
		venda = vendas.save(venda);
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
		log.setOperacao(TipoAtividade.VENDA.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		log.setDescricao("Desfez entrega, venda Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
		log.setUsuario(usuario);		
		logs.save(log);

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Entrega da venda N."
				+ venda.getNumeroVenda() + " desfeita com sucesso!' });");
	}
	
	public void desfazerEntregaPagamento() {
		entregaVenda.setStatus(StatusPedido.PENDENTE.name());
		entregaVenda = entregas.save(entregaVenda);

		Venda venda = entregaVenda.getVenda();
		venda.setStatus(false);
		venda.setVendaPaga(false);
		venda = vendas.save(venda);
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
		log.setOperacao(TipoAtividade.VENDA.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		log.setDescricao("Desfez entrega e pagamento, venda Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
		log.setUsuario(usuario);		
		logs.save(log);
		

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Entrega da venda N."
				+ venda.getNumeroVenda() + " desfeita com sucesso!' });");
	}
	
	public void emitirPedido() {

		Venda vendaSelecionada = venda;

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
				
				pedido.getItensPagamento().add(pagamentosPedido);
				
			} else if(pedido.getConta() && vendaSelecionada.getTipoPagamento() == TipoPagamento.PARCELADO) {
				List<Conta> listaContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario.getEmpresa());
				
				listaContas.forEach(f -> {
					ItemEspelhoVendaParcelamentos parcelamentosPedido = new ItemEspelhoVendaParcelamentos();
					parcelamentosPedido.setParcela(f.getParcela());
					parcelamentosPedido.setValor(nf.format(f.getValor().doubleValue()));
					parcelamentosPedido.setVencimento(sdf.format(f.getVencimento()));
					
					if(f.getParcela().equals("Entrada")) { parcelamentosPedido.setStatus("✓"); }
		            
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

	public BigDecimal getMargemDeLucro() {
		return margemDeLucro;
	}

	public void setMargemDeLucro(BigDecimal margemDeLucro) {
		this.margemDeLucro = margemDeLucro.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}
}
