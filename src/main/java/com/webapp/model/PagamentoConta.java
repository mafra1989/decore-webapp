package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "pagamentos_conta")
@SequenceGenerator(name="Pagamentos_Conta_Seq", sequenceName="pagamentos_conta_sequence", allocationSize=1,initialValue = 2)
public class PagamentoConta implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Pagamentos_Conta_Seq")
	private Long id;
	
	@NotNull
	@Column
	private Date dataPagamento = new Date();
	
	@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal valorPago;
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private FormaPagamento formaPagamento;
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private Conta conta;
	
	
	/* Campos para relat�rio */
	@Column
	private Long diaPagamento;

	@Column
	private Long nomeDiaPagamento;

	@Column
	private Long semanaPagamento;

	@Column
	private Long mesPagamento;

	@Column
	private Long anoPagamento;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}

	public Long getDiaPagamento() {
		return diaPagamento;
	}

	public void setDiaPagamento(Long diaPagamento) {
		this.diaPagamento = diaPagamento;
	}

	public Long getNomeDiaPagamento() {
		return nomeDiaPagamento;
	}

	public void setNomeDiaPagamento(Long nomeDiaPagamento) {
		this.nomeDiaPagamento = nomeDiaPagamento;
	}

	public Long getSemanaPagamento() {
		return semanaPagamento;
	}

	public void setSemanaPagamento(Long semanaPagamento) {
		this.semanaPagamento = semanaPagamento;
	}

	public Long getMesPagamento() {
		return mesPagamento;
	}

	public void setMesPagamento(Long mesPagamento) {
		this.mesPagamento = mesPagamento;
	}

	public Long getAnoPagamento() {
		return anoPagamento;
	}

	public void setAnoPagamento(Long anoPagamento) {
		this.anoPagamento = anoPagamento;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PagamentoConta other = (PagamentoConta) obj;
		return Objects.equals(id, other.id);
	}

}