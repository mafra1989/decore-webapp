package com.webapp.model;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EspelhoCompra implements Serializable {

	private static final long serialVersionUID = 203356217547759664L;
	
	/* Dados da Empresa */
	private InputStream logo;
	private String xNome = "DECORE";
	private String CNPJ = "32.783.038/0001-74";
	private String xLgr = "Rua Rio Andirá";
	private String nro = "n4";
	private String xBairro = "São José Operário";
	private String xMun = "Manaus";
	private String UF = "AM";
	private String contato = "(99) 9 9999-9999";
	
	private String totalItens = "30";
	private String subTotal = "0,00";
	private String desconto = "0,00";
	private String acrescimo = "0,00";
	private String frete = "0,00";
	
	private String valorAPagar = "0,00";
	private String troco = "0,00";
	private String valorRecebido = "0,00";
	
	private String entrega = "N";

	/* Dados da Compra */
	private String compraNum;
	private String dataCompra;
	private String funcionario;
	private String tipoPagamento;
	private Boolean conta;
	private String observacoes;
	

	/* Lista de Produtos */
	private List<Object> itensPedidos;
	
	/* Lista de Pagamentos */
	private List<Object> itensPagamentos;
	
	/* Lista de Pagamentos */
	private List<Object> itensPagamento;
	
	/* Lista de Parcelamentos */
	private List<Object> itensParcelamentos;

	/* Total da Venda */
	private String totalVenda;
	
	private String compraPaga;
	
	

	public String getCompraNum() {
		return compraNum;
	}

	public void setCompraNum(String vendaNum) {
		this.compraNum = vendaNum;
	}

	public String getDataCompra() {
		return dataCompra;
	}

	public void setDataCompra(String dataVenda) {
		this.dataCompra = dataVenda;
	}

	public String getFuncionario() {
		return funcionario;
	}

	public void setFuncionario(String vendedor) {
		this.funcionario = vendedor;
	}

	public String getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(String tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}

	public Boolean getConta() {
		return conta;
	}

	public void setConta(Boolean conta) {
		this.conta = conta;
	}
	
	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
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

	public List<Object> getItensPagamentos() {
		if (itensPagamentos == null) {
			itensPagamentos = new ArrayList<Object>();
		}
		return itensPagamentos;
	}

	public void setItensPagamentos(List<Object> itensPagamentos) {
		this.itensPagamentos = itensPagamentos;
	}
	
	public List<Object> getItensPagamento() {
		if (itensPagamento == null) {
			itensPagamento = new ArrayList<Object>();
		}
		return itensPagamento;
	}

	public void setItensPagamento(List<Object> itensPagamento) {
		this.itensPagamento = itensPagamento;
	}
	
	public List<Object> getItensParcelamentos() {
		if (itensParcelamentos == null) {
			itensParcelamentos = new ArrayList<Object>();
		}
		return itensParcelamentos;
	}

	public void setItensParcelamentos(List<Object> itensParcelamentos) {
		this.itensParcelamentos = itensParcelamentos;
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

	public String getContato() {
		return contato;
	}

	public void setContato(String contato) {
		this.contato = contato;
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

	public String getFrete() {
		return frete;
	}

	public void setFrete(String frete) {
		this.frete = frete;
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

	public String getEntrega() {
		return entrega;
	}

	public void setEntrega(String entrega) {
		this.entrega = entrega;
	}

	public InputStream getLogo() {
		return logo;
	}

	public void setLogo(InputStream logo) {
		this.logo = logo;
	}

	public String getCompraPaga() {
		return compraPaga;
	}

	public void setCompraPaga(String vendaPaga) {
		this.compraPaga = vendaPaga;
	}

}