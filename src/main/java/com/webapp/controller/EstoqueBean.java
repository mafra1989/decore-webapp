package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Compras;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.uploader.WebException;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
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

	private boolean loop = true;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;
	
	private String empresa = "";
	
	
	private List<Produto> portasFiltradas;
	
	private List<Produto> fechadurasFiltradas;
	
	private List<Produto> maisVendidos;
	
	private List<Produto> produtosEmDestaque;

	

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
			
			todasCategoriasProdutos();
			
			if(!empresa.equals(usuario.getEmpresa())) {
				System.out.println(empresa + " " + usuario.getEmpresa());
				if(!empresa.equals("")) {
					pesquisar();
				} 
			}
		}
	}
	

	public void pesquisar() {
		
		if(!empresa.equals(usuario.getEmpresa())) {			
			empresa = usuario.getEmpresa();
		}

		boolean zerarEstoque = false;
		if (filter.getDescricao().equalsIgnoreCase("ZerarEstoque")) {
			zerarEstoque = true;
			filter.setDescricao("");
		}

		boolean estoqueDisponivel = false;
		if (filter.getDescricao().equalsIgnoreCase("EstoqueDisponivel")) {
			estoqueDisponivel = true;
			filter.setDescricao("");
		}
		
		boolean customedio = false;
		if (filter.getDescricao().equalsIgnoreCase("Customedio")) {
			customedio = true;
			filter.setDescricao("");
		}

		filter.setEmpresa(usuario.getEmpresa());
		produtosFiltrados = produtos.filtrados(filter);

		long value = 0;
		for (Produto produto : produtosFiltrados) {
			value += produto.getQuantidadeAtual();
		}

		estoqueTotal = String.valueOf(value);

		produtoSelecionado = null;

		if (pedido) {

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

				if (totalItemVendido > 0) {

					produto.setPercentualVenda(
							nf.format((totalItemVendido * 100) / totalItensVendidos.doubleValue()) + "%");
				} else {
					produto.setPercentualVenda(nf.format(0D) + "%");
				}

				if (quantidadePedido == null) {
					produto.setQuantidadePedido(
							(long) ((totalItemVendido * 100) / totalItensVendidos.doubleValue()) * 0);
				} else {
					produto.setQuantidadePedido(
							(long) (((totalItemVendido * 100) / totalItensVendidos.doubleValue()) * quantidadePedido)
									/ 100);
				}
			}

		} else {
			quantidadePedido = null;
		}

		if (zerarEstoque) {

			for (Produto produto : produtosFiltrados) {
				produto = produtos.porId(produto.getId());
				produto.setQuantidadeAtual(0L);

				produto = produtos.save(produto);
			}

			// produtosFiltrados = produtos.filtrados(filter);
		}

		if (filter.getDescricao().contains("Ajuste")) {
			String[] dados = filter.getDescricao().replace("Ajuste", "").split(",");

			Compra compra = compras.porNumeroCompra(Long.parseLong(dados[0]), usuario.getEmpresa());
			Produto produto = produtos.porCodigo(dados[1], usuario.getEmpresa());

			ItemCompra itemCompra = itensCompras.porCompra(compra, produto);
			if (itemCompra != null) {
				itemCompra.setQuantidadeDisponivel(Long.parseLong(dados[2]));
				itensCompras.save(itemCompra);

				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Ajuste realizado com sucesso! Compra N. "
								+ compra.getNumeroCompra() + ", Produto: " + produto.getCodigo()
								+ ", Quantidade Disponível: " + itemCompra.getQuantidadeDisponivel() + " ' });");
			}
		}

		if (estoqueDisponivel && loop) {

			Long totalDisponivel = 0L;

			loop = false;
			for (Produto produto : produtosFiltrados) {
				List<ItemCompra> itensCompra = itensCompras.porProduto(produto);

				Long disponivel = 0L;

				for (ItemCompra itemCompra : itensCompra) {
					Number totalVendido = itensVendas.vendasPorCompra(itemCompra.getCompra(), produto);
					itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade() - totalVendido.longValue());

					if(itemCompra.getQuantidadeDisponivel() > 0) {
						disponivel += itemCompra.getQuantidadeDisponivel();
						totalDisponivel += itemCompra.getQuantidadeDisponivel();
					}
					
					System.out.println("Produto: " + itemCompra.getProduto().getCodigo() + " Quantidade: "
							+ itemCompra.getQuantidade() + " Disponível: " + itemCompra.getQuantidadeDisponivel());

					itensCompras.save(itemCompra);
				}

				produto.setQuantidadeAtual(disponivel);
				produtos.save(produto);
			}

			loop = true;
			System.out.println("Total Disponível: " + totalDisponivel);
		}
		
		
		if (customedio && loop) {

			loop = false;
			for (Produto produto : produtosFiltrados) {
				List<ItemCompra> itensCompra = itensCompras.porProduto(produto);
				
				System.out.println(produto.getCodigo());

				Long quantidadeDisponivel = 0L;
				Double custo = 0D;

				for (ItemCompra itemCompra : itensCompra) {

					if(itemCompra.getQuantidadeDisponivel() > 0) {
						quantidadeDisponivel += itemCompra.getQuantidadeDisponivel();
						custo += itemCompra.getQuantidadeDisponivel().longValue() * itemCompra.getValorUnitario().doubleValue();
					}

				}

				if(quantidadeDisponivel > 0) {
					produto.setCustoTotal(new BigDecimal(custo));
					produto.setCustoMedioUnitario(new BigDecimal(custo/quantidadeDisponivel));
				}
				
				produtos.save(produto);
				
				System.out.println("Custo Médio Un.: " + produto.getCustoMedioUnitario());
				System.out.println("Custo Total: " + produto.getCustoTotal());
			}

			loop = true;
		}
	}

	public void prepareFoto() {
		//fileContent = produtoSelecionado.getFoto();
		produtoId = produtoSelecionado.getId();
	}

	public void prepareId() {
		produtoId = produtoSelecionado.getId();
	}

	public String getImageContentsAsBase64() {
		return Base64.getEncoder().encodeToString(fileContent);
	}

	private void todasCategoriasProdutos() {
		todasCategoriasProdutos = categoriasProdutos.todos("Decore");
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
	
	public void metotoParaUpload() {
		
		for (Produto produto : produtosFiltrados) {
			
			//if(produto.getFoto() != null) {
				try {
					
					//File file = new File("c:/upload/" + produto.getCodigo() + ".dat"); //Criamos um nome para o arquivo
					//BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file)); //Criamos o arquivo
					//bos.write(produto.getFoto()); //Gravamos os bytes lá
					//bos.close(); //Fechamos o stream.
					
					//String json = Uploader.upload(produto.getFoto());
					//System.out.println(json);
					
					/*
					JSONObject jObj = new JSONObject(json);
					jObj = new JSONObject(jObj.get("data").toString());
					System.out.println(jObj.get("link"));
					
					produto.setUrlImagem(jObj.get("link").toString());
					produtos.save(produto);
					*/
					
				} catch(WebException e) {
					System.out.println("Erro ao enviar o produto: " + produto.getCodigo());
				}
			//}
					
		}
	}
	
	public void buscarProdutos(String categoria) {
		
		filter.setDescricao("");
		CategoriaProduto categoriaProduto = new CategoriaProduto();
		categoriaProduto.setNome(categoria);
		filter.setCategoriaProduto(categoriaProduto);		
		filter.setEmpresa("Decore");
		produtosFiltrados = produtos.filtrados(filter);
		
		
		List<Produto> listaDeProdutos = new ArrayList<>();
		for (Produto produto : produtosFiltrados) {
			produto.setDescricao( convertToTitleCaseIteratingChars(produto.getDescricao()));
			listaDeProdutos.add(produto);
		}
		
		if(categoria.equals("PORTA")) {			
			portasFiltradas = new ArrayList<>();
			portasFiltradas.addAll(listaDeProdutos);
		} 
		
		if(categoria.equals("FECHADURA")) {
			if(categoria.equals("FECHADURA")) {			
				fechadurasFiltradas = new ArrayList<>();
				fechadurasFiltradas.addAll(listaDeProdutos);
			}
		}
		
	}
	
	
	public void buscarProdutosEmDestaque() {
				
		filter.setEmpresa("Decore");
		produtosEmDestaque = produtos.produtosEmDestaque(filter);
		
		for (Produto produto : produtosEmDestaque) {
			produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));
			
			Long totalItensVendidos = 0L;
			Double quantidadeItensVendidos = 0D;
			List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
			for (ItemVenda itemVenda : itensVenda) {
				quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade();
				totalItensVendidos += itemVenda.getQuantidade();
			}
			
			produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));

			if(totalItensVendidos > 0) {
				produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).intValue()));
			} else {
				produto.setPrecoMedioVenda(BigDecimal.ZERO);
			}
		}		
	}
	
	
	public void buscarMaisVendidos() {
		maisVendidos = new ArrayList<Produto>();
		List<Object[]> produtosMaisVendidos = itensVendas.maisVendidos("2020", null, "Decore");
		 for (Object[] object : produtosMaisVendidos) {
			Produto produto = produtos.porId((Long) object[1]);
			
			Long totalItensVendidos = 0L;
			Double quantidadeItensVendidos = 0D;
			List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
			for (ItemVenda itemVenda : itensVenda) {
				quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade();
				totalItensVendidos += itemVenda.getQuantidade();
			}
			
			produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));

			if(totalItensVendidos > 0) {
				produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).intValue()));
			} else {
				produto.setPrecoMedioVenda(BigDecimal.ZERO);
			}
			
			maisVendidos.add(produto);
		}	
		 
		 
		
	}


	public void buscarCategorias() {		
		todasCategoriasProdutos = categoriasProdutos.todosEmDestaque("Decore");		
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


	public List<Produto> getPortasFiltradas() {
		return portasFiltradas;
	}


	public List<Produto> getFechadurasFiltradas() {
		return fechadurasFiltradas;
	}


	public List<Produto> getMaisVendidos() {
		return maisVendidos;
	}


	public List<Produto> getProdutosEmDestaque() {
		return produtosEmDestaque;
	}

}
