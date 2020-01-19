package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.CategoriaLancamento;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = CategoriaLancamento.class)
public class CategoriaLancamentoConverter implements Converter {

	private CategoriasLancamentos categoriasDespesas = CDIServiceLocator.getBean(CategoriasLancamentos.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		CategoriaLancamento retorno = null;

		if (value != null) {
			retorno = this.categoriasDespesas.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((CategoriaLancamento) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}