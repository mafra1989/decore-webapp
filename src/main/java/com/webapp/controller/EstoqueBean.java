package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Compras;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class EstoqueBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<CategoriaProduto> todasCategoriasProdutos;
	
	@Inject
	private CategoriasProdutos categoriasProdutos;
	
	private List<Produto> produtosFiltrados;
	
	@Inject
	private Produtos produtos;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private Produto produtoSelecionado;
	
	private ProdutoFilter filter = new ProdutoFilter();
	
	private String estoqueTotal = "0";
	
	private byte[] fileContent;
	
	private boolean pedido;
	
	private Long quantidadePedido;
	
	private NumberFormat nf = new DecimalFormat("###,##0.00");

	private Long produtoId;
        
    @Inject
	private ItensCompras itensCompras;
	
	@Inject
	private Compras compras;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todasCategoriasProdutos();
		}
	}
	
	public void pesquisar() {
		
		boolean zerarEstoque = false;
		if(filter.getDescricao().equalsIgnoreCase("ZerarEstoque")) {
			zerarEstoque = true;
			filter.setDescricao("");
		}
		
		boolean estoqueDisponivel = false;
		if(filter.getDescricao().equalsIgnoreCase("EstoqueDisponivel")) {
			estoqueDisponivel = true;
			filter.setDescricao("");
		}
		
		produtosFiltrados = produtos.filtrados(filter);	
		
		long value = 0;
		for (Produto produto : produtosFiltrados) {
			value += produto.getQuantidadeAtual();
		}
		
		estoqueTotal = String.valueOf(value);
		
		produtoSelecionado = null;
		
		if(pedido) {
			
			Long totalItensVendidos = 0L;
			for (Produto produto : produtosFiltrados) {
								
				List<ItemVenda> itensVenda = itensVendas.porProduto(produto);
				for (ItemVenda itemVenda : itensVenda) {
					totalItensVendidos += itemVenda.getQuantidade();
				}
			}
			
			for (Produto produto : produtosFiltrados) {
				
				Long totalItemVendido = 0L;
				
				List<ItemVenda> itensVenda = itensVendas.porProduto(produto);
				for (ItemVenda itemVenda : itensVenda) {
					totalItemVendido += itemVenda.getQuantidade();
				}
				
				if(totalItemVendido > 0) {
					
					produto.setPercentualVenda(nf.format((totalItemVendido * 100) / totalItensVendidos.doubleValue()) + "%");
				} else {
					produto.setPercentualVenda(nf.format(0D) + "%");
				}			
				
				if(quantidadePedido == null) {
					produto.setQuantidadePedido((long) ((totalItemVendido * 100) / totalItensVendidos.doubleValue()) * 0);
				} else {
					produto.setQuantidadePedido((long) (((totalItemVendido * 100) / totalItensVendidos.doubleValue()) * quantidadePedido)/100);
				}
			}
			
		} else {
			quantidadePedido = null;
		}
		
		
		if(zerarEstoque) {
			
			for (Produto produto : produtosFiltrados) {
				produto = produtos.porId(produto.getId());
				produto.setQuantidadeAtual(0L);
				
				produto = produtos.save(produto);
			}
			
			//produtosFiltrados = produtos.filtrados(filter);
		}
		
		if(filter.getDescricao().contains("Ajuste")) {
			String[] dados = filter.getDescricao().replace("Ajuste", "").split(",");
			
			Compra compra = compras.porNumeroCompra(dados[0]);
			Produto produto = produtos.porCodigo(dados[1]);
			
			ItemCompra itemCompra = itensCompras.porCompra(compra, produto);
			if(itemCompra != null) {
				itemCompra.setQuantidadeDisponivel(Long.parseLong(dados[2]));
				itensCompras.save(itemCompra);
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Ajuste realizado com sucesso! Compra N. " 
				+ compra.getNumeroCompra() + ", Produto: " + produto.getCodigo() + ", Quantidade Disponível: "
								+ itemCompra.getQuantidadeDisponivel() + " ' });");
			}		
		}	
		
		if(estoqueDisponivel) {
			for (Produto produto : produtosFiltrados) {
				List<ItemCompra> itensCompra = itensCompras.porProduto(produto);
				
				for (ItemCompra itemCompra : itensCompra) {
					Number totalVendido = itensVendas.vendasPorCompra(itemCompra.getCompra(), produto);
					itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade() - totalVendido.longValue());
					
					System.out.println("Produto: " + itemCompra.getProduto().getCodigo() + " Quantidade: " + itemCompra.getQuantidade() + " Disponível: " + itemCompra.getQuantidadeDisponivel());
					
					itensCompras.save(itemCompra);
				}
			}
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Estoque Disponível ajustado com sucesso!' });");
		}
	}
	
	
	public void prepareFoto() {
		fileContent = produtoSelecionado.getFoto();
                produtoId = produtoSelecionado.getId();
	} 

        public void prepareId() {
               produtoId = produtoSelecionado.getId();
        }
	
	public String getImageContentsAsBase64() {
	    return Base64.getEncoder().encodeToString(fileContent);
	}
	
	private void todasCategoriasProdutos() {
		todasCategoriasProdutos = categoriasProdutos.todos();
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

	public List<CategoriaProduto> getTodasCategoriasProdutos() {
		return todasCategoriasProdutos;
	}

	public Produto getProdutoSelecionado() {
		return produtoSelecionado;
	}

	public void setProdutoSelecionado(Produto produtoSelecionado) {
		this.produtoSelecionado = produtoSelecionado;
	}

	public String getEstoqueTotal() {
		return estoqueTotal;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public boolean isPedido() {
		return pedido;
	}

	public void setPedido(boolean pedido) {
		this.pedido = pedido;
	}

	public Long getQuantidadePedido() {
		return quantidadePedido;
	}

	public void setQuantidadePedido(Long quantidadePedido) {
		this.quantidadePedido = quantidadePedido;
	}

        public Long getProdutoId() {
              return produtoId;
        }



}
