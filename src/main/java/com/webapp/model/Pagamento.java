package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

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

import org.hibernate.annotations.Type;


@Entity
@Table(name = "pagamentos")
public class Pagamento implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue//(strategy=GenerationType.SEQUENCE, generator="Pagamento_Seq")
	private Long id;
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private FormaPagamento formaPagamento;
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private Venda venda;
	
	@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal valor;
	
	@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal troco = BigDecimal.ZERO;
	
	@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal acrescimo;
	
	@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal taxaDeAcrescimo;
	
	@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal valorDeAcrescimo;
	
	@Type(type = "yes_no")
	@Column(nullable = true)
	private boolean exclusao = false;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public Venda getVenda() {
		return venda;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public BigDecimal getTroco() {
		return troco;
	}

	public void setTroco(BigDecimal troco) {
		this.troco = troco;
	}

	public BigDecimal getAcrescimo() {
		return acrescimo;
	}

	public void setAcrescimo(BigDecimal acrescimo) {
		this.acrescimo = acrescimo;
	}

	public BigDecimal getTaxaDeAcrescimo() {
		return taxaDeAcrescimo;
	}

	public void setTaxaDeAcrescimo(BigDecimal taxaDeAcrescimo) {
		this.taxaDeAcrescimo = taxaDeAcrescimo;
	}

	public BigDecimal getValorDeAcrescimo() {
		return valorDeAcrescimo;
	}

	public void setValorDeAcrescimo(BigDecimal valorDeAcrescimo) {
		this.valorDeAcrescimo = valorDeAcrescimo;
	}

	public boolean isExclusao() {
		return exclusao;
	}

	public void setExclusao(boolean exclusao) {
		this.exclusao = exclusao;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(code, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pagamento other = (Pagamento) obj;
		return Objects.equals(code, other.code) && Objects.equals(id, other.id);
	}

	@Transient
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}