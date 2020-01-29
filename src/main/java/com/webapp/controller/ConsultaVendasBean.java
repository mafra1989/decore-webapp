package com.webapp.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaVendasBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private List<Venda> vendasFiltradas;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Vendas vendas;

	
	private Venda vendaSelecionada;
	
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
		
		vendasFiltradas = vendas.vendasFiltradas(dateStart, calendarioTemp.getTime(), usuario);
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


	public Venda getVendaSelecionada() {
		return vendaSelecionada;
	}

	public void setVendaSelecionada(Venda vendaSelecionada) {
		this.vendaSelecionada = vendaSelecionada;
	}

	public List<Venda> getVendasFiltradas() {
		return vendasFiltradas;
	}

	public void setVendasFiltradas(List<Venda> vendasFiltradas) {
		this.vendasFiltradas = vendasFiltradas;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}
