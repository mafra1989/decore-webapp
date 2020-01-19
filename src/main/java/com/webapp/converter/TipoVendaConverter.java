package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.TipoVenda;
import com.webapp.repository.TiposVendas;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = TipoVenda.class)
public class TipoVendaConverter implements Converter {

	private TiposVendas tiposVendas = CDIServiceLocator.getBean(TiposVendas.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		TipoVenda retorno = null;

		if (value != null) {
			retorno = this.tiposVendas.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((TipoVenda) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}