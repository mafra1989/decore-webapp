package com.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Log;
import com.webapp.model.TipoAtividade;
import com.webapp.model.Usuario;
import com.webapp.repository.Logs;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class AtividadesBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Usuario usuario_;
	
	@Inject
	private Usuarios usuarios;

	private Date dateStart = new Date();

	private Date dateStop = new Date();
	
	private TipoAtividade tipoAtividade;
	
	@Inject
	private Usuario usuario;
	
	private List<Usuario> todosUsuarios;
	
	private List<Log> logsFiltrados = new ArrayList<>();
	
	private Log logSelecionado;
	
	@Inject
	private Logs logs;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario_ = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
		}
	}

	public void pesquisar() {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		logSelecionado = null;
		
		logsFiltrados = new ArrayList<>();
		logsFiltrados = logs.logsFiltrados(dateStart, calendarioTemp.getTime(), tipoAtividade, usuario, usuario_.getEmpresa());
		
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
	
	
	public TipoAtividade[] getTiposAtividades() {
		return TipoAtividade.values();
	}

	public TipoAtividade getTipoAtividade() {
		return tipoAtividade;
	}

	public void setTipoAtividade(TipoAtividade tipoAtividade) {
		this.tipoAtividade = tipoAtividade;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public Log getLogSelecionado() {
		return logSelecionado;
	}

	public void setLogSelecionado(Log logSelecionado) {
		this.logSelecionado = logSelecionado;
	}

	public List<Log> getLogsFiltrados() {
		return logsFiltrados;
	}

}
