package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.Caixa;
import com.webapp.model.Empresa;
import com.webapp.model.FormaPagamento;
import com.webapp.model.ItemCaixa;
import com.webapp.model.Usuario;
import com.webapp.util.jpa.Transacional;

public class Caixas implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Caixa porId(Long id) {
		return this.manager.find(Caixa.class, id);
	}

	@Transacional
	public Caixa save(Caixa caixa) {
		return this.manager.merge(caixa);
	}

	@Transacional
	public void remove(Caixa caixa) {
		Caixa caixaTemp = new Caixa();
		caixaTemp = this.manager.merge(caixa);

		this.manager.remove(caixaTemp);
	}
	
	public List<Caixa> todos(Empresa empresa) {
		return this.manager.createQuery("from Caixa where empresa.id = :empresa order by id", Caixa.class)
				.setParameter("empresa", empresa.getId()).getResultList();
	}

	public Caixa porUsuario(Usuario usuario, Empresa empresa) {
		try {
			return this.manager
					.createQuery("select e from Caixa e where e.status = 'N' and e.usuario.id = :id and e.empresa.id = :empresa", Caixa.class)
					.setParameter("id", usuario.getId())
					.setParameter("empresa", empresa.getId()).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ItemCaixa> porFormaPagamento(Usuario usuario, FormaPagamento formaPagamento, Empresa empresa) {
		
		String pagamento = "";
		if(formaPagamento != null) {
			pagamento = "e.formaPagamento.id = :formaPagamentoID and";
		}
		
		String jpql = "select e from ItemCaixa e where " + pagamento + " e.caixa.usuario.id = :id and e.caixa.empresa.id = :empresa and e.caixa.status = 'N'";
		Query q = manager.createQuery(jpql).setParameter("id", usuario.getId())
				.setParameter("empresa", empresa.getId());

		if(formaPagamento != null) {
			q.setParameter("formaPagamentoID", formaPagamento.getId());
		}
		
		return q.getResultList();
	}
}