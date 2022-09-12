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

import com.webapp.model.Conta;
import com.webapp.model.Devolucao;
import com.webapp.model.Entrega;
import com.webapp.model.ItemCaixa;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Log;
import com.webapp.model.Pagamento;
import com.webapp.model.Produto;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoOperacao;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Contas_;
import com.webapp.repository.Devolucoes;
import com.webapp.repository.Entregas;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Logs;
import com.webapp.repository.Pagamentos;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class AtividadesBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Usuario usuario_;
	
	@Inject
	private Usuarios usuarios;

	private Date dateStart = new Date();

	private Date dateStop = new Date();
	
	private TipoAtividade tipoAtividade;
	
	@Inject
	private Usuario usuario;
	
	private List<Usuario> todosUsuarios;
	
	private List<Log> logsFiltrados = new ArrayList<>();
	
	private Log logSelecionado;
	
	@Inject
	private Logs logs;
	
	@Inject
	private Vendas vendas;
	
	@Inject
	private Venda venda;
	
	private boolean habilitaOpcoes = false;
	
	@Inject
	private Contas_ contas;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	@Inject
	private Pagamentos pagamentos;

	@Inject
	private Produtos produtos;
	
	@Inject
	private Devolucoes devolucoes;
	
	@Inject
	private ItensDevolucoes itensDevolucoes;
	
	@Inject
	private Entregas entregas;

	@Inject
	private Entrega entrega;
	
	@Inject
	private ItensCompras itensCompras;
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario_ = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
		}
	}

	public void pesquisar() {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		logSelecionado = null;
		
		logsFiltrados = new ArrayList<>();
		logsFiltrados = logs.logsFiltrados(dateStart, calendarioTemp.getTime(), tipoAtividade, usuario, usuario_.getEmpresa());
		
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
	
	
	public TipoAtividade[] getTiposAtividades() {
		return TipoAtividade.values();
	}

	public TipoAtividade getTipoAtividade() {
		return tipoAtividade;
	}

	public void setTipoAtividade(TipoAtividade tipoAtividade) {
		this.tipoAtividade = tipoAtividade;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public Log getLogSelecionado() {
		return logSelecionado;
	}

	public void setLogSelecionado(Log logSelecionado) {
		this.logSelecionado = logSelecionado;
	}

	public List<Log> getLogsFiltrados() {
		return logsFiltrados;
	}
	
	public int getLogsFiltradosSize() {
		return logsFiltrados.size();
	}
	
	public void prepararOpcoes() {
		venda = null;
		habilitaOpcoes = false;
		if(logSelecionado.getDescricao().contains("Deletou venda")) { 
			String numeroVenda = logSelecionado.getDescricao().substring(18).split(",")[0].trim();
			venda = vendas.porNumeroVenda(Long.parseLong(numeroVenda), usuario_.getEmpresa());
			
			if(venda != null && venda.isExclusao()) {
				habilitaOpcoes = true;
			}
		}	
	}

	public Venda getVenda() {
		return venda;
	}
	
	public boolean isHabilitaOpcoes() {
		return habilitaOpcoes;
	}
	
	public void desfazerExclusao() {
		exclusao(false);
	}
	
	public void excluirPraSempre() {
		exclusao(true);
	}
	
	public void exclusao(boolean excluir) {

		if (logSelecionado != null && venda != null && venda.isExclusao()) {
					
			List<Conta> contasTemp = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario_.getEmpresa());
			for (Conta conta : contasTemp) {
				if(!excluir) {
					conta.setExclusao(false);
					contas.save(conta);
				} else {
					contas.remove(conta);
				}
			}
			
			/*
			Lancamento lancamento = lancamentos.porValor(vendaSelecionada.getLucro().setScale(2, BigDecimal.ROUND_HALF_EVEN), usuario_.getEmpresa());
			if(lancamento != null) {
				lancamentos.remove(lancamento);
			}
			*/
			
			List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
			for (ItemVenda itemVenda : itensVenda) {
					
				Produto produto = itemVenda.getProduto();
				
				if(excluir) {	
					if(produto.isEstoque()) {
						
						produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()));
	
						List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
						for (ItemCompra itemCompra : itensCompra) {
							
							if(venda.isPdv()) {
								
								List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
								
								for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {	
									
									if (itemCompra.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId().longValue()) {
										if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
											System.out.println("Quantidade Disponivel: "+itemCompra.getQuantidadeDisponivel());
											System.out.println("Quantidade Retornada: "+itemVenda.getQuantidade());
											
											itemCompra.setQuantidadeDisponivel(new BigDecimal(
													itemCompra.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));
											itensCompras.save(itemCompra);
										}
									}
								}		
								
							} else {
								
								if (itemCompra.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
									if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
										System.out.println(itemCompra.getQuantidadeDisponivel());
										System.out.println(itemVenda.getQuantidade());
										itemCompra.setQuantidadeDisponivel(new BigDecimal(
												itemCompra.getQuantidadeDisponivel().doubleValue() + itemVenda.getQuantidade().doubleValue()));
										itensCompras.save(itemCompra);
									}
								}
							}
						}
					}
				
					List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
					
					for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {
						itensVendasCompras.remove(itemVendaCompra);
					}
	
					itensVendas.remove(itemVenda);	
							
				} else {
			
					List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
					
					for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {
						itemVendaCompra.setExclusao(false);
						itensVendasCompras.save(itemVendaCompra);
					}
		
					itemVenda.setExclusao(false);
					itensVendas.save(itemVenda);
				
				}
						
				
				if(venda.isPdv()) {
					
					if(venda.isAjuste()) {
						
						if(!venda.isRecuperarValores()) {
							
							//List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
							if(excluir) {	
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemVenda.getQuantidade().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));					
							}
							//}
						}
												
					} else {					
							
						if(produto.isEstoque()) {
							
							/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
							/*
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));											
							
							Object[] result = itensCompras.porQuantidadeDisponivel(produto);
							
							if((BigDecimal) result[0] != null) {
							
								Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
								
								//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
								
								produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
								
								produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
								
								produto.setCustoTotal((BigDecimal) result[1]);	
							}
							*/

							
							/*
							if(produto.getQuantidadeAtual().doubleValue() <= 0) {
								produto.setCustoMedioUnitario(BigDecimal.ZERO);
								
								if(produto.getCustoTotal().doubleValue() > 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() - produto.getCustoTotal().doubleValue()));													
									
								} else if(produto.getCustoTotal().doubleValue() < 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + (-1 * produto.getCustoTotal().doubleValue())));								
								
								} else {
									produto.setEstorno(BigDecimal.ZERO);
								}
								
								produto.setCustoTotal(BigDecimal.ZERO);
							}
							*/
						}
						
						
						List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(venda.getNumeroVenda(), TipoOperacao.VENDA, venda.getEmpresa());

						if(itensCaixa.size() > 0) {
							
							for (ItemCaixa itemCaixa : itensCaixa) {
								if(!excluir) {	
									itemCaixa.setExclusao(false);
									itensCaixas.save(itemCaixa);
								} else {
									itensCaixas.remove(itemCaixa);
								}
							}
							
						}
						
						Pagamento pagamento = pagamentos.porVenda(venda, venda.getEmpresa());
						if(pagamento != null) {
							if(!excluir) {
								pagamento.setExclusao(false);
								pagamentos.save(pagamento);
							} else {
								pagamentos.remove(pagamento);
							}
						}				
						
					}
					
				} else {
					
					if(venda.isAjuste()) {
						
						if(!venda.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());												
							if(excluir) {
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemVenda.getQuantidade().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));					
							}
						}
												
					} else {					
						
						if(produto.isEstoque()) {
							
							/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
							/*
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));											
							
							Object[] result = itensCompras.porQuantidadeDisponivel(produto);
							
							if((BigDecimal) result[0] != null) {
							
								Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
								
								//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
								
								produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
								
								produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
								
								produto.setCustoTotal((BigDecimal) result[1]);	
							}

							
							
							if(produto.getQuantidadeAtual().doubleValue() <= 0) {
								produto.setCustoMedioUnitario(BigDecimal.ZERO);
								
								if(produto.getCustoTotal().doubleValue() > 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() - produto.getCustoTotal().doubleValue()));													
									
								} else if(produto.getCustoTotal().doubleValue() < 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + (-1 * produto.getCustoTotal().doubleValue())));								
								
								} else {
									produto.setEstorno(BigDecimal.ZERO);
								}
								
								produto.setCustoTotal(BigDecimal.ZERO);
							}
							*/
						}				
					}
				}
				
				//produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / produto.getQuantidadeAtual().intValue()));
				produtos.save(produto);
			}
			
			
			List<Devolucao> listaDeDevolucoes = new ArrayList<Devolucao>();
			List<ItemDevolucao> itensDevolucao = itensDevolucoes.porVenda(venda);
			for (ItemDevolucao itemDevolucao : itensDevolucao) {
				
				Devolucao devolucao = devolucoes.porId(itemDevolucao.getDevolucao().getId());
				
				if(!listaDeDevolucoes.contains(devolucao)) {
					listaDeDevolucoes.add(devolucao);
				}
				/*
				List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(itemDevolucao.getId(), TipoOperacao.DEVOLUCAO, vendaSelecionada.getEmpresa());

				if(itensCaixa.size() > 0) {
					
					for (ItemCaixa itemVenda_ : itensCaixa) {
						itensCaixas.remove(itemVenda_);
					}
					
				}
				*/
				
				if(!excluir) {
					itemDevolucao.setExclusao(false);
					itensDevolucoes.save(itemDevolucao);
				} else {
					itensDevolucoes.remove(itemDevolucao);
				}
			}
			
			for (Devolucao devolucao : listaDeDevolucoes) {
				System.out.println(devolucao);
				//devolucoes.remove(devolucao);
			}
			

			entrega = entregas.porVenda(venda);
			
			if (entrega.getId() != null) {
				
				if(!excluir) {
					entrega.setExclusao(false);
					entregas.save(entrega);
				} else {
					entregas.remove(entrega);
				}
			}

			if(!excluir) {
				venda.setExclusao(false);
				vendas.save(venda);
			} else {
				vendas.remove(venda);
			}
			
			Log log = new Log();
			log.setDataLog(new Date());
			log.setCodigoOperacao(String.valueOf(venda.getNumeroVenda()));
			log.setOperacao(TipoAtividade.VENDA.name());
			
			NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
			
			if(!excluir) {
				log.setDescricao("Desfez exclusão, venda Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));							
			} else {
				log.setDescricao("Excluiu pra sempre, venda Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));											
			}

			log.setUsuario(usuario_);
			logs.save(log);
			
			venda = null;

			pesquisar();
			
			if(!excluir) {
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Exclusão desfeita com sucesso!' });");
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Venda excluída pra sempre realizada com sucesso!' });");
			}

		}

	}

}
