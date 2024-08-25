package com.webapp.model;

import java.io.Serializable;

public class ItemEspelhoCompraProdutos implements Serializable {

	private static final long serialVersionUID = 1L;

	private String codigo;
	private String descricao;
	private String UN = "un";
	private String valorUnitario;
	private String quantidade;
	private String subTotal;
	private String observacao;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getUN() {
		return UN;
	}

	public void setUN(String uN) {
		UN = uN;
	}

	public String getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(String valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public String getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(String quantidade) {
		this.quantidade = quantidade;
	}

	public String getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(String subTotal) {
		this.subTotal = subTotal;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

}