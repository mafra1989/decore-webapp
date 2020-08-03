package com.webapp.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.json.JSONObject;
import org.primefaces.model.UploadedFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Grupo;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.CategoriaProdutoFilter;
import com.webapp.uploader.Uploader;
import com.webapp.uploader.WebException;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroCategoriaProdutoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CategoriasProdutos categoriasProdutos;

	@Inject
	private CategoriaProduto categoriaProduto;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;

	private List<CategoriaProduto> todasCategoriasProduto;

	private CategoriaProduto categoriaProdutoSelecionado;

	private CategoriaProdutoFilter filtro = new CategoriaProdutoFilter();
	
	private UploadedFile file;
	

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
			
			
			listarTodasCategoriasProdutos();
		}
	}

	public void prepararNovoCadastro() {
		categoriaProduto = new CategoriaProduto();
	}

	public void prepararEditarCadastro() {
		categoriaProduto = categoriaProdutoSelecionado;
	}

	public void salvar() {
		
		categoriaProduto.setEmpresa(usuario.getEmpresa());

		categoriasProdutos.save(categoriaProduto);

		categoriaProdutoSelecionado = null;

		listarTodasCategoriasProdutos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Categoria de produto salvo com sucesso!' });");
	}

	public void excluir() {

		categoriasProdutos.remove(categoriaProdutoSelecionado);

		categoriaProdutoSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Categoria de produto excluído com sucesso!' });");

	}

	public void pesquisar() {
		filtro.setEmpresa(usuario.getEmpresa());
		todasCategoriasProduto = categoriasProdutos.filtrados(filtro);
		categoriaProdutoSelecionado = null;
	}

	private void listarTodasCategoriasProdutos() {
		todasCategoriasProduto = categoriasProdutos.todos(usuario.getEmpresa());
	}

	public List<CategoriaProduto> getTodasCategoriasProdutos() {
		return todasCategoriasProduto;
	}

	public CategoriaProdutoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(CategoriaProdutoFilter filtro) {
		this.filtro = filtro;
	}

	public CategoriaProduto getCategoriaProdutoSelecionado() {
		return categoriaProdutoSelecionado;
	}

	public void setCategoriaProdutoSelecionado(CategoriaProduto categoriaProdutoSelecionado) {
		this.categoriaProdutoSelecionado = categoriaProdutoSelecionado;
	}

	public CategoriaProduto getCategoriaProduto() {
		return categoriaProduto;
	}

	public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
		this.categoriaProduto = categoriaProduto;
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

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
}