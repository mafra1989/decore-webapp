package com.webapp.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.json.JSONObject;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Fornecedor;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Fornecedores;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
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
			todosFornecedores();
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
		produto.setPrecoDeVenda(new BigDecimal(valorDeCusto / (1 - produto.getMargemLucroReal().doubleValue()/100)));
		
		if(produto.getPrecoDeVenda().doubleValue() > 0) {
			produto.setMargemLucro(new BigDecimal(((produto.getPrecoDeVenda().doubleValue() * 100) / valorDeCusto.doubleValue()) - 100));
		} else {
			produto.setMargemLucro(BigDecimal.ZERO);
		}
			
		calculaAvender();
	}
	
	public void buscar() {
		produto = produtos.porId(produto.getId());
		//fileContent = produto.getFoto();
		
		Double quantidadeItensComprados = 0D;
		Long totalItensComprados = 0L;
		List<ItemCompra> itensCompra = itensCompras.porProduto(produto, false);
		for (ItemCompra itemCompra : itensCompra) {
			quantidadeItensComprados += itemCompra.getValorUnitario().doubleValue() * itemCompra.getQuantidade();
			totalItensComprados += itemCompra.getQuantidade();
		}
		
		produto.setTotalCompras(BigDecimal.valueOf(quantidadeItensComprados));
		

		Long totalAjusteItensComprados = 0L;
		itensCompra = itensCompras.porProduto(produto, true);
		for (ItemCompra itemCompra : itensCompra) {
			totalAjusteItensComprados += itemCompra.getQuantidade();
		}
		
		produto.setTotalAjusteItensComprados(totalAjusteItensComprados);
			
		Long totalItensVendidos = 0L;
		Double quantidadeItensVendidos = 0D;
		List<ItemVenda> itensVenda = itensVendas.porProduto(produto, false);
		for (ItemVenda itemVenda : itensVenda) {
			quantidadeItensVendidos += itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade();
			totalItensVendidos += itemVenda.getQuantidade();
		}
		
		produto.setTotalVendas(BigDecimal.valueOf(quantidadeItensVendidos));
		
		
		Long totalAjusteItensVendidos = 0L;
		itensVenda = itensVendas.porProduto(produto, true);
		for (ItemVenda itemVenda : itensVenda) {
			totalAjusteItensVendidos += itemVenda.getQuantidade();
		}
		
		produto.setTotalAjusteItensVendidos(totalAjusteItensVendidos);
		
		
		produto.setTotalAcumulado(BigDecimal.valueOf(itensCompras.aVender(produto).doubleValue() * (1 + (produto.getMargemLucro().doubleValue()/100))));
		//produto.setTotalAcumulado(BigDecimal.valueOf(produto.getQuantidadeAtual() * produto.getLocalizacao().doubleValue()));
		
		if(totalItensVendidos > 0) {
			produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).intValue()));
		} else {
			produto.setPrecoMedioVenda(BigDecimal.ZERO);
		}
		
		produto.setQuantidadeItensComprados(totalItensComprados);
		
		produto.setQuantidadeItensVendidos(totalItensVendidos);
		
		calculaMargens();
	}
	
	public void calculaAvender() {
		
		if(produto.getCustoMedioUnitario().doubleValue() > 0) {
			//Double valorDeVendaEstimado = produto.getCustoMedioUnitario().doubleValue() + (produto.getCustoMedioUnitario().doubleValue() * produto.getMargemLucro().doubleValue()/100);
			
			produto.setTotalAcumulado(BigDecimal.valueOf(produto.getPrecoDeVenda().doubleValue() * produto.getQuantidadeAtual()));
			System.out.println(produto.getTotalAcumulado());
		}
		
	}
	
	public void salvar() {

		if(fileContent != null) {
			//produto.setFoto(fileContent);
		}

		if (produto.getId() == null) {
			
			Produto produtoTemp = produtos.porCodigo(produto.getCodigo(), usuario.getEmpresa());
			
			if(produtoTemp == null) {
				
				if(!produto.getCodigoDeBarras().equals("") && produto.getCodigoDeBarras() != null) {
					produtoTemp = produtos.porCodigoDeBarras(produto.getCodigoDeBarras(), usuario.getEmpresa());
				}
				
				if(produtoTemp == null) {
					produto = produtos.save(produto);
					
					produto = new Produto();
					fileContent = null;
					file = null;
					
					categoriaProduto = new CategoriaProduto();
					fornecedor = new Fornecedor();
					
					PrimeFaces.current().executeScript(
							"swal({ type: 'success', title: 'Concluído!', text: 'Produto cadastrado com sucesso!' });");
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
				
				//fileContent = file.getContents();	
								
				String json = Uploader.upload(file);
				//System.out.println(json);
				
				JSONObject jObj = new JSONObject(json);
				jObj = new JSONObject(jObj.get("data").toString());
				System.out.println(jObj.get("link"));
				
				produto.setUrlImagem(jObj.get("link").toString());
				
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
		
}
