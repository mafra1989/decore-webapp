package com.webapp.controller;

import java.io.Serializable;
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
import org.primefaces.model.charts.line.LineChartDataSet;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Produto;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Contas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Vendas;

@Named
@ViewScoped
public class RelatorioLucrosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Vendas vendas;

	@Inject
	private Produtos produtos;

	@Inject
	private Contas contas;

	private BarChartModel mixedModel;

	private BarChartModel mixedModelPorDia;

	private BarChartModel mixedModelPorSemana;

	private BarChartModel mixedModelPorMes;

	private BarChartModel mixedModelPorAno;

	private List<CategoriaProduto> todasCategoriasProduto;

	@Inject
	private CategoriasProdutos categoriasProdutos;

	@Inject
	private CategoriaProduto categoriaPorDia;

	@Inject
	private CategoriaProduto categoriaPorSemana;

	@Inject
	private CategoriaProduto categoriaPorMes;

	@Inject
	private CategoriaProduto categoriaPorAno;

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

	private Boolean lucroPorLote = false;

	@PostConstruct
	public void init() {
		listarTodasCategoriasProdutos();

		Calendar calendar = Calendar.getInstance();
		Calendar calendarTemp = Calendar.getInstance();

		int semana01 = calendarTemp.get(Calendar.WEEK_OF_YEAR);
		if (semana01 >= 5) {

			String semanaTemp = String.valueOf(semana01);
			if (semana01 < 10) {
				semanaTemp = "0" + semana01;
			}

			this.semana01 = "W" + semanaTemp;

			calendar.add(Calendar.DAY_OF_MONTH, -5);

			int semana02 = calendarTemp.get(Calendar.WEEK_OF_YEAR);
			semanaTemp = String.valueOf(semana02);
			if (semana02 < 10) {
				semanaTemp = "0" + semana02;
			}

			this.semana02 = "W" + semanaTemp;

		} else {
			this.semana01 = "W01";

			int semana02 = calendarTemp.get(Calendar.WEEK_OF_YEAR);
			String semanaTemp = String.valueOf(semana02);
			if (semana02 < 10) {
				semanaTemp = "0" + semana02;
			}
			this.semana02 = "W" + semanaTemp;
		}

		calendar.add(Calendar.DAY_OF_MONTH, -5);
		dateStart = calendar.getTime();

		createMixedModelPorDia();

		for (int i = 2019; i <= calendar.get(Calendar.YEAR); i++) {
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

		createDonutModel(new ArrayList<Object[]>());

		ano03 = "2019";

		ano04 = String.valueOf(calendarTemp.get(Calendar.YEAR));

		createMixedModelPorAno();
	}

	private void listarTodasCategoriasProdutos() {
		todasCategoriasProduto = categoriasProdutos.todos();
	}

	public void changeCategoriaPorDia() {
		produtosPorDia = new ArrayList<Produto>();
		produto01 = null;
		if (categoriaPorDia != null) {
			produtosPorDia = produtos.porCategoria_(categoriaPorDia);
		}

		createMixedModelPorDia();
	}

	public void changeProdutoPorDia() {

		createMixedModelPorDia();
	}

	public void changeCategoriaPorSemana() {
		produtosPorSemana = new ArrayList<Produto>();
		produto02 = null;
		if (categoriaPorSemana != null) {
			produtosPorSemana = produtos.porCategoria_(categoriaPorSemana);
		}

		createMixedModelPorSemana();
	}

	public void changeProdutoPorSemana() {

		createMixedModelPorSemana();
	}

	public void changeCategoriaPorMes() {
		produtosPorMes = new ArrayList<Produto>();
		produto03 = null;
		if (categoriaPorMes != null) {
			produtosPorMes = produtos.porCategoria_(categoriaPorMes);
		} else {
			lucroPorLote = false;
		}

		createMixedModelPorMes();
	}

	public void changeProdutoPorMes() {

		createMixedModelPorMes();
	}

	public void changeCategoriaPorAno() {
		produtosPorAno = new ArrayList<Produto>();
		produto04 = null;
		if (categoriaPorAno != null) {
			produtosPorAno = produtos.porCategoria_(categoriaPorAno);
		}

		createMixedModelPorAno();
	}

	public void changeProdutoPorAno() {

		createMixedModelPorAno();
	}

	public void createMixedModelPorDia() {
		mixedModelPorDia = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);

		List<Object[]> result = new ArrayList<>();

		if (calendarStart.before(calendarStop)) {

			Calendar calendarStartTemp = (Calendar) calendarStart.clone();

			do {
				Calendar calendarStopTemp = (Calendar) calendarStartTemp.clone();
				calendarStopTemp.add(Calendar.DAY_OF_MONTH, 1);

				List<Object[]> resultTemp = vendas.totalLucrosPorData(calendarStartTemp, calendarStopTemp,
						categoriaPorDia, produto01, true);

				System.out.println("Data: " + calendarStartTemp.getTime() + " - " + resultTemp.size());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[5];
					object[0] = calendarStartTemp.get(Calendar.DAY_OF_MONTH);
					if (calendarStartTemp.get(Calendar.DAY_OF_MONTH) < 10) {
						object[0] = "0" + calendarStartTemp.get(Calendar.DAY_OF_MONTH);
					}

					object[1] = calendarStartTemp.get(Calendar.MONTH) + 1;
					if (calendarStartTemp.get(Calendar.MONTH) + 1 < 10) {
						object[1] = "0" + (calendarStartTemp.get(Calendar.MONTH) + 1);
					}

					object[2] = calendarStartTemp.get(Calendar.YEAR);

					object[3] = 0;
					object[4] = 0;

					System.out.println("totalComprasPorData: " + object[4]);

					result.add(object);
				} else {
					for (Object[] object : resultTemp) {
						result.add(object);
					}
				}

				calendarStartTemp.add(Calendar.DAY_OF_MONTH, 1);

				System.out.println("Data calendarStartTemp: " + calendarStartTemp.getTime());
				System.out.println("Data calendarStop: " + calendarStop.getTime());

				System.out.println("Data While: " + calendarStartTemp.before(calendarStop));

			} while (calendarStartTemp.before(calendarStop));
		}

		System.out.println("result.size(): " + result.size());

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		for (Object[] object : result) {
			Double totalDeVendas = ((Number) object[3]).doubleValue();

			System.out.println("totalComprasPorData: " + object[4]);
			Double totalCompras = ((Number) object[4]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorData(Long.parseLong(object[0].toString()),
			 * Long.parseLong(object[1].toString()), Long.parseLong(object[2].toString()),
			 * categoriaPorDia, produto01, true) .doubleValue();
			 */

			if (categoriaPorDia == null || categoriaPorDia.getId() == null) {

				totalDeReceitas = contas
						.totalDeReceitasPorDia(Long.parseLong(object[0].toString()),
								Long.parseLong(object[1].toString()), Long.parseLong(object[2].toString()))
						.doubleValue();

				System.out.println("Total vendas: " + totalDeVendas);
				System.out.println("Receitas: " + totalDeReceitas);

				totalDeDespesas = contas
						.totalDespesasPorData(Long.parseLong(object[0].toString()),
								Long.parseLong(object[1].toString()), Long.parseLong(object[2].toString()))
						.doubleValue();

				System.out.println("Despesas: " + totalDeDespesas);

				if (totalDeReceitas > 0 || totalDeDespesas > 0 || totalDeVendas > 0 || totalCompras > 0) {

					values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

					if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) == 0 && totalDeDespesas > 0) {
						values2.add(-100.0);
						System.out.println("Valor Percentual: -100.0");
					} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) > 0 && totalDeDespesas == 0) {
						values2.add(100.0);
						System.out.println("Valor Percentual: 100.0");
					} else {
						values2.add((((totalDeVendas + totalDeReceitas) - totalDeDespesas)
								/ (totalCompras + totalDeDespesas)) * 100);
						System.out.println("Valor Percentual: " + (((totalDeVendas + totalDeReceitas) - totalDeDespesas)
								/ (totalCompras + totalDeDespesas)) * 100);
					}

					System.out.println("Percentual: "
							+ (((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);

					labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
				}

			} else {
				if (totalDeVendas > 0 || totalCompras > 0) {
					values.add(totalDeVendas/* - totalDeCompras */);
					values2.add((totalDeVendas / totalCompras) * 100);

					labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
				}
			}

			System.out.println(object[3]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Percentual");
		dataSet2.setYaxisID("right-y-axis");
		// dataSet2.setFill(false);
		dataSet2.setBorderColor("rgba(255, 159, 64");

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		data.setLabels(labels);

		mixedModelPorDia.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

		CartesianLinearAxes linearAxes2 = new CartesianLinearAxes();
		linearAxes2.setId("right-y-axis");
		linearAxes2.setPosition("right");

		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		CartesianLinearTicks ticks2 = new CartesianLinearTicks();
		ticks2.setBeginAtZero(true);
		linearAxes2.setTicks(ticks2);

		cScales.addYAxesData(linearAxes);
		cScales.addYAxesData(linearAxes2);
		options.setScales(cScales);

		mixedModelPorDia.setOptions(options);

		mixedModelPorDia.setExtender("percentExtender2");
	}

	public void createMixedModelPorSemana() {
		mixedModelPorSemana = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();
		if (Integer.parseInt(semana01.replace("W", "")) <= Integer.parseInt(semana02.replace("W", ""))) {

			for (int i = Integer.parseInt(semana01.replace("W", "")); i <= Integer
					.parseInt(semana02.replace("W", "")); i++) {

				semana01 = "W";
				if (i < 10) {
					semana01 += "0" + i;
				} else {
					semana01 += i;
				}

				List<Object[]> resultTemp = vendas.totalLucrosPorSemana(ano01, semana01, semana01, categoriaPorSemana,
						produto02, true);

				if (resultTemp.size() == 0) {

					Object[] object = new Object[4];

					object[0] = i;
					object[1] = ano01;

					object[2] = 0;
					object[3] = 0;

					result.add(object);
				} else {
					for (Object[] object : resultTemp) {
						result.add(object);
					}
				}
			}
		}

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeVendas = ((Number) object[2]).doubleValue();
			Double totalCompras = ((Number) object[3]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorSemana(Long.parseLong(object[0].toString()),
			 * Long.parseLong(object[1].toString()), categoriaPorSemana, produto02, true)
			 * .doubleValue();
			 */

			if (categoriaPorSemana == null || categoriaPorSemana.getId() == null) {

				totalDeReceitas = contas.totalDeReceitasPorSemana(Long.parseLong(object[0].toString()),
						Long.parseLong(object[1].toString())).doubleValue();

				totalDeDespesas = contas.totalDespesasPorSemana(Long.parseLong(object[0].toString()),
						Long.parseLong(object[1].toString())).doubleValue();

				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				values2.add((((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
						* 100);
			} else {

				values.add(totalDeVendas/* - totalDeCompras */);
				values2.add((totalDeVendas / totalCompras) * 100);
			}
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Percentual");
		dataSet2.setYaxisID("right-y-axis");
		// dataSet2.setFill(false);
		dataSet2.setBorderColor("rgba(255, 159, 64");

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {
			long semana = (long) object[0];
			String semanaTemp = String.valueOf(semana);
			if (semana < 10) {
				semanaTemp = "0" + semana;
			}
			labels.add("W" + semanaTemp);
		}

		data.setLabels(labels);

		mixedModelPorSemana.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

		CartesianLinearAxes linearAxes2 = new CartesianLinearAxes();
		linearAxes2.setId("right-y-axis");
		linearAxes2.setPosition("right");

		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		CartesianLinearTicks ticks2 = new CartesianLinearTicks();
		ticks2.setBeginAtZero(true);
		linearAxes2.setTicks(ticks2);

		cScales.addYAxesData(linearAxes);
		cScales.addYAxesData(linearAxes2);
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

		List<Object[]> result = new ArrayList<>();
		if (lucroPorLote != true) {
			result = vendas.totalLucrosPorMes(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes, produto03,
					true);
		} else {
			result = vendas.totalLucrosPorLote(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes, produto03,
					true);
		}

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeVendas = ((Number) object[2]).doubleValue();
			Double totalCompras = ((Number) object[3]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorMes(Long.parseLong(object[0].toString()),
			 * Long.parseLong(object[1].toString()), categoriaPorMes, produto03, true)
			 * .doubleValue();
			 */

			if (categoriaPorMes == null || categoriaPorMes.getId() == null) {

				totalDeReceitas = contas.totalDeReceitasPorMes(Long.parseLong(object[0].toString()),
						Long.parseLong(object[1].toString())).doubleValue();

				totalDeDespesas = contas
						.totalDespesasPorMes(Long.parseLong(object[0].toString()), Long.parseLong(object[1].toString()))
						.doubleValue();

				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				values2.add((((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
						* 100);
			} else {

				values.add(totalDeVendas/* - totalDeCompras */);
				values2.add((totalDeVendas / totalCompras) * 100);
			}
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Percentual");
		dataSet2.setYaxisID("right-y-axis");
		// dataSet2.setFill(false);
		dataSet2.setBorderColor("rgba(255, 159, 64");

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {
			if (lucroPorLote != true) {
				labels.add(nameMes(((Long) object[0]).intValue()));
			} else {
				labels.add(nameMes(((Long) object[4]).intValue()));
			}
		}

		data.setLabels(labels);

		mixedModelPorMes.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

		CartesianLinearAxes linearAxes2 = new CartesianLinearAxes();
		linearAxes2.setId("right-y-axis");
		linearAxes2.setPosition("right");

		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		CartesianLinearTicks ticks2 = new CartesianLinearTicks();
		ticks2.setBeginAtZero(true);
		linearAxes2.setTicks(ticks2);

		cScales.addYAxesData(linearAxes);
		cScales.addYAxesData(linearAxes2);
		options.setScales(cScales);

		mixedModelPorMes.setOptions(options);

		mixedModelPorMes.setExtender("percentExtender2");
	}

	public void createMixedModelPorAno() {
		mixedModelPorAno = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		List<Object[]> result = vendas.totalLucrosPorAno(ano03, ano04, categoriaPorAno, produto04, true);

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeVendas = ((Number) object[1]).doubleValue();
			Double totalCompras = ((Number) object[2]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorAno(Long.parseLong(object[0].toString()), categoriaPorAno,
			 * produto04, true) .doubleValue();
			 */

			if (categoriaPorAno == null || categoriaPorAno.getId() == null) {

				totalDeReceitas = contas.totalDeReceitasPorAno(Long.parseLong(object[0].toString())).doubleValue();

				totalDeDespesas = contas.totalDespesasPorAno(Long.parseLong(object[0].toString())).doubleValue();

				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				values2.add((((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
						* 100);
			} else {

				values.add(totalDeVendas/* - totalDeCompras */);
				values2.add((totalDeVendas / totalCompras) * 100);
			}
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Percentual");
		dataSet2.setYaxisID("right-y-axis");
		// dataSet2.setFill(false);
		dataSet2.setBorderColor("rgba(255, 159, 64");

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {
			labels.add(String.valueOf(((Long) object[0]).longValue()));
		}

		data.setLabels(labels);

		mixedModelPorAno.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

		CartesianLinearAxes linearAxes2 = new CartesianLinearAxes();
		linearAxes2.setId("right-y-axis");
		linearAxes2.setPosition("right");

		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		CartesianLinearTicks ticks2 = new CartesianLinearTicks();
		ticks2.setBeginAtZero(true);
		linearAxes2.setTicks(ticks2);

		cScales.addYAxesData(linearAxes);
		cScales.addYAxesData(linearAxes2);
		options.setScales(cScales);

		mixedModelPorAno.setOptions(options);

		mixedModelPorAno.setExtender("percentExtender2");
	}

	public void prepareLucroPorLote() {
		createMixedModelPorMes();
	}

	public void prepareDonutModelPorDia() {

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);

		List<Object[]> result = vendas.totalLucrosPorData(calendarStart, calendarStop, categoriaPorDia, produto01,
				false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorSemana() {

		List<Object[]> result = vendas.totalLucrosPorSemana(ano01, semana01, semana02, categoriaPorSemana, produto02,
				false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorMes() {

		List<Object[]> result = vendas.totalLucrosPorMes(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes,
				produto03, false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorAno() {

		List<Object[]> result = vendas.totalLucrosPorAno(ano03, ano04, categoriaPorAno, produto04, false);

		createDonutModel(result);
	}

	public void createDonutModel(List<Object[]> result) {
		donutModel = new DonutChartModel();
		ChartData data = new ChartData();

		DonutChartDataSet dataSet = new DonutChartDataSet();
		List<Number> values = new ArrayList<>();

		for (Object[] object : result) {
			values.add((Number) object[3]);
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

	public List<CategoriaProduto> getTodasCategoriasProduto() {
		return todasCategoriasProduto;
	}

	public CategoriaProduto getCategoriaPorDia() {
		return categoriaPorDia;
	}

	public void setCategoriaPorDia(CategoriaProduto categoriaPorDia) {
		this.categoriaPorDia = categoriaPorDia;
	}

	public CategoriaProduto getCategoriaPorSemana() {
		return categoriaPorSemana;
	}

	public void setCategoriaPorSemana(CategoriaProduto categoriaPorSemana) {
		this.categoriaPorSemana = categoriaPorSemana;
	}

	public CategoriaProduto getCategoriaPorMes() {
		return categoriaPorMes;
	}

	public void setCategoriaPorMes(CategoriaProduto categoriaPorMes) {
		this.categoriaPorMes = categoriaPorMes;
	}

	public CategoriaProduto getCategoriaPorAno() {
		return categoriaPorAno;
	}

	public void setCategoriaPorAno(CategoriaProduto categoriaPorAno) {
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

	public Boolean getLucroPorLote() {
		return lucroPorLote;
	}

	public void setLucroPorLote(Boolean lucroPorLote) {
		this.lucroPorLote = lucroPorLote;
	}
}
