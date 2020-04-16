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
import com.webapp.model.Produto;
import com.webapp.repository.CategoriasLancamentos;
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

	@PostConstruct
	public void init() {
		listarTodasCategoriasLancamentos();

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

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);

		List<Object[]> result = lancamentos.totalDespesasPorData(calendarStart, calendarStop, categoriaPorDia,
				true);
		for (Object[] object : result) {
			values.add((Number) object[3]);
			System.out.println(object[3]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet);
		// data.addChartDataSet(dataSet2);

		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {
			labels.add(object[0] + "/" + object[1] + "/" + object[2]);
		}

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

		List<Object[]> result = lancamentos.totalDespesasPorSemana(ano01, semana01, semana02, categoriaPorSemana, true);
		for (Object[] object : result) {
			values.add((Number) object[2]);
			System.out.println(object[2]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet);
		// data.addChartDataSet(dataSet2);

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

		List<Object[]> result = lancamentos.totalDespesasPorMes(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes, true);
		for (Object[] object : result) {
			values.add((Number) object[2]);
			System.out.println(object[2]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet);
		// data.addChartDataSet(dataSet2);

		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {
			labels.add(nameMes(((Long) object[0]).intValue()));
		}

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

		List<Object[]> result = lancamentos.totalDespesasPorAno(ano03, ano04, categoriaPorAno, true);
		for (Object[] object : result) {
			values.add((Number) object[1]);
			System.out.println(object[1]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet);
		// data.addChartDataSet(dataSet2);

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

		List<Object[]> result = lancamentos.totalDespesasPorData(calendarStart, calendarStop, categoriaPorDia,
				false);

		createDonutModel(result);
	}

	public void prepareDonutModelPorSemana() {

		List<Object[]> result = lancamentos.totalDespesasPorSemana(ano01, semana01, semana02, categoriaPorSemana, false);

		createDonutModel(result);
	}
	
	public void prepareDonutModelPorMes() {

		List<Object[]> result = lancamentos.totalDespesasPorMes(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes, false);

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
}