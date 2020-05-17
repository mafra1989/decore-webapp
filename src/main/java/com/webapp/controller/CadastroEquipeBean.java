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
import org.primefaces.json.JSONObject;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.webapp.model.Grupo;
import com.webapp.model.Usuario;
import com.webapp.repository.Grupos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.UsuarioFilter;
import com.webapp.upload.Uploader;
import com.webapp.upload.WebException;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CadastroEquipeBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Grupos grupos;
	
	@Inject
	private Grupo grupo;

	private List<Grupo> todosGrupos;

	private Usuario membroSelecionado;

	private UsuarioFilter filtro = new UsuarioFilter();
	
	private UploadedFile file;
	
	private byte[] fileContent;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
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
		
		try {
			grupo = usuarioTemp.getGrupos().get(0);
			
		} catch(IndexOutOfBoundsException e) {
			grupo = null;
		}
		
		todosGrupos = grupos.todos();
		
	}

	public void salvar() {

		try {
			
			if(usuario != null && usuario.getLogin() != null && usuario.getSenha() != null && grupo != null && grupo.getId() != null) {
				
				if(StringUtils.isNotBlank(usuario.getLogin()) && StringUtils.isNotBlank(usuario.getSenha())) {

					BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
					String hashedPassword = passwordEncoder.encode(usuario.getSenha());

					usuario.setSenha(hashedPassword);
					
					List<Grupo> listaDeGrupos = new ArrayList<Grupo>();
					listaDeGrupos.add(grupo);
					usuario.setGrupos(listaDeGrupos);					

				} else {
					
					if(usuario.getId() == null) {
						usuario.setLogin(null);
						usuario.setSenha(null);
						usuario.setGrupos(null);
					} else {
						Usuario usuarioTemp = usuarios.porId(usuario.getId());
						usuario.setLogin(usuarioTemp.getLogin());
						usuario.setSenha(usuarioTemp.getSenha());					
						usuario.setGrupos(usuarioTemp.getGrupos());
					}
				}
			} else {
				
				if(usuario != null && usuario.getId() == null) {
					usuario.setLogin(null);
					usuario.setSenha(null);
					usuario.setGrupos(null);
				} else {
					Usuario usuarioTemp = usuarios.porId(usuario.getId());
					usuario.setLogin(usuarioTemp.getLogin());
					usuario.setSenha(usuarioTemp.getSenha());					
					usuario.setGrupos(usuarioTemp.getGrupos());
				}
			}

			usuarios.save(usuario);

			membroSelecionado = null;

			listarTodos();

			PrimeFaces.current().executeScript(
					"PF('downloadLoading').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Membro de equipe salvo com sucesso!' });");


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
		todosUsuarios = usuarios.filtrados(filtro);
		membroSelecionado = null;
	}

	private void listarTodos() {
		todosUsuarios = usuarios.todos();
		todosGrupos = grupos.todos();
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
		fileContent = membroSelecionado.getFoto();
	}

}
