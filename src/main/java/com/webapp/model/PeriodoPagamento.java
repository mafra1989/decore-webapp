package com.webapp.model;

public enum PeriodoPagamento {

	/*ANOS("Anos"), SEMESTRES("Semestres"), TRIMESTRES("Trimestres"), BIMESTRES("Bimestres"), */
	MESES("Meses"), QUINZENAS("Quinzenas"), SEMANAS("Semanas"), DIAS("Dias");

	private String descricao;

	PeriodoPagamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
