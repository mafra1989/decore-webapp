package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
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
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.polar.PolarAreaChartDataSet;
import org.primefaces.model.charts.polar.PolarAreaChartModel;

import com.webapp.model.FluxoDeCaixa;
import com.webapp.model.Lancamento;
import com.webapp.model.Top5Despesa;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Produtos;
import com.webapp.repository.Vendas;

@Named
@RequestScoped
public class DashboardBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Compras compras;

	@Inject
	private Vendas vendas;

	@Inject
	private Lancamentos lancamentos;

	@Inject
	private Produtos produtos;

	@Inject
	private Contas contas;

	private PieChartModel pieModel;

	private PolarAreaChartModel polarAreaModel;

	private BarChartModel barModel;

	private BarChartModel mixedModel;

	private DonutChartModel donutModel;

	private List<FluxoDeCaixa> tabela = new ArrayList<FluxoDeCaixa>();
	
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String saldoGeral = "0,00";

	@PostConstruct
	public void init() {
		createPieModel();
		createPolarAreaModel();
		createBarModel();
		createMixedModel();
		createDonutModel();
	}

	private void createPieModel() {
		pieModel = new PieChartModel();
		ChartData data = new ChartData();

		PieChartDataSet dataSet = new PieChartDataSet();
		List<Number> values = new ArrayList<>();

		List<Object[]> despesasTemp = contas.totalDespesasPorCategoriaMesAtual();

		List<Top5Despesa> top5Despesas = new ArrayList<>();
		for (Object[] object : despesasTemp) {
			System.out.println("Top5Despesas: " + object[1].toString());
			Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[0].toString()));
			if (lancamento != null) {

				Top5Despesa top5Despesa = new Top5Despesa();
				top5Despesa.setItem(lancamento.getCategoriaLancamento().getNome());
				top5Despesa.setValue(Double.parseDouble(object[1].toString()));
				if (!top5Despesas.contains(top5Despesa)) {
					top5Despesas.add(top5Despesa);
				} else {
					top5Despesas.get(top5Despesas.indexOf(top5Despesa))
							.setValue(top5Despesas.get(top5Despesas.indexOf(top5Despesa)).getValue().doubleValue()
									+ top5Despesa.getValue().doubleValue());
				}
			}
		}
		
		List<String> labels = new ArrayList<>();
		for (Top5Despesa top5Despesa : top5Despesas) {
			values.add(top5Despesa.getValue());
			labels.add(top5Despesa.getItem());
		}
		
		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		bgColors.add("rgb(255, 99, 132)");
		bgColors.add("rgb(54, 162, 235)");
		bgColors.add("rgb(255, 205, 86)");
		bgColors.add("rgb(201, 203, 207)");
		bgColors.add("rgb(249, 24, 24)");
		dataSet.setBackgroundColor(bgColors);

		data.addChartDataSet(dataSet);

		data.setLabels(labels);

		pieModel.setData(data);

		pieModel.setExtender("percentExtender");
	}

	private void createPolarAreaModel() {
		polarAreaModel = new PolarAreaChartModel();
		ChartData data = new ChartData();

		PolarAreaChartDataSet dataSet = new PolarAreaChartDataSet();
		List<Number> values = new ArrayList<>();

		List<Object[]> totalParaVendasPorCategoria = vendas.totalParaVendasPorCategoria();
		for (Object[] object : totalParaVendasPorCategoria) {
			values.add((Number) object[1]);
		}

		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		bgColors.add("rgb(255, 99, 132)");
		bgColors.add("rgb(75, 192, 192)");
		bgColors.add("rgb(255, 205, 86)");
		bgColors.add("rgb(201, 203, 207)");
		bgColors.add("rgb(249, 24, 24)");
		dataSet.setBackgroundColor(bgColors);

		data.addChartDataSet(dataSet);
		List<String> labels = new ArrayList<>();
		for (Object[] object : totalParaVendasPorCategoria) {
			labels.add((String) object[0]);
		}

		data.setLabels(labels);

		polarAreaModel.setData(data);

		polarAreaModel.setExtender("percentExtender4");
	}

	public void createBarModel() {
		barModel = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet barDataSet = new BarChartDataSet();
		barDataSet.setLabel("Valor Total");

		Number totalVendas = vendas.totalVendas();
		Number totalCompras = compras.totalCompras();

		Number totalDespesasPagas = contas.porContasPagas("DEBITO", "LANCAMENTO");

		Number totalDebitosPagos = contas.porContasPagas("DEBITO");
		Number totalCreditosPagos = contas.porContasPagas("CREDITO");

		// Number totalDebitos = lancamentos.totalDebitos();
		// Number totalCreditos = lancamentos.totalCreditos();

		List<Number> values = new ArrayList<>();
		values.add((totalVendas.doubleValue() + totalCreditosPagos.doubleValue()) - totalDebitosPagos.doubleValue());// Em
																														// Caixa
		values.add(totalVendas);// Vendas
		values.add(totalCreditosPagos);// Receitas
		// values.add(0);//Contas à Receber

		values.add(totalCompras);// Compras
		values.add(totalDespesasPagas);// Despesas
		// values.add(0);//Contas à Pagar
		values.add(produtos.totalAVender());// À Vender

		barDataSet.setData(values);

		tabela = new ArrayList<FluxoDeCaixa>();
		FluxoDeCaixa fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("Caixa");
		fluxoDeCaixa.setValue(
				(totalVendas.doubleValue() + totalCreditosPagos.doubleValue()) - totalDebitosPagos.doubleValue());
		tabela.add(fluxoDeCaixa);
		
		double saldo = (totalVendas.doubleValue() + totalCreditosPagos.doubleValue()) - totalDebitosPagos.doubleValue();
		saldoGeral = nf.format(saldo);

		fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("Vendas");
		fluxoDeCaixa.setValue(totalVendas.doubleValue());
		tabela.add(fluxoDeCaixa);

		fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("Receitas");
		fluxoDeCaixa.setValue(totalCreditosPagos.doubleValue());
		tabela.add(fluxoDeCaixa);
		/*
		 * fluxoDeCaixa = new FluxoDeCaixa(); fluxoDeCaixa.setItem("Contas à Receber");
		 * fluxoDeCaixa.setValue(0D); tabela.add(fluxoDeCaixa);
		 */

		fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("Compras");
		fluxoDeCaixa.setValue(totalCompras.doubleValue());
		tabela.add(fluxoDeCaixa);

		fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("Despesas");
		fluxoDeCaixa.setValue(totalDespesasPagas.doubleValue());
		tabela.add(fluxoDeCaixa);
		/*
		 * fluxoDeCaixa = new FluxoDeCaixa(); fluxoDeCaixa.setItem("Contas à Pagar");
		 * fluxoDeCaixa.setValue(0D); tabela.add(fluxoDeCaixa);
		 */

		fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("À Vender");
		fluxoDeCaixa.setValue(produtos.totalAVender().doubleValue());
		tabela.add(fluxoDeCaixa);

		List<String> bgColor = new ArrayList<>();
		bgColor.add("rgba(255, 99, 132, 0.2)");
		bgColor.add("rgba(255, 159, 64, 0.2)");
		bgColor.add("rgba(255, 205, 86, 0.2)");
		bgColor.add("rgba(75, 192, 192, 0.2)");
		bgColor.add("rgba(54, 162, 235, 0.2)");
		bgColor.add("rgba(201, 203, 207, 0.2)");
		barDataSet.setBackgroundColor(bgColor);

		List<String> borderColor = new ArrayList<>();
		borderColor.add("rgb(255, 99, 132)");
		borderColor.add("rgb(255, 159, 64)");
		borderColor.add("rgb(255, 205, 86)");
		borderColor.add("rgb(75, 192, 192)");
		borderColor.add("rgb(54, 162, 235)");
		borderColor.add("rgb(201, 203, 207)");
		barDataSet.setBorderColor(borderColor);
		barDataSet.setBorderWidth(1);

		data.addChartDataSet(barDataSet);

		List<String> labels = new ArrayList<>();
		labels.add("Caixa");
		labels.add("Vendas");
		labels.add("Receitas");
		// labels.add("À Receber");
		labels.add("Compras");
		labels.add("Despesas");
		// labels.add("À Pagar");
		labels.add("À Vender");
		data.setLabels(labels);
		barModel.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();
		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);
		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);

		Title title = new Title();
		title.setDisplay(true);
		title.setText("Fluxo de Caixa");
		options.setTitle(title);
		/*
		 * Legend legend = new Legend(); legend.setDisplay(false);
		 * legend.setPosition("top"); LegendLabel legendLabels = new LegendLabel();
		 * legendLabels.setFontStyle("bold"); legendLabels.setFontColor("#2980B9");
		 * legendLabels.setFontSize(24); legend.setLabels(legendLabels);
		 * options.setLegend(legend);
		 */

		barModel.setOptions(options);

		barModel.setExtender("percentExtender5");
	}

	public void createMixedModel() {
		mixedModel = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		values.add(vendas.totalVendasPorDiaDaSemana(1L));
		values.add(vendas.totalVendasPorDiaDaSemana(2L));
		values.add(vendas.totalVendasPorDiaDaSemana(3L));
		values.add(vendas.totalVendasPorDiaDaSemana(4L));
		values.add(vendas.totalVendasPorDiaDaSemana(5L));
		values.add(vendas.totalVendasPorDiaDaSemana(6L));
		values.add(vendas.totalVendasPorDiaDaSemana(7L));

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
		labels.add("Dom");
		labels.add("Seg");
		labels.add("Ter");
		labels.add("Qua");
		labels.add("Qui");
		labels.add("Sex");
		labels.add("Sáb");
		data.setLabels(labels);

		mixedModel.setData(data);

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();
		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);
		mixedModel.setOptions(options);

		mixedModel.setExtender("percentExtender2");
	}

	public void createDonutModel() {
		donutModel = new DonutChartModel();
		ChartData data = new ChartData();

		DonutChartDataSet dataSet = new DonutChartDataSet();
		List<Number> values = new ArrayList<>();

		List<Object[]> result = vendas.totalVendasPorCategoria();
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

	public PieChartModel getPieModel() {
		if (pieModel == null) {
			return pieModel = new PieChartModel();
		}
		return pieModel;
	}

	public void setPieModel(PieChartModel pieModel) {
		this.pieModel = pieModel;
	}

	public PolarAreaChartModel getPolarAreaModel() {
		if (polarAreaModel == null) {
			return polarAreaModel = new PolarAreaChartModel();
		}
		return polarAreaModel;
	}

	public void setPolarAreaModel(PolarAreaChartModel polarAreaModel) {
		this.polarAreaModel = polarAreaModel;
	}

	public BarChartModel getBarModel() {
		if (barModel == null) {
			return barModel = new BarChartModel();
		}
		return barModel;
	}

	public void setBarModel(BarChartModel barModel) {
		this.barModel = barModel;
	}

	public BarChartModel getMixedModel() {
		if (mixedModel == null) {
			mixedModel = new BarChartModel();
		}
		return mixedModel;
	}

	public void setMixedModel(BarChartModel mixedModel) {
		this.mixedModel = mixedModel;
	}

	public DonutChartModel getDonutModel() {
		if (donutModel == null) {
			return donutModel = new DonutChartModel();
		}
		return donutModel;
	}

	public void setDonutModel(DonutChartModel donutModel) {
		this.donutModel = donutModel;
	}

	public List<FluxoDeCaixa> getTabela() {
		return tabela;
	}

	public String getSaldoGeral() {
		return saldoGeral;
	}

}
