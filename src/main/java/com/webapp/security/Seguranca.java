package com.webapp.security;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.webapp.controller.EmpresaBean;
import com.webapp.model.Grupo;
import com.webapp.util.jsf.FacesUtil;

@Named
@RequestScoped
public class Seguranca {

	@Inject
	private ExternalContext externalContext;

	private String urlImagem;
	

	public String getNomeUsuario() {
		String nome = null;

		UsuarioSistema usuarioLogado = getUsuarioLogado();

		if (usuarioLogado != null) {
			nome = usuarioLogado.getUsuario().getNome();

			if (usuarioLogado.getUsuario().getUrlImagem() != null) {
				urlImagem = getUsuarioLogado().getUsuario().getUrlImagem();
			}
		}

		return nome;
	}

	public String getPrimeiroNome() {
		String nome = null;

		UsuarioSistema usuarioLogado = getUsuarioLogado();

		if (usuarioLogado != null) {
			String nomeTemp = usuarioLogado.getUsuario().getNome();
			String[] dados = nomeTemp.split(" ");
			
			nome = dados[0];
		}

		return nome;
	}

	@Produces
	@UsuarioLogado
	public UsuarioSistema getUsuarioLogado() {
		UsuarioSistema usuario = null;

		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) FacesContext
				.getCurrentInstance().getExternalContext().getUserPrincipal();

		if (auth != null && auth.getPrincipal() != null) {
			usuario = (UsuarioSistema) auth.getPrincipal();
		}
		
		List<Grupo> grupos = usuario.getUsuario().getGrupos();
		
		if(grupos.size() > 0) {
			for (Grupo grupo : grupos) {
				if(grupo.getNome().equals("ADMINISTRADOR")) {
					EmpresaBean empresaBean = (EmpresaBean) FacesUtil.getObjectSession("empresaBean");
					if(empresaBean != null && empresaBean.getEmpresa() != null) {
						usuario.getUsuario().setEmpresa(empresaBean.getEmpresa());
					}
				}
			}
		}

		return usuario;
	}

	public boolean acessoAdministrador() {
		System.out.println(externalContext.isUserInRole("ADMINISTRADOR"));
		return externalContext.isUserInRole("ADMINISTRADOR");
	}

	public boolean acessoUsuarioAvancado() {
		System.out.println(externalContext.isUserInRole("USUARIO_AVANCADO"));
		return externalContext.isUserInRole("USUARIO_AVANCADO");
	}

	public boolean acessoUsuarioComum() {
		System.out.println(externalContext.isUserInRole("USUARIO_COMUM"));
		return externalContext.isUserInRole("USUARIO_COMUM");
	}
	
	public boolean acessoVendedor() {
		System.out.println(externalContext.isUserInRole("VENDEDOR"));
		return externalContext.isUserInRole("VENDEDOR");
	}

	/*public String getImageContentsAsBase64() {
		return Base64.getEncoder().encodeToString(fileContent);
	}*/

	public String getUrlImagem() {
		
		if (getUsuarioLogado().getUsuario().getUrlImagem() != null) {
			urlImagem = getUsuarioLogado().getUsuario().getUrlImagem();
		}

		return urlImagem;
	}

}
