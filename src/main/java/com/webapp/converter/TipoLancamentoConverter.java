package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.TipoLancamento;
import com.webapp.repository.TiposDespesas;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = TipoLancamento.class)
public class TipoLancamentoConverter implements Converter {

	private TiposDespesas tiposDespesas = CDIServiceLocator.getBean(TiposDespesas.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		TipoLancamento retorno = null;

		if (value != null) {
			retorno = this.tiposDespesas.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((TipoLancamento) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}