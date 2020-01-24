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
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "lancamentos")
public class Lancamento implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue//(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column
	private Date dataLancamento = new Date();

	@NotBlank
	@Column(nullable = false, length = 120)
	private String descricao;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal valor;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private OrigemLancamento origemLancamento;

	@NotNull
	@ManyToOne
	@JoinColumn
	private DestinoLancamento destinoLancamento;

	@NotNull
	@ManyToOne
	@JoinColumn
	private TipoLancamento tipoLancamento;

	@NotNull
	@ManyToOne
	@JoinColumn
	private CategoriaLancamento categoriaLancamento;

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn private Usuario usuario;
	 */

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

	public Date getDataLancamento() {
		return dataLancamento;
	}

	public void setDataLancamento(Date dataLancamento) {
		this.dataLancamento = dataLancamento;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}

	public OrigemLancamento getOrigemLancamento() {
		return origemLancamento;
	}

	public void setOrigemLancamento(OrigemLancamento origemLancamento) {
		this.origemLancamento = origemLancamento;
	}

	public DestinoLancamento getDestinoLancamento() {
		return destinoLancamento;
	}

	public void setDestinoLancamento(DestinoLancamento destino) {
		this.destinoLancamento = destino;
	}

	public TipoLancamento getTipoLancamento() {
		return tipoLancamento;
	}

	public void setTipoLancamento(TipoLancamento tipoLancamento) {
		this.tipoLancamento = tipoLancamento;
	}

	public CategoriaLancamento getCategoriaLancamento() {
		return categoriaLancamento;
	}

	public void setCategoriaLancamento(CategoriaLancamento categoriaLancamento) {
		this.categoriaLancamento = categoriaLancamento;
	}

	/*
	 * public Usuario getUsuario() { return usuario; }
	 * 
	 * public void setUsuario(Usuario usuario) { this.usuario = usuario; }
	 */

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
		Lancamento other = (Lancamento) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}