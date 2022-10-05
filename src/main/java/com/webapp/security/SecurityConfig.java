package com.webapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Bean
	public AcessoAutorizadoListener successHandler() {
		return new AcessoAutorizadoListener();
	}

	@Override
	@Bean
	public AppUserDetailsService userDetailsService() {
		return new AppUserDetailsService();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		JsfLoginUrlAuthenticationEntryPoint jsfLoginEntry = new JsfLoginUrlAuthenticationEntryPoint();
		jsfLoginEntry.setLoginFormUrl("/Login.xhtml");
		jsfLoginEntry.setRedirectStrategy(new JsfRedirectStrategy());
		
		JsfAccessDeniedHandler jsfDeniedEntry = new JsfAccessDeniedHandler();
		jsfDeniedEntry.setLoginPath("/403.xhtml");
		jsfDeniedEntry.setContextRelative(true);
		
		http
			.csrf().disable()
			.headers().frameOptions().sameOrigin()
			.and()
			
			/*
			 .requiresChannel()
	            .anyRequest()
	            .requiresSecure().and()
	            */
		
		.authorizeRequests()
			.antMatchers("/Login.xhtml", "/Cadastro.xhtml", "/404.xhtml", "/500.xhtml", "/javax.faces.resource/**").permitAll()
			.antMatchers("/403.xhtml").authenticated()
			
			.antMatchers(
					"/Dashboard.xhtml",
					"/Financeiro.xhtml",
					"/Contas.xhtml", 
					"/Caixa.xhtml", 
					"/PDV.xhtml", 
					"/DevolucaoTroca.xhtml", 
					"/cadastros/CadastroProdutos.xhtml",
					"/operacoes/RegistroCompras.xhtml",
					"/operacoes/RegistroVendas.xhtml",
					"/operacoes/RegistroLancamentos.xhtml",
					"/consultas/Estoque.xhtml",
					"/consultas/Compras.xhtml",
					"/consultas/Vendas.xhtml",
					"/consultas/Lancamentos.xhtml",
					
					"/operacoes/RegistroLancamentos.xhtml",
					"/consultas/Orcamentos.xhtml",				
					
					"/relatorios/Vendas.xhtml",
					"/relatorios/Lucros.xhtml",
					"/relatorios/Lancamentos.xhtml",
					"Configuracoes.xhtml")
			.hasAnyRole("ADMINISTRADOR", "USUARIO_AVANCADO", "USUARIO_COMUM")
			
			.antMatchers(
					"/Caixa.xhtml",
					"/PDV.xhtml",
					"/DevolucaoTroca.xhtml", 
					"/cadastros/CadastroProdutos.xhtml",
					"/consultas/Estoque.xhtml",
					"/consultas/Vendas.xhtml")
			.hasAnyRole("VENDEDOR", "ADMINISTRADOR", "USUARIO_AVANCADO", "USUARIO_COMUM")
			
			.antMatchers(
					"/PDV.xhtml")
			.hasAnyRole("VENDEDOR", "ADMINISTRADOR")
			
			.antMatchers("/Empresas.xhtml", 
					"/cadastros/CadastroBairros.xhtml", 
					"/cadastros/CadastroCategoriaLancamento.xhtml", 
					"/cadastros/CadastroCategoriaProduto.xhtml", 
					"/cadastros/CadastroClientes.xhtml",
					"/cadastros/CadastroDestinoLancamento.xhtml", 
					"/cadastros/CadastroEmpresas.xhtml",
					"/cadastros/CadastroEquipe.xhtml", 
					"/cadastros/CadastroFormaPagamento.xhtml",
					"/cadastros/CadastroFornecedores.xhtml", 
					"/cadastros/CadastroTipoLancamento.xhtml", 
					"/cadastros/CadastroTipoVenda.xhtml", 
					"/Atividades.xhtml",
					"/importar/ImportarDados.xhtml")
			.hasAnyRole("ADMINISTRADOR")
			.and()
		
		.formLogin()
			.loginPage("/Login.xhtml")
			.failureUrl("/Login.xhtml?invalid=true")
			.successHandler(successHandler())
			.and()
		
		.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.and()
		
		.exceptionHandling()
			.accessDeniedPage("/500.xhtml")
			.authenticationEntryPoint(jsfLoginEntry)
			.accessDeniedHandler(jsfDeniedEntry);

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}