package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.DestinoLancamento;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = DestinoLancamento.class)
public class DestinoLancamentoConverter implements Converter {

	private DestinosLancamentos destinoLancamento = CDIServiceLocator.getBean(DestinosLancamentos.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		DestinoLancamento retorno = null;

		if (value != null) {
			retorno = this.destinoLancamento.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((DestinoLancamento) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}