package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.Empresa;
import com.webapp.repository.Empresas;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = Empresa.class)
public class EmpresaConverter implements Converter {

	private Empresas empresas = CDIServiceLocator.getBean(Empresas.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Empresa retorno = null;

		if (value != null) {
			retorno = empresas.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((Empresa) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}