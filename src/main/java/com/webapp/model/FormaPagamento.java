package com.webapp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "forma_pagamentos")
public class FormaPagamento implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String descricao;
	private boolean avista;
	private boolean entrada;
	private Long parcelas;
	private Long dias;


	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@NotBlank
	@Column(nullable = false, length = 120)
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Type(type = "yes_no")
	@Column(nullable = false)
	public boolean isAvista() {
		return avista;
	}

	public void setAvista(boolean avista) {
		this.avista = avista;
	}

	@Type(type = "yes_no")
	@Column(nullable = false)
	public boolean isEntrada() {
		return entrada;
	}

	public void setEntrada(boolean entrada) {
		this.entrada = entrada;
	}

	//@NotNull
	@Column(nullable = true)
	public Long getParcelas() {
		return parcelas;
	}

	public void setParcelas(Long parcelas) {
		this.parcelas = parcelas;
	}

	//@NotNull
	@Column(nullable = true)
	public Long getDias() {
		return dias;
	}

	public void setDias(Long dias) {
		this.dias = dias;
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
		FormaPagamento other = (FormaPagamento) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}