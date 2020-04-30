package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Conta;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Contas;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.Lancamentos;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaLancamentosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Lancamento lancamento;

	private List<Lancamento> lancamentosFiltrados = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuario usuario;

	@Inject
	private Lancamentos lancamentos;

	private Lancamento lancamentoSelecionado;
	
	private Long numeroLancamento;

	private Date dateStart = new Date();

	private Date dateStop = new Date();

	private OrigemLancamento[] origemLancamento;

	@Inject
	private CategoriasLancamentos categoriasDespesas;

	private List<CategoriaLancamento> todasCategoriasDespesas;

	@Inject
	private DestinosLancamentos destinosLancamentos;

	private List<DestinoLancamento> todosDestinosLancamentos;

	@Inject
	private CategoriaLancamento categoriaLancamento;

	@Inject
	private DestinoLancamento destinoLancamento;
	
	@Inject
	private Contas contas;

	private NumberFormat nf = new DecimalFormat("###,##0.00");

	private String totalLancamentos = "0,00";

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			// todosUsuarios = usuarios.todos();
			todasCategoriasDespesas = categoriasDespesas.todos();
			todosDestinosLancamentos = destinosLancamentos.todos();
		}
	}

	public void pesquisar() {
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		lancamentosFiltrados = new ArrayList<>();
		lancamentosFiltrados = lancamentos.lancamentosFiltrados(numeroLancamento, dateStart, calendarioTemp.getTime(), origemLancamento,
				categoriaLancamento, destinoLancamento);

		double totalLancamentosTemp = 0;
		for (Lancamento lancamento : lancamentosFiltrados) {
			totalLancamentosTemp += lancamento.getValor().doubleValue();
		}

		totalLancamentos = nf.format(totalLancamentosTemp);
		
		if(origemLancamento.length > 0) {
			for (Lancamento lancamento : lancamentosFiltrados) {
				Conta conta = new Conta();
				conta.setOperacao("LANCAMENTO");
				conta.setCodigoOperacao(lancamento.getNumeroLancamento());
				conta.setVencimento(lancamento.getDataLancamento());
				conta.setPagamento(lancamento.getDataLancamento());
				conta.setValor(lancamento.getValor());
				conta.setParcela("AVISTA");
				conta.setTipo("DEBITO");
				conta.setStatus(true);
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(lancamento.getDataLancamento());
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
				
				contas.save(conta);
				
				System.out.println(lancamento.getNumeroLancamento());
			}
		}
	}

	public void changeCategoria() {
		if (categoriaLancamento == null) {
			destinoLancamento = null;
		} else {
			destinoLancamento = categoriaLancamento.getDestinoLancamento();
		}
	}

	public void excluir() {

		boolean contasPagas = false;
		//List<Conta> contasTemp = contas.porContasPagas(lancamentoSelecionado.getNumeroLancamento(), "LANCAMENTO");

		//if (contasTemp.size() == 0) {

		List<Conta> contasTemp = contas.porCodigoOperacao(lancamentoSelecionado.getNumeroLancamento(), "LANCAMENTO");
		for (Conta conta : contasTemp) {
			contas.remove(conta);
		}

		/*} else {

			String tipoConta = "";
			if (contasTemp.get(0).getTipo().equals("DEBITO")) {
				tipoConta = "contas à pagar";
			} else {
				tipoConta = "contas à receber";
			}

			contasPagas = true;
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Erro!', text: 'Existe " + tipoConta
					+ " já registradas para esse lançamento!' });");
		}*/

		if (contasPagas != true) {
			lancamentos.remove(lancamentoSelecionado);

			lancamentoSelecionado = null;
			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Lançamento excluído com sucesso!' });");
		}
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

	public int getLancamentosFiltradosSize() {
		return lancamentosFiltrados.size();
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

	public OrigemLancamento[] getOrigemLancamento() {
		return origemLancamento;
	}

	public void setOrigemLancamento(OrigemLancamento[] origemLancamento) {
		this.origemLancamento = origemLancamento;
	}

	public List<CategoriaLancamento> getTodasCategoriasDespesas() {
		return todasCategoriasDespesas;
	}

	public List<DestinoLancamento> getTodosDestinosLancamentos() {
		return todosDestinosLancamentos;
	}

	public CategoriaLancamento getCategoriaLancamento() {
		return categoriaLancamento;
	}

	public void setCategoriaLancamento(CategoriaLancamento categoriaLancamento) {
		this.categoriaLancamento = categoriaLancamento;
	}

	public Lancamento getLancamento() {
		return lancamento;
	}

	public void setLancamento(Lancamento lancamento) {
		this.lancamento = lancamento;
	}

	public DestinoLancamento getDestinoLancamento() {
		return destinoLancamento;
	}

	public void setDestinoLancamento(DestinoLancamento destinoLancamento) {
		this.destinoLancamento = destinoLancamento;
	}

	public String getTotalLancamentos() {
		return totalLancamentos;
	}

	public Long getNumeroLancamento() {
		return numeroLancamento;
	}

	public void setNumeroLancamento(Long numeroLancamento) {
		this.numeroLancamento = numeroLancamento;
	}

}
