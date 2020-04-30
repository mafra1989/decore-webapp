package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Conta;
import com.webapp.model.OrigemConta;
import com.webapp.model.TipoOperacao;
import com.webapp.repository.Contas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaContasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Contas contas;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalContas = "0,00";
	
	private Long codigo;
	
	private Date vencimento = new Date();
	
	private boolean contasPagas;
	
	private OrigemConta[] origemConta;
	
	private TipoOperacao TipoOperacao;
	
	private List<Conta> contasFiltradas = new ArrayList<>();

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			// todosUsuarios = usuarios.todos();
		}
	}

	public void pesquisar() {
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(vencimento);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		contasFiltradas = new ArrayList<>();
		contasFiltradas = contas.contasFiltradas(codigo, TipoOperacao, calendarioTemp, origemConta, vencimento,
				contasPagas);

		double totalContasTemp = 0;
		for (Conta conta : contasFiltradas) {
			totalContasTemp += conta.getValor().doubleValue();
		}

		totalContas = nf.format(totalContasTemp);
/*		
		if(origemConta.length > 0 ) {
			for (Conta conta : contasFiltradas) {
				contas.remove(conta);
			}
		}
*/
	}
	
	public void pagar(Conta conta) {
		
		if(conta.isStatus()) {
			conta.setPagamento(new Date());
			
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(conta.getPagamento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
			
		} else {
			conta.setPagamento(null);
			
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(conta.getVencimento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
		}
		
		contas.save(conta);
	}
	
	public OrigemConta[] getOrigensContas() {
		return OrigemConta.values();
	}
	
	public TipoOperacao[] getTiposOperacoes() {
		return com.webapp.model.TipoOperacao.values();
	}

	public OrigemConta[] getOrigemConta() {
		return origemConta;
	}

	public void setOrigemConta(OrigemConta[] origemConta) {
		this.origemConta = origemConta;
	}

	public String getTotalContas() {
		return totalContas;
	}

	public TipoOperacao getTipoOperacao() {
		return TipoOperacao;
	}

	public void setTipoOperacao(TipoOperacao tipoOperacao) {
		TipoOperacao = tipoOperacao;
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public boolean isContasPagas() {
		return contasPagas;
	}

	public void setContasPagas(boolean contasPagas) {
		this.contasPagas = contasPagas;
	}

	public List<Conta> getContasFiltradas() {
		return contasFiltradas;
	}

}
