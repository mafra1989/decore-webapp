package com.webapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "pedidos")
public class Pedido implements Serializable {

	private static final long serialVersionUID = 203356217547759664L;
	
	/* Dados do Pedido */
	@Id
	@GeneratedValue
	private Long id;
	
	@NotNull
	@Column(nullable = false)
	private Date dataPedido = new Date();
	
	@NotNull
	@Column(nullable = false)
	private Long quantidadeItens = 0L;
	
	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal valorTotal = BigDecimal.ZERO;
	
	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 4 /* scale */)
	private BigDecimal lucro = BigDecimal.ZERO;

	@NotNull
	@Column(nullable = false)
	@Digits(integer = 10 /* precision */, fraction = 2 /* scale */)
	private BigDecimal percentualLucro = BigDecimal.ZERO;
	
	@Column(nullable = false, length = 20)
	private String cupom;
	
	@Column
	@Digits(integer = 10 /* precision */, fraction = 0 /* scale */)
	private BigDecimal desconto = BigDecimal.ZERO;
	
	@NotBlank
	@Column(nullable = false, length = 60)
	private String empresa;
	
	@Column(nullable = false, length = 20)
	private String status;
	
	@Type(type = "yes_no")
	@Column(nullable = false)
	private boolean emailenviado;

	/* Informações de contato */
	@Email
	@NotBlank
	@Column(nullable = false, length = 120)
	private String email;

	/* Endereço de entrega */
	@NotBlank
	@Column(nullable = false, length = 120)
	private String nome;
	
	@NotBlank
	@Column(nullable = false, length = 12)
	private String telefone;
	
	@NotBlank
	@Column(nullable = false, columnDefinition="TEXT")
	private String endereco;
	
	@NotNull
	@ManyToOne
	@JoinColumn(nullable = false)
	private Bairro bairro;
	
	@NotBlank
	@Column(nullable = false, length = 10)
	private String cep;

	
	/* Campos para relatório */
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
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataPedido() {
		return dataPedido;
	}

	public void setDataPedido(Date dataPedido) {
		this.dataPedido = dataPedido;
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

	public String getCupom() {
		return cupom;
	}

	public void setCupom(String cupom) {
		this.cupom = cupom;
	}

	public BigDecimal getDesconto() {
		return desconto;
	}

	public void setDesconto(BigDecimal desconto) {
		this.desconto = desconto;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isEmailenviado() {
		return emailenviado;
	}

	public void setEmailenviado(boolean emailenviado) {
		this.emailenviado = emailenviado;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public Bairro getBairro() {
		return bairro;
	}

	public void setBairro(Bairro bairro) {
		this.bairro = bairro;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
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
		Pedido other = (Pedido) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}