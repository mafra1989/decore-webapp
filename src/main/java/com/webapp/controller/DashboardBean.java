package com.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

import com.webapp.model.CategoriaLancamento;
import com.webapp.repository.Compras;
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
	
	
	private PieChartModel pieModel;
     
    private PolarAreaChartModel polarAreaModel;
     
    private BarChartModel barModel;
     
    private BarChartModel mixedModel;
     
    private DonutChartModel donutModel;
     
 
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
        
        List<Object[]> despesasTemp = lancamentos.totalDespesasPorCategoriaMesAtual();
        
        for (Object[] object : despesasTemp) {
        	values.add((Number) object[1]);
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
        List<String> labels = new ArrayList<>();
        for (Object[] object : despesasTemp) {
        	labels.add(((CategoriaLancamento) object[0]).getNome());
		}
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
        
        Number totalDebitos = lancamentos.totalDebitos();
        Number totalCreditos = lancamentos.totalCreditos();
         
        List<Number> values = new ArrayList<>();
        values.add((totalVendas.doubleValue() + totalCreditos.doubleValue()) - (totalCompras.doubleValue() + totalDebitos.doubleValue()));//Em Caixa
        values.add(totalVendas);//Vendas
        values.add(totalCreditos);//Outras Receitas
        values.add(totalCompras);//Compras
        values.add(totalDebitos);//Despesas
        values.add(produtos.totalAVender());//À Vender
        barDataSet.setData(values);
         
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
        labels.add("Outras Receitas");
        labels.add("Compras");
        labels.add("Despesas");
        labels.add("À Vender");
        data.setLabels(labels);
        barModel.setData(data);
         
        //Options
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
        Legend legend = new Legend();
        legend.setDisplay(false);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontStyle("bold");
        legendLabels.setFontColor("#2980B9");
        legendLabels.setFontSize(24);
        legend.setLabels(legendLabels);
        options.setLegend(legend);*/
 
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
        LineChartDataSet dataSet2 = new LineChartDataSet();
        List<Object> values2 = new ArrayList<>();
        values2.add(50);
        values2.add(50);
        values2.add(50);
        values2.add(50);
        dataSet2.setLabel("Line Dataset");
        dataSet2.setFill(false);
        dataSet2.setBorderColor("rgb(54, 162, 235)");
        */
         
        data.addChartDataSet(dataSet);
        //data.addChartDataSet(dataSet2);
         
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
         
        //Options
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
        return pieModel;
    }
 
    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }
 
    public PolarAreaChartModel getPolarAreaModel() {
        return polarAreaModel;
    }
 
    public void setPolarAreaModel(PolarAreaChartModel polarAreaModel) {
        this.polarAreaModel = polarAreaModel;
    }
 
    public BarChartModel getBarModel() {
        return barModel;
    }
 
    public void setBarModel(BarChartModel barModel) {
        this.barModel = barModel;
    }
 
    public BarChartModel getMixedModel() {
        return mixedModel;
    }
 
    public void setMixedModel(BarChartModel mixedModel) {
        this.mixedModel = mixedModel;
    }
 
    public DonutChartModel getDonutModel() {
        return donutModel;
    }
 
    public void setDonutModel(DonutChartModel donutModel) {
        this.donutModel = donutModel;
    }
    
}