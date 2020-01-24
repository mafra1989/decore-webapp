package com.webapp.controller;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.webapp.model.Parcela;
import com.webapp.model.Simulacao;

@Named
@SessionScoped
public class SimularOperacaoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean result = false;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	@Inject
	private Simulacao simulacao;

	private String valorEmprestado;

	private Parcela parcela;

	private List<Parcela> parcelas = new ArrayList<Parcela>();

	public void calcular() {

		try {
			result = true;
			valorEmprestado = nf.format(Double.parseDouble(simulacao.getValorEmprestimo()));

			parcelas = new ArrayList<Parcela>();

			double juros = (((double) simulacao.getPercentualJuros()) / 100) + 1;
			double emprestimoComJuros = Double.parseDouble(simulacao.getValorEmprestimo()) * juros;

			boolean ultimaParcela = true;
			boolean loop = true;
			int numParcela = 0;

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(simulacao.getDataVencimento());
			int dia = calendar.get(Calendar.DAY_OF_MONTH);

			if (simulacao.getQuantidadeParcelas() > 1) {

				for (int i = 1; i <= simulacao.getQuantidadeParcelas(); i++) {

					if (loop == true) {

						if (Double.parseDouble(valorEmprestado.replace(".", "").replace(",", ".")) * juros > Double
								.parseDouble(simulacao.getPrimeiraParcela())) {

							if (i == 1) {
								emprestimoComJuros = (emprestimoComJuros
										- Double.parseDouble(simulacao.getPrimeiraParcela()));

								parcela = new Parcela();
								parcela.setParcela(i + "/" + simulacao.getQuantidadeParcelas());
								parcela.setValorParcela(nf.format(Double.parseDouble(simulacao.getPrimeiraParcela().replace(".", "").replace(",", "."))));
								parcela.setVencimentoParcela(simulacao.getDataVencimento());

								parcelas.add(parcela);

								numParcela = i;

							} else {

								calendar.add(Calendar.MONTH, 1);
								int numDias = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

								/* Tratamento para os dias 29, 30 e 31 */
								if (dia > numDias) {
									calendar.set(Calendar.DAY_OF_MONTH, numDias);
								} else {
									calendar.set(Calendar.DAY_OF_MONTH, dia);
								}
								
								System.out.println((emprestimoComJuros * juros)
										- Double.parseDouble(simulacao.getPrimeiraParcela()));

								if ((emprestimoComJuros * juros)
										- Double.parseDouble(simulacao.getPrimeiraParcela()) > 0) {

									if (i == simulacao.getQuantidadeParcelas()) {

										emprestimoComJuros = (emprestimoComJuros * juros);

										parcela = new Parcela();
										parcela.setParcela(i + "/" + simulacao.getQuantidadeParcelas());
										parcela.setValorParcela(nf.format(emprestimoComJuros));
										parcela.setVencimentoParcela(calendar.getTime());

										parcelas.add(parcela);

										numParcela = i;

									} else {

										emprestimoComJuros = (emprestimoComJuros * juros)
												- Double.parseDouble(simulacao.getPrimeiraParcela());

										parcela = new Parcela();
										parcela.setParcela(i + "/" + simulacao.getQuantidadeParcelas());
										parcela.setValorParcela(nf.format(Double.parseDouble(simulacao.getPrimeiraParcela().replace(".", "").replace(",", "."))));
										parcela.setVencimentoParcela(calendar.getTime());

										parcelas.add(parcela);

										numParcela = i;

									}

								} else if (ultimaParcela == true) {
									
									emprestimoComJuros = (emprestimoComJuros * juros);

									parcela = new Parcela();
									parcela.setParcela(i + "/" + simulacao.getQuantidadeParcelas());
									parcela.setValorParcela(nf.format(emprestimoComJuros));
									parcela.setVencimentoParcela(calendar.getTime());

									parcelas.add(parcela);

									ultimaParcela = false;

									numParcela = i;									
									
									loop = false;
								} 

							}
						} else {

							parcela = new Parcela();
							parcela.setParcela(i + "/1");
							parcela.setValorParcela(nf.format(emprestimoComJuros));
							parcela.setVencimentoParcela(simulacao.getDataVencimento());

							parcelas.add(parcela);
							
							simulacao.setQuantidadeParcelas(1);

							loop = false;
						}
					}
				}

			} else {

				parcela = new Parcela();
				parcela.setParcela("1/1");
				parcela.setValorParcela(nf.format(emprestimoComJuros));
				parcela.setVencimentoParcela(simulacao.getDataVencimento());

				parcelas.add(parcela);

			}

			if (numParcela < simulacao.getQuantidadeParcelas() && numParcela > 0) {
				simulacao.setQuantidadeParcelas(numParcela);
				for (int i = 0; i < parcelas.size(); i++) {
					parcelas.get(i).setParcela((i + 1) + "/" + parcelas.size());
				}
			}

			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("/simulacao/SimularOperacao.xhtml");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void recalcular() {
		try {
			result = false;
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("/simulacao/SimularOperacao.xhtml");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void novaSimulacao() {
		try {
			result = false;
			simulacao = new Simulacao();
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("/simulacao/SimularOperacao.xhtml");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getValorEmprestado() {
		return valorEmprestado;
	}

	public void setValorEmprestado(String valorEmprestado) {
		this.valorEmprestado = valorEmprestado;
	}

	public Simulacao getSimulacao() {
		return simulacao;
	}

	public void setSimulacao(Simulacao simulacao) {
		this.simulacao = simulacao;
	}

	public List<Parcela> getParcelas() {
		return parcelas;
	}
}
