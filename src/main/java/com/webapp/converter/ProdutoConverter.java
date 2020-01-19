package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.Produto;
import com.webapp.repository.Produtos;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = Produto.class)
public class ProdutoConverter implements Converter {

	private Produtos produtos = CDIServiceLocator.getBean(Produtos.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Produto retorno = null;

		if (value != null) {
			retorno = this.produtos.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((Produto) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}