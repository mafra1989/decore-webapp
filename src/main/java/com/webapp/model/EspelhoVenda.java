package com.webapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EspelhoVenda implements Serializable {

	private static final long serialVersionUID = 203356217547759664L;

	/* Dados da Venda */
	private String vendaNum;
	private String tipoVenda;
	private String bairro;
	private String dataVenda;
	private String vendedor;

	/* Dados da Entrega */
	private String responsavel;
	private String localizacao;
	private String observacao;

	/* Lista de Produtos */
	private List<Object> itensPedidos;

	/* Total da Venda */
	private String totalVenda;

	public String getVendaNum() {
		return vendaNum;
	}

	public void setVendaNum(String vendaNum) {
		this.vendaNum = vendaNum;
	}

	public String getTipoVenda() {
		return tipoVenda;
	}

	public void setTipoVenda(String tipoVenda) {
		this.tipoVenda = tipoVenda;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getDataVenda() {
		return dataVenda;
	}

	public void setDataVenda(String dataVenda) {
		this.dataVenda = dataVenda;
	}

	public String getVendedor() {
		return vendedor;
	}

	public void setVendedor(String vendedor) {
		this.vendedor = vendedor;
	}

	public String getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(String responsavel) {
		this.responsavel = responsavel;
	}

	public String getLocalizacao() {
		return localizacao;
	}

	public void setLocalizacao(String localizacao) {
		this.localizacao = localizacao;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public List<Object> getItensPedidos() {
		if (itensPedidos == null) {
			itensPedidos = new ArrayList<Object>();
		}
		return itensPedidos;
	}

	public void setItensPedidos(List<Object> itensPedidos) {
		this.itensPedidos = itensPedidos;
	}

	public String getTotalVenda() {
		return totalVenda;
	}

	public void setTotalVenda(String totalVenda) {
		this.totalVenda = totalVenda;
	}

}