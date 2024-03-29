package com.webapp.controller;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Caixa;
import com.webapp.model.Devolucao;
import com.webapp.model.Entrega;
import com.webapp.model.EspelhoVenda;
import com.webapp.model.ItemCaixa;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.ItemEspelhoVendaProdutos;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.TipoOperacao;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Caixas;
import com.webapp.repository.Devolucoes;
import com.webapp.repository.Entregas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class DevolucoesTrocaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Venda> vendasFiltradas = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuario usuario_;

	@Inject
	private Vendas vendas;

	@Inject
	private Entregas entregas;

	@Inject
	private Entrega entrega;

	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;

	@Inject
	private Produtos produtos;

	@Inject
	private ItensCompras itensCompras;

	private Venda vendaSelecionada;
	
	private ItemVenda itemVendaSelecionada;

	private Long numeroVenda;

	private Date dateStart = new Date();

	private Date dateStop = new Date();
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalVendas = "0,00";

	private Long totalItens = 0L;

	private boolean vendasNormais;

	private StatusPedido[] statusPedido;
	
	@Inject
	private Produto produto;
	
	@Inject
	private Produto produto_;
	
	private ProdutoFilter filter = new ProdutoFilter();
	
	private boolean leitor = true;
	
	@Inject
	private Devolucao devolucao;
	
	@Inject
	private Devolucoes devolucoes;
	
	@Inject
	private ItensDevolucoes itensDevolucoes;
	
	private List<ItemVenda> itensVendasFiltradas;
	
	private List<ItemVenda> listaDevolucao = new ArrayList<ItemVenda>();
	
	private String saldoParaTroca;
	
	private Long itensParaDevolucao = 0L;
	
	private boolean trocaPendente = false;
	
	private List<ItemDevolucao> listaDevolucaoTemp;
	
	
	@Inject
	private Caixas caixas;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	private boolean caixaAberto = false;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			numeroVenda = null;
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario_ = usuarios.porLogin(user.getUsername());
			
			
			Calendar calendario = Calendar.getInstance();
			calendario.setTime(dateStop);
			calendario.set(Calendar.HOUR, 00);
			calendario.set(Calendar.MINUTE, 00);
			calendario.set(Calendar.SECOND, 00);
			calendario.set(Calendar.DAY_OF_MONTH, 1);
			
			dateStart = calendario.getTime();
			
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
			
			Object[] saldo = itensDevolucoes.saldoParaTroca(usuario_, usuario_.getEmpresa());
			
			if(saldo[0] != null) {
				saldoParaTroca = nf.format(((BigDecimal)saldo[1]).doubleValue());
				itensParaDevolucao = ((BigDecimal) saldo[0]).longValue();
				
				trocaPendente = true;
			}
			
			listaDevolucaoTemp = itensDevolucoes.listaDevolucao();
			for (ItemDevolucao itemDevolucao : listaDevolucaoTemp) {
				ItemVenda itemVenda = new ItemVenda();
				itemVenda.setVenda(itemDevolucao.getVenda());
				itemVenda.setProduto(itemDevolucao.getProduto());
				itemVenda.setQuantidade(itemDevolucao.getQuantidade());
				itemVenda.setTotal(itemDevolucao.getValorTotal());
				
				listaDevolucao.add(itemVenda);
			}
			
			
			Caixa caixa = caixas.porUsuario(usuario_, usuario_.getEmpresa());
			if(caixa != null) {
				caixaAberto = true;
			}
		
		}
	}
	
	
	public List<Produto> completeText_Produto(String query) {
		System.out.println(query);
		filter.setEmpresa(usuario_.getEmpresa());
		filter.setDescricao(query);
		
		List<Produto> listaProdutos = produtos.filtrados(filter);
         
        return listaProdutos;
    }
	
	public void onItemSelect(SelectEvent event) {	
		
        System.out.println(event.getObject().toString());
        produto = (Produto) event.getObject();
    }
	
	
	
	public void pesquisar_() {
		System.out.println("Código escaneado: " + filter.getCodigo());
		
		if(caixaAberto) {
			
			if(!trocaPendente) {
				
				if(leitor) {
					
					if(!filter.getCodigo().trim().equals("")) {
						
						produto = produtos.porCodigoDeBarras(filter.getCodigo(), usuario_.getEmpresa());	
						
						pesquisar(produto);
						
						if(produto == null) {
							
							filter = new ProdutoFilter();
							PrimeFaces.current()
							.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto não encontrado!', timer: 1500 });");
							
							itensVendasFiltradas = new ArrayList<ItemVenda>();
										
						} 
						
					} else {
						produto = null;
						pesquisar(produto);
					}
					
				} else {
					
					pesquisar(produto_);
					
					/*if(produto != null) {
						
						pesquisar();
												
					} else {

						PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto não encontrado!', timer: 1500 });");

					}*/
				}
				
			} else {
				
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Ops!', text: 'Exitem itens pendentes para troca!', timer: 2000 });");

			}
	        
		} else {

			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Caixa Fechado!', text: 'Para fazer devolução, primeiro você deve abrir o caixa!', timer: 5000 });");		
		}
		
	}
	

	public void pesquisar(Produto produto) {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		
		
		List<ItemVenda> vendasFiltradasPorProduto = new ArrayList<ItemVenda>();
 
		totalItens = 0L;
			
		vendasFiltradas = vendas.vendasFiltradasPDV(numeroVenda, dateStart, calendarioTemp.getTime(), usuario, usuario_.getEmpresa());
		
		if(produto != null) {
					
			for (Venda venda : vendasFiltradas) {
				
				if(!venda.isAjuste() && !venda.isConta()) {
								
					itensVendasFiltradas = itensVendas.porVendaProduto(venda, produto);
					
					if(itensVendasFiltradas.size() > 0) {						

						for (ItemVenda object : itensVendasFiltradas) {
							
							for (ItemVenda itemVenda : listaDevolucao) {
								
								if(object.getId().longValue() == itemVenda.getId().longValue()) {
									Double valorUnitario = object.getTotal().doubleValue() / object.getQuantidade().doubleValue();
									object.setQuantidade(new BigDecimal(object.getQuantidade().doubleValue() - itemVenda.getQuantidade().doubleValue()));
									object.setTotal(new BigDecimal(object.getQuantidade().doubleValue() * valorUnitario));
								}
							}
							
							if(object.getQuantidade().doubleValue() > 0) {
								vendasFiltradasPorProduto.add(object);
							}
							
						}
						
					}
				}
			}
			
		} else {
			
			for (Venda venda : vendasFiltradas) {
								
				itensVendasFiltradas = itensVendas.porVenda(venda);

				if(itensVendasFiltradas.size() > 0) {						
							
					for (ItemVenda object : itensVendasFiltradas) {
						
						for (ItemVenda itemVenda : listaDevolucao) {
							
							if(itemVenda.getId() != null) {
								
								if(object.getId().longValue() == itemVenda.getId().longValue()) {
									Double valorUnitario = object.getTotal().doubleValue() / object.getQuantidade().doubleValue();
									object.setQuantidade(new BigDecimal(object.getQuantidade().doubleValue() - itemVenda.getQuantidade().doubleValue()));
									object.setTotal(new BigDecimal(object.getQuantidade().doubleValue() * valorUnitario));
								}
							}
							
						}		
						
						if(object.getQuantidade().doubleValue() > 0) {
							vendasFiltradasPorProduto.add(object);
						}
					}

				}
			}
		}
		
		
		
			
		itensVendasFiltradas = new ArrayList<ItemVenda>();
		itensVendasFiltradas.addAll(vendasFiltradasPorProduto);
		
		
		/*if(vendasFiltradasPorProduto.size() > 0) {
			produto.setValorPago(new BigDecimal(vendasFiltradasPorProduto.get(0).getTotal().doubleValue() / vendasFiltradasPorProduto.get(0).getQuantidade().longValue()));
		}*/
		
		
		
		if(itensVendasFiltradas.size() > 0) {
			
			Long qtdItens = 0L;
			Double valorTotal = 0D;
			for (ItemVenda itemVenda : itensVendasFiltradas) {
				
				if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {
					qtdItens += itemVenda.getQuantidade().longValue();		
				} else {
					qtdItens += 1;
				}
								
				valorTotal += itemVenda.getTotal().doubleValue();
			}						
			
			totalVendas = nf.format(valorTotal);
			totalItens += new BigDecimal(qtdItens).longValue();
			
			
			
			String mensagem = itensVendasFiltradas.size() + " resultado encontrado!";
			
			if(itensVendasFiltradas.size() > 1) {
				mensagem = itensVendasFiltradas.size() + " resultados encontrados!";
			}
			
			
			
			//PrimeFaces.current()
			//.executeScript("swal({ type: 'success', title: 'Pronto!', text: '" + mensagem + "', timer: 1000 });");
			
			PrimeFaces.current().executeScript(
					"Toast.fire({ " +
					  "icon: 'success', " +
					  "title: '"+ mensagem +"'" +
					"}) ");
			
			
			devolucao = new Devolucao();
			//PrimeFaces.current().executeScript("PF('produto-dialog').show();");
			
			
		} else {
			
			filter = new ProdutoFilter();
			//PrimeFaces.current()
			//.executeScript("swal({ type: 'warning', title: 'Atenção!', text: 'Nenhuma resultado encontrado!', timer: 1500 });");
			PrimeFaces.current().executeScript(
					"Toast.fire({ " +
					  "icon: 'error', " +
					  "title: 'Nenhum resultado encontrado!'" +
					"}) ");
		}
		
		

		itemVendaSelecionada = null;
	}
	

	
	public void prepararDevolucaoSelecionada() {	
		
		produto = itemVendaSelecionada.getProduto();
		if((produto.getUnidadeMedida().trim().toUpperCase().equals("UN") || produto.getUnidadeMedida().trim().toUpperCase().equals("CX") || produto.getUnidadeMedida().trim().toUpperCase().equals("FARDO")) && produto.isEstoque()) {
			
			produto.setValorPago(new BigDecimal(itemVendaSelecionada.getTotal().doubleValue() / itemVendaSelecionada.getQuantidade().doubleValue()));
			devolucao = new Devolucao();
			
			PrimeFaces.current().executeScript("PF('produto-dialog_').show();");
			
		} else {
			PrimeFaces.current()
			.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto com unidade de medida não permitido para devolução ou sem controle de estoque!' });");
		}
		
	}
	
	
	public void prepararItemAdicionado(ItemVenda itemVenda) {	
		produto = itemVenda.getProduto();
		produto.setValorPago(new BigDecimal(itemVenda.getTotal().doubleValue() / itemVenda.getQuantidade().doubleValue()));
		devolucao.setQuantidade(itemVenda.getQuantidade());
	}
	
	
	public void resetInput() {
		produto_ = new Produto();
	}
	
	
	public void removeItem(ItemVenda itemVenda) {
		listaDevolucao.remove(itemVenda);
		pesquisar_();
		
		Double saldo = 0D;
		itensParaDevolucao = 0L;
		
		for (ItemVenda itemVendaTmp : listaDevolucao) {
			saldo += itemVendaTmp.getTotal().doubleValue();
			
			if(!itemVendaTmp.getProduto().getUnidadeMedida().equals("Kg") && !itemVendaTmp.getProduto().getUnidadeMedida().equals("Lt")) {
				itensParaDevolucao += itemVendaTmp.getQuantidade().longValue();		
			} else {
				itensParaDevolucao += 1;
			}
		}
		
		saldoParaTroca = nf.format(saldo);
		
		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide();"
				+ "Toast.fire({ " +
				  "icon: 'success', " +
				  "title: 'Item removido com sucesso!'" +
				"}) ");
	}
	
	
	public void adicionarItem() {
		 
		if(devolucao.getQuantidade().doubleValue() <= itemVendaSelecionada.getQuantidade().doubleValue()) {
			
			if(devolucao.getQuantidade().doubleValue() > 0) {
				
				ItemVenda itemVenda = itemVendaSelecionada;
				itemVenda.setCode(itemVendaSelecionada.getProduto().getCodigo().concat("_" + new Date().getTime()));
				itemVenda.setId(itemVendaSelecionada.getId());
				itemVenda.setVenda(itemVendaSelecionada.getVenda());
				itemVenda.setQuantidade(devolucao.getQuantidade());	
				itemVenda.setTotal(new BigDecimal(devolucao.getQuantidade().doubleValue() * produto.getValorPago().doubleValue()));	
				itemVenda.setProduto(itemVendaSelecionada.getProduto());
				
				listaDevolucao.add(itemVenda);
				
				Double saldo = 0D;
				itensParaDevolucao = 0L;
				
				for (ItemVenda itemVendaTmp : listaDevolucao) {
					saldo += itemVendaTmp.getTotal().doubleValue();
					if(!itemVendaTmp.getProduto().getUnidadeMedida().equals("Kg") && !itemVendaTmp.getProduto().getUnidadeMedida().equals("Lt")) {
						itensParaDevolucao += itemVendaTmp.getQuantidade().longValue();		
					} else {
						itensParaDevolucao += 1;
					}
				}
				
				saldoParaTroca = nf.format(saldo);
				
				pesquisar_();
				
				PrimeFaces.current().executeScript(
						"PF('downloadLoading').hide();PF('produto-dialog_').hide();"
						+ "Toast.fire({ " +
						  "icon: 'success', " +
						  "title: 'Item adicionado para devolução!'" +
						"}) ");

			} else {
				
				PrimeFaces.current()
				.executeScript("PF('downloadLoading').hide();swal({ type: 'error', title: 'Erro!', text: 'Quantidade informada deve ser maior que zero!' });");
			}			
			
		} else {
			PrimeFaces.current()
			.executeScript("PF('downloadLoading').hide();swal({ type: 'error', title: 'Erro!', text: 'Quantidade informada maior que o total de itens da venda selecionada!' });");
		}
	}
	
	
	public void devolucaoSelecionada() {
		
		try {
			
			for (ItemVenda itemVenda : listaDevolucao) {
				devolverProdutoSelecionado("D", true, itemVenda);
			}
			
			
			Caixa caixa = caixas.porUsuario(usuario_, usuario_.getEmpresa());
			
			if(caixa != null) {
				
				ItemCaixa itemCaixa = new ItemCaixa();
				itemCaixa.setCaixa(caixa);
				itemCaixa.setData(new Date());
				itemCaixa.setDescricao("Devolução/Troca");
				itemCaixa.setOperacao(TipoOperacao.DEVOLUCAO);
				itemCaixa.setCodigoOperacao(devolucao.getId());
				
				itemCaixa.setTipoPagamento("Saída");
				itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario_.getEmpresa()));
				
				Double saldo = 0D;
				
				for (ItemVenda itemVendaTmp : listaDevolucao) {
					saldo += itemVendaTmp.getTotal().doubleValue();
				}
				
				itemCaixa.setValor(new BigDecimal(saldo));
				
				itensCaixas.save(itemCaixa);
			}
			
			
			itensVendasFiltradas = new ArrayList<ItemVenda>();
			listaDevolucao = new ArrayList<ItemVenda>();
			saldoParaTroca = null;
			itensParaDevolucao = 0L;
			
			produto = null;
			produto_ = null;
			
			totalItens = 0L;
			totalVendas = "0,00";
			
			filter = new ProdutoFilter();
			
			numeroVenda = null;
		
			PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Devolução realizada com sucesso!' });");
			
		} catch(Exception e) {
			e.printStackTrace();
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Algo saiu errado, entre em contato com o suporte!' });");
		}
	}


	public void devolucaoTrocaSelecionada() {
				
		try {
			
			if(!trocaPendente) {
				
				for (ItemVenda itemVenda : listaDevolucao) {
					devolverProdutoSelecionado("T", false, itemVenda);
				}
				
				
				
				Caixa caixa = caixas.porUsuario(usuario_, usuario_.getEmpresa());
				
				if(caixa != null) {
					
					ItemCaixa itemCaixa = new ItemCaixa();
					itemCaixa.setCaixa(caixa);
					itemCaixa.setData(new Date());
					itemCaixa.setDescricao("Devolução/Troca");
					itemCaixa.setOperacao(TipoOperacao.DEVOLUCAO);
					itemCaixa.setCodigoOperacao(devolucao.getId());
					
					itemCaixa.setTipoPagamento("Saída");
					itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario_.getEmpresa()));
					
					Double saldo = 0D;
					
					for (ItemVenda itemVendaTmp : listaDevolucao) {
						saldo += itemVendaTmp.getTotal().doubleValue();
					}
					
					itemCaixa.setValor(new BigDecimal(saldo));
					
					itensCaixas.save(itemCaixa);
				}
							
				
				itensVendasFiltradas = new ArrayList<ItemVenda>();
				itensParaDevolucao = 0L;
				
				produto = null;
				produto_ = null;
				
				filter = new ProdutoFilter();
				
				totalItens = 0L;
				totalVendas = "0,00";
				
				numeroVenda = null;
			
				PrimeFaces.current().executeScript(
					"devolucaoRealizada();");
				
			} else {
				
				PrimeFaces.current().executeScript(
						"trocarItens();");
			}
						
		} catch(Exception e) {
			e.printStackTrace();
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Algo saiu errado, entre em contato com o suporte!' });");
		}
	}
	
	
	public void devolverProdutoSelecionado(String tipo, boolean status, ItemVenda itemVenda_) {
				
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		
		List<ItemVendaCompra> itensVendasComprasFiltradasPorProduto = new ArrayList<ItemVendaCompra>();	
		List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda_);
		itensVendasComprasFiltradasPorProduto.addAll(itensVendaCompra);
		
		System.out.println("ItemVendaSelecionada: " + itemVenda_.getVenda().getId());
				
		System.out.println("Qtd devolvida: " + itemVenda_.getQuantidade());
		
		devolucao.setDataDevolucao(new Date());
		devolucao.setProduto(itemVenda_.getProduto());
		devolucao.setUsuario(usuario_);
		devolucao.setEmpresa(usuario_.getEmpresa());
		devolucao.setTipo(tipo);
		devolucao.setStatus(status);
		devolucao.setQuantidade(new BigDecimal(itensParaDevolucao));
		
		devolucao = devolucoes.save(devolucao);
		
		Double saldo = 0D;
		
		if(!itemVenda_.getProduto().isEstoque() || itensVendasComprasFiltradasPorProduto.size() == 0) {
			
			ItemVenda itemVenda__ = itensVendas.porId(itemVenda_.getId());
			saldo = itemVenda_.getQuantidade().doubleValue();
			
			ItemDevolucao itemDevolucao = new ItemDevolucao();
			itemDevolucao.setDevolucao(devolucao);
			itemDevolucao.setVenda(itemVenda__.getVenda());
			itemDevolucao.setProduto(itemVenda_.getProduto());
			itemDevolucao.setQuantidade(new BigDecimal(saldo));
			itemDevolucao.setValorTotal(new BigDecimal(saldo.doubleValue() * itemVenda__.getValorUnitario().doubleValue()));
			
			//devolucoesitemVenda.add(devolucaoItemVenda);
			itensDevolucoes.save(itemDevolucao);
			
			
			
			
			
										
				
			ItemVenda itemVenda = itensVendas.porId(itemVenda__.getId());
			
			Double valorDeCustoUnitario = itemVenda.getValorCompra().doubleValue() / itemVenda.getQuantidade().doubleValue();
			
			double desconto = 0;				
			if(itemVenda.getDesconto() != null) {
				desconto = itemVenda.getDesconto().doubleValue() / 100;
			}
			
			BigDecimal subtotal = BigDecimal.valueOf(
					itemVenda.getValorUnitario().doubleValue() * saldo.doubleValue());	
			
			itemVenda.setQuantidade(new BigDecimal(itemVenda.getQuantidade().doubleValue() - saldo.doubleValue()));
			itemVenda.setTotal(new BigDecimal(itemVenda.getTotal().doubleValue() - (subtotal.doubleValue() - (subtotal.doubleValue() * desconto))));
			
			Double lucro_ = itemVenda.getLucro().doubleValue();									
			
			itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
					* (itemVenda.getValorUnitario().doubleValue() * saldo.doubleValue())
					- (itemVenda.getValorUnitario().doubleValue() * saldo.doubleValue()) * desconto));

			itemVenda.setLucro(new BigDecimal(lucro_.doubleValue() - itemVenda.getLucro().doubleValue()));
			
			itemVenda.setPercentualLucro(BigDecimal.ZERO);
			if(itemVenda.getTotal().doubleValue() > 0) {
				itemVenda.setPercentualLucro(new BigDecimal((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100));
			}
			
			itemVenda.setValorCompra(new BigDecimal(itemVenda.getValorCompra().doubleValue() - (valorDeCustoUnitario.doubleValue() * saldo.doubleValue())));
							
			//itensVendas.save(itemVenda);
			
			
			
			Venda venda = vendas.porId(itemVenda.getVenda().getId());
			
			if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {
				venda.setQuantidadeItens(venda.getQuantidadeItens() - saldo.longValue());
			} else {
				if(saldo.doubleValue() <= 0) {
					venda.setQuantidadeItens(venda.getQuantidadeItens() - 1);
				}
			}
						
			
			BigDecimal totalDesconto = BigDecimal.ZERO;
			if(itemVenda.getDesconto() != null) {
				totalDesconto = new BigDecimal((itemVenda.getValorUnitario().doubleValue() * saldo.doubleValue() * itemVenda.getDesconto().doubleValue()) / 100);
			}
			
			venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));
			
			venda.setValorTotal(BigDecimal
					.valueOf(venda.getValorTotal().doubleValue() - (subtotal.doubleValue() - (subtotal.doubleValue() * desconto))));

			
			Long totalDeItens = 0L;
			double valorTotal = 0;
			double lucro = 0;
			double valorCompra = 0;
			
			BigDecimal totalSemDesconto = BigDecimal.ZERO;
			List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
			for (ItemVenda itemVendaTemp : itensVenda) {
				totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVendaTemp.getQuantidade().doubleValue() * itemVendaTemp.getValorUnitario().doubleValue()));
			
				
				if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {
					totalDeItens += itemVendaTemp.getQuantidade().longValue();
				} else {
					totalDeItens += 1;
				}
				
				valorTotal += itemVendaTemp.getTotal().doubleValue();
				valorCompra += itemVendaTemp.getValorCompra().doubleValue();

				lucro += itemVendaTemp.getLucro().doubleValue();
			}
			
			Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
			if(totalDescontoEmDinheiro > 0) {
				venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
			} else {
				venda.setTotalDesconto(BigDecimal.ZERO);
			}
			
			
			venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			
			
			

			BigDecimal totalDeAcrescimo = new BigDecimal(valorTotal * (venda.getAcrescimo().doubleValue()/100));																
			venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + venda.getTaxaDeEntrega().doubleValue()));
			
			
			venda.setQuantidadeItens(totalDeItens);
			
			if(venda.getDesconto() != null) {
				venda.setLucro(BigDecimal.valueOf(lucro - venda.getDesconto().doubleValue()));				
			} else {
				venda.setLucro(BigDecimal.valueOf(lucro));
			}			
			
			venda.setPercentualLucro(BigDecimal.ZERO);
			if(venda.getValorTotal().doubleValue() > 0) {
				venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));							
			}
			
			venda = vendas.save(venda);
			
			
			

			if(itemVenda.getQuantidade().doubleValue() > 0L) {
				
				itensVendas.save(itemVenda);

			} else {
				
				itensVendas.remove(itemVenda);
				
			}		
												
			
			saldo = 0D;
			
		} else {
			
			//saldo = devolucao.getQuantidade().doubleValue();
			saldo = itemVenda_.getQuantidade().doubleValue();
			
			for (ItemVendaCompra itemVendaCompra : itensVendasComprasFiltradasPorProduto) {
				
				System.out.println("Saldo: " + saldo);
				
				if(saldo > 0L) {
					if(saldo.longValue() <= itemVendaCompra.getQuantidade().longValue()) {
						
						ItemDevolucao itemDevolucao = new ItemDevolucao();
						itemDevolucao.setDevolucao(devolucao);
						itemDevolucao.setVenda(itemVendaCompra.getItemVenda().getVenda());
						itemDevolucao.setProduto(itemVenda_.getProduto());
						itemDevolucao.setQuantidade(new BigDecimal(saldo));
						itemDevolucao.setValorTotal(new BigDecimal(saldo.doubleValue() * itemVendaCompra.getItemVenda().getValorUnitario().doubleValue()));
						
						//devolucoesitemVenda.add(devolucaoItemVenda);
						itensDevolucoes.save(itemDevolucao);
						
						
						
						
						ItemCompra itemCompra = itensCompras.porCompra(itemVendaCompra.getCompra(), itemVenda_.getProduto());
						
						itemCompra.setQuantidadeDisponivel(new BigDecimal(
								itemCompra.getQuantidadeDisponivel().doubleValue() + saldo.doubleValue()));	
						itensCompras.save(itemCompra);
													
							
						ItemVenda itemVenda = itensVendas.porId(itemVendaCompra.getItemVenda().getId());
						
						Double valorDeCustoUnitario = itemVenda.getValorCompra().doubleValue() / itemVenda.getQuantidade().doubleValue();
						
						double desconto = 0;				
						if(itemVenda.getDesconto() != null) {
							desconto = itemVenda.getDesconto().doubleValue() / 100;
						}
						
						BigDecimal subtotal = BigDecimal.valueOf(
								itemVenda.getValorUnitario().doubleValue() * saldo.longValue());	
						
						itemVenda.setQuantidade(new BigDecimal(itemVenda.getQuantidade().doubleValue() - saldo.doubleValue()));
						itemVenda.setTotal(new BigDecimal(itemVenda.getTotal().doubleValue() - (subtotal.doubleValue() - (subtotal.doubleValue() * desconto))));
						
						Double lucro_ = itemVenda.getLucro().doubleValue();									
						
						itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
								* (itemVenda.getValorUnitario().doubleValue() * saldo.doubleValue())
								- (itemVenda.getValorUnitario().doubleValue() * saldo.doubleValue()) * desconto));
	
						itemVenda.setLucro(new BigDecimal(lucro_.doubleValue() - itemVenda.getLucro().doubleValue()));
						
						itemVenda.setPercentualLucro(BigDecimal.ZERO);
						if(itemVenda.getTotal().doubleValue() > 0) {
							itemVenda.setPercentualLucro(new BigDecimal((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * saldo.doubleValue())) / itemVenda.getTotal().doubleValue() * 100));
						}
						
						itemVenda.setValorCompra(new BigDecimal(itemVenda.getValorCompra().doubleValue() - (valorDeCustoUnitario.doubleValue() * saldo.doubleValue())));
										
						//itensVendas.save(itemVenda);
						
						
						
						Venda venda = vendas.porId(itemVenda.getVenda().getId());
						
						venda.setQuantidadeItens(venda.getQuantidadeItens() - saldo.longValue());			
						
						BigDecimal totalDesconto = BigDecimal.ZERO;
						if(itemVenda.getDesconto() != null) {
							totalDesconto = new BigDecimal((itemVenda.getValorUnitario().doubleValue() * saldo.longValue() * itemVenda.getDesconto().doubleValue()) / 100);
						}
						
						venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));
						
						venda.setValorTotal(BigDecimal
								.valueOf(venda.getValorTotal().doubleValue() - (subtotal.doubleValue() - (subtotal.doubleValue() * desconto))));
	
						
						Long totalDeItens = 0L;
						double valorTotal = 0;
						double lucro = 0;
						double valorCompra = 0;
						
						BigDecimal totalSemDesconto = BigDecimal.ZERO;
						List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
						for (ItemVenda itemVendaTemp : itensVenda) {
							totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVendaTemp.getQuantidade().doubleValue() * itemVendaTemp.getValorUnitario().doubleValue()));
						
							
							if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {
								totalDeItens += itemVendaTemp.getQuantidade().longValue();	
							} else {
								totalDeItens += 1;
							}
							
							valorTotal += itemVendaTemp.getTotal().doubleValue();
							valorCompra += itemVendaTemp.getValorCompra().doubleValue();
			
							lucro += itemVendaTemp.getLucro().doubleValue();
						}
						
						Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
						if(totalDescontoEmDinheiro > 0) {
							venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
						} else {
							venda.setTotalDesconto(BigDecimal.ZERO);
						}
						
						
						venda.setValorCompra(BigDecimal.valueOf(valorCompra));
						//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
						
						
						
	
						BigDecimal totalDeAcrescimo = new BigDecimal(valorTotal * (venda.getAcrescimo().doubleValue()/100));																
						venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + venda.getTaxaDeEntrega().doubleValue()));
						
						
						venda.setQuantidadeItens(totalDeItens);
						
						if(venda.getDesconto() != null) {
							venda.setLucro(BigDecimal.valueOf(lucro - venda.getDesconto().doubleValue()));				
						} else {
							venda.setLucro(BigDecimal.valueOf(lucro));
						}
						
						venda.setPercentualLucro(BigDecimal.ZERO);
						if(venda.getValorTotal().doubleValue() > 0) {
							venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));							
						}
						
						venda = vendas.save(venda);
						
						
						
						
						
	
						
						itemVendaCompra.setQuantidade(new BigDecimal(itemVendaCompra.getQuantidade().doubleValue() - saldo.doubleValue()));
						if(itemVendaCompra.getQuantidade().doubleValue() > 0) {
							
							itensVendasCompras.save(itemVendaCompra);
			
						} else {
							/* Atualiza ItemVenda e Venda */
							itensVendasCompras.remove(itemVendaCompra);
							
						}
						
	
						if(itemVenda.getQuantidade().doubleValue() > 0) {
							
							itensVendas.save(itemVenda);
			
						} else {
							
							itensVendas.remove(itemVenda);
							
						}		
						
						
						
						Produto produto = produtos.porId(itemVenda.getProduto().getId());
						produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + saldo.doubleValue()));
						
						/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (valorDeCustoUnitario.doubleValue() * saldo.doubleValue())));											
						
						Object[] result = itensCompras.porQuantidadeDisponivel(produto);
						
						if(((BigDecimal) result[0]) != null) {
						
							Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
							
							//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
							
							produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
							
							produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
							
							produto.setCustoTotal((BigDecimal) result[1]);	
															
							venda.setEstorno(new BigDecimal(venda.getEstorno().doubleValue() + estorno));
							vendas.save(venda);
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
						
						produtos.save(produto);
																
						
						saldo = 0D; 
						
					} else {
						
						saldo -= itemVendaCompra.getQuantidade().doubleValue();
						
						ItemDevolucao itemDevolucao = new ItemDevolucao();
						itemDevolucao.setDevolucao(devolucao);
						itemDevolucao.setVenda(itemVendaCompra.getItemVenda().getVenda());
						itemDevolucao.setProduto(itemVenda_.getProduto());
						itemDevolucao.setQuantidade(itemVendaCompra.getQuantidade());
						itemDevolucao.setValorTotal(new BigDecimal(itemVendaCompra.getQuantidade().doubleValue() * itemVendaCompra.getItemVenda().getValorUnitario().doubleValue()));
						
						//devolucoesitemVenda.add(devolucaoItemVenda);
						itensDevolucoes.save(itemDevolucao);
						
						
						
						
						ItemCompra itemCompra = itensCompras.porCompra(itemVendaCompra.getCompra(), itemVenda_.getProduto());
						
						itemCompra.setQuantidadeDisponivel(new BigDecimal(
								itemCompra.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));	
						itensCompras.save(itemCompra);
													
							
						ItemVenda itemVenda = itensVendas.porId(itemVendaCompra.getItemVenda().getId());
						
						Double valorDeCustoUnitario = itemVenda.getValorCompra().doubleValue() / itemVenda.getQuantidade().doubleValue();
						
						double desconto = 0;				
						if(itemVenda.getDesconto() != null) {
							desconto = itemVenda.getDesconto().doubleValue() / 100;
						}
						
						BigDecimal subtotal = BigDecimal.valueOf(
								itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().doubleValue());	
						
						itemVenda.setQuantidade(new BigDecimal(itemVenda.getQuantidade().doubleValue() - itemVendaCompra.getQuantidade().doubleValue()));
						itemVenda.setTotal(new BigDecimal(itemVenda.getTotal().doubleValue() - (subtotal.doubleValue() - (subtotal.doubleValue() * desconto))));
						
						Double lucro_ = itemVenda.getLucro().doubleValue();
						
						itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
								* (itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().doubleValue())
								- (itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().doubleValue()) * desconto));
				
						itemVenda.setLucro(new BigDecimal(lucro_.doubleValue() - itemVenda.getLucro().doubleValue()));
								
						itemVenda.setPercentualLucro(BigDecimal.ZERO);
						if(itemVenda.getTotal().doubleValue() > 0) {
							itemVenda.setPercentualLucro(new BigDecimal(((valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().doubleValue()) / itemVenda.getTotal().doubleValue() * 100)));
						}
						
						itemVenda.setValorCompra(new BigDecimal(itemVenda.getValorCompra().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().doubleValue())));
						
						//itemVenda.setValorCompra(new BigDecimal(itemVenda.getValorCompra().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().longValue())));
										
						//itensVendas.save(itemVenda);
						
						
						
						Venda venda = vendas.porId(itemVenda.getVenda().getId());
						
						
						//venda.setQuantidadeItens(venda.getQuantidadeItens() - itemVendaCompra.getQuantidade().longValue());	
						if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {	
							venda.setQuantidadeItens(venda.getQuantidadeItens() - itemVendaCompra.getQuantidade().longValue());
						} else {
							if(itemVendaCompra.getQuantidade().doubleValue() <= 0) {
								venda.setQuantidadeItens(venda.getQuantidadeItens() - 1);
							}
						}
						
						
						
						BigDecimal totalDesconto = BigDecimal.ZERO;
						if(itemVenda.getDesconto() != null) {
							totalDesconto = new BigDecimal((itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().doubleValue() * itemVenda.getDesconto().doubleValue()) / 100);
						}
						
						venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));
						
						venda.setValorTotal(BigDecimal
								.valueOf(venda.getValorTotal().doubleValue() - (subtotal.doubleValue() - (subtotal.doubleValue() * desconto))));
	
						
						Long totalDeItens = 0L;
						double valorTotal = 0;
						double lucro = 0;
						double valorCompra = 0;
						
						BigDecimal totalSemDesconto = BigDecimal.ZERO;
						List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
						for (ItemVenda itemVendaTemp : itensVenda) {
							totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVendaTemp.getQuantidade().doubleValue() * itemVendaTemp.getValorUnitario().doubleValue()));
						
							
							if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {
								totalDeItens += itemVendaTemp.getQuantidade().longValue();	
							} else {
								totalDeItens += 1;
							}
							
							
							valorTotal += itemVendaTemp.getTotal().doubleValue();
							valorCompra += itemVendaTemp.getValorCompra().doubleValue();
			
							lucro += itemVendaTemp.getLucro().doubleValue();
						}
						
						Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
						if(totalDescontoEmDinheiro > 0) {
							venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
						} else {
							venda.setTotalDesconto(BigDecimal.ZERO);
						}
						
						
						venda.setValorCompra(BigDecimal.valueOf(valorCompra));
						
						//venda.setValorTotal(BigDecimal.valueOf(valorTotal));							
						BigDecimal totalDeAcrescimo = new BigDecimal(valorTotal * (venda.getAcrescimo().doubleValue()/100));																
						venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + venda.getTaxaDeEntrega().doubleValue()));
						
						venda.setQuantidadeItens(totalDeItens);
						
						if(venda.getDesconto() != null) {
							venda.setLucro(BigDecimal.valueOf(lucro - venda.getDesconto().doubleValue()));				
						} else {
							venda.setLucro(BigDecimal.valueOf(lucro));
						}
						
						venda.setPercentualLucro(BigDecimal.ZERO);
						if(venda.getValorTotal().doubleValue() > 0) {
							venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));							
						}
						
						venda = vendas.save(venda);
						
	
						Double quantidadeProduto = itemVendaCompra.getQuantidade().doubleValue();
						
						itemVendaCompra.setQuantidade(BigDecimal.ZERO);
						//itemVendaCompra.setQuantidade(itemVendaCompra.getQuantidade().longValue() - itemVendaCompra.getQuantidade().longValue());
						if(itemVendaCompra.getQuantidade().doubleValue() > 0D) {
							
							itensVendasCompras.save(itemVendaCompra);
			
						} else {
							/* Atualiza ItemVenda e Venda */
							itensVendasCompras.remove(itemVendaCompra);
							
						}
						
	
						if(itemVenda.getQuantidade().doubleValue() > 0D) {
							
							itensVendas.save(itemVenda);
			
						} else {
							
							itensVendas.remove(itemVenda);
							
						}		
						
						
						Produto produto = produtos.porId(itemVenda.getProduto().getId());
						produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + quantidadeProduto.doubleValue()));
						
						/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (valorDeCustoUnitario.doubleValue() * quantidadeProduto.doubleValue())));											
						
						Object[] result = itensCompras.porQuantidadeDisponivel(produto);
						
						if(((BigDecimal) result[0]) != null) {
						
							Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
							
							//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
							
							produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
							
							produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
							
							produto.setCustoTotal((BigDecimal) result[1]);	
							
							venda.setEstorno(new BigDecimal(venda.getEstorno().doubleValue() + estorno));
							vendas.save(venda);
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
						
						produtos.save(produto);
						
					}
				}
			}
		}
			
			
		System.out.println("Saldo: " + saldo);
				
		
	}
	
	
	
	

	public void prepararEntrega() {
		entrega = entregas.porVenda(vendaSelecionada);
	}

	public void verLocalizacao() {
		if (entrega.getLocalizacao() != null && !entrega.getLocalizacao().trim().equals("")) {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect("https://maps.google.com/maps?daddr=" + entrega.getLocalizacao());
			} catch (IOException e) {

			}
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe informação de localização de entrega para esta venda!' });");
		}
	}

	public void emitirPedido() {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		EspelhoVenda pedido = new EspelhoVenda();
		pedido.setVendaNum(vendaSelecionada.getNumeroVenda() + "");
		pedido.setTipoVenda(vendaSelecionada.getTipoVenda().getDescricao().toUpperCase());
		pedido.setBairro(vendaSelecionada.getBairro().getNome().toUpperCase());
		pedido.setDataVenda(sdf.format(vendaSelecionada.getDataVenda()));
		pedido.setVendedor(vendaSelecionada.getUsuario().getNome().toUpperCase());

		Entrega entrega = entregas.porVenda(vendaSelecionada);
		if (entrega != null) {
			pedido.setResponsavel(entrega.getNome());
			pedido.setLocalizacao(entrega.getLocalizacao());
			pedido.setObservacao(entrega.getObservacao());
		}
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemEspelhoVendaProdutos itemPedido = new ItemEspelhoVendaProdutos();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			itemPedido.setDescricao(itemVenda.getProduto().getDescricao());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().intValue()));
			itemPedido.setQuantidade(String.valueOf(itemVenda.getQuantidade()));
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}

		pedido.setTotalVenda(nf.format(vendaSelecionada.getValorTotal()));

		List<EspelhoVenda> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
		try {
			report.getRelatorio(pedidos, "Venda-N" + vendaSelecionada.getNumeroVenda().longValue());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public Venda getVendaSelecionada() {
		return vendaSelecionada;
	}

	public void setVendaSelecionada(Venda vendaSelecionada) {
		this.vendaSelecionada = vendaSelecionada;
	}

	public List<Venda> getVendasFiltradas() {
		return vendasFiltradas;
	}

	public void setVendasFiltradas(List<Venda> vendasFiltradas) {
		this.vendasFiltradas = vendasFiltradas;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public int getVendasFiltradasSize() {
		return vendasFiltradas.size();
	}

	public String getTotalVendas() {
		return totalVendas;
	}

	public Long getTotalItens() {
		return totalItens;
	}

	public Long getNumeroVenda() {
		return numeroVenda;
	}

	public void setNumeroVenda(Long numeroVenda) {
		this.numeroVenda = numeroVenda;
	}

	public boolean isVendasNormais() {
		return vendasNormais;
	}

	public void setVendasNormais(boolean vendasNormais) {
		this.vendasNormais = vendasNormais;
	}

	public StatusPedido[] getStatusPedidos() {
		return StatusPedido.values();
	}

	public StatusPedido[] getStatusPedido() {
		return statusPedido;
	}

	public void setStatusPedido(StatusPedido[] statusPedido) {
		this.statusPedido = statusPedido;
	}

	public Entrega getEntrega() {
		return entrega;
	}

	public void setEntrega(Entrega entrega) {
		this.entrega = entrega;
	}


	public ProdutoFilter getFilter() {
		return filter;
	}


	public void setFilter(ProdutoFilter filter) {
		this.filter = filter;
	}


	public boolean isLeitor() {
		return leitor;
	}


	public void setLeitor(boolean leitor) {
		this.leitor = leitor;
	}


	public Produto getProduto() {
		return produto;
	}


	public void setProduto(Produto produto) {
		this.produto = produto;
	}


	public Devolucao getDevolucao() {
		return devolucao;
	}


	public void setDevolucao(Devolucao devolucao) {
		this.devolucao = devolucao;
	}


	public List<ItemVenda> getItensVendasFiltradas() {
		return itensVendasFiltradas;
	}


	public ItemVenda getItemVendaSelecionada() {
		return itemVendaSelecionada;
	}


	public void setItemVendaSelecionada(ItemVenda itemVendaSelecionada) {
		this.itemVendaSelecionada = itemVendaSelecionada;
	}


	public List<ItemVenda> getListaDevolucao() {
		return listaDevolucao;
	}
	
	public Integer getQuantidadeItens() {
		int quantidadeItens = 0;
		
		for (ItemVenda itemVenda : listaDevolucao) {
			quantidadeItens += itemVenda.getQuantidade().longValue();
		}
		
		return quantidadeItens;
	}


	public String getSaldoParaTroca() {
		return saldoParaTroca;
	}


	public Long getItensParaDevolucao() {
		return itensParaDevolucao.longValue();
	}
	
	public Integer getListaDevolucaoSize() {
		return listaDevolucao.size();
	}


	public Produto getProduto_() {
		return produto_;
	}


	public void setProduto_(Produto produto_) {
		this.produto_ = produto_;
	}


	public boolean isTrocaPendente() {
		return trocaPendente;
	}
	
	public String getImageContentsAsBase64() {
		return Base64.getEncoder().encodeToString(produto.getFoto());
	}

}
