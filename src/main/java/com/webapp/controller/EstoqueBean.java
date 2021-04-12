package com.webapp.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Configuracao;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Log;
import com.webapp.model.Produto;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Bairros;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Clientes;
import com.webapp.repository.Compras;
import com.webapp.repository.Configuracoes;
import com.webapp.repository.Empresas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Logs;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
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
	private Vendas vendas;
	
	@Inject
	private Empresas empresas;

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
	
	private List<Produto> portasFiltradas;
	
	private List<Produto> fechadurasFiltradas;
	
	private List<Produto> maisVendidos;
	
	private List<Produto> produtosEmDestaque;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;
	
	
	@NotNull
	private BigDecimal novaQuantidadeAtual = BigDecimal.ZERO;
	
	@Inject
	private Compra compra;
	
	@Inject
	private ItemCompra itemCompra;
	
	
	@Inject
	private Venda venda;
	
	@Inject
	private ItemVenda itemVenda;

	
	private List<ItemCompra> itensCompra = new ArrayList<ItemCompra>();
	

	@Inject
	private Clientes clientes;
	
	@Inject
	private Bairros bairros;
	
	@Inject
	private TiposVendas tiposVendas;
	
	
	@NotNull
	private BigDecimal novoCustoMedio;
	
	
	@Inject
	private Configuracoes configuracoes;
	
	@Inject
	private Configuracao configuracao;
	
	
	@Inject
	private Logs logs;
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);
	
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario = usuarios.porLogin(user.getUsername());
			
			todasCategoriasProdutos();
			
			configuracao = configuracoes.porId(1L);
			
			NumberFormat nf_ = new DecimalFormat("###,##0.000");
			estoqueTotal = nf_.format(0);
		}
	}
	

	public void pesquisar() {

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
		
		boolean saveImage = false;
		if (filter.getDescricao().equalsIgnoreCase("saveImage")) {
			saveImage = true;
			filter.setDescricao("");
		}
		
		boolean savePath = false;
		if (filter.getDescricao().equalsIgnoreCase("savePath")) {
			savePath = true;
			filter.setDescricao("");
		}

		filter.setEmpresa(usuario.getEmpresa());
		produtosFiltrados = produtos.filtrados(filter);

		double value = 0;
		for (Produto produto : produtosFiltrados) {
			produto.setQuantidadeAtual(new BigDecimal(itensCompras.quantidadeDisponivelPorProduto(produto).doubleValue()));
			produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));
			value += produto.getQuantidadeAtual().doubleValue();		
		}
		
		
		if (saveImage) {

			for (Produto produto : produtosFiltrados) {			
				
				if(produto.getFoto() != null) {
					
					String path = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/produtos/" + produto.getId() + ".jpg";
					
					File file = new File(path);
					
					try {
						FileOutputStream fos = new FileOutputStream(file);
						try {
							fos.write(produto.getFoto());
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		
		if (savePath) {

			for (Produto produto : produtosFiltrados) {			
				
				if(produto.getFoto() != null) {
					
					String path = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/produtos/" + produto.getId() + ".jpg";
					
					produto.setUrlImagem(path);
					produto.setFoto(null);
					
					produtos.save(produto);	
				}
			}
		}
		
		NumberFormat nf_ = new DecimalFormat("###,##0.000");
		estoqueTotal = nf_.format(value);

		produtoSelecionado = null;

		if (pedido) {

			Double totalItensVendidos = 0D;
			for (Produto produto : produtosFiltrados) {

				List<ItemVenda> itensVenda = itensVendas.porProduto(produto);
				for (ItemVenda itemVenda : itensVenda) {
					totalItensVendidos += itemVenda.getQuantidade().doubleValue();
				}
			}

			for (Produto produto : produtosFiltrados) {

				Double totalItemVendido = 0D;

				List<ItemVenda> itensVenda = itensVendas.porProduto(produto);
				for (ItemVenda itemVenda : itensVenda) {
					totalItemVendido += itemVenda.getQuantidade().doubleValue();
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
				produto.setQuantidadeAtual(BigDecimal.ZERO);

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
				itemCompra.setQuantidadeDisponivel(new BigDecimal(Double.parseDouble(dados[2])));
				itensCompras.save(itemCompra);

				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Ajuste realizado com sucesso! Compra N. "
								+ compra.getNumeroCompra() + ", Produto: " + produto.getCodigo()
								+ ", Quantidade Disponível: " + itemCompra.getQuantidadeDisponivel().doubleValue() + " ' });");
			}
		}

		if (estoqueDisponivel && loop) {

			Double totalDisponivel = 0D;

			loop = false;
			for (Produto produto : produtosFiltrados) {
				List<ItemCompra> itensCompra = itensCompras.porProduto(produto);

				Double disponivel = 0D;

				for (ItemCompra itemCompra : itensCompra) {
					Number totalVendido = itensVendas.vendasPorCompra(itemCompra.getCompra(), produto);
					itemCompra.setQuantidadeDisponivel(new BigDecimal(itemCompra.getQuantidade().doubleValue() - totalVendido.doubleValue()));

					if(itemCompra.getQuantidadeDisponivel().doubleValue() > 0) {
						disponivel += itemCompra.getQuantidadeDisponivel().doubleValue();
						totalDisponivel += itemCompra.getQuantidadeDisponivel().doubleValue();
					}
					
					System.out.println("Produto: " + itemCompra.getProduto().getCodigo() + " Quantidade: "
							+ itemCompra.getQuantidade().doubleValue() + " Disponível: " + itemCompra.getQuantidadeDisponivel().doubleValue());

					itensCompras.save(itemCompra);
				}

				produto.setQuantidadeAtual(new BigDecimal(disponivel));
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

				Double quantidadeDisponivel = 0D;
				Double custo = 0D;

				for (ItemCompra itemCompra : itensCompra) {

					if(itemCompra.getQuantidadeDisponivel().doubleValue() > 0) {
						quantidadeDisponivel += itemCompra.getQuantidadeDisponivel().doubleValue();
						custo += itemCompra.getQuantidadeDisponivel().doubleValue() * itemCompra.getValorUnitario().doubleValue();
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
		todasCategoriasProdutos = categoriasProdutos.todos(empresas.porId(7111L));
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
	
	public Integer getProdutosSize() {
		return produtosFiltrados != null ? produtosFiltrados.size() : 0;
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
		//filter.setEmpresa(usuario.getEmpresa());
		filter.setEmpresa(empresas.porId(7111L));
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
				
		//filter.setEmpresa(usuario.getEmpresa());
		filter.setEmpresa(empresas.porId(7111L));
		produtosEmDestaque = produtos.produtosEmDestaque(filter);
		
		for (Produto produto : produtosEmDestaque) {
			produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));
			
			Double totalItensVendidos = 0D;
			Double quantidadeItensVendidos = 0D;
			List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
			for (ItemVenda itemVenda : itensVenda) {
				quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue();
				totalItensVendidos += itemVenda.getQuantidade().doubleValue();
			}
			
			produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));

			if(totalItensVendidos > 0) {
				produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).doubleValue()));
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
			produto.setDescricao(convertToTitleCaseIteratingChars(produto.getDescricao()));
			
			Double totalItensVendidos = 0D;
			Double quantidadeItensVendidos = 0D;
			List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
			for (ItemVenda itemVenda : itensVenda) {
				quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue();
				totalItensVendidos += itemVenda.getQuantidade().doubleValue();
			}
			
			produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));

			if(totalItensVendidos > 0) {
				produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).doubleValue()));
			} else {
				produto.setPrecoMedioVenda(BigDecimal.ZERO);
			}
			
			maisVendidos.add(produto);
		}	
		 
		 
		
	}


	public void buscarCategorias() {		
		todasCategoriasProdutos = categoriasProdutos.todosEmDestaque(usuario.getEmpresa());		
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
	
	
	public void excluir() {

		if (produtoSelecionado != null) {
			
			try {
				
				List<ItemVenda> itensVendidos = itensVendas.porProduto(produtoSelecionado, false);
				
				if(itensVendidos.size() == 0) {
					
					List<ItemCompra> itensComprados = itensCompras.porProduto(produtoSelecionado, false);
					
					if(itensComprados.size() == 0) {
						
	
						List<ItemVenda> itensComAjustesDeSaida = itensVendas.porProduto(produtoSelecionado, true);	
						
						for (ItemVenda itemVenda : itensComAjustesDeSaida) {
							
							List<ItemVendaCompra> itensVendidosPorCompra = itensVendasCompras.porItemVenda(itemVenda);
							for (ItemVendaCompra itemVendaCompra : itensVendidosPorCompra) {
								itensVendasCompras.remove(itemVendaCompra);
							}
							
							itensVendas.remove(itemVenda);
							vendas.remove(itemVenda.getVenda());
						}					
						
						List<ItemCompra> itensComAjustesDeEntrada = itensCompras.porProduto(produtoSelecionado, true);	
						
						for (ItemCompra itemCompra : itensComAjustesDeEntrada) {
							itensCompras.remove(itemCompra);
							compras.remove(itemCompra.getCompra());
						}
												
						
						produtos.remove(produtoSelecionado);
						
						String path = "C:/xampp/tomcat/webapps/produtos/" + produtoSelecionado.getId() + ".jpg";
						
						new File(path).delete();
			
						produtos.remove(produtoSelecionado);
			
						produtoSelecionado = null;
						pesquisar();
								
						PrimeFaces.current().executeScript(
										"swal({ type: 'success', title: 'Concluído!', text: 'Produto excluído com sucesso!' });");
						
					} else {
						PrimeFaces.current().executeScript(
								"swal({ type: 'error', title: 'Ops!', text: 'Operação não realizada, produto com movimentação de compra ou venda!' });");
					}
					
				} else {
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Ops!', text: 'Operação não realizada, produto com movimentação de compra ou venda!' });");
				}						
				
			} catch(Exception e) {
				e.printStackTrace();
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Ops!', text: 'Operação não realizada, produto com movimentação de compra ou venda!' });");
			}

		}
	}
	
	
	
	public void atualizarCustoMedio() {
		
		if(produtoSelecionado.getId() != null) {
			
			if(novoCustoMedio.doubleValue() >= 0) {

				Log log = new Log();
				log.setDataLog(new Date());
				log.setCodigoOperacao(String.valueOf(produtoSelecionado.getCodigo()));
				log.setOperacao(TipoAtividade.ESTOQUE.name());
				
				NumberFormat nf = new DecimalFormat("###,##0.0000", REAL);
				
				log.setDescricao("Alterou custo médio, produto " + produtoSelecionado.getCodigo() + ", " + produtoSelecionado.getDescricao() + ", de R$ " + nf.format(produtoSelecionado.getCustoMedioUnitario()) + " para R$ " + nf.format(novoCustoMedio));
				log.setUsuario(usuario);		
				logs.save(log);
				
				
				produtoSelecionado.setCustoMedioUnitario(novoCustoMedio);		
				produtoSelecionado = produtos.save(produtoSelecionado);
				
				
				Produto produtoSelecionadoTemp = produtos.porId(produtoSelecionado.getId());
				pesquisar();
				produtoSelecionado = produtoSelecionadoTemp;
				
				PrimeFaces.current().executeScript(
						"stop();PF('custo-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Custo médio atualizado com sucesso!', timer: 1500 });");
				
				
			} else {
				 
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Custo médio não pode ser menor que zero!' });");

			}
			
		}
		
	}
	
	
	
	public void ajustarQuantidade() {
		
		if(produtoSelecionado.getId() != null) {
			
			if(novaQuantidadeAtual.doubleValue() >= 0) {
				
				if(novaQuantidadeAtual.doubleValue() > produtoSelecionado.getQuantidadeAtual().doubleValue()) {
					
					compra.setAjuste(true);
					
					itemCompra = new ItemCompra();
					itemCompra.setProduto(produtoSelecionado);			
					itemCompra.setValorUnitario(produtoSelecionado.getCustoMedioUnitario());
					itemCompra.setQuantidade(new BigDecimal(novaQuantidadeAtual.doubleValue() - produtoSelecionado.getQuantidadeAtual().doubleValue()));
					
					registrarCompra();
					
					PrimeFaces.current().executeScript(
							"stop();PF('ajuste-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Quantidade atualizada com sucesso!', timer: 1500 });");
				
				} else if(novaQuantidadeAtual.doubleValue() < produtoSelecionado.getQuantidadeAtual().doubleValue()) {
					
					venda.setAjuste(true);
					
					itemVenda = new ItemVenda();
					itemVenda.setProduto(produtoSelecionado);			
					itemVenda.setValorUnitario(produtoSelecionado.getCustoMedioUnitario());
					itemVenda.setQuantidade(new BigDecimal(produtoSelecionado.getQuantidadeAtual().doubleValue() - novaQuantidadeAtual.doubleValue()));
					
					registrarVenda();
					
					PrimeFaces.current().executeScript(
							"stop();PF('ajuste-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Quantidade atualizada com sucesso!', timer: 1500 });");
					
				}
				
			} else {
				 
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor que zero!' });");

			}
			
		}
		
	}


	public BigDecimal getNovaQuantidadeAtual() {
		return novaQuantidadeAtual;
	}


	public void setNovaQuantidadeAtual(BigDecimal novaQuantidadeAtual) {
		this.novaQuantidadeAtual = novaQuantidadeAtual;
	}


	public Compra getCompra() {
		return compra;
	}


	public void setCompra(Compra compra) {
		this.compra = compra;
	}


	public ItemCompra getItemCompra() {
		return itemCompra;
	}


	public void setItemCompra(ItemCompra itemCompra) {
		this.itemCompra = itemCompra;
	}
	
	
	public void prepararAtualizacaoCustoMedio() {
		novoCustoMedio = produtoSelecionado.getCustoMedioUnitario();
	}
	
	public void prepararAjusteQuantidade() {
		novaQuantidadeAtual = produtoSelecionado.getQuantidadeAtual();
	}
	

	public void registrarCompra() {

		if(produtoSelecionado.getId() != null) {	
			
			if(itemCompra.getValorUnitario().doubleValue() >= 0) {
				
				if(itemCompra.getQuantidade().doubleValue() > 0) {
		
					long totalDeItens = 0;
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
			
					Compra compraTemp = compras.ultimoNCompra(usuario.getEmpresa());
			
					if (compraTemp == null) {
						compra.setNumeroCompra(1L);
					} else {
						if (compra.getId() == null) {
							compra.setNumeroCompra(compraTemp.getNumeroCompra() + 1);
						}
					}
						
					compra.setTipoPagamento(TipoPagamento.AVISTA);
					compra.setCompraPaga(true);
					compra.setConta(false);
			
					compra.setUsuario(usuario);
					compra.setEmpresa(usuario.getEmpresa());
					
					compra = compras.save(compra);
							
					itemCompra.setProduto(produtoSelecionado);
					Double quantidadeDisponivel = itensCompras.quantidadeDisponivelPorProduto(itemCompra.getProduto()).doubleValue();
					
					itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade());
					itemCompra.setTotal(new BigDecimal(itemCompra.getQuantidade().doubleValue() * itemCompra.getValorUnitario().doubleValue()));
							
					itemCompra.setCompra(compra);
					itemCompra = itensCompras.save(itemCompra);
			
					produtoSelecionado = produtos.porId(itemCompra.getProduto().getId());
					produtoSelecionado.setQuantidadeAtual(new BigDecimal(itensCompras.quantidadeDisponivelPorProduto(produtoSelecionado).doubleValue()));
			
					if(!compra.isAjuste()) {
						
						if(quantidadeDisponivel.doubleValue() > 0) {
							
							List<ItemVenda> itensVenda = itensVendas.porCompra(compra, itemCompra);
							List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porCompra(itemCompra.getCompra(), itemCompra.getProduto());
			
							if (itensVenda.size() == 0 && itensVendaCompra.size() == 0) {
								
								// Atualizar custo total e custo medio un. 
								produtoSelecionado.setCustoTotal(new BigDecimal(produtoSelecionado.getCustoTotal().doubleValue() + itemCompra.getTotal().doubleValue()));					
								
							}
							
						
						} else {
							
							// Atualizar custo total e custo medio un. 
							produtoSelecionado.setCustoTotal(new BigDecimal(itemCompra.getTotal().doubleValue()));	
						}
					
					} else {
						
						System.out.println("Custo médio Un.: " + produtoSelecionado.getCustoMedioUnitario().doubleValue());
						if(quantidadeDisponivel.doubleValue() > 0) {
							// Atualizar custo total 
							produtoSelecionado.setCustoTotal(new BigDecimal(produtoSelecionado.getCustoTotal().doubleValue() + itemCompra.getTotal().doubleValue()));
						} else {
							// Atualizar custo total e custo medio un. 
							produtoSelecionado.setCustoTotal(new BigDecimal(itemCompra.getTotal().doubleValue()));	
						}					
					}
							
					//produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / produto.getQuantidadeAtual().doubleValue()));
					
					produtoSelecionado.setEstoque(true);
					produtoSelecionado = produtos.save(produtoSelecionado);
			
					if(!produtoSelecionado.getUnidadeMedida().equals("KG") && !produtoSelecionado.getUnidadeMedida().equals("LT") && !produtoSelecionado.getUnidadeMedida().equals("PT")) {
						totalDeItens += itemCompra.getQuantidade().doubleValue();				
					} else {
						totalDeItens += 1;
					}
						
					valorTotal += itemCompra.getTotal().doubleValue();
									
					compra.setValorTotal(BigDecimal.valueOf(valorTotal));
					compra.setQuantidadeItens(totalDeItens);
					compra = compras.save(compra);
			
					if(!compra.isAjuste()) {
						
						PrimeFaces.current().executeScript(
								"stop();PF('compra-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
										+ compra.getNumeroCompra() + " registrada com sucesso!', timer: 1500 });");
					}
					
					
					
					Log log = new Log();
					log.setDataLog(new Date());
					log.setCodigoOperacao(String.valueOf(produtoSelecionado.getCodigo()));
					log.setOperacao(TipoAtividade.ESTOQUE.name());
					
					NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
					String novaQuantidadeAtualFormatada = "";
					
					if(produtoSelecionado.getUnidadeMedida().equals("Kg") || produtoSelecionado.getUnidadeMedida().equals("Lt")) {
						nf = new DecimalFormat("###,##0.000", REAL);
						produtoSelecionado.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
								produtoSelecionado.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
						novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
						
					} else if(produtoSelecionado.getUnidadeMedida().equals("Pt")) {
						nf = new DecimalFormat("###,##0.0", REAL);
						produtoSelecionado.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
								produtoSelecionado.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
						novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
						
					} else if(produtoSelecionado.getUnidadeMedida().equals("Un") || produtoSelecionado.getUnidadeMedida().equals("Cx")) {
						nf = new DecimalFormat("###,##0", REAL);
						produtoSelecionado.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
								produtoSelecionado.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
						novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
					}
					
					log.setDescricao("Ajustou estoque, produto " + produtoSelecionado.getCodigo() + ", " + produtoSelecionado.getDescricao() + ", de " + produtoSelecionado.getQuantidadeAtualFormatada() + " " + produtoSelecionado.getUnidadeMedida() + " para " + novaQuantidadeAtualFormatada + " " + produtoSelecionado.getUnidadeMedida());
					log.setUsuario(usuario);		
					logs.save(log);
					
					
					
					Produto produtoSelecionadoTemp = produtos.porId(produtoSelecionado.getId());
					pesquisar();
					produtoSelecionado = produtoSelecionadoTemp;
					
					
					Compra compraTemp_ = new Compra();
					compraTemp_.setUsuario(compra.getUsuario());
					
					compra = new Compra();
					itemCompra = new ItemCompra();
					
					compra = compraTemp_;
					
				} else {
					PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor ou igual a zero!' });");
				}
				
			} else {
				
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Valor unitário não pode ser menor que zero!' });");
			}
			
		} else {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Para registrar essa compra, primeiro salve o produto!', timer: 3000 });");
		}
	}

	
	

	public void registrarVenda() {
		
		itensCompra = itensCompras.porProduto(produtoSelecionado);
							
		List<ItemVendaCompra> itensVendaCompra = new ArrayList<ItemVendaCompra>();	
		
		Double saldo = itemVenda.getQuantidade().doubleValue();
		for (int i = itensCompra.size() - 1; i >= 0; i--) {
			if(saldo > 0L) {
				if(saldo.longValue() <= itensCompra.get(i).getQuantidadeDisponivel().doubleValue()) {
					ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
					itemVendaCompra.setItemVenda(itemVenda);
					itemVendaCompra.setCompra(itensCompra.get(i).getCompra());
					itemVendaCompra.setQuantidade(new BigDecimal(saldo));
					
					itensVendaCompra.add(itemVendaCompra);
					
					saldo = 0D; 
				} else {
					
					ItemVendaCompra itemVendaCompra = new ItemVendaCompra();
					itemVendaCompra.setItemVenda(itemVenda);
					itemVendaCompra.setCompra(itensCompra.get(i).getCompra());
					itemVendaCompra.setQuantidade(itensCompra.get(i).getQuantidadeDisponivel());
					
					itensVendaCompra.add(itemVendaCompra);
					
					saldo -= itensCompra.get(i).getQuantidadeDisponivel().doubleValue();
				}
			}
		}
		
		
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
		venda.setVendaPaga(true);
		venda.setTipoPagamento(null);	
		venda.setValorRecebido(BigDecimal.ZERO);
		venda.setFaltando(BigDecimal.ZERO);
		venda.setTroco(BigDecimal.ZERO);		
		venda.setTaxaDeEntrega(BigDecimal.ZERO);		
		venda.setRecuperarValores(false);
	
		
	
		Venda vendaTemp = vendas.ultimoNVenda(usuario.getEmpresa());
		
		if (vendaTemp == null) {
			venda.setNumeroVenda(1L);
		} else {
			if (venda.getId() == null) {
				venda.setNumeroVenda(vendaTemp.getNumeroVenda() + 1);
			}
		}
		
		
		if(!produtoSelecionado.getUnidadeMedida().equals("Kg") && !produtoSelecionado.getUnidadeMedida().equals("Lt") && !produtoSelecionado.getUnidadeMedida().equals("Pt")) {
			venda.setQuantidadeItens(itemVenda.getQuantidade().longValue());
		} else {
			venda.setQuantidadeItens(1L);
		}
			 	
		
		Double valorDeCustoUnitario = produtoSelecionado.getCustoMedioUnitario().doubleValue();
		venda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));
			
		BigDecimal valorTotal = BigDecimal.valueOf(
				itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue());		
		venda.setValorTotal(valorTotal);	
		
		venda.setCliente(clientes.porId(1L));
		venda.setUsuario(usuario);
		
		if(usuario.getEmpresa().getId() == 7111 || usuario.getEmpresa().getId() == 7112) {
			venda.setTipoVenda(tiposVendas.porId(33L));
			venda.setBairro(bairros.porId(3008L));
		} else {
			venda.setTipoVenda(tiposVendas.porId(8L));
			venda.setBairro(bairros.porId(1L));			
		}
		
		venda.setStatus(true);
		venda.setConta(false);
		venda.setLucro(BigDecimal.ZERO);	
		venda.setPercentualLucro(BigDecimal.ZERO);
		venda.setClientePagouTaxa(false);
		venda.setDesconto(BigDecimal.ZERO);
		venda.setPdv(false);
		venda.setEmpresa(usuario.getEmpresa());
		venda = vendas.save(venda);
		
		
		

		itemVenda.setValorCompra(new BigDecimal(valorDeCustoUnitario.doubleValue() * itemVenda.getQuantidade().doubleValue()));										
		itemVenda.setDesconto(BigDecimal.ZERO);	
		itemVenda.setEstoque(produtoSelecionado.isEstoque());					
		itemVenda.setTotal(valorTotal);
		itemVenda.setLucro(BigDecimal.ZERO);
		itemVenda.setPercentualLucro(BigDecimal.ZERO);		
		itemVenda.setVenda(venda);
		itemVenda = itensVendas.save(itemVenda);
		
		
		
		
		produtoSelecionado.setCustoTotal(new BigDecimal(produtoSelecionado.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));																					
		produtoSelecionado.setQuantidadeAtual(new BigDecimal(produtoSelecionado.getQuantidadeAtual().doubleValue() - itemVenda.getQuantidade().doubleValue()));				
		produtoSelecionado = produtos.save(produtoSelecionado);
		
		
		
	
		List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
		for (ItemCompra itemCompra : itensCompra) {
				
			for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
					
				itemVendaCompra.setItemVenda(itemVenda);
				
				if (itemCompra.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId()
						.longValue()) {
					if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId()
							.longValue()) {
						
						itemCompra.setQuantidadeDisponivel(new BigDecimal(
								itemCompra.getQuantidadeDisponivel().doubleValue() - itemVendaCompra.getQuantidade().doubleValue()));
					}
				}

			}					
			
			itensCompras.save(itemCompra);					
		}

			
		
		for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
			itensVendasCompras.save(itemVendaCompra);
		}
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(produtoSelecionado.getCodigo()));
		log.setOperacao(TipoAtividade.ESTOQUE.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		String novaQuantidadeAtualFormatada = "";
		
		if(produtoSelecionado.getUnidadeMedida().equals("Kg") || produtoSelecionado.getUnidadeMedida().equals("Lt")) {
			nf = new DecimalFormat("###,##0.000", REAL);
			produtoSelecionado.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
					produtoSelecionado.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
			novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
			
		} else if(produtoSelecionado.getUnidadeMedida().equals("Pt")) {
			nf = new DecimalFormat("###,##0.0", REAL);
			produtoSelecionado.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
					produtoSelecionado.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
			novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
			
		} else if(produtoSelecionado.getUnidadeMedida().equals("Un") || produtoSelecionado.getUnidadeMedida().equals("Cx")) {
			nf = new DecimalFormat("###,##0", REAL);
			produtoSelecionado.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
					produtoSelecionado.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
			novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
		}
		
		log.setDescricao("Ajustou estoque, produto " + produtoSelecionado.getCodigo() + ", " + produtoSelecionado.getDescricao() + ", de " + produtoSelecionado.getQuantidadeAtualFormatada() + " " + produtoSelecionado.getUnidadeMedida() + " para " + novaQuantidadeAtualFormatada + " " + produtoSelecionado.getUnidadeMedida());
		log.setUsuario(usuario);		
		logs.save(log);
		
		
		
		Produto produtoSelecionadoTemp = produtos.porId(produtoSelecionado.getId());
		pesquisar();
		produtoSelecionado = produtoSelecionadoTemp;
				
		

		venda = new Venda();
		itemVenda = new ItemVenda();
	
	}


	public BigDecimal getNovoCustoMedio() {
		return novoCustoMedio;
	}


	public void setNovoCustoMedio(BigDecimal novoCustoMedio) {
		this.novoCustoMedio = novoCustoMedio;
	}


	public Configuracao getConfiguracao() {
		return configuracao;
	}


	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}
	


}
