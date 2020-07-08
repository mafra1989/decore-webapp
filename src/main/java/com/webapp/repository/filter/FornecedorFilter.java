package com.webapp.repository.filter;

import java.io.Serializable;

public class FornecedorFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome = "";
	
	private String empresa = "";

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

}