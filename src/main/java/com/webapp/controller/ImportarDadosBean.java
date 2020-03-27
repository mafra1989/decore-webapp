package com.webapp.controller;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.UploadedFile;

import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ImportarDadosBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private UploadedFile file;
	
	private byte[] fileContent;
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
		}
	}
	
	public UploadedFile getFile() {
        return file;
    }
	
	public void setFile(UploadedFile file) {
        this.file = file;
    }
	
	public void importar() {

		if(file != null && file.getFileName() != null) {
			fileContent = file.getContents();
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Dados importados com sucesso!' });");
			
			fileContent = null;
			
		} else {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
		}
		
		System.out.println(fileContent);
		
		
	}
		
}
