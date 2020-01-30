package com.webapp.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.Usuario;
import com.webapp.repository.Lancamentos;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaLancamentosBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private List<Lancamento> lancamentosFiltrados;
	
	private List<Usuario> todosUsuarios;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Lancamentos lancamentos;

	
	private Lancamento lancamentoSelecionado;
	
	private Date dateStart = new Date();
	
	private Date dateStop = new Date();
	
	private OrigemLancamento origemLancamento;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {	
			//todosUsuarios = usuarios.todos();
		}
	}
	
	public void pesquisar() { 	
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		lancamentosFiltrados = lancamentos.lancamentosFiltrados(dateStart, calendarioTemp.getTime(), origemLancamento);
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


	public Lancamento getLancamentoSelecionado() {
		return lancamentoSelecionado;
	}

	public void setLancamentoSelecionado(Lancamento lancamentoSelecionado) {
		this.lancamentoSelecionado = lancamentoSelecionado;
	}

	public List<Lancamento> getLancamentosFiltrados() {
		return lancamentosFiltrados;
	}

	public void setLancamentosFiltrados(List<Lancamento> comprasFiltradas) {
		this.lancamentosFiltrados = comprasFiltradas;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	public OrigemLancamento[] getOrigensLancamentos() {
		return OrigemLancamento.values();
	}

	public OrigemLancamento getOrigemLancamento() {
		return origemLancamento;
	}

	public void setOrigemLancamento(OrigemLancamento origemLancamento) {
		this.origemLancamento = origemLancamento;
	}

}
