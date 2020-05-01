package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.Produto;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Contas;
import com.webapp.repository.Lancamentos;

@Named
@ViewScoped
public class RelatorioLancamentosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Lancamentos lancamentos;

	private BarChartModel mixedModel;

	private BarChartModel mixedModelPorDia;

	private BarChartModel mixedModelPorSemana;

	private BarChartModel mixedModelPorMes;

	private BarChartModel mixedModelPorAno;

	private List<CategoriaLancamento> todasCategoriasLancamento;

	@Inject
	private Contas contas;

	@Inject
	private CategoriasLancamentos categoriasLancamentos;

	@Inject
	private CategoriaLancamento categoriaPorDia;

	@Inject
	private CategoriaLancamento categoriaPorSemana;

	@Inject
	private CategoriaLancamento categoriaPorMes;

	@Inject
	private CategoriaLancamento categoriaPorAno;

	@Inject
	private Produto produto01;

	@Inject
	private Produto produto02;

	@Inject
	private Produto produto03;

	@Inject
	private Produto produto04;

	private List<Produto> produtosPorDia = new ArrayList<Produto>();

	private List<Produto> produtosPorSemana = new ArrayList<Produto>();

	private List<Produto> produtosPorMes = new ArrayList<Produto>();

	private List<Produto> produtosPorAno = new ArrayList<Produto>();

	private Date dateStart;

	private Date dateStop = new Date();

	private List<String> anos = new ArrayList<String>();

	private String ano01;

	private String ano02;

	private String ano03;

	private String ano04;

	private List<String> semanas = new ArrayList<String>();

	private String semana01;

	private String semana02;

	private List<String> meses = new ArrayList<String>();

	private String mes01;

	private String mes02;

	private DonutChartModel donutModel;
	
	
	private String[] categoriasPorDia;

	private String[] categoriasPorSemana;

	private String[] categoriasPorMes;

	private String[] categoriasPorAno;

	@PostConstruct
	public void init() {
		listarTodasCategoriasLancamentos();

		Calendar calendar = Calendar.getInstance();
		Calendar calendarTemp = Calendar.getInstance();

		int semana01 = calendarTemp.get(Calendar.WEEK_OF_YEAR) - 3;
		if (semana01 >= 4) {

			String semanaTemp = String.valueOf(semana01);
			if (semana01 < 10) {
				semanaTemp = "0" + semana01;
			}

			this.semana01 = "W" + semanaTemp;

			int semana02 = calendarTemp.get(Calendar.WEEK_OF_YEAR);
			semanaTemp = String.valueOf(semana02);
			if (semana02 < 10) {
				semanaTemp = "0" + semana02;
			}

			this.semana02 = "W" + semanaTemp;

		} else {
			this.semana01 = "W01";

			int semana02 = calendarTemp.get(Calendar.WEEK_OF_YEAR) + 3;
			String semanaTemp = String.valueOf(semana02);
			if (semana02 < 10) {
				semanaTemp = "0" + semana02;
			}
			this.semana02 = "W" + semanaTemp;
		}

		calendar.add(Calendar.DAY_OF_MONTH, -3);
		dateStart = calendar.getTime();

		createMixedModelPorDia();

		for (int i = 2018; i <= calendar.get(Calendar.YEAR); i++) {
			anos.add(String.valueOf(i));
		}

		ano01 = String.valueOf(calendar.get(Calendar.YEAR));

		for (int i = 1; i <= 52; i++) {

			semanas.add("W" + (i < 10 ? "0" + i : i));
		}

		meses.add("Janeiro");
		meses.add("Fevereiro");
		meses.add("Março");
		meses.add("Abril");
		meses.add("Maio");
		meses.add("Junho");
		meses.add("Julho");
		meses.add("Agosto");
		meses.add("Setembro");
		meses.add("Outubro");
		meses.add("Novembro");
		meses.add("Dezembro");

		createMixedModelPorSemana();

		ano02 = String.valueOf(calendarTemp.get(Calendar.YEAR));

		int mes = calendarTemp.get(Calendar.MONTH) + 1;
		if (mes >= 5) {
			mes01 = nameMes(mes - 4);
			mes02 = nameMes(mes);
		} else {
			mes01 = nameMes(1);
			mes02 = nameMes(mes);
		}

		createMixedModelPorMes();

		ano03 = "2019";

		ano04 = String.valueOf(calendarTemp.get(Calendar.YEAR));

		createMixedModelPorAno();

		createDonutModel(new ArrayList<Object[]>());
	}

	private void listarTodasCategoriasLancamentos() {
		todasCategoriasLancamento = categoriasLancamentos.todos();
	}

	public void changeCategoriaPorDia() {

		createMixedModelPorDia();
	}

	public void changeCategoriaPorSemana() {

		createMixedModelPorSemana();
	}

	public void changeCategoriaPorMes() {

		createMixedModelPorMes();
	}

	public void changeProdutoPorMes() {

		createMixedModelPorMes();
	}

	public void changeCategoriaPorAno() {

		createMixedModelPorAno();
	}

	public void createMixedModelPorDia() {
		mixedModelPorDia = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);

		List<Object[]> result = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		if (calendarStart.before(calendarStop)) {

			Calendar calendarStartTemp = (Calendar) calendarStart.clone();

			do {
				Calendar calendarStopTemp = (Calendar) calendarStartTemp.clone();
				calendarStopTemp.add(Calendar.DAY_OF_MONTH, 1);

				List<Object[]> resultTemp = contas.totalLancamentosPorData(calendarStartTemp, calendarStopTemp);

				if (resultTemp.size() == 0) {

					Object[] object = new Object[6];
					object[0] = calendarStartTemp.get(Calendar.DAY_OF_MONTH);
					if (calendarStartTemp.get(Calendar.DAY_OF_MONTH) < 10) {
						object[0] = "0" + calendarStartTemp.get(Calendar.DAY_OF_MONTH);
					}

					object[1] = calendarStartTemp.get(Calendar.MONTH) + 1;
					if (calendarStartTemp.get(Calendar.MONTH) + 1 < 10) {
						object[1] = "0" + (calendarStartTemp.get(Calendar.MONTH) + 1);
					}

					object[2] = calendarStartTemp.get(Calendar.YEAR);

					object[3] = "";
					object[4] = "";
					object[5] = 0;

					result.add(object);
				} else {
					double valor = 0;
					Object[] objectTemp = null;
					String tipo = "";
					List<String> tipos = new ArrayList<>();

					for (Object[] object : resultTemp) {

						if (!tipo.equals(object[4].toString()) && !tipo.equals("")) {
							objectTemp[5] = valor;
							result.add(objectTemp);
							valor = 0;
						}

						if (!tipo.equals(object[4].toString())) {
							objectTemp = new Object[6];
							tipo = object[4].toString();
							tipos.add(tipo);
						}

						objectTemp[0] = object[0];
						objectTemp[1] = object[1];
						objectTemp[2] = object[2];
						objectTemp[4] = object[4];

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[3].toString()));
						if (lancamento != null) {
							if (categoriasPorDia != null || categoriasPorDia.length > 0) {
								for (String categoria : categoriasPorDia) {
									if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
										valor += new BigDecimal(object[5].toString()).doubleValue();
									}
								}								
							} else {
								valor += new BigDecimal(object[5].toString()).doubleValue();
							}
						}
					}

					if (!tipos.contains("DEBITO")) {
						Object[] object = new Object[6];
						object[0] = calendarStartTemp.get(Calendar.DAY_OF_MONTH);
						if (calendarStartTemp.get(Calendar.DAY_OF_MONTH) < 10) {
							object[0] = "0" + calendarStartTemp.get(Calendar.DAY_OF_MONTH);
						}

						object[1] = calendarStartTemp.get(Calendar.MONTH) + 1;
						if (calendarStartTemp.get(Calendar.MONTH) + 1 < 10) {
							object[1] = "0" + (calendarStartTemp.get(Calendar.MONTH) + 1);
						}

						object[2] = calendarStartTemp.get(Calendar.YEAR);

						object[3] = "";
						object[4] = "DEBITO";
						object[5] = 0;

						result.add(object);
					}

					if (!tipos.contains("CREDITO")) {
						Object[] object = new Object[6];
						object[0] = calendarStartTemp.get(Calendar.DAY_OF_MONTH);
						if (calendarStartTemp.get(Calendar.DAY_OF_MONTH) < 10) {
							object[0] = "0" + calendarStartTemp.get(Calendar.DAY_OF_MONTH);
						}

						object[1] = calendarStartTemp.get(Calendar.MONTH) + 1;
						if (calendarStartTemp.get(Calendar.MONTH) + 1 < 10) {
							object[1] = "0" + (calendarStartTemp.get(Calendar.MONTH) + 1);
						}

						object[2] = calendarStartTemp.get(Calendar.YEAR);

						object[3] = "";
						object[4] = "CREDITO";
						object[5] = 0;

						result.add(object);
					}

					objectTemp[5] = valor;
					result.add(objectTemp);
				}

				calendarStartTemp.add(Calendar.DAY_OF_MONTH, 1);

			} while (calendarStartTemp.before(calendarStop));
		}
		
		boolean debito = false, credito = false;
		String date = "";
		for (Object[] object : result) {

			if (!date.equals(object[0] + "/" + object[1])) {
				date = object[0] + "/" + object[1];
				labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
			}

			if (object[4].toString().equals("DEBITO")) {
				values.add((Number) object[5]);
				debito = true;

			} else if (object[4].toString().equals("CREDITO")) {
				values2.add((Number) object[5]);
				credito = true;

			} else if (object[4].toString().equals("")) {
				values.add((Number) object[5]);
				values2.add((Number) object[5]);
			}
		}

		if (categoriasPorDia == null || categoriasPorDia.length == 0
				|| (!debito && !credito) || debito) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriasPorDia == null || categoriasPorDia.length == 0
				|| (!debito && !credito) || credito) {
			dataSet2.setData(values2);
			dataSet2.setLabel("Crédito");
			dataSet2.setBorderColor("rgba(255, 205, 86)");
			dataSet2.setBackgroundColor("rgba(255, 205, 86)");
			data.addChartDataSet(dataSet2);
		}

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.setLabels(labels);

		mixedModelPorDia.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();
		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);
		mixedModelPorDia.setOptions(options);

		mixedModelPorDia.setExtender("percentExtender2");
	}

	public void createMixedModelPorSemana() {
		mixedModelPorSemana = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();
		if (Integer.parseInt(semana01.replace("W", "")) <= Integer.parseInt(semana02.replace("W", ""))) {

			for (int i = Integer.parseInt(semana01.replace("W", "")); i <= Integer
					.parseInt(semana02.replace("W", "")); i++) {

				String semana01 = "W";
				if (i < 10) {
					semana01 += "0" + i;
				} else {
					semana01 += i;
				}

				System.out.println(semana01);

				List<Object[]> resultTemp = contas.totalLancamentosPorSemana(ano01, semana01, semana01, true);

				if (resultTemp.size() == 0) {

					Object[] object = new Object[5];

					object[0] = i;
					object[1] = ano01;

					object[2] = "";
					object[3] = "";

					object[4] = 0;

					result.add(object);
				} else {
					double valor = 0;
					Object[] objectTemp = null;
					String tipo = "";
					List<String> tipos = new ArrayList<>();

					for (Object[] object : resultTemp) {

						if (!tipo.equals(object[3].toString()) && !tipo.equals("")) {
							objectTemp[4] = valor;
							result.add(objectTemp);
							valor = 0;
						}

						if (!tipo.equals(object[3].toString())) {
							objectTemp = new Object[5];
							tipo = object[3].toString();
							tipos.add(tipo);
						}

						objectTemp[0] = object[0];
						objectTemp[1] = object[1];
						objectTemp[2] = object[2];
						objectTemp[3] = object[3];

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[2].toString()));
						if (lancamento != null) {
							if (categoriaPorSemana != null && categoriaPorSemana.getId() != null) {
								if (lancamento.getCategoriaLancamento().getId().intValue() == categoriaPorSemana.getId()
										.intValue()) {
									valor += new BigDecimal(object[4].toString()).doubleValue();
								}
							} else {
								valor += new BigDecimal(object[4].toString()).doubleValue();
							}
						}
					}

					if (!tipos.contains("DEBITO")) {
						Object[] object = new Object[5];

						object[0] = i;
						object[1] = ano01;

						object[2] = "";
						object[3] = "DEBITO";

						object[4] = 0;

						result.add(object);
					}

					if (!tipos.contains("CREDITO")) {
						Object[] object = new Object[5];

						object[0] = i;
						object[1] = ano01;

						object[2] = "";
						object[3] = "CREDITO";

						object[4] = 0;

						result.add(object);
					}

					objectTemp[4] = valor;
					result.add(objectTemp);
				}
			}

			System.out.println("result.size(): " + result.size());
		}

		String week = "";
		for (Object[] object : result) {

			int semana = Integer.parseInt(object[0].toString());
			String semanaTemp = String.valueOf(semana);
			if (semana < 10) {
				semanaTemp = "0" + semana;
			}
			String weekTemp = "W" + semanaTemp;

			if (!week.equals(weekTemp)) {
				labels.add(weekTemp);
				week = weekTemp;
			}

			if (object[3].toString().equals("DEBITO")) {
				values.add((Number) object[4]);

			} else if (object[3].toString().equals("CREDITO")) {
				values2.add((Number) object[4]);

			} else if (object[3].toString().equals("")) {
				values.add((Number) object[4]);
				values2.add((Number) object[4]);
			}
		}

		if (categoriaPorSemana == null || categoriaPorSemana.getId() == null
				|| categoriaPorSemana.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriaPorSemana == null || categoriaPorSemana.getId() == null
				|| categoriaPorSemana.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
			dataSet2.setData(values2);
			dataSet2.setLabel("Crédito");
			dataSet2.setBorderColor("rgba(255, 205, 86)");
			dataSet2.setBackgroundColor("rgba(255, 205, 86)");
			data.addChartDataSet(dataSet2);
		}

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.setLabels(labels);

		mixedModelPorSemana.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();
		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);
		mixedModelPorSemana.setOptions(options);

		mixedModelPorSemana.setExtender("percentExtender2");
	}

	public String nameMes(int mes) {

		switch (mes) {
		case 1:
			return "Janeiro";
		case 2:
			return "Fevereiro";
		case 3:
			return "Março";
		case 4:
			return "Abril";
		case 5:
			return "Maio";
		case 6:
			return "Junho";
		case 7:
			return "Julho";
		case 8:
			return "Agosto";
		case 9:
			return "Setembro";
		case 10:
			return "Outubro";
		case 11:
			return "Novembro";
		case 12:
			return "Dezembro";
		}

		return "";
	}

	public String numberMes(String mes) {

		switch (mes) {
		case "Janeiro":
			return "01";
		case "Fevereiro":
			return "02";
		case "Março":
			return "03";
		case "Abril":
			return "04";
		case "Maio":
			return "05";
		case "Junho":
			return "06";
		case "Julho":
			return "07";
		case "Agosto":
			return "08";
		case "Setembro":
			return "09";
		case "Outubro":
			return "10";
		case "Novembro":
			return "11";
		case "Dezembro":
			return "12";
		}

		return "";
	}

	public void createMixedModelPorMes() {
		mixedModelPorMes = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		if (Integer.parseInt(numberMes(mes01)) <= Integer.parseInt(numberMes(mes02))) {

			for (int i = Integer.parseInt(numberMes(mes01)); i <= Integer.parseInt(numberMes(mes02)); i++) {
				String mes01 = String.valueOf(i);
				if (i < 10) {
					mes01 = "0" + i;
				}

				List<Object[]> resultTemp = contas.totalDespesasPorMes(ano02, mes01, mes01, true);

				if (resultTemp.size() == 0) {

					Object[] object = new Object[5];

					object[0] = i;
					object[1] = ano01;

					object[2] = "";
					object[3] = "";

					object[4] = 0;

					result.add(object);
				} else {
					double valor = 0;
					Object[] objectTemp = null;
					String tipo = "";
					List<String> tipos = new ArrayList<>();

					for (Object[] object : resultTemp) {

						if (!tipo.equals(object[3].toString()) && !tipo.equals("")) {
							objectTemp[4] = valor;
							result.add(objectTemp);
							valor = 0;
						}

						if (!tipo.equals(object[3].toString())) {
							objectTemp = new Object[5];
							tipo = object[3].toString();
							tipos.add(tipo);
						}

						objectTemp[0] = object[0];
						objectTemp[1] = object[1];
						objectTemp[2] = object[2];
						objectTemp[3] = object[3];

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[2].toString()));
						if (lancamento != null) {
							if (categoriaPorMes != null && categoriaPorMes.getId() != null) {
								if (lancamento.getCategoriaLancamento().getId().intValue() == categoriaPorMes.getId()
										.intValue()) {
									valor += new BigDecimal(object[4].toString()).doubleValue();
								}
							} else {
								valor += new BigDecimal(object[4].toString()).doubleValue();
							}
						}
					}

					if (!tipos.contains("DEBITO")) {
						Object[] object = new Object[5];

						object[0] = i;
						object[1] = ano01;

						object[2] = "";
						object[3] = "DEBITO";

						object[4] = 0;

						result.add(object);
					}

					if (!tipos.contains("CREDITO")) {
						Object[] object = new Object[5];

						object[0] = i;
						object[1] = ano01;

						object[2] = "";
						object[3] = "CREDITO";

						object[4] = 0;

						result.add(object);
					}

					objectTemp[4] = valor;
					result.add(objectTemp);
				}
			}

		}
		
		String month = "";
		for (Object[] object : result) {

			int mes = Integer.parseInt(object[0].toString());
			String monthTemp = String.valueOf(mes);
			if (mes < 10) {
				monthTemp = "0" + mes;
			}

			if (!month.equals(monthTemp)) {
				labels.add(nameMes(Integer.parseInt(object[0].toString())));
				month = monthTemp;
			}

			if (object[3].toString().equals("DEBITO")) {
				values.add((Number) object[4]);

			} else if (object[3].toString().equals("CREDITO")) {
				values2.add((Number) object[4]);

			} else if (object[3].toString().equals("")) {
				values.add((Number) object[4]);
				values2.add((Number) object[4]);
			}
		}

		if (categoriaPorMes == null || categoriaPorMes.getId() == null
				|| categoriaPorMes.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriaPorMes == null || categoriaPorMes.getId() == null
				|| categoriaPorMes.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
			dataSet2.setData(values2);
			dataSet2.setLabel("Crédito");
			dataSet2.setBorderColor("rgba(255, 205, 86)");
			dataSet2.setBackgroundColor("rgba(255, 205, 86)");
			data.addChartDataSet(dataSet2);
		}

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		// data.addChartDataSet(dataSet);
		// data.addChartDataSet(dataSet2);

		data.setLabels(labels);

		mixedModelPorMes.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();
		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);
		mixedModelPorMes.setOptions(options);

		mixedModelPorMes.setExtender("percentExtender2");
	}

	public void createMixedModelPorAno() {
		mixedModelPorAno = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();
		
		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();
		
		List<String> labels = new ArrayList<>();
		
		List<Object[]> result = new ArrayList<>();
		if(Integer.parseInt(ano03) <= Integer.parseInt(ano04)) {
			
			for (int i = Integer.parseInt(ano03); i <= Integer.parseInt(ano04); i++) {
				
				String ano03 = String.valueOf(i);
				
				List<Object[]> resultTemp = contas.totalDespesasPorAno(ano03, ano03, true);
				
				if (resultTemp.size() == 0) {

					Object[] object = new Object[4];

					object[0] = i;
					object[1] = "";
					object[2] = "";
					object[3] = 0;

					result.add(object);
				} else {
					double valor = 0;
					Object[] objectTemp = null;
					String tipo = "";
					List<String> tipos = new ArrayList<>();

					for (Object[] object : resultTemp) {

						if (!tipo.equals(object[2].toString()) && !tipo.equals("")) {
							objectTemp[3] = valor;
							result.add(objectTemp);
							valor = 0;
						}

						if (!tipo.equals(object[2].toString())) {
							objectTemp = new Object[4];
							tipo = object[2].toString();
							tipos.add(tipo);
						}

						objectTemp[0] = object[0];
						objectTemp[1] = object[1];
						objectTemp[2] = object[2];

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[1].toString()));
						if (lancamento != null) {
							if (categoriaPorAno != null && categoriaPorAno.getId() != null) {
								if (lancamento.getCategoriaLancamento().getId().intValue() == categoriaPorAno.getId()
										.intValue()) {
									valor += new BigDecimal(object[3].toString()).doubleValue();
								}
							} else {
								valor += new BigDecimal(object[3].toString()).doubleValue();
							}
						}
					}

					if (!tipos.contains("DEBITO")) {
						Object[] object = new Object[4];

						object[0] = i;
						object[1] = "";
						object[2] = "DEBITO";
						object[3] = 0;

						result.add(object);
					}

					if (!tipos.contains("CREDITO")) {
						Object[] object = new Object[4];

						object[0] = i;
						object[1] = "";
						object[2] = "CREDITO";
						object[3] = 0;

						result.add(object);
					}

					objectTemp[3] = valor;
					result.add(objectTemp);
				}
			}		
		}

		
		String year = "";
		for (Object[] object : result) {

			int ano = Integer.parseInt(object[0].toString());
			String yearTemp = String.valueOf(ano);
			if (ano < 10) {
				yearTemp = "0" + ano;
			}

			if (!year.equals(yearTemp)) {
				labels.add(object[0].toString());
				year = yearTemp;
			}

			if (object[2].toString().equals("DEBITO")) {
				values.add((Number) object[3]);

			} else if (object[2].toString().equals("CREDITO")) {
				values2.add((Number) object[3]);

			} else if (object[2].toString().equals("")) {
				values.add((Number) object[3]);
				values2.add((Number) object[3]);
			}
		}

		
		if (categoriaPorAno == null || categoriaPorAno.getId() == null
				|| categoriaPorAno.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriaPorAno == null || categoriaPorAno.getId() == null
				|| categoriaPorAno.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
			dataSet2.setData(values2);
			dataSet2.setLabel("Crédito");
			dataSet2.setBorderColor("rgba(255, 205, 86)");
			dataSet2.setBackgroundColor("rgba(255, 205, 86)");
			data.addChartDataSet(dataSet2);
		}

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		// data.addChartDataSet(dataSet);
		// data.addChartDataSet(dataSet2);

		data.setLabels(labels);

		mixedModelPorAno.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();
		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);
		mixedModelPorAno.setOptions(options);

		mixedModelPorAno.setExtender("percentExtender4");
	}

	public void prepareDonutModelPorDia() {

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);

		List<Object[]> result = lancamentos.totalDespesasPorData(calendarStart, calendarStop, categoriaPorDia, false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorSemana() {

		List<Object[]> result = lancamentos.totalDespesasPorSemana(ano01, semana01, semana02, categoriaPorSemana,
				false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorMes() {

		List<Object[]> result = lancamentos.totalDespesasPorMes(ano02, numberMes(mes01), numberMes(mes02),
				categoriaPorMes, false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorAno() {

		List<Object[]> result = lancamentos.totalDespesasPorAno(ano03, ano04, categoriaPorAno, false);

		createDonutModel(result);
	}

	public void createDonutModel(List<Object[]> result) {
		donutModel = new DonutChartModel();
		ChartData data = new ChartData();

		DonutChartDataSet dataSet = new DonutChartDataSet();
		List<Number> values = new ArrayList<>();

		for (Object[] object : result) {
			values.add((Number) object[1]);
		}

		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		bgColors.add("rgb(255, 99, 132)");
		bgColors.add("rgb(255, 159, 64)");
		bgColors.add("rgb(255, 205, 86)");
		bgColors.add("rgb(75, 192, 192)");
		bgColors.add("rgb(54, 162, 235)");
		bgColors.add("rgb(201, 203, 207)");
		dataSet.setBackgroundColor(bgColors);

		data.addChartDataSet(dataSet);
		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {
			labels.add((String) object[0]);
		}
		data.setLabels(labels);

		donutModel.setData(data);

		donutModel.setExtender("percentExtender3");
	}

	public BarChartModel getMixedModel() {
		return mixedModel;
	}

	public void setMixedModel(BarChartModel mixedModel) {
		this.mixedModel = mixedModel;
	}

	public List<CategoriaLancamento> getTodasCategoriasLancamento() {
		return todasCategoriasLancamento;
	}

	public CategoriaLancamento getCategoriaPorDia() {
		return categoriaPorDia;
	}

	public void setCategoriaPorDia(CategoriaLancamento categoriaPorDia) {
		this.categoriaPorDia = categoriaPorDia;
	}

	public CategoriaLancamento getCategoriaPorSemana() {
		return categoriaPorSemana;
	}

	public void setCategoriaPorSemana(CategoriaLancamento categoriaPorSemana) {
		this.categoriaPorSemana = categoriaPorSemana;
	}

	public CategoriaLancamento getCategoriaPorMes() {
		return categoriaPorMes;
	}

	public void setCategoriaPorMes(CategoriaLancamento categoriaPorMes) {
		this.categoriaPorMes = categoriaPorMes;
	}

	public CategoriaLancamento getCategoriaPorAno() {
		return categoriaPorAno;
	}

	public void setCategoriaPorAno(CategoriaLancamento categoriaPorAno) {
		this.categoriaPorAno = categoriaPorAno;
	}

	public List<Produto> getProdutosPorDia() {
		return produtosPorDia;
	}

	public List<Produto> getProdutosPorSemana() {
		return produtosPorSemana;
	}

	public List<Produto> getProdutosPorMes() {
		return produtosPorMes;
	}

	public List<Produto> getProdutosPorAno() {
		return produtosPorAno;
	}

	public Produto getProduto01() {
		return produto01;
	}

	public void setProduto01(Produto produto01) {
		this.produto01 = produto01;
	}

	public Produto getProduto02() {
		return produto02;
	}

	public void setProduto02(Produto produto02) {
		this.produto02 = produto02;
	}

	public Produto getProduto03() {
		return produto03;
	}

	public void setProduto03(Produto produto03) {
		this.produto03 = produto03;
	}

	public Produto getProduto04() {
		return produto04;
	}

	public void setProduto04(Produto produto04) {
		this.produto04 = produto04;
	}

	public Date getDateStart() {
		return dateStart;
	}

	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}

	public Date getDateStop() {
		return dateStop;
	}

	public void setDateStop(Date dateStop) {
		this.dateStop = dateStop;
	}

	public BarChartModel getMixedModelPorDia() {
		return mixedModelPorDia;
	}

	public BarChartModel getMixedModelPorSemana() {
		return mixedModelPorSemana;
	}

	public BarChartModel getMixedModelPorMes() {
		return mixedModelPorMes;
	}

	public BarChartModel getMixedModelPorAno() {
		return mixedModelPorAno;
	}

	public List<String> getAnos() {
		return anos;
	}

	public String getAno01() {
		return ano01;
	}

	public void setAno01(String ano01) {
		this.ano01 = ano01;
	}

	public String getAno02() {
		return ano02;
	}

	public void setAno02(String ano02) {
		this.ano02 = ano02;
	}

	public String getAno03() {
		return ano03;
	}

	public void setAno03(String ano03) {
		this.ano03 = ano03;
	}

	public String getAno04() {
		return ano04;
	}

	public void setAno04(String ano04) {
		this.ano04 = ano04;
	}

	public String getSemana01() {
		return semana01;
	}

	public void setSemana01(String semana) {
		this.semana01 = semana;
	}

	public String getSemana02() {
		return semana02;
	}

	public void setSemana02(String semana02) {
		this.semana02 = semana02;
	}

	public List<String> getSemanas() {
		return semanas;
	}

	public List<String> getMeses() {
		return meses;
	}

	public String getMes01() {
		return mes01;
	}

	public void setMes01(String mes01) {
		this.mes01 = mes01;
	}

	public String getMes02() {
		return mes02;
	}

	public void setMes02(String mes02) {
		this.mes02 = mes02;
	}

	public DonutChartModel getDonutModel() {
		return donutModel;
	}

	public void setDonutModel(DonutChartModel donutModel) {
		this.donutModel = donutModel;
	}

	public String[] getCategoriasPorDia() {
		return categoriasPorDia;
	}

	public void setCategoriasPorDia(String[] categoriasPorDia) {
		this.categoriasPorDia = categoriasPorDia;
	}

	public String[] getCategoriasPorSemana() {
		return categoriasPorSemana;
	}

	public void setCategoriasPorSemana(String[] categoriasPorSemana) {
		this.categoriasPorSemana = categoriasPorSemana;
	}

	public String[] getCategoriasPorMes() {
		return categoriasPorMes;
	}

	public void setCategoriasPorMes(String[] categoriasPorMes) {
		this.categoriasPorMes = categoriasPorMes;
	}

	public String[] getCategoriasPorAno() {
		return categoriasPorAno;
	}

	public void setCategoriasPorAno(String[] categoriasPorAno) {
		this.categoriasPorAno = categoriasPorAno;
	}
}