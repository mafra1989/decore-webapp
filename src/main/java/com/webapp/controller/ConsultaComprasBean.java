package com.webapp.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Compra;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private List<Compra> comprasFiltradas;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Compras compras;

	
	private Compra compraSelecionada;
	
	private Date dateStart = new Date();
	
	private Date dateStop = new Date();
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {	
			todosUsuarios = usuarios.todos();
		}
	}
	
	public void pesquisar() { 	
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		comprasFiltradas = compras.comprasFiltradas(dateStart, calendarioTemp.getTime(), usuario);
	}
	
	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}
	
	public Date getDateStart() {
		return dateStart;
	}

	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}

	public Date getDateStop() {
		return dateStop;
	}

	public void setDateStop(Date dateStop) {
		this.dateStop = dateStop;
	}


	public Compra getCompraSelecionada() {
		return compraSelecionada;
	}

	public void setCompraSelecionada(Compra compraSelecionada) {
		this.compraSelecionada = compraSelecionada;
	}

	public List<Compra> getComprasFiltradas() {
		return comprasFiltradas;
	}

	public void setComprasFiltradas(List<Compra> comprasFiltradas) {
		this.comprasFiltradas = comprasFiltradas;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}
