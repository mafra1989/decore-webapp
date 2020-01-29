package com.webapp.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.ItemCompra;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoLancamento;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Lancamentos;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class LancamentoDespesasBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Lancamento despesa;
	
	@Inject
	private Lancamentos despesas;
	
	@Inject
	private CategoriasLancamentos categoriasDespesas;
	
	private List<DestinoLancamento> todosDestinosLancamentos;
	
	private List<TipoLancamento> todosTiposDespesas;
	
	private List<CategoriaLancamento> todasCategoriasDespesas;
	
	@Inject
	private CategoriaLancamento categoriaLancamento;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {			
			todasCategoriasDespesas = categoriasDespesas.todos();
		}
	}
	
	public void buscar() {
		despesa = despesas.porId(despesa.getId());
		categoriaLancamento = despesa.getCategoriaLancamento();
	}
	
	public void changeCategoria() { 
		if(categoriaLancamento == null) {
			despesa.setCategoriaLancamento(new CategoriaLancamento());
		} else {
			despesa.setCategoriaLancamento(categoriaLancamento);
		}
	}

	public void salvar() {
		
		if(despesa.getId() == null) {
			//despesa.setDataLancamento(new Date());
		}
		
		Calendar calendario = Calendar.getInstance();	
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(despesa.getDataLancamento());
		calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
		calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
		calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
		despesa.setDataLancamento(calendarioTemp.getTime());
		
		despesa.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
		despesa.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
		despesa.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
		despesa.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
		despesa.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
		
		despesas.save(despesa);
		
		if(despesa.getId() == null) {

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Lançamento registrado com sucesso!' });");
			
			despesa = new Lancamento();
			categoriaLancamento = new CategoriaLancamento();
		} else {
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Lançamento salvo com sucesso!' });");
		}

	}
	

	public Lancamento getDespesa() {
		return despesa;
	}

	public void setDespesa(Lancamento despesa) {
		this.despesa = despesa;
	}

	public List<DestinoLancamento> getTodosDestinosLancamentos() {
		return todosDestinosLancamentos;
	}

	public List<TipoLancamento> getTodosTiposDespesas() {
		return todosTiposDespesas;
	}

	public List<CategoriaLancamento> getTodasCategoriasDespesas() {
		return todasCategoriasDespesas;
	}
	
	public OrigemLancamento[] getOrigensLancamentos() {
		return OrigemLancamento.values();
	}

	public CategoriaLancamento getCategoriaLancamento() {
		return categoriaLancamento;
	}

	public void setCategoriaLancamento(CategoriaLancamento categoriaLancamento) {
		this.categoriaLancamento = categoriaLancamento;
	}
}
