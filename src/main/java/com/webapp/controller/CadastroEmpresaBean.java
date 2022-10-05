package com.webapp.controller;

import java.io.Serializable;
import java.util.Base64;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Empresa;
import com.webapp.model.Usuario;
import com.webapp.repository.Empresas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.EmpresaFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEmpresaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Empresas empresas;

	@Inject
	private Empresa empresa;

	private List<Empresa> todasEmpresas;

	private Empresa empresaSelecionada;

	private EmpresaFilter filtro = new EmpresaFilter();
	
	private UploadedFile file;
	
	private byte[] fileContent;
	
	private UploadedFile file_;
	
	private byte[] fileContent_;
	
	@SuppressWarnings("unused")
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario = usuarios.porLogin(user.getUsername());
			
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		empresa = new Empresa();
	}

	public void prepararEditarCadastro() {
		empresa = empresaSelecionada;
	}

	public void salvar() {

		empresas.save(empresa);

		empresaSelecionada = null;

		listarTodos();

		PrimeFaces.current().executeScript(
				"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Empresa salva com sucesso!' });");
	}

	public void excluir() {

		try {
			empresas.remove(empresaSelecionada);
	
			empresaSelecionada = null;
	
			pesquisar();
	
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Empresa excluída com sucesso!' });");
			
		} catch(Exception e) {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Ops!', text: 'Não foi possível excluir a empresa!' });");
		}

	}

	public void pesquisar() {
		todasEmpresas = empresas.filtrados(filtro);
		empresaSelecionada = null;
	}

	private void listarTodos() {
		todasEmpresas = empresas.todos(usuario.getEmpresa());
	}

	public List<Empresa> getTodasEmpresas() {
		return todasEmpresas;
	}

	public EmpresaFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(EmpresaFilter filtro) {
		this.filtro = filtro;
	}

	public Empresa getEmpresaSelecionada() {
		return empresaSelecionada;
	}

	public void setEmpresaSelecionada(Empresa empresaSelecionada) {
		this.empresaSelecionada = empresaSelecionada;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public byte[] getFileContent() {
		return fileContent;
	}
	
	public UploadedFile getFile_() {
		return file_;
	}

	public void setFile_(UploadedFile file_) {
		this.file_ = file_;
	}

	public byte[] getFileContent_() {
		return fileContent_;
	}

	public String getImageContentsAsBase64() {
	    return Base64.getEncoder().encodeToString(fileContent);
	}
	
	public void upload() {
		if(file != null && file.getFileName() != null) {
			
			try {
				
				fileContent = file.getContent();
				
				empresaSelecionada.setLogo(fileContent);
				empresaSelecionada = empresas.save(empresaSelecionada);
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Sua logo foi adicionada ao sistema com sucesso!' });");
				
			} catch(Exception e) {
				
				e.printStackTrace();
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Erro ao enviar logo da empresa: " + empresaSelecionada.getNome() + "!' });");
			}
						
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Por favor, selecione uma imagem!' });"); 
		}
	}
	
	
	public void upload_() {
		if(file_ != null && file_.getFileName() != null) {
			
			try {
				
				fileContent_ = file_.getContent();
				
				empresaSelecionada.setLogoRelatorio(fileContent_);
				empresaSelecionada = empresas.save(empresaSelecionada);
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Sua logo foi adicionada aos relatórios com sucesso!' });");
				
			} catch(Exception e) {
				
				e.printStackTrace();
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Erro ao enviar logo da empresa: " + empresaSelecionada.getNome() + "!' });");
			}
						
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Por favor, selecione uma imagem!' });"); 
		}
	}
}