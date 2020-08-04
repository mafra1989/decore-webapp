package com.webapp.controller;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Produto;
import com.webapp.repository.Produtos;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class CarrinhoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Produto> listaDeProdutos = new ArrayList<Produto>();

	@Inject
	private Produtos produtos;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
	
	private String totalGeralEmString = "R$ 0,00";
	
	private Long totalDeItens = 0L;
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {			
			System.out.println("Iniciou carrinho . . .");
		}
	}
	
	public void atualizarCarrinho() {
		
		List<Produto> produtos = new ArrayList<>();	
		
		totalDeItens = 0L;
		Double totalGeral = 0D;
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
	}
	
	public void adicionarNoCarrinho(Produto produto) throws IOException {
		
		if(!listaDeProdutos.contains(produto)) {
			produto.setQuantidadePedido(1L);
			listaDeProdutos.add(produto);
		} else {
			Produto produtoTemp = listaDeProdutos.get(listaDeProdutos.indexOf(produto));
			produtoTemp.setQuantidadePedido(produtoTemp.getQuantidadePedido() + 1L);
		}
		
		try {
			Thread.sleep(2000);
			FacesContext.getCurrentInstance().getExternalContext().redirect("/webstore/decore/cart.xhtml");
			
		} catch (InterruptedException e) {
		}
		
		totalDeItens = 0L;
		Double totalGeral = 0D;
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
		Double totalGeral = 0D;
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
	
	

}
