package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "lancamentos")
public class Lancamento implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue // (strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column
	private Date dataLancamento = new Date();
	
	@Column
	private Date dataPagamento;

	@Column//(unique = true)
	private Long numeroLancamento;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String descricao;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valor;

	@NotNull
	@ManyToOne
	@JoinColumn
	private CategoriaLancamento categoriaLancamento = new CategoriaLancamento();

	/*@NotNull*/
	@ManyToOne
	@JoinColumn
	private DestinoLancamento destinoLancamento = new DestinoLancamento();

	@ManyToOne
	@JoinColumn
	private Usuario usuario;
	
	@Type(type = "yes_no")
	@Column(nullable = true)
	private boolean ajuste;

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
	
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private Empresa empresa;
	
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean conta;
	
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private TipoPagamento tipoPagamento;
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean lancamentoPago = true;
	

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

	public Date getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public Long getNumeroLancamento() {
		return numeroLancamento;
	}

	public void setNumeroLancamento(Long numeroLancamento) {
		this.numeroLancamento = numeroLancamento;
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
		this.valor = valor != null ? valor.setScale(4, BigDecimal.ROUND_HALF_EVEN) : null ;
	}

	public CategoriaLancamento getCategoriaLancamento() {
		return categoriaLancamento;
	}

	public void setCategoriaLancamento(CategoriaLancamento categoriaLancamento) {
		this.categoriaLancamento = categoriaLancamento;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public boolean isAjuste() {
		return ajuste;
	}

	public void setAjuste(boolean ajuste) {
		this.ajuste = ajuste;
	}

	public DestinoLancamento getDestinoLancamento() {
		return destinoLancamento;
	}

	public void setDestinoLancamento(DestinoLancamento destinoLancamento) {
		this.destinoLancamento = destinoLancamento;
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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public boolean isConta() {
		return conta;
	}

	public void setConta(boolean conta) {
		this.conta = conta;
	}

	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(TipoPagamento tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}
	
	public boolean isLancamentoPago() {
		return lancamentoPago;
	}

	public void setLancamentoPago(boolean lancamentoPago) {
		this.lancamentoPago = lancamentoPago;
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
	
	
	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorPago;
	
	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal totalPago;


	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	}

	public BigDecimal getTotalPago() {
		return totalPago;
	}

	public void setTotalPago(BigDecimal totalPago) {
		this.totalPago = totalPago;
	}
	
}