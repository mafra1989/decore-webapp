package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.validator.constraints.NotBlank;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;

import com.webapp.model.Emprestimo;
import com.webapp.model.Parcela;
import com.webapp.model.StatusEmprestimo;
import com.webapp.repository.Emprestimos;
import com.webapp.repository.Parcelas;
import com.webapp.repository.filter.EmprestimoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class PesquisaEmprestimosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Emprestimos emprestimos;

	@Inject
	private Parcelas parcelas;

	private List<Emprestimo> listaEmprestimos = new ArrayList<Emprestimo>();

	private List<Parcela> listaParcelas = new ArrayList<Parcela>();

	private EmprestimoFilter filtro = new EmprestimoFilter();

	@Inject
	private Emprestimo emprestimo;

	private Emprestimo emprestimoSelecionado;

	@Inject
	private Parcela parcela;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String valorEmprestado;

	private String valorTotalPago;

	private String saldoDevedorInicial;

	private String valorJurosInicial;

	private String valorJurosFinal;

	private String debito;

	private String juros;

	private String total;

	@NotBlank
	private String valorPago;

	@NotBlank
	private String debitoRestante;

	@NotBlank
	private String jurosDaParcela;

	private String desconto;

	@NotBlank
	private String valorRestante;

	private Calendar calendar = Calendar.getInstance();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			//listaEmprestimos = emprestimos.todos();
			pesquisar();
		}
	}

	public void prepararInfo() {
		valorEmprestado = nf.format(Double.parseDouble(emprestimoSelecionado.getValorEmprestimo()));
		valorTotalPago = nf.format(emprestimoSelecionado.getTotalPago().doubleValue());
		saldoDevedorInicial = nf.format(emprestimoSelecionado.getSaldoDevedorInicial().doubleValue());
		valorJurosInicial = nf.format(emprestimoSelecionado.getJurosInicial().doubleValue());
		valorJurosFinal = nf.format(emprestimoSelecionado.getJurosFinal().doubleValue());

		debito = nf.format(emprestimoSelecionado.getDebito().doubleValue());
		juros = nf.format(emprestimoSelecionado.getJuros().doubleValue());
		total = nf.format(emprestimoSelecionado.getTotal().doubleValue());

		listaParcelas = parcelas.todasParcelas(emprestimoSelecionado.getId());
		
		if(listaParcelas.size() > 0) {
			for (Parcela parcela : listaParcelas) {
				parcela.setValorPagoTemp(nf.format(parcela.getValorPago().doubleValue()));
				parcela.setDescontoTemp(nf.format(parcela.getDesconto().doubleValue()));
				parcela.setValorRestanteTemp(nf.format(parcela.getValorRestante().doubleValue()));
			}
		}

	}

	public void pesquisar() {
		listaEmprestimos = emprestimos.filtrados(filtro);
		
		for (Emprestimo emprestimo : listaEmprestimos) {
			emprestimo.setTotalTemp(nf.format(emprestimo.getTotal().doubleValue()));
		}
		
		emprestimoSelecionado = null;
	}

	public void excluir() {

		listaParcelas = parcelas.todasParcelas(emprestimoSelecionado.getId());
		
		if(listaParcelas.size() > 0) {
			for (Parcela parcela : listaParcelas) {
				parcelas.remove(parcela);
			}
		}
		
		emprestimos.remove(emprestimoSelecionado);

		emprestimoSelecionado = null;

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Empréstimo excluído com sucesso!' });");

	}

	public String calculaPagamento(FlowEvent event) {
		double descontoTemp = 0.0;
		if (!desconto.isEmpty()) {
			descontoTemp = Double.parseDouble(desconto);
		}
		parcela.setDesconto(BigDecimal.valueOf(descontoTemp).setScale(2, BigDecimal.ROUND_HALF_EVEN));

		if(!valorPago.equals("")) {
			parcela.setValorPago(BigDecimal.valueOf(Double.parseDouble(valorPago)).setScale(2, BigDecimal.ROUND_HALF_EVEN));
			
			double debitoRestante = emprestimoSelecionado.getTotal().doubleValue()
					- (Double.parseDouble(valorPago) + descontoTemp);
		
			this.debitoRestante = nf.format(debitoRestante);
			parcela.setDebito(BigDecimal.valueOf(debitoRestante).setScale(2, BigDecimal.ROUND_HALF_EVEN));
	
			double jurosDaParcela = (debitoRestante * (1 + emprestimoSelecionado.getPercentualJuros().doubleValue() / 100))
					- debitoRestante;
			this.jurosDaParcela = nf.format(jurosDaParcela);
			parcela.setJuros(BigDecimal.valueOf(jurosDaParcela).setScale(2, BigDecimal.ROUND_HALF_EVEN));
	
			double valorRestante = debitoRestante + jurosDaParcela;
			this.valorRestante = nf.format(valorRestante);
	
			parcela.setValorRestante(BigDecimal.valueOf(valorRestante).setScale(2, BigDecimal.ROUND_HALF_EVEN));
		}

		return event.getNewStep();
	}

	
	public void prepararPagamento() {
		
		parcela = new Parcela();
		
		calendar.setTime(emprestimoSelecionado.getProximoVencimento());
		int dia = calendar.get(Calendar.DAY_OF_MONTH);

		calendar.add(Calendar.MONTH, 1);
		int numDias = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

		/* Tratamento para os dias 29, 30 e 31 */
		if (dia > numDias) {
			calendar.set(Calendar.DAY_OF_MONTH, numDias);
		} else {
			calendar.set(Calendar.DAY_OF_MONTH, dia);
		}
		
		emprestimoSelecionado.setProximoVencimentoTemp(calendar.getTime());
		
	}
	
	public void confirmarPagamento() {
		parcela.setEmprestimo(emprestimoSelecionado);
		parcelas.save(parcela);

		// valorEmprestado =
		// nf.format(Double.parseDouble(emprestimoSelecionado.getValorEmprestimo()));
		emprestimoSelecionado.setTotalPago(BigDecimal
				.valueOf(emprestimoSelecionado.getTotalPago().doubleValue() + parcela.getValorPago().doubleValue()));
		// saldoDevedorInicial =
		// nf.format(emprestimoSelecionado.getSaldoDevedorInicial().doubleValue());
		// valorJurosInicial =
		// nf.format(emprestimoSelecionado.getJurosInicial().doubleValue());
		emprestimoSelecionado.setJurosFinal(BigDecimal
				.valueOf(emprestimoSelecionado.getJurosFinal().doubleValue() + parcela.getJuros().doubleValue()));

		emprestimoSelecionado.setDebito(parcela.getDebito());
		emprestimoSelecionado.setJuros(parcela.getJuros());
		emprestimoSelecionado.setTotal(parcela.getValorRestante());
		
		emprestimoSelecionado.setProximoVencimento(emprestimoSelecionado.getProximoVencimentoTemp());

		System.out.println("Total pago: " + BigDecimal
				.valueOf(emprestimoSelecionado.getTotalPago().doubleValue() + parcela.getValorPago().doubleValue()));
		
		if(emprestimoSelecionado.getTotal().doubleValue() <= 0) {
			emprestimoSelecionado.setStatusEmprestimo(StatusEmprestimo.FECHADO);
		}
		
		emprestimoSelecionado.setTotalTemp(nf.format(emprestimoSelecionado.getTotal().doubleValue()));
		
		emprestimos.save(emprestimoSelecionado);

		parcela = new Parcela();
		emprestimoSelecionado = new Emprestimo();
		
		valorPago = "";
		desconto = "";

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Pagamento registrado com sucesso!' });"
				+ "PF('wizard').loadStep(PF('wizard').cfg.steps[0], true);");
	}

	public List<Emprestimo> getListaEmprestimos() {
		return listaEmprestimos;
	}

	public List<Parcela> getListaParcelas() {
		return listaParcelas;
	}

	public EmprestimoFilter getFiltro() {
		return filtro;
	}

	public void setFiltro(EmprestimoFilter filtro) {
		this.filtro = filtro;
	}

	public Emprestimo getEmprestimo() {
		return emprestimo;
	}

	public void setEmprestimo(Emprestimo emprestimo) {
		this.emprestimo = emprestimo;
	}

	public Emprestimo getEmprestimoSelecionado() {
		return emprestimoSelecionado;
	}

	public void setEmprestimoSelecionado(Emprestimo emprestimoSelecionado) {
		this.emprestimoSelecionado = emprestimoSelecionado;
	}

	public Parcela getParcela() {
		return parcela;
	}

	public void setParcela(Parcela parcela) {
		this.parcela = parcela;
	}

	public String getValorEmprestado() {
		return valorEmprestado;
	}

	public String getValorTotalPago() {
		return valorTotalPago;
	}

	public String getSaldoDevedorInicial() {
		return saldoDevedorInicial;
	}

	public String getValorJurosInicial() {
		return valorJurosInicial;
	}

	public String getValorJurosFinal() {
		return valorJurosFinal;
	}

	public String getDebito() {
		return debito;
	}

	public String getJuros() {
		return juros;
	}

	public String getTotal() {
		return total;
	}

	public String getValorPago() {
		return valorPago;
	}

	public void setValorPago(String valorPago) {
		this.valorPago = valorPago;
	}

	public String getDebitoRestante() {
		return debitoRestante;
	}

	public void setDebitoRestante(String debitoRestante) {
		this.debitoRestante = debitoRestante;
	}

	public String getJurosDaParcela() {
		return jurosDaParcela;
	}

	public void setJurosDaParcela(String jurosDaParcela) {
		this.jurosDaParcela = jurosDaParcela;
	}

	public String getDesconto() {
		return desconto;
	}

	public void setDesconto(String desconto) {
		this.desconto = desconto;
	}

	public String getValorRestante() {
		return valorRestante;
	}

	public void setValorRestante(String valorRestante) {
		this.valorRestante = valorRestante;
	}
}
