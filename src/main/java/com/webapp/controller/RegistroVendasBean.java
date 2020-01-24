package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;

import com.webapp.model.Bairro;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Bairros;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RegistroVendasBean implements Serializable {

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
	private TiposVendas tiposVendas;
	
	@Inject
	private Vendas vendas;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private ItensCompras itensCompras;

	private List<Usuario> todosUsuarios;
	
	private List<Bairro> todosBairros;
	
	private List<TipoVenda> todosTiposVendas;
	
	private List<Produto> produtosFiltrados;
	
	@Inject
	private ItemVenda itemVenda;
	
	@NotNull
	@Inject
	private ItemCompra itemCompra;

	private List<ItemVenda> itensVenda = new ArrayList<ItemVenda>();
	
	private List<ItemCompra> itensCompra = new ArrayList<ItemCompra>();
	
	private ProdutoFilter filter = new ProdutoFilter();
	
	private ItemVenda itemSelecionado;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todosUsuarios = usuarios.todos();
			todosTiposVendas = tiposVendas.todos();
			todosBairros = bairros.todos();
		}
	}
	
	public void pesquisar() {
		produtosFiltrados = produtos.filtrados(filter);
		System.out.println(produtosFiltrados.size());
	}

	public void salvar() {
		
		if(itensVenda.size() > 0) {
			
			Long totalDeItens = 0L;
			double valorTotal = 0;
			double lucro = 0;
			double percentualLucro = 0;
			double valorCompra = 0;
			
			Calendar calendario = Calendar.getInstance();	
			Calendar calendarioTemp = Calendar.getInstance();
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
			
			venda = vendas.save(venda);
			
			for (ItemVenda itemVenda : itensVenda) {
				
				itemVenda.setVenda(venda);
				itensVendas.save(itemVenda);
				
				Produto produto = produtos.porId(itemVenda.getProduto().getId());
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemVenda.getQuantidade());
				produtos.save(produto);
				
				totalDeItens += itemVenda.getQuantidade();
				valorTotal += itemVenda.getTotal().doubleValue();
				valorCompra += itemVenda.getValorCompra().doubleValue();
				
				lucro += itemVenda.getLucro().doubleValue();
				percentualLucro += itemVenda.getPercentualLucro().doubleValue();
				
				List<ItemCompra> itensCompraTemp = itensCompras.porCompra(itemVenda.getProduto());
				for (ItemCompra itemCompraTemp : itensCompraTemp) {

					if(itemCompraTemp.getCompra().getId() == itemVenda.getCompra().getId()) {
						if(itemCompraTemp.getProduto().getId() == itemVenda.getProduto().getId()) {
							itemCompraTemp.setQuantidadeDisponivel(itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());											
							itensCompras.save(itemCompraTemp);
						}
					}	
				}			
			}
			
			
			venda.setValorCompra(BigDecimal.valueOf(valorCompra));
			venda.setValorTotal(BigDecimal.valueOf(valorTotal));
			venda.setQuantidadeItens(totalDeItens);
			venda.setLucro(BigDecimal.valueOf(lucro));
			venda.setPercentualLucro(BigDecimal.valueOf(percentualLucro / itensVenda.size()));
			venda = vendas.save(venda);
			

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Venda registrada com sucesso!' });");
			
			venda = new Venda();
			itensVenda = new ArrayList<ItemVenda>();
			itemVenda = new ItemVenda();
			itemSelecionado = null;

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à venda!' });");
		}

	}
	
	public void selecionarProduto(Produto produto) {
		itemVenda = new ItemVenda();
		itemVenda.setProduto(produto);
		itemVenda.setCode(produto.getCodigo().concat("_" + new Date().getTime()));
		System.out.println(itemVenda.getCode());
		
		itensCompra = new ArrayList<ItemCompra>();
		List<ItemCompra> itensCompraTemp = itensCompras.porCompra(produto);
		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			itemCompraTemp.getCompra().setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
			
			boolean produtoNaLista = false;
			for (ItemVenda itemVenda : itensVenda) {
				if(itemCompraTemp.getCompra().getId() == itemVenda.getCompra().getId()) {
					if(itemCompraTemp.getProduto().getId() == itemVenda.getProduto().getId()) {
						
						produtoNaLista = true;
						itemCompraTemp.setQuantidadeDisponivel(itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
											
					}
				}	
			}
			
			if(produtoNaLista != false) {
				if(itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}
			
			if(produtoNaLista != true) {
				if(itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}
		}
		
		if(itensCompra.size() == 0) {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe quantidade disponível!' });");
		}
	}

	public void adicionarItem() {
		
		for (ItemVenda itemVenda : itensVenda) {
			if(itemCompra.getCompra().getId() == itemVenda.getCompra().getId()) {
				if(itemCompra.getProduto().getId() == itemVenda.getProduto().getId()) {
					
					itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidadeDisponivel() - itemVenda.getQuantidade());									
				}
			}	
		}
		
		System.out.println("itemVenda.getQuantidade(): " + itemVenda.getQuantidade());
		System.out.println("itemCompra.getQuantidadeDisponivel(): " + itemCompra.getQuantidadeDisponivel());
		
		if(itemVenda.getQuantidade() <= itemCompra.getQuantidadeDisponivel()) {
			if(itemVenda.getValorUnitario().doubleValue() >= itemCompra.getValorUnitario().doubleValue()) {
				itemVenda.setTotal(BigDecimal.valueOf(itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue()));
				itemVenda.setVenda(venda);
				itemVenda.setCompra(itemCompra.getCompra());
				
				/* Calculo do Lucro em valor e percentual */
				itemVenda.setLucro(BigDecimal.valueOf((itemVenda.getQuantidade().doubleValue() * itemVenda.getValorUnitario().doubleValue()) - (itemVenda.getQuantidade().doubleValue() * itemCompra.getValorUnitario().doubleValue())));
				itemVenda.setPercentualLucro(BigDecimal.valueOf((itemVenda.getLucro().doubleValue() / (itemVenda.getQuantidade().doubleValue() * itemCompra.getValorUnitario().doubleValue())) * 100));
				itemVenda.setValorCompra(BigDecimal.valueOf(itemVenda.getQuantidade().doubleValue() * itemCompra.getValorUnitario().doubleValue()));
				
				System.out.println(itemVenda.getLucro());
				System.out.println(itemVenda.getPercentualLucro());
				
				venda.setValorTotal(BigDecimal.valueOf(venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));
				
				itensVenda.add(itemVenda); 		
				itemVenda = new ItemVenda();
				
				itemCompra = new ItemCompra();
				itensCompra = new ArrayList<ItemCompra>();
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Valor unitário menor que o valor de compra!' });");
			}
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Quantidade maior que a disponível!' });");
		}
	}

	public void removeItem() {
		
		itensCompra = new ArrayList<ItemCompra>();
		List<ItemCompra> itensCompraTemp = itensCompras.porCompra(itemSelecionado.getProduto());
		
		venda.setValorTotal(BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
		itensVenda.remove(itemSelecionado);
		itemSelecionado = null;
		
		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			itemCompraTemp.getCompra().setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
			
			System.out.println(itemCompraTemp.getCompra().getId());
			boolean produtoNaLista = false;
			for (ItemVenda itemVenda : itensVenda) {
				if(itemCompraTemp.getCompra().getId() == itemVenda.getCompra().getId()) {
					if(itemCompraTemp.getProduto().getId() == itemVenda.getProduto().getId()) {
						
						produtoNaLista = true;
						itemCompraTemp.setQuantidadeDisponivel(itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
											
					}
				}	
			}
			
			if(produtoNaLista != false) {
				if(itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}
			
			if(produtoNaLista != true) {
				itensCompra.add(itemCompraTemp);
			}
			
			if(itemCompra != null && itemCompra.getCompra() != null) {
				if(itemCompraTemp.getCompra().getId() == itemCompra.getCompra().getId()) {
					if(itemCompraTemp.getProduto().getId() == itemCompra.getProduto().getId()) {
						itemCompra = itemCompraTemp;
					}
				}
			}
		}
		
		

	}
	
	public void editarItem() {

		itemVenda = itemSelecionado;
		venda.setValorTotal(BigDecimal.valueOf(venda.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
		itensVenda.remove(itemSelecionado);
		itemSelecionado = null;
		
		itensCompra = new ArrayList<ItemCompra>();
		List<ItemCompra> itensCompraTemp = itensCompras.porCompra(itemVenda.getProduto());
		for (ItemCompra itemCompraTemp : itensCompraTemp) {
			itemCompraTemp.getCompra().setDataCompraFormatada(sdf.format(itemCompraTemp.getCompra().getDataCompra()));
			
			if(itemCompraTemp.getCompra().getId() == itemVenda.getCompra().getId()) {
				if(itemCompraTemp.getProduto().getId() == itemVenda.getProduto().getId()) {
					itemCompra = itemCompraTemp;
				}
			}
			
			boolean produtoNaLista = false;
			for (ItemVenda itemVenda : itensVenda) {
				if(itemCompraTemp.getCompra().getId() == itemVenda.getCompra().getId()) {
					if(itemCompraTemp.getProduto().getId() == itemVenda.getProduto().getId()) {
						
						produtoNaLista = true;
						itemCompraTemp.setQuantidadeDisponivel(itemCompraTemp.getQuantidadeDisponivel() - itemVenda.getQuantidade());
											
					}
				}	
			}
			
			if(produtoNaLista != false) {
				if(itemCompraTemp.getQuantidadeDisponivel() > 0) {
					itensCompra.add(itemCompraTemp);
				}
			}
			
			if(produtoNaLista != true) {
				itensCompra.add(itemCompraTemp);
			}
		}

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
}
