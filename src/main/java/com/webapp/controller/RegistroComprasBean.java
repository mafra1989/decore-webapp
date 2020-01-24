package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RegistroComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Compra compra;

	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Produtos produtos;
	
	@Inject
	private Compras compras;
	
	@Inject
	private ItensCompras itensCompras;

	private List<Usuario> todosUsuarios;
	
	private List<Produto> produtosFiltrados;
	
	@Inject
	private ItemCompra itemCompra;

	private List<ItemCompra> itensCompra = new ArrayList<ItemCompra>();
	
	private ProdutoFilter filter = new ProdutoFilter();
	
	private ItemCompra itemSelecionado;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todosUsuarios = usuarios.todos();
		}
	}
	
	public void pesquisar() {
		produtosFiltrados = produtos.filtrados(filter);
		System.out.println(produtosFiltrados.size());
	}

	public void salvar() {
		
		if(itensCompra.size() > 0) {
			
			Long totalDeItens = 0L;
			double valorTotal = 0;
			
			Calendar calendario = Calendar.getInstance();	
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(compra.getDataCompra());
			calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
			calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
			calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
			compra.setDataCompra(calendarioTemp.getTime());
			
			compra.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			compra.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			compra.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			compra.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			compra.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
			compra = compras.save(compra);
			
			for (ItemCompra itemCompra : itensCompra) {
				itemCompra.setCompra(compra);
				itensCompras.save(itemCompra);
				
				Produto produto = produtos.porId(itemCompra.getProduto().getId());
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemCompra.getQuantidade());
				produtos.save(produto);
				
				totalDeItens += itemCompra.getQuantidade();
				valorTotal += itemCompra.getTotal().doubleValue();
			}
			
			compra.setValorTotal(BigDecimal.valueOf(valorTotal));
			compra.setQuantidadeItens(totalDeItens);
			compra = compras.save(compra);
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Compra registrada com sucesso!' });");
			
			compra = new Compra();
			itensCompra = new ArrayList<ItemCompra>();
			itemCompra = new ItemCompra();
			itemSelecionado = null;

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à compra!' });");
		}

	}
	
	public void selecionarProduto(Produto produto) {
		itemCompra = new ItemCompra();
		itemCompra.setProduto(produto);
		itemCompra.setCode(produto.getCodigo());
	}

	public void adicionarItem() {
	
		if(!itensCompra.contains(itemCompra)) {
			itemCompra.setTotal(BigDecimal.valueOf(itemCompra.getValorUnitario().doubleValue() * itemCompra.getQuantidade().longValue()));
			itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade());
			itemCompra.setCompra(compra);
			itensCompra.add(itemCompra); 		
			
			compra.setValorTotal(BigDecimal.valueOf(compra.getValorTotal().doubleValue() + itemCompra.getTotal().doubleValue()));
			
			itemCompra = new ItemCompra();
			
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Produto já foi adicionado!' });");
		}
		
	}

	public void removeItem() {

		compra.setValorTotal(BigDecimal.valueOf(compra.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
		itensCompra.remove(itemSelecionado);		
		itemSelecionado = null;

	}
	
	public void editarItem() {

		itemCompra = itemSelecionado;
		compra.setValorTotal(BigDecimal.valueOf(compra.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
		itensCompra.remove(itemSelecionado);
		itemSelecionado = null;

	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public List<ItemCompra> getItensCompra() {
		return itensCompra;
	}

	public Compra getCompra() {
		return compra;
	}

	public ItemCompra getItemCompra() {
		return itemCompra;
	}

	public void setItemCompra(ItemCompra itemCompra) {
		this.itemCompra = itemCompra;
	}

	public void setCompra(Compra compra) {
		this.compra = compra;
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

	public ItemCompra getItemSelecionado() {
		return itemSelecionado;
	}

	public void setItemSelecionado(ItemCompra itemSelecionado) {
		this.itemSelecionado = itemSelecionado;
	}

	public int getItensCompraSize() {
		return itensCompra.size();
	}
}
