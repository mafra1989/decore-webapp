package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
@Table(name = "itens_venda")
public class ItemVenda implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue//(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorUnitario;

	@NotNull
	@Column(nullable = false)
	private Long quantidade;

	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal total = BigDecimal.ZERO;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal lucro;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal percentualLucro;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorCompra;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 0 /* scale */)
	private BigDecimal desconto = BigDecimal.ZERO;
	

	@ManyToOne
	@JoinColumn
	private Produto produto = new Produto();

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn private Fornecedor fornecedor;
	 */

	@ManyToOne
	@JoinColumn
	private Venda venda;

	@ManyToOne
	@JoinColumn
	private Compra compra;
	
	
	@Column(columnDefinition="TEXT")
	private String observacoes;
	

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

	public Long getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Long quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getLucro() {
		return lucro;
	}

	public void setLucro(BigDecimal lucro) {
		this.lucro = lucro.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getPercentualLucro() {
		return percentualLucro;
	}

	public void setPercentualLucro(BigDecimal percentualLucro) {
		this.percentualLucro = percentualLucro.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getValorCompra() {
		return valorCompra;
	}

	public void setValorCompra(BigDecimal valorCompra) {
		this.valorCompra = valorCompra.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
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
	public Venda getVenda() {
		return venda;
	}

	public void setVenda(Venda venda) {
		this.venda = venda;
	}

	public Compra getCompra() {
		return compra;
	}

	public void setCompra(Compra compra) {
		this.compra = compra;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
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
		ItemVenda other = (ItemVenda) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Transient
	private String code;
	
	@Transient
	private Long saldo;
	
	@Transient
	private List<ItemVendaCompra> itensVendaCompra;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getSaldo() {
		return saldo;
	}

	public void setSaldo(Long saldo) {
		this.saldo = saldo;
	}

	public List<ItemVendaCompra> getItensVendaCompra() {
		return itensVendaCompra;
	}

	public void setItensVendaCompra(List<ItemVendaCompra> itensVendaCompra) {
		this.itensVendaCompra = itensVendaCompra;
	}

}