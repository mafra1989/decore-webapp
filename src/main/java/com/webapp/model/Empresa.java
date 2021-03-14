package com.webapp.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "empresas")
@SequenceGenerator(name="Empresa_Seq", sequenceName="empresas_sequence", allocationSize=1, initialValue = 2)
public class Empresa implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String nome;
	
	private String cnpj;
	private String endereco;
	private String numero;
	private String bairro;
	
	private String cidade;
	private String uf;
	
	private String contato;
	
	private byte[] logo;
	
	private byte[] logoRelatorio;

	@Id
	//@GeneratedValue
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Empresa_Seq")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@NotBlank
	@Column(nullable = false, length = 120)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(nullable = true, length = 30)
	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	@Column(columnDefinition="TEXT")
	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	@Column(nullable = true, length = 10)
	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	@Column(nullable = true, length = 120)
	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	@Column(nullable = true, length = 60)
	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	@Column(nullable = true, length = 60)
	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	@Column(nullable = true, length = 20)
	public String getContato() {
		return contato;
	}

	public void setContato(String contato) {
		this.contato = contato;
	}

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column
	public byte[] getLogo() {
		return logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column
	public byte[] getLogoRelatorio() {
		return logoRelatorio;
	}

	public void setLogoRelatorio(byte[] logoRelatorio) {
		this.logoRelatorio = logoRelatorio;
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
		Empresa other = (Empresa) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}