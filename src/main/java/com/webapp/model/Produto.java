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
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "produtos")
public class Produto implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue // (strategy = GenerationType.IDENTITY)
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

	@Column
	private String locacao;
	
	/*
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column
	private byte[] foto;
	*/

	@Column
	private String urlImagem;

	@NotNull
	@Column
	private Long quantidadeAtual = 0L;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 0 /* scale */)
	private BigDecimal margemLucro = BigDecimal.valueOf(20);

	@Transient
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal totalAcumulado = BigDecimal.ZERO;

	@Transient
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal totalCompras = BigDecimal.ZERO;

	@Transient
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

	public String getLocacao() {
		return locacao;
	}

	public void setLocacao(String locacao) {
		this.locacao = locacao;
	}
/*
	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}
*/
	public String getUrlImagem() {
		return urlImagem;
	}

	public void setUrlImagem(String urlImagem) {
		this.urlImagem = urlImagem;
	}

	public Long getQuantidadeAtual() {
		return quantidadeAtual;
	}

	public void setQuantidadeAtual(Long quantidadeAtual) {
		this.quantidadeAtual = quantidadeAtual;
	}

	public BigDecimal getMargemLucro() {
		return margemLucro;
	}

	public void setMargemLucro(BigDecimal margemLucro) {
		this.margemLucro = margemLucro.setScale(0, BigDecimal.ROUND_HALF_EVEN);
		;
	}

	public BigDecimal getTotalAcumulado() {
		return totalAcumulado;
	}

	public void setTotalAcumulado(BigDecimal totalAcumulado) {
		this.totalAcumulado = totalAcumulado.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		;
	}

	public BigDecimal getTotalCompras() {
		return totalCompras;
	}

	public void setTotalCompras(BigDecimal totalCompras) {
		this.totalCompras = totalCompras.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		;
	}

	public BigDecimal getTotalVendas() {
		return totalVendas;
	}

	public void setTotalVendas(BigDecimal totalVendas) {
		this.totalVendas = totalVendas.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		;
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

	@Transient
	private String percentualVenda = "0";

	@Transient
	private Long quantidadePedido = 0L;

	@Transient
	private String codeTemp;
	
	@Transient
	private String quantidade;
	
	@Transient
	private String valor;

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

	public String getPercentualVenda() {
		return percentualVenda;
	}

	public void setPercentualVenda(String percentualVenda) {
		this.percentualVenda = percentualVenda;
	}

	public Long getQuantidadePedido() {
		return quantidadePedido;
	}

	public void setQuantidadePedido(Long quantidadePedido) {
		this.quantidadePedido = quantidadePedido;
	}

	public String getCodeTemp() {
		return codeTemp;
	}

	public void setCodeTemp(String codeTemp) {
		this.codeTemp = codeTemp;
	}

	public String getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(String quantidade) {
		this.quantidade = quantidade;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

}