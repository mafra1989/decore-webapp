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
@Table(name = "vendas")
public class Venda implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue // (strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column
	private Date dataVenda = new Date();
	
	@Column
	private Long numeroVenda;

	@NotNull
	@Column
	private Long quantidadeItens = 0L;

	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal lucro = BigDecimal.ZERO;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal percentualLucro = BigDecimal.ZERO;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal totalDesconto = BigDecimal.ZERO;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal totalDescontoEmDinheiro = BigDecimal.ZERO;

	@Column
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorCompra = BigDecimal.ZERO;

	@NotNull
	@ManyToOne
	@JoinColumn
	private TipoVenda tipoVenda;

	@NotNull
	@ManyToOne
	@JoinColumn
	private Usuario usuario;

	@NotNull
	@ManyToOne
	@JoinColumn
	private Bairro bairro = new Bairro();
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean status;
		
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
	@Column(nullable = true)
	private boolean pdv;
	
	
	@Type(type = "yes_no")
	@Column(nullable = true)
	private boolean recuperarValores = false;
	
	//@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private TipoPagamento tipoPagamento;
	
	//@NotNull
	@ManyToOne
	@JoinColumn
	private FormaPagamento formaPagamento;
	
	
	//@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal taxaDeEntrega = BigDecimal.ZERO;
	
	
	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal acrescimo = BigDecimal.ZERO;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal taxaDeAcrescimo;
	
	
	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal valorRecebido = BigDecimal.ZERO;
	
	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal faltando = BigDecimal.ZERO;
	
	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal troco = BigDecimal.ZERO;
	
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean vendaPaga = true;
	
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean conta;
	
	
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal estorno = BigDecimal.ZERO;
	
	
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal saldoParaTroca = BigDecimal.ZERO;
	
	
	@NotNull
	@ManyToOne
	@JoinColumn
	private Cliente cliente = new Cliente();
	
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal desconto;
	
	
	@ManyToOne
	@JoinColumn
	private Usuario entregador;
	
	
	@Column
	private String mesa;
	
	
	@Column
	private String statusMesa;
	
	
	@Type(type = "yes_no")
	@Column(nullable = true)
	private boolean clientePagouTaxa;
	
	
	@Type(type = "yes_no")
	@Column(nullable = true)
	private boolean exclusao = false;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataVenda() {
		return dataVenda;
	}

	public void setDataVenda(Date dataVenda) {
		this.dataVenda = dataVenda;
	}

	public Long getNumeroVenda() {
		return numeroVenda;
	}

	public void setNumeroVenda(Long numeroVenda) {
		this.numeroVenda = numeroVenda;
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

	public BigDecimal getTotalDesconto() {
		return totalDesconto;
	}

	public void setTotalDesconto(BigDecimal totalDesconto) {
		this.totalDesconto = totalDesconto.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getTotalDescontoEmDinheiro() {
		return totalDescontoEmDinheiro;
	}

	public void setTotalDescontoEmDinheiro(BigDecimal totalDescontoEmDinheiro) {
		this.totalDescontoEmDinheiro = totalDescontoEmDinheiro.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getValorCompra() {
		return valorCompra;
	}

	public void setValorCompra(BigDecimal valorCompra) {
		this.valorCompra = valorCompra.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public TipoVenda getTipoVenda() {
		return tipoVenda;
	}

	public void setTipoVenda(TipoVenda tipoVenda) {
		this.tipoVenda = tipoVenda;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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

	public boolean isPdv() {
		return pdv;
	}

	public void setPdv(boolean pdv) {
		this.pdv = pdv;
	}

	public boolean isRecuperarValores() {
		return recuperarValores;
	}

	public void setRecuperarValores(boolean recuperarValores) {
		this.recuperarValores = recuperarValores;
	}

	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(TipoPagamento tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}

	public Bairro getBairro() {
		return bairro;
	}

	public void setBairro(Bairro bairro) {
		this.bairro = bairro;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isAjuste() {
		return ajuste;
	}

	public void setAjuste(boolean ajuste) {
		this.ajuste = ajuste;
	}

	public BigDecimal getTaxaDeEntrega() {
		return taxaDeEntrega;
	}

	public void setTaxaDeEntrega(BigDecimal taxaDeEntrega) {
		this.taxaDeEntrega = taxaDeEntrega.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getAcrescimo() {
		return acrescimo;
	}

	public void setAcrescimo(BigDecimal acrescimo) {
		this.acrescimo = acrescimo.setScale(2, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getTaxaDeAcrescimo() {
		return taxaDeAcrescimo;
	}

	public void setTaxaDeAcrescimo(BigDecimal taxaDeAcrescimo) {
		this.taxaDeAcrescimo = taxaDeAcrescimo;
	}

	public BigDecimal getValorRecebido() {
		return valorRecebido;
	}

	public void setValorRecebido(BigDecimal valorRecebido) {
		this.valorRecebido = valorRecebido != null ? valorRecebido.setScale(2, BigDecimal.ROUND_HALF_EVEN) : BigDecimal.ZERO;
	}

	public BigDecimal getFaltando() {
		return faltando;
	}

	public void setFaltando(BigDecimal faltando) {
		this.faltando = faltando != null ? faltando.setScale(2, BigDecimal.ROUND_HALF_EVEN) : BigDecimal.ZERO;
	}

	public BigDecimal getTroco() {
		return troco;
	}

	public void setTroco(BigDecimal troco) {
		this.troco = troco != null ? troco.setScale(2, BigDecimal.ROUND_HALF_EVEN) : BigDecimal.ZERO;
	}

	public boolean isVendaPaga() {
		return vendaPaga;
	}

	public void setVendaPaga(boolean vendaPaga) {
		this.vendaPaga = vendaPaga;
	}

	public boolean isConta() {
		return conta;
	}

	public void setConta(boolean conta) {
		this.conta = conta;
	}

	public BigDecimal getEstorno() {
		return estorno;
	}

	public void setEstorno(BigDecimal estorno) {
		this.estorno = estorno.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public BigDecimal getSaldoParaTroca() {
		return saldoParaTroca;
	}

	public void setSaldoParaTroca(BigDecimal saldoParaTroca) {
		this.saldoParaTroca = saldoParaTroca.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto != null ? desconto.setScale(2, BigDecimal.ROUND_HALF_EVEN) : null;
	}

	public Usuario getEntregador() {
		return entregador;
	}

	public void setEntregador(Usuario entregador) {
		this.entregador = entregador;
	}

	public String getMesa() {
		return mesa;
	}

	public void setMesa(String mesa) {
		this.mesa = mesa;
	}

	public String getStatusMesa() {
		return statusMesa;
	}

	public void setStatusMesa(String statusMesa) {
		this.statusMesa = statusMesa;
	}
	
	public boolean isClientePagouTaxa() {
		return clientePagouTaxa;
	}

	public void setClientePagouTaxa(boolean clientePagouTaxa) {
		this.clientePagouTaxa = clientePagouTaxa;
	}

	public boolean isExclusao() {
		return exclusao;
	}

	public void setExclusao(boolean exclusao) {
		this.exclusao = exclusao;
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
		Venda other = (Venda) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Transient
	private List<ItemVenda> itensVenda;
	
	@Transient
	private BigDecimal valorTotalComDesconto = BigDecimal.ZERO;
	
	@Transient
	private String pagamento = "";

	public List<ItemVenda> getItensVenda() {
		return itensVenda;
	}

	public void setItensVenda(List<ItemVenda> itensVenda) {
		this.itensVenda = itensVenda;
	}

	public BigDecimal getValorTotalComDesconto() {
		return valorTotalComDesconto;
	}

	public void setValorTotalComDesconto(BigDecimal valorTotalComDesconto) {
		this.valorTotalComDesconto = valorTotalComDesconto.setScale(4, BigDecimal.ROUND_HALF_EVEN);
	}

	public String getPagamento() {
		return pagamento;
	}

	public void setPagamento(String pagamento) {
		this.pagamento = pagamento;
	}

}