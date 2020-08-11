package com.webapp.controller;

import java.io.IOException;
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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.webapp.mail.configuration.AppConfig;
import com.webapp.mail.model.CustomerInfo;
import com.webapp.mail.model.ProductOrder;
import com.webapp.mail.service.OrderService;
import com.webapp.model.Bairro;
import com.webapp.model.ItemPedido;
import com.webapp.model.Pedido;
import com.webapp.model.Produto;
import com.webapp.repository.Bairros;
import com.webapp.repository.ItensPedidos;
import com.webapp.repository.Pedidos;
import com.webapp.repository.filter.BairroFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class CarrinhoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Produto> listaDeProdutos = new ArrayList<Produto>();

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
	
	private String totalGeralEmString = "R$ 0,00";
	
	private Long totalDeItens = 0L;
	
	private Double totalGeral = 0D;
	
	@Inject
	private Pedido pedido;
	
	@Inject
	private Bairros bairros;
	
	@Inject
	private Pedidos pedidos;
	
	@Inject
	private ItensPedidos itensPedidos;
	
	
	private String paymentMethodId;
	
	private Integer Installments;
	
	private Integer token;
	
	private static String TEST_ACCESS_TOKEN = "TEST-1852237905175376-080820-c7142e78a910796fbc26999ef2fa808d-277250128";
	
	private Preference preference;
	
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {			
		}
	}
		
	public List<Bairro> completeText(String query) {
		
        BairroFilter filtro = new BairroFilter();
        filtro.setNome(query);
        
        List<Bairro> listaDeBairros = bairros.filtrados(filtro);       
        
        return listaDeBairros;
    }	
	
	
	public void pagarComMercadoPago() throws IOException, MPException {

		/*
		 
		Map<String, String> requestParamMap = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap();

		String token = requestParamMap.get("token") != null ? requestParamMap.get("token") : "";


		
		
		MercadoPago.SDK.setAccessToken(TEST_ACCESS_TOKEN);

		Payment payment = new Payment();
		payment.setTransactionAmount(totalGeral.floatValue())
		       .setToken(token)
		       .setDescription("Compras na Decore Web Store")
		       .setInstallments(Installments)
		       .setPaymentMethodId(paymentMethodId)
		       .setPayer(new Payer()
		         .setEmail(pedido.getEmail()));

		payment.save();

		System.out.println(payment.getStatus());
		
		if(payment.getStatus() != null && payment.getStatus().equals(Payment.Status.approved)) {
			
			sendMailAndSavePedido();
			
		} else {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Pagamento não realizado.' });");			
		}
		
		
		PrimeFaces.current().ajax().update("form");	
		
		*/
		
		
		Long totalDeItens = 0L;
		Double totalGeral = 0D;
		for (Item item : preference.getItems()) {	
			totalDeItens += item.getQuantity().intValue();
			totalGeral += item.getUnitPrice().doubleValue() * item.getQuantity().intValue();
		}
		
		
		if(this.totalDeItens.intValue() == totalDeItens.intValue() && this.totalGeral.doubleValue() == totalGeral.doubleValue()) {
			//sendMailAndSavePedido();
			PrimeFaces.current().executeScript("start__();");
			
		} else {
			
			// Configura credenciais
			MercadoPago.SDK.setAccessToken(TEST_ACCESS_TOKEN);	

			// Cria um objeto de preferência
			preference = new Preference();

			// Cria um itens na preferência
			for (Produto produto : listaDeProdutos) {
				Item item = new Item();
				item.setTitle(produto.getDescricaoConvertida())
				    .setQuantity(produto.getQuantidadePedido().intValue())
				    .setUnitPrice(produto.getPrecoDeVenda().floatValue());
				preference.appendItem(item);			
			}
			
			com.mercadopago.resources.datastructures.preference.Payer payer = new com.mercadopago.resources.datastructures.preference.Payer()
	        .setEmail(pedido.getEmail());
			preference.setPayer(payer);
				
			preference.save();
						
			PrimeFaces.current().executeScript("stop__();atualizaPedido();");
		}	
		
	}
	
	
	private void sendMailAndSavePedido() {
					
		pedido.setDataPedido(new Date());
		pedido.setQuantidadeItens(totalDeItens);
		pedido.setValorTotal(new BigDecimal(totalGeral));
		
		pedido.setEmail(pedido.getEmail().toLowerCase().trim());
		pedido.setNome(convertToTitleCaseIteratingChars(pedido.getNome().trim()));
		
		pedido.setLucro(new BigDecimal(0));
		pedido.setPercentualLucro(new BigDecimal(0));
		
		pedido.setEmpresa("Decore");
		pedido.setStatus("AGUARDANDO");		
		
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(pedido.getDataPedido());
		
		pedido.setDia(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
		pedido.setNomeDia(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
		pedido.setSemana(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
		pedido.setMes(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
		pedido.setAno(Long.valueOf((calendario.get(Calendar.YEAR))));
		
		pedido = pedidos.save(pedido);
		
		for (Produto produto : listaDeProdutos) {
			ItemPedido itemPedido = new ItemPedido();
			itemPedido.setQuantidade(produto.getQuantidadePedido());
			itemPedido.setValorUnitario(produto.getPrecoDeVenda());
			itemPedido.setTotal(new BigDecimal(produto.getQuantidadePedido().intValue() * produto.getPrecoDeVenda().doubleValue()));
			itemPedido.setProduto(produto);
			itemPedido.setPedido(pedido);
			
			itensPedidos.save(itemPedido);
		}
		
		
		
		
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(
				AppConfig.class);

		OrderService orderService = (OrderService) context.getBean("orderService");
		
		boolean emailEnviado = orderService.sendOrderConfirmation(getDummyOrder());
		
		((AbstractApplicationContext) context).close();
		
		
		
		
		
		pedido.setEmailenviado(emailEnviado);
		pedidos.save(pedido);
		
		
		
		
		
		pedido = new Pedido();
		
		listaDeProdutos = new ArrayList<Produto>();
		
		atualizarCarrinho();
		
		PrimeFaces.current().executeScript("pedidoEnviado();");
		
	}
	
	
	public void realizarPagamento() throws IOException, MPException {
		
		// Configura credenciais
		MercadoPago.SDK.setAccessToken(TEST_ACCESS_TOKEN);	

		// Cria um objeto de preferência
		preference = new Preference();

		// Cria um itens na preferência
		for (Produto produto : listaDeProdutos) {
			Item item = new Item();
			item.setTitle(produto.getDescricaoConvertida())
			    .setQuantity(produto.getQuantidadePedido().intValue())
			    .setUnitPrice(produto.getPrecoDeVenda().floatValue());
			preference.appendItem(item);			
		}
		
		com.mercadopago.resources.datastructures.preference.Payer payer = new com.mercadopago.resources.datastructures.preference.Payer()
        .setEmail(pedido.getEmail());
		preference.setPayer(payer);
			
		preference.save();

		PrimeFaces.current().executeScript("pagamento();");
				
	}
	
	public ProductOrder getDummyOrder()
	{
		ProductOrder order = new ProductOrder();
		order.setOrderId(String.valueOf(pedido.getId()));
		//order.setProductName("Thinkpad Laptop");
		//order.setStatus("Confirmed");
	
		CustomerInfo customerInfo = new CustomerInfo();
		customerInfo.setName(convertToTitleCaseIteratingChars(pedido.getNome().trim()));
		//customerInfo.setAddress("25, West Street, Bangalore");
		customerInfo.setEmail(pedido.getEmail().toLowerCase().trim());
		order.setCustomerInfo(customerInfo);
		
		System.out.println(customerInfo.getName());
		System.out.println(customerInfo.getEmail());
		
		return order;
	}
	
	public void finalizarPedido() throws IOException, MPException {
		
		atualizarCarrinho();
		
		if(totalDeItens > 0) {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/webstore/decore/checkout.xhtml");
		} else {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/webstore/decore/cart.xhtml");
		}
				
	}
	
	public void atualizarCarrinho() {
		
		List<Produto> produtos = new ArrayList<>();	
		
		totalDeItens = 0L;
		totalGeral = 0D;
		for (Produto produtoTemp : listaDeProdutos) {
	
			if(produtoTemp.getQuantidadePedido() == null || produtoTemp.getQuantidadePedido().intValue() == 0) {
				produtos.add(produtoTemp);
			} else {
				produtoTemp.setDescricao(convertToTitleCaseIteratingChars(produtoTemp.getDescricao()));
				totalGeral += produtoTemp.getPrecoDeVenda().doubleValue() * produtoTemp.getQuantidadePedido().intValue();
				totalDeItens += produtoTemp.getQuantidadePedido().intValue();
			}
		}
		
		for (Produto produto : produtos) {
			listaDeProdutos.remove(produto);
		}
		
		totalGeralEmString = nf.format(totalGeral.doubleValue());
		
		try {
			Thread.sleep(1500);
			
		} catch (InterruptedException e) {
		}
	}
	
	public void adicionarNoCarrinho(Produto produto) throws IOException {
		
		if(!listaDeProdutos.contains(produto)) {
			produto.setQuantidadePedido(produto.getQuantidadePedido());
			listaDeProdutos.add(produto);
		} else {
			Produto produtoTemp = listaDeProdutos.get(listaDeProdutos.indexOf(produto));
			produtoTemp.setQuantidadePedido(produtoTemp.getQuantidadePedido() + produto.getQuantidadePedido());
		}
		
		try {
			Thread.sleep(2000);
			FacesContext.getCurrentInstance().getExternalContext().redirect("/webstore/decore/cart.xhtml");
			
		} catch (InterruptedException e) {
		}
		
		totalDeItens = 0L;
		totalGeral = 0D;
		for (Produto produtoTemp : listaDeProdutos) {
			produtoTemp.setDescricao(convertToTitleCaseIteratingChars(produtoTemp.getDescricao()));
			totalGeral += produtoTemp.getPrecoDeVenda().doubleValue() * produtoTemp.getQuantidadePedido().intValue();
			totalDeItens += produtoTemp.getQuantidadePedido().intValue();
		}
		
		totalGeralEmString = nf.format(totalGeral.doubleValue());
	}
	
	public void removerDoCarrinho(Produto produto) throws IOException {
		Produto produtoTemp = listaDeProdutos.get(listaDeProdutos.indexOf(produto));
		listaDeProdutos.remove(produtoTemp);
		
		totalDeItens = 0L;
		totalGeral = 0D;
		for (Produto produtoTemp_ : listaDeProdutos) {
			produtoTemp_.setDescricao(convertToTitleCaseIteratingChars(produtoTemp_.getDescricao()));
			totalGeral += produtoTemp_.getPrecoDeVenda().doubleValue() * produtoTemp_.getQuantidadePedido().intValue();
			totalDeItens += produtoTemp_.getQuantidadePedido().intValue();
		}
		
		totalGeralEmString = nf.format(totalGeral.doubleValue());
		
		try {
			Thread.sleep(2000);
			
		} catch (InterruptedException e) {
		}
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

	public List<Produto> getListaDeProdutos() {
		return listaDeProdutos;
	}

	public String getTotalGeralEmString() {
		return totalGeralEmString;
	}

	public Long getTotalDeItens() {
		return totalDeItens;
	}


	public Pedido getPedido() {
		return pedido;
	}


	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
	
	
	public Double getTotalGeral() {
		return totalGeral;
	}

	public String getPaymentMethodId() {
		return paymentMethodId;
	}

	public void setPaymentMethodId(String paymentMethodId) {
		this.paymentMethodId = paymentMethodId;
	}

	public Integer getInstallments() {
		return Installments;
	}

	public void setInstallments(Integer installments) {
		Installments = installments;
	}

	public Integer getToken() {
		return token;
	}

	public void setToken(Integer token) {
		this.token = token;
	}

	public Preference getPreference() {
		return preference;
	}

}
