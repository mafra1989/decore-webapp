package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.PrimeFaces;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaProduto;
import com.webapp.model.Grupo;
import com.webapp.model.Produto;
import com.webapp.model.Target;
import com.webapp.model.Usuario;
import com.webapp.model.VendaPorCategoria;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Produtos;
import com.webapp.repository.Targets;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RelatorioVendasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Vendas vendas;

	@Inject
	private Produtos produtos;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Targets targets;

	private BarChartModel mixedModel;

	private BarChartModel mixedModelPorDia;

	private BarChartModel mixedModelPorSemana;

	private BarChartModel mixedModelPorMes;

	private BarChartModel mixedModelPorAno;

	private List<CategoriaProduto> todasCategoriasProduto;

	private List<Usuario> todosVendedores;

	@Inject
	private Usuario usuarioPorDia;

	@Inject
	private Usuario usuarioPorSemana;

	@Inject
	private Usuario usuarioPorMes;

	@Inject
	private Usuario usuarioPorAno;

	@Inject
	private CategoriasProdutos categoriasProdutos;

	@Inject
	private CategoriaProduto categoriaPorDia;

	private String[] categoriasPorDia;

	private String[] categoriasPorSemana;

	private String[] categoriasPorMes;

	private String[] categoriasPorAno;

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

	private String targetMsg;

	private Double target;
	private String option;

	private double targetDiario;
	private double targetSemanal;
	private double targetMensal;
	private double targetAnual;
	
	private List<VendaPorCategoria> detalhesVendasPorProduto = new ArrayList<>();
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
	
	private String totalValorVenda = "0,00";
	private String totalItensVenda = "0";
	
	@Inject
	private Produto produto;
	
	private Long produtoId;
	
	@Inject
	private Usuario usuario;

	@PostConstruct
	public void init() {
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	
		usuario = usuarios.porNome(user.getUsername());
		
		List<Grupo> grupos = usuario.getGrupos();
		
		if(grupos.size() > 0) {
			for (Grupo grupo : grupos) {
				if(grupo.getNome().equals("ADMINISTRADOR")) {
					EmpresaBean empresaBean = (EmpresaBean) FacesUtil.getObjectSession("empresaBean");
					if(empresaBean != null && empresaBean.getEmpresa() != null) {
						usuario.setEmpresa(empresaBean.getEmpresa());
					}
				}
			}
		}
		
		listarTodasCategoriasProdutos();

		Target target = targets.porPeriodoTipo("DIARIO", "VENDA", usuario.getEmpresa());
		if (target == null) {
			targetDiario = 0;
		} else {
			targetDiario = target.getValor().doubleValue();
		}

		target = targets.porPeriodoTipo("SEMANAL", "VENDA", usuario.getEmpresa());
		if (target == null) {
			targetSemanal = 0;
		} else {
			targetSemanal = target.getValor().doubleValue();
		}

		target = targets.porPeriodoTipo("MENSAL", "VENDA", usuario.getEmpresa());
		if (target == null) {
			targetMensal = 0;
		} else {
			targetMensal = target.getValor().doubleValue();
		}

		target = targets.porPeriodoTipo("ANUAL", "VENDA", usuario.getEmpresa());
		if (target == null) {
			targetAnual = 0;
		} else {
			targetAnual = target.getValor().doubleValue();
		}

		todosVendedores = usuarios.todos(usuario.getEmpresa());

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
		if (mes >= 4) {
			mes01 = nameMes(mes - 3);
			mes02 = nameMes(mes);
		} else {
			mes01 = nameMes(1);
			mes02 = nameMes(mes + 3);
		}

		createMixedModelPorMes();

		createDonutModel(new ArrayList<Object[]>());

		ano03 = "2019";

		ano04 = String.valueOf(calendarTemp.get(Calendar.YEAR));

		createMixedModelPorAno();
	}

	private void listarTodasCategoriasProdutos() {
		todasCategoriasProduto = categoriasProdutos.todos(usuario.getEmpresa());
	}
	
	public void carregarProduto(String codigo) {
		produto = produtos.porCodigo(codigo, usuario.getEmpresa());
		produtoId = produto.getId();
	}

	public void changeVendedorPorDia() {

		createMixedModelPorDia();
	}

	public void changeCategoriaPorDia() {
		produtosPorDia = new ArrayList<Produto>();
		produto01 = null;
		if (categoriasPorDia != null && categoriasPorDia.length > 0) {
			produtosPorDia = produtos.porCategoria(categoriasPorDia);
		}

		createMixedModelPorDia();
	}

	public void changeProdutoPorDia() {

		createMixedModelPorDia();
	}

	public void changeCategoriaPorSemana() {
		produtosPorSemana = new ArrayList<Produto>();
		produto02 = null;
		if (categoriasPorSemana != null && categoriasPorSemana.length > 0) {
			produtosPorSemana = produtos.porCategoria(categoriasPorSemana);
		}

		createMixedModelPorSemana();
	}

	public void changeProdutoPorSemana() {

		createMixedModelPorSemana();
	}

	public void changeVendedorPorSemana() {

		createMixedModelPorSemana();
	}

	public void changeCategoriaPorMes() {
		produtosPorMes = new ArrayList<Produto>();
		produto03 = null;
		if (categoriasPorMes != null && categoriasPorMes.length > 0) {
			produtosPorMes = produtos.porCategoria(categoriasPorMes);
		}

		createMixedModelPorMes();
	}

	public void changeProdutoPorMes() {

		createMixedModelPorMes();
	}

	public void changeVendedorPorMes() {

		createMixedModelPorMes();
	}

	public void changeCategoriaPorAno() {
		produtosPorAno = new ArrayList<Produto>();
		produto04 = null;
		if (categoriasPorAno != null && categoriasPorAno.length > 0) {
			produtosPorAno = produtos.porCategoria(categoriasPorAno);
		}

		createMixedModelPorAno();
	}

	public void changeProdutoPorAno() {

		createMixedModelPorAno();
	}

	public void changeVendedorPorAno() {

		createMixedModelPorAno();
	}

	public void createMixedModelPorDia() {
		mixedModelPorDia = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

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
				Calendar calendarTemp = Calendar.getInstance();
				calendarTemp.setTime(calendarStartTemp.getTime());
				Calendar calendarStopTemp = calendarTemp;
				calendarStopTemp.set(Calendar.HOUR, 23);
				calendarStopTemp.set(Calendar.MINUTE, 59);
				calendarStopTemp.set(Calendar.SECOND, 59);

				List<Object[]> resultTemp = vendas.totalVendasPorData(calendarStartTemp, calendarStopTemp,
						categoriaPorDia, categoriasPorDia, produto01, usuarioPorDia, true, usuario.getEmpresa());

				System.out.println("Data: " + calendarStartTemp.getTime() + " - " + resultTemp.size());
				System.out.println("Data Stop: " + calendarStopTemp.getTime());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[4];
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

					result.add(object);
				} else {
					for (Object[] object : resultTemp) {
						
						System.out.println(Arrays.asList(object));
						/* Transferido de Repository */
						if ((long) object[0] < 10) {
							object[0] = "0" + object[0];
						}

						if ((long) object[1] < 10) {
							object[1] = "0" + object[1];
						}
						
						result.add(object);
					}
				}

				calendarStartTemp.add(Calendar.DAY_OF_MONTH, 1);

				System.out.println("Data calendarStartTemp: " + calendarStartTemp.getTime());
				System.out.println("Data calendarStop: " + calendarStop.getTime());

				System.out.println("Data While: " + calendarStartTemp.before(calendarStop));

			} while (calendarStartTemp.before(calendarStop));
		}

		List<String> labels = new ArrayList<>();

		for (Object[] object : result) {
			values.add((Number) object[3]);
			values2.add(targetDiario);

			labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
			System.out.println(object[3]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Target");
		//dataSet2.setYaxisID("right-y-axis");
		dataSet2.setBorderColor("rgba(75, 192, 192)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		data.setLabels(labels);

		mixedModelPorDia.setData(data);

		// Options
		/*
		 * BarChartOptions options = new BarChartOptions(); CartesianScales cScales =
		 * new CartesianScales(); CartesianLinearAxes linearAxes = new
		 * CartesianLinearAxes(); CartesianLinearTicks ticks = new
		 * CartesianLinearTicks(); ticks.setBeginAtZero(true);
		 * linearAxes.setTicks(ticks);
		 * 
		 * cScales.addYAxesData(linearAxes); options.setScales(cScales);
		 * mixedModelPorDia.setOptions(options);
		 */

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

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
		
		LineChartDataSet dataSet2 = new LineChartDataSet();
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

				List<Object[]> resultTemp = vendas.totalVendasPorSemana(ano01, semana01, semana01, categoriaPorSemana,
						categoriasPorSemana, produto02, usuarioPorSemana, true, usuario.getEmpresa());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[3];

					object[0] = i;
					object[1] = ano01;

					object[2] = 0;

					result.add(object);
				} else {
					for (Object[] object : resultTemp) {
						result.add(object);
					}
				}
			}

			System.out.println("result.size(): " + result.size());
		}

		for (Object[] object : result) {
			values.add((Number) object[2]);
			values2.add(targetSemanal);

			long semana = Long.parseLong(object[0].toString());
			String semanaTemp = String.valueOf(semana);
			if (semana < 10) {
				semanaTemp = "0" + semana;
			}
			labels.add("W" + semanaTemp);

			System.out.println(object[2]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Target");
		//dataSet2.setYaxisID("right-y-axis");
		dataSet2.setBorderColor("rgba(75, 192, 192)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		data.setLabels(labels);

		mixedModelPorSemana.setData(data);

		// Options
		/*
		 * BarChartOptions options = new BarChartOptions(); CartesianScales cScales =
		 * new CartesianScales(); CartesianLinearAxes linearAxes = new
		 * CartesianLinearAxes(); CartesianLinearTicks ticks = new
		 * CartesianLinearTicks(); ticks.setBeginAtZero(true);
		 * linearAxes.setTicks(ticks);
		 * 
		 * cScales.addYAxesData(linearAxes); options.setScales(cScales);
		 * mixedModelPorSemana.setOptions(options);
		 */

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

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
		
		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		if (Integer.parseInt(numberMes(mes01)) <= Integer.parseInt(numberMes(mes02))) {

			for (int i = Integer.parseInt(numberMes(mes01)); i <= Integer.parseInt(numberMes(mes02)); i++) {
				String mes01 = String.valueOf(i);
				if (i < 10) {
					mes01 = "0" + i;
				}
				List<Object[]> resultTemp = vendas.totalVendasPorMes(ano02, mes01, mes01, categoriaPorMes,
						categoriasPorMes, produto03, usuarioPorMes, true, usuario.getEmpresa());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[4];

					object[0] = i;
					object[1] = ano02;

					object[2] = 0;

					result.add(object);

				} else {
					for (Object[] object : resultTemp) {
						result.add(object);
					}
				}
			}

		}

		for (Object[] object : result) {
			values.add((Number) object[2]);
			values2.add(targetMensal);

			labels.add(nameMes(Integer.parseInt(object[0].toString())));
			System.out.println(object[2]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Target");
		//dataSet2.setYaxisID("right-y-axis");
		dataSet2.setBorderColor("rgba(75, 192, 192)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		data.setLabels(labels);

		mixedModelPorMes.setData(data);

		// Options
		/*
		 * BarChartOptions options = new BarChartOptions(); CartesianScales cScales =
		 * new CartesianScales(); CartesianLinearAxes linearAxes = new
		 * CartesianLinearAxes(); CartesianLinearTicks ticks = new
		 * CartesianLinearTicks(); ticks.setBeginAtZero(true);
		 * linearAxes.setTicks(ticks);
		 * 
		 * cScales.addYAxesData(linearAxes); options.setScales(cScales);
		 * mixedModelPorMes.setOptions(options);
		 */

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

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
		
		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();
		if (Integer.parseInt(ano03) <= Integer.parseInt(ano04)) {

			for (int i = Integer.parseInt(ano03); i <= Integer.parseInt(ano04); i++) {

				String ano03 = String.valueOf(i);

				List<Object[]> resultTemp = vendas.totalVendasPorAno(ano03, ano03, categoriaPorAno, categoriasPorAno,
						produto04, usuarioPorAno, true, usuario.getEmpresa());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[2];

					object[0] = i;
					object[1] = 0;

					result.add(object);

				} else {
					for (Object[] object : resultTemp) {
						result.add(object);
					}
				}
			}
		}

		for (Object[] object : result) {
			values.add((Number) object[1]);
			values2.add(targetAnual);

			labels.add(String.valueOf(Integer.parseInt(object[0].toString())));
			System.out.println(object[1]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Valor Total");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Target");
		//dataSet2.setYaxisID("right-y-axis");
		dataSet2.setBorderColor("rgba(75, 192, 192)");

		/*
		 * LineChartDataSet dataSet2 = new LineChartDataSet(); List<Object> values2 =
		 * new ArrayList<>(); values2.add(50); values2.add(50); values2.add(50);
		 * values2.add(50); dataSet2.setLabel("Line Dataset"); dataSet2.setFill(false);
		 * dataSet2.setBorderColor("rgb(54, 162, 235)");
		 */

		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

		data.setLabels(labels);

		mixedModelPorAno.setData(data);

		// Options
		/*
		 * BarChartOptions options = new BarChartOptions(); CartesianScales cScales =
		 * new CartesianScales(); CartesianLinearAxes linearAxes = new
		 * CartesianLinearAxes(); CartesianLinearTicks ticks = new
		 * CartesianLinearTicks(); ticks.setBeginAtZero(true);
		 * linearAxes.setTicks(ticks); cScales.addYAxesData(linearAxes);
		 * options.setScales(cScales); mixedModelPorAno.setOptions(options);
		 */

		// Options
		BarChartOptions options = new BarChartOptions();
		CartesianScales cScales = new CartesianScales();

		CartesianLinearAxes linearAxes = new CartesianLinearAxes();
		linearAxes.setId("left-y-axis");
		linearAxes.setPosition("left");

		CartesianLinearTicks ticks = new CartesianLinearTicks();
		ticks.setBeginAtZero(true);
		linearAxes.setTicks(ticks);

		cScales.addYAxesData(linearAxes);
		options.setScales(cScales);

		mixedModelPorAno.setOptions(options);
		mixedModelPorAno.setExtender("percentExtender2");
	}

	public void prepareDonutModelPorDia() {
		
		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);

		List<Object[]> result = vendas.totalVendasPorData(calendarStart, calendarStop, categoriaPorDia,
				categoriasPorDia, produto01, usuarioPorDia, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void prepareDonutModelPorSemana() {

		List<Object[]> result = vendas.totalVendasPorSemana(ano01, semana01, semana02, categoriaPorSemana,
				categoriasPorSemana, produto02, usuarioPorSemana, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void prepareDonutModelPorMes() {

		List<Object[]> result = vendas.totalVendasPorMes(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes,
				categoriasPorMes, produto03, usuarioPorMes, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void prepareDonutModelPorAno() {

		List<Object[]> result = vendas.totalVendasPorAno(ano03, ano04, categoriaPorAno, categoriasPorAno, produto04,
				usuarioPorAno, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void createDonutModel(List<Object[]> result) {
		donutModel = new DonutChartModel();
		ChartData data = new ChartData();

		DonutChartDataSet dataSet = new DonutChartDataSet();
		List<Number> values = new ArrayList<>();
		
		detalhesVendasPorProduto = new ArrayList<>();

		int cont = 0;
		double totalValorVenda = 0;
		long totalItensVenda = 0;

		for (Object[] object : result) {
			
			if (cont < 5) {
				values.add((Number) object[2]);
				cont++;
			}

			VendaPorCategoria vendaPorProduto = new VendaPorCategoria();

			vendaPorProduto.setItem(object[0].toString());
			vendaPorProduto.setValue((Number) object[1]);
			vendaPorProduto.setQuantidade((Number) object[2]);
			vendaPorProduto.setCodigo(object[3].toString());

			totalValorVenda += vendaPorProduto.getValue().doubleValue();
			totalItensVenda += vendaPorProduto.getQuantidade().doubleValue();

			detalhesVendasPorProduto.add(vendaPorProduto);
		}
		
		this.totalValorVenda = nf.format(totalValorVenda);
		this.totalItensVenda = String.valueOf(totalItensVenda);
		
		System.out.println(totalItensVenda);

		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		bgColors.add("rgb(255, 99, 132)");
		bgColors.add("rgb(255, 159, 64)");
		bgColors.add("rgb(255, 205, 86)");
		bgColors.add("rgb(75, 192, 192)");
		bgColors.add("rgb(54, 162, 235)");
		bgColors.add("rgb(201, 203, 207)");
		dataSet.setBackgroundColor(bgColors);
		
		cont = 0;
		data.addChartDataSet(dataSet);
		List<String> labels = new ArrayList<>();
		for (Object[] object : result) {

			if (cont < 5) {
				labels.add((String) object[0]);
				cont++;
			}
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

	public Usuario getUsuarioPorDia() {
		return usuarioPorDia;
	}

	public void setUsuarioPorDia(Usuario usuarioPorDia) {
		this.usuarioPorDia = usuarioPorDia;
	}

	public Usuario getUsuarioPorSemana() {
		return usuarioPorSemana;
	}

	public void setUsuarioPorSemana(Usuario usuarioPorSemana) {
		this.usuarioPorSemana = usuarioPorSemana;
	}

	public Usuario getUsuarioPorMes() {
		return usuarioPorMes;
	}

	public void setUsuarioPorMes(Usuario usuarioPorMes) {
		this.usuarioPorMes = usuarioPorMes;
	}

	public Usuario getUsuarioPorAno() {
		return usuarioPorAno;
	}

	public void setUsuarioPorAno(Usuario usuarioPorAno) {
		this.usuarioPorAno = usuarioPorAno;
	}

	public List<Usuario> getTodosVendedores() {
		return todosVendedores;
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

	public void definirTargetDiario() {
		targetMsg = "Target para venda diária";
		target = targetDiario;
		option = "1";
	}

	public void definirTargetSemanal() {
		targetMsg = "Target para venda semanal";
		target = targetSemanal;
		option = "2";
	}

	public void definirTargetMensal() {
		targetMsg = "Target para venda mensal";
		target = targetMensal;
		option = "3";
	}

	public void definirTargetAnual() {
		targetMsg = "Target para venda anual";
		target = targetAnual;
		option = "4";
	}

	public String getTargetMsg() {
		return targetMsg;
	}

	public void confirmarTarget() {
		System.out.println("OPTION: " + option);
		System.out.println("TARGET: " + target);

		if (option.equalsIgnoreCase("1")) {

			Target targetTemp = targets.porPeriodoTipo("DIARIO", "VENDA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("DIARIO");
				targetTemp.setTipo("VENDA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetDiario = target;
			createMixedModelPorDia();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target diário salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-vendasPorDia");
		}

		if (option.equalsIgnoreCase("2")) {

			Target targetTemp = targets.porPeriodoTipo("SEMANAL", "VENDA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("SEMANAL");
				targetTemp.setTipo("VENDA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetSemanal = target;
			createMixedModelPorSemana();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target semanal salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-vendasPorSemana");
		}

		if (option.equalsIgnoreCase("3")) {

			Target targetTemp = targets.porPeriodoTipo("MENSAL", "VENDA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("MENSAL");
				targetTemp.setTipo("VENDA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetMensal = target;
			createMixedModelPorMes();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target mensal salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-vendasPorMes");
		}

		if (option.equalsIgnoreCase("4")) {

			Target targetTemp = targets.porPeriodoTipo("ANUAL", "VENDA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("ANUAL");
				targetTemp.setTipo("VENDA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetAnual = target;
			createMixedModelPorAno();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target anual salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-vendasPorAno");
		}
	}

	public Double getTarget() {
		return target;
	}

	public void setTarget(Double target) {
		this.target = target;
	}

	public String getTotalValorVenda() {
		return totalValorVenda;
	}

	public String getTotalItensVenda() {
		return totalItensVenda;
	}

	public List<VendaPorCategoria> getDetalhesVendasPorProduto() {
		return detalhesVendasPorProduto;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Long getProdutoId() {
		return produtoId;
	}
}