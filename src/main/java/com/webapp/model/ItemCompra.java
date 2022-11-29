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

@Entity
@Table(name = "itens_compra")
public class ItemCompra implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue//(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorUnitario;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 3 /* scale */)
	private BigDecimal quantidade;

	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal total;

	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 3 /* scale */)
	private BigDecimal quantidadeDisponivel;

	@ManyToOne
	@JoinColumn
	private Produto produto;
	
	//@NotNull
	@Column(nullable = true)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal precoDeVenda;

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn private Fornecedor fornecedor;
	 */

	@ManyToOne
	@JoinColumn
	private Compra compra;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(BigDecimal quantidade) {
		this.quantidade = quantidade.setScale(3, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getQuantidadeDisponivel() {
		return quantidadeDisponivel;
	}

	public void setQuantidadeDisponivel(BigDecimal quantidadeDisponivel) {
		this.quantidadeDisponivel = quantidadeDisponivel.setScale(3, BigDecimal.ROUND_HALF_EVEN);
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	/*
	 * public Fornecedor getFornecedor() { return fornecedor; }
	 * 
	 * public void setFornecedor(Fornecedor fornecedor) { this.fornecedor =
	 * fornecedor; }
	 */
	public Compra getCompra() {
		return compra;
	}

	public void setCompra(Compra compra) {
		this.compra = compra;
	}

	public BigDecimal getPrecoDeVenda() {
		return precoDeVenda;
	}

	public void setPrecoDeVenda(BigDecimal precoDeVenda) {
		this.precoDeVenda = precoDeVenda;
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
		ItemCompra other = (ItemCompra) obj;
		return Objects.equals(code, other.code) && Objects.equals(id, other.id);
	}


	@Transient
	private String code;
	
	@Transient
	private String valorUnitarioFormatado;
	
	@Transient
	private String quantidadeDisponivel_;
	

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValorUnitarioFormatado() {
		return valorUnitarioFormatado;
	}

	public void setValorUnitarioFormatado(String valorUnitarioFormatado) {
		this.valorUnitarioFormatado = valorUnitarioFormatado;
	}

	public String getQuantidadeDisponivel_() {
		return quantidadeDisponivel_;
	}

	public void setQuantidadeDisponivel_(String quantidadeDisponivel_) {
		this.quantidadeDisponivel_ = quantidadeDisponivel_;
	}
}