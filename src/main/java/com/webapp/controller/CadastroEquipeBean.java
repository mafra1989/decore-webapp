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

import com.webapp.manhattan.view.GuestPreferences.LayoutMode;
import com.webapp.model.Compra;
import com.webapp.model.Configuracao;
import com.webapp.model.Empresa;
import com.webapp.model.Grupo;
import com.webapp.model.Lancamento;
import com.webapp.model.Log;
import com.webapp.model.TipoImpressao;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Compras;
import com.webapp.repository.Configuracoes;
import com.webapp.repository.Empresas;
import com.webapp.repository.Grupos;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
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
	private Configuracoes configuracoes;

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
	
	@Inject
	private Vendas vendas;
	
	@Inject
	private Compras compras;
	
	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private Logs logs;
	

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
		
		todosGrupos = grupos.todos(usuario_.getEmpresa());	
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
							usuario = usuarios.save(usuario);
							
							salvaConfiguracao();				
							
							membroSelecionado = null;

							listarTodos();
							
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Funcionário atualizado com sucesso!' });");
						
							PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
							
						} else {
							usuario.setSenha("");
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'O login informado já existe!' });");
						}
						
					} else {
						
						Usuario usuarioTemp_ = usuarios.porLogin(usuario.getLogin());					
						
						if(usuarioTemp_ == null) {
							
							usuario.setEntregador(entregador);
							usuario = usuarios.save(usuario);
							
							salvaConfiguracao();
							
							membroSelecionado = null;

							listarTodos();
							
							PrimeFaces.current().executeScript(
									"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Funcionário salvo com sucesso!' });");
						
							PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
							
						} else {
							usuario.setSenha("");
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
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Funcionário salvo com sucesso!' });");
						
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
						
					} else {
						Usuario usuarioTemp = usuarios.porId(usuario.getId());
						usuario.setLogin(usuarioTemp.getLogin());
						usuario.setSenha(usuarioTemp.getSenha());					
						usuario.setGrupos(usuarioTemp.getGrupos());
						
						usuario.setEntregador(entregador);
						usuario = usuarios.save(usuario);
						
						salvaConfiguracao();
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Funcionário atualizado com sucesso!' });");
					
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
						usuario = usuarios.save(usuario);
						
						salvaConfiguracao();
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Funcionário salvo com sucesso!' });");
						
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
						
					} else {
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'Informe a função e a empresa do funcionário!' });");
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
						usuario = usuarios.save(usuario);
						
						salvaConfiguracao();
						
						membroSelecionado = null;

						listarTodos();
						
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); PF('usuario-dialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Funcionário salvo com sucesso!' });");
					
						PrimeFaces.current().ajax().update("msg", "form-dialog", "form");
						
					} else {
						usuario.setSenha("");
						PrimeFaces.current().executeScript(
								"PF('downloadLoading').hide(); swal({ type: 'error', title: 'Erro!', text: 'O login informado já existe!' });");
					}
					
				}
			}
			

		} catch (Exception e) {
			e.printStackTrace();
			FacesUtil.addErrorMessage("Erro ao cadastrar funcionário!");
		}

	}
	
	private void salvaConfiguracao() {
		
		Configuracao configuracao = configuracoes.porUsuario(usuario);
		if(configuracao == null) {
			
			configuracao = new Configuracao();
			configuracao.setLeitorPDV(false);
			configuracao.setLeitorCompra(false);
			configuracao.setCupomAtivado(true);
			configuracao.setTipoImpressao(TipoImpressao.IMPRESSORA01);
			configuracao.setAbaPDV(1);
			configuracao.setLayoutMode(LayoutMode.STATIC);
			configuracao.setLightMenu(false);
			configuracao.setVias(1);
			configuracao.setPdvRapido(false);
			configuracao.setPopupCliente(false);
			configuracao.setControleMesas(true);
			configuracao.setProdutosUrlNuvem(true);
			configuracao.setUsuario(usuario);
			configuracao.setQuantidadeMesas(10);
			
			configuracoes.save(configuracao);
		}
	}

	public void excluir() {
		
		List<Venda> todasVendas = vendas.porVendedor(membroSelecionado);		
		if(todasVendas.size() == 0) {
			
			List<Compra> todasCompras = compras.porUsuario(membroSelecionado);
			if(todasCompras.size() == 0) {
				
				List<Lancamento> todosLancamentos = lancamentos.porUsuario(membroSelecionado);
				if(todosLancamentos.size() == 0) {
					
					List<Log> todasLogs = logs.porUsuario(membroSelecionado);
					
					if(todasLogs.size() > 0) {
						for (Log log : todasLogs) {
							logs.remove(log);
						}			
					}
					
					Configuracao configuracao = configuracoes.porUsuario(membroSelecionado);
					configuracoes.remove(configuracao);
				}			
			}
		}

		usuarios.remove(membroSelecionado);

		membroSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Funcionário excluído com sucesso!' });");

	}

	public void pesquisar() {
		filtro.setEmpresa(usuario_.getEmpresa());
		todosUsuarios = usuarios.filtrados(filtro);
		membroSelecionado = null;
	}

	private void listarTodos() {
		todosUsuarios = usuarios.todos_(usuario_.getEmpresa());
		todosGrupos = grupos.todos(usuario_.getEmpresa());
		todasEmpresas = empresas.todos(usuario_.getEmpresa());
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
