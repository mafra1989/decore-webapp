package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Caixa;
import com.webapp.model.CategoriaLancamento;
import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.Devolucao;
import com.webapp.model.Entrega;
import com.webapp.model.ItemCaixa;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Lancamento;
import com.webapp.model.Log;
import com.webapp.model.Produto;
import com.webapp.model.TipoAtividade;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Caixas;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.Devolucoes;
import com.webapp.repository.Entregas;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class ConsultaComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Compra> comprasFiltradas = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuario usuario_;

	@Inject
	private Compras compras;

	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;

	@Inject
	private ItensCompras itensCompras;

	
	
	@Inject
	private ItensDevolucoes itensDevolucoes;
	
	@Inject
	private Devolucoes devolucoes;
	
	@Inject
	private Entregas entregas;
	
	@Inject
	private Vendas vendas;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	@Inject
	private Caixas caixas;

	
	

	@Inject
	private Produtos produtos;
	
	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private CategoriasLancamentos categoriasLancamentos;
	
	@Inject
	private Contas contas;
	
	private Long numeroCompra;

	private Compra compraSelecionada;

	private Date dateStart = new Date();

	private Date dateStop = new Date();

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalCompras = "0,00";

	private Integer totalItens = 0;
	
	@Inject
	private CategoriasProdutos categoriasProdutos;
	
	private List<CategoriaProduto> todasCategoriasProduto = new ArrayList<CategoriaProduto>();
	
	private String[] categorias;
	
	private Produto produto;
	
	private List<Produto> todosProdutos;
	
	
	@Inject
	private Logs logs;
	
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();			
			usuario_ = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
			
			listarTodasCategoriasProdutos();
			
			numeroCompra = null;
		}
	}
	
	private void listarTodasCategoriasProdutos() {
		todasCategoriasProduto = categoriasProdutos.todos(usuario_.getEmpresa());
	}
	
	public void changeCategoria() {
		todosProdutos = new ArrayList<Produto>();
		produto = null;
		if (categorias != null && categorias.length > 0) {
			todosProdutos = produtos.porCategoria(categorias);
		}
	}

	public void pesquisar() {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		comprasFiltradas = compras.comprasFiltradas(numeroCompra, dateStart, calendarioTemp.getTime(), usuario, usuario_.getEmpresa());
		
		compraSelecionada = null;
		
		List<Compra> comprasFiltradasPorCategoria = new ArrayList<Compra>();
		List<Compra> comprasFiltradasPorProduto = new ArrayList<Compra>();

		double totalComprasTemp = 0; //double valorTotal = 0; double valorTotalTemp = 0;
		totalItens = 0;
		for (Compra compra : comprasFiltradas) {
			
			if(!compra.isAjuste()) {
				
				if(produto != null) {					
					ItemCompra itemCompra = itensCompras.porCompra(compra, produto);
					if(itemCompra != null) {
						comprasFiltradasPorProduto.add(compra);
						totalComprasTemp += itemCompra.getTotal().doubleValue();
						totalItens += itemCompra.getQuantidade().intValue();
					}
					
				} else if (categorias != null && categorias.length > 0) {
									
					List<ItemCompra> itens = itensCompras.porCategoria(compra, categorias);
					for (ItemCompra itemCompra : itens) {
						
						if(!comprasFiltradasPorCategoria.contains(compra)) {
							comprasFiltradasPorCategoria.add(compra);
						}						
						
						totalComprasTemp += itemCompra.getTotal().doubleValue();
						totalItens += itemCompra.getQuantidade().intValue();
					}
					
				} else {
					totalComprasTemp += compra.getValorTotal().doubleValue();
					totalItens += compra.getQuantidadeItens().intValue();
				}
				
				
				if(compra.isConta()) {
					compra.setCompraPaga(true);
					List<Conta> listaDeContas = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA", usuario_.getEmpresa());
					for (Conta conta : listaDeContas) {
						if(!conta.isStatus()) {
							compra.setCompraPaga(false);
						}
					}
				}
			}
		/*	
			if(compra.getNumeroCompra().intValue() == 12 && compra.getId() == 28592) {
				compra.setNumeroCompra(7L);
				compras.save(compra);
			}
			
			List<Conta> listaDeContas = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA");
			if(listaDeContas.size() > 0) {

				for (Conta contaTemp : listaDeContas) {
					valorTotalTemp += contaTemp.getValor().doubleValue(); 
					System.out.println("Valor: " + valorTotalTemp);
					contas.remove(contaTemp);
				}
			}
				
			
			if(!compra.isAjuste()) {
				
				Conta conta = new Conta();
				conta.setCodigoOperacao(compra.getNumeroCompra());
				conta.setOperacao("COMPRA");
				conta.setParcela(TipoPagamento.AVISTA.name());
				conta.setTipo("DEBITO");
				conta.setStatus(true);
				conta.setValor(compra.getValorTotal());
				
				conta.setVencimento(compra.getDataCompra());
				conta.setPagamento(compra.getDataCompra());
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getVencimento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				conta = contas.save(conta);	
				System.out.println(conta.getId());
				valorTotal += conta.getValor().doubleValue(); 
				System.out.println(valorTotal);
			}
			*/
					
		}
		
		if(produto != null) {					
			comprasFiltradas = new ArrayList<Compra>();
			comprasFiltradas.addAll(comprasFiltradasPorProduto);			
		} else if (categorias != null && categorias.length > 0) {
			comprasFiltradas = new ArrayList<Compra>();
			comprasFiltradas.addAll(comprasFiltradasPorCategoria);
		}

		totalCompras = nf.format(totalComprasTemp);
	}

	public void excluir() {

		if (compraSelecionada != null) {

			List<ItemVenda> itensVenda = itensVendas.porCompra(compraSelecionada);
			
			List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porCompra_(compraSelecionada);

			if (itensVenda.size() == 0 && itensVendaCompra.size() == 0) {

				boolean contasPagas = false;
				//List<Conta> contasTemp = contas.porContasPagas(compraSelecionada.getNumeroCompra(), "COMP");

				//if (contasTemp.size() == 0) {

				List<Conta> contasTemp = contas.porCodigoOperacao(compraSelecionada.getNumeroCompra(), "COMPRA", usuario_.getEmpresa());
				for (Conta conta : contasTemp) {
					contas.remove(conta);
				}

				/*} else {
					contasPagas = true;
					PrimeFaces.current().executeScript(
							"stop();swal({ type: 'error', title: 'Erro!', text: 'Existe contas à pagar já registradas para essa compra!' });");
				}*/


				if (contasPagas != true) {
					
					List<ItemCompra> itensCompra = itensCompras.porCompra(compraSelecionada);
					for (ItemCompra itemCompra : itensCompra) {
						
						Produto produto = produtos.porId(itemCompra.getProduto().getId());
						
						/*if(produto.isEstoque()) {
							produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()));							
						}*/						
						
						itensCompras.remove(itemCompra);
											
						if(!compraSelecionada.isAjuste()) {
							
							if(produto.isEstoque()) {
							
								/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - (itemCompra.getQuantidade().longValue() * itemCompra.getValorUnitario().doubleValue())));											
	
								//itensCompras.remove(itemCompra);
								
								/*								
								Object[] result = itensCompras.porQuantidadeDisponivel(produto);
								
								if((BigDecimal) result[0] != null) {
								
									Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
									
									//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
									
									produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().longValue()));
									
									produto.setCustoTotal((BigDecimal) result[1]);	
								}

							
							
								if(produto.getQuantidadeAtual().longValue() <= 0) {
									produto.setCustoMedioUnitario(BigDecimal.ZERO);
									
									if(produto.getCustoTotal().doubleValue() > 0) {
										produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() - produto.getCustoTotal().doubleValue()));													
										
									} else if(produto.getCustoTotal().doubleValue() < 0) {
										produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + (-1 * produto.getCustoTotal().doubleValue())));								
									
									} else {
										//produto.setEstorno(BigDecimal.ZERO);
									}
									
									produto.setCustoTotal(BigDecimal.ZERO);
								}
								*/
								
								produtos.save(produto);
							}
							
							
						} else {
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - (itemCompra.getQuantidade().longValue() * produto.getCustoMedioUnitario().doubleValue())));							
						}
				
						/*
						if(produto.getQuantidadeAtual().doubleValue() <= 0) {
							produto.setCustoMedioUnitario(BigDecimal.ZERO);
							//produto.setEstorno(BigDecimal.ZERO);
						}*/
						
						produtos.save(produto);

					}

					
					compras.remove(compraSelecionada);
					
					
					List<Compra> todasCompras = compras.todas(usuario_.getEmpresa());	
					List<Venda> todasVendas_ = vendas.todas_(usuario_.getEmpresa());	
					
					if(todasCompras.size() == 0 && todasVendas_.size() == 0) {
						
						List<ItemDevolucao> todosItensDevolucoes = itensDevolucoes.todos(usuario_.getEmpresa());
						for (ItemDevolucao itemDevolucao : todosItensDevolucoes) {
							itensDevolucoes.remove(itemDevolucao);
						}
						
						List<Devolucao> todasDevolucoes = devolucoes.todas(usuario_.getEmpresa());
						for (Devolucao devolucao : todasDevolucoes) {
							devolucoes.remove(devolucao);
						}
						
						List<Entrega> todasEntregas = entregas.todas(usuario_.getEmpresa());
						for (Entrega entrega : todasEntregas) {
							entregas.remove(entrega);
						}
						
						List<Venda> todasVendas = vendas.todas(usuario_.getEmpresa());
						for (Venda venda : todasVendas) {
							vendas.remove(venda);
						}
						
						List<ItemCaixa> todosItensCaixa = itensCaixas.todos(usuario_.getEmpresa());
						for (ItemCaixa itemCaixa : todosItensCaixa) {
							itensCaixas.remove(itemCaixa);
						}
						
						List<Caixa> todosCaixas = caixas.todos(usuario_.getEmpresa());
						for (Caixa caixa : todosCaixas) {
							caixas.remove(caixa);
						}
						
						List<Produto> todosProdutos = produtos.todos(usuario_.getEmpresa());
						for (Produto produto : todosProdutos) {
							produto.setEstorno(BigDecimal.ZERO);
							produtos.save(produto);
						}
						
						CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome("Retirada de lucro", null);
						List<Lancamento> todosLancamento = lancamentos.todos(usuario_.getEmpresa(), categoriaLancamento);
						for (Lancamento lancamento : todosLancamento) {
							lancamentos.remove(lancamento);
						}
					}
					
					
					Log log = new Log();
					log.setDataLog(new Date());
					log.setCodigoOperacao(String.valueOf(compraSelecionada.getNumeroCompra()));
					log.setOperacao(TipoAtividade.COMPRA.name());
					
					NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
					
					log.setDescricao("Deletou compra, Nº " + compraSelecionada.getNumeroCompra() + ", quantidade de itens " + compraSelecionada.getQuantidadeItens() + ", valor total R$ " + nf.format(compraSelecionada.getValorTotal()));
					log.setUsuario(usuario_);		
					logs.save(log);
					

					compraSelecionada = null;
					pesquisar();
					PrimeFaces.current().executeScript(
							"swal({ type: 'success', title: 'Concluído!', text: 'Compra excluída com sucesso!' });");
				}
				
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Existem itens dessa compra já vinculados a uma ou mais vendas!' });");
			}

		} /*else {

			for (Compra compra : comprasFiltradas) {
				List<ItemVenda> itensVenda = itensVendas.porCompra(compra);

				if (itensVenda.size() == 0) {

					List<ItemCompra> itensCompra = itensCompras.porCompra(compra);
					for (ItemCompra itemCompra : itensCompra) {
						Produto produto = itemCompra.getProduto();
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
						produtos.save(produto);

						itensCompras.remove(itemCompra);
					}

					compras.remove(compra);
				}
			}

			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Compras excluídas com sucesso!' });");
		}*/

	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
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

	public Compra getCompraSelecionada() {
		return compraSelecionada;
	}

	public void setCompraSelecionada(Compra compraSelecionada) {
		this.compraSelecionada = compraSelecionada;
	}

	public List<Compra> getComprasFiltradas() {
		return comprasFiltradas;
	}

	public void setComprasFiltradas(List<Compra> comprasFiltradas) {
		this.comprasFiltradas = comprasFiltradas;
	}

	public int getComprasFiltradasSize() {
		return comprasFiltradas.size();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getTotalCompras() {
		return totalCompras;
	}

	public Integer getTotalItens() {
		return totalItens;
	}

	public Long getNumeroCompra() {
		return numeroCompra;
	}

	public void setNumeroCompra(Long numeroCompra) {
		this.numeroCompra = numeroCompra;
	}

	public List<CategoriaProduto> getTodasCategoriasProduto() {
		return todasCategoriasProduto;
	}

	public String[] getCategorias() {
		return categorias;
	}

	public void setCategorias(String[] categorias) {
		this.categorias = categorias;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public List<Produto> getTodosProdutos() {
		return todosProdutos;
	}

}
