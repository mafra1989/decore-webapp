package com.webapp.security;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class AcessoAutorizadoListener implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Authentication authentication) throws IOException, ServletException {
	
		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
		/*
		if (roles.contains("ROLE_ADMINISTRADOR")) {
			httpServletResponse.sendRedirect("/Empresas.xhtml");
			
		} else 
		*/
		if(roles.contains("ROLE_VENDEDOR")) {
			httpServletResponse.sendRedirect("/PDV.xhtml");
			
		} else {
			httpServletResponse.sendRedirect("/Dashboard.xhtml");
		}
		
	}

}