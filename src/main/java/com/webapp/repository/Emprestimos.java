package com.webapp.repository;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import com.webapp.model.Emprestimo;
import com.webapp.model.StatusEmprestimo;
import com.webapp.repository.filter.EmprestimoFilter;
import com.webapp.util.jpa.Transacional;

public class Emprestimos implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private EntityManager manager;

	public Emprestimo porId(Long id) {
		return this.manager.find(Emprestimo.class, id);
	}

	@Transacional
	public Emprestimo save(Emprestimo emprestimo) {
		return this.manager.merge(emprestimo);
	}

	@Transacional
	public void remove(Emprestimo emprestimo) {
		Emprestimo emprestimoTemp = new Emprestimo();
		emprestimoTemp = this.manager.merge(emprestimo);

		this.manager.remove(emprestimoTemp);
	}

	public List<Emprestimo> todos() {
		return this.manager.createQuery("from Emprestimo order by id", Emprestimo.class).getResultList();
	}

	public List<Emprestimo> porStatus(StatusEmprestimo statusEmprestimo) {
		return this.manager
				.createQuery("from Emprestimo e where e.statusEmprestimo = :status order by id", Emprestimo.class)
				.setParameter("status", statusEmprestimo).getResultList();
	}

	public List<Emprestimo> filtrados(EmprestimoFilter filter) {

		TypedQuery<Emprestimo> typedQuery;

		if (StringUtils.isNotBlank(filter.getNome())) {
			typedQuery = manager.createQuery(
					"select e from Emprestimo e join fetch e.cliente c where c.nome like :nome order by e.id",
					Emprestimo.class).setParameter("nome", "%" + filter.getNome() + "%");

		} else {
			typedQuery = manager.createQuery("select e from Emprestimo e", Emprestimo.class);
		}

		return typedQuery.getResultList();

	}

	public List<Emprestimo> porCliente(Long id) {

		TypedQuery<Emprestimo> typedQuery;

		typedQuery = manager
				.createQuery("select e from Emprestimo e join fetch e.cliente c where c.id = :id order by e.id",
						Emprestimo.class)
				.setParameter("id", id);

		return typedQuery.getResultList();

	}

}