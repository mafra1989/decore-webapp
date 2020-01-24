package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Emprestimo;
import com.webapp.model.Investidor;
import com.webapp.model.Parcela;
import com.webapp.model.StatusEmprestimo;
import com.webapp.repository.Clientes;
import com.webapp.repository.Depositos;
import com.webapp.repository.Emprestimos;
import com.webapp.repository.Investidores;
import com.webapp.repository.Parcelas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class DashboardBean_ implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Emprestimos emprestimos;

	@Inject
	private Parcelas parcelas;

	@Inject
	private Depositos depositos;

	@Inject
	private Investidores investidores;

	@Inject
	private Clientes clientes;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String emprestado;
	private String ultimoEmprestimo;
	private Double totalEmprestado;

	private String emCaixa;
	private String ultimoPagamento;
	private Double totalEmCaixa;

	private String pendente;
	private String lastUpdate;

	private String pago;

	private String investido;
	
	private String jurosReceber;
	
	private String lucro;

	private Number totalInvestidores;

	private Number totalClientes;
	
	private Number totalDepositos;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			emprestado();
			emCaixa();
			pendente();
			
			clientes();
			investidores();
			
			lucro();
		}
	}

	public void emprestado() {

		List<Emprestimo> todosEmprestimos = emprestimos.todos();

		totalEmprestado = 0.0;

		for (Emprestimo emprestimo : todosEmprestimos) {
			totalEmprestado += Double.parseDouble(emprestimo.getValorEmprestimo());
			ultimoEmprestimo = sdf.format(emprestimo.getDataEmprestimo());
		}

		if (todosEmprestimos.size() == 0) {
			ultimoEmprestimo = sdf.format(new Date());
		}

		emprestado = nf.format(totalEmprestado);
	}

	public void emCaixa() {
		List<Parcela> todasParcelas = parcelas.todas();

		Double totalPago = 0.0;

		for (Parcela parcela : todasParcelas) {
			totalPago += parcela.getValorPago().doubleValue();
			ultimoPagamento = sdf.format(parcela.getDataPagamento());
		}

		if (todasParcelas.size() == 0) {
			ultimoPagamento = sdf.format(new Date());
		}

		totalEmCaixa = (depositos().doubleValue() - totalEmprestado) + (totalPago - despesas().doubleValue());
		emCaixa = nf.format(totalEmCaixa);
		pago = nf.format(totalPago);
	}

	private Number despesas() {
		
		return 0;
	}

	private Number depositos() {
		totalDepositos = depositos.totalDepositos();

		if (totalDepositos != null) {
			investido = nf.format(totalDepositos.doubleValue());
			return totalDepositos;
		} else {
			totalDepositos = 0;
		}

		investido = nf.format(totalDepositos.doubleValue());
		return totalDepositos;
	}

	public void pendente() {

		List<Emprestimo> todosEmprestimos = emprestimos.porStatus(StatusEmprestimo.ABERTO);

		Double totalPendente = 0.0;
		Double totalJuros = 0.0;

		for (Emprestimo emprestimo : todosEmprestimos) {
			totalPendente += emprestimo.getTotal().doubleValue();
			totalJuros += emprestimo.getJuros().doubleValue();
		}

		lastUpdate = sdf.format(new Date());

		pendente = nf.format(totalPendente);
		
		jurosReceber = nf.format(totalJuros);
	}

	public void investidores() {
		Number totalInvestidoresTemp = investidores.totalInvestidores();
		
		if(totalInvestidoresTemp != null) {
			totalInvestidores = totalInvestidoresTemp;
		} else {
			totalInvestidores = 0;
		}
	}
	
	public void clientes() {
		Number totalClientesTemp = clientes.totalClientes();
		
		if(totalClientesTemp != null) {
			totalClientes = totalClientesTemp;
		} else {
			totalClientes = 0;
		}
	}
	
	public void lucro() {
		List<Investidor> todosInvestidores = investidores.todos();
		
		Double lucroParcial = 0.0;
		Double percentual = 0.0;
		Double resgateAtual = 0.0;
		Double lucroTotal = 0.0;
		
		for (int i = 0; i < todosInvestidores.size(); i++) {
			Number totalDepositosParcial = depositos.porInvestidor(todosInvestidores.get(i));
			
			if(totalDepositosParcial != null) {
				percentual = totalDepositosParcial.doubleValue()/totalDepositos.doubleValue();
			} else {
				totalDepositosParcial = 0;
			}
			
			resgateAtual = totalEmCaixa * percentual;
			
			lucroParcial = resgateAtual - totalDepositosParcial.doubleValue();
			
			lucroTotal += lucroParcial;
		}
		
		lucro = nf.format(lucroTotal);
	}

	public String getEmprestado() {
		return emprestado;
	}

	public String getUltimoEmprestimo() {
		return ultimoEmprestimo;
	}

	public String getEmCaixa() {
		return emCaixa;
	}

	public String getUltimoPagamento() {
		return ultimoPagamento;
	}

	public String getPago() {
		return pago;
	}

	public String getPendente() {
		return pendente;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public String getInvestido() {
		return investido;
	}

	public String getJurosReceber() {
		return jurosReceber;
	}

	public String getLucro() {
		return lucro;
	}

	public Number getTotalInvestidores() {
		return totalInvestidores;
	}

	public Number getTotalClientes() {
		return totalClientes;
	}

}