package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "emprestimo")
public class Emprestimo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String contrato;// SB-0001/2019

	@NotNull
	@Column
	private Date dataEmprestimo = new Date();

	@NotBlank
	@Column
	private String valorEmprestimo;

	@Column
	private Integer percentualJuros = 20;

	@NotNull
	@Column
	private Date dataVencimento;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column
	private StatusEmprestimo statusEmprestimo = StatusEmprestimo.ABERTO;

	@ManyToOne
	@JoinColumn
	private Cliente cliente = new Cliente();
	
	

	@Column(nullable = false)
	private Date proximoVencimento;
	
	@Transient
	private Date proximoVencimentoTemp;
	

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal debito;

	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	@Column(nullable = false)
	private BigDecimal juros;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal total = BigDecimal.ZERO;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal totalPago;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal saldoDevedorInicial;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal jurosInicial;

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	private BigDecimal jurosFinal;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContrato() {
		return contrato;
	}

	public void setContrato(String contrato) {
		this.contrato = contrato;
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

	public Integer getPercentualJuros() {
		return percentualJuros;
	}

	public void setPercentualJuros(Integer percentualJuros) {
		this.percentualJuros = percentualJuros;
	}

	public Date getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(Date dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public StatusEmprestimo getStatusEmprestimo() {
		return statusEmprestimo;
	}

	public void setStatusEmprestimo(StatusEmprestimo statusEmprestimo) {
		this.statusEmprestimo = statusEmprestimo;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Date getProximoVencimento() {
		return proximoVencimento;
	}

	public void setProximoVencimento(Date proximoVencimento) {
		this.proximoVencimento = proximoVencimento;
	}

	public Date getProximoVencimentoTemp() {
		return proximoVencimentoTemp;
	}

	public void setProximoVencimentoTemp(Date proximoVencimentoTemp) {
		this.proximoVencimentoTemp = proximoVencimentoTemp;
	}

	public BigDecimal getDebito() {
		return debito;
	}

	public void setDebito(BigDecimal debito) {
		this.debito = debito.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public BigDecimal getJuros() {
		return juros;
	}

	public void setJuros(BigDecimal juros) {
		this.juros = juros.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public BigDecimal getTotalPago() {
		return totalPago;
	}

	public void setTotalPago(BigDecimal totalPago) {
		this.totalPago = totalPago.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public BigDecimal getSaldoDevedorInicial() {
		return saldoDevedorInicial;
	}

	public void setSaldoDevedorInicial(BigDecimal saldoDevedorInicial) {
		this.saldoDevedorInicial = saldoDevedorInicial.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public BigDecimal getJurosInicial() {
		return jurosInicial;
	}

	public void setJurosInicial(BigDecimal jurosInicial) {
		this.jurosInicial = jurosInicial.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public BigDecimal getJurosFinal() {
		return jurosFinal;
	}

	public void setJurosFinal(BigDecimal jurosFinal) {
		this.jurosFinal = jurosFinal.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cliente == null) ? 0 : cliente.hashCode());
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
		Emprestimo other = (Emprestimo) obj;
		if (cliente == null) {
			if (other.cliente != null)
				return false;
		} else if (!cliente.equals(other.cliente))
			return false;
		return true;
	}
	
	@Transient
	private String totalTemp;

	public String getTotalTemp() {
		return totalTemp;
	}

	public void setTotalTemp(String totalTemp) {
		this.totalTemp = totalTemp;
	}
	

}