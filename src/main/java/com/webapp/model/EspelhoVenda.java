package com.webapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EspelhoVenda implements Serializable {

	private static final long serialVersionUID = 203356217547759664L;
	
	/* Dados da Empresa */
	private String xNome = "DECORE";
	private String CNPJ = "32.783.038/0001-74";
	private String xLgr = "Rua Rio Andirá";
	private String nro = "n4";
	private String xBairro = "São José Operário";
	private String xMun = "Manaus";
	private String UF = "AM";
	
	private String totalItens = "30";
	private String subTotal = "0,00";
	private String desconto = "0,00";
	private String acrescimo = "0,00";
	
	private String valorAPagar = "0,00";
	private String troco = "0,00";
	private String valorRecebido = "0,00";

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

	
	public String getxNome() {
		return xNome;
	}

	public void setxNome(String xNome) {
		this.xNome = xNome;
	}

	public String getCNPJ() {
		return CNPJ;
	}

	public void setCNPJ(String cNPJ) {
		CNPJ = cNPJ;
	}

	public String getxLgr() {
		return xLgr;
	}

	public void setxLgr(String xLgr) {
		this.xLgr = xLgr;
	}

	public String getNro() {
		return nro;
	}

	public void setNro(String nro) {
		this.nro = nro;
	}

	public String getxBairro() {
		return xBairro;
	}

	public void setxBairro(String xBairro) {
		this.xBairro = xBairro;
	}

	public String getxMun() {
		return xMun;
	}

	public void setxMun(String xMun) {
		this.xMun = xMun;
	}

	public String getUF() {
		return UF;
	}

	public void setUF(String uF) {
		UF = uF;
	}

	public String getTotalItens() {
		return totalItens;
	}

	public void setTotalItens(String totalItens) {
		this.totalItens = totalItens;
	}

	public String getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}

	public String getDesconto() {
		return desconto;
	}

	public void setDesconto(String desconto) {
		this.desconto = desconto;
	}

	public String getAcrescimo() {
		return acrescimo;
	}

	public void setAcrescimo(String acrescimo) {
		this.acrescimo = acrescimo;
	}

	public String getValorAPagar() {
		return valorAPagar;
	}

	public void setValorAPagar(String valorAPagar) {
		this.valorAPagar = valorAPagar;
	}

	public String getTroco() {
		return troco;
	}

	public void setTroco(String troco) {
		this.troco = troco;
	}

	public String getValorRecebido() {
		return valorRecebido;
	}

	public void setValorRecebido(String valorRecebido) {
		this.valorRecebido = valorRecebido;
	}

}