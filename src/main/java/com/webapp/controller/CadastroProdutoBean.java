package com.webapp.controller;

import java.io.ByteArrayInputStream;
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

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.shaded.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Compra;
import com.webapp.model.Configuracao;
import com.webapp.model.FormaPagamento;
import com.webapp.model.Fornecedor;
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
import com.webapp.repository.Fornecedores;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Logs;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.uploader.Uploader;
import com.webapp.uploader.WebException;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroProdutoBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<CategoriaProduto> todasCategoriasProdutos;
	
	private List<Fornecedor> todosFornecedores;
	
	@Inject
	private CategoriasProdutos categoriasProdutos;
	
	@Inject
	private Fornecedores fornecedores;
	
	@Inject
	private Produtos produtos;
	
	@Inject
	private Produto produto;
	
	private UploadedFile file;
	
	private byte[] fileContent;
	
	@Inject
	private ItensCompras itensCompras;
	
	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;

	@Inject
	private CategoriaProduto categoriaProduto;
	
	private UploadedFile file_;

	@Inject
	private Fornecedor fornecedor;
	
	private boolean produtoMovimentado = false;
	
	@Inject
	private Compra compra;

	@Inject
	private Compras compras;
	
	@Inject
	private ItemCompra itemCompra;
	
	
	@Inject
	private Venda venda;

	@Inject
	private Vendas vendas;
	
	@Inject
	private ItemVenda itemVenda;
	
	
	private List<ItemCompra> itensCompra = new ArrayList<ItemCompra>();
	
	
	@Inject
	private ItensVendasCompras itensVendasCompras;
	
	@NotNull
	private BigDecimal novaQuantidadeAtual = BigDecimal.ZERO;
	
	
	@Inject
	private Clientes clientes;
	
	@Inject
	private Bairros bairros;
	
	@Inject
	private TiposVendas tiposVendas;

	
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
			todosFornecedores();
			
			gerarCodigoAleatorio();
			
			configuracao = configuracoes.porId(1L);
		}
	}
	
	public void calculaMargens() {
		calculaMargemLucroReal();
		calculaMargemContribuicao();
		
	}
	
	public void calculaMargemLucroReal() {
		
		if(produto.getCustoMedioUnitario().doubleValue() > 0) {
			if(produto.getPrecoDeVenda().doubleValue() > 0) {
				produto.setMargemLucroReal(new BigDecimal(((produto.getPrecoDeVenda().doubleValue() - produto.getCustoMedioUnitario().doubleValue()) / produto.getPrecoDeVenda().doubleValue()) * 100));
			} else {
				produto.setMargemLucroReal(BigDecimal.ZERO);
			}
		} else {
			produto.setMargemLucroReal(BigDecimal.ZERO);
		}
		
		calculaAvender();
	}
	
	public void calculaMargemLucroReal_() {
		
		if(produto.getCustoMedioUnitario().doubleValue() > 0) {
			produto.setPrecoDeVenda(new BigDecimal(produto.getCustoMedioUnitario().doubleValue() + (produto.getCustoMedioUnitario().doubleValue() * produto.getMargemLucro().doubleValue()/100)));
			if(produto.getPrecoDeVenda().doubleValue() > 0) {
				produto.setMargemLucroReal(new BigDecimal(((produto.getPrecoDeVenda().doubleValue() - produto.getCustoMedioUnitario().doubleValue()) / produto.getPrecoDeVenda().doubleValue()) * 100));
			} else {
				produto.setMargemLucroReal(BigDecimal.ZERO);
			}
		}
		
		calculaAvender();
	}
	
	public void calculaMargemContribuicao() {
		Double valorDeCusto = produto.getCustoMedioUnitario().doubleValue();		
		
		if(produto.getPrecoDeVenda().doubleValue() > 0 && valorDeCusto.doubleValue() > 0) {
			produto.setMargemLucro(new BigDecimal(((produto.getPrecoDeVenda().doubleValue() * 100) / valorDeCusto.doubleValue()) - 100));
		} else {
			produto.setMargemLucro(BigDecimal.ZERO);
		}
			
		calculaAvender();
	}
	
	public void calculaMargemContribuicao_() {
		Double valorDeCusto = produto.getCustoMedioUnitario().doubleValue();		
		
		if(valorDeCusto > 0) {
			produto.setPrecoDeVenda(new BigDecimal(valorDeCusto / (1 - produto.getMargemLucroReal().doubleValue()/100)));	
			
			if(produto.getPrecoDeVenda().doubleValue() > 0) {
				produto.setMargemLucro(new BigDecimal(((produto.getPrecoDeVenda().doubleValue() * 100) / valorDeCusto.doubleValue()) - 100));
			} else {
				produto.setMargemLucro(BigDecimal.ZERO);
			}
		}
		
		/*
		if(produto.getPrecoDeVenda().doubleValue() > 0) {
			produto.setMargemLucro(new BigDecimal(((produto.getPrecoDeVenda().doubleValue() * 100) / valorDeCusto.doubleValue()) - 100));
		} else {
			produto.setMargemLucro(BigDecimal.ZERO);
		} 
		*/
			
		calculaAvender();
	}
	
	public void buscar() {
		produto = produtos.porId(produto.getId());
		fileContent = produto.getFoto();
		
		Double quantidadeItensComprados = 0D;
		Double totalItensComprados = 0D;
		List<ItemCompra> itensCompra = itensCompras.porProduto(produto, false);
		for (ItemCompra itemCompra : itensCompra) {
			quantidadeItensComprados += itemCompra.getValorUnitario().doubleValue() * itemCompra.getQuantidade().doubleValue();
			totalItensComprados += itemCompra.getQuantidade().doubleValue();
		}
		
		produto.setTotalCompras(BigDecimal.valueOf(quantidadeItensComprados));
		
		produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));

		Double totalAjusteItensComprados = 0D;
		itensCompra = itensCompras.porProduto(produto, true);
		for (ItemCompra itemCompra : itensCompra) {
			totalAjusteItensComprados += itemCompra.getQuantidade().doubleValue();
		}
		
		produto.setTotalAjusteItensComprados(totalAjusteItensComprados);
			
		Double totalItensVendidos = 0D;
		Double quantidadeItensVendidos = 0D;
		List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
		for (ItemVenda itemVenda : itensVenda) {
			quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue();
			totalItensVendidos += itemVenda.getQuantidade().doubleValue();
		}
		
		produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));
		
		
		
		if(quantidadeItensComprados > 0 || quantidadeItensVendidos > 0) {
			produtoMovimentado = true;
		}
		
		
		
		Double totalAjusteItensVendidos = 0D;
		itensVenda = itensVendas.porProduto(produto, true);
		for (ItemVenda itemVenda : itensVenda) {
			totalAjusteItensVendidos += itemVenda.getQuantidade().doubleValue();
		}
		
		produto.setTotalAjusteItensVendidos(totalAjusteItensVendidos);
		
		
		//produto.setTotalAcumulado(BigDecimal.valueOf(itensCompras.aVender(produto).doubleValue() * (1 + (produto.getMargemLucro().doubleValue()/100))));
		produto.setTotalAcumulado(BigDecimal.valueOf(produto.getQuantidadeAtual().doubleValue() * produto.getPrecoDeVenda().doubleValue()));
		produto.setEstimativaLucro(BigDecimal.valueOf(produto.getTotalAcumulado().doubleValue() - (produto.getQuantidadeAtual().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));
		
		
		if(totalItensComprados > 0) {
			produto.setPrecoMedioCompra(BigDecimal.valueOf(produto.getTotalCompras().doubleValue() / BigDecimal.valueOf(totalItensComprados).doubleValue()));
		} else {
			produto.setPrecoMedioCompra(BigDecimal.ZERO);
		}
		
		
		if(totalItensVendidos > 0) {
			produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).doubleValue()));
		} else {
			produto.setPrecoMedioVenda(BigDecimal.ZERO);
		}
		
		produto.setQuantidadeItensComprados(totalItensComprados);
		
		produto.setQuantidadeItensVendidos(totalItensVendidos);
		
		produto.setQuantidadeAtual(new BigDecimal(itensCompras.quantidadeDisponivelPorProduto(produto).doubleValue()));
		
		calculaMargens();
		
		if(produto.getMargemLucro().doubleValue() < 0) {
			produto.setMargemLucro(BigDecimal.ZERO);
		}
		
		if(produto.getMargemLucroReal().doubleValue() < 0) {
			produto.setMargemLucroReal(BigDecimal.ZERO);
		}
		
		novaQuantidadeAtual = produto.getQuantidadeAtual();
	}
	
	
	public void atualizaInfo() {
		
		Double quantidadeItensComprados = 0D;
		Double totalItensComprados = 0D;
		List<ItemCompra> itensCompra = itensCompras.porProduto(produto, false);
		for (ItemCompra itemCompra : itensCompra) {
			quantidadeItensComprados += itemCompra.getValorUnitario().doubleValue() * itemCompra.getQuantidade().doubleValue();
			totalItensComprados += itemCompra.getQuantidade().doubleValue();
		}
		
		produto.setTotalCompras(BigDecimal.valueOf(quantidadeItensComprados));

		Double totalAjusteItensComprados = 0D;
		itensCompra = itensCompras.porProduto(produto, true);
		for (ItemCompra itemCompra : itensCompra) {
			totalAjusteItensComprados += itemCompra.getQuantidade().doubleValue();
		}
		
		produto.setTotalAjusteItensComprados(totalAjusteItensComprados);
			
		Double totalItensVendidos = 0D;
		Double quantidadeItensVendidos = 0D;
		List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
		for (ItemVenda itemVenda : itensVenda) {
			quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().doubleValue();
			totalItensVendidos += itemVenda.getQuantidade().doubleValue();
		}
		
		produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));
		
		
		Double totalAjusteItensVendidos = 0D;
		itensVenda = itensVendas.porProduto(produto, true);
		for (ItemVenda itemVenda : itensVenda) {
			totalAjusteItensVendidos += itemVenda.getQuantidade().doubleValue();
		}
		
		produto.setTotalAjusteItensVendidos(totalAjusteItensVendidos);
		
		
		//produto.setTotalAcumulado(BigDecimal.valueOf(itensCompras.aVender(produto).doubleValue() * (1 + (produto.getMargemLucro().doubleValue()/100))));
		produto.setTotalAcumulado(BigDecimal.valueOf(produto.getQuantidadeAtual().doubleValue() * produto.getPrecoDeVenda().doubleValue()));
		produto.setEstimativaLucro(BigDecimal.valueOf(produto.getTotalAcumulado().doubleValue() - (produto.getQuantidadeAtual().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));
		
		
		if(totalItensComprados > 0) {
			produto.setPrecoMedioCompra(BigDecimal.valueOf(produto.getTotalCompras().doubleValue() / BigDecimal.valueOf(totalItensComprados).doubleValue()));
		} else {
			produto.setPrecoMedioCompra(BigDecimal.ZERO);
		}
		
		
		if(totalItensVendidos > 0) {
			produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).doubleValue()));
		} else {
			produto.setPrecoMedioVenda(BigDecimal.ZERO);
		}
		
		produto.setQuantidadeItensComprados(totalItensComprados);
		
		produto.setQuantidadeItensVendidos(totalItensVendidos);
		
		
	}
	
	
	public void calculaAvender() {
		
		if(produto.getCustoMedioUnitario().doubleValue() > 0) {
			//Double valorDeVendaEstimado = produto.getCustoMedioUnitario().doubleValue() + (produto.getCustoMedioUnitario().doubleValue() * produto.getMargemLucro().doubleValue()/100);
			
			produto.setTotalAcumulado(BigDecimal.valueOf(produto.getPrecoDeVenda().doubleValue() * produto.getQuantidadeAtual().doubleValue()));
			System.out.println(produto.getTotalAcumulado());
		}
		
		produto.setEstimativaLucro(BigDecimal.valueOf(produto.getTotalAcumulado().doubleValue() - (produto.getQuantidadeAtual().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));
		
	}
			
	
	public void validaBarcode() {
		
		if (produto.getId() == null) {
			
			Produto produtoTemp = null;
				
			if(!produto.getCodigoDeBarras().equals("") && produto.getCodigoDeBarras() != null) {
				produtoTemp = produtos.porCodigoDeBarras(produto.getCodigoDeBarras(), usuario.getEmpresa());
			}
				
			if(produtoTemp != null) {
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Já existe um produto com o código informado!' });");
			}
						
		} else {
			
			Produto produtoTemp = null;
				
			if(!produto.getCodigoDeBarras().equals("") && produto.getCodigoDeBarras() != null) {
				produtoTemp = produtos.porCodigoDeBarrasCadastrado(produto);
			}
			
			if(produtoTemp != null) {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Já existe um produto com o código de barras informado!' });");
			}		
		}
	}
	
	
	public void salvar() {
	
		//if(produto.getPrecoDeVenda().doubleValue() > 0) {
		
		produto.setCodigoDeBarras(produto.getCodigoDeBarras().trim().replace(" ", ""));

		if (produto.getId() == null) {
			
			Produto produtoTemp = produtos.porCodigo(produto.getCodigo(), usuario.getEmpresa());
			
			if(produtoTemp == null) {
				
				if(!produto.getCodigoDeBarras().equals("") && produto.getCodigoDeBarras() != null) {
					produtoTemp = produtos.porCodigoDeBarras(produto.getCodigoDeBarras(), usuario.getEmpresa());
				}
				
				if(produtoTemp == null) {
					
					produto = produtos.save(produto);
					
					if(fileContent != null) {
						//produto.setFoto(fileContent);	
						String path = "C:/xampp/tomcat/webapps/produtos/" + produto.getId() + ".jpg";
						
						new File(path).delete();
						
						produto.setUrlImagem(path);	
						
						File file = new File(path);
						
						try {
							FileOutputStream fos = new FileOutputStream(file);
							try {
								fos.write(fileContent);
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
					
					
					produto = produtos.save(produto);
					
					if(produto.isEstoque()) {
						itemCompra.setProduto(produto);			
						itemCompra.setValorUnitario(produto.getCustoMedioUnitario());
						
						if(produto.getQuantidadeAtual().doubleValue() > 0) {
							registrarCompra();
						}
						
					} else {			
						produto.setQuantidadeAtual(BigDecimal.ZERO);
					}
					
					/*
					produto = new Produto();
					fileContent = null;
					file = null;
					
					categoriaProduto = new CategoriaProduto();
					fornecedor = new Fornecedor();
					*/
					
					PrimeFaces.current().executeScript(
							"reloadBarCode();swal({ type: 'success', title: 'Concluído!', text: 'Produto cadastrado com sucesso!' });");
				} else {
					
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Já existe um produto com o código de barras informado!' });");	
				}
				
							
			} else {
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Já existe um produto com o código informado!' });");	
			}
						
	
		} else {
			
			Produto produtoTemp = produtos.porCodigoCadastrado(produto);
			if(produtoTemp == null) {
				
				if(!produto.getCodigoDeBarras().equals("") && produto.getCodigoDeBarras() != null) {
					produtoTemp = produtos.porCodigoDeBarrasCadastrado(produto);
				}
				
				if(produtoTemp == null) {	
					
					Produto produtoTemp_ = produtos.porId(produto.getId());
					if(produtoTemp_.getCustoMedioUnitario().doubleValue() != produto.getCustoMedioUnitario().doubleValue()) {
						Log log = new Log();
						log.setDataLog(new Date());
						log.setCodigoOperacao(String.valueOf(produto.getCodigo()));
						log.setOperacao(TipoAtividade.ESTOQUE.name());
						
						NumberFormat nf = new DecimalFormat("###,##0.0000", REAL);
						
						log.setDescricao("Alterou custo médio, produto " + produto.getCodigo() + ", " + produto.getDescricao() + ", de R$ " + nf.format(produtoTemp_.getCustoMedioUnitario()) + " para R$ " + nf.format(produto.getCustoMedioUnitario()));
						log.setUsuario(usuario);		
						logs.save(log);
					}
					
					produto = produtos.save(produto);
					
					if(fileContent != null) {
						//produto.setFoto(fileContent);	
							
						String path = "C:/xampp/tomcat/webapps/produtos/" + produto.getId() + ".jpg";
						
						new File(path).delete();
						
						produto.setUrlImagem(path);	
						
						File file = new File(path);
						
						try {
							FileOutputStream fos = new FileOutputStream(file);
							try {
								fos.write(fileContent);
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
					
					produto = produtos.save(produto);
					
					PrimeFaces.current().executeScript(
							"swal({ type: 'success', title: 'Concluído!', text: 'Produto atualizado com sucesso!' });");
				} else {
					
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Já existe um produto com o código de barras informado!' });");	
				}
				
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Já existe um produto com o código informado!' });");	
			}		
		}
			
		/*} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Custo médio deve ser maior que zero!' });");	
		}*/

	}
	
	private void todasCategoriasProdutos() {
		todasCategoriasProdutos = categoriasProdutos.todos(usuario.getEmpresa());
	}
	
	private void todosFornecedores() {
		todosFornecedores = fornecedores.todos(usuario.getEmpresa());
	}
	
	public UploadedFile getFile() {
        return file;
    }
	
	public void setFile(UploadedFile file) {
        this.file = file;
    }
	
	public List<CategoriaProduto> getTodasCategoriasProdutos() {
		return todasCategoriasProdutos;
	}
	
	public List<Fornecedor> getTodosFornecedores() {
		return todosFornecedores;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}
	
	public String getImageContentsAsBase64() {
	    return Base64.getEncoder().encodeToString(fileContent);
	}
		
	public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        }
        else {
            // So, browser is requesting the image. Return a real StreamedContent with the image bytes.
            //String id = context.getExternalContext().getRequestParameterMap().get("id");
            //Image image = service.find(Long.valueOf(id));
            return new DefaultStreamedContent(new ByteArrayInputStream(fileContent));
        }
    }
	
	public void upload() {
		if(file != null && file.getFileName() != null) {
			
			try {
				
				produto.setUrlImagem(null);
				fileContent = file.getContent();	
					
				/*
				String json = Uploader.upload(file);
				//System.out.println(json);
				
				
				JSONObject jObj = new JSONObject(json);
				jObj = new JSONObject(jObj.get("data").toString());
				System.out.println(jObj.get("link"));
				
				produto.setUrlImagem(jObj.get("link").toString());
				*/
				
				PrimeFaces.current().executeScript("JsBarcode('.barcode', $('.bar-code').val());");
				
			} catch(WebException e) {
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Erro ao enviar imagem do produto: " + produto.getCodigo() + "!' });");
			
				PrimeFaces.current().executeScript("JsBarcode('.barcode', $('.bar-code').val());");
			}
						
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Selecione uma imagem com até 200KB!' });");
			
			PrimeFaces.current().executeScript("JsBarcode('.barcode', $('.bar-code').val());"); 
		}
	}
	
	
	public void salvarCategoria() {
		
		categoriaProduto.setEmpresa(usuario.getEmpresa());
		CategoriaProduto categoria = categoriasProdutos.porNome(categoriaProduto.getNome(), usuario.getEmpresa());
		
		if(categoria == null) {
			categoriasProdutos.save(categoriaProduto);		
			categoriaProduto = new CategoriaProduto();
			
			PrimeFaces.current().executeScript("PF('categoriaProduto-dialog').hide();");
			
			todasCategoriasProdutos();
			
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Já existe uma categoria com o nome informado!' });");
		}
		
	}
	
	
	public void salvarFornecedor() {
		
		fornecedor.setEmpresa(usuario.getEmpresa());
		Fornecedor fornecedor = fornecedores.porNome(this.fornecedor.getNome(), usuario.getEmpresa());
		
		if(fornecedor == null) {
			fornecedores.save(this.fornecedor);		
			this.fornecedor = new Fornecedor();
			
			PrimeFaces.current().executeScript("PF('fornecedor-dialog').hide();");
			
			todosFornecedores();
			
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Já existe um fornecedor com o nome informado!' });");
		}
	}


	public byte[] getFileContent() {
		return fileContent;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public CategoriaProduto getCategoriaProduto() {
		return categoriaProduto;
	}

	public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
		this.categoriaProduto = categoriaProduto;
	}
	
	public void uploadFotoCategoria() {
		if(file_ != null && file_.getFileName() != null) {
			
			try {
				
				//fileContent = file.getContents();	
								
				String json = Uploader.upload(file_);
				//System.out.println(json);
				
				JSONObject jObj = new JSONObject(json);
				jObj = new JSONObject(jObj.get("data").toString());
				System.out.println(jObj.get("link"));
				
				categoriaProduto.setUrlImagem(jObj.get("link").toString());
				
				PrimeFaces.current().executeScript("PF('categoriaProduto-dialog').show();");
				
			} catch(WebException e) {
				
				PrimeFaces.current().executeScript("PF('categoriaProduto-dialog').show();");
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Erro ao enviar imagem da categoria: " + categoriaProduto.getNome() + "!' });");
			}
						
		} else {
			
			PrimeFaces.current().executeScript("PF('categoriaProduto-dialog').show();");
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Selecione uma imagem com até 200KB!' });");
		}
	}

	public UploadedFile getFile_() {
		return file_;
	}

	public void setFile_(UploadedFile file_) {
		this.file_ = file_;
	}

	public Fornecedor getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(Fornecedor fornecedor) {
		this.fornecedor = fornecedor;
	}
		
	
	public void gerenciarEstoque() {
		
		//produto.setCustoMedioUnitario(BigDecimal.ZERO);
		
		if(produto.isEstoque()) {
			produto.setUnidadeMedida(convertToTitleCaseIteratingChars(produto.getUnidadeMedida()));
			PrimeFaces.current().executeScript("mostrarGridEstoque();");
		} else {
			
			PrimeFaces.current().executeScript("ocultarGridEstoque();");
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

	public boolean isProdutoMovimentado() {
		return produtoMovimentado;
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
	
	public void prepararRegistroCompra() {
		novaQuantidadeAtual = produto.getQuantidadeAtual();
	}
	
	
	public void ajustarQuantidade() {
		
		if(produto.getId() != null) {
			
			if(novaQuantidadeAtual.doubleValue() >= 0) {
			
				if(novaQuantidadeAtual.doubleValue() > produto.getQuantidadeAtual().doubleValue()) {
					
					compra.setAjuste(true);
					
					itemCompra = new ItemCompra();
					itemCompra.setProduto(produto);			
					itemCompra.setValorUnitario(produto.getCustoMedioUnitario());
					itemCompra.setQuantidade(new BigDecimal(novaQuantidadeAtual.doubleValue() - produto.getQuantidadeAtual().doubleValue()));
					
					registrarCompra();
					
					atualizaInfo();
					
					PrimeFaces.current().executeScript(
							"stop();PF('ajuste-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Quantidade atualizada com sucesso!', timer: 1500 });");
				
				} else if(novaQuantidadeAtual.doubleValue() < produto.getQuantidadeAtual().doubleValue()) {
					
					venda.setAjuste(true);
					
					itemVenda = new ItemVenda();
					itemVenda.setProduto(produto);			
					itemVenda.setValorUnitario(produto.getCustoMedioUnitario());
					itemVenda.setQuantidade(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - novaQuantidadeAtual.doubleValue()));
					
					registrarVenda();
					
					atualizaInfo();
					
					PrimeFaces.current().executeScript(
							"stop();PF('ajuste-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Quantidade atualizada com sucesso!', timer: 1500 });");
					
				}
				
			} else {
				 
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor que zero!' });");

			}
			
		} else {
			
			if(novaQuantidadeAtual.doubleValue() >= 0) {
				
				compra.setAjuste(true);
				
				itemCompra = new ItemCompra();
				itemCompra.setQuantidade(novaQuantidadeAtual);
				
				produto.setQuantidadeAtual(novaQuantidadeAtual);
				
				PrimeFaces.current().executeScript("stop();PF('ajuste-dialog').hide();");
				
			} else {
				 
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor que zero!' });");
	
			}

			
/*			
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Para ajustar quantidade, primeiro salve o produto!', timer: 3000 });");
*/
		}
		
	}
	

	public void registrarCompra() {

		if(produto.getId() != null) {	
			
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
							
					itemCompra.setProduto(produto);
					Double quantidadeDisponivel = itensCompras.quantidadeDisponivelPorProduto(itemCompra.getProduto()).doubleValue();
					
					itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade());
					itemCompra.setTotal(new BigDecimal(itemCompra.getQuantidade().doubleValue() * itemCompra.getValorUnitario().doubleValue()));
							
					itemCompra.setCompra(compra);
					itemCompra = itensCompras.save(itemCompra);
					
						
			
					produto = produtos.porId(itemCompra.getProduto().getId());
					produto.setQuantidadeAtual(new BigDecimal(itensCompras.quantidadeDisponivelPorProduto(produto).doubleValue()));
			
					if(!compra.isAjuste()) {
						
						if(quantidadeDisponivel.doubleValue() > 0) {
							
							List<ItemVenda> itensVenda = itensVendas.porCompra(compra, itemCompra);
							List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porCompra(itemCompra.getCompra(), itemCompra.getProduto());
			
							if (itensVenda.size() == 0 && itensVendaCompra.size() == 0) {
								
								// Atualizar custo total e custo medio un. 
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemCompra.getTotal().doubleValue()));					
								
							}
							
						
						} else {
							
							// Atualizar custo total e custo medio un. 
							produto.setCustoTotal(new BigDecimal(itemCompra.getTotal().doubleValue()));
						}
					
					} else {
						
						System.out.println("Custo médio Un.: " + produto.getCustoMedioUnitario().doubleValue());
						if(quantidadeDisponivel.doubleValue() > 0) {
							// Atualizar custo total 
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemCompra.getTotal().doubleValue()));
						} else {
							// Atualizar custo total e custo medio un. 
							produto.setCustoTotal(new BigDecimal(itemCompra.getTotal().doubleValue()));	
						}					
					}
							
					//produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / produto.getQuantidadeAtual().doubleValue()));
					
					produto.setEstoque(true);
					produto = produtos.save(produto);
					
					
					
					
					
					
					
					Log log = new Log();
					log.setDataLog(new Date());
					log.setCodigoOperacao(String.valueOf(produto.getCodigo()));
					log.setOperacao(TipoAtividade.ESTOQUE.name());
					
					NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
					String novaQuantidadeAtualFormatada = "";
					
					if(produto.getUnidadeMedida().equals("Kg") || produto.getUnidadeMedida().equals("Lt")) {
						nf = new DecimalFormat("###,##0.000", REAL);
						produto.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
								produto.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
						novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
						
					} else if(produto.getUnidadeMedida().equals("Pt")) {
						nf = new DecimalFormat("###,##0.0", REAL);
						produto.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
								produto.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
						novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
						
					} else if(produto.getUnidadeMedida().equals("Un") || produto.getUnidadeMedida().equals("Cx")) {
						nf = new DecimalFormat("###,##0", REAL);
						produto.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
								produto.getQuantidadeAtual().doubleValue() - itemCompra.getQuantidade().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
						novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
					}
					
					log.setDescricao("Ajustou estoque, produto " + produto.getCodigo() + ", " + produto.getDescricao() + ", de " + produto.getQuantidadeAtualFormatada() + " " + produto.getUnidadeMedida() + " para " + novaQuantidadeAtualFormatada + " " + produto.getUnidadeMedida());
					log.setUsuario(usuario);		
					logs.save(log);
					
					
					
					
					
			
					if(!produto.getUnidadeMedida().equals("KG") && !produto.getUnidadeMedida().equals("LT") && !produto.getUnidadeMedida().equals("PT")) {
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
					
					
					Compra compraTemp_ = new Compra();
					compraTemp_.setUsuario(compra.getUsuario());
					
					compra = new Compra();
					itemCompra = new ItemCompra();
					
					compra = compraTemp_;
					
					calculaMargens();
					
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
	
	
	public void duplicar() {
		
		produto.setId(null);
		
		gerarCodigoAleatorio();
		
		produto.setCodigoDeBarras("");
		
		produto.setQuantidadeAtual(BigDecimal.ZERO);
		
		produto.setEstorno(BigDecimal.ZERO);
		
		produto.setEstoque(false);
		
		System.out.println(produto.getUrlImagem());
		
		
		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Produto duplicado com sucesso!' });");
		
	}
	
	
	public void novoCadastro() throws IOException {
		
		FacesContext.getCurrentInstance().getExternalContext().redirect("/cadastros/CadastroProdutos.xhtml");
	}

	public BigDecimal getNovaQuantidadeAtual() {
		return novaQuantidadeAtual;
	}

	public void setNovaQuantidadeAtual(BigDecimal novaQuantidadeAtual) {
		this.novaQuantidadeAtual = novaQuantidadeAtual;
	}
	
	public void gerarCodigoAleatorio() {
		
		List<String> listaDeCodigos = new ArrayList<String>();
		List<String> todosCodigos = new ArrayList<String>();
		
		List<Produto> todosProdutos = produtos.todos(usuario.getEmpresa());
		for (Produto produto : todosProdutos) {
			todosCodigos.add(produto.getCodigo());
		}
		
		int quantidade = 1;
		
		long sorteio;
	    do {
	      boolean adicionarLista = true;
	      sorteio = (long) (Math.random() * 999999);  //ajustar valores aqui
	      for (String i : todosCodigos){
	        if (i.equals(String.valueOf(sorteio))) {
	          adicionarLista = false;
	          break;
	        }
	      }
	      if (adicionarLista) { listaDeCodigos.add(String.valueOf(sorteio)); }
	    } while (listaDeCodigos.size() < quantidade); 
	    
	    produto.setCodigo(listaDeCodigos.get(0));
	}
	
	
	public void registrarVenda() {
		
		itensCompra = itensCompras.porProduto(produto);
							
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
		
		
		if(!produto.getUnidadeMedida().equals("Kg") && !produto.getUnidadeMedida().equals("Lt") && !produto.getUnidadeMedida().equals("Pt")) {
			venda.setQuantidadeItens(itemVenda.getQuantidade().longValue());
		} else {
			venda.setQuantidadeItens(1L);
		}
			 	
		
		Double valorDeCustoUnitario = produto.getCustoMedioUnitario().doubleValue();
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
		itemVenda.setEstoque(produto.isEstoque());					
		itemVenda.setTotal(valorTotal);
		itemVenda.setLucro(BigDecimal.ZERO);
		itemVenda.setPercentualLucro(BigDecimal.ZERO);		
		itemVenda.setVenda(venda);
		itemVenda = itensVendas.save(itemVenda);
		
		
		
		produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() - itemVenda.getValorCompra().doubleValue()));																					
		produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() - itemVenda.getQuantidade().doubleValue()));				
		produtos.save(produto);
		
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(produto.getCodigo()));
		log.setOperacao(TipoAtividade.ESTOQUE.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		String novaQuantidadeAtualFormatada = "";
		
		if(produto.getUnidadeMedida().equals("Kg") || produto.getUnidadeMedida().equals("Lt")) {
			nf = new DecimalFormat("###,##0.000", REAL);
			produto.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
					produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()).setScale(3, BigDecimal.ROUND_HALF_EVEN)));
			novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
			
		} else if(produto.getUnidadeMedida().equals("Pt")) {
			nf = new DecimalFormat("###,##0.0", REAL);
			produto.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
					produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()).setScale(1, BigDecimal.ROUND_HALF_EVEN)));
			novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
			
		} else if(produto.getUnidadeMedida().equals("Un") || produto.getUnidadeMedida().equals("Cx")) {
			nf = new DecimalFormat("###,##0", REAL);
			produto.setQuantidadeAtualFormatada(nf.format(new BigDecimal(
					produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()).setScale(0, BigDecimal.ROUND_HALF_EVEN)));
			novaQuantidadeAtualFormatada = nf.format(novaQuantidadeAtual);
		}
		
		log.setDescricao("Ajustou estoque, produto " + produto.getCodigo() + ", " + produto.getDescricao() + ", de " + produto.getQuantidadeAtualFormatada() + " " + produto.getUnidadeMedida() + " para " + novaQuantidadeAtualFormatada + " " + produto.getUnidadeMedida());
		log.setUsuario(usuario);		
		logs.save(log);
		
		
	
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
					

		venda = new Venda();
		itemVenda = new ItemVenda();
	
	}

	public Configuracao getConfiguracao() {
		return configuracao;
	}

	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}
	

}
