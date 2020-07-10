package com.webapp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name = "categoria_lancamentos")
public class CategoriaLancamento implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	@NotBlank
	private String nome;
	
	private TipoLancamento tipoLancamento = new TipoLancamento();
	
	private DestinoLancamento destinoLancamento = new DestinoLancamento();
	
	@Column
	private String empresa;
	
	@Id
	@GeneratedValue//(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable = false, length = 120)
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@NotNull
	@ManyToOne
	@JoinColumn
	public TipoLancamento getTipoLancamento() {
		return tipoLancamento;
	}

	public void setTipoLancamento(TipoLancamento tipoLancamento) {
		this.tipoLancamento = tipoLancamento;
	}

	@ManyToOne
	@JoinColumn
	public DestinoLancamento getDestinoLancamento() {
		return destinoLancamento;
	}

	public void setDestinoLancamento(DestinoLancamento destinoLancamento) {
		this.destinoLancamento = destinoLancamento;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
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
		CategoriaLancamento other = (CategoriaLancamento) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
