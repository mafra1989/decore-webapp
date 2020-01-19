package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.Fornecedor;
import com.webapp.repository.Fornecedores;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = Fornecedor.class)
public class FornecedorConverter implements Converter {

	private Fornecedores fornecedores = CDIServiceLocator.getBean(Fornecedores.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Fornecedor retorno = null;

		if (value != null) {
			retorno = this.fornecedores.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((Fornecedor) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}