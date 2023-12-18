package com.webapp.controller;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
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
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
import javax.validation.constraints.NotNull;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.printing.PDFPageable;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.google.common.io.ByteSource;
import com.webapp.model.Bairro;
import com.webapp.model.Caixa;
import com.webapp.model.CategoriaProduto;
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
import com.webapp.model.ItemEspelhoVendaPagamentos;
import com.webapp.model.ItemEspelhoVendaProdutos;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Log;
import com.webapp.model.Mesa;
import com.webapp.model.Pagamento;
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
import com.webapp.repository.Caixas;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Clientes;
import com.webapp.repository.Configuracoes;
import com.webapp.repository.Contas_;
import com.webapp.repository.Devolucoes;
import com.webapp.repository.Entregas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Logs;
import com.webapp.repository.Pagamentos;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.repository.filter.BairroFilter;
import com.webapp.repository.filter.ClienteFilter;
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
	private Contas_ contas;

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
	
	private List<Produto> listaDeProdutos01;
	
	private List<Produto> listaDeProdutos02;

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
	
	private boolean leitor = false;
	
	private Integer activeIndex = 0;
	
	private boolean edit;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	@Inject
	private Clientes clientes;
	
	private List<FormaPagamento> todasFormasPagamentos = new ArrayList<FormaPagamento>();
	
	private List<Cliente> todosClientes = new ArrayList<Cliente>();
	
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
	
	private boolean imprimirCupom = true;
	
	private boolean gerarPDF = false;
	
	private boolean pdvRapido = false;
	
	private int vias = 1;
	
	private StreamedContent fileContent;
	
	
	@Inject
	private Configuracoes configuracoes;
	
	@Inject
	private Configuracao configuracao;
	
	
	private List<CategoriaProduto> todasCategoriasProdutos;
	
	
	@Inject
	private CategoriasProdutos categoriasProdutos;
	
	
	private ProdutoFilter filterProdutos01 = new ProdutoFilter();
	
	private ProdutoFilter filterProdutos02 = new ProdutoFilter();
	
	private List<Usuario> todosEntregadores;
	
	
	private List<String> tamanhos01 = new ArrayList<String>();
	
	private List<String> unidades01 = new ArrayList<String>();
	
	private List<String> tamanhos02 = new ArrayList<String>();
	
	private List<String> unidades02 = new ArrayList<String>();
	
	
	private DualListModel<Produto> pizzas;
	
	
	@Inject
	private Cliente cliente;	
	
	private InputStream targetStream;
	
	private Venda vendaTemp_ = new Venda();
	
	private Mesa mesa;
	
	private List<Mesa> mesas = new ArrayList<Mesa>();
	
	
	
	@Inject
	private Pagamento pagamento;
	
	@Inject
	private Pagamentos pagamentos;
	
	@Inject
	private Caixa caixa;
	
	
	private String keyword_produto1 = "";
	
	private String keyword_produto2 = "";
	
	@Inject
	private Logs logs;
	
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario = usuarios.porLogin(user.getUsername());
			
			Caixa caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
			if(caixa != null) {
				caixaAberto = true;
			}
			
			todosUsuarios = usuarios.todosVendedores(usuario.getEmpresa());
			todosTiposVendas = tiposVendas.todos(usuario.getEmpresa());
			todosBairros = bairros.todos(usuario.getEmpresa());
			todasFormasPagamentos = formasPagamentos.todos(usuario.getEmpresa());
			todosClientes = clientes.todos(usuario.getEmpresa());
			
			todosEntregadores = usuarios.todosEntregadores(usuario.getEmpresa());
						
			
			if(usuario.getEmpresa().getId() == 7111 || usuario.getEmpresa().getId() == 7112) {
				venda.setTipoVenda(tiposVendas.porId(33L));
				FormaPagamento formaPagamento = formasPagamentos.porId(13987L);
				venda.setFormaPagamento(formaPagamento);
				venda.setBairro(bairros.porId(3008L));
			} else {
				venda.setTipoVenda(tiposVendas.porId(3L));
				FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro", usuario.getEmpresa());
				venda.setFormaPagamento(formaPagamento);
				venda.setBairro(bairros.porNome("Nao Informado", usuario.getEmpresa()));
			}
			
					
			venda.setUsuario(usuario);
			
			
			//Cliente cliente = clientes.porId(1L);
			//venda.setCliente(cliente);
			
			itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
			
			
			Object[] saldo = itensDevolucoes.saldoParaTroca(usuario, usuario.getEmpresa());
			
			if(saldo[0] != null) {
				saldoParaTroca = ((BigDecimal)saldo[1]).doubleValue();
				saldoParaTrocaEmString = nf.format(((BigDecimal)saldo[1]).doubleValue());			
				trocaPendente = true;
			}			
			
			configuracao = configuracoes.porUsuario(usuario);
			leitor = configuracao.isLeitorPDV();
			imprimirCupom = configuracao.isCupomAtivado();
			
			pdvRapido = configuracao.isPdvRapido();
			vias = configuracao.getVias();
			
			activeIndex = configuracao.getAbaPDV();
			
			filterProdutos01.setCategoriaProduto(configuracao.getCategoriaProduto01());
			filterProdutos02.setCategoriaProduto(configuracao.getCategoriaProduto02());
			
			todasCategoriasProdutos = categoriasProdutos.todos(usuario.getEmpresa());
			
			
			
			tamanhos01 = produtos.tamanhos(usuario.getEmpresa(), configuracao.getCategoriaProduto01());
			unidades01 = produtos.unidades(usuario.getEmpresa(), configuracao.getCategoriaProduto01());
			
			filterProdutos01.setTamanho(configuracao.getTamanho01());
			filterProdutos01.setUnidade(configuracao.getUnidade01());
			
			
			tamanhos02 = produtos.tamanhos(usuario.getEmpresa(), configuracao.getCategoriaProduto02());
			unidades02 = produtos.unidades(usuario.getEmpresa(), configuracao.getCategoriaProduto02());
			
			filterProdutos02.setTamanho(configuracao.getTamanho02());
			filterProdutos02.setUnidade(configuracao.getUnidade02());
			
			
			
			if(venda.getId() == null) {
				if(configuracao.isPopupCliente()) {
					PrimeFaces.current().executeScript("PF('cliente-dialog').show();");
				} else {
					venda.setCliente(clientes.porNome("Nao Informado", usuario.getEmpresa()));
				}
			}
			
			
			
			listarProdutos01();
			
			listarProdutos02();
			
			
			
			List<Produto> themesSource = new ArrayList<Produto>();
	        List<Produto> themesTarget = new ArrayList<Produto>();
	         
	        pizzas = new DualListModel<Produto>(themesSource, themesTarget);
	        
	        
	        mesas = new ArrayList<Mesa>();
	        for (int i = 1; i <= 10; i++) {
				Mesa mesa = new Mesa();
				mesa.setCodigo("" + i);
				mesa.setNumero("Mesa " + i);
				if(i < 10) {
					mesa.setCodigo("0" + i);
					mesa.setNumero("Mesa 0" + i);
				}
				
				Venda venda = vendas.porStatusMesa(mesa.getCodigo(), null, usuario.getEmpresa());				
				if(venda != null) {
					mesa.setVenda(venda);
					mesa.setStatus("O");
				} else {
					mesa.setStatus("L");
				}
								
				mesas.add(mesa);
			}
			
		}
	}
	
	
	public void selecionaMesa(Mesa mesa) {
		this.mesa = mesa;
		venda.setMesa(mesa.getCodigo());
	}
	
	public void abrirMesa(Mesa mesa) {
		this.mesa = mesa;
		venda = mesa.getVenda();
		buscar();
	}
	

	public void pesquisar() {
		
		activeIndex = 1;
		configuracao.setAbaPDV(activeIndex);
		configuracao = configuracoes.save(configuracao);
		
		System.out.println("Código escaneado: " + filter.getCodigo());
		filter.setCodigo(filter.getCodigo().trim().replace(" ", ""));
		
		if(caixaAberto) {
			
			if(!filter.getCodigo().equals("")) {
				
				String quantidade = null;
				
				if(filter.getCodigo().length() == 13 && filter.getCodigo().substring(0, 1).equals("2")) {
					
					String codigo = filter.getCodigo().substring(1, 7);				
					quantidade = filter.getCodigo().substring(7, filter.getCodigo().length());
					
					filter.setCodigo(codigo.replaceFirst("0*", ""));
				}
				
				Produto produto = produtos.porCodigoDeBarras(filter.getCodigo(), usuario.getEmpresa());	
				
				if(produto != null) {
					filter = new ProdutoFilter();
					selecionarProduto(produto, quantidade);
					
				} else {
					filter = new ProdutoFilter();
					itemVenda = new ItemVenda();
					itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
					PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto não encontrado!', timer: 1500 });");
				}
				
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
			
			if(venda.getFormaPagamento().isClientePaga()) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotalComDesconto().doubleValue() + venda.getValorTotalComDesconto().doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100)));				
			}
			
			
			if(venda.getId() != null) {
				if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
					venda.setLucro(new BigDecimal(venda.getLucro().doubleValue() - ((venda.getValorTotalComDesconto().doubleValue() * venda.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
				}
			}
			
			
		
		venda.setAcrescimo(venda.getFormaPagamento().getAcrescimo());
		
		venda.setTaxaDeAcrescimo(venda.getFormaPagamento().getAcrescimo());
		
		venda.setTaxaDeEntrega(taxaDeEntrega);
		
		if(!trocaPendente) {
			
			//faltando = venda.getValorTotal().doubleValue();
			
		}
		
		
		changeFormaPagamento();
	}
	
	
	public void confirmaCliente() {
		
		if(caixaAberto) {
			
			Double subtotal = 0D;
			
			totalDeAcrescimo = BigDecimal.ZERO;
			
			for (ItemVenda itemVenda : itensVenda) {			
				subtotal += itemVenda.getTotal().doubleValue();
			}
			
			BigDecimal taxaDeEntrega = venda.getTaxaDeEntrega();
			if(venda.getCliente() != null && venda.getCliente().getId() != null) {
				if(venda.getTaxaDeEntrega().doubleValue() == venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue()) {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
				} else {
					taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;			
				}
			} else {
				venda.setCliente(clientes.porNome("Nao Informado", usuario.getEmpresa()));
			}
			
			subtotal += taxaDeEntrega.doubleValue();
			
			
			if(venda.getDesconto() != null) {
				venda.setValorTotalComDesconto(new BigDecimal(subtotal.doubleValue() - venda.getDesconto().doubleValue()));
			} else {
				venda.setValorTotalComDesconto(new BigDecimal(subtotal.doubleValue()));
			}			
	
			
			if(venda.getFormaPagamento().getAcrescimo().doubleValue() > 0 && venda.getFormaPagamento().isClientePaga()) {
				totalDeAcrescimo = new BigDecimal(venda.getValorTotalComDesconto().doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100));		
			}
			
			if(venda.getFormaPagamento().isClientePaga()) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotalComDesconto().doubleValue() + venda.getValorTotalComDesconto().doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100)));				
			}
		
			venda.setAcrescimo(venda.getFormaPagamento().getAcrescimo());
			
			venda.setTaxaDeAcrescimo(venda.getFormaPagamento().getAcrescimo());
			
			venda.setTaxaDeEntrega(taxaDeEntrega);
			
			/*
			if(venda.getFormaPagamento().getAcrescimo().doubleValue() > 0 && venda.getFormaPagamento().isClientePaga()) {
				totalDeAcrescimo = new BigDecimal(subtotal.doubleValue() * (venda.getFormaPagamento().getAcrescimo().doubleValue()/100));		
			}
			
			System.out.println(subtotal + " - " + venda.getFormaPagamento().getAcrescimo());
			
			venda.setValorTotal(new BigDecimal(subtotal.doubleValue() + totalDeAcrescimo.doubleValue()));
			venda.setValorTotalComDesconto(venda.getValorTotal());
			
			if(venda.getId() != null) {
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - (venda.getDesconto() != null ? venda.getDesconto().doubleValue() : 0)));
			}			
		
			venda.setAcrescimo(venda.getFormaPagamento().getAcrescimo());
			
			venda.setTaxaDeEntrega(taxaDeEntrega);
			*/
			
			if(!trocaPendente) {
				
				faltando = venda.getValorTotal().doubleValue();
				if(venda.getId() != null) {
					faltando = venda.getFaltando().doubleValue();
				}
				
			}		
			
			changeFormaPagamento();
			
			if(venda.getId() != null) {
				calculaAcrescimo();
			}
			
			PrimeFaces.current().executeScript("PF('cliente-dialog').hide();");				
	        
		} else {
			if(venda.getCliente() == null || venda.getCliente().getId() == null) {
				venda.setCliente(clientes.porNome("Nao Informado", usuario.getEmpresa()));
			}
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Caixa Fechado!', text: 'Para usar o PDV, primeiro você deve abrir o caixa!'});");		
		}
	}

	
	public void novaVenda() {
		
		venda = new Venda();
		itensVenda = new ArrayList<ItemVenda>();
		itemVenda = new ItemVenda();
		itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
		itemSelecionado = null;

		itensCompra = new ArrayList<>();
		itemCompra = new ItemCompra();

		entregaVenda = new Entrega();
		entrega = false;
		
		pagamento = new Pagamento();
		
		edit = false;
		
		venda.setTipoVenda(tiposVendas.porDescricao("Nao Informado", usuario.getEmpresa()));
		venda.setBairro(bairros.porNome("Nao Informado", usuario.getEmpresa()));
		venda.setCliente(null);
		venda.setUsuario(usuario);
		
		FormaPagamento formaPagamento = formasPagamentos.porNome("Dinheiro", usuario.getEmpresa());
		venda.setFormaPagamento(formaPagamento);
		
		filter = new ProdutoFilter();
		produto = new Produto();
		
		activeIndex = configuracao.getAbaPDV();
		
		mesa = new Mesa();
		
		if(configuracao.isPopupCliente()) {
			PrimeFaces.current().executeScript("PF('cliente-dialog').show();");
		} else {
			venda.setCliente(clientes.porNome("Nao Informado", usuario.getEmpresa()));
		}
        
		entrega = false;
		
		mesas = new ArrayList<Mesa>();
        for (int i = 1; i <= 10; i++) {
			Mesa mesa = new Mesa();
			mesa.setCodigo("" + i);
			mesa.setNumero("Mesa " + i);
			if(i < 10) {
				mesa.setCodigo("0" + i);
				mesa.setNumero("Mesa 0" + i);
			}
			
			Venda venda = vendas.porStatusMesa(mesa.getCodigo(), null, usuario.getEmpresa());				
			if(venda != null) {
				mesa.setVenda(venda);
				mesa.setStatus("O");
			} else {
				mesa.setStatus("L");
			}
							
			mesas.add(mesa);
		}
		//PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda cancelada com sucesso!', timer: 1500 });");
		
	}
	
	public List<Bairro> completeText(String query) {
		
        BairroFilter filtro = new BairroFilter();
        filtro.setNome(query);
        
        List<Bairro> listaDeBairros = bairros.filtrados(filtro, usuario.getEmpresa());       
        
        return listaDeBairros;
    }
	
	public List<Cliente> completeText_Cliente(String query) {
		
	    ClienteFilter filtro = new ClienteFilter();
	    filtro.setNome(query);
	    
	    List<Cliente> listaDeClientes = clientes.filtrados_(filtro);  
	    
	    for (Cliente cliente : listaDeClientes) {
	    	cliente.setNomeFormatado(cliente.getNome());
			if(cliente.getContato() != null && !cliente.getContato().trim().equals("")) {
				cliente.setNomeFormatado(cliente.getNome() + " | " + cliente.getContato());
			}
		}
	    
	    return listaDeClientes;
	}
	
	public List<Produto> completeText_Produto(String query) {
		
		filter.setEmpresa(usuario.getEmpresa());
		filter.setDescricao(query);
		
		List<Produto> listaProdutos = produtos.filtrados(filter);
		
		for (Produto produto : listaProdutos) {
			produto.setDescricaoFormatada(convertToTitleCaseIteratingChars(produto.getCodigo()  + " | " + produto.getNome()  + " | " + produto.getDescricao() + ", " + produto.getUnidadeMedida() + (produto.getNumeracao() != null && !produto.getNumeracao().equals("") ? ", " + produto.getNumeracao() : "") + ", R$ " + nf.format(produto.getPrecoDeVenda())));
		}
         
        return listaProdutos;
    }
	
	public void selectProduto1() {
		
		listarProdutos01();
		
		if(!keyword_produto1.trim().equals("")) {
			List<Produto> listaDeProdutos01_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos01) {
				if(produto.getDescricao().toLowerCase().contains(keyword_produto1.toLowerCase())) {
					listaDeProdutos01_Temp.add(produto);
				}
			}
			
			listaDeProdutos01 = new ArrayList<Produto>();
			listaDeProdutos01.addAll(listaDeProdutos01_Temp);
		}
		
	}

	public void selectProduto2() {
		
		listarProdutos02();
		
		if(!keyword_produto2.trim().equals("")) {
			List<Produto> listaDeProdutos02_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos02) {
				if(produto.getDescricao().toLowerCase().contains(keyword_produto2.toLowerCase())) {
					listaDeProdutos02_Temp.add(produto);
				}
			}
			
			listaDeProdutos02 = new ArrayList<Produto>();
			listaDeProdutos02.addAll(listaDeProdutos02_Temp);
		}
		
	}
	
	public void onItemSelect(SelectEvent event) {
		
		activeIndex = 1;
		configuracao.setAbaPDV(activeIndex);
		configuracao = configuracoes.save(configuracao);
		
		if(caixaAberto) {
			
	        System.out.println(event.getObject().toString());
	        selecionarProduto((Produto) event.getObject(), null);
	        produto = new Produto();
	        
		} else {
			produto = new Produto();
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Caixa Fechado!', text: 'Para usar o PDV, primeiro você deve abrir o caixa!', timer: 5000 });");		
		}
    }

	public void buscar() {
		venda = vendas.porId(venda.getId());
		itensVenda = itensVendas.porVenda(venda);
		
		Pagamento pagamentoTemp = pagamentos.porVenda(venda, venda.getEmpresa());
		if(pagamentoTemp != null) {
			pagamento = pagamentoTemp;
		}
		
		if(venda.getTipoPagamento() == null) {
			
			mesa = new Mesa();
			mesa.setCodigo(venda.getMesa());
			mesa.setNumero("Mesa " + venda.getMesa());
			
			mesas = new ArrayList<Mesa>();
	        for (int i = 1; i <= 10; i++) {
				Mesa mesa = new Mesa();
				mesa.setCodigo("" + i);
				mesa.setNumero("Mesa " + i);
				if(i < 10) {
					mesa.setCodigo("0" + i);
					mesa.setNumero("Mesa 0" + i);
				}
				
				Venda venda = vendas.porStatusMesa(mesa.getCodigo(), null, usuario.getEmpresa());				
				if(venda != null) {
					mesa.setVenda(venda);
					mesa.setStatus("O");
				} else {
					mesa.setStatus("L");
				}
								
				mesas.add(mesa);
			}
		}

		Double subtotal = 0D;
		for (ItemVenda itemVenda : itensVenda) {
			
			List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
			itemVenda.setItensVendaCompra(itensVendaCompra);
			
			itemVenda.getProduto().setUnidadeMedida(convertToTitleCaseIteratingChars(itemVenda.getProduto().getUnidadeMedida()));
			
			subtotal += itemVenda.getTotal().doubleValue();
			
			try {
				Thread.sleep(100);
				itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
			} catch (InterruptedException e) {
			}
			
			
			
			itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNome());
			itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricao());		
			
			if(itemVenda.getComposicao() != null && !itemVenda.getComposicao().trim().equals("")) {
	        	String[] itens = itemVenda.getComposicao().split(",");
				for (int i = 0; i < itens.length; i++) {
					Produto produto = produtos.porId(Long.parseLong(itens[i]));
					
					itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNomeCompleto() + " | " + produto.getNome());
					itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricaoCompleta() + " | " + produto.getDescricao());		
				}
	        }
			
			if(itemVenda.getProduto().getNomeCompleto().length() > 35) {
				itemVenda.getProduto().setNomeFormatado(convertToTitleCaseIteratingChars(itemVenda.getProduto().getNomeCompleto().substring(0, 34) + "..."));    			    
			} 			
			
			if(itemVenda.getProduto().getDescricaoCompleta().length() > 73) {
				itemVenda.getProduto().setDescricaoFormatada(convertToTitleCaseIteratingChars(itemVenda.getProduto().getDescricaoCompleta().substring(0, 72) + "..."));    			    
			}
			
			if(itemVenda.getProduto().getCategoriaProduto().getNome().toLowerCase().contains("pizza")) {
				itemVenda.setPizza(true);
			} else {
				itemVenda.setPizza(false);
			}
	        
		}
		
		saldoParaTroca = venda.getSaldoParaTroca().doubleValue();
		saldoParaTrocaEmString = nf.format(saldoParaTroca);	
		
		entregaVenda = entregas.porVenda(venda);
		entrega = entregaVenda.getId() != null;
		
		valorRecebido = null;//venda.getValorRecebido().doubleValue();
		//faltando = venda.getFaltando().doubleValue();
		troco = 0D;//venda.getTroco().doubleValue();
		
		venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
		if(venda.getDesconto() != null) {
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - (venda.getDesconto() != null ? venda.getDesconto().doubleValue() : 0)));
		}
		
		if(venda.isClientePagouTaxa()) {
			//totalDeAcrescimo = new BigDecimal(venda.getValorTotalComDesconto().doubleValue() * (venda.getTaxaDeAcrescimo().doubleValue()/100));
			if(venda.getDesconto() != null) {
				totalDeAcrescimo = new BigDecimal(((subtotal.doubleValue() + (entrega ? venda.getTaxaDeEntrega().doubleValue() : 0)) - venda.getDesconto().doubleValue()) * (venda.getTaxaDeAcrescimo().doubleValue()/100));
			} else {
				totalDeAcrescimo = new BigDecimal((subtotal.doubleValue() + (entrega ? venda.getTaxaDeEntrega().doubleValue() : 0)) * (venda.getTaxaDeAcrescimo().doubleValue()/100));
			}
			
		} else {
			totalDeAcrescimo = BigDecimal.ZERO;	
		}
		
		faltando = venda.getValorTotalComDesconto().doubleValue();//venda.getFaltando().doubleValue();
		
		venda.setAcrescimo(totalDeAcrescimo);
		
		
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
		
		activeIndex = 2;//configuracao.getAbaPDV();
		
		edit = true;
	}
	
	
	public void ativarLeitor() {
		activeIndex = 1;
		configuracao.setLeitorPDV(leitor);
		configuracao = configuracoes.save(configuracao);
	}
	
		
	public void ativarCupom() {
		configuracao.setCupomAtivado(imprimirCupom);
		configuracao = configuracoes.save(configuracao);
	}
	
	
	public void ativarPDVRapido() {
		configuracao.setPdvRapido(pdvRapido);
		configuracao = configuracoes.save(configuracao);
	}
	
	
	public void changeVias() {
		configuracao.setVias(vias);
		configuracao = configuracoes.save(configuracao);
	}
	
	
	public void finalizar() {
		
		activeIndex = 2; 
		
		if (itensVenda.size() > 0) {			
									
			if(venda.isAjuste()) {
				
				tipoPagamento = null;
				valorRecebido = 0D;
				troco = 0D;
				vendaPaga = true;
				
				salvar_(true);
				
			} else {		
				
				pagamento = new Pagamento();
				
				if(venda.getId() == null) {
					tipoPagamento = TipoPagamento.AVISTA;
					parcelasConfirmadas = false;
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
					
					todasContas = new ArrayList<>();

					valorEntrada = null;
					
					if(entrega) {
						vendaPaga = false;
					}
					
					parcelasConfirmadas = true;
					
					PrimeFaces.current().executeScript("selectTab2();PF('confirmDialog').show();");							
						
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
						
						PrimeFaces.current().executeScript("selectTab2();PF('confirmDialog').show();");
						
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
							
							PrimeFaces.current().executeScript("selectTab2();PF('confirmDialog').show();");
							
						 } else {
							 
							faltando = venda.getValorTotalComDesconto().doubleValue();//vendaTemp.getFaltando().doubleValue();
							troco = 0D;//vendaTemp.getTroco().doubleValue();
						 
							List<ItemDevolucao> listaDevolucao = itensDevolucoes.porVenda(venda);
							if(listaDevolucao.size() == 0) {
								
								PrimeFaces.current().executeScript("selectTab2();PF('confirmDialog').show();");
							
							} else {
								
								PrimeFaces.current().executeScript(
										"swal({ type: 'error', title: 'Ops!', text: 'Não é possível alterar esta venda, já existem devoluções registradas!' });");
							}
					 }
				}				
							
			}
		
		} else {
			
			activeIndex = configuracao.getAbaPDV();
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!', timer: 1500 });");
		}
		
	}
	
	
	
	public void salvarVendaMesa() {
		vendaPaga = false;
		tipoPagamento = null;
		
		if (itensVenda.size() > 0) {
			
			salvar_(false);
			
			if(venda.getId() == null) {
				mesa = new Mesa();
			}
				
			mesas = new ArrayList<Mesa>();
	        for (int i = 1; i <= 10; i++) {
				Mesa mesa = new Mesa();
				mesa.setCodigo("" + i);
				mesa.setNumero("Mesa " + i);
				if(i < 10) {
					mesa.setCodigo("0" + i);
					mesa.setNumero("Mesa 0" + i);
				}
				
				Venda venda = vendas.porStatusMesa(mesa.getCodigo(), null, usuario.getEmpresa());				
				if(venda != null) {
					mesa.setVenda(venda);
					mesa.setStatus("O");
				} else {
					mesa.setStatus("L");
				}
								
				mesas.add(mesa);
			}
			
		} else {
			
			activeIndex = configuracao.getAbaPDV();
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!', timer: 1500 });");
		}
	}
	
	
	
	public void calculaTroco_() {
		
		Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
		faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue() + ((pagamento.getValor() != null) ? pagamento.getValor().doubleValue() : 0));
		
		troco = valorRecebidoTemp.doubleValue() + ((pagamento.getValor() != null) ? pagamento.getValor().doubleValue() : 0) - venda.getValorTotalComDesconto().doubleValue();
		
		if(faltando < 0) {
			troco = -1 * faltando.doubleValue();
			faltando = 0D;			
		}
	}
	
	

	public void salvar() {
		
		calculaTroco_();
		
		if(tipoPagamento == TipoPagamento.AVISTA/* && venda.getFormaPagamento().getNome().equals("Dinheiro")*/) {
		
			if(valorRecebido != null || trocaPendente) {
				
				
				if(venda.getFormaPagamento().getNome().equals("Dinheiro")) {
					
					if(entrega != true) {
						
						if(vendaPaga == true) {
							
							if(faltando.doubleValue() == 0D) {
								
								salvar_(true);
								
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
							salvar_(true);
						}
						
						
					} else {				
							
						if(faltando.doubleValue() == 0D) {
							
							salvar_(true);
							
						} else {
								
							PrimeFaces.current().executeScript(
									"Toast.fire({ " +
									  "icon: 'error', " +
									  "title: 'Não foi possível salvar a venda. Está faltando R$ " + nf.format(faltando) + "!'" +
									"}) ");
						}		
						
					}
					
				} else {
					
					if(vendaPaga == true) {
						
						Double valorPagamento = pagamento.getValor() != null ? pagamento.getValor().doubleValue() : 0D;
						
						if(valorRecebido.doubleValue() > venda.getValorTotalComDesconto().doubleValue()) {
							//PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Ops!', text: 'O valor cobrado é maior que o total a pagar!'});");						
						
							PrimeFaces.current().executeScript(
									"Toast_.fire({ " +
									  "icon: 'error', " +
									  "title: 'O valor cobrado é maior que o total a pagar!'" +
									"}) ");
							
						} else if(valorRecebido.doubleValue() < venda.getValorTotalComDesconto().doubleValue()) {
							//PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Ops!', text: 'O valor informado é menor que o total a pagar!'});");						
							if((faltando.doubleValue() - valorPagamento) > 0) {
								
								PrimeFaces.current().executeScript(
										"Toast.fire({ " +
										  "icon: 'error', " +
										  "title: 'Não foi possível salvar a venda. Está faltando R$ " + nf.format(faltando) + "!'" +
										"}) ");
								
							} else {
								salvar_(true);
							}
							
						} else {
							
							salvar_(true);
							
						}
						
					} else {
						
						salvar_(true);
						
					}				
					
				}
			
				
			} else {
				
				if(entrega != true) {
					
					if(vendaPaga) {
						PrimeFaces.current().executeScript(
								"Toast.fire({ " +
								  "icon: 'error', " +
								  "title: 'Valor recebido deve ser informado.'" +
								"}) ");
					} else {
						salvar_(true);
					}
					
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
			
			salvar_(true);
			
		} else if(tipoPagamento == TipoPagamento.PARCELADO) {
			
			/* Salva venda e gera conta a receber. */
			System.out.println("Salva venda e gera conta a receber.");
			salvar_(true);
		}

	}
	
	
	public void salvar_(boolean pagarConta) {
		
		if (venda.getId() != null) {

			List<Conta> contasTemp = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario.getEmpresa());
			for (Conta conta : contasTemp) {
				contas.remove(conta);
			}

		}
		
		if(pagarConta) {
			venda.setStatusMesa("PAGO");
			mesa = new Mesa();
		}
		
		List<ItemVenda> itensVenda_ = new ArrayList<ItemVenda>();
		
		List<ItemVendaCompra> itensVendaCompraDefault = new ArrayList<ItemVendaCompra>();
		
		if(venda.getId() != null) {
			
			List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
			
			itensVenda_.addAll(itensVenda);
			
			for (ItemVenda itemVenda : itensVenda) {
				
				if(itemVenda.isEstoque()) {
					
					boolean contains = false;
					for (ItemVenda itemVenda_ : this.itensVenda) {
						
						if(itemVenda_.getId() != null) {
							if(itemVenda_.getId().longValue() == itemVenda.getId().longValue()) {
								contains = true;
							}
						}				
					}
					
					
					if(itemVenda.isEstoque()) {
									
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
					
						List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
						for (ItemCompra itemCompra : itensCompra) {
							
							if(venda.isPdv()) {
								
								List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
								
								for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {	
									
									if (itemCompra.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId().longValue()) {
										if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
											
											System.out.println("Quantidade Disponivel: "+itemCompra.getQuantidadeDisponivel().doubleValue());
											System.out.println("Quantidade Retornada: "+itemVenda.getQuantidade().doubleValue());
											
											itemCompra.setQuantidadeDisponivel(new BigDecimal(
													itemCompra.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));
											itensCompras.save(itemCompra);
											
											
											
											/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
											/*
											produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));											
											
											Object[] result = itensCompras.porQuantidadeDisponivel(produto);
											
											if((Long) result[0] != null) {
											
												Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
												
												//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
												
												produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
												
												produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
												
												produto.setCustoTotal((BigDecimal) result[1]);	
											}
											
											produtos.save(produto);
											*/
											
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
					
				} else {
					
					itensVendas.remove(itemVenda);	
				}
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
				/*if(!entrega) {
					venda.setValorRecebido(BigDecimal.ZERO);
					venda.setFaltando(venda.getValorTotalComDesconto());
					venda.setTroco(BigDecimal.ZERO);
				} else {*/
					venda.setValorRecebido(new BigDecimal(valorRecebido != null ? valorRecebido : 0));
					venda.setFaltando(new BigDecimal(faltando));
					venda.setTroco(new BigDecimal(troco));
				//}
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
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getCliente().getBairro().getTaxaDeEntrega() : BigDecimal.ZERO;			
				venda.setTaxaDeEntrega(taxaDeEntrega);
			} else {
				taxaDeEntrega = (venda.getCliente().getBairro() != null && entrega) ? venda.getTaxaDeEntrega() : BigDecimal.ZERO;	
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
		}
		

		venda.setPdv(true);
		venda.setEmpresa(usuario.getEmpresa());
		venda = vendas.save(venda);
		
		List<ItemVenda> itensVendaTemp = new ArrayList<ItemVenda>();
		itensVendaTemp.addAll(itensVenda);

		for (ItemVenda itemVenda : itensVenda) {
			
			if(itemVenda.getId() == null || !itemVenda.isEstoque() || itemVenda.isEstoque()) {
				
				if(itemVenda.getItensVendaCompra() == null) {
					
					for (ItemVenda itemVendaTemp : itensVenda_) {
						if(itemVenda.getId() != null) {
						
							if(itemVenda.getId().longValue() == itemVendaTemp.getId().longValue()) {
								itemVenda.setItensVendaCompra(itemVendaTemp.getItensVendaCompra());
							}
						}
					}			
				}
	
				List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();
				itensVendaCompra = itemVenda.getItensVendaCompra();
				
				/*if(itemVenda.getId() == null) {
					itensVendaCompra = itemVenda.getItensVendaCompra();
				} else {
					itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
				}*/
				
				if(venda.getId() != null) {
					Produto produto = produtos.porId(itemVenda.getProduto().getId());
					Double valorDeCustoUnitario = produto.getCustoMedioUnitario().doubleValue();
					itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));					
				
					if(itemVenda.getProduto().isEstoque()) {
						if(produto.getQuantidadeAtual().doubleValue() <= 0 && valorDeCustoUnitario.doubleValue() <= 0) {
							valorDeCustoUnitario = (itemVenda.getProduto().getQuantidadeAtual().doubleValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()) / itemVenda.getProduto().getQuantidadeAtual().doubleValue();
						}
					}				
					
					double desconto = 0;				
					if(itemVenda.getDesconto() != null) {
						desconto = itemVenda.getDesconto().doubleValue() / 100;
					} else {
						itemVenda.setDesconto(BigDecimal.ZERO);
					}
					
					//itemVenda.setEstoque(produto.isEstoque());
					
					BigDecimal subtotal = BigDecimal.valueOf(
						itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue());					
					itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * desconto)));

					/* Calculo do Lucro em valor e percentual */										
					Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
							* itemVenda.getQuantidade().doubleValue())
					.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
					
				
					itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
							* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue())
							- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));

					itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100)));
					itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));

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
						
					if(itemVenda.getItensVendaCompra() != null) {
						
						for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
							
							itemVendaCompra.setItemVenda(itemVenda);
							
							if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
									.longValue()) {
								if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
										.longValue()) {
									
									//if (itemVendaTemp.getId() == null && venda.getId() == null) {
										
										System.out.println("Saldo: " + itemVendaCompra.getQuantidade().doubleValue() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
										
										System.out.println("itemVendaTemp.getSaldo() > 0L : " + (itemVendaCompra.getQuantidade().doubleValue() > 0L));
										//if(itemVenda.getSaldo() > 0L) {
											
											//if(itemVenda.getSaldo().longValue() <= itemCompraTemp.getQuantidadeDisponivel().longValue()) {
												itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
														itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVendaCompra.getQuantidade().doubleValue()));
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
					}
						
					
					System.out.println(itemCompraTemp.getQuantidadeDisponivel().doubleValue());
					itensCompras.save(itemCompraTemp);
					
				}
				
				/*
				if(itemVenda.getProduto().isEstoque()) {
					if(produto.getQuantidadeAtual().doubleValue() <= 0) {
						produto.setCustoMedioUnitario(BigDecimal.ZERO);
	
					}
					
					
					produtos.save(produto);
				}
				*/
				
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
			
		
		for (ItemVenda itemVenda : itensVenda) {
				
			if(itemVenda.getProduto().isEstoque()) {
				
				if(itemVenda.getId() == null || itemVenda.isEstoque()) {
					for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
						itensVendasCompras.save(itemVendaCompra);
					}
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
				
				
				conta.setSubTotal(venda.getValorTotalComDesconto());
				conta.setCustoMedio(new BigDecimal(valorCompra));
				
				
				if(venda.getDesconto() != null) {
					conta.setLucro(new BigDecimal(lucro - venda.getDesconto().doubleValue()));
				} else {
					conta.setLucro(new BigDecimal(lucro));
				}
				
				
				//conta.setLucro(new BigDecimal(lucro));
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
		
		String download = "", cupom = "";
			
		if (edit != true) {
			
			venda.setStatus(!entrega); 

			if (entrega) {
				entregaVenda.setStatus("PENDENTE");
				entregaVenda.setVenda(venda);
				entregaVenda = entregas.save(entregaVenda);
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
			
			
			if(venda.isVendaPaga() != true) {
				
				if(!entrega) {
					venda.setValorRecebido(BigDecimal.ZERO);
					venda.setFaltando(BigDecimal.ZERO);
					venda.setTroco(BigDecimal.ZERO);
				} else {
					if(valorRecebido != null) venda.setValorRecebido(new BigDecimal(valorRecebido));
					if(faltando != null) venda.setFaltando(new BigDecimal(faltando));
					if(troco != null) venda.setTroco(new BigDecimal(troco));
				}
				
			} else {
				if(valorRecebido != null) venda.setValorRecebido(new BigDecimal(valorRecebido));
				if(faltando != null) venda.setFaltando(new BigDecimal(faltando));
				if(troco != null) venda.setTroco(new BigDecimal(troco));
			}
	
			
			venda.setClientePagouTaxa(venda.getFormaPagamento().isClientePaga());
			
			if(venda.getDesconto() == null) {
				venda.setDesconto(BigDecimal.ZERO);
			}
			
			venda = vendas.save(venda);
			
			
			
			
			
			if(!venda.isConta() && venda.isVendaPaga() && !venda.isAjuste()) {
				
				Caixa caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
				
				if(caixa != null) {
					
					if(valorRecebido.doubleValue() > 0) {
						
						ItemCaixa itemCaixa = new ItemCaixa();
						itemCaixa.setCaixa(caixa);
						itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
						itemCaixa.setData(new Date());
						itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
						itemCaixa.setFormaPagamento(venda.getFormaPagamento());
						itemCaixa.setOperacao(TipoOperacao.VENDA);
						itemCaixa.setTipoPagamento("Entrada");
						
						if(venda.getDesconto() != null) {
							venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
						} else {
							venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
						}
						
						if(!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
				
							itemCaixa.setValor(new BigDecimal(valorRecebido));
							
						} else {
							
							if(valorRecebido.doubleValue() > venda.getValorTotalComDesconto().doubleValue()) {
								itemCaixa.setValor(new BigDecimal(valorRecebido - venda.getTroco().doubleValue()));
							} else {
								itemCaixa.setValor(new BigDecimal(valorRecebido));				
							}
							
							/*if(venda.getDesconto() != null) {
								itemCaixa.setValor(new BigDecimal(valorTotal - venda.getDesconto().doubleValue()));
							} else {
								itemCaixa.setValor(new BigDecimal(valorTotal));
							}*/
							
						}		
						
						itensCaixas.save(itemCaixa);
					}
					
					if(pagamento.getVenda() != null && pagamento.getValor().doubleValue() > 0) {
						
						ItemCaixa itemCaixa = new ItemCaixa();
						itemCaixa.setCaixa(caixa);
						itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
						itemCaixa.setData(new Date());
						itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
						itemCaixa.setFormaPagamento(pagamento.getFormaPagamento());
						itemCaixa.setOperacao(TipoOperacao.VENDA);
						itemCaixa.setTipoPagamento("Entrada");
						
						if(!pagamento.getFormaPagamento().getNome().equals("Dinheiro")) {
				
							itemCaixa.setValor(pagamento.getValor());
							
							/*if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
								itemCaixa.setValor(new BigDecimal(pagamento.getValor().doubleValue() - ((pagamento.getValor().doubleValue() * pagamento.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
							}*/
							
						} else {
							
							if(valorRecebido.doubleValue() < venda.getValorTotalComDesconto().doubleValue()) {
								itemCaixa.setValor(new BigDecimal(venda.getValorTotalComDesconto().doubleValue() - valorRecebido.doubleValue()));
								
								/*if(!venda.getFormaPagamento().isClientePaga() && venda.getFormaPagamento().getAcrescimo().doubleValue() > 0) {
									itemCaixa.setValor(new BigDecimal(itemCaixa.getValor().doubleValue() - ((itemCaixa.getValor().doubleValue() * itemCaixa.getFormaPagamento().getAcrescimo().doubleValue()) / 100)));
								}*/
							}
							
						}		
						
						itensCaixas.save(itemCaixa);
						
						pagamento.setVenda(venda);
						pagamento = pagamentos.save(pagamento);
			
					}
				
				}
				
			}
			

			vendaTemp_ = new Venda();
			vendaTemp_.setNumeroVenda(null);
			vendaTemp_.setTipoVenda(venda.getTipoVenda());
			vendaTemp_.setBairro(venda.getBairro());
			vendaTemp_.setUsuario(venda.getUsuario());	
			
			//Cliente cliente = clientes.porId(1L);
			//vendaTemp_.setCliente(cliente);
			
			if(imprimirCupom && !venda.isAjuste()) {
				cupom += "downloadCupom();";
				vendaTemp_.setId(venda.getId());
				if(pagarConta) {
					emitirCupom();
				} else {
					mesa = new Mesa();
					vendaTemp_.setId(null);
				}
				//imprimirCupom(itensVenda, venda);
			}
			
			//imprimirCupom = false;
			
			if(gerarPDF && !venda.isAjuste()) {
				if(pagarConta) {
					emitirPedido(venda);
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
			
			log.setDescricao("Registrou venda PDV, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
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
			
			filter = new ProdutoFilter();
			produto = new Produto();
			
			totalDeAcrescimo = BigDecimal.ZERO;
			
			activeIndex = configuracao.getAbaPDV();
			
			pagamento = new Pagamento();
			
			if(configuracao.isPopupCliente()) {
				PrimeFaces.current().executeScript("PF('cliente-dialog').show();");
			} else {
				venda.setCliente(clientes.porNome("Nao Informado", usuario.getEmpresa()));
			}

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
			}
			
			/*if(venda.isVendaPaga() != true) {
				venda.setValorRecebido(BigDecimal.ZERO);
				venda.setFaltando(BigDecimal.ZERO);
				venda.setTroco(BigDecimal.ZERO);
			} else {*/
				if(valorRecebido != null) venda.setValorRecebido(new BigDecimal(valorRecebido));
				if(faltando != null) venda.setFaltando(new BigDecimal(faltando));
				if(troco != null) venda.setTroco(new BigDecimal(troco));
			//}
			
			
			venda.setClientePagouTaxa(venda.getFormaPagamento().isClientePaga());
			
			if(venda.getDesconto() == null) {
				venda.setDesconto(BigDecimal.ZERO);
			}
			
			venda = vendas.save(venda);
			
			
			
			
			

			
			//if(caixaAberto) {
				
				List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(venda.getNumeroVenda(), TipoOperacao.VENDA, venda.getEmpresa());

				if(itensCaixa.size() > 0) {
					
					for (ItemCaixa itemCaixaTemp : itensCaixa) {
						itensCaixas.remove(itemCaixaTemp);
					}
					
				} 
				
				
				Pagamento pagamentoTemp = pagamentos.porVenda(venda, venda.getEmpresa());
				if(pagamentoTemp != null) {
					pagamentos.remove(pagamentoTemp);
				}
				
				
				if(pagamento.getId() != null) {
					pagamentos.remove(pagamento);
				} /*else {
					pagamento.setVenda(venda);
				}*/
				
				
					
				
				
				if(!venda.isConta() && venda.isVendaPaga()) {
					
					Caixa caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
					
					if(caixa != null) {
						
						if(valorRecebido.doubleValue() > 0) {
							
							ItemCaixa itemCaixa = new ItemCaixa();
							itemCaixa.setCaixa(caixa);
							itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
							itemCaixa.setData(new Date());
							itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
							itemCaixa.setFormaPagamento(venda.getFormaPagamento());
							itemCaixa.setOperacao(TipoOperacao.VENDA);
							itemCaixa.setTipoPagamento("Entrada");
							
							if(venda.getDesconto() != null) {
								venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
							} else {
								venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
							}
							
							if(!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
					
								itemCaixa.setValor(new BigDecimal(valorRecebido));
								
							} else {
								
								if(valorRecebido.doubleValue() > venda.getValorTotalComDesconto().doubleValue()) {
									itemCaixa.setValor(new BigDecimal(valorRecebido - venda.getTroco().doubleValue()));
								} else {
									itemCaixa.setValor(new BigDecimal(valorRecebido));				
								}
								
								/*if(venda.getDesconto() != null) {
									itemCaixa.setValor(new BigDecimal(valorTotal - venda.getDesconto().doubleValue()));
								} else {
									itemCaixa.setValor(new BigDecimal(valorTotal));
								}*/
								
							}		
							
							itensCaixas.save(itemCaixa);
						}
						
						if(pagamento.getVenda() != null && pagamento.getValor().doubleValue() > 0) {
							
							ItemCaixa itemCaixa = new ItemCaixa();
							itemCaixa.setCaixa(caixa);
							itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
							itemCaixa.setData(new Date());
							itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
							itemCaixa.setFormaPagamento(pagamento.getFormaPagamento());
							itemCaixa.setOperacao(TipoOperacao.VENDA);
							itemCaixa.setTipoPagamento("Entrada");
							
							if(!pagamento.getFormaPagamento().getNome().equals("Dinheiro")) {
					
								itemCaixa.setValor(pagamento.getValor());
								
							} else {
								
								if(valorRecebido.doubleValue() < venda.getValorTotalComDesconto().doubleValue()) {
									itemCaixa.setValor(new BigDecimal(venda.getValorTotalComDesconto().doubleValue() - valorRecebido.doubleValue()));
								}
								
							}		
							
							itensCaixas.save(itemCaixa);
							
							pagamento.setVenda(venda);
							pagamento = pagamentos.save(pagamento);
						}
					
					}
					
				} else if(!venda.isConta() && !venda.isVendaPaga()) {
					
					if(pagamento.getVenda() != null && pagamento.getValor().doubleValue() > 0) {
		
						pagamento.setVenda(venda);
						pagamento = pagamentos.save(pagamento);
					}
					
				}
				
			//}
				
				
				
				if(imprimirCupom && !venda.isAjuste()) {
					cupom += "downloadCupom();";
					vendaTemp_.setId(venda.getId());					
					
					if(pagarConta) {
						emitirCupom();
						//imprimirCupom(itensVenda, venda);
					} else {
						mesa = new Mesa();
						vendaTemp_.setId(null);
					}
				}
				
				//imprimirCupom = false;
				
				if(gerarPDF && !venda.isAjuste()) {
					if(pagarConta) {
						emitirPedido(venda);
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
			
			log.setDescricao("Alterou venda PDV, Nº " + venda.getNumeroVenda() + ", quantidade de itens " + venda.getQuantidadeItens() + ", valor total R$ " + nf.format(venda.getValorTotal()));
			log.setUsuario(usuario);		
			logs.save(log);
				
				
			//PF('confirmDialog').hide();
			PrimeFaces.current().executeScript(download + "PF('confirmDialog').hide();swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
					+ venda.getNumeroVenda() + " atualizada com sucesso!', timer: 5000 });");
			
			activeIndex = 2;//configuracao.getAbaPDV();
		}
	}
	

	public void selecionarProduto(Produto produtoTemp, String quantidade) {
		
		itemVenda = new ItemVenda();		
		itemVenda.setQuantidade(new BigDecimal(1));
		itemVenda.setEstoque(produtoTemp.isEstoque());
		
		
		if(quantidade != null) {
			String quantidadeTemp = quantidade.substring(0, quantidade.length() - 1);
			itemVenda.setQuantidade(new BigDecimal(Double.parseDouble(quantidadeTemp.replaceFirst("0*", ""))/1000));
		}
		
		
		Produto produto = produtos.porId(produtoTemp.getId());
		
		List<Produto> pizzasSource = produtos.porCategoria(produto.getCategoriaProduto(), produto);
        List<Produto> pizzasTarget = new ArrayList<Produto>();
        
        pizzasTarget.add(produto);
        
        pizzas = new DualListModel<Produto>(pizzasSource, pizzasTarget);
        
        produto.setNome(convertToTitleCaseIteratingChars(produto.getNome()));
        produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));
  
        produto.setNomeCompleto(produto.getNome());
        produto.setDescricaoCompleta(produto.getDescricao());		
        
        
        
        if(produto.getNomeCompleto().length() > 35) {
        	produto.setNomeFormatado(convertToTitleCaseIteratingChars(produto.getNomeCompleto().substring(0, 34) + "..."));    			    
		}
		
		if(produto.getDescricaoCompleta().length() > 73) {
			produto.setDescricaoFormatada(convertToTitleCaseIteratingChars(produto.getDescricaoCompleta().substring(0, 72) + "..."));    			    
		}

        
        
        
		
		produto.setDescontoMaximo(produto.getDesconto().longValue());
		produto.setDesconto(new BigDecimal(0));
		produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));
		
		NumberFormat nf;
		//BigDecimal quantidadeAtual = BigDecimal.ZERO;
		if(produto.getUnidadeMedida().equals("Kg") || produto.getUnidadeMedida().equals("Lt")) {
			nf = new DecimalFormat("###,##0.000", REAL);
			//quantidadeAtual = produto.getQuantidadeAtual().setScale(3, BigDecimal.ROUND_HALF_EVEN);		
		} else if(produto.getUnidadeMedida().equals("Pt")) {
			nf = new DecimalFormat("###,##0.0", REAL);
			
		} else {
			nf = new DecimalFormat("###,##0", REAL);
			//quantidadeAtual = produto.getQuantidadeAtual().setScale(2, BigDecimal.ROUND_HALF_EVEN);
		}	
		
		itemVenda.setProduto(produto);
		itemVenda.setCode(produto.getCodigo().concat("_" + new Date().getTime()));
		System.out.println(itemVenda.getCode());

		itensCompra = new ArrayList<ItemCompra>();
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
		
		List<String> itens = new ArrayList<>();
		
		Double quantidadeDisponivel = itensCompras.saldoPorProduto(produto).doubleValue();//produto.getQuantidadeAtual();
		
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
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
	
								quantidadeDisponivel += itemVendaCompra.getQuantidade().doubleValue();
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));	
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
					itemVenda.setSaldo(itemVenda.getQuantidade().doubleValue());
					itens.add(itemVenda.getCode());
				}
				
				if(itemVenda.getItensVendaCompra() != null) {
					
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
									
									System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());

									if(itemVenda.getSaldo() > 0L) {
										
										if(itemVenda.getSaldo() <= itemCompraTemp.getQuantidadeDisponivel().doubleValue()) {
											quantidadeDisponivel -= itemVenda.getSaldo();
											itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
													itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getSaldo()));
											itemVenda.setSaldo(0D);										
											
										} else {	
											quantidadeDisponivel -= itemCompraTemp.getQuantidadeDisponivel().doubleValue();
											itemVenda.setSaldo(itemVenda.getSaldo() - itemCompraTemp.getQuantidadeDisponivel().doubleValue());
											itemCompraTemp.setQuantidadeDisponivel(BigDecimal.ZERO);				
											System.out.println("Saldo: "+itemVenda.getSaldo());
										}
									}
									
									System.out.println("Saldo Atual: "+itemVenda.getSaldo());
									
								//} 

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
						
		}
		
		produto.setQuantidadeAtual(new BigDecimal(quantidadeDisponivel));
		produto.setQuantidadeAtualFormatada(nf.format(produto.getQuantidadeAtual()) + " " + produto.getUnidadeMedida() + ".");
		System.out.println("QuantidadeAtual: "+quantidadeDisponivel);

		if (itensCompra.size() == 0 && produto.isEstoque()) {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!', timer: 1500 });");
		} else {

			System.out.println("Estoque: " + this.itemVenda.getProduto().isEstoque());
			if(produto.isEstoque()) {
				
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
				
				//PrimeFaces.current().executeScript("selectTab1();");
				PrimeFaces.current().executeScript("ajaxDelay('', 0);");
				
				if(pdvRapido) {
					PrimeFaces.current().executeScript("adicionaItem();");
				} else {
					if(produto.getCategoriaProduto().getNome().toLowerCase().contains("pizza")) {
						PrimeFaces.current().executeScript("blink('.blink');");
					}
				}
							
			} else {
				
				//PrimeFaces.current().executeScript("selectTab1();");
				PrimeFaces.current().executeScript("ajaxDelay('', 0);");
				
				if(pdvRapido) {
					PrimeFaces.current().executeScript("adicionaItem();");
				} else {
					if(produto.getCategoriaProduto().getNome().toLowerCase().contains("pizza")) {
						PrimeFaces.current().executeScript("blink('.blink');");
					}
				}
			}
			
			
			
		}
		
		
	}
	
	
	
	
	public void selecionarProduto_(Produto produtoTemp) {
		
		activeIndex = 0;
		
		configuracao.setAbaPDV(activeIndex);
		configuracao = configuracoes.save(configuracao);
		
		if(caixaAberto) {
			
			
			Produto produto = produtos.porId(produtoTemp.getId());
			
			List<Produto> pizzasSource = produtos.porCategoria(produto.getCategoriaProduto(), produto);
	        List<Produto> pizzasTarget = new ArrayList<Produto>();
	        
	        pizzasTarget.add(produto);
	        
	        pizzas = new DualListModel<Produto>(pizzasSource, pizzasTarget);
	        
		
			itemVenda = new ItemVenda();
			produto.setDescontoMaximo(produto.getDesconto().longValue());
			produto.setDesconto(new BigDecimal(0));
			produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));
			
			produto.setNome(convertToTitleCaseIteratingChars(produto.getNome()));
	        produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));
	              
	        produto.setNomeCompleto(produto.getNome());
	        produto.setDescricaoCompleta(produto.getDescricao());	
	        
	        
	        
	        if(produto.getNomeCompleto().length() > 35) {
	        	produto.setNomeFormatado(convertToTitleCaseIteratingChars(produto.getNomeCompleto().substring(0, 34) + "..."));    			    
			}
			
			if(produto.getDescricaoCompleta().length() > 73) {
				produto.setDescricaoFormatada(convertToTitleCaseIteratingChars(produto.getDescricaoCompleta().substring(0, 72) + "..."));    			    
			}
	        
	        
			
			NumberFormat nf;
			//BigDecimal quantidadeAtual = BigDecimal.ZERO;
			if(produto.getUnidadeMedida().equals("Kg") || produto.getUnidadeMedida().equals("Lt")) {
				nf = new DecimalFormat("###,##0.000", REAL);
				//quantidadeAtual = produto.getQuantidadeAtual().setScale(3, BigDecimal.ROUND_HALF_EVEN);		
			} else if(produto.getUnidadeMedida().equals("Pt")) {
				nf = new DecimalFormat("###,##0.0", REAL);
				
			} else {
				nf = new DecimalFormat("###,##0", REAL);
				//quantidadeAtual = produto.getQuantidadeAtual().setScale(2, BigDecimal.ROUND_HALF_EVEN);
			}	
			
			itemVenda.setEstoque(produto.isEstoque());
			itemVenda.setQuantidade(new BigDecimal(1));
			itemVenda.setProduto(produto);
			itemVenda.setCode(produto.getCodigo().concat("_" + new Date().getTime()));
			System.out.println(itemVenda.getCode());
	
			itensCompra = new ArrayList<ItemCompra>();
			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(produto);
			
			List<String> itens = new ArrayList<>();
			
			Double quantidadeDisponivel = itensCompras.saldoPorProduto(produto).doubleValue();//produto.getQuantidadeAtual();
			
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
									System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
		
									quantidadeDisponivel += itemVendaCompra.getQuantidade().doubleValue();
									itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
											itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));	
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
					
					if(!itens.contains(itemVenda.getCode())) {
						itemVenda.setSaldo(itemVenda.getQuantidade().doubleValue());
						itens.add(itemVenda.getCode());
					}
					
					if(itemVenda.getItensVendaCompra() != null) {
						
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
										
										System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
	
										if(itemVenda.getSaldo() > 0L) {
											
											if(itemVenda.getSaldo() <= itemCompraTemp.getQuantidadeDisponivel().doubleValue()) {
												quantidadeDisponivel -= itemVenda.getSaldo();
												itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
														itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getSaldo()));
												itemVenda.setSaldo(0D);										
												
											} else {	
												quantidadeDisponivel -= itemCompraTemp.getQuantidadeDisponivel().doubleValue();
												itemVenda.setSaldo(itemVenda.getSaldo() - itemCompraTemp.getQuantidadeDisponivel().doubleValue());
												itemCompraTemp.setQuantidadeDisponivel(BigDecimal.ZERO);				
												System.out.println("Saldo: "+itemVenda.getSaldo());
											}
										}
										
										System.out.println("Saldo Atual: "+itemVenda.getSaldo());
										
									//} 
	
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
							
			}
			
			produto.setQuantidadeAtual(new BigDecimal(quantidadeDisponivel));
			produto.setQuantidadeAtualFormatada(nf.format(produto.getQuantidadeAtual()) + " " + produto.getUnidadeMedida() + ".");
			System.out.println("QuantidadeAtual: "+quantidadeDisponivel);
	
			if (itensCompra.size() == 0 && produto.isEstoque()) {
				PrimeFaces.current().executeScript(
						"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!', timer: 1500 });");
			} else {
	
				System.out.println("Estoque: " + this.itemVenda.getProduto().isEstoque());
				if(produto.isEstoque()) {
					
					itensCompraTemp = new ArrayList<>();
					for (int i = itensCompra.size() - 1; i >= 0; i--) {
						itensCompra.get(i).setValorUnitarioFormatado(
								"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
						itensCompraTemp.add(itensCompra.get(i));
					}		
					
					
					itensCompra = new ArrayList<>();
					itensCompra.addAll(itensCompraTemp);
					
					if (quantidadeDisponivel < produto.getEstoqueMinimo().longValue()) {					
						PrimeFaces.current().executeScript(
								"swal({ type: 'warning', title: 'Atenção!', text: 'Produto com quantidade abaixo do estoque mínimo!', timer: 1500 });");
					}
				}			
				
			}
			
			PrimeFaces.current().executeScript("ajaxDelay('', 0);");
			
			/*if(itensCompra.size() > 0) {
				itemCompra = itensCompra.stream().sorted((o1, o2)-> o1.getCompra().getDataCompra().compareTo(o2.getCompra().getDataCompra())).findFirst().get();
			}*/
			
			if(pdvRapido) {
				
				if (itensCompra.size() > 0 && produto.isEstoque() || !produto.isEstoque()) {
					PrimeFaces.current().executeScript("adicionaItem();");
				}
				
			} else {
				
				if (itensCompra.size() > 0 && produto.isEstoque() || !produto.isEstoque()) {
					PrimeFaces.current().executeScript("selectTab1();");
					if(produto.getCategoriaProduto().getNome().toLowerCase().contains("pizza")) {
						PrimeFaces.current().executeScript("blink('.blink');");
					}
				}
				
			}
			
		} else {
			filter = new ProdutoFilter();
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Caixa Fechado!', text: 'Para usar o PDV, primeiro você deve abrir o caixa!', timer: 5000 });");
			
		}
	}




	public void adicionarItem() {
		
		activeIndex = configuracao.getAbaPDV();

		//if (venda.getId() == null) {
		

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
	
		itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNome());
		itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricao());	
		
		if(itemVenda.getProduto().getCategoriaProduto().getNome().toLowerCase().contains("pizza")) {
			itemVenda.setPizza(true);
		} else {
			itemVenda.setPizza(false);
		}
		
		
		if(itemVenda.getComposicao() != null && !itemVenda.getComposicao().trim().equals("")) {
        	String[] itens = itemVenda.getComposicao().split(",");
			for (int i = 0; i < itens.length; i++) {
				Produto produto = produtos.porId(Long.parseLong(itens[i]));
				
				itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNomeCompleto() + " | " + produto.getNome());
				itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricaoCompleta() + " | " + produto.getDescricao());		
			}
        }
		
		if(itemVenda.getProduto().getNomeCompleto().length() > 35) {
			itemVenda.getProduto().setNomeFormatado(convertToTitleCaseIteratingChars(itemVenda.getProduto().getNomeCompleto().substring(0, 34) + "..."));    			    
		} 			
		
		if(itemVenda.getProduto().getDescricaoCompleta().length() > 73) {
			itemVenda.getProduto().setDescricaoFormatada(convertToTitleCaseIteratingChars(itemVenda.getProduto().getDescricaoCompleta().substring(0, 72) + "..."));    			    
		}
	
	
			
			/* Método custo médio */
			itemVenda.setValorUnitario(itemVenda.getProduto().getPrecoDeVenda());
			
				
			if(itemVenda.getValorUnitario().doubleValue() > 0) {
				
				Double quantidadeDisponivel = itemVenda.getProduto().getQuantidadeAtual().doubleValue();//itemCompra.getQuantidadeDisponivel();
	
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
	
				System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade().doubleValue());
				System.out.println("quantidadeDisponivel: " + quantidadeDisponivel);
	
				if(itemVenda.getQuantidade().doubleValue() > 0) {
	
					if (itemVenda.getQuantidade().doubleValue() <= quantidadeDisponivel || !itemVenda/*.getProduto()*/.isEstoque()) {
						
						if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt") && !itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {
							venda.setQuantidadeItens(venda.getQuantidadeItens().longValue() + itemVenda.getQuantidade().longValue());
							
						} else {
							venda.setQuantidadeItens(venda.getQuantidadeItens().longValue() + 1);
						}
						
						
						BigDecimal totalDesconto = BigDecimal.ZERO;
						if(itemVenda.getDesconto() != null) {
							totalDesconto = new BigDecimal((itemVenda.getProduto().getPrecoDeVenda().doubleValue() * itemVenda.getQuantidade().doubleValue() * itemVenda.getDesconto().doubleValue()) / 100);
						}
						
						venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() + totalDesconto.doubleValue()));
						
						
							
						
							double desconto = 0;				
							if(itemVenda.getDesconto() != null) {
								desconto = itemVenda.getDesconto().doubleValue() / 100;
							} else {
								itemVenda.setDesconto(BigDecimal.ZERO);
							}
							
							BigDecimal subtotal = BigDecimal.valueOf(
								itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue());					
							itemVenda.setTotal(new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * desconto)));
							
							itemVenda.setVenda(venda);
							
							itemVenda.setSaldo(itemVenda.getQuantidade().doubleValue());
							
							Double valorDeCustoUnitario = itemVenda.getProduto().getCustoMedioUnitario().doubleValue();
							
							if(itemVenda.isEstoque()) {
								
								List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();	
								//itensVendaCompra.addAll(itensVendaCompra_);
								
								Double saldo = itemVenda.getQuantidade().doubleValue();
								for (int i = itensCompra.size() - 1; i >= 0; i--) {
									System.out.println("Saldo: "+saldo);
									if(saldo > 0L) {
										if(saldo.longValue() <= itensCompra.get(i).getQuantidadeDisponivel().doubleValue()) {
											ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
											itemVendaCompra.setItemVenda(itemVenda);
											itemVendaCompra.setItemCompra(itensCompra.get(i));//adicionado, causando nullpointer
											itemVendaCompra.setCompra(itensCompra.get(i).getCompra());
											itemVendaCompra.setQuantidade(new BigDecimal(saldo));
											//itemVendaCompra.setTotal(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().intValue()));
											
											itensVendaCompra.add(itemVendaCompra);
											
											saldo = 0D; 
										} else {
											
											ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
											itemVendaCompra.setItemVenda(itemVenda);
											itemVendaCompra.setItemCompra(itensCompra.get(i));//adicionado, causando nullpointer
											itemVendaCompra.setCompra(itensCompra.get(i).getCompra());
											itemVendaCompra.setQuantidade(itensCompra.get(i).getQuantidadeDisponivel());
											//itemVendaCompra.setTotal(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVendaCompra.getQuantidade().intValue()));
											
											itensVendaCompra.add(itemVendaCompra);
											
											saldo -= itensCompra.get(i).getQuantidadeDisponivel().doubleValue();
										}
									}
								}
								
								//itemVenda.setPercentualLucro(new BigDecimal(itemVenda.getTotal().doubleValue() - (itemVenda.getProduto().getCustoMedioUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue())/ itemVenda.getTotal().doubleValue() * 100));
								
								itemVenda.setItensVendaCompra(itensVendaCompra);				
								//itemVenda.setCompra(itemCompra.getCompra());
							}
		
							/* Calculo do Lucro em valor e percentual */						
							
							Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
									* itemVenda.getQuantidade().doubleValue())
							.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
							
						
							itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
									* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue())
									- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));
		
							itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100)));
							itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));
		
							System.out.println("ValorDeCustoTotal: " + valorDeCustoTotal);
							System.out.println("ValorDeCustoUnitario: " + valorDeCustoUnitario);
							System.out.println("Lucro: " + itemVenda.getLucro());
							System.out.println("PercentualDeLucro: " + itemVenda.getPercentualLucro());
		
							venda.setValorTotal(BigDecimal
									.valueOf(venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));				
							venda.setValorTotalComDesconto(venda.getValorTotal());
		
							itemVenda.setCode(itemVenda.getProduto().getCodigo().concat("_" + new Date().getTime()));
							itensVenda.add(itemVenda);
		
							String code = itemVenda.getCode();
							Produto produto = itemVenda.getProduto();
												
		
							itemVenda = new ItemVenda();
							itemVenda.setCode(code);
							itemVenda.setProduto(produto);
							
							
							BigDecimal totalSemDesconto = BigDecimal.ZERO;
							for (ItemVenda itemVenda : itensVenda) {
								totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()));
							}
							
							Double totalDescontoEmDinheiro = venda.getTotalDescontoEmDinheiro().doubleValue();
							if(totalDescontoEmDinheiro > 0) {
								venda.setTotalDesconto(new BigDecimal((totalDescontoEmDinheiro * 100)/totalSemDesconto.doubleValue()));
							} else {
								venda.setTotalDesconto(BigDecimal.ZERO);
							}
										
							if(itemVenda.isEstoque()) {
								atualizaSaldoLoteDeCompras(produto);
							}
											
							
							/* Nova entrada de produto */
							itemVenda = new ItemVenda();
							itemVenda.getProduto().setMargemLucro(BigDecimal.ZERO);
							this.produto = new Produto();
							filter = new ProdutoFilter();
							
							//calculaAcrescimo();
							confirmaCliente();
							
							if(venda.getId() != null) {
								calculaAcrescimo();
							}
							
							
							
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

		/*} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível adicionar itens à esta venda!', timer: 1500 });");
		}*/
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
					itemVenda.setSaldo(itemVenda.getQuantidade().doubleValue());
					itens.add(itemVenda.getCode());
				}
				
				if(itemVenda.getItensVendaCompra() != null) {
					
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
									
									System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
									
									System.out.println("itemVenda.getSaldo() > 0L : " + (itemVenda.getSaldo() > 0L));
									if(itemVenda.getSaldo() > 0L) {
										
										if(itemVenda.getSaldo() <= itemCompraTemp.getQuantidadeDisponivel().doubleValue()) {
											itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
													itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getSaldo()));
											itemVenda.setSaldo(0D);
											
										} else {			
											itemVenda.setSaldo(itemVenda.getSaldo() - itemCompraTemp.getQuantidadeDisponivel().doubleValue());
											itemCompraTemp.setQuantidadeDisponivel(BigDecimal.ZERO);
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
			
			System.out.println(itemCompraTemp.getQuantidadeDisponivel().doubleValue());
			
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

		//if (venda.getId() == null || itemSelecionado.getId() == null || (itemSelecionado.getId() != null && !itemSelecionado/*.getProduto()*/.isEstoque())) {
				
		
		
		
			
		
		
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
		
		Double quantidadeDisponivel = itensCompras.saldoPorProduto(itemSelecionado.getProduto()).doubleValue(); 
		
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
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemSelecionado.getQuantidade().doubleValue());
	
								quantidadeDisponivel += itemVendaCompra.getQuantidade().doubleValue();
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));	
								//itemVenda.setSaldo(itemVenda.getSaldo().longValue() + itemCompraTemp.getQuantidadeDisponivel().longValue());
								
								System.out.println("Saldo: "+quantidadeDisponivel);
								
								System.out.println("Saldo Atual: "+quantidadeDisponivel);
	
							}
						}
					}	
				}
			}
			
		}
		
		
		
		
		
		
		
			if(!itemSelecionado.getProduto().getUnidadeMedida().equals("Kg") && !itemSelecionado.getProduto().getUnidadeMedida().equals("Lt") && !itemSelecionado.getProduto().getUnidadeMedida().equals("Pt")) {
				venda.setQuantidadeItens(venda.getQuantidadeItens() - itemSelecionado.getQuantidade().longValue());
			} else {
				venda.setQuantidadeItens(venda.getQuantidadeItens() - 1);
			}
			
			
			BigDecimal totalDesconto = BigDecimal.ZERO;
			if(itemVenda.getDesconto() != null) {
				totalDesconto = new BigDecimal((itemSelecionado.getProduto().getPrecoDeVenda().doubleValue() * itemSelecionado.getQuantidade().doubleValue() * itemSelecionado.getDesconto().doubleValue()) / 100);
			}
			
			venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));


			// itemVenda = itemSelecionado;
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));		
			venda.setValorTotalComDesconto(venda.getValorTotal());
			
			itensVenda.remove(itemSelecionado);
			
			if(itemVenda.getProduto() != null) {
				itemVenda.getProduto().setQuantidadeAtual(new BigDecimal(itemVenda.getProduto().getQuantidadeAtual().doubleValue() + itemSelecionado.getQuantidade().doubleValue()));
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
						itemVenda.setSaldo(itemVenda.getQuantidade().doubleValue());
						itens.add(itemVenda.getCode());
					}
					
					if(itemVenda.getItensVendaCompra() != null) {
						
						for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
							
							if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
									.longValue()) {
								if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
										.longValue()) {

									produtoNaLista = true;
									
									//if (itemVenda.getId() == null && venda.getId() == null) {
										
										System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
										
										System.out.println("itemVenda.getSaldo() > 0D : " + (itemVenda.getSaldo() > 0D));
										if(itemVenda.getSaldo() > 0L) {
											
											if(itemVenda.getSaldo() <= itemCompraTemp.getQuantidadeDisponivel().doubleValue()) {
												itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
														itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getSaldo()));
												itemVenda.setSaldo(0D);
												
											} else {			
												itemVenda.setSaldo(itemVenda.getSaldo() - itemCompraTemp.getQuantidadeDisponivel().doubleValue());
												itemCompraTemp.setQuantidadeDisponivel(BigDecimal.ZERO);
												System.out.println("Saldo: "+itemVenda.getSaldo());
											}
										}
										
										quantidadeDisponivel -= itemVendaCompra.getQuantidade().doubleValue();
										
										System.out.println("Saldo Atual: "+itemVenda.getSaldo());
									//}

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
			}

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}
			
			
			BigDecimal totalSemDesconto = BigDecimal.ZERO;
			for (ItemVenda itemVenda : itensVenda) {
				totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()));
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
			
			activeIndex = configuracao.getAbaPDV();
			
			//calculaAcrescimo();
			confirmaCliente();
			
			PrimeFaces.current().executeScript("ajaxDelay('', 0);");
			
			if(venda.getId() != null) {
				aplicarDesconto__();
			}
	
		/*
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível remover este item! Item com estoque controlado.', timer: 3000 });");
		}*/
	}

	public void editarItem() {

		//if (venda.getId() == null || itemSelecionado.getId() == null || (itemSelecionado.getId() != null && !itemSelecionado/*.getProduto()*/.isEstoque())) {
		
		
		List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
		
		Double quantidadeDisponivel = itensCompras.saldoPorProduto(itemSelecionado.getProduto()).doubleValue(); 
		
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
								System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemSelecionado.getQuantidade().doubleValue());
	
								quantidadeDisponivel += itemVendaCompra.getQuantidade().doubleValue();
								itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
										itemCompraTemp.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));	
								//itemVenda.setSaldo(itemVenda.getSaldo().longValue() + itemCompraTemp.getQuantidadeDisponivel().longValue());
								
								System.out.println("Saldo: "+quantidadeDisponivel);
								
								System.out.println("Saldo Atual: "+quantidadeDisponivel);
	
							}
						}
					}	
				}
			}
			
		}
		
		
		
		
			if(!itemSelecionado.getProduto().getUnidadeMedida().equals("Kg") && !itemSelecionado.getProduto().getUnidadeMedida().equals("Lt") && !itemSelecionado.getProduto().getUnidadeMedida().equals("Pt")) {
				venda.setQuantidadeItens(venda.getQuantidadeItens() - itemSelecionado.getQuantidade().longValue());
			} else {
				venda.setQuantidadeItens(venda.getQuantidadeItens() - 1);
			}
			
			
			BigDecimal totalDesconto = BigDecimal.ZERO;
			if(itemSelecionado.getDesconto() != null) {
				totalDesconto = new BigDecimal((itemSelecionado.getProduto().getPrecoDeVenda().doubleValue() * itemSelecionado.getQuantidade().doubleValue() * itemSelecionado.getDesconto().doubleValue()) / 100);
			}
			
			venda.setTotalDescontoEmDinheiro(new BigDecimal(venda.getTotalDescontoEmDinheiro().doubleValue() - totalDesconto.doubleValue()));


			//itemVenda = itemSelecionado;
			itemVenda = new ItemVenda();
			venda.setValorTotal(
					BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			venda.setValorTotalComDesconto(venda.getValorTotal());
			//itensVenda.remove(itemSelecionado);
			
			//itemVenda.setId(itemSelecionado.getId());
			
			itemVenda.setEstoque(itemSelecionado.isEstoque());	
			
			itemVenda.setCode(itemSelecionado.getCode());		
			itemVenda.setProduto(itemSelecionado.getProduto());
			
			
			if(itemSelecionado.getId() != null) {
				itemVenda.setId(itemSelecionado.getId());
			}
			
			
			itemVenda.getProduto().setNome(convertToTitleCaseIteratingChars(itemVenda.getProduto().getNome()));
			itemVenda.getProduto().setDescricao(convertToTitleCaseIteratingChars(itemVenda.getProduto().getDescricao()));	        
			itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNome());
			itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricao());
			
			NumberFormat nf__;
			
			if(itemVenda.getProduto().getUnidadeMedida().equals("Kg") || itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {
				nf__ = new DecimalFormat("###,##0.000", REAL);
		
			} else if(itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {
				nf__ = new DecimalFormat("###,##0.0", REAL);
				
			} else {
				nf__ = new DecimalFormat("###,##0", REAL);
			}
			
			itemVenda.getProduto().setQuantidadeAtualFormatada(nf__.format(quantidadeDisponivel) + " " + itemVenda.getProduto().getUnidadeMedida() + ".");
			itemVenda.getProduto().setDescontoMaximo(itemSelecionado.getProduto().getDescontoMaximo().longValue());
			itemVenda.getProduto().setPrecoDeVenda(itemSelecionado.getValorUnitario());
			
			itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNome());
			itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricao());		
			
			if(itemSelecionado.getComposicao() != null && !itemSelecionado.getComposicao().trim().equals("")) {
	        	String[] itens = itemSelecionado.getComposicao().split(",");
				for (int i = 0; i < itens.length; i++) {
					Produto produto = produtos.porId(Long.parseLong(itens[i]));
					
					itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNomeCompleto() + " | " + produto.getNome());
					itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricaoCompleta() + " | " + produto.getDescricao());		
				}
	        }
			
			if(itemVenda.getProduto().getNomeCompleto().length() > 35) {
				itemVenda.getProduto().setNomeFormatado(convertToTitleCaseIteratingChars(itemVenda.getProduto().getNomeCompleto().substring(0, 34) + "..."));    			    
			} 			
			
			if(itemVenda.getProduto().getDescricaoCompleta().length() > 73) {
				itemVenda.getProduto().setDescricaoFormatada(convertToTitleCaseIteratingChars(itemVenda.getProduto().getDescricaoCompleta().substring(0, 72) + "..."));    			    
			}
			
			
			itemVenda.setQuantidade(itemSelecionado.getQuantidade());
			itemVenda.setLucro(itemSelecionado.getLucro());
			itemVenda.setTotal(itemSelecionado.getTotal());
			itemVenda.setDesconto(itemSelecionado.getDesconto());
			itemVenda.setObservacoes(itemSelecionado.getObservacoes());
			itemVenda.setComposicao(itemSelecionado.getComposicao());
			
			itemVenda.setUpdate(true);
			
			
			itensVenda.remove(itemSelecionado);
			
			

			//List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());
			
			if(itemVenda/*.getProduto()*/.isEstoque()) {

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
							itemVenda.setSaldo(itemVenda.getQuantidade().doubleValue());
							itens.add(itemVenda.getCode());
						}
						
						
						if(itemVenda.getItensVendaCompra() != null) {
							
							for (ItemVendaCompra itemVendaCompra : itemVenda.getItensVendaCompra()) {
								
								if (itemCompraTemp.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
										.longValue()) {
									if (itemCompraTemp.getProduto().getId().longValue() == itemVenda.getProduto().getId()
											.longValue()) {
		
										produtoNaLista = true;
										
										//if (itemVenda.getId() == null && venda.getId() == null) {
											
											System.out.println("Saldo: "+itemVenda.getSaldo() + " - " + itemCompraTemp.getQuantidadeDisponivel().doubleValue());
											
											System.out.println("itemVenda.getSaldo() > 0L : " + (itemVenda.getSaldo() > 0L));
											if(itemVenda.getSaldo() > 0L) {
												
												if(itemVenda.getSaldo() <= itemCompraTemp.getQuantidadeDisponivel().doubleValue()) {
													itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(
															itemCompraTemp.getQuantidadeDisponivel().doubleValue() - itemVenda.getSaldo()));
													itemVenda.setSaldo(0L);
													
												} else {			
													itemVenda.setSaldo(itemVenda.getSaldo() - itemCompraTemp.getQuantidadeDisponivel().doubleValue());
													itemCompraTemp.setQuantidadeDisponivel(BigDecimal.ZERO);
													System.out.println("Saldo: "+itemVenda.getSaldo());
												}
											}
											
											quantidadeDisponivel -= itemVendaCompra.getQuantidade().doubleValue();
											
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
					
					//quantidadeDisponivel += itemCompraTemp.getQuantidadeDisponivel();
				}
			
			
			
					
			
				itemVenda.getProduto().setQuantidadeAtual(new BigDecimal(quantidadeDisponivel));				
				
	
				itensCompraTemp = new ArrayList<>();
				for (int i = itensCompra.size() - 1; i >= 0; i--) {
					itensCompra.get(i).setValorUnitarioFormatado(
							"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
					itensCompraTemp.add(itensCompra.get(i));
				}
				
				
				BigDecimal totalSemDesconto = BigDecimal.ZERO;
				for (ItemVenda itemVenda : itensVenda) {
					totalSemDesconto = new BigDecimal(totalSemDesconto.doubleValue() + (itemVenda.getQuantidade().doubleValue() * itemVenda.getProduto().getPrecoDeVenda().doubleValue()));
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
				
				activeIndex = 1;
		    	configuracao.setAbaPDV(activeIndex);
				configuracao = configuracoes.save(configuracao);
				
				//calculaAcrescimo();
				confirmaCliente();
				
				if(venda.getId() != null) {
					aplicarDesconto__();
				}
				
			} else {
				
				activeIndex = 1;
		    	configuracao.setAbaPDV(activeIndex);
				configuracao = configuracoes.save(configuracao);
			}

		/*
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Não é possível editar este item! Item com estoque controlado.', timer: 3000 });");
		}*/

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
			itemVenda.getProduto().setPrecoDeVenda(itemSelecionado.getValorUnitario());
			System.out.println("Margem: "+itemVenda.getProduto().getMargemLucro());
			System.out.println("Custo medio Un: "+itemVenda.getProduto().getCustoMedioUnitario());
			itemVenda.setQuantidade(itemSelecionado.getQuantidade());
			itemVenda.setDesconto(itemSelecionado.getDesconto());
			itemVenda.setObservacoes(itemSelecionado.getObservacoes());
			itemVenda.setLucro(itemSelecionado.getLucro());
			itemVenda.setTotal(itemSelecionado.getTotal());
			itemVenda.setComposicao(itemSelecionado.getComposicao());
		
			//itemVenda.getProduto().setPrecoDeVenda(itemSelecionado.getProduto().getCustoMedioUnitario());


			List<ItemCompra> itensCompraTemp = itensCompras.porProduto(itemSelecionado.getProduto());

			itensCompra = new ArrayList<ItemCompra>();
			
			Double quantidadeDisponivel = 0D;

			for (ItemCompra itemCompraTemp : itensCompraTemp) {
				itemCompraTemp.getCompra()
						.setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
	
				if (itemCompraTemp.getQuantidadeDisponivel().doubleValue() > 0) {
					itensCompra.add(itemCompraTemp);
				}
		
				quantidadeDisponivel += itemCompraTemp.getQuantidadeDisponivel().doubleValue();
			}
			
			
			itemVenda.getProduto().setQuantidadeAtual(new BigDecimal(quantidadeDisponivel));				
			

			itensCompraTemp = new ArrayList<>();
			for (int i = itensCompra.size() - 1; i >= 0; i--) {
				itensCompra.get(i).setValorUnitarioFormatado(
						"R$ " + nf.format(itensCompra.get(i).getValorUnitario().doubleValue()));
				itensCompraTemp.add(itensCompra.get(i));
			}

			itensCompra = new ArrayList<>();
			itensCompra.addAll(itensCompraTemp);
			
			//activeIndex = configuracao.getAbaPDV();
			activeIndex = 1;
	    	configuracao.setAbaPDV(activeIndex);
			configuracao = configuracoes.save(configuracao);

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
				//faltando = venda.getValorTotalComDesconto().doubleValue();
			} else {
							
				Double valorRecebidoTemp = (valorRecebido != null) ? valorRecebido : 0;
				faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
				
				if(faltando < 0) {
					troco = -1 * faltando.doubleValue();
					faltando = 0D;			
				}	
			}
			
			PrimeFaces.current().executeScript("ocultar();");
			PrimeFaces.current().executeScript("mostrarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
			
		} else {
			
			venda.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario.getEmpresa()));
			parcelasConfirmadas = false;			
			//calculaAcrescimo();
			confirmaCliente();
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
		
		if(/*venda.getFormaPagamento().getNome().equals("Dinheiro") &&*/entrega != true) {
			
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
		faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
		
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
			faltando = venda.getValorTotalComDesconto().doubleValue() - (saldoParaTroca.doubleValue() + valorRecebidoTemp.doubleValue());
			
			if(faltando < 0) {
				troco = -1 * faltando.doubleValue();
				faltando = 0D;			
			}	
		}
		
		valorRecebido = null;
		faltando = venda.getValorTotalComDesconto().doubleValue();
		troco = 0D;
		
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
	
				/* Calculo do Lucro em valor e percentual 										
				Double valorDeCustoTotal = new BigDecimal(valorDeCustoUnitario
						* itemVenda.getQuantidade().doubleValue())
				.setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
				*/
				
			
				itemVenda.setLucro(new BigDecimal(((itemVenda.getValorUnitario().doubleValue() - valorDeCustoUnitario.doubleValue()) / itemVenda.getValorUnitario().doubleValue())
						* (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue())
						- (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue()) * desconto));
	
				itemVenda.setPercentualLucro(new BigDecimal(((itemVenda.getTotal().doubleValue() - (valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue())) / itemVenda.getTotal().doubleValue() * 100)));
				itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));
	
				
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
		
		Double valorVenda = venda.getValorTotal().doubleValue() - descontos;
		Double taxaEntrega = venda.getCliente().getBairro().getTaxaDeEntrega().doubleValue();
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
	
	
	
	public void aplicarDesconto__() {
		
		if(venda.getDesconto() != null && venda.getDesconto().doubleValue() >= 0) {
			
			if(venda.getDesconto().doubleValue() <= venda.getValorTotal().doubleValue()) {

				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
				
			} else {
				venda.setValorTotalComDesconto(venda.getValorTotal());
			}
						
		} else {
			venda.setValorTotalComDesconto(venda.getValorTotal());
		}
		
		calculaAcrescimo();
	}
	
	
	
	public void aplicarDesconto() {
		
		if(venda.getDesconto() != null && venda.getDesconto().doubleValue() >= 0) {
			
			if(venda.getDesconto().doubleValue() <= venda.getValorTotal().doubleValue()) {

				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
				
			} else {
				venda.setDesconto(null);
				venda.setValorTotalComDesconto(venda.getValorTotal());
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor menor ou igual ao valor da venda!', timer: 3000 });");
			}
						
		} else {

			if(venda.getDesconto() != null) {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor maior que R$ 0,00!', timer: 3000 });");
			}	
			
			venda.setDesconto(null);
			venda.setValorTotalComDesconto(venda.getValorTotal());
		}
		
		calculaAcrescimo();
	}


	public void aplicarDesconto_() {
		
		if(venda.getDesconto() != null && venda.getDesconto().doubleValue() >= 0) {
			
			if(venda.getDesconto().doubleValue() <= venda.getValorTotal().doubleValue()) {
	
				venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
				finalizar();
				
			} else {
				venda.setDesconto(null);
				venda.setValorTotalComDesconto(venda.getValorTotal());
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor menor ou igual ao valor da venda!', timer: 3000 });");
			}
						
		} else {
	
			if(venda.getDesconto() != null) {				
				venda.setDesconto(null);
				venda.setValorTotalComDesconto(venda.getValorTotal());
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Informe um valor maior que R$ 0,00!', timer: 3000 });");
			} else {
				venda.setDesconto(null);
				venda.setValorTotalComDesconto(venda.getValorTotal());
				finalizar();
			}	
			
		}	
		
		calculaAcrescimo();
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
	
	
	@SuppressWarnings("unused")
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
        
        String quantidade = "";

        for (ItemVenda itemVenda : itensVenda) {
        	
        	if(!itemVenda.getProduto().getUnidadeMedida().equals("Kg") && !itemVenda.getProduto().getUnidadeMedida().equals("Lt")) {				
        		NumberFormat nf = new DecimalFormat("###,##0", REAL);
        		quantidade = nf.format(itemVenda.getQuantidade().doubleValue());
        	} else if(!itemVenda.getProduto().getUnidadeMedida().equals("Pt")) {				
        		NumberFormat nf = new DecimalFormat("###,##0.0", REAL);
        		quantidade = nf.format(itemVenda.getQuantidade().doubleValue());
        	} else {
        		NumberFormat nf = new DecimalFormat("###,##0.000", REAL);  
        		quantidade = nf.format(itemVenda.getQuantidade().doubleValue());
        	}
        	
        	conteudoImprimir += itemVenda.getProduto().getCodigoDeBarras() + "    "
                    + quantidade + "     "
                    + nf.format(itemVenda.getValorUnitario().doubleValue()) + "    "
                    + itemVenda.getProduto().getDescricao() + "\n\r";
		}
         String contato = "";
        if(usuario.getEmpresa().getContato() != null) {
        	contato = "CONTATO " + usuario.getEmpresa().getContato() + "\n\r";
        }
        
        
        if(venda.getDesconto() != null) {
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
		} else {
			venda.setValorTotalComDesconto(venda.getValorTotal());
		}
        
        
        String mensagemCustomizada = ""; //maxlenght 45
        
        String pTexto = 
                  usuario.getEmpresa().getNome() + "\n\r"
                + (usuario.getEmpresa().getEndereco() != null ? usuario.getEmpresa().getEndereco() : "") + " " + (usuario.getEmpresa().getNumero() != null ? usuario.getEmpresa().getNumero() : "") + "\n\r"
                + (usuario.getEmpresa().getBairro() != null ? usuario.getEmpresa().getBairro() : "")  + ", " + (usuario.getEmpresa().getCidade() != null ? usuario.getEmpresa().getCidade() : "") + " - " + (usuario.getEmpresa().getUf() != null ? usuario.getEmpresa().getUf() : "") + "\n\r"
                + contato
                + "Data: " + data + "             Hora: " + hora +"\n\r"
                + "NUMERO DA VENDA: " + numeroVenda +             "\n\r"
                + "------------------------------------------------\n\r"
                + "                CUPOM NAO FISCAL                \n\r"
                + "------------------------------------------------\n\r"
                + "COD  QTD   PRECO   DESCRICAO                    \n\r"
                + conteudoImprimir +                              "\n\r"
                + "------------------------------------------------\n\r"
                + "SUBTOTAL    R$ " + nf.format(venda.getValorTotal().doubleValue()) + "\n\r"
                
				+ "ACRESCIMO   R$ " + nf.format(venda.getAcrescimo().doubleValue()) + "\n\r"
				
				+ "TX. ENTREGA R$ " + nf.format(venda.getTaxaDeEntrega().doubleValue()) + "   " + "VALOR RECEBIDO R$ " + nf.format(venda.getValorRecebido().doubleValue()) + "\n\r"

				+ "DESCONTO    R$ " + nf.format(venda.getTotalDescontoEmDinheiro().doubleValue() + ((venda.getDesconto() != null) ? venda.getDesconto().doubleValue() : 0))   +"     " + "       TROCO R$ " + nf.format(venda.getTroco().doubleValue())+ "\n\r"
           
                + "VALOR TOTAL R$ " + nf.format(venda.getValorTotalComDesconto().doubleValue()) + "\n\r"
                + "------------------------------------------------\n\r"
                + mensagemCustomizada
                + "            OBRIGADO E VOLTE SEMPRE!          \n\r"
                + "\n\r\n\r\n\r\n\r"
                
                
        ;
               
        imprimir(pTexto);
        imprimir_(pTexto);
    }

    /**
     * Função para imprimir
     *
     * @param pTexto
     */
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
                PrintJob.print(documentoTexto, printerAttributes);
                //tenta imprimir
            } catch (Exception e) {
                e.printStackTrace();
                //Mensarem de erro
            }
            
            prin.close();
            
        } catch (Exception e) {
        	
        	e.printStackTrace();
        }
    }
    
    
    /**
     * Função para imprimir em byte
     *
     * @param pTexto
     */
    public void imprimir_(String pTexto) {
    	
    	System.out.println(pTexto);
	
		try {
			
			PDDocument documento = PDDocument.load(pTexto.getBytes());
			PrintService servico = PrintServiceLookup.lookupDefaultPrintService();

			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PDFPageable(documento));
			job.setPrintService(servico);
			job.print();
			documento.close();
			
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (PrinterException e) {
			e.printStackTrace();
		}
			
    }
    
	
	public void emitirCupom(/*Venda venda*/) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");//hh:mm:ss

		Venda venda = vendas.porId(vendaTemp_.getId());
		
		Pagamento pagamento = pagamentos.porVenda(venda, venda.getEmpresa());
		
		EspelhoVenda pedido = new EspelhoVenda();
		pedido.setVendaNum(venda.getNumeroVenda() + "");
		pedido.setTipoVenda(venda.getTipoVenda().getDescricao().toUpperCase());
		
		if(usuario.getEmpresa().getLogoRelatorio() != null) {
			
			InputStream targetStream;
			try {
				targetStream = ByteSource.wrap(usuario.getEmpresa().getLogoRelatorio()).openStream();
				pedido.setLogo(targetStream);
				
			} catch (IOException e1) {
			}	
		}
		
		pedido.setVendaPaga(venda.isConta() ? "Y" : "N");
		
		pedido.setBairro(venda.getBairro().getNome().toUpperCase());
		
		pedido.setDataVenda(sdf.format(venda.getDataVenda()));
		pedido.setVendedor(venda.getUsuario().getNome().toUpperCase());
		
		pedido.setCliente(venda.getCliente().getNome());
		if(!venda.getCliente().getNome().equals("Nao Informado")) {
			pedido.setCpfCnpj(venda.getCliente().getDocumento());
			pedido.setTelefone(venda.getCliente().getContato());
			pedido.setEndereco(venda.getCliente().getEndereco());
			pedido.setBairro(venda.getCliente().getBairro().getNome());
		}		

		Entrega entrega = entregas.porVenda(venda);
		if (entrega != null) {
			pedido.setResponsavel(entrega.getNome());
			
			pedido.setLocalizacao(entrega.getLocalizacao());
			
			pedido.setObservacao(entrega.getObservacao());
		}
		
		NumberFormat nf_ = new DecimalFormat("###,##0.000", REAL);
		NumberFormat nf__ = new DecimalFormat("###,##0", REAL);
		NumberFormat nf___ = new DecimalFormat("###,##0.0", REAL);
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemEspelhoVendaProdutos itemPedido = new ItemEspelhoVendaProdutos();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			//itemPedido.setDescricao(itemVenda.getProduto().getDescricao());
			itemPedido.setUN(itemVenda.getProduto().getUnidadeMedida());
			
			if(itemVenda.getComposicao() != null && !itemVenda.getComposicao().trim().equals("")) {
	        	String[] itens = itemVenda.getComposicao().split(",");
				for (int i = 0; i < itens.length; i++) {
					Produto produto = produtos.porId(Long.parseLong(itens[i]));
					
					itemVenda.getProduto().setNome(itemVenda.getProduto().getNome() + " | " + produto.getNome());
					if(itemVenda.getProduto().getNome().length() > 35) {
						itemVenda.getProduto().setNomeFormatado(convertToTitleCaseIteratingChars(itemVenda.getProduto().getNome().substring(0, 34) + "..."));    			    
					}
					
					itemVenda.getProduto().setDescricao(itemVenda.getProduto().getDescricao() + " | " + produto.getDescricao());
					if(itemVenda.getProduto().getDescricao().length() > 73) {
						itemVenda.getProduto().setDescricaoFormatada(convertToTitleCaseIteratingChars(itemVenda.getProduto().getDescricao().substring(0, 72) + "..."));    			    
					}
				}
	        }
			
			if(!itemVenda.getProduto().getNumeracao().equals("")) {
				itemVenda.getProduto().setNome(itemVenda.getProduto().getNome() + " (" + itemVenda.getProduto().getNumeracao() + ")");		
			}
			
			String quantidade = "0";
			if(itemVenda.getProduto().getUnidadeMedida().equals("Kg") || itemVenda.getProduto().getUnidadeMedida().equals("Lt"))  {
				quantidade = nf_.format(itemVenda.getQuantidade().doubleValue());
			} else if(itemVenda.getProduto().getUnidadeMedida().equals("Pt"))  {
				quantidade = nf___.format(itemVenda.getQuantidade().doubleValue());
			} else {
				quantidade = nf__.format(itemVenda.getQuantidade());
			}
			
			itemPedido.setDescricao((itemVenda.getProduto().getCodigoDeBarras() != null ? itemVenda.getProduto().getCodigoDeBarras().trim() : itemVenda.getProduto().getCodigo().trim()) + " " + (itemVenda.getComposicao() != null && !itemVenda.getComposicao().trim().equals("") || itemVenda.getProduto().getCategoriaProduto().getNome().toLowerCase().contains("pizza") ? itemVenda.getProduto().getNome().trim() : itemVenda.getProduto().getDescricao().trim()) 
					 + " " + quantidade.trim() + " " + itemPedido.getUN().trim() + " x " + nf.format(itemVenda.getValorUnitario().doubleValue()).trim());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().doubleValue()));
			itemPedido.setQuantidade(String.valueOf(itemVenda.getQuantidade().doubleValue()));
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			itemPedido.setObservacao(itemVenda.getObservacoes());
			
			pedido.getItensPedidos().add(itemPedido);
		}
		
		
		/*
		List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(venda.getNumeroVenda(), TipoOperacao.VENDA, venda.getEmpresa());

		if(itensCaixa.size() > 0) {
			for (ItemCaixa itemCaixa : itensCaixa) {
				
				ItemEspelhoVendaPagamentos itemPagamento = new ItemEspelhoVendaPagamentos();		
				itemPagamento.setTipo(itemCaixa.getTipoPagamento());
				itemPagamento.setValor(nf.format(itemCaixa.getValor()));

				pedido.getItensPagamentos().add(itemPagamento);
			}
		}*/
		
		
		if(venda.getValorRecebido().doubleValue() > 0) {
			
			ItemEspelhoVendaPagamentos itemPagamento = new ItemEspelhoVendaPagamentos();		
			itemPagamento.setFormaPagamento(pagamento.getFormaPagamento().getNome());
			itemPagamento.setValor(nf.format(venda.getValorRecebido()));

			pedido.getItensPagamentos().add(itemPagamento);
	
		}
		
		if(pagamento != null) {
			
			ItemEspelhoVendaPagamentos itemPagamento = new ItemEspelhoVendaPagamentos();		
			itemPagamento.setFormaPagamento(pagamento.getFormaPagamento().getNome());
			itemPagamento.setValor(nf.format(pagamento.getValor()));

			pedido.getItensPagamentos().add(itemPagamento);
			
		}
		
		
		
	
		
		pedido.setxNome(usuario.getEmpresa().getNome() != null ? usuario.getEmpresa().getNome() : "");
		pedido.setCNPJ(usuario.getEmpresa().getCnpj() != null ? usuario.getEmpresa().getCnpj() : "");
		pedido.setxLgr(usuario.getEmpresa().getEndereco() != null ? usuario.getEmpresa().getEndereco().trim() : "");
		pedido.setNro(usuario.getEmpresa().getNumero() != null ? usuario.getEmpresa().getNumero().trim() : "");
		pedido.setxBairro(usuario.getEmpresa().getBairro() != null ? usuario.getEmpresa().getBairro().trim() : "");
		pedido.setxMun(usuario.getEmpresa().getCidade() != null ? usuario.getEmpresa().getCidade() : "");
		pedido.setUF(usuario.getEmpresa().getUf() != null ? usuario.getEmpresa().getUf() : "");
		pedido.setContato(usuario.getEmpresa().getContato() != null ? usuario.getEmpresa().getContato() : "");

		pedido.setTotalVenda(nf.format(venda.getValorTotalComDesconto()));
		pedido.setTotalItens(String.valueOf(venda.getQuantidadeItens()));
		pedido.setDataVenda(sdf.format(venda.getDataVenda()));
		
		pedido.setSubTotal(nf.format(new BigDecimal(venda.getValorTotal().doubleValue() - totalDeAcrescimo.doubleValue() - venda.getTaxaDeEntrega().doubleValue())));
		pedido.setAcrescimo(nf.format(totalDeAcrescimo.doubleValue() + venda.getTaxaDeEntrega().doubleValue()));
		
		
		if(venda.getDesconto() != null) {
			pedido.setValorAPagar(nf.format(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue())));
			pedido.setDesconto(nf.format(venda.getTotalDescontoEmDinheiro().doubleValue() + venda.getDesconto().doubleValue()));
		} else {
			pedido.setValorAPagar(nf.format(venda.getValorTotal()));
			pedido.setDesconto(nf.format(venda.getTotalDescontoEmDinheiro().doubleValue()));
		}
		
		pedido.setTroco(nf.format(venda.getTroco()));
		
		List<EspelhoVenda> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
		try {
			
			for (int i = 0; i < vias; i++) {
				report.getRelatorio(pedidos, "Venda-N" + venda.getNumeroVenda().longValue());
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		vendaTemp_.setId(null);
	}
	
	
	public void onColumnToggle(ToggleEvent event) {
	    final Visibility visibility = event.getVisibility();
	    final FacesMessage msg = new FacesMessage();

	    msg.setSummary("Column index " + event.getData().toString() + " toggled");
	    msg.setDetail("Visibility: " + visibility);
	    FacesContext.getCurrentInstance().addMessage(null, msg);
	    
	    System.out.println("Column index " + event.getData().toString() + " toggled");
	    System.out.println("Visibility: " + visibility);
	}
	
	
	private EspelhoVenda pedido;
	private List<EspelhoVenda> pedidos;
	
	@SuppressWarnings("deprecation")
	public void emitirPedido(Venda venda) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		pedido = new EspelhoVenda();
		pedido.setVendaNum(venda.getNumeroVenda() + "");
		pedido.setTipoVenda(venda.getTipoVenda().getDescricao().toUpperCase());
		pedido.setBairro(venda.getCliente().getBairro().getNome().toUpperCase());
		pedido.setDataVenda(sdf.format(venda.getDataVenda()));
		pedido.setVendedor(venda.getUsuario().getNome().toUpperCase());
		
		if(!venda.getCliente().getNome().equals("Nao Informado")) {
			pedido.setCliente(venda.getCliente().getNome());
		} else {
			pedido.setCliente("-");
		}

		Entrega entrega = entregas.porVenda(venda);
		if (entrega != null) {
						
			pedido.setResponsavel(venda.getCliente().getNome());
			
			pedido.setLocalizacao(venda.getCliente().getBairro().getNome());
			if(venda.getCliente().getEndereco() != null) {
				pedido.setLocalizacao(venda.getCliente().getEndereco() + ", " + venda.getCliente().getBairro().getNome());
			}
			
			pedido.setObservacao(entrega.getObservacao());
			pedido.setTelefone(venda.getCliente().getContato());
			
			if(entrega.getStatus() == StatusPedido.ENTREGUE.name()) {
				pedido.setEntrega("Y");
			}
		}
	
		NumberFormat nf_ = new DecimalFormat("###,##0.000", REAL);
		NumberFormat nf__ = new DecimalFormat("###,##0", REAL);
		NumberFormat nf___ = new DecimalFormat("###,##0.0", REAL);
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemEspelhoVendaProdutos itemPedido = new ItemEspelhoVendaProdutos();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			itemPedido.setDescricao(itemVenda.getProduto().getDescricao().trim());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().doubleValue()));
			
			String quantidade = "0";
			if(itemVenda.getProduto().getUnidadeMedida().equals("Kg") || itemVenda.getProduto().getUnidadeMedida().equals("Lt"))  {
				quantidade = nf_.format(itemVenda.getQuantidade().doubleValue());
			} else if(itemVenda.getProduto().getUnidadeMedida().equals("Pt"))  {
				quantidade = nf___.format(itemVenda.getQuantidade().doubleValue());
			} else {
				quantidade = nf__.format(itemVenda.getQuantidade());
			}
			
			itemPedido.setQuantidade(quantidade);
			
			itemPedido.setUN(itemVenda.getProduto().getUnidadeMedida());
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}

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
				
		if(venda.getDesconto() != null) {
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
		} else {
			venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
		}
		
		pedido.setTotalVenda(nf.format(venda.getValorTotalComDesconto()));
		
		pedido.setFrete(nf.format(venda.getTaxaDeEntrega()));

		if(venda.getDesconto() != null) {
			pedido.setSubTotal(nf.format(venda.getValorTotal().doubleValue() - venda.getTaxaDeEntrega().doubleValue()));
			pedido.setDesconto(nf.format(venda.getDesconto()));
		} else {
			pedido.setSubTotal(nf.format(venda.getValorTotal().doubleValue() - venda.getTaxaDeEntrega().doubleValue()));
		}

		pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
		try {
			fileContent = new DefaultStreamedContent();
			
			byte[] stream = report.getRelatorio__(pedidos, "Venda-N" + venda.getNumeroVenda().longValue());
			
			InputStream targetStream = ByteSource.wrap(stream).openStream();

			fileContent =  new DefaultStreamedContent(targetStream, "application/pdf", "Venda-N" + venda.getNumeroVenda().longValue() + ".pdf");
			
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	
	public StreamedContent getTempPdfFile() {	 	
	 	return fileContent;
	 }
	
	
	public String getImageContentsAsBase64() {
		return Base64.getEncoder().encodeToString(itemVenda.getProduto().getFoto());
	}
	
	public String convertToTitleCaseIteratingChars(String text) {
	    if (text == null || text.isEmpty()) {
	        return text;
	    }
	 
	    StringBuilder converted = new StringBuilder();
	 
	    boolean convertNext = true;
	    for (char ch : text.toCharArray()) {
	        if (Character.isSpaceChar(ch)) {
	            convertNext = true;
	        } else if (convertNext) {
	            ch = Character.toTitleCase(ch);
	            convertNext = false;
	        } else {
	            ch = Character.toLowerCase(ch);
	        }
	        converted.append(ch);
	    }
	 
	    return converted.toString();
	}

	public List<Cliente> getTodosClientes() {
		return todosClientes;
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

	public StreamedContent getFileContent() {
		return fileContent;
	}
	
	public void listarProdutos01() {

		filterProdutos01.setEmpresa(usuario.getEmpresa());
		
		listaDeProdutos01 = new ArrayList<>();
		listaDeProdutos01 = produtos.filtrados(filterProdutos01);

		for (Produto produto : listaDeProdutos01) {
			produto.setNome(convertToTitleCaseIteratingChars(produto.getNome()));
	        produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));
			produto.setQuantidadeAtual(new BigDecimal(itensCompras.quantidadeDisponivelPorProduto(produto).doubleValue()));
			produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));
		}
	}
	
	public void listarProdutos02() {

		filterProdutos02.setEmpresa(usuario.getEmpresa());
		
		listaDeProdutos02 = new ArrayList<>();
		listaDeProdutos02 = produtos.filtrados(filterProdutos02);

		for (Produto produto : listaDeProdutos02) {
			produto.setNome(convertToTitleCaseIteratingChars(produto.getNome()));
	        produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));			
			produto.setQuantidadeAtual(new BigDecimal(itensCompras.quantidadeDisponivelPorProduto(produto).doubleValue()));
			produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));
		}
	}

	public List<Produto> getListaDeProdutos01() {
		return listaDeProdutos01;
	}

	public List<Produto> getListaDeProdutos02() {
		return listaDeProdutos02;
	}
	
	public String getImageContentsAsBase64(Produto produto) {
		produto = produtos.porId(produto.getId());
		return Base64.getEncoder().encodeToString(produto.getFoto());
	}

	public List<CategoriaProduto> getTodasCategoriasProdutos() {
		return todasCategoriasProdutos;
	}

	public ProdutoFilter getFilterProdutos01() {
		return filterProdutos01;
	}

	public void setFilterProdutos01(ProdutoFilter filterProdutos01) {
		this.filterProdutos01 = filterProdutos01;
	}

	public ProdutoFilter getFilterProdutos02() {
		return filterProdutos02;
	}

	public void setFilterProdutos02(ProdutoFilter filterProdutos02) {
		this.filterProdutos02 = filterProdutos02;
	}
	
	public void changeCategoria01() {
		
		filterProdutos01.setTamanho(null);		
		filterProdutos01.setUnidade(null);
			
		configuracao.setTamanho01(null);
		configuracao.setUnidade01(null);
		
		configuracao.setCategoriaProduto01(filterProdutos01.getCategoriaProduto());
		configuracao = configuracoes.save(configuracao);	
		
		tamanhos01 = produtos.tamanhos(usuario.getEmpresa(), configuracao.getCategoriaProduto01());
		
		unidades01 = produtos.unidades(usuario.getEmpresa(), configuracao.getCategoriaProduto01());
		
		listarProdutos01();
		
		if(!keyword_produto1.trim().equals("")) {
			List<Produto> listaDeProdutos01_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos01) {
				if(produto.getNome().toLowerCase().contains(keyword_produto1.toLowerCase())) {
					listaDeProdutos01_Temp.add(produto);
				}
			}
			
			listaDeProdutos01 = new ArrayList<Produto>();
			listaDeProdutos01.addAll(listaDeProdutos01_Temp);
		}
	}
	
	public void changeTamanho01() {
		
		configuracao.setTamanho01(filterProdutos01.getTamanho());
		configuracao = configuracoes.save(configuracao);	
		
		listarProdutos01();
		
		if(!keyword_produto1.trim().equals("")) {
			List<Produto> listaDeProdutos01_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos01) {
				if(produto.getNome().toLowerCase().contains(keyword_produto1.toLowerCase())) {
					listaDeProdutos01_Temp.add(produto);
				}
			}
			
			listaDeProdutos01 = new ArrayList<Produto>();
			listaDeProdutos01.addAll(listaDeProdutos01_Temp);
		}
	}

	public void changeUnidade01() {
		
		configuracao.setUnidade01(filterProdutos01.getUnidade());
		configuracao = configuracoes.save(configuracao);	
		
		listarProdutos01();
		
		if(!keyword_produto1.trim().equals("")) {
			List<Produto> listaDeProdutos01_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos01) {
				if(produto.getNome().toLowerCase().contains(keyword_produto1.toLowerCase())) {
					listaDeProdutos01_Temp.add(produto);
				}
			}
			
			listaDeProdutos01 = new ArrayList<Produto>();
			listaDeProdutos01.addAll(listaDeProdutos01_Temp);
		}
	}
	
	public void changeCategoria02() {
		
		filterProdutos02.setTamanho(null);		
		filterProdutos02.setUnidade(null);
			
		configuracao.setTamanho02(null);
		configuracao.setUnidade02(null);
		
		configuracao.setCategoriaProduto02(filterProdutos02.getCategoriaProduto());
		configuracao = configuracoes.save(configuracao);
		
		tamanhos02 = produtos.tamanhos(usuario.getEmpresa(), configuracao.getCategoriaProduto02());
		
		unidades02 = produtos.unidades(usuario.getEmpresa(), configuracao.getCategoriaProduto02());
		
		listarProdutos02();
		
		if(!keyword_produto2.trim().equals("")) {
			List<Produto> listaDeProdutos02_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos02) {
				if(produto.getNome().toLowerCase().contains(keyword_produto2.toLowerCase())) {
					listaDeProdutos02_Temp.add(produto);
				}
			}
			
			listaDeProdutos02 = new ArrayList<Produto>();
			listaDeProdutos02.addAll(listaDeProdutos02_Temp);
		}
	}
	
	public void changeTamanho02() {
		
		configuracao.setTamanho02(filterProdutos02.getTamanho());
		configuracao = configuracoes.save(configuracao);	
		
		listarProdutos02();
		
		if(!keyword_produto2.trim().equals("")) {
			List<Produto> listaDeProdutos02_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos02) {
				if(produto.getNome().toLowerCase().contains(keyword_produto2.toLowerCase())) {
					listaDeProdutos02_Temp.add(produto);
				}
			}
			
			listaDeProdutos02 = new ArrayList<Produto>();
			listaDeProdutos02.addAll(listaDeProdutos02_Temp);
		}
	}

	public void changeUnidade02() {
		
		configuracao.setUnidade02(filterProdutos02.getUnidade());
		configuracao = configuracoes.save(configuracao);	
		
		listarProdutos02();
		
		if(!keyword_produto2.trim().equals("")) {
			List<Produto> listaDeProdutos02_Temp = new ArrayList<Produto>();
			
			for (Produto produto : listaDeProdutos02) {
				if(produto.getNome().toLowerCase().contains(keyword_produto2.toLowerCase())) {
					listaDeProdutos02_Temp.add(produto);
				}
			}
			
			listaDeProdutos02 = new ArrayList<Produto>();
			listaDeProdutos02.addAll(listaDeProdutos02_Temp);
		}
	}
	

	public List<Usuario> getTodosEntregadores() {
		return todosEntregadores;
	}

	public List<String> getTamanhos01() {
		return tamanhos01;
	}

	public List<String> getUnidades01() {
		return unidades01;
	}
	
	public List<String> getTamanhos02() {
		return tamanhos02;
	}

	public List<String> getUnidades02() {
		return unidades02;
	}

	public boolean isPdvRapido() {
		return pdvRapido;
	}

	public void setPdvRapido(boolean pdvRapido) {
		this.pdvRapido = pdvRapido;
	}

	public int getVias() {
		return vias;
	}

	public void setVias(int vias) {
		this.vias = vias;
	}
	
	
    public DualListModel<Produto> getPizzas() {
        return pizzas;
    }
 
    public void setPizzas(DualListModel<Produto> pizzas) {
        this.pizzas = pizzas;
    }
    
    
    public void confirmaComposicao() {
    	System.out.println("Produto: " + itemVenda.getProduto().getNome());
    	System.out.println("Composições:");
    	
    	activeIndex = 1;
    	configuracao.setAbaPDV(activeIndex);
		configuracao = configuracoes.save(configuracao);
    	
    	List<Produto> pizzasTarget = new ArrayList<Produto>();
    	
    	//Produto produto_ = produtos.porId(itemVenda.getProduto().getId());
    	//itemVenda.setProduto(produto_);
    	//itemVenda.getProduto().setNome("");
    	//itemVenda.getProduto().setDescricao("");
    	itemVenda.getProduto().setPrecoDeVenda(BigDecimal.ZERO);
		itemVenda.setComposicao("");
		
		itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNome());
		itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricao());
		itemVenda.getProduto().setNomeFormatado("");
		itemVenda.getProduto().setDescricaoFormatada("");
    	
    	int cont_pizzas = 0;
    	for (Object id : pizzas.getTarget()) {
    		Produto produto = produtos.porId(Long.parseLong(id.toString()));
    		
    			
    		if(!pizzasTarget.contains(produto)) {
    			
    			if(itemVenda.getProduto().getId().longValue() != produto.getId().longValue()) {
    	    		  			
	    			if(!itemVenda.getProduto().getDescricaoCompleta().equals("")) {
	    				
	    				
	    				itemVenda.getProduto().setNomeCompleto(itemVenda.getProduto().getNomeCompleto() + " | " + produto.getNome());
	    				itemVenda.getProduto().setDescricaoCompleta(itemVenda.getProduto().getDescricaoCompleta() + " | " + produto.getDescricao());		
	    			
	    			} else {
	    				itemVenda.getProduto().setNomeCompleto(produto.getNome());
	    				itemVenda.getProduto().setDescricaoCompleta(produto.getDescricao());	    			
	    			}
	    			
	    			
	    			if(itemVenda.getProduto().getId().longValue() != produto.getId().longValue()) {
	    				if(itemVenda.getComposicao() != null && !itemVenda.getComposicao().trim().equals("")) {
		    				itemVenda.setComposicao(itemVenda.getComposicao() + "," + id.toString());
		    			} else {
		    				itemVenda.setComposicao(id.toString());
		    			}
	    			} 		
    			}
    			
    			itemVenda.getProduto().setPrecoDeVenda(new BigDecimal(itemVenda.getProduto().getPrecoDeVenda().doubleValue() + produto.getPrecoDeVenda().doubleValue()));
    			
    			cont_pizzas++;
    		}
		}
    	
    	if(itemVenda.getProduto().getNomeCompleto().length() > 35) {
			itemVenda.getProduto().setNomeFormatado(convertToTitleCaseIteratingChars(itemVenda.getProduto().getNomeCompleto().substring(0, 34) + "..."));    			    
		}
		
		if(itemVenda.getProduto().getDescricaoCompleta().length() > 73) {
			itemVenda.getProduto().setDescricaoFormatada(convertToTitleCaseIteratingChars(itemVenda.getProduto().getDescricaoCompleta().substring(0, 72) + "..."));    			    
		}
    	
    	if(cont_pizzas > 0) {
    		if(cont_pizzas == 1) {
    			Produto produto_ = produtos.porId(Long.parseLong(String.valueOf((pizzas.getTarget().get(0)))));
        		itemVenda.setProduto(produto_);
        		itemVenda.setComposicao("");
        		itemVenda.getProduto().setNomeCompleto(produto_.getNome());
    			itemVenda.getProduto().setDescricaoCompleta(produto_.getDescricao());
        	} else {
        		itemVenda.getProduto().setPrecoDeVenda(new BigDecimal(itemVenda.getProduto().getPrecoDeVenda().doubleValue() / cont_pizzas));       	
        	}
    	} else {
    		Produto produto_ = produtos.porId(itemVenda.getProduto().getId());
    		itemVenda.setProduto(produto_);
    		itemVenda.setComposicao("");
    		itemVenda.getProduto().setNomeCompleto(produto_.getNome());
			itemVenda.getProduto().setDescricaoCompleta(produto_.getDescricao());
    	}
    		
    }

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
		
	}
	
	public void preparaNovoCliente() {
		cliente = new Cliente();
				
		if(usuario.getEmpresa().getId() == 7111 || usuario.getEmpresa().getId() == 7112) {
			cliente.setBairro(bairros.porId(3008L));
		} else {
			cliente.setBairro(bairros.porNome("Nao Informado", usuario.getEmpresa()));
		}
	}
    
	public void salvarCliente() {
		clientes.save(cliente);
		PrimeFaces.current().executeScript("PF('cliente-dialog_cliente').hide();");
		
		PrimeFaces.current().executeScript(
				"Toast_.fire({ " +
				  "icon: 'success', " +
				  "title: 'Cliente salvo com sucesso!'" +
				"}) ");
	}
	
	public void prepararComposicao() {
		
		Produto produto = produtos.porId(itemVenda.getProduto().getId());
		
		produto.setDescricaoFormatada(convertToTitleCaseIteratingChars(produto.getCodigo() + " | " + produto.getNome() + (produto.getNumeracao() != null && !produto.getNumeracao().equals("") ? ", " + produto.getNumeracao() : "") + ", R$ " + nf.format(produto.getPrecoDeVenda())));

		System.out.println(produto.getCodigo());
		
		List<Produto> pizzasSource = produtos.porCategoria(produto.getCategoriaProduto(), produto);
		
		for (Produto produtoTemp : pizzasSource) {
			produtoTemp.setDescricaoFormatada(convertToTitleCaseIteratingChars(produtoTemp.getCodigo() + " | " + produtoTemp.getNome() + (produto.getNumeracao() != null && !produto.getNumeracao().equals("") ? ", " + produto.getNumeracao() : "") + ", R$ " + nf.format(produtoTemp.getPrecoDeVenda())));
		}
		
        List<Produto> pizzasTarget = new ArrayList<Produto>();
        pizzasTarget.add(produto);
        
        if(itemVenda.getComposicao() != null && !itemVenda.getComposicao().trim().equals("")) {
        	String[] itens = itemVenda.getComposicao().split(",");
			for (int i = 0; i < itens.length; i++) {
				Produto produtoTemp = produtos.porId(Long.parseLong(itens[i]));
				pizzasTarget.add(produtoTemp);	
				
				if(pizzasSource.contains(produtoTemp)) {
					pizzasSource.remove(produtoTemp);
				}
			}
        }
        	        
		
		pizzas = new DualListModel<Produto>(pizzasSource, pizzasTarget);
		
	}

	public Mesa getMesa() {
		return mesa;
	}


	public List<Mesa> getMesas() {
		return mesas;
	}


	public Pagamento getPagamento() {
		return pagamento;
	}


	public void setPagamento(Pagamento pagamento) {
		this.pagamento = pagamento;
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
	
	public void adicionarPagamento() {	
		
		valorRecebido = valorRecebido != null ? valorRecebido : 0;
		faltando = venda.getValorTotalComDesconto().doubleValue() - (valorRecebido != null ? valorRecebido : 0);
		
		if(faltando.doubleValue() > 0) {
			
			if(pagamento.getValor().doubleValue() > 0) {
				
				if(pagamento.getFormaPagamento().getNome().equals("Dinheiro")) {
					
					if(pagamento.getValor().doubleValue() >= faltando.doubleValue()) {
						
						pagamento.setVenda(venda);
						
						//faltando = venda.getValorTotal().doubleValue() - (valorRecebido != null ? valorRecebido : 0);
						
						if(pagamento.getValor().doubleValue() >= faltando.doubleValue()) {
							faltando = 0D;
							troco = (pagamento.getValor().doubleValue() + (valorRecebido != null ? valorRecebido : 0)) - venda.getValorTotalComDesconto().doubleValue();
						} else {
							faltando = faltando - pagamento.getValor().doubleValue();
							troco = 0D;
						}
						
								
						
						PrimeFaces.current().executeScript(
								"PF('pagamento-dialog').hide();"
								+ "Toast_.fire({ " +
								  "icon: 'success', " +
								  "title: 'Pagamento adicionado!'" +
								"}) ");					
						
					} else {
						PrimeFaces.current().executeScript(
								"Toast_.fire({ " +
								  "icon: 'error', " +
								  "title: 'O valor informado é menor que o valor que está faltando!'" +
								"}) ");
					}
										
				} else {
					
					if(pagamento.getValor().doubleValue() > faltando.doubleValue()) {
						//PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Ops!', text: 'O valor informado é maior que o valor que está faltando!'});");						
						
						PrimeFaces.current().executeScript(
								"Toast_.fire({ " +
								  "icon: 'error', " +
								  "title: 'O valor informado é maior que o valor que está faltando!'" +
								"}) ");
					
					} else if(pagamento.getValor().doubleValue() < faltando.doubleValue()) {
						//PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Ops!', text: 'O valor informado é menor que o valor que está faltando!'});");						
					
						PrimeFaces.current().executeScript(
								"Toast_.fire({ " +
								  "icon: 'error', " +
								  "title: 'O valor informado é menor que o valor que está faltando!'" +
								"}) ");
						
					} else {
						pagamento.setVenda(venda);
						
						faltando = 0D;
						troco = 0D;
						
						PrimeFaces.current().executeScript(
								"PF('pagamento-dialog').hide();"
								+ "Toast_.fire({ " +
								  "icon: 'success', " +
								  "title: 'Pagamento adicionado!'" +
								"}) ");
					}
				}		
				
			} else {
				
				pagamento.setVenda(null);
				//faltando = venda.getValorTotalComDesconto().doubleValue();
				troco = 0D;
				PrimeFaces.current().executeScript(
						"PF('pagamento-dialog').hide();");
			}
			
		} else {
			faltando = 0D;
			//PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Ops!', text: 'Não é possível adicionar pagamento!'});");		
		
			PrimeFaces.current().executeScript(
					"Toast_.fire({ " +
					  "icon: 'error', " +
					  "title: 'Não é possível adicionar pagamento!'" +
					"}) ");
		}	
		
	}
	
	
	public void abrirCaixa() {
		
		caixa.setDataAbertura(new Date());
				
		if(caixa.getSaldoInicial() == null) {
			caixa.setSaldoInicial(BigDecimal.ZERO);
		}
		
		caixa.setUsuario(usuario);
		caixa.setEmpresa(usuario.getEmpresa());
		
		caixa = caixas.save(caixa);
		
		caixaAberto = true;
		
		ItemCaixa itemCaixa = new ItemCaixa();
		itemCaixa.setCaixa(caixa);
		itemCaixa.setData(caixa.getDataAbertura());
		itemCaixa.setDescricao("SALDO INICIAL");
		itemCaixa.setValor(caixa.getSaldoInicial());
		itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario.getEmpresa()));
		itemCaixa.setTipoPagamento("Entrada");
		
		itemCaixa = itensCaixas.save(itemCaixa);
		
			
		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Pronto!', text: 'O caixa foi aberto com sucesso!', timer: 1500 });");
	}


	public Caixa getCaixa() {
		return caixa;
	}


	public void setCaixa(Caixa caixa) {
		this.caixa = caixa;
	}


	public boolean isCaixaAberto() {
		return caixaAberto;
	}


	public String getKeyword_produto1() {
		return keyword_produto1;
	}


	public void setKeyword_produto1(String keyword_produto1) {
		this.keyword_produto1 = keyword_produto1;
	}


	public String getKeyword_produto2() {
		return keyword_produto2;
	}


	public void setKeyword_produto2(String keyword_produto2) {
		this.keyword_produto2 = keyword_produto2;
	}


	public Configuracao getConfiguracao() {
		return configuracao;
	}
   
}
