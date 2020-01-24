package com.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import javax.inject.Named;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.line.LineChartDataSet;

@Named
@RequestScoped
public class ChartJsView implements Serializable {
     
	private static final long serialVersionUID = 1L;
	
	private BarChartModel mixedModel;
     
 
    @PostConstruct
    public void init() {
        createMixedModel();
    }

    
    public void createMixedModel() {
        mixedModel = new BarChartModel();
        ChartData data = new ChartData();
         
        BarChartDataSet dataSet = new BarChartDataSet();
        List<Number> values = new ArrayList<>();
        values.add(10);
        values.add(20);
        values.add(30);
        values.add(40);
        dataSet.setData(values);
        dataSet.setLabel("Bar Dataset");
        dataSet.setBorderColor("rgb(255, 99, 132)");
        dataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
         
        LineChartDataSet dataSet2 = new LineChartDataSet();
        List<Number> values2 = new ArrayList<>();
        values2.add(50);
        values2.add(50);
        values2.add(50);
        values2.add(50);
        dataSet2.setData(values2);
        dataSet2.setLabel("Line Dataset");
        dataSet2.setFill(false);
        dataSet2.setBorderColor("rgb(54, 162, 235)");
         
        data.addChartDataSet(dataSet);
        data.addChartDataSet(dataSet2);
         
        List<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
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
    }
     
 
    public BarChartModel getMixedModel() {
        return mixedModel;
    }
 
    public void setMixedModel(BarChartModel mixedModel) {
        this.mixedModel = mixedModel;
    }

}