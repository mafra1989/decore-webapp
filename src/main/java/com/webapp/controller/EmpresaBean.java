package com.webapp.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class EmpresaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String empresa;

	public void defineEmpresa(String empresa) throws IOException {
		this.empresa = empresa;
		FacesContext.getCurrentInstance().getExternalContext().redirect("/Dashboard.xhtml");
	}

	public String getEmpresa() {
		return empresa;
	}
}