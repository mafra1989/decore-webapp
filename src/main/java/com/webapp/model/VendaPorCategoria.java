package com.webapp.model;

import java.io.Serializable;

public class VendaPorCategoria implements Serializable {

	private static final long serialVersionUID = 1L;

	private String item;

	private Number value;
	
	private Number quantidade;

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

}