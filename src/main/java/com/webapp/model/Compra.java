package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

@Entity
@Table(name = "compras")
public class Compra implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue//(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Column
	private Date dataCompra = new Date();
	
	@Column
	private Date dataPagamento;
	
	@Column
	private Long numeroCompra;
	
	@Column
	private String lote;

	@NotNull
	@Column
	private Long quantidadeItens = 0L;

	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@NotNull
	@ManyToOne
	@JoinColumn
	private Usuario usuario;
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private Empresa empresa;
	
	/*
	@NotNull
	@ManyToOne
	@JoinColumn
	private FormaPagamento formaPagamento;
	*/
	
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
	
	
	
	//@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private TipoPagamento tipoPagamento;
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean compraPaga = true;
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean conta;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataCompra() {
		return dataCompra;
	}

	public void setDataCompra(Date dataCompra) {
		this.dataCompra = dataCompra;
	}

	public Long getNumeroCompra() {
		return numeroCompra;
	}

	public void setNumeroCompra(Long numeroCompra) {
		this.numeroCompra = numeroCompra;
	}

	public Long getQuantidadeItens() {
		return quantidadeItens;
	}

	public void setQuantidadeItens(Long quantidadeItens) {
		this.quantidadeItens = quantidadeItens;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	/*
	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}
	*/

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public boolean isAjuste() {
		return ajuste;
	}

	public void setAjuste(boolean ajuste) {
		this.ajuste = ajuste;
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

	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(TipoPagamento tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}

	public boolean isCompraPaga() {
		return compraPaga;
	}

	public void setCompraPaga(boolean compraPaga) {
		this.compraPaga = compraPaga;
	}

	public boolean isConta() {
		return conta;
	}

	public void setConta(boolean conta) {
		this.conta = conta;
	}
	
	public Date getDataPagamento() {
		return dataPagamento;
	}

	public void setDataPagamento(Date dataPagamento) {
		this.dataPagamento = dataPagamento;
	}

	public String getLote() {
		return lote;
	}

	public void setLote(String lote) {
		this.lote = lote;
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
		Compra other = (Compra) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Transient
	private String dataCompraFormatada;
	
	@Transient
	private List<ItemCompra> itensCompra;
	
	@Transient
	private String tipoCompra;
	
	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorPago;
	
	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal totalPago;
	
	@Column
	private String observacao;

	public String getDataCompraFormatada() {
		return dataCompraFormatada;
	}

	public void setDataCompraFormatada(String dataCompraFormatada) {
		this.dataCompraFormatada = dataCompraFormatada;
	}

	public List<ItemCompra> getItensCompra() {
		return itensCompra;
	}

	public void setItensCompra(List<ItemCompra> itensCompra) {
		this.itensCompra = itensCompra;
	}

	public String getTipoCompra() {
		return this.ajuste ? "Ajuste: " : "Compra: ";
	}

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

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

}