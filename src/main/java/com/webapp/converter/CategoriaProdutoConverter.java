package com.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.webapp.model.CategoriaProduto;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.util.cdi.CDIServiceLocator;

@FacesConverter(forClass = CategoriaProduto.class)
public class CategoriaProdutoConverter implements Converter {

	private CategoriasProdutos categoriasProdutos = CDIServiceLocator.getBean(CategoriasProdutos.class);;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		CategoriaProduto retorno = null;

		if (value != null) {
			retorno = this.categoriasProdutos.porId(new Long(value));
		}

		return retorno;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value != null) {
			Long codigo = ((CategoriaProduto) value).getId();
			String retorno = (codigo == null ? null : codigo.toString());

			return retorno;
		}

		return "";
	}
}