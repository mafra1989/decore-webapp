package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
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
	
	@Column
	private String codigoDeBarras;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String nome;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String descricao;
	

	@Column(columnDefinition="TEXT")
	private String informacaoExtra;

	
	@Column
	private String marca;
	
	@Column
	private String cor;
	
	@Column
	private String numeracao;
	
	
	@Column
	private String locacao;
	
	@Column(length = 5, nullable = false)
	private String unidadeMedida = "Un";
	
	
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column
	private byte[] foto;
	

	@Column
	private String urlImagem;

	@NotNull
	@Column
	@Digits(integer = 10 /* precision */, fraction = 3 /* scale */)
	private BigDecimal quantidadeAtual = BigDecimal.ZERO;
	
	
	@NotNull
	@Column
	@Digits(integer = 10 /* precision */, fraction = 3 /* scale */)
	private BigDecimal estoqueMinimo = BigDecimal.ZERO;
	

	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal margemLucro = BigDecimal.ZERO;
	
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 0 /* scale */)
	private BigDecimal desconto = BigDecimal.ZERO;
	
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal margemContribuicao = BigDecimal.valueOf(20);
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal margemContribuicaoEmDinheiro = BigDecimal.valueOf(20);
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal margemLucroReal = BigDecimal.ZERO;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal margemLucroRealEmDinheiro = BigDecimal.valueOf(20);
	
	@NotNull
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal precoDeVenda = BigDecimal.ZERO;
	

	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal totalAcumulado = BigDecimal.ZERO;
	
	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal estimativaLucro = BigDecimal.ZERO;

	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal totalCompras = BigDecimal.ZERO;

	@Transient
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal totalVendas = BigDecimal.ZERO;

	@NotNull
	@ManyToOne
	@JoinColumn
	private CategoriaProduto categoriaProduto;

	
	@ManyToOne
	@JoinColumn
	private Fornecedor fornecedor;
	
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal custoTotal = BigDecimal.ZERO;
		
	@NotNull
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal custoMedioUnitario = BigDecimal.ZERO;
	
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal estorno = BigDecimal.ZERO;
	
	
	@Type(type = "yes_no")
	@Column
	private boolean destaque;
	
	
	@Type(type = "yes_no")
	@Column
	private boolean estoque;
	

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

	public String getInformacaoExtra() {
		return informacaoExtra;
	}

	public void setInformacaoExtra(String informacaoExtra) {
		this.informacaoExtra = informacaoExtra;
	}

	public String getLocacao() {
		return locacao;
	}

	public void setLocacao(String locacao) {
		this.locacao = locacao;
	}
	
	public String getUnidadeMedida() {
		return unidadeMedida;
	}

	public void setUnidadeMedida(String unidadeMedida) {
		this.unidadeMedida = unidadeMedida;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	public String getUrlImagem() {
		return urlImagem;
	}

	public void setUrlImagem(String urlImagem) {
		this.urlImagem = urlImagem;
	}

	public BigDecimal getQuantidadeAtual() {
		return quantidadeAtual;
	}

	public void setQuantidadeAtual(BigDecimal quantidadeAtual) {
		this.quantidadeAtual = quantidadeAtual.setScale(3, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getEstoqueMinimo() {
		return estoqueMinimo;
	}

	public void setEstoqueMinimo(BigDecimal estoqueMinimo) {
		this.estoqueMinimo = estoqueMinimo.setScale(3, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getMargemLucro() {
		return margemLucro;
	}

	public void setMargemLucro(BigDecimal margemLucro) {
		this.margemLucro = margemLucro.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getTotalAcumulado() {
		return totalAcumulado;
	}

	public void setTotalAcumulado(BigDecimal totalAcumulado) {
		this.totalAcumulado = totalAcumulado.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getEstimativaLucro() {
		return estimativaLucro;
	}

	public void setEstimativaLucro(BigDecimal estimativaLucro) {
		this.estimativaLucro = estimativaLucro.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getTotalCompras() {
		return totalCompras;
	}

	public void setTotalCompras(BigDecimal totalCompras) {
		this.totalCompras = totalCompras.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getTotalVendas() {
		return totalVendas;
	}

	public void setTotalVendas(BigDecimal totalVendas) {
		this.totalVendas = totalVendas.setScale(4, BigDecimal.ROUND_HALF_EVEN);
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

	public BigDecimal getCustoTotal() {
		return custoTotal;
	}

	public void setCustoTotal(BigDecimal custoTotal) {
		this.custoTotal = custoTotal.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getCustoMedioUnitario() {
		return custoMedioUnitario;
	}

	public void setCustoMedioUnitario(BigDecimal custoMedioUnitario) {
		this.custoMedioUnitario = custoMedioUnitario != null ? custoMedioUnitario.setScale(4, BigDecimal.ROUND_HALF_EVEN) : null;
	}

	public BigDecimal getEstorno() {
		return estorno;
	}

	public void setEstorno(BigDecimal estorno) {
		this.estorno = estorno.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public String getCodigoDeBarras() {
		return codigoDeBarras;
	}

	public void setCodigoDeBarras(String codigoDeBarras) {
		this.codigoDeBarras = codigoDeBarras;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getCor() {
		return cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public String getNumeracao() {
		return numeracao;
	}

	public void setNumeracao(String numeracao) {
		this.numeracao = numeracao;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto.setScale(0, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getMargemContribuicao() {
		return margemContribuicao;
	}

	public void setMargemContribuicao(BigDecimal margemContribuicao) {
		this.margemContribuicao = margemContribuicao.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getMargemContribuicaoEmDinheiro() {
		return margemContribuicaoEmDinheiro;
	}

	public void setMargemContribuicaoEmDinheiro(BigDecimal margemContribuicaoEmDinheiro) {
		this.margemContribuicaoEmDinheiro = margemContribuicaoEmDinheiro.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getMargemLucroReal() {
		return margemLucroReal;
	}

	public void setMargemLucroReal(BigDecimal margemLucroReal) {
		this.margemLucroReal = margemLucroReal.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getMargemLucroRealEmDinheiro() {
		return margemLucroRealEmDinheiro;
	}

	public void setMargemLucroRealEmDinheiro(BigDecimal margemLucroRealEmDinheiro) {
		this.margemLucroRealEmDinheiro = margemLucroRealEmDinheiro.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getPrecoDeVenda() {
		return precoDeVenda;
	}

	public void setPrecoDeVenda(BigDecimal precoDeVenda) {
		this.precoDeVenda = precoDeVenda != null ? precoDeVenda.setScale(2, BigDecimal.ROUND_HALF_EVEN) : null;
	}

	public boolean isDestaque() {
		return destaque;
	}

	public void setDestaque(boolean destaque) {
		this.destaque = destaque;
	}

	public boolean isEstoque() {
		return estoque;
	}

	public void setEstoque(boolean estoque) {
		this.estoque = estoque;
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
	private BigDecimal precoMedioCompra = BigDecimal.ZERO;
	
	@Transient
	private BigDecimal precoMedioVenda = BigDecimal.ZERO;

	@Transient
	private Double quantidadeItensComprados = 0D;

	@Transient
	private Double quantidadeItensVendidos = 0D;

	@Transient
	private String percentualVenda = "0";

	@Transient
	private Long quantidadePedido = 1L;

	@Transient
	private String codeTemp;
	
	@Transient
	private String quantidade;
	
	@Transient
	private String valor;
	
	@Transient
	private Double totalAjusteItensComprados = 0D;

	@Transient
	private Double totalAjusteItensVendidos = 0D;
	
	@Transient
	private Long descontoMaximo = 0L;
	
	@Transient
	private BigDecimal valorPago = BigDecimal.ZERO;
	
	@Transient
	private String quantidadeAtualFormatada;
	
	@Transient
	private boolean valid = true;
	
	@Transient
	private String NomeFormatado;
	
	@Transient
	private String NomeCompleto;
	
	@Transient
	private String descricaoFormatada;
	
	@Transient
	private String descricaoCompleta;
	
	
	public BigDecimal getPrecoMedioCompra() {
		return precoMedioCompra;
	}

	public void setPrecoMedioCompra(BigDecimal precoMedioCompra) {
		this.precoMedioCompra = precoMedioCompra;
	}
	
	public BigDecimal getPrecoMedioVenda() {
		return precoMedioVenda;
	}

	public void setPrecoMedioVenda(BigDecimal precoMedioVenda) {
		this.precoMedioVenda = precoMedioVenda;
	}

	public BigDecimal getPrecoMedioUnitario() {
		return new BigDecimal(custoMedioUnitario.doubleValue() * (1 + (margemLucro.doubleValue()/100))).setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getMargemLucroEmDinheiro() {
		return new BigDecimal(custoMedioUnitario.doubleValue() * (margemLucro.doubleValue()/100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public Double getQuantidadeItensComprados() {
		return quantidadeItensComprados;
	}

	public void setQuantidadeItensComprados(Double quantidadeItensComprados) {
		this.quantidadeItensComprados = quantidadeItensComprados;
	}

	public Double getQuantidadeItensVendidos() {
		return quantidadeItensVendidos;
	}

	public void setQuantidadeItensVendidos(Double quantidadeItensVendidos) {
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

	public Double getTotalAjusteItensComprados() {
		return totalAjusteItensComprados;
	}

	public void setTotalAjusteItensComprados(Double totalAjusteItensComprados) {
		this.totalAjusteItensComprados = totalAjusteItensComprados;
	}

	public Double getTotalAjusteItensVendidos() {
		return totalAjusteItensVendidos;
	}

	public void setTotalAjusteItensVendidos(Double totalAjusteItensVendidos) {
		this.totalAjusteItensVendidos = totalAjusteItensVendidos;
	}

	public Long getDescontoMaximo() {
		return descontoMaximo;
	}

	public void setDescontoMaximo(Long descontoMaximo) {
		this.descontoMaximo = descontoMaximo;
	}
	
	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago.setScale(2, BigDecimal.ROUND_HALF_EVEN);;
	}
	
	public String getQuantidadeAtualFormatada() {
		return quantidadeAtualFormatada;
	}

	public void setQuantidadeAtualFormatada(String quantidadeAtualFormatada) {
		this.quantidadeAtualFormatada = quantidadeAtualFormatada;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getNomeFormatado() {
		return NomeFormatado;
	}

	public void setNomeFormatado(String nomeFormatado) {
		NomeFormatado = nomeFormatado;
	}

	public String getDescricaoFormatada() {
		return descricaoFormatada;
	}

	public void setDescricaoFormatada(String descricaoFormatada) {
		this.descricaoFormatada = descricaoFormatada;
	}

	public String getDescricaoConvertida() {
		return convertToTitleCaseIteratingChars(descricao);
	}
	
	public String getNomeCompleto() {
		return NomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		NomeCompleto = nomeCompleto;
	}

	public String getDescricaoCompleta() {
		return descricaoCompleta;
	}

	public void setDescricaoCompleta(String descricaoCompleta) {
		this.descricaoCompleta = descricaoCompleta;
	}

	public String convertToTitleCaseIteratingChars(String text) {
	    if (text == null || text.isEmpty()) {
	        return text;
	    }
	 
	    StringBuilder converted = new StringBuilder();
	 
	    boolean convertNext = true;
	    for (char ch : text.toCharArray()) {
	        if (Character.isSpaceChar(ch)) {
	            convertNext = true;
	        } else if (convertNext) {
	            ch = Character.toTitleCase(ch);
	            convertNext = false;
	        } else {
	            ch = Character.toLowerCase(ch);
	        }
	        converted.append(ch);
	    }
	 
	    return converted.toString();
	}

}