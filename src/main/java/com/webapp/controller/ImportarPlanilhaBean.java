package com.webapp.controller;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.UploadedFile;

import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ImportarPlanilhaBean implements Serializable {

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
		System.out.println("File: " + file);
		if(file != null && file.getFileName() != null) {
			fileContent = file.getContents();
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Planilha importada com sucesso!' });");
			
			fileContent = null;
			
		} else {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Nenhuma planilha anexada!' });");
		}
		
		System.out.println(fileContent);
		
		
	}
		
}
