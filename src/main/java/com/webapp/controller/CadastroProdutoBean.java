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
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Fornecedor;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Fornecedores;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
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
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todasCategoriasProdutos();
			todosFornecedores();
		}
	}
	
	public void buscar() {
		produto = produtos.porId(produto.getId());
		fileContent = produto.getFoto();
		
		Long totalItensComprados = 0L;
		List<ItemCompra> itensCompra = itensCompras.porCompra(produto);
		for (ItemCompra itemCompra : itensCompra) {
			produto.setTotalCompras(BigDecimal.valueOf(produto.getTotalCompras().doubleValue() + (itemCompra.getValorUnitario().doubleValue() * itemCompra.getQuantidade())));
			totalItensComprados += itemCompra.getQuantidade();
		}
			
		Long totalItensVendidos = 0L;
		List<ItemVenda> itensVenda = itensVendas.porVenda(produto);
		for (ItemVenda itemVenda : itensVenda) {
			produto.setTotalVendas(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() + (itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade())));
			totalItensVendidos += itemVenda.getQuantidade();
			System.out.println(totalItensVendidos);
		}
		
		produto.setTotalAcumulado(BigDecimal.valueOf(produto.getQuantidadeAtual() * produto.getPrecoVenda().doubleValue()));
		
		if(totalItensVendidos > 0) {
			produto.setPrecoMedioVenda(BigDecimal.valueOf(produto.getTotalVendas().doubleValue() / BigDecimal.valueOf(totalItensVendidos).intValue()));
		} else {
			produto.setPrecoMedioVenda(BigDecimal.ZERO);
		}
		
		produto.setQuantidadeItensComprados(totalItensComprados);
		
		produto.setQuantidadeItensVendidos(totalItensVendidos);
	}
	
	public void salvar() {

		if(fileContent != null) {
			produto.setFoto(fileContent);
		}

		if (produto.getId() == null) {			

			produto = produtos.save(produto);
			
			produto = new Produto();
			fileContent = null;
			file = null;
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Conclu�do!', text: 'Produto cadastrado com sucesso!' });");			
	
		} else {
/*			
			List<Parcela> todasParcelas = parcelas.todasParcelas(emprestimo.getId());

			if (todasParcelas.size() > 0) {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Já existem parcelas lançadas para esse contrato.' });");
				
			} else {
				emprestimo = emprestimos.save(emprestimo);
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Empréstimo atualizado com sucesso!' });");
			}
*/			
		}

	}
	
	private void todasCategoriasProdutos() {
		todasCategoriasProdutos = categoriasProdutos.todos();
	}
	
	private void todosFornecedores() {
		todosFornecedores = fornecedores.todos();
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
			fileContent = file.getContents();						
			
		} else {
			FacesUtil.addErrorMessage("Selecione uma imagem!");
		}
	}

	public byte[] getFileContent() {
		return fileContent;
	}
	
}
