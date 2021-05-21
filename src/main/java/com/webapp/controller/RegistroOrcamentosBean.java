package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Bairro;
import com.webapp.model.Cliente;
import com.webapp.model.Entrega;
import com.webapp.model.FormaPagamento;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Log;
import com.webapp.model.Produto;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoPagamento;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Bairros;
import com.webapp.repository.Clientes;
import com.webapp.repository.Entregas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Logs;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.repository.filter.BairroFilter;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RegistroOrcamentosBean implements Serializable {

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

	private List<Usuario> todosUsuarios;

	private List<Bairro> todosBairros;

	private List<TipoVenda> todosTiposVendas;

	private List<Produto> produtosFiltrados;

	@Inject
	private ItemVenda itemVenda;

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
	
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario.getEmpresa());
			todosTiposVendas = tiposVendas.todos();
			todosBairros = bairros.todos();
			
			venda.setUsuario(usuario);
			venda.setStatusMesa("PAGO");
			
			Cliente cliente = clientes.porId(1L);
			venda.setCliente(cliente);
			
			if(usuario.getEmpresa().getId() == 7111 || usuario.getEmpresa().getId() == 7112) {
				venda.setTipoVenda(tiposVendas.porId(34L));
				FormaPagamento formaPagamento = formasPagamentos.porId(13987L);
				venda.setFormaPagamento(formaPagamento);
				venda.setBairro(bairros.porId(3008L));
			} else {
				venda.setTipoVenda(tiposVendas.porId(3L));
				FormaPagamento formaPagamento = formasPagamentos.porId(1L);
				venda.setFormaPagamento(formaPagamento);
				venda.setBairro(bairros.porId(1L));
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
        
        List<Bairro> listaDeBairros = bairros.filtrados(filtro);       
        
        return listaDeBairros;
    }

	public void buscar() {
		venda = vendas.porId(venda.getId());
		itensVenda = itensVendas.porVenda(venda);

		for (ItemVenda itemVenda : itensVenda) {
			
			/*if(itemVenda.getLucro().doubleValue() < 0) {
				disableAjuste = true;
			}*/
			
			try {
				Thread.sleep(100);
				itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
			} catch (InterruptedException e) {
			}
		}
		
		entregaVenda = entregas.porVenda(venda);
		entrega = entregaVenda.getId() != null;
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
			
			
			
			venda.setTipoPagamento(TipoPagamento.AVISTA);

			FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro");
			venda.setFormaPagamento(formaPagamento);
			
			
			
			venda.setValorRecebido(venda.getValorTotal());
			venda.setFaltando(BigDecimal.ZERO);
			venda.setTroco(BigDecimal.ZERO);
			
			
			
			venda.setAjuste(true);
			
			
			if(!venda.isAjuste()) {
				venda.setRecuperarValores(false);
			}

			boolean edit = false;
			if (venda.getId() != null) {
				edit = true;

/*
				List<ItemVenda> itemVendaTemp = itensVendas.porVenda(venda);

				for (ItemVenda itemVenda : itemVendaTemp) {
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemVenda.getQuantidade());
					produtos.save(produto);

					itensVendas.remove(itemVenda);

				}

				for (ItemVenda itemVenda : itensVenda) {

					List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompraTemp : itensCompraTemp) {

						if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								System.out.println(itemCompraTemp.getQuantidadeDisponivel());
								System.out.println(itemVenda.getQuantidade());
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel() + itemVenda.getQuantidade());
								itensCompras.save(itemCompraTemp);
							}
						}
					}

				}
*/
			
				/*
				for (ItemVenda itemVenda : itensVenda) {
					
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					
					Venda vendaTemp = vendas.porId(venda.getId());
					System.out.println("Ajuste: " + (venda.isAjuste() != vendaTemp.isAjuste()));	
					
					
	
					if(vendaTemp.isAjuste()) {
						
						if(!vendaTemp.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
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
				*/
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
	
				venda.setConta(true);
				venda.setEmpresa(usuario.getEmpresa());
				venda = vendas.save(venda);
	
				for (ItemVenda itemVenda : itensVenda) {
	
					itemVenda.setVenda(venda);
					itensVendas.save(itemVenda);
	
					
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					
					/*
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - itemVenda.getQuantidade().doubleValue()));
					
					if(venda.isAjuste()) {
								
						if(!venda.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
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
					*/
	
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
			}


			if (!edit) {
				
				venda.setStatus(!entrega);
				
				venda.setVendaPaga(!entrega);

				if (entrega) {
					entregaVenda.setStatus("PENDENTE");
					entregaVenda.setVenda(venda);
					entregaVenda = entregas.save(entregaVenda);
				}
				
				venda.setValorCompra(BigDecimal.valueOf(valorCompra));
				venda.setValorTotal(BigDecimal.valueOf(valorTotal));
				venda.setQuantidadeItens(totalDeItens);
				venda.setLucro(BigDecimal.valueOf(lucro));
				
				//venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));				
				venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));
				venda = vendas.save(venda);

				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Orçamento N."
						+ venda.getNumeroVenda() + " registrado com sucesso!' });");
				
				
				
				
				
				
				Log log = new Log();
				log.setDataLog(new Date());
				log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
				log.setOperacao("ORÇAMENTO");
				
				NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
				
				log.setDescricao("Registrou orçamento, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
				log.setUsuario(usuario);		
				logs.save(log);
				
				

				Venda vendaTemp_ = new Venda();
				vendaTemp_.setNumeroVenda(null);
				vendaTemp_.setTipoVenda(venda.getTipoVenda());
				vendaTemp_.setBairro(venda.getBairro());
				vendaTemp_.setUsuario(venda.getUsuario());	
				vendaTemp_.setStatusMesa("PAGO");
				Cliente cliente = clientes.porId(1L);
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
						venda.setVendaPaga(!entrega);
						
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
						venda.setVendaPaga(!entrega);
					}
				}

				//venda.setValorCompra(BigDecimal.valueOf(valorCompra));
				//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
				//venda.setQuantidadeItens(totalDeItens);
				//venda.setLucro(BigDecimal.valueOf(lucro));
				//venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));
				venda = vendas.save(venda);
				
				Log log = new Log();
				log.setDataLog(new Date());
				log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
				log.setOperacao("ORÇAMENTO");
				
				NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
				
				log.setDescricao("Alterou orçamento, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
				log.setUsuario(usuario);		
				logs.save(log);
				
				
				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Orçamento N."
						+ venda.getNumeroVenda() + " atualizado com sucesso!' });");
			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item ao Orçamento!' });");
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

			boolean produtoNaLista = false;
			for (ItemVenda itemVenda : itensVenda) {
				
				if(itemVenda.getCompra() != null) {
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
			
			} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx")) {
				nf = new DecimalFormat("###,##0", REAL);
				itemCompraTemp.setQuantidadeDisponivel_(nf.format(new BigDecimal(
						itemCompraTemp.getQuantidadeDisponivel().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
			}
		}

		if (itensCompra.size() == 0) {
			//PrimeFaces.current().executeScript(
					//"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!' });");
		} else {

			nf = new DecimalFormat("###,##0.00", REAL);
			
			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);
		}
	}

	public void adicionarItem() {

		//if(itemVenda.getQuantidade().doubleValue() > 0) {
			
			if(itemVenda.getValorUnitario().doubleValue() >= 0) {
		
				/*
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
					
					*/
		
					//if (itemVenda.getQuantidade().doubleValue() <= quantidadeDisponivel.doubleValue()) {
						
							/*if (itemVenda.getValorUnitario().doubleValue() <= itemCompra.getValorUnitario().doubleValue()) {					
								PrimeFaces.current().executeScript(
										"swal({ type: 'warning', title: 'Atenção!', text: 'Produto adicionado com valor unitário menor ou igual ao valor de compra.' });");
							}*/
						
							itemVenda.setTotal(BigDecimal.valueOf(
									itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue()));
							itemVenda.setVenda(venda);
							//itemVenda.setCompra(itemCompra.getCompra());
		
							/* Calculo do Lucro em valor e percentual */
							Double valorDeCustoUnitario = itemVenda.getProduto().getCustoMedioUnitario().doubleValue();	
							
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
		
							/*
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
								
								} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx")) {
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
											
							
					} else {
						PrimeFaces.current().executeScript(
								"swal({ type: 'error', title: 'Erro!', text: 'Quantidade maior que a disponível!' });");
					}
		
				} else {
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Não é possível adicionar itens à esta venda!' });");
				}
			*/
				
			} else {
				
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Valor unitário não pode ser menor que zero!' });");
			}
			
		/*} else {
			PrimeFaces.current()
				.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor ou igual a zero!' });");
		}*/
	}

	public void removeItem() {

		//if (venda.getId() == null) {

			// itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);

			//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());

			//itensCompra = new ArrayList<ItemCompra>();

			/*
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
				
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx")) {
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
			*/

			itemSelecionado = null;

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();

			/*
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível remover os itens desta venda!' });");
		}*/
	}

	public void editarItem() {

		//if (venda.getId() == null) {

			itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);

			//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());

			//itensCompra = new ArrayList<ItemCompra>();

			/*
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
				
				} else if(itemCompraTemp.getProduto().getUnidadeMedida().equals("Un") || itemCompraTemp.getProduto().getUnidadeMedida().equals("Cx")) {
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
			*/

			//itensCompra = new ArrayList<>();
			//itensCompra.addAll(itensCompraTemp);

			itemSelecionado = null;

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();

			/*
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível editar os itens desta venda!' });");
		}
		*/

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

}
