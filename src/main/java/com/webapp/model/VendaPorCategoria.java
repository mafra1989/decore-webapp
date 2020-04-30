package com.webapp.model;

import java.io.Serializable;

public class VendaPorCategoria implements Serializable {

	private static final long serialVersionUID = 1L;

	
	private String item;

	private Number value;

	private Number quantidade;
	
	private String codigo;


	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}

	public Number getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Number quantidade) {
		this.quantidade = quantidade;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
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
		VendaPorCategoria other = (VendaPorCategoria) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		return true;
	}

}