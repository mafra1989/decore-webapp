package com.webapp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "anexo")
public class Anexo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String fileName;
	
	private byte[] file;
	
	private Cliente cliente;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable = false, length = 80)
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Lob
	@Column(nullable = false)
	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}
	
	@ManyToOne
	@JoinColumn(nullable = false)
	public Cliente getCliente() {
		return cliente;
	}
	
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
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
		Anexo other = (Anexo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}