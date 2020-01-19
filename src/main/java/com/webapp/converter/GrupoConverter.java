package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.Grupo;
import com.webapp.repository.Grupos;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = Grupo.class)
public class GrupoConverter implements Converter {

	private Grupos grupos = CDIServiceLocator.getBean(Grupos.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Grupo retorno = null;

		if (value != null) {
			retorno = this.grupos.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((Grupo) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}