package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Digits;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "conta_fix_v2")
public class Conta implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private Long codigoOperacao;

	@Column(nullable = false, length = 10)
	private String parcela;

	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal valor;

	@Column(nullable = false)
	private Date vencimento = new Date();

	@Column(nullable = false, length = 10)
	private String tipo;

	@Column(nullable = false, length = 10)
	private String operacao;

	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean status;
	
	
	/* Campos para relat�rio */
	@Column(nullable = false)
	private Long dia;

	@Column(nullable = false)
	private Long nomeDia;

	@Column(nullable = false)
	private Long semana;

	@Column(nullable = false)
	private Long mes;

	@Column(nullable = false)
	private Long ano;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCodigoOperacao() {
		return codigoOperacao;
	}

	public void setCodigoOperacao(Long codigoOperacao) {
		this.codigoOperacao = codigoOperacao;
	}

	public String getParcela() {
		return parcela;
	}

	public void setParcela(String parcela) {
		this.parcela = parcela;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Long getDia() {
		return dia;
	}

	public void setDia(Long dia) {
		this.dia = dia;
	}

	public Long getNomeDia() {
		return nomeDia;
	}

	public void setNomeDia(Long nomeDia) {
		this.nomeDia = nomeDia;
	}

	public Long getSemana() {
		return semana;
	}

	public void setSemana(Long semana) {
		this.semana = semana;
	}

	public Long getMes() {
		return mes;
	}

	public void setMes(Long mes) {
		this.mes = mes;
	}

	public Long getAno() {
		return ano;
	}

	public void setAno(Long ano) {
		this.ano = ano;
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
		Conta other = (Conta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}