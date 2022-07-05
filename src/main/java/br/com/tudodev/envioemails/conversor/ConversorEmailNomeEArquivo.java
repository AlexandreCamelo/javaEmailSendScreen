package br.com.tudodev.envioemails.conversor;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

import br.com.tudodev.envioemails.modelos.EmailNomeEArquivo;


@Named
@FacesConverter(value = "conversorEmailNomeArquivo", managed = true)
public class ConversorEmailNomeEArquivo implements Converter<EmailNomeEArquivo> {


	@Override
	public EmailNomeEArquivo getAsObject(FacesContext context, UIComponent component, String value) {
		if (value != null && !value.isEmpty()) {
			return (EmailNomeEArquivo) component.getAttributes().get(value);
		}
		return null;

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, EmailNomeEArquivo value) {
		if (value instanceof EmailNomeEArquivo) {
			EmailNomeEArquivo emailNomeEArquivo = (EmailNomeEArquivo) value;

			if (emailNomeEArquivo instanceof EmailNomeEArquivo && emailNomeEArquivo != null) {
				component.getAttributes().put(emailNomeEArquivo.getId().toString(), emailNomeEArquivo);
				return emailNomeEArquivo.getId().toString();
			}
		}
		return "";

	}

}