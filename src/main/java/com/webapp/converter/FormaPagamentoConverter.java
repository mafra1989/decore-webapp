package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.FormaPagamento;
import com.webapp.repository.FormasPagamentos;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = FormaPagamento.class)
public class FormaPagamentoConverter implements Converter {

	private FormasPagamentos formasPagamentos = CDIServiceLocator.getBean(FormasPagamentos.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		FormaPagamento retorno = null;

		if (value != null) {
			retorno = formasPagamentos.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((FormaPagamento) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}