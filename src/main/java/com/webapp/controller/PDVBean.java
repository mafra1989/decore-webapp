package com.webapp.controller;

import java.io.ByteArrayInputStream;
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

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JOptionPane;
import javax.validation.constraints.NotNull;

import org.apache.poi.hssf.record.formula.functions.Subtotal;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Bairro;
import com.webapp.model.Caixa;
import com.webapp.model.Conta;
import com.webapp.model.Devolucao;
import com.webapp.model.Entrega;
import com.webapp.model.EspelhoVenda;
import com.webapp.model.FormaPagamento;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCaixa;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.ItemEspelhoVenda;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.PeriodoPagamento;
import com.webapp.model.Produto;
import com.webapp.model.TipoOperacao;
import com.webapp.model.TipoPagamento;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Bairros;
import com.webapp.repository.Caixas;
import com.webapp.repository.Contas;
import com.webapp.repository.Devolucoes;
import com.webapp.repository.Entregas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
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
	private Contas contas;

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
	
	private List<ItemVenda> itensVendaRemovidos = new ArrayList<ItemVenda>();

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
	
	private boolean leitor = true;
	
	private Integer activeIndex = 0;
	
	private boolean edit;
	
	@Inject
	private FormaPagamento formaPagamento;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	private List<FormaPagamento> todasFormasPagamentos = new ArrayList<FormaPagamento>();
	
	private TipoPagamento tipoPagamento = TipoPagamento.AVISTA;

	@NotNull
	private Long parcelas = 2L;

	private PeriodoPagamento periodoPagamento = PeriodoPagamento.MESES;

	private boolean contaAPagar;
	
	private Double valorEntrada;
	
	private Double valorRecebido;
	
	private Double troco;
	
	private Double faltando;
	
	private List<Conta> todasContas = new ArrayList<>();
	
	private List<Conta> entradas = new ArrayList<>();
	
	private boolean parcelasConfirmadas;
	
	private BigDecimal totalDeAcrescimo = BigDecimal.ZERO;
	
	private boolean vendaPaga = true;
	
	private Date primeiraParcela;
	
	private String primeiraParcelaEmString;
	
	
	private Date pagamentoPara;
	
	private String pagamentoParaEmString;
	
	@Inject
	private ItensDevolucoes itensDevolucoes;
	
	@Inject
	private Devolucoes devolucoes;
	
	private Double saldoParaTroca = 0D;
	
	private String saldoParaTrocaEmString = "0,00";
	
	private boolean trocaPendente = false;
	
	
	@Inject
	private Caixas caixas;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	private boolean caixaAberto = false;
	

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
			
			Caixa caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
			if(caixa != null) {
				caixaAberto = true;
			}
			
			todosUsuarios = usuarios.todos(usuario.getEmpresa());
			todosTiposVendas = tiposVendas.todos();
			todosBairros = bairros.todos();
			todasFormasPagamentos = formasPagamentos.todos();
			
			venda.setTipoVenda(tiposVendas.porId(33L));
			venda.setBairro(bairros.porId(3008L));
			venda.setUsuario(usuario);
			
			FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro");
			venda.setFormaPagamento(formaPagamento);
			
			itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
			
			
			Object[] saldo = itensDevolucoes.saldoParaTroca(usuario, usuario.getEmpresa());
			
			if(saldo[0] != null) {
				saldoParaTroca = ((BigDecimal)saldo[1]).doubleValue();
				saldoParaTrocaEmString = nf.format(((BigDecimal)saldo[1]).doubleValue());			
				trocaPendente = true;
			}
		}
	}

	public void pesquisar() {
		activeIndex = 0;
		System.out.println("Código escaneado: " + filter.getCodigo());
		
		if(caixaAberto) {
			
			Produto produto = produtos.porCodigoDeBarras(filter.getCodigo(), usuario.getEmpresa());	
			
			if(produto != null) {
				filter = new ProdutoFilter();
				selecionarProduto(produto);
				
			} else {
				filter = new ProdutoFilter();
				itemVenda = new ItemVenda();
				itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
				PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto não encontrado!', timer: 1500 });");
			}
		
		} else {
			filter = new ProdutoFilter();
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Caixa Fechado!', text: 'Para usar o PDV, primeiro você deve abrir o caixa!', timer: 5000 });");
			
		}
	
	}
	
	public void atualizaValores() {
		System.out.println(itemVenda.getProduto().getMargemLucro());
		
	}
	
	public void calculaAcrescimo() {
		
		Double subtotal = 0D;
		
		for (ItemVenda itemVenda : itensVenda) {			
			subtotal += itemVenda.getTotal().doubleValue();
		}
		
		BigDecimal taxaDeEntrega = (venda.getBairro() != null && entrega) ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;
		
		subtotal += taxaDeEntrega.doubleValue();
		
		totalDeAcrescimo = new BigDecimal(subtotal.doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100));
		
		System.out.println(subtotal + " - " + venda.getFormaPagamento().getAcrescimo());
		
		venda.setValorTotal(new BigDecimal(subtotal.doubleValue() + totalDeAcrescimo.doubleValue()));
	
		venda.setAcrescimo(venda.getFormaPagamento().getAcrescimo());
		
		venda.setTaxaDeEntrega(taxaDeEntrega);
		
		if(!trocaPendente) {
			
			faltando = venda.getValorTotal().doubleValue();
			
		}
		
		
		changeFormaPagamento();
	}
	
	public void novaVenda() {
		
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
		
		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda cancelada com sucesso!', timer: 1500 });");
		
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
		if(caixaAberto) {
			
	        System.out.println(event.getObject().toString());
	        selecionarProduto((Produto) event.getObject());
	        produto = new Produto();
	        
		} else {
			produto = new Produto();
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Caixa Fechado!', text: 'Para usar o PDV, primeiro você deve abrir o caixa!', timer: 5000 });");		
		}
    }

	public void buscar() {
		venda = vendas.porId(venda.getId());
		itensVenda = itensVendas.porVenda(venda);

		Double subtotal = 0D;
		for (ItemVenda itemVenda : itensVenda) {
			
			List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
			itemVenda.setItensVendaCompra(itensVendaCompra);
			
			subtotal += itemVenda.getTotal().doubleValue();
			
			try {
				Thread.sleep(100);
				itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
			} catch (InterruptedException e) {
			}
		}
		
		saldoParaTroca = venda.getSaldoParaTroca().doubleValue();
		saldoParaTrocaEmString = nf.format(saldoParaTroca);
				
		
		totalDeAcrescimo = new BigDecimal(subtotal.doubleValue() * (venda.getAcrescimo().doubleValue()/100));
		venda.setAcrescimo(totalDeAcrescimo);
		
		entregaVenda = entregas.porVenda(venda);
		entrega = entregaVenda.getId() != null;
		
		valorRecebido = venda.getValorRecebido().doubleValue();
		faltando = venda.getFaltando().doubleValue();
		troco = venda.getTroco().doubleValue();
		
		calculaTroco();
		
		
		tipoPagamento = venda.getTipoPagamento();
		//parcelasConfirmadas = false;
		//valorRecebido = null;
		//troco = 0D;
		vendaPaga = venda.isVendaPaga();
		//faltando = venda.getValorTotal().doubleValue();
		
		//if(venda.getTipoPagamento() == TipoPagamento.PARCELADO) {
			todasContas = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario.getEmpresa());
			
			//entradas = new ArrayList<>();
			//valorEntrada = null;
		//}
		
		boolean primeiraParcela = false;
		for (Conta conta : todasContas) {
			
			if(conta.getParcela().equals("Entrada")) {
				valorEntrada = conta.getValor().doubleValue();
			}
			
			if(!primeiraParcela) {
				this.primeiraParcela = conta.getVencimento();
				primeiraParcelaEmString = sdf.format(this.primeiraParcela);
				primeiraParcela = true;
			}
			
			if(conta.getParcela().equals("AVISTA")) {
				pagamentoPara = conta.getVencimento();
				pagamentoParaEmString = sdf.format(pagamentoPara);
			}
		}
		
		contaAPagar = true;
		
		parcelasConfirmadas = true;
		
		activeIndex = 1;
		
		edit = true;
	}
	
	public void ativarLeitor() {
		activeIndex = 0;
	}
	
	
	public void finalizar() {
		
		activeIndex = 1; 
		
		if (itensVenda.size() > 0) {			
									
			if(venda.isAjuste()) {
				/*
				Long totalDeItens = 0L;
				double valorTotal = 0;
				
				if (compra.getId() != null) {
					List<Conta> contasTemp = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA", usuario.getEmpresa());
					for (Conta conta : contasTemp) {
						contas.remove(conta);
					}
				}
				
				Calendar calendario = Calendar.getInstance();
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(compra.getDataCompra());
				calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
				calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
				calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
				compra.setDataCompra(calendarioTemp.getTime());

				boolean edit = false;
				if (compra.getId() != null) {
					edit = true;

					List<ItemCompra> itemCompraTemp = itensCompras.porCompra(compra);

					for (ItemCompra itemCompra : itemCompraTemp) {
						Produto produto = produtos.porId(itemCompra.getProduto().getId());
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
						produtos.save(produto);

						itensCompras.remove(itemCompra);
					}
				}

				compra.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				compra.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				compra.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				compra.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				compra.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				Compra compraTemp = compras.ultimoNCompra(usuario.getEmpresa());

				if (compraTemp == null) {
					compra.setNumeroCompra(1L);
				} else {
					if (compra.getId() == null) {
						compra.setNumeroCompra(compraTemp.getNumeroCompra() + 1);
					}
				}
				
				compra.setEmpresa(usuario.getEmpresa());
				compra = compras.save(compra);

				for (ItemCompra itemCompra : itensCompra) {
					itemCompra.setCompra(compra);
					itensCompras.save(itemCompra);

					Produto produto = produtos.porId(itemCompra.getProduto().getId());
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemCompra.getQuantidade());
					
					System.out.println("Custo médio Un.: " + produto.getCustoMedioUnitario().doubleValue());
					if(produto.getCustoMedioUnitario().doubleValue() > 0) {
						// Atualizar custo total 
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * produto.getCustoMedioUnitario().doubleValue())));
					} else {
						// Atualizar custo total e custo medio un. 
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * itemCompra.getValorUnitario().doubleValue())));					
						
						Long saldo =  produto.getQuantidadeAtual();//(Long) itensCompras.saldoPorProduto(produto);
						System.out.println(produto.getCustoTotal().doubleValue() + " / " + saldo.longValue());
						produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / saldo.longValue()));
					}
					
					produtos.save(produto);

					totalDeItens += itemCompra.getQuantidade();
					valorTotal += itemCompra.getTotal().doubleValue();
				}

				compra.setValorTotal(BigDecimal.valueOf(valorTotal));
				compra.setQuantidadeItens(totalDeItens);
				compra = compras.save(compra);
				
				if (!edit) {
					PrimeFaces.current().executeScript(
							"stop();PF('confirmDialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
									+ compra.getNumeroCompra() + " registrada com sucesso!' });");

					compra = new Compra();
					itensCompra = new ArrayList<ItemCompra>();
					itemCompra = new ItemCompra();
					itemSelecionado = null;
					todasContas = new ArrayList<>();
					tipoPagamento = TipoPagamento.AVISTA;
					parcelas = 2L;
					periodoPagamento = PeriodoPagamento.MESES;
					contaAPagar = false;
					compraPaga = true;
					
					entradas = new ArrayList<>();
					valorEntrada = null;

				} else {
					PrimeFaces.current().executeScript(
							"stop();PF('confirmDialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
									+ compra.getNumeroCompra() + " atualizada com sucesso!' });");
				}
				*/
			} else {
				
				if(venda.getId() == null) {
					tipoPagamento = TipoPagamento.AVISTA;
					parcelasConfirmadas = false;
					valorRecebido = null;
					troco = 0D;
					vendaPaga = true;
					
					if(!trocaPendente) {
						faltando = venda.getValorTotal().doubleValue();
					} else {
						faltando = venda.getValorTotal().doubleValue() - saldoParaTroca.doubleValue();
						
						if(faltando < 0) {
							troco = -1 * faltando.doubleValue();
							faltando = 0D;			
						}	
					}
					
					todasContas = new ArrayList<>();
					
					entradas = new ArrayList<>();
					valorEntrada = null;
					
					parcelasConfirmadas = true;
					
					PrimeFaces.current().executeScript("selectTab1();PF('confirmDialog').show();");
					
				} else {
					 
					Venda vendaTemp = vendas.porId(venda.getId());
					 if(!vendaTemp.getFormaPagamento().getNome().equals(venda.getFormaPagamento().getNome()) && !venda.getFormaPagamento().getNome().equals("Dinheiro")) {
						
						tipoPagamento = TipoPagamento.AVISTA;
						parcelasConfirmadas = false;
						valorRecebido = null;
						troco = 0D;
						vendaPaga = true;
						faltando = venda.getValorTotal().doubleValue();
						
						faltando = venda.getValorTotal().doubleValue() - saldoParaTroca.doubleValue();
						
						todasContas = new ArrayList<>();
						
						entradas = new ArrayList<>();
						valorEntrada = null;
						
						parcelasConfirmadas = true;
						
					 } else {
						 
						List<ItemDevolucao> listaDevolucao = itensDevolucoes.porVenda(venda);
						if(listaDevolucao.size() == 0) {
							
							PrimeFaces.current().executeScript("selectTab1();PF('confirmDialog').show();");
						
						} else {
							
							PrimeFaces.current().executeScript(
									"swal({ type: 'error', title: 'Ops!', text: 'Não é possível alterar esta venda, já existem devoluções registradas!' });");
						}
					 }
				}				
							
			}
		
		} else {
			
			activeIndex = 0;
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!', timer: 1500 });");
		}
		
	}
	

	public void salvar() {
		
		if (venda.getId() != null) {

			List<Conta> contasTemp = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario.getEmpresa());
			for (Conta conta : contasTemp) {
				contas.remove(conta);
			}

		}
		
		if(tipoPagamento == TipoPagamento.AVISTA && venda.getFormaPagamento().getNome().equals("Dinheiro")) {
		
			if(valorRecebido != null || trocaPendente) {
				
				if(entrega != true) {
					
					if(vendaPaga == true) {
						
						if(faltando.doubleValue() == 0D) {
							
							salvar_();
							
						} else {
								
							PrimeFaces.current().executeScript(
								"Toast.fire({ " +
								  "icon: 'error', " +
								  "title: 'Não foi possível salvar a venda. Está faltando R$ " + nf.format(faltando) + "!'" +
								"}) ");
						}	
						
					} else {
						
						/* Salva venda e gera conta a receber */
						System.out.println("Salva venda e gera conta a receber.");
						salvar_();
					}
					
					
				} else {				
						
					if(faltando.doubleValue() == 0D) {
						
						salvar_();
						
					} else {
							
						PrimeFaces.current().executeScript(
								"Toast.fire({ " +
								  "icon: 'error', " +
								  "title: 'Não foi possível salvar a venda. Está faltando R$ " + nf.format(faltando) + "!'" +
								"}) ");
					}		
					
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
			
			
		} else if(tipoPagamento == TipoPagamento.AVISTA) {
			
			salvar_();
			
		} else if(tipoPagamento == TipoPagamento.PARCELADO) {
			
			/* Salva venda e gera conta a receber. */
			System.out.println("Salva venda e gera conta a receber.");
			salvar_();
		}

	}
	
	
	public void salvar_() {
		
		List<ItemVenda> itensVenda_ = new ArrayList<ItemVenda>();
		
		List<ItemVendaCompra> itensVendaCompraDefault = new ArrayList<ItemVendaCompra>();
		
		if(venda.getId() != null) {
			
			List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
			
			itensVenda_.addAll(itensVenda);
			
			for (ItemVenda itemVenda : itensVenda) {
				
				
				boolean contains = false;
				for (ItemVenda itemVenda_ : this.itensVenda) {
					
					if(itemVenda_.getId() != null) {
						if(itemVenda_.getId().longValue() == itemVenda.getId().longValue()) {
							contains = true;
						}
					}				
				}
				
				
				if(!contains) {
								
					Produto produto = itemVenda.getProduto();
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemVenda.getQuantidade());
					
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
				
					List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompra : itensCompra) {
						
						if(venda.isPdv()) {
							
							List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							
							for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {	
								
								if (itemCompra.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId().longValue()) {
									if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
										
										System.out.println("Quantidade Disponivel: "+itemCompra.getQuantidadeDisponivel());
										System.out.println("Quantidade Retornada: "+itemVenda.getQuantidade());
										
										itemCompra.setQuantidadeDisponivel(
												itemCompra.getQuantidadeDisponivel() + itemVendaCompra.getQuantidade());
										itensCompras.save(itemCompra);
										
										
										
										/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
										produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));											
										
										Object[] result = itensCompras.porQuantidadeDisponivel(produto);
										
										if((Long) result[0] != null) {
										
											Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
											
											//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
											
											produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
											
											produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().longValue()));
											
											produto.setCustoTotal((BigDecimal) result[1]);	
										}
										
										produtos.save(produto);
										
									}
								}
							}	
	
							
						}
					}
					
					itensVendaCompraDefault = itensVendasCompras.porItemVenda(itemVenda);
					
					itemVenda.setItensVendaCompra(itensVendaCompraDefault);
					
					for(ItemVendaCompra itemVendaCompra : itensVendaCompraDefault) {
						itensVendasCompras.remove(itemVendaCompra);
					}
	
					itensVendas.remove(itemVenda);				
					
					//produtos.save(produto);
				}
			}
			
			
			Entrega entrega = entregas.porVenda(venda);
			if (entrega.getId() != null) {
				entregas.remove(entrega);
			}
			
			

			//vendas.remove(venda);
		}
		
		
		
		
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
		
		venda.setVendaPaga(vendaPaga);
		venda.setTipoPagamento(tipoPagamento);
		
		System.out.println(venda.getTipoPagamento());
		System.out.println();
		
		if(venda.getTipoPagamento() == TipoPagamento.AVISTA && venda.getFormaPagamento().getNome().equals("Dinheiro")) {
			
			if(vendaPaga) {
				venda.setValorRecebido(new BigDecimal(valorRecebido != null ? valorRecebido : 0));
				venda.setFaltando(new BigDecimal(faltando));
				venda.setTroco(new BigDecimal(troco));
			} else {
				if(!entrega) {
					venda.setValorRecebido(BigDecimal.ZERO);
					venda.setFaltando(venda.getValorTotal());
					venda.setTroco(BigDecimal.ZERO);
				} else {
					venda.setValorRecebido(new BigDecimal(valorRecebido != null ? valorRecebido : 0));
					venda.setFaltando(new BigDecimal(faltando));
					venda.setTroco(new BigDecimal(troco));
				}
			}
			
			
		} else {
			
			venda.setValorRecebido(BigDecimal.ZERO);
			venda.setFaltando(BigDecimal.ZERO);
			venda.setTroco(BigDecimal.ZERO);
		}
			
		venda.setTaxaDeEntrega(entrega ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO);
		
		if(!venda.isAjuste()) {
			venda.setRecuperarValores(false);
		}

		
		if (false) {
		//if (venda.getId() != null) {
			//edit = true;
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
				//produtos.save(produto);
								
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
		}
		
		
		if (tipoPagamento == TipoPagamento.PARCELADO) {
			venda.setConta(true);
			venda.setVendaPaga(false);
		} else {
			venda.setConta((vendaPaga != true && !entrega) ? true : false);
		}
		

		venda.setPdv(true);
		venda.setEmpresa(usuario.getEmpresa());
		venda = vendas.save(venda);
		
		List<ItemVenda> itensVendaTemp = new ArrayList<ItemVenda>();
		itensVendaTemp.addAll(itensVenda);

		for (ItemVenda itemVenda : itensVenda) {
			
			if(itemVenda.getId() == null) {
				
				if(itemVenda.getItensVendaCompra() == null) {
					
					for (ItemVenda itemVendaTemp : itensVenda_) {
						if(itemVenda.getId().longValue() == itemVendaTemp.getId().longValue()) {
							itemVenda.setItensVendaCompra(itemVendaTemp.getItensVendaCompra());
						}
					}			
				}
	
				List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();
				if(itemVenda.getId() == null) {
					itensVendaCompra = itemVenda.getItensVendaCompra();
				} else {
					itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
				}
				
				if(venda.getId() != null) {
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					Double valorDeCustoUnitario = produto.getCustoMedioUnitario().doubleValue();
					itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));					
				
					if(produto.getQuantidadeAtual().longValue() <= 0 && valorDeCustoUnitario.doubleValue() <= 0) {
						valorDeCustoUnitario = (itemVenda.getProduto().getQuantidadeAtual().longValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()) / itemVenda.getProduto().getQuantidadeAtual().longValue();
					}
					
					double desconto = 0;				
					if(itemVenda.getDesconto() != null) {
						desconto = itemVenda.getDesconto().doubleValue() / 100;
					} else {
						itemVenda.setDesconto(BigDecimal.ZERO);
					}
					
					BigDecimal subtotal = BigDecimal.valueOf(
						itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue());					
					itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * desconto)));

					/* Calculo do Lucro em valor e percentual */										
					Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
							* itemVenda.getQuantidade().intValue())
					.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
					
				
					itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
							* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().intValue())
							- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));

					itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade())) / itemVenda.getTotal().doubleValue() * 100)));
					itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));

				}
				
				
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
					//if(itemVenda.getLucro().doubleValue() >= 0) {
						
						//for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));					
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
					
					
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemVenda.getQuantidade());				
	
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
				
				
				if(produto.getQuantidadeAtual().longValue() <= 0) {
					produto.setCustoMedioUnitario(BigDecimal.ZERO);

				}
				
				
				produtos.save(produto);
				
			} else {
				
				totalDeItens += itemVenda.getQuantidade();
				valorTotal += itemVenda.getTotal().doubleValue();
				valorCompra += itemVenda.getValorCompra().doubleValue();
	
				lucro += itemVenda.getLucro().doubleValue();
			}
		}
			
			
		for (ItemVenda itemVenda : itensVenda) {
				
			if(itemVenda.getId() == null || itemVenda.isUpdate()) {
				for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
					itensVendasCompras.save(itemVendaCompra);
				}
			}
		}
		//}
		
		
		
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
				conta.setValor(venda.getValorTotal());
				
				conta.setSubTotal(new BigDecimal(valorTotal));
				conta.setCustoMedio(new BigDecimal(valorCompra));
				conta.setLucro(new BigDecimal(lucro));
				conta.setTaxaEntrega(venda.getTaxaDeEntrega());
				
				calendario = Calendar.getInstance();
				Calendar vencimento = Calendar.getInstance();
				vencimento.setTime(pagamentoPara);
				vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
				vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
				vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
				
				conta.setVencimento(vencimento.getTime());

				conta.setPagamento(null);
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(venda.getDataVenda());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				conta.setEmpresa(usuario.getEmpresa());
				contas.save(conta);
			}

		} else {
			
			/*for (Conta conta : entradas) {

				conta.setCodigoOperacao(venda.getNumeroVenda());
				conta.setOperacao("VENDA");
				conta.setTipo("CREDITO");
				conta.setStatus(true);

				conta.setEmpresa(usuario.getEmpresa());
				contas.save(conta);
			}*/
			
			for (Conta conta : todasContas) {
				conta.setCodigoOperacao(venda.getNumeroVenda());
				conta.setOperacao("VENDA");
				conta.setTipo("CREDITO");
				conta.setPagamento(null);
				
				conta.setEmpresa(usuario.getEmpresa());
				conta = contas.save(conta);
			}
		}
		

			
		//venda.setStatus(true); 
		//venda.setStatus(entrega);
			
		if (edit != true) {
			
			venda.setStatus(!entrega); 

			if (entrega) {
				entregaVenda.setStatus("PENDENTE");
				entregaVenda.setVenda(venda);
				entregaVenda = entregas.save(entregaVenda);
			}
			
			venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			BigDecimal taxaDeEntrega = (venda.getBairro() != null && entrega) ? venda.getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;
			venda.setValorTotal(new BigDecimal(valorTotal + totalDeAcrescimo.doubleValue() + taxaDeEntrega.doubleValue()));

		
			venda.setQuantidadeItens(totalDeItens);
			venda.setLucro(BigDecimal.valueOf(lucro));
			
			venda.setPercentualLucro(new BigDecimal(((venda.getValorTotal().doubleValue() - venda.getValorCompra().doubleValue())/venda.getValorTotal().doubleValue())*100));
			
			
			if(venda.getTipoPagamento() == TipoPagamento.AVISTA && venda.getFormaPagamento().getNome().equals("Dinheiro")) {
				
				if(trocaPendente && vendaPaga) {
				
					List<ItemDevolucao> listaDevolucao = itensDevolucoes.listaDevolucao();
					for (ItemDevolucao itemDevolucao : listaDevolucao) {				
						Devolucao devolucao = itemDevolucao.getDevolucao();
						devolucao.setStatus(true);
						devolucoes.save(devolucao);
					}
					
					venda.setSaldoParaTroca(new BigDecimal(saldoParaTroca));
					
					trocaPendente = false;
					saldoParaTroca = 0D;
					saldoParaTrocaEmString = "0,00";
				}
			}
			
			
			venda = vendas.save(venda);
			
			
			
			
			
			if(!venda.isConta()) {
				
				Caixa caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
				
				if(caixa != null) {
					
					ItemCaixa itemCaixa = new ItemCaixa();
					itemCaixa.setCaixa(caixa);
					itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
					itemCaixa.setData(new Date());
					itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
					itemCaixa.setFormaPagamento(venda.getFormaPagamento());
					itemCaixa.setOperacao(TipoOperacao.VENDA);
					itemCaixa.setTipoPagamento("Entrada");
					
					if(!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
						itemCaixa.setValor(venda.getValorTotal());
					} else {
						itemCaixa.setValor(new BigDecimal(valorTotal));
					}		
					
					itensCaixas.save(itemCaixa);
				}
				
			}
			
			
			
			

			PrimeFaces.current().executeScript("PF('confirmDialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
					+ venda.getNumeroVenda() + " registrada com sucesso!', timer: 5000 });");

			Venda vendaTemp_ = new Venda();
			vendaTemp_.setNumeroVenda(null);
			vendaTemp_.setTipoVenda(venda.getTipoVenda());
			vendaTemp_.setBairro(venda.getBairro());
			vendaTemp_.setUsuario(venda.getUsuario());	
			
			
			emitirCupom(venda);
			//imprimirCupom(itensVenda, venda);

			
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
			FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro");
			venda.setFormaPagamento(formaPagamento);
			
			filter = new ProdutoFilter();
			produto = new Produto();
			
			totalDeAcrescimo = BigDecimal.ZERO;
			
			activeIndex = 0;

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
			} else {
				if(entregaVenda.getId() != null) {
					entregas.remove(entregaVenda);
					entregaVenda = new Entrega();
					
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
			
			venda = vendas.save(venda);

			
			//if(caixaAberto) {
				
				ItemCaixa itemCaixa = itensCaixas.porCodigoOperacao(venda.getNumeroVenda(), TipoOperacao.VENDA, venda.getEmpresa());

				if(itemCaixa != null) {
					
					if(!venda.isConta()) {
						
						itemCaixa.setData(new Date());
						itemCaixa.setDescricao("Venda Nº" + venda.getNumeroVenda());
						
						if(!itemCaixa.getFormaPagamento().getNome().equals("Dinheiro")) {
							itemCaixa.setValor(new BigDecimal(valorTotal));
						}
						
						itemCaixa.setFormaPagamento(venda.getFormaPagamento());
						itemCaixa.setTipoPagamento("Entrada");
										
						
						if(!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
							itemCaixa.setValor(venda.getValorTotal());
						}
											
						itensCaixas.save(itemCaixa);
						
					} else {
						
						itensCaixas.remove(itemCaixa);
					}
					
				} else if(!venda.isConta()) {
					
					Caixa caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
					
					if(caixa != null) {
						
						itemCaixa = new ItemCaixa();
						itemCaixa.setCaixa(caixa);
						itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
						itemCaixa.setData(new Date());
						itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
						itemCaixa.setFormaPagamento(venda.getFormaPagamento());
						itemCaixa.setOperacao(TipoOperacao.VENDA);
						itemCaixa.setTipoPagamento("Entrada");
						
						if(!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
							itemCaixa.setValor(venda.getValorTotal());
						} else {
							itemCaixa.setValor(new BigDecimal(valorTotal));
						}		
						
						itensCaixas.save(itemCaixa);
					}
					
				}
				
			//}
			

			//venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			//venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			//venda.setQuantidadeItens(totalDeItens);
			//venda.setLucro(BigDecimal.valueOf(lucro));
			//venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));
			//venda = vendas.save(venda);
			
			PrimeFaces.current().executeScript("PF('confirmDialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
					+ venda.getNumeroVenda() + " atualizada com sucesso!', timer: 5000 });");
			
			activeIndex = 0;
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
		
		List<ItemVenda> itensVendasTemp = itensVendas.porVenda(venda);

		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			
			
			
			
			
			
			
			if (venda.getId() != null) {
				
				for (ItemVenda itemVenda : itensVendasTemp) {
					
					List<ItemVendaCompra> listaItensVendasCompras = itensVendasCompras.porItemVenda(itemVenda);
									
					for (ItemVendaCompra itemVendaCompra : listaItensVendasCompras) {
						
						if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
																
								System.out.println("ITEM REMOVIDO");
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().longValue());
	
								quantidadeDisponivel += itemVendaCompra.getQuantidade().longValue();
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel().longValue() + itemVendaCompra.getQuantidade().longValue());	
								//itemVenda.setSaldo(itemVenda.getSaldo().longValue() + itemCompraTemp.getQuantidadeDisponivel().longValue());
								
								System.out.println("Saldo: "+quantidadeDisponivel);
								
								System.out.println("Saldo Atual: "+quantidadeDisponivel);
	
							}
						}
					}	
				}
				
			}
			
			
			
			
			
			
			
			
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
					
					System.out.println("itemCompraTemp: "+itemCompraTemp.getCompra().getId() + " - itemVendaCompra: " + itemVendaCompra.getCompra().getId());
					
					if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
							.longValue()) {
						
						System.out.println("itemCompraTemp: "+itemCompraTemp.getProduto().getId() + " - itemVenda: " + itemVenda.getProduto().getId());
						
						if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
							
							produtoNaLista = true;
							
							/*if(!saldo) {
								itemVenda.setSaldo(itemVenda.getQuantidade());
								saldo = true;
							}	*/						
							
							//if (itemVenda.getId() == null && venda.getId() == null) {
								
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
								
							//} 

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
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!', timer: 1500 });");
		} else {

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}		
			
			/*
			if(venda.getId() != null) {
				Double valorDeCustoUnitario = 0D;//produto.getCustoMedioUnitario().doubleValue();
				
				//if(valorDeCustoUnitario.doubleValue() <= 0) {
				Produto produtoTemp = produtos.porId(produto.getId());
				if(produtoTemp.getQuantidadeAtual().longValue() > produto.getQuantidadeAtual().longValue()
						|| produtoTemp.getQuantidadeAtual().longValue() < produto.getQuantidadeAtual().longValue()) {
						
					Double saldo = 0D;
					for (ItemCompra itemCompra : itensCompra) {
						saldo += itemCompra.getQuantidadeDisponivel().longValue() * itemCompra.getValorUnitario().doubleValue();
					}
					
					valorDeCustoUnitario = saldo.doubleValue() / produto.getQuantidadeAtual().longValue();
					produto.setCustoMedioUnitario(new BigDecimal(valorDeCustoUnitario));
				}	
				//}	
			}*/
			
			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);
			
			if (quantidadeDisponivel < produto.getEstoqueMinimo().longValue()) {					
				PrimeFaces.current().executeScript(
						"swal({ type: 'warning', title: 'Atenção!', text: 'Produto com quantidade abaixo do estoque mínimo!', timer: 1500 });");
			}
			
		}
	}

	public void adicionarItem() {
		
		activeIndex = 0;

		if (venda.getId() == null) {
		

		/*
		List<ItemVendaCompra> itensVendaCompra_ = new ArrayList<ItemVendaCompra>();
		
		if (venda.getId() != null) {
			
			Long saldo_ = this.itemVenda.getQuantidade();			
			
			for (ItemVenda itemVenda : itensVendaRemovidos) {
				
				for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
					
					if (this.itemVenda.getProduto().getId().longValue() == itemVenda.getProduto().getId()
								.longValue()) {
																				
						if(itemVendaCompra.getQuantidade().intValue() > 0L) {
							
							if(itemVendaCompra.getQuantidade().intValue() <= this.itemVenda.getQuantidade().intValue()) {									
																	
								ItemVendaCompra itemVendaCompra_ = new ItemVendaCompra();
								itemVendaCompra_.setItemVenda(this.itemVenda);
								itemVendaCompra_.setCompra(itemVendaCompra.getCompra());
								itemVendaCompra_.setQuantidade(itemVendaCompra.getQuantidade().longValue());
								
								saldo_ -= itemVendaCompra.getQuantidade().intValue();
								itemVendaCompra.setQuantidade(0L);
								
							} else {
								
								ItemVendaCompra itemVendaCompra_ = new ItemVendaCompra();
								itemVendaCompra_.setItemVenda(this.itemVenda);
								itemVendaCompra_.setCompra(itemVendaCompra.getCompra());
								itemVendaCompra_.setQuantidade(itemVendaCompra.getQuantidade().longValue());
								
								itensVendaCompra_.add(itemVendaCompra_);
								
								saldo_ -= itemVendaCompra.getQuantidade().intValue();
								itemVendaCompra.setQuantidade(itemVendaCompra.getQuantidade().longValue() - saldo_.longValue());
							}
														
						}
						
						
						System.out.println("Saldo Atual ITENS REM.: "+itemVendaCompra.getQuantidade());
			
					}
				}	
			}
		}
		*/

	
	
	
	
	
			
			/* Método custo médio */
			itemVenda.setValorUnitario(itemVenda.getProduto().getPrecoDeVenda());
			
				
			if(itemVenda.getValorUnitario().doubleValue() > 0) {
				
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
						
						venda.setQuantidadeItens(venda.getQuantidadeItens() + itemVenda.getQuantidade());
						
						BigDecimal totalDesconto = BigDecimal.ZERO;
						if(itemVenda.getDesconto() != null) {
							totalDesconto = new BigDecimal((itemVenda.getProduto().getPrecoDeVenda().doubleValue() * itemVenda.getQuantidade().intValue() * itemVenda.getDesconto().doubleValue()) / 100);
						}
						
						venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() + totalDesconto.doubleValue()));
						
						
							
						
							double desconto = 0;				
							if(itemVenda.getDesconto() != null) {
								desconto = itemVenda.getDesconto().doubleValue() / 100;
							} else {
								itemVenda.setDesconto(BigDecimal.ZERO);
							}
							
							BigDecimal subtotal = BigDecimal.valueOf(
								itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue());					
							itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * desconto)));
							
							itemVenda.setVenda(venda);
							
							itemVenda.setSaldo(itemVenda.getQuantidade());
							
							Double valorDeCustoUnitario = itemVenda.getProduto().getCustoMedioUnitario().doubleValue();
							
							List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();	
							//itensVendaCompra.addAll(itensVendaCompra_);
							
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
									- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));
		
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
							
							
							BigDecimal totalSemDesconto = BigDecimal.ZERO;
							for (ItemVenda itemVenda : itensVenda) {
								totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVenda.getQuantidade().intValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()));
							}
							
							Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
							if(totalDescontoEmDinheiro > 0) {
								venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
							} else {
								venda.setTotalDesconto(BigDecimal.ZERO);
							}
										
		 
							atualizaSaldoLoteDeCompras(produto);
											
							
							/* Nova entrada de produto */
							itemVenda = new ItemVenda();
							itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
							this.produto = new Produto();
							filter = new ProdutoFilter();
							
							calculaAcrescimo();
							
							
					} else {
						PrimeFaces.current().executeScript(
								"swal({ type: 'error', title: 'Erro!', text: 'Quantidade maior que a disponível!', timer: 1500 });");
					}
					
				} else {
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor ou igual a zero!', timer: 1500 });");
				}
				
			} else {
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Item com valor unitário igual a zero, vá em cadastro e informe o preço de venda do produto!', timer: 3000 });");

			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível adicionar itens à esta venda!', timer: 1500 });");
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
							
							//if (itemVenda.getId() == null && venda.getId() == null) {
								
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
							//}

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
		
		
		
			
		
		
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
		
		Long quantidadeDisponivel = itensCompras.saldoPorProduto(itemSelecionado.getProduto()).longValue(); 
		
		List<ItemVenda> itensVendasTemp = itensVendas.porVenda(venda);
		
		if (venda.getId() != null) {
			
			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				
				for (ItemVenda itemVenda : itensVendasTemp) {
					
					List<ItemVendaCompra> listaItensVendasCompras = itensVendasCompras.porItemVenda(itemVenda);
									
					for (ItemVendaCompra itemVendaCompra : listaItensVendasCompras) {
						
						if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
																
								System.out.println("ITEM SELECIONADO");
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemSelecionado.getQuantidade().longValue());
	
								quantidadeDisponivel += itemVendaCompra.getQuantidade().longValue();
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel().longValue() + itemVendaCompra.getQuantidade().longValue());	
								//itemVenda.setSaldo(itemVenda.getSaldo().longValue() + itemCompraTemp.getQuantidadeDisponivel().longValue());
								
								System.out.println("Saldo: "+quantidadeDisponivel);
								
								System.out.println("Saldo Atual: "+quantidadeDisponivel);
	
							}
						}
					}	
				}
			}
			
		}
		
		
		
		
		
		
		
			
			venda.setQuantidadeItens(venda.getQuantidadeItens() - itemSelecionado.getQuantidade());
			
			BigDecimal totalDesconto = BigDecimal.ZERO;
			if(itemVenda.getDesconto() != null) {
				totalDesconto = new BigDecimal((itemSelecionado.getProduto().getPrecoDeVenda().doubleValue() * itemSelecionado.getQuantidade().intValue() * itemSelecionado.getDesconto().doubleValue()) / 100);
			}
			
			venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));


			// itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensVenda.remove(itemSelecionado);
			
			if(itemVenda.getProduto() != null) {
				itemVenda.getProduto().setQuantidadeAtual(itemVenda.getProduto().getQuantidadeAtual().longValue() + itemSelecionado.getQuantidade());
			}

			//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());

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
								
								//if (itemVenda.getId() == null && venda.getId() == null) {
									
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
									
									quantidadeDisponivel -= itemVendaCompra.getQuantidade();
									
									System.out.println("Saldo Atual: "+itemVenda.getSaldo());
								//}

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
			
			
			BigDecimal totalSemDesconto = BigDecimal.ZERO;
			for (ItemVenda itemVenda : itensVenda) {
				totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVenda.getQuantidade().intValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()));
			}
			
			Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
			if(totalDescontoEmDinheiro > 0) {
				venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
			} else {
				venda.setTotalDesconto(BigDecimal.ZERO);
			}
			
	

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);

			itemSelecionado = null;

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();
			
			activeIndex = 0;
			
			calculaAcrescimo();

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível remover os itens desta venda!', timer: 1500 });");
		}
	}

	public void editarItem() {

		if (venda.getId() == null) {
		
		
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
		
		Long quantidadeDisponivel = itensCompras.saldoPorProduto(itemSelecionado.getProduto()).longValue(); 
		
		List<ItemVenda> itensVendasTemp = itensVendas.porVenda(venda);
		
		if (venda.getId() != null) {
			
			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				
				for (ItemVenda itemVenda : itensVendasTemp) {
					
					List<ItemVendaCompra> listaItensVendasCompras = itensVendasCompras.porItemVenda(itemVenda);
									
					for (ItemVendaCompra itemVendaCompra : listaItensVendasCompras) {
						
						if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
								.longValue()) {
							if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
																
								System.out.println("ITEM SELECIONADO");
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemSelecionado.getQuantidade().longValue());
	
								quantidadeDisponivel += itemVendaCompra.getQuantidade().longValue();
								itemCompraTemp.setQuantidadeDisponivel(
										itemCompraTemp.getQuantidadeDisponivel().longValue() + itemVendaCompra.getQuantidade().longValue());	
								//itemVenda.setSaldo(itemVenda.getSaldo().longValue() + itemCompraTemp.getQuantidadeDisponivel().longValue());
								
								System.out.println("Saldo: "+quantidadeDisponivel);
								
								System.out.println("Saldo Atual: "+quantidadeDisponivel);
	
							}
						}
					}	
				}
			}
			
		}
		
		
		
		
			
			venda.setQuantidadeItens(venda.getQuantidadeItens() - itemSelecionado.getQuantidade());
			
			BigDecimal totalDesconto = BigDecimal.ZERO;
			if(itemSelecionado.getDesconto() != null) {
				totalDesconto = new BigDecimal((itemSelecionado.getProduto().getPrecoDeVenda().doubleValue() * itemSelecionado.getQuantidade().intValue() * itemSelecionado.getDesconto().doubleValue()) / 100);
			}
			
			venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));


			//itemVenda = itemSelecionado;
			itemVenda = new ItemVenda();
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			//itensVenda.remove(itemSelecionado);
			
			//itemVenda.setId(itemSelecionado.getId());
			
			itemVenda.setCode(itemSelecionado.getCode());		
			itemVenda.setProduto(itemSelecionado.getProduto());
			itemVenda.getProduto().setDescontoMaximo(itemSelecionado.getProduto().getDescontoMaximo().longValue());
			//itemVenda.getProduto().setPrecoDeVenda(itemVenda.getProduto().getCustoMedioUnitario());
			itemVenda.setQuantidade(itemSelecionado.getQuantidade());
			itemVenda.setLucro(itemSelecionado.getLucro());
			itemVenda.setTotal(itemSelecionado.getTotal());
			itemVenda.setDesconto(itemSelecionado.getDesconto());
			itemVenda.setObservacoes(itemSelecionado.getObservacoes());
			
			itemVenda.setUpdate(true);
			
			
			itensVenda.remove(itemSelecionado);
			
			

			//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());

			itensCompra = new ArrayList<ItemCompra>();
			List<String> itens = new ArrayList<String>();
			
			//Long quantidadeDisponivel = 0L;

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
								
								//if (itemVenda.getId() == null && venda.getId() == null) {
									
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
									
									quantidadeDisponivel -= itemVendaCompra.getQuantidade();
									
									System.out.println("Saldo Atual: "+itemVenda.getSaldo());
									
								/*} else {
									
									itemCompraTemp.setQuantidadeDisponivel(
											itemCompraTemp.getQuantidadeDisponivel().longValue() + itemVenda.getSaldo().longValue());
									
									System.out.println("Saldo Atual: "+itemVenda.getSaldo());
								}*/

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
				
				//quantidadeDisponivel += itemCompraTemp.getQuantidadeDisponivel();
			}
			
			
			
					
			
			itemVenda.getProduto().setQuantidadeAtual(quantidadeDisponivel);				
			

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}
			
			
			BigDecimal totalSemDesconto = BigDecimal.ZERO;
			for (ItemVenda itemVenda : itensVenda) {
				totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVenda.getQuantidade().intValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()));
			}
			
			Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
			if(totalDescontoEmDinheiro > 0) {
				venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
			} else {
				venda.setTotalDesconto(BigDecimal.ZERO);
			}
			
			

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);

			itemSelecionado = null;

			// itemVenda = new ItemVenda();
			// itemCompra = new ItemCompra();
			
			activeIndex = 0;
			
			calculaAcrescimo();


		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível editar os itens desta venda!', timer: 1500 });");
		}

	}
	
	
	public void detalharItem() {

		//if (venda.getId() == null) {

			//itemVenda = itemSelecionado;
			itemVenda = new ItemVenda();
			//venda.setValorTotal(
					//BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			//itensVenda.remove(itemSelecionado);
			
			

			itemVenda.setId(itemSelecionado.getId());
			itemVenda.setCode(itemSelecionado.getCode());
			itemVenda.setProduto(itemSelecionado.getProduto());
			itemVenda.getProduto().setDescontoMaximo(itemSelecionado.getProduto().getDesconto().longValue());
			//itemVenda.getProduto().setCustoMedioUnitario(itemSelecionado.getValorCompra());
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

	public List<FormaPagamento> getTodasFormasPagamentos() {
		return todasFormasPagamentos;
	}
	
	
	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(TipoPagamento tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
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

	public boolean isContaAPagar() {
		return contaAPagar;
	}

	public void setContaAPagar(boolean contaAPagar) {
		this.contaAPagar = contaAPagar;
	}
	
	public TipoPagamento[] getTiposPagamentos() {
		return TipoPagamento.values();
	}

	public Double getValorEntrada() {
		return valorEntrada;
	}

	public void setValorEntrada(Double valorEntrada) {
		this.valorEntrada = valorEntrada;
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

	public Integer getTodasContasSize() {
		return todasContas.size();
	}

	public void zerarParcelas() {
		todasContas = new ArrayList<>();
		parcelasConfirmadas = false;
	}
	
	public PeriodoPagamento[] getPeriodosPagamentos() {
		return PeriodoPagamento.values();
	}
	
	public void changeTipoPagamento() {
		
		/*valorEntrada = 0D;
		valorRecebido = 0D;
		troco = 0D;
		faltando = 0D;*/
		
		if (tipoPagamento == TipoPagamento.AVISTA) {
			
			contaAPagar = false;
			vendaPaga = true;
			parcelasConfirmadas = true;
			
			if(!trocaPendente) {
				faltando = venda.getValorTotal().doubleValue();
			} else {
							
				Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
				faltando = venda.getValorTotal().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
				
				if(faltando < 0) {
					troco = -1 * faltando.doubleValue();
					faltando = 0D;			
				}	
			}
			
			PrimeFaces.current().executeScript("ocultar();");
			PrimeFaces.current().executeScript("mostrarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
			
		} else {
			
			venda.setFormaPagamento(formasPagamentos.porNome("Dinheiro"));
			parcelasConfirmadas = false;			
			calculaAcrescimo();
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


	public void changeStatusPago() {
		
		if(venda.getFormaPagamento().getNome().equals("Dinheiro") && entrega != true) {
			
			if (!vendaPaga) {
				
				if(valorRecebido == null) {
					valorRecebido = 0D;
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
	
	
	public void calculaTroco() {
				
		Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
		faltando = venda.getValorTotal().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
		
		troco = 0D;
		
		if(faltando < 0) {
			troco = -1 * faltando.doubleValue();
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
	
	
	public void changeFormaPagamento() {
		
		if(trocaPendente) {
			
			Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
			faltando = venda.getValorTotal().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
			
			if(faltando < 0) {
				troco = -1 * faltando.doubleValue();
				faltando = 0D;			
			}	
		}
		
		vendaPaga = true;
		
		if (!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
			PrimeFaces.current().executeScript("ocultarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		} else {		
			PrimeFaces.current().executeScript("mostrarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		}
	}
	
	
	public void confirmarPrimeiraParcela() {
		
		primeiraParcelaEmString = sdf.format(primeiraParcela);
	}
	
	public void preparaPrimeiraParcela() throws ParseException {
		
		primeiraParcela = sdf.parse(primeiraParcelaEmString);
		parcelasConfirmadas = false;
	}


	public void confirmarPagamentoPara() {
		
		pagamentoParaEmString = sdf.format(pagamentoPara);
	}

	public void preparaPagamentoPara() throws ParseException {
		
		pagamentoPara = sdf.parse(pagamentoParaEmString);
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
				Produto produto = produtos.porId(itemVenda.getProduto().getId());
				Double valorDeCustoUnitario = produto.getCustoMedioUnitario().doubleValue();
				itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));					
			
				if(produto.getQuantidadeAtual().longValue() <= 0 && valorDeCustoUnitario.doubleValue() <= 0) {
					valorDeCustoUnitario = (itemVenda.getProduto().getQuantidadeAtual().longValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()) / itemVenda.getProduto().getQuantidadeAtual().longValue();
				}
				
				double desconto = 0;				
				if(itemVenda.getDesconto() != null) {
					desconto = itemVenda.getDesconto().doubleValue() / 100;
				} else {
					itemVenda.setDesconto(BigDecimal.ZERO);
				}
				
				BigDecimal subtotal = BigDecimal.valueOf(
					itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue());					
				itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * desconto)));
	
				/* Calculo do Lucro em valor e percentual */										
				Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
						* itemVenda.getQuantidade().intValue())
				.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
				
			
				itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
						* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().intValue())
						- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));
	
				itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade())) / itemVenda.getTotal().doubleValue() * 100)));
				itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().intValue()));
	
				
				subTotal += itemVenda.getTotal().doubleValue();
				custoMedio += itemVenda.getValorCompra().doubleValue();
				lucro += itemVenda.getLucro().doubleValue();
				
			} else {
				
				subTotal += itemVenda.getTotal().doubleValue();
				custoMedio += itemVenda.getValorCompra().doubleValue();
				lucro += itemVenda.getLucro().doubleValue();
			}
		}
		
		
		

		Double valorVenda = venda.getValorTotal().doubleValue();
		Double taxaEntrega = venda.getBairro().getTaxaDeEntrega().doubleValue();
		/*if (valorEntrada != null && valorEntrada > 0) {
			valorVenda = venda.getValorTotal().doubleValue() - valorEntrada;

			Conta conta = new Conta();
			conta.setParcela("Entrada");
			conta.setValor(new BigDecimal(valorEntrada));
			conta.setVencimento(vencimento.getTime());
			
			conta.setPagamento(vencimento.getTime());
			 
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(conta.getVencimento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			entradas.add(conta);
		}*/

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
	
	public void confirmarParcelas() {
		
		parcelasConfirmadas = true;
	}

	public List<Conta> getTodasContas() {
		return todasContas;
	}

	public boolean isParcelasConfirmadas() {
		return parcelasConfirmadas;
	}

	public BigDecimal getTotalDeAcrescimo() {
		return totalDeAcrescimo;
	}

	public boolean isVendaPaga() {
		return vendaPaga;
	}

	public void setVendaPaga(boolean vendaPaga) {
		this.vendaPaga = vendaPaga;
	}

	public Double getFaltando() {
		return faltando;
	}

	public void setFaltando(Double faltando) {
		this.faltando = faltando;
	}

	public Date getPrimeiraParcela() {
		return primeiraParcela;
	}

	public void setPrimeiraParcela(Date primeiraParcela) {
		this.primeiraParcela = primeiraParcela;
	}

	public String getPrimeiraParcelaEmString() {
		return primeiraParcelaEmString;
	}

	public Date getPagamentoPara() {
		return pagamentoPara;
	}

	public void setPagamentoPara(Date pagamentoPara) {
		this.pagamentoPara = pagamentoPara;
	}

	public String getPagamentoParaEmString() {
		return pagamentoParaEmString;
	}

	public boolean isTrocaPendente() {
		return trocaPendente;
	}

	public String getSaldoParaTrocaEmString() {
		return saldoParaTrocaEmString;
	}
	
	
	private void imprimirCupom(List<ItemVenda> itensVenda, Venda venda) {
        
		String dataF = "dd/MM/yyyy";
        String horaF = "HH:mm:ss";
        String data, hora;

        Date tempoAtual = new Date();
        //Pegando a data
        SimpleDateFormat formata = new SimpleDateFormat(dataF);
        data = formata.format(tempoAtual);
        //pegando a hora
        formata = new SimpleDateFormat(horaF);
        hora = formata.format(tempoAtual);

        String conteudoImprimir = "";
        int numeroVenda = venda.getNumeroVenda().intValue();

        for (ItemVenda itemVenda : itensVenda) {
        	conteudoImprimir += itemVenda.getProduto().getCodigoDeBarras() + "    "
                    + itemVenda.getQuantidade() + "     "
                    + nf.format(itemVenda.getValorUnitario().doubleValue()) + "    "
                    + itemVenda.getProduto().getDescricao() + "\n\r";
		}
        
        System.out.println(
                  "EXCLUSIVA MODAS                                 \n\r"
                + "RUA DO COMERCIO 245, CENTRO, SANTA INES - MA    \n\r"
                + "CONTATO (98) 98495-7066                         \n\r"
                + "Data: " + data + "             Hora: " + hora +"\n\r"
                + "NUMERO DA VENDA: " + numeroVenda +              "\n\r"
                + "------------------------------------------------\n\r"
                + "                CUPOM NAO FISCAL                \n\r"
                + "------------------------------------------------\n\r"
                + "COD  QTD   PRECO   DESCRICAO                    \n\r"
                + conteudoImprimir +                              "\n\r"
                + "------------------------------------------------\n\r"
                + "SUBTOTAL    R$: " + nf.format(venda.getValorTotal().doubleValue()) + "\n\r"
                
				+ "ACRÉSCIMO   R$: " + nf.format(venda.getAcrescimo().doubleValue()) + "\n\r"
				
				+ "TX. ENTREGA R$: " + nf.format(venda.getTaxaDeEntrega().doubleValue()) + "   " + "VALOR RECEBIDO R$: " + nf.format(venda.getValorRecebido().doubleValue()) + "\n\r"

				+ "DESCONTO    R$: " + nf.format(venda.getTotalDescontoEmDinheiro().doubleValue())   +"     " + "       TROCO R$: " + nf.format(venda.getTroco().doubleValue())+ "\n\r"
           
                + "VALOR TOTAL R$: " + nf.format(venda.getValorTotal().doubleValue()) + "\n\r"
                + "------------------------------------------------\n\r"
                + "   TROCA SOMENTE EM ATE 7 DIAS E COM A NOTA  \n\r"
                + "            OBRIGADO E VOLTE SEMPRE          \n\r"
                + "\n\r\n\r\n\r\n\r\n\r\n\r\n\r"
                
                
        );
    }

    /**
     * Função para imprimir
     *
     * @param pTexto
    public void imprimir(String pTexto) {
    	System.out.println(pTexto);
        try {
            InputStream prin = new ByteArrayInputStream(pTexto.getBytes());
            DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            SimpleDoc documentoTexto = new SimpleDoc(prin, docFlavor, null);
            PrintService impressora = PrintServiceLookup.lookupDefaultPrintService();
            //pega a impressora padrão
            PrintRequestAttributeSet printerAttributes = new HashPrintRequestAttributeSet();
            printerAttributes.add(new JobName("Impressão", null));
            printerAttributes.add(OrientationRequested.PORTRAIT);
            printerAttributes.add(MediaSizeName.ISO_A4);
            //informa o tipo de folha
            DocPrintJob PrintJob = impressora.createPrintJob();

            try {
                PrintJob.print(documentoTexto, (PrintRequestAttributeSet) printerAttributes);
                //tenta imprimir
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Não foi possível realizar a impressão!", "Erro", JOptionPane.ERROR_MESSAGE);
                //Mensarem de erro
            }
            prin.close();
        } catch (Exception e) {

        }
    }
    */
	
	public void emitirCupom(Venda venda) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		EspelhoVenda pedido = new EspelhoVenda();
		pedido.setVendaNum(venda.getNumeroVenda() + "");
		pedido.setTipoVenda(venda.getTipoVenda().getDescricao().toUpperCase());
		pedido.setBairro(venda.getBairro().getNome().toUpperCase());
		pedido.setDataVenda(sdf.format(venda.getDataVenda()));
		pedido.setVendedor(venda.getUsuario().getNome().toUpperCase());

		Entrega entrega = entregas.porVenda(venda);
		if (entrega != null) {
			pedido.setResponsavel(entrega.getNome());
			pedido.setLocalizacao(entrega.getLocalizacao());
			pedido.setObservacao(entrega.getObservacao());
		}
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemEspelhoVenda itemPedido = new ItemEspelhoVenda();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			//itemPedido.setDescricao(itemVenda.getProduto().getDescricao());
			itemPedido.setDescricao(itemVenda.getProduto().getCodigoDeBarras() + " " + itemVenda.getProduto().getDescricao() 
					 + " " + itemVenda.getQuantidade() + " " + itemPedido.getUN() + " x " + nf.format(itemVenda.getValorUnitario().doubleValue()));
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().doubleValue()));
			itemPedido.setQuantidade(String.valueOf(itemVenda.getQuantidade()));
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}

		pedido.setTotalVenda(nf.format(venda.getValorTotal()));

		List<EspelhoVenda> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
		try {
			report.getRelatorio(pedidos, "Venda-N" + venda.getNumeroVenda().longValue());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
