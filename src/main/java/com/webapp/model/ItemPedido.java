package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido implements Serializable {

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
	

	@ManyToOne
	@JoinColumn
	private Produto produto = new Produto();


	@ManyToOne
	@JoinColumn
	private Pedido pedido;


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
		this.valorUnitario = valorUnitario.setScale(4, BigDecimal.ROUND_HALF_EVEN);;
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
		this.total = total.setScale(4, BigDecimal.ROUND_HALF_EVEN);;
	}


	public Produto getProduto() {
		return produto;
	}


	public void setProduto(Produto produto) {
		this.produto = produto;
	}


	public Pedido getPedido() {
		return pedido;
	}


	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
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
		ItemPedido other = (ItemPedido) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}