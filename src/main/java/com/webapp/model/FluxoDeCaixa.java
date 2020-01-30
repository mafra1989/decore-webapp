package com.webapp.model;

import java.io.Serializable;

public class FluxoDeCaixa implements Serializable {

	private static final long serialVersionUID = 1L;

	private String item;

	private Double value;

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}