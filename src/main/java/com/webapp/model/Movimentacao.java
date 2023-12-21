package com.webapp.model;

import java.io.Serializable;

public class Movimentacao implements Serializable {

	private static final long serialVersionUID = 1L;

	private String codigoLancamento;
	private String operacao;
	private String dataOperacao;
	private String valorTotal;
	private String tipo;
	private String dataPagamento;
	private String valorPagoNestaData;
	private String saldo;

	public String getCodigoLancamento() {
		return codigoLancamento;
	}

	public void setCodigoLancamento(String codigoLancamento) {
		this.codigoLancamento = codigoLancamento;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public String getDataOperacao() {
		return dataOperacao;
	}

	public void setDataOperacao(String dataOperacao) {
		this.dataOperacao = dataOperacao;
	}

	public String getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(String valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(String dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public String getValorPagoNestaData() {
		return valorPagoNestaData;
	}

	public void setValorPagoNestaData(String valorPagoNestaData) {
		this.valorPagoNestaData = valorPagoNestaData;
	}

	public String getSaldo() {
		return saldo;
	}

	public void setSaldo(String saldo) {
		this.saldo = saldo;
	}

}