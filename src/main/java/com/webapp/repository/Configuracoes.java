package com.webapp.repository;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.webapp.model.Configuracao;
import com.webapp.util.jpa.Transacional;

public class Configuracoes implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;
	
	public Configuracao porId(Long id) {
		return this.manager.find(Configuracao.class, id);
	}
	
	@Transacional
	public Configuracao save(Configuracao configuracao) {
		return this.manager.merge(configuracao);
	}
	
}