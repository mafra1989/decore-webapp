package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.webapp.model.Caixa;
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
	
	public List<Caixa> todos(String empresa) {
		return this.manager.createQuery("from Caixa where empresa = :empresa order by id", Caixa.class)
				.setParameter("empresa", empresa).getResultList();
	}

	public Caixa porUsuario(Usuario usuario, String empresa) {
		try {
			return this.manager
					.createQuery("select e from Caixa e where e.status = 'N' and e.usuario.id = :id and e.empresa = :empresa", Caixa.class)
					.setParameter("id", usuario.getId())
					.setParameter("empresa", empresa).getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ItemCaixa> porFormaPagamento(Usuario usuario, FormaPagamento formaPagamento, String empresa) {
		
		String pagamento = "";
		if(formaPagamento != null) {
			pagamento = "e.formaPagamento.id = :formaPagamentoID and";
		}
		
		String jpql = "select e from ItemCaixa e where " + pagamento + " e.caixa.usuario.id = :id and e.caixa.empresa = :empresa and e.caixa.status = 'N'";
		Query q = manager.createQuery(jpql).setParameter("id", usuario.getId())
				.setParameter("empresa", empresa);

		if(formaPagamento != null) {
			q.setParameter("formaPagamentoID", formaPagamento.getId());
		}
		
		return q.getResultList();
	}
}