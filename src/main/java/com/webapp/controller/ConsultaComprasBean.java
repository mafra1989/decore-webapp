package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private List<Compra> comprasFiltradas = new ArrayList<>();
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Compras compras;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private ItensCompras itensCompras;
	
	@Inject
	private Produtos produtos;

	
	private Compra compraSelecionada;
	
	private Date dateStart = new Date();
	
	private Date dateStop = new Date();
	
	
	private NumberFormat nf = new DecimalFormat("###,##0.00");
	
	private String totalCompras = "0,00";
	
	private Integer totalItens = 0;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {	
			todosUsuarios = usuarios.todos();
		}
	}
	
	public void pesquisar() { 	
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		comprasFiltradas = compras.comprasFiltradas(dateStart, calendarioTemp.getTime(), usuario);
		
		double totalComprasTemp = 0;
		totalItens = 0;
		for (Compra compra : comprasFiltradas) {
			totalComprasTemp += compra.getValorTotal().doubleValue();
			totalItens += compra.getQuantidadeItens().intValue();
		}
		
		totalCompras = nf.format(totalComprasTemp);
	}
	
	public void excluir() { 	
		
		List<ItemVenda> itensVenda = itensVendas.porCompra(compraSelecionada);
		
		if(itensVenda.size() == 0) {		
	
			List<ItemCompra> itensCompra = itensCompras.porCompra(compraSelecionada);
			for (ItemCompra itemCompra : itensCompra) {
				Produto produto = itemCompra.getProduto();
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
				produto.setQuantidadeItensComprados(produto.getQuantidadeItensComprados() - itemCompra.getQuantidade());
				produtos.save(produto);
				
				itensCompras.remove(itemCompra);
			}	
			
			compras.remove(compraSelecionada);
			
			compraSelecionada = null;
			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Compra excluída com sucesso!' });");
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Existem itens dessa compra já vinculados a uma ou mais vendas!' });");
		}
		
		/*
		for (Compra compra : comprasFiltradas) {
			List<ItemVenda> itensVenda = itensVendas.porCompra(compra);
			
			if(itensVenda.size() == 0) {		
		
				List<ItemCompra> itensCompra = itensCompras.porCompra(compra);
				for (ItemCompra itemCompra : itensCompra) {
					Produto produto = itemCompra.getProduto();
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
					produto.setQuantidadeItensComprados(produto.getQuantidadeItensComprados() - itemCompra.getQuantidade());
					produtos.save(produto);
					
					itensCompras.remove(itemCompra);
				}	
				
				compras.remove(compra);
			}
		}
		
		pesquisar();
		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Compras excluídas com sucesso!' });");
		*/
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

}
