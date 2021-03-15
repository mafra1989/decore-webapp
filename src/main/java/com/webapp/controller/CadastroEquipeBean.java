package com.webapp.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.shaded.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.webapp.model.Empresa;
import com.webapp.model.Grupo;
import com.webapp.model.Usuario;
import com.webapp.repository.Empresas;
import com.webapp.repository.Grupos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.UsuarioFilter;
import com.webapp.uploader.Uploader;
import com.webapp.uploader.WebException;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuario usuario_;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Grupos grupos;
	
	@Inject
	private Empresas empresas;
	
	@Inject
	private Grupo grupo;

	private List<Grupo> todosGrupos;
	
	private List<Empresa> todasEmpresas;

	private Usuario membroSelecionado;

	private UsuarioFilter filtro = new UsuarioFilter();
	
	private UploadedFile file;
	
	private byte[] fileContent;
	
	private boolean entregador = false;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();			
			usuario_ = usuarios.porLogin(user.getUsername());
			
			listarTodos();
		}
	}

	public void prepararNovoCadastro() {
		usuario = new Usuario();
		grupo = new Grupo();
	}

	public void prepararEditarCadastro() {
		
		usuario = membroSelecionado;
		usuario.setSenha("");
		Usuario usuarioTemp = usuarios.porId(usuario.getId());
		entregador = usuario.isEntregador();
		
		try {
			grupo = usuarioTemp.getGrupos().get(0);
			
		} catch(IndexOutOfBoundsException e) {
			grupo = null;
		}
		
		todosGrupos = grupos.todos();	
	}

	public void salvar() {
		
		//usuario.setEmpresa(usuario_.getEmpresa());

        try {
			
			if(usuario != null && usuario.getLogin() != null && usuario.getSenha() != null && grupo != null && grupo.getId() != null) {
				
				if(StringUtils.isNotBlank(usuario.getLogin()) && StringUtils.isNotBlank(usuario.getSenha())) {

					BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
					String hashedPassword = passwordEncoder.encode(usuario.getSenha());

					usuario.setSenha(hashedPassword);
					
					List<Grupo> listaDeGrupos = new ArrayList<Grupo>();
					listaDeGrupos.add(grupo);
					usuario.setGrupos(listaDeGrupos);		
					
					if(usuario.getId() != null) {
						
						Usuario usuarioTemp_ = usuarios.porLogin(usuario.getLogin(), usuario.getId());					
						
						if(usuarioTemp_ == null) {
							
							usuario.setEntregador(entregador);
							usuarios.save(usuario);
							
							membroSelecionado = null;

							listarTodos();
							
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe atualizado com sucesso!' });");
						
							PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
							
						} else {
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'O login informado já existe!' });");
						}
						
					} else {
						
						Usuario usuarioTemp_ = usuarios.porLogin(usuario.getLogin());					
						
						if(usuarioTemp_ == null) {
							
							usuario.setEntregador(entregador);
							usuarios.save(usuario);
							
							membroSelecionado = null;

							listarTodos();
							
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe salvo com sucesso!' });");
						
							PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
							
						} else {
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'O login informado já existe!' });");
						}
					}

				} else {
					
					if(usuario.getId() == null) {
						usuario.setLogin(null);
						usuario.setSenha(null);
						usuario.setGrupos(null);
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe salvo com sucesso!' });");
						
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
						
					} else {
						Usuario usuarioTemp = usuarios.porId(usuario.getId());
						usuario.setLogin(usuarioTemp.getLogin());
						usuario.setSenha(usuarioTemp.getSenha());					
						usuario.setGrupos(usuarioTemp.getGrupos());
						
						usuario.setEntregador(entregador);
						usuarios.save(usuario);
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe atualizado com sucesso!' });");
					
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
					}
				}
			} else {
				
				if(usuario != null && usuario.getId() == null) {
					usuario.setLogin(null);
					usuario.setSenha(null);
					usuario.setGrupos(null);
					
					if(usuario.getEmpresa() != null && usuario.getFuncao() != null) {
						
						usuario.setEntregador(entregador);
						usuarios.save(usuario);
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe salvo com sucesso!' });");
						
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
						
					} else {
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'Informe a função e a empresa do membro da equipe!' });");
					}			
					
				} else {
					Usuario usuarioTemp = usuarios.porId(usuario.getId());
					usuario.setLogin(usuarioTemp.getLogin());
					usuario.setSenha(usuarioTemp.getSenha());					
					usuario.setGrupos(usuarioTemp.getGrupos());
					
					Usuario usuarioTemp_ = null;
					if(usuario.getLogin() != null) {
						usuarioTemp_ = usuarios.porLogin(usuario.getLogin());
					}
					
					
					if(usuarioTemp_ == null) {
						
						usuario.setEntregador(entregador);
						usuarios.save(usuario);
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe salvo com sucesso!' });");
					
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
						
					} else {
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'O login informado já existe!' });");
					}
					
				}
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			FacesUtil.addErrorMessage("Erro ao cadastrar membro da equipe!");
		}

	}

	public void excluir() {

		usuarios.remove(membroSelecionado);

		membroSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe excluído com sucesso!' });");

	}

	public void pesquisar() {
		filtro.setEmpresa(usuario_.getEmpresa());
		todosUsuarios = usuarios.filtrados(filtro);
		membroSelecionado = null;
	}

	private void listarTodos() {
		todosUsuarios = usuarios.todos_(usuario_.getEmpresa());
		todosGrupos = grupos.todos();
		todasEmpresas = empresas.todos();
	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public List<Grupo> getTodosGrupos() {
		return todosGrupos;
	}

	public UsuarioFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(UsuarioFilter filtro) {
		this.filtro = filtro;
	}

	public Usuario getMembroSelecionado() {
		return membroSelecionado;
	}

	public void setMembroSelecionado(Usuario membroSelecionado) {
		this.membroSelecionado = membroSelecionado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Grupo getGrupo() {
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}
	
	public String getGrupo(Usuario usuario) {

		if(usuario.getLogin() != null) {
			usuario = usuarios.porLogin(usuario.getLogin());
	
			if(usuario != null) {
				if (usuario.getGrupos() != null && usuario.getGrupos().size() != 0) {
					return usuario.getGrupos().get(0).getDescricao();
				}
			}

		}

		return "";
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
				
				membroSelecionado.setUrlImagem(jObj.get("link").toString());
				
				//membroSelecionado.setFoto(fileContent);
				usuarios.save(membroSelecionado);
				
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Foto adicionada com sucesso!' });");
				
			} catch(WebException e) {
				
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Erro ao enviar imagem!' });");
			}
			
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Selecione uma imagem com até 200KB!' });");
		}
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
	
	public void prepareFoto() {
		//fileContent = membroSelecionado.getFoto();
	}

	public List<Empresa> getTodasEmpresas() {
		return todasEmpresas;
	}

	public void setTodasEmpresas(List<Empresa> todasEmpresas) {
		this.todasEmpresas = todasEmpresas;
	}

	public boolean isEntregador() {
		return entregador;
	}

	public void setEntregador(boolean entregador) {
		this.entregador = entregador;
	}

}
