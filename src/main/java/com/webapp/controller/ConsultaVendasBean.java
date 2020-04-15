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

import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaVendasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Venda> vendasFiltradas = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;

	@Inject
	private Vendas vendas;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private Produtos produtos;

	private Venda vendaSelecionada;
	
	private Long numeroVenda;

	private Date dateStart = new Date();

	private Date dateStop = new Date();

	private NumberFormat nf = new DecimalFormat("###,##0.00");

	private String totalVendas = "0,00";

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

		vendasFiltradas = vendas.vendasFiltradas(numeroVenda, dateStart, calendarioTemp.getTime(), usuario);

		double totalVendasTemp = 0;
		totalItens = 0;
		for (Venda venda : vendasFiltradas) {
			totalVendasTemp += venda.getValorTotal().doubleValue();
			totalItens += venda.getQuantidadeItens().intValue();
		}

		totalVendas = nf.format(totalVendasTemp);
	}

	public void excluir() {
		
		if(vendaSelecionada != null) {
			
			List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
			for (ItemVenda itemVenda : itensVenda) {
				Produto produto = itemVenda.getProduto();
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemVenda.getQuantidade());
				produtos.save(produto);

				itensVendas.remove(itemVenda);
			}

			vendas.remove(vendaSelecionada);

			vendaSelecionada = null;
			
			pesquisar();
			PrimeFaces.current()
					.executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda excluída com sucesso!' });");
			
			
		} else {
			
			for (Venda venda : vendasFiltradas) {
				
				venda = vendas.porId(venda.getId());
				
				List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
				
				for (ItemVenda itemVenda : itensVenda) {
					Produto produto = itemVenda.getProduto();
					produto = produtos.porId(produto.getId());
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemVenda.getQuantidade());
					produtos.save(produto);

					itensVendas.remove(itemVenda);
				}

				vendas.remove(venda);
			}
			
			pesquisar();
			PrimeFaces.current()
					.executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda excluída com sucesso!' });");
		
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

	public Integer getTotalItens() {
		return totalItens;
	}

	public Long getNumeroVenda() {
		return numeroVenda;
	}

	public void setNumeroVenda(Long numeroVenda) {
		this.numeroVenda = numeroVenda;
	}

}
