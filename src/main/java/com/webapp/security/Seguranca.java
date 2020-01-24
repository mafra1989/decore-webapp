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
		}
		
		return nome;
	}

	@Produces
	@UsuarioLogado
	public UsuarioSistema getUsuarioLogado() {
		UsuarioSistema usuario = null;
		
		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) 
				FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
		
		if (auth != null && auth.getPrincipal() != null) {
			usuario = (UsuarioSistema) auth.getPrincipal();
		}
		
		return usuario;
	}
	
	/* MENU SEARCH */
	
	/* PART LIST */
	public boolean acessoPartList() {
		System.out.println(externalContext.isUserInRole("ADMINISTRADORES"));
		return externalContext.isUserInRole("ADMINISTRADORES");
	}

	/* COMPARATION BOM vs BOM */
	public boolean acessoBOMvsBOM() {
		return externalContext.isUserInRole("ADMINISTRADORES") || externalContext.isUserInRole("USUARIOS");
	}
	
	
	
	public String getImageContentsAsBase64() {
	    return Base64.getEncoder().encodeToString(fileContent);
	}
	
	public byte[] getFileContent() {
		fileContent = getUsuarioLogado().getUsuario().getFoto();
		return fileContent;
	}
	
	
}
