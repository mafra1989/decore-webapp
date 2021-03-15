package com.webapp.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.webapp.model.Empresa;
import com.webapp.model.Log;
import com.webapp.model.TipoAtividade;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Logs implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Log porId(Long id) {
		return this.manager.find(Log.class, id);
	}

	@Transacional
	public Log save(Log log) {
		return this.manager.merge(log);
	}

	@Transacional
	public void remove(Log log) {
		Log logTemp = new Log();
		logTemp = this.manager.merge(log);

		this.manager.remove(logTemp);
	}
	

	@SuppressWarnings("unchecked")
	public List<Log> logsFiltrados(Date dateStart, Date dateStop,
			TipoAtividade tipoAtividade, Usuario usuario, Empresa empresa) {

		String conditionOperacao = "";
		String conditionUsuario = "";
		
		if (tipoAtividade != null) { 
			conditionOperacao += "AND i.operacao = :operacao ";
		}
		
		if (usuario != null && usuario.getId() != null) { 
			 conditionUsuario += "AND i.usuario.id = :idUsuario ";
		}

		String jpql = "SELECT i FROM Log i WHERE i.usuario.empresa.id = :empresa AND i.dataLog between :dateStart and :dateStop "
				+ conditionOperacao + conditionUsuario
				+ "order by i.id asc";

		System.out.println(jpql);

		Query q = manager.createQuery(jpql).setParameter("empresa", empresa.getId()).setParameter("dateStart", dateStart).setParameter("dateStop", dateStop);

		if (tipoAtividade != null) { 
			q.setParameter("operacao", tipoAtividade.name());
		}
		
		if (usuario != null && usuario.getId() != null) { 
			 q.setParameter("idUsuario", usuario.getId());
		}

		return q.getResultList();
	}

}
