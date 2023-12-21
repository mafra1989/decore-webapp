package com.webapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExtratoMovimentacoes implements Serializable {

	private static final long serialVersionUID = 203356217547759664L;

	/* Dados da Lista */
	private String saldoAnterior;
	private String dataEmissao;
	private String dataInicial;
	private String compras;
	private String vendas;
	private String despesas;
	private String receitas;
	private String saldoDisponivel;

	/* Produtos */
	private List<Object> movimentacoes = new ArrayList<Object>();


	public String getSaldoAnterior() {
		return saldoAnterior;
	}

	public void setSaldoAnterior(String saldoAnterior) {
		this.saldoAnterior = saldoAnterior;
	}

	public String getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(String dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	public String getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(String dataInicial) {
		this.dataInicial = dataInicial;
	}

	public String getCompras() {
		return compras;
	}

	public void setCompras(String compras) {
		this.compras = compras;
	}

	public String getVendas() {
		return vendas;
	}

	public void setVendas(String vendas) {
		this.vendas = vendas;
	}

	public String getDespesas() {
		return despesas;
	}

	public void setDespesas(String despesas) {
		this.despesas = despesas;
	}

	public String getReceitas() {
		return receitas;
	}

	public void setReceitas(String receitas) {
		this.receitas = receitas;
	}

	public String getSaldoDisponivel() {
		return saldoDisponivel;
	}

	public void setSaldoDisponivel(String saldoDisponivel) {
		this.saldoDisponivel = saldoDisponivel;
	}

	public List<Object> getMovimentacoes() {
		return movimentacoes;
	}

	public void setMovimentacoes(List<Object> movimentacoes) {
		this.movimentacoes = movimentacoes;
	}

}