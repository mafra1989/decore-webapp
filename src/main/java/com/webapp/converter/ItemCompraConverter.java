package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.ItemCompra;
import com.webapp.repository.ItensCompras;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = ItemCompra.class)
public class ItemCompraConverter implements Converter {

	private ItensCompras itensCompras = CDIServiceLocator.getBean(ItensCompras.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		ItemCompra retorno = null;

		if (value != null) {
			retorno = this.itensCompras.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((ItemCompra) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}