package com.webapp.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TipoOperacao {

	COMPRA(1L, "Compra"), VENDA(2L, "Venda"), LANCAMENTO(3L, "Lançamento"), DEVOLUCAO(4L, "Devolução/Troca");

	private Long id;
	private String descricao;

	TipoOperacao(Long id, String descricao) {
		this.id = id;
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

	public Long getId() {
		return id;
	}
	
	public static List<TipoOperacao> filtrarOperacoesContasPagarReceber() {
		return Stream.of(values()).filter(tipoOperacao -> tipoOperacao.getId() < 4L).collect(Collectors.toList());		
	}

}
