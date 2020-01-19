package com.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class Simulacao implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank
	private String nome;
	private String cpf;
	private String contato;
	@NotNull
	private Date dataEmprestimo = new Date();
	@NotBlank
	private String valorEmprestimo = "";
	private Integer quantidadeParcelas = 3;
	private Integer percentualJuros = 20;
	@NotBlank
	private String primeiraParcela;
	@NotNull
	private Date dataVencimento;

	private Parcela parcelas;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getContato() {
		return contato;
	}

	public void setContato(String contato) {
		this.contato = contato;
	}

	public Date getDataEmprestimo() {
		return dataEmprestimo;
	}

	public void setDataEmprestimo(Date dataEmprestimo) {
		this.dataEmprestimo = dataEmprestimo;
	}

	public String getValorEmprestimo() {
		return valorEmprestimo;
	}

	public void setValorEmprestimo(String valorEmprestimo) {
		this.valorEmprestimo = valorEmprestimo;
	}

	public Integer getQuantidadeParcelas() {
		return quantidadeParcelas;
	}

	public void setQuantidadeParcelas(Integer quantidadeParcelas) {
		this.quantidadeParcelas = quantidadeParcelas;
	}

	public Integer getPercentualJuros() {
		return percentualJuros;
	}

	public void setPercentualJuros(Integer percentualJuros) {
		this.percentualJuros = percentualJuros;
	}

	public String getPrimeiraParcela() {
		return primeiraParcela;
	}

	public void setPrimeiraParcela(String primeiraParcela) {
		this.primeiraParcela = primeiraParcela;
	}

	public Date getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public Parcela getParcelas() {
		return parcelas;
	}

	public void setParcelas(Parcela parcelas) {
		this.parcelas = parcelas;
	}

}