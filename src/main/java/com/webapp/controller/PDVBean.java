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

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Bairro;
import com.webapp.model.Entrega;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Produto;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Bairros;
import com.webapp.repository.Entregas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.repository.filter.BairroFilter;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PDVBean implements Serializable {

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

	private List<Usuario> todosUsuarios;

	private List<Bairro> todosBairros;

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
	private Produto produto;
	
	private boolean leitor;
	
	private Integer activeIndex = 0;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario = usuarios.porNome(user.getUsername());
			
			List<Grupo> grupos = usuario.getGrupos();
			
			if(grupos.size() > 0) {
				for (Grupo grupo : grupos) {
					if(grupo.getNome().equals("ADMINISTRADOR")) {
						EmpresaBean empresaBean = (EmpresaBean) FacesUtil.getObjectSession("empresaBean");
						if(empresaBean != null && empresaBean.getEmpresa() != null) {
							usuario.setEmpresa(empresaBean.getEmpresa());
						}
					}
				}
			}
			
			todosUsuarios = usuarios.todos(usuario.getEmpresa());
			todosTiposVendas = tiposVendas.todos();
			todosBairros = bairros.todos();
			
			venda.setTipoVenda(tiposVendas.porId(33L));
			venda.setBairro(bairros.porId(3008L));
			venda.setUsuario(usuario);
			
			itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
		}
	}

	public void pesquisar() {
		activeIndex = 0;
		System.out.println("Código escaneado: " + filter.getCodigo());
		
		Produto produto = produtos.porCodigoDeBarras(filter.getCodigo(), usuario.getEmpresa());	
		
		if(produto != null) {
			filter = new ProdutoFilter();
			selecionarProduto(produto);
			
		} else {
			filter = new ProdutoFilter();
			itemVenda = new ItemVenda();
			itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto não encontrado!', timer: 3000 });");
		}
		
	}
	
	public void atualizaValores() {
		System.out.println(itemVenda.getProduto().getMargemLucro());
		
	}
	
	public List<Bairro> completeText(String query) {
		
        BairroFilter filtro = new BairroFilter();
        filtro.setNome(query);
        
        List<Bairro> listaDeBairros = bairros.filtrados(filtro);       
        
        return listaDeBairros;
    }
	
	public List<Produto> completeText_Produto(String query) {
		
		filter.setEmpresa(usuario.getEmpresa());
		filter.setDescricao(query);
		
		List<Produto> listaProdutos = produtos.filtrados(filter);
         
        return listaProdutos;
    }
	
	public void onItemSelect(SelectEvent event) {		
        System.out.println(event.getObject().toString());
        selecionarProduto((Produto) event.getObject());
        produto = new Produto();
    }

	public void buscar() {
		venda = vendas.porId(venda.getId());
		itensVenda = itensVendas.porVenda(venda);

		for (ItemVenda itemVenda : itensVenda) {
			
			List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
			itemVenda.setItensVendaCompra(itensVendaCompra);
			
			try {
				Thread.sleep(100);
				itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
			} catch (InterruptedException e) {
			}
		}
		
		entregaVenda = entregas.porVenda(venda);
		entrega = entregaVenda.getId() != null;
		
		activeIndex = 1;
	}
	
	public void ativarLeitor() {
		activeIndex = 0;
	}

	public void salvar() {

		if (itensVenda.size() > 0) {
			
			Long totalDeItens = 0L;
			double valorTotal = 0;
			double lucro = 0;
			//double percentualLucro = 0;
			double valorCompra = 0;
			
			Calendar calendario = Calendar.getInstance();
			Calendar calendarioTemp = Calendar.getInstance();

			//if (venda.getId() == null) {			
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
			//}
			
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
				
				for (ItemVenda itemVenda : itensVenda) {
	
					//List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
	
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					
					Venda vendaTemp = vendas.porId(venda.getId());
					System.out.println("Ajuste: " + (venda.isAjuste() != vendaTemp.isAjuste()));	
					
					
	
					if(vendaTemp.isAjuste()) {
						
						if(!vendaTemp.isRecuperarValores()) {
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {								
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
							//}
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
								
								//BigDecimal total = BigDecimal.valueOf( 
											//itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().longValue());
								BigDecimal total = itemVenda.getTotal();
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + total.doubleValue()));					
	
							//}											
						}
					}

					
					
					if(venda.isAjuste()) {
						
						if(!vendaTemp.isRecuperarValores()) {
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
							//}
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
								
								//BigDecimal total = BigDecimal.valueOf( 
											//itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().longValue());
							BigDecimal total = itemVenda.getTotal();
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - total.doubleValue()));					
							
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
	
				venda.setPdv(true);
				venda.setEmpresa(usuario.getEmpresa());
				venda = vendas.save(venda);
				
				List<ItemVenda> itensVendaTemp = new ArrayList<ItemVenda>();
				itensVendaTemp.addAll(itensVenda);
	
				for (ItemVenda itemVenda : itensVenda) {
	
					List<ItemVendaCompra> itensVendaCompra = itemVenda.getItensVendaCompra();
					itemVenda.setVenda(venda);
					itemVenda = itensVendas.save(itemVenda);
					itemVenda.setItensVendaCompra(itensVendaCompra);
	
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
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
							//}
							
						} else {
							
							//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								/*BigDecimal subtotal = BigDecimal.valueOf( 
											itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().longValue());					
								BigDecimal total = new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * (itemVenda.getDesconto().doubleValue() / 100)));		
								*/	
								BigDecimal total = itemVenda.getTotal();
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - total.doubleValue()));					
								
							//}											
						}
					}
					
					
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemVenda.getQuantidade());
					produtos.save(produto);
	
					totalDeItens += itemVenda.getQuantidade();
					valorTotal += itemVenda.getTotal().doubleValue();
					valorCompra += itemVenda.getValorCompra().doubleValue();
	
					lucro += itemVenda.getLucro().doubleValue();
					//percentualLucro += itemVenda.getPercentualLucro().doubleValue();
					
					
					//List<String> itens = new ArrayList<String>();
	
					List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompraTemp : itensCompraTemp) {
	
						//System.out.println(itemCompraTemp.getCompra().getId() + " == " + itemVenda.getCompra().getId());
						//System.out.println(itemCompraTemp.getProduto().getId() + " == " + itemVenda.getProduto().getId());
	
						/*if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								// if(itemVenda.getId() == null) {
								System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
								System.out.println("itemCompraTemp.getQuantidadeDisponivel(): "
										+ itemCompraTemp.getQuantidadeDisponivel());
								
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
								
								System.out
										.println("Nova QuantidadeDisponivel: " + itemCompraTemp.getQuantidadeDisponivel());
								itensCompras.save(itemCompraTemp);
								// }
							}
						}*/
						
						
						//for (ItemVenda itemVendaTemp : itensVendaTemp) {
							
							/*if(!itens.contains(itemVendaTemp.getCode())) {
								itemVendaTemp.setSaldo(itemVendaTemp.getQuantidade());
								itens.add(itemVendaTemp.getCode());
							}*/
							
							for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								itemVendaCompra.setItemVenda(itemVenda);
								
								if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
										.longValue()) {
									if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
											.longValue()) {
										
										//if (itemVendaTemp.getId() == null && venda.getId() == null) {
											
											System.out.println("Saldo: " + itemVendaCompra.getQuantidade() + " - " + itemCompraTemp.getQuantidadeDisponivel().longValue());
											
											System.out.println("itemVendaTemp.getSaldo() > 0L : " + (itemVendaCompra.getQuantidade() > 0L));
											//if(itemVenda.getSaldo() > 0L) {
												
												//if(itemVenda.getSaldo().longValue() <= itemCompraTemp.getQuantidadeDisponivel().longValue()) {
													itemCompraTemp.setQuantidadeDisponivel(
															itemCompraTemp.getQuantidadeDisponivel().longValue() - itemVendaCompra.getQuantidade().longValue());
													//itemVenda.setSaldo(0L);
													
												//} else {			
													/*itemVenda.setSaldo(itemVenda.getSaldo().longValue() - itemCompraTemp.getQuantidadeDisponivel().longValue());
													itemCompraTemp.setQuantidadeDisponivel(0L);
													System.out.println("Saldo: " + itemVenda.getSaldo());*/
												//}
											//}
											
											//System.out.println("Saldo Atual: " + itemVenda.getSaldo());
											
										//}
	
									}
								}
							}		
						
						//}
						
						System.out.println(itemCompraTemp.getQuantidadeDisponivel());
						itensCompras.save(itemCompraTemp);
						
					}
				}
				
				
				for (ItemVenda itemVenda : itensVenda) {
					for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
						itensVendasCompras.save(itemVendaCompra);
					}
				}
			}


			if (!edit) {
				
				venda.setStatus(!entrega);

				if (entrega) {
					entregaVenda.setStatus("PENDENTE");
					entregaVenda.setVenda(venda);
					entregaVenda = entregas.save(entregaVenda);
				}
				
				venda.setValorCompra(BigDecimal.valueOf(valorCompra));
				venda.setValorTotal(BigDecimal.valueOf(valorTotal));
				venda.setQuantidadeItens(totalDeItens);
				venda.setLucro(BigDecimal.valueOf(lucro));
				
				venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));
				venda = vendas.save(venda);

				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
						+ venda.getNumeroVenda() + " registrada com sucesso!', timer: 10000 });");

				Venda vendaTemp_ = new Venda();
				vendaTemp_.setNumeroVenda(null);
				vendaTemp_.setTipoVenda(venda.getTipoVenda());
				vendaTemp_.setBairro(venda.getBairro());
				vendaTemp_.setUsuario(venda.getUsuario());			
				
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
				
				filter = new ProdutoFilter();
				produto = new Produto();
				
				activeIndex = 0;

			} else {

				if (entrega) {
					if(entregaVenda.getId() == null) {
						venda.setStatus(!entrega);
						
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
					}
				}

				//venda.setValorCompra(BigDecimal.valueOf(valorCompra));
				//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
				//venda.setQuantidadeItens(totalDeItens);
				//venda.setLucro(BigDecimal.valueOf(lucro));
				//venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));
				venda = vendas.save(venda);
				
				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
						+ venda.getNumeroVenda() + " atualizada com sucesso!', timer: 10000 });");
				
				activeIndex = 0;
			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!', timer: 3000 });");
		}

	}

	public void selecionarProduto(Produto produto) {
		
		itemVenda = new ItemVenda();
		produto.setDescontoMaximo(produto.getDesconto().longValue());
		produto.setDesconto(new BigDecimal(0));
		itemVenda.setProduto(produto);
		itemVenda.setCode(produto.getCodigo().concat("_" + new Date().getTime()));
		System.out.println(itemVenda.getCode());

		itensCompra = new ArrayList<ItemCompra>();
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
		
		List<String> itens = new ArrayList<>();
		
		Long quantidadeDisponivel = itensCompras.saldoPorProduto(produto).longValue();//produto.getQuantidadeAtual();

		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			itemCompraTemp.getCompra().setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

			boolean produtoNaLista = false;
			for (ItemVenda itemVenda : itensVenda) {
				
				
				/*if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
					if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {

						produtoNaLista = true;
						if (itemVenda.getId() == null && venda.getId() == null) {
							itemCompraTemp.setQuantidadeDisponivel(
									itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
						}

					}
				}*/
				
				if(!itens.contains(itemVenda.getCode())) {
					itemVenda.setSaldo(itemVenda.getQuantidade());
					itens.add(itemVenda.getCode());
				}
				
				for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
							.longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
							
							produtoNaLista = true;
							
							/*if(!saldo) {
								itemVenda.setSaldo(itemVenda.getQuantidade());
								saldo = true;
							}	*/						
							
							if (itemVenda.getId() == null && venda.getId() == null) {
								
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().longValue());

								if(itemVenda.getSaldo() > 0L) {
									
									if(itemVenda.getSaldo().longValue() <= itemCompraTemp.getQuantidadeDisponivel().longValue()) {
										quantidadeDisponivel -= itemVenda.getSaldo();
										itemCompraTemp.setQuantidadeDisponivel(
												itemCompraTemp.getQuantidadeDisponivel().longValue() - itemVenda.getSaldo().longValue());
										itemVenda.setSaldo(0L);										
										
									} else {	
										quantidadeDisponivel -= itemCompraTemp.getQuantidadeDisponivel().longValue();
										itemVenda.setSaldo(itemVenda.getSaldo().longValue() - itemCompraTemp.getQuantidadeDisponivel().longValue());
										itemCompraTemp.setQuantidadeDisponivel(0L);				
										System.out.println("Saldo: "+itemVenda.getSaldo());
									}
								}
								
								System.out.println("Saldo Atual: "+itemVenda.getSaldo());
							}

						}
					}
				}
				
			}

			if (produtoNaLista != false) {
				if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}

			if (produtoNaLista != true) {
				if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}
		}
		
		produto.setQuantidadeAtual(quantidadeDisponivel);
		System.out.println("QuantidadeAtual: "+quantidadeDisponivel);

		if (itensCompra.size() == 0) {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!', timer: 3000 });");
		} else {

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
		
		activeIndex = 0;

		if (venda.getId() == null) {
			
			/* Método custo médio */
			itemVenda.setValorUnitario(itemVenda.getProduto().getPrecoDeVenda());

			Long quantidadeDisponivel = itemVenda.getProduto().getQuantidadeAtual();//itemCompra.getQuantidadeDisponivel();

			//for (ItemVenda itemVenda : itensVenda) {
				
				/*if (itemCompra.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
					if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
						if (itemVenda.getId() == null && venda.getId() == null) {
							quantidadeDisponivel -= itemVenda.getQuantidade();
						}
					}
				}*/
								
				/*for (Compra compra : itemVenda.getCompras()) {
					if (itemCompra.getCompra().getId().longValue() == compra.getId()
							.longValue()) {
						if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
							
							quantidadeDisponivel -= itemVenda.getQuantidade();
						}
					}
				}			
			}*/

			System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
			System.out.println("quantidadeDisponivel: " + quantidadeDisponivel);

			if(itemVenda.getQuantidade() > 0) {

				if (itemVenda.getQuantidade() <= quantidadeDisponivel) {
					
						/*if (itemVenda.getValorUnitario().doubleValue() <= itemCompra.getValorUnitario().doubleValue()) {					
							PrimeFaces.current().executeScript(
									"swal({ type: 'warning', title: 'Atenção!', text: 'Produto adicionado com valor unitário menor ou igual ao valor de compra.' });");
						}*/
					
						BigDecimal subtotal = BigDecimal.valueOf(
							itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue());					
						itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * (itemVenda.getDesconto().doubleValue() / 100))));
						
						itemVenda.setVenda(venda);
						
						itemVenda.setSaldo(itemVenda.getQuantidade());
						
						Double valorDeCustoUnitario = itemVenda.getProduto().getCustoMedioUnitario().doubleValue();
						
						List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();				
						Long saldo = itemVenda.getQuantidade();
						for (int i = itensCompra.size() - 1; i >= 0; i--) {
							System.out.println("Saldo: "+saldo);
							if(saldo > 0L) {
								if(saldo.longValue() <= itensCompra.get(i).getQuantidadeDisponivel().longValue()) {
									ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
									itemVendaCompra.setItemVenda(itemVenda);
									itemVendaCompra.setCompra(itensCompra.get(i).getCompra());
									itemVendaCompra.setQuantidade(saldo);
									//itemVendaCompra.setTotal(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().intValue()));
									
									itensVendaCompra.add(itemVendaCompra);
									
									saldo = 0L; 
								} else {
									
									ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
									itemVendaCompra.setItemVenda(itemVenda);
									itemVendaCompra.setCompra(itensCompra.get(i).getCompra());
									itemVendaCompra.setQuantidade(itensCompra.get(i).getQuantidadeDisponivel());
									//itemVendaCompra.setTotal(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().intValue()));
									
									itensVendaCompra.add(itemVendaCompra);
									
									saldo -= itensCompra.get(i).getQuantidadeDisponivel().longValue();
								}
							}
						}
						
						//itemVenda.setPercentualLucro(new BigDecimal(itemVenda.getTotal().doubleValue() - (itemVenda.getProduto().getCustoMedioUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue())/ itemVenda.getTotal().doubleValue() * 100));
						
						itemVenda.setItensVendaCompra(itensVendaCompra);				
						//itemVenda.setCompra(itemCompra.getCompra());
	
						/* Calculo do Lucro em valor e percentual */						
						
						Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
								* itemVenda.getQuantidade().intValue())
						.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
						
					
						itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
								* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().intValue())
								- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * itemVenda.getDesconto().doubleValue() / 100));
	
						itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade())) / itemVenda.getTotal().doubleValue() * 100)));
						itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));
	
						System.out.println("ValorDeCustoTotal: " + valorDeCustoTotal);
						System.out.println("ValorDeCustoUnitario: " + valorDeCustoUnitario);
						System.out.println("Lucro: " + itemVenda.getLucro());
						System.out.println("PercentualDeLucro: " + itemVenda.getPercentualLucro());
	
						venda.setValorTotal(BigDecimal
								.valueOf(venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));
	
						itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
						itensVenda.add(itemVenda);
	
						String code = itemVenda.getCode();
						Produto produto = itemVenda.getProduto();
											
	
						itemVenda = new ItemVenda();
						itemVenda.setCode(code);
						itemVenda.setProduto(produto);
									
	
						atualizaSaldoLoteDeCompras(produto);
										
						
						/* Nova entrada de produto */
						itemVenda = new ItemVenda();
						itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
						this.produto = new Produto();
						filter = new ProdutoFilter();
						
				} else {
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Quantidade maior que a disponível!', timer: 3000 });");
				}
				
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor ou igual a zero!', timer: 3000 });");
			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível adicionar itens à esta venda!' });");
		}
	}
	

	private void atualizaSaldoLoteDeCompras(Produto produto) {
		
		itensCompra = new ArrayList<ItemCompra>();
		
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
		
		System.out.println("Lista: "+itensVenda.size());
		
		List<String> itens = new ArrayList<>();
		
		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			itemCompraTemp.getCompra()
					.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

			boolean produtoNaLista = false;
			for (ItemVenda itemVenda : itensVenda) {
				
				if(!itens.contains(itemVenda.getCode())) {
					itemVenda.setSaldo(itemVenda.getQuantidade());
					itens.add(itemVenda.getCode());
				}
				
				for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
					if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
							.longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {

							produtoNaLista = true;
							
							/*if(!saldo) {
								itemVenda.setSaldo(itemVenda.getQuantidade());
								saldo = true;
							}	*/
							
							if (itemVenda.getId() == null && venda.getId() == null) {
								
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().longValue());
								
								System.out.println("itemVenda.getSaldo() > 0L : " + (itemVenda.getSaldo() > 0L));
								if(itemVenda.getSaldo() > 0L) {
									
									if(itemVenda.getSaldo().longValue() <= itemCompraTemp.getQuantidadeDisponivel().longValue()) {
										itemCompraTemp.setQuantidadeDisponivel(
												itemCompraTemp.getQuantidadeDisponivel().longValue() - itemVenda.getSaldo().longValue());
										itemVenda.setSaldo(0L);
										
									} else {			
										itemVenda.setSaldo(itemVenda.getSaldo().longValue() - itemCompraTemp.getQuantidadeDisponivel().longValue());
										itemCompraTemp.setQuantidadeDisponivel(0L);
										System.out.println("Saldo: "+itemVenda.getSaldo());
									}
								}
								
								System.out.println("Saldo Atual: "+itemVenda.getSaldo());
								
								/*itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel() - saldo);*/
							}

						}
					}
				}		
			
			}

			if (produtoNaLista != false) {
				if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}

			if (produtoNaLista != true) {
				if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}
			
			System.out.println(itemCompraTemp.getQuantidadeDisponivel());
			
		}


		itensCompraTemp = new ArrayList<>();
		for (int i = itensCompra.size() - 1; i >= 0; i--) {
			itensCompra.get(i).setValorUnitarioFormatado(
					"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
			itensCompraTemp.add(itensCompra.get(i));
		}
		

		itensCompra = new ArrayList<>();
		itensCompra.addAll(itensCompraTemp);
	}
	

	public void removeItem() {

		if (venda.getId() == null) {

			// itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);
			
			if(itemVenda.getProduto() != null) {
				itemVenda.getProduto().setQuantidadeAtual(itemVenda.getProduto().getQuantidadeAtual().longValue() + itemSelecionado.getQuantidade());
			}

			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());

			itensCompra = new ArrayList<ItemCompra>();
			List<String> itens = new ArrayList<String>();

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

				boolean produtoNaLista = false;
				for (ItemVenda itemVenda : itensVenda) {
					
					/*if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {

							produtoNaLista = true;
							if (itemVenda.getId() == null && venda.getId() == null) {
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
							}

						}
					}*/
					
					
					if(!itens.contains(itemVenda.getCode())) {
						itemVenda.setSaldo(itemVenda.getQuantidade());
						itens.add(itemVenda.getCode());
					}
					
					for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
						if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {

								produtoNaLista = true;
								
								if (itemVenda.getId() == null && venda.getId() == null) {
									
									System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().longValue());
									
									System.out.println("itemVenda.getSaldo() > 0L : " + (itemVenda.getSaldo() > 0L));
									if(itemVenda.getSaldo() > 0L) {
										
										if(itemVenda.getSaldo().longValue() <= itemCompraTemp.getQuantidadeDisponivel().longValue()) {
											itemCompraTemp.setQuantidadeDisponivel(
													itemCompraTemp.getQuantidadeDisponivel().longValue() - itemVenda.getSaldo().longValue());
											itemVenda.setSaldo(0L);
											
										} else {			
											itemVenda.setSaldo(itemVenda.getSaldo().longValue() - itemCompraTemp.getQuantidadeDisponivel().longValue());
											itemCompraTemp.setQuantidadeDisponivel(0L);
											System.out.println("Saldo: "+itemVenda.getSaldo());
										}
									}
									
									System.out.println("Saldo Atual: "+itemVenda.getSaldo());
								}

							}
						}
					}
					
				}

				if (produtoNaLista != false) {
					if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}

				if (produtoNaLista != true) {
					if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}
			}

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
			
			activeIndex = 0;

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível remover os itens desta venda!' });");
		}
	}

	public void editarItem() {

		if (venda.getId() == null) {

			//itemVenda = itemSelecionado;
			itemVenda = new ItemVenda();
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);
			
			
			itemVenda.setCode(itemSelecionado.getCode());		
			itemVenda.setProduto(itemSelecionado.getProduto());
			//itemVenda.getProduto().setPrecoDeVenda(itemVenda.getProduto().getCustoMedioUnitario());
			itemVenda.setQuantidade(itemSelecionado.getQuantidade());
			itemVenda.setLucro(itemSelecionado.getLucro());
			itemVenda.setTotal(itemSelecionado.getTotal());
			itemVenda.setDesconto(itemSelecionado.getDesconto());
			itemVenda.setObservacoes(itemSelecionado.getObservacoes());
			

			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());

			itensCompra = new ArrayList<ItemCompra>();
			List<String> itens = new ArrayList<String>();
			
			Long quantidadeDisponivel = 0L;

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));

				boolean produtoNaLista = false;
				for (ItemVenda itemVenda : itensVenda) {
					
					/*if (itemCompraTemp.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {

							produtoNaLista = true;
							if (itemVenda.getId() == null && venda.getId() == null) {
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
							}

						}
					}*/
					
					
					if(!itens.contains(itemVenda.getCode())) {
						itemVenda.setSaldo(itemVenda.getQuantidade());
						itens.add(itemVenda.getCode());
					}
					
					for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
						if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {

								produtoNaLista = true;
								
								if (itemVenda.getId() == null && venda.getId() == null) {
									
									System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().longValue());
									
									System.out.println("itemVenda.getSaldo() > 0L : " + (itemVenda.getSaldo() > 0L));
									if(itemVenda.getSaldo() > 0L) {
										
										if(itemVenda.getSaldo().longValue() <= itemCompraTemp.getQuantidadeDisponivel().longValue()) {
											itemCompraTemp.setQuantidadeDisponivel(
													itemCompraTemp.getQuantidadeDisponivel().longValue() - itemVenda.getSaldo().longValue());
											itemVenda.setSaldo(0L);
											
										} else {			
											itemVenda.setSaldo(itemVenda.getSaldo().longValue() - itemCompraTemp.getQuantidadeDisponivel().longValue());
											itemCompraTemp.setQuantidadeDisponivel(0L);
											System.out.println("Saldo: "+itemVenda.getSaldo());
										}
									}
									
									System.out.println("Saldo Atual: "+itemVenda.getSaldo());
								}

							}
						}
					}
				}

				if (produtoNaLista != false) {
					if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}

				if (produtoNaLista != true) {
					if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
						itensCompra.add(itemCompraTemp);
					}
				}
				
				quantidadeDisponivel += itemCompraTemp.getQuantidadeDisponivel();
			}
			
			
			itemVenda.getProduto().setQuantidadeAtual(quantidadeDisponivel);				
			

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
			
			activeIndex = 0;

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível editar os itens desta venda!' });");
		}

	}
	
	
	public void detalharItem() {

		//if (venda.getId() == null) {

			//itemVenda = itemSelecionado;
			itemVenda = new ItemVenda();
			//venda.setValorTotal(
					//BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			//itensVenda.remove(itemSelecionado);
			
			
			itemVenda.setCode(itemSelecionado.getCode());
			itemVenda.setProduto(itemSelecionado.getProduto());
			itemVenda.getProduto().setCustoMedioUnitario(itemSelecionado.getValorCompra());
			itemVenda.getProduto().setMargemLucro(itemSelecionado.getPercentualLucro());
			System.out.println("Margem: "+itemVenda.getProduto().getMargemLucro());
			System.out.println("Custo medio Un: "+itemVenda.getProduto().getCustoMedioUnitario());
			itemVenda.setQuantidade(itemSelecionado.getQuantidade());
			itemVenda.setDesconto(itemSelecionado.getDesconto());
			itemVenda.setObservacoes(itemSelecionado.getObservacoes());
			itemVenda.setLucro(itemSelecionado.getLucro());
			itemVenda.setTotal(itemSelecionado.getTotal());
		
			//itemVenda.getProduto().setPrecoDeVenda(itemSelecionado.getProduto().getCustoMedioUnitario());


			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());

			itensCompra = new ArrayList<ItemCompra>();
			
			Long quantidadeDisponivel = 0L;

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
	
				if (itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
		
				quantidadeDisponivel += itemCompraTemp.getQuantidadeDisponivel();
			}
			
			
			itemVenda.getProduto().setQuantidadeAtual(quantidadeDisponivel);				
			

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);
			
			activeIndex = 0;

			//itemSelecionado = null;

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();

		/*} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível editar os itens desta venda!' });");
		}*/

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

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public boolean isLeitor() {
		return leitor;
	}

	public void setLeitor(boolean leitor) {
		this.leitor = leitor;
	}

	public Integer getActiveIndex() {
		return activeIndex;
	}
}
