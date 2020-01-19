package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "produtos")
public class Produto implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	@Column(nullable = false, length = 120)
	private String codigo;
	
	@NotBlank
	@Column(nullable = false, length = 120)
	private String nome;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String descricao;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal precoVenda;

	@Lob
	@Column
	private byte[] foto;

	@Column
	private Long quantidadeAtual = 0L;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal totalAcumulado = BigDecimal.ZERO;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal totalCompras = BigDecimal.ZERO;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal totalVendas = BigDecimal.ZERO;

	@NotNull
	@ManyToOne
	@JoinColumn
	private CategoriaProduto categoriaProduto;
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private Fornecedor fornecedor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getPrecoVenda() {
		return precoVenda;
	}

	public void setPrecoVenda(BigDecimal precoVenda) {
		this.precoVenda = precoVenda;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	public Long getQuantidadeAtual() {
		return quantidadeAtual;
	}

	public void setQuantidadeAtual(Long quantidadeAtual) {
		this.quantidadeAtual = quantidadeAtual;
	}

	public BigDecimal getTotalAcumulado() {
		return totalAcumulado;
	}

	public void setTotalAcumulado(BigDecimal totalAcumulado) {
		this.totalAcumulado = totalAcumulado;
	}

	public BigDecimal getTotalCompras() {
		return totalCompras;
	}

	public void setTotalCompras(BigDecimal totalCompras) {
		this.totalCompras = totalCompras;
	}

	public BigDecimal getTotalVendas() {
		return totalVendas;
	}

	public void setTotalVendas(BigDecimal totalVendas) {
		this.totalVendas = totalVendas;
	}

	public CategoriaProduto getCategoriaProduto() {
		return categoriaProduto;
	}

	public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
		this.categoriaProduto = categoriaProduto;
	}

	public Fornecedor getFornecedor() {
		return fornecedor;
	}

	public void setFornecedor(Fornecedor fornecedor) {
		this.fornecedor = fornecedor;
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
		Produto other = (Produto) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Transient
	private BigDecimal precoMedioVenda = BigDecimal.ZERO;
	
	@Transient
	private Long quantidadeItensComprados = 0L;
	
	@Transient
	private Long quantidadeItensVendidos = 0L;

	public BigDecimal getPrecoMedioVenda() {
		return precoMedioVenda;
	}

	public void setPrecoMedioVenda(BigDecimal precoMedioVenda) {
		this.precoMedioVenda = precoMedioVenda;
	}

	public Long getQuantidadeItensComprados() {
		return quantidadeItensComprados;
	}

	public void setQuantidadeItensComprados(Long quantidadeItensComprados) {
		this.quantidadeItensComprados = quantidadeItensComprados;
	}

	public Long getQuantidadeItensVendidos() {
		return quantidadeItensVendidos;
	}

	public void setQuantidadeItensVendidos(Long quantidadeItensVendidos) {
		this.quantidadeItensVendidos = quantidadeItensVendidos;
	}

}