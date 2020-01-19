package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "deposito")
public class Deposito implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	@NotNull
	private Date data = new Date();
	
	@NotNull
	private Investidor investidor;
	
	@NotNull
	private BigDecimal valor;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable = false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}
	
	@ManyToOne
	@JoinColumn(nullable = false)
	public Investidor getInvestidor() {
		return investidor;
	}

	public void setInvestidor(Investidor investidor) {
		this.investidor = investidor;
	}

	@Column(nullable = false)
	@Digits(integer = 10 /*precision*/, fraction = 2 /*scale*/)
	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
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
		Deposito other = (Deposito) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Transient
	private String valorTemp;

	public String getValorTemp() {
		return valorTemp;
	}

	public void setValorTemp(String valorTemp) {
		this.valorTemp = valorTemp;
	}

}