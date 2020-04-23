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

	private byte[] fileContent;

	public String getNomeUsuario() {
		String nome = null;

		UsuarioSistema usuarioLogado = getUsuarioLogado();

		if (usuarioLogado != null) {
			nome = usuarioLogado.getUsuario().getNome();

			if (usuarioLogado.getUsuario().getFoto() != null) {
				fileContent = getUsuarioLogado().getUsuario().getFoto();
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

	public String getImageContentsAsBase64() {
		return Base64.getEncoder().encodeToString(fileContent);
	}

	public byte[] getFileContent() {

		if (getUsuarioLogado().getUsuario().getFoto() != null) {
			fileContent = getUsuarioLogado().getUsuario().getFoto();
		}

		return fileContent;
	}

}
