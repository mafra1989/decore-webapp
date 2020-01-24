package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;

@Entity
@Table(name = "parcela")
public class Parcela implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private Date dataPagamento = new Date();

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal valorPago;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal debito;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal juros;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal desconto;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal valorRestante;

	@ManyToOne
	@JoinColumn
	private Emprestimo emprestimo;

	/* Simulação de Empréstimo */
	@Transient
	private String parcela;

	@Transient
	private String valorParcela;

	@Transient
	private Date vencimentoParcela;
	
	@Transient
	public String valorPagoTemp;
	
	@Transient
	public String descontoTemp;
	
	@Transient
	public String valorRestanteTemp;

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

	public BigDecimal getDebito() {
		return debito;
	}

	public void setDebito(BigDecimal debito) {
		this.debito = debito;
	}

	public BigDecimal getJuros() {
		return juros;
	}

	public void setJuros(BigDecimal juros) {
		this.juros = juros;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public BigDecimal getValorRestante() {
		return valorRestante;
	}

	public void setValorRestante(BigDecimal valorRestante) {
		this.valorRestante = valorRestante;
	}

	public Emprestimo getEmprestimo() {
		return emprestimo;
	}

	public void setEmprestimo(Emprestimo emprestimo) {
		this.emprestimo = emprestimo;
	}

	public String getParcela() {
		return parcela;
	}

	public void setParcela(String parcela) {
		this.parcela = parcela;
	}

	public String getValorParcela() {
		return valorParcela;
	}

	public void setValorParcela(String valorParcela) {
		this.valorParcela = valorParcela;
	}

	public Date getVencimentoParcela() {
		return vencimentoParcela;
	}

	public void setVencimentoParcela(Date vencimentoParcela) {
		this.vencimentoParcela = vencimentoParcela;
	}

	public String getValorPagoTemp() {
		return valorPagoTemp;
	}

	public void setValorPagoTemp(String valorPagoTemp) {
		this.valorPagoTemp = valorPagoTemp;
	}

	public String getDescontoTemp() {
		return descontoTemp;
	}

	public void setDescontoTemp(String descontoTemp) {
		this.descontoTemp = descontoTemp;
	}

	public String getValorRestanteTemp() {
		return valorRestanteTemp;
	}

	public void setValorRestanteTemp(String valorRestanteTemp) {
		this.valorRestanteTemp = valorRestanteTemp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parcela other = (Parcela) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}