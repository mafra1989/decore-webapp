package com.webapp.controller;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.webapp.model.Usuario;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class AlterarSenhaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Usuarios usuarios;

	@NotBlank
	private String senhaAtual;

	@NotBlank
	private String novaSenha;

	@NotBlank
	private String confirmacao;

	public void prepararAlterarSenha() {
		senhaAtual = "";
		novaSenha = "";
		confirmacao = "";
	}


	public void alterarSenha(Usuario usuario) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		if (passwordEncoder.matches(this.senhaAtual, usuario.getSenha())) {
			if (!this.novaSenha.equals(this.confirmacao)) {
				FacesUtil.addErrorMessage("Confirmação inválida!");
				//RequestContext.getCurrentInstance().update(Arrays.asList("msg-password"));

			} else {
				String hashedPassword = passwordEncoder.encode(this.novaSenha);
				usuario.setSenha(hashedPassword);
				usuarios.save(usuario);

				FacesUtil.addInfoMessage("Alteração realizada com sucesso!");
				//RequestContext.getCurrentInstance().update(Arrays.asList("msg-password"));
			}
		} else {
			FacesUtil.addErrorMessage("Senha atual inválida!");
			//RequestContext.getCurrentInstance().update(Arrays.asList("msg-password"));
		}
	}

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getNovaSenha() {
		return novaSenha;
	}

	public void setNovaSenha(String novaSenha) {
		this.novaSenha = novaSenha;
	}

	public String getConfirmacao() {
		return confirmacao;
	}

	public void setConfirmacao(String confirmacao) {
		this.confirmacao = confirmacao;
	}
}
