package com.webapp.security;

import java.util.Base64;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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

		return usuario;
	}

	public boolean acessoAdministrador() {
		return externalContext.isUserInRole("ADMINISTRADOR");
	}

	public boolean acessoUsuarioAvancado() {
		return externalContext.isUserInRole("USUARIO_AVANCADO");
	}

	public boolean acessoUsuarioComum() {
		return externalContext.isUserInRole("USUARIO_COMUM");
	}
	
	public boolean acessoVendedor() {
		return externalContext.isUserInRole("VENDEDOR");
	}

	public String getImageContentsAsBase64_Logo() {
		return Base64.getEncoder().encodeToString(getUsuarioLogado().getUsuario().getEmpresa().getLogo());
	}

	public String getUrlImagem() {
		
		if (getUsuarioLogado().getUsuario().getUrlImagem() != null) {
			urlImagem = getUsuarioLogado().getUsuario().getUrlImagem();
		}

		return urlImagem;
	}

}
