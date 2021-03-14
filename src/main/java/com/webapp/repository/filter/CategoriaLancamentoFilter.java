package com.webapp.repository.filter;

import java.io.Serializable;

import com.webapp.model.Empresa;

public class CategoriaLancamentoFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome = "";
	
	private Empresa empresa;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}