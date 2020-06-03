package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.Bairro;
import com.webapp.repository.Bairros;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = Bairro.class)
public class BairroConverter implements Converter {

	private Bairros bairros = CDIServiceLocator.getBean(Bairros.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Bairro retorno = null;

		if (value != null) {
			retorno = bairros.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((Bairro) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}