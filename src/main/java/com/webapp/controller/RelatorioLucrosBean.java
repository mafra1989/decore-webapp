package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Contas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Targets;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

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
	
	@Inject
	private Targets targets;
	
	@Inject
	private Usuarios usuarios;

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

	private String[] categoriasPorDia;

	private String[] categoriasPorSemana;

	private String[] categoriasPorMes;

	private String[] categoriasPorAno;

	private String targetMsg;

	private Double target;
	private String option;
	
	private double targetDiario;
	private double targetSemanal;
	private double targetMensal;
	private double targetAnual;
	
	@Inject
	private Usuario usuarioPorDia;

	@Inject
	private Usuario usuarioPorSemana;

	@Inject
	private Usuario usuarioPorMes;

	@Inject
	private Usuario usuarioPorAno;
	
	private List<Usuario> todosVendedores;
	
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
		todosVendedores = usuarios.todos(usuario.getEmpresa());
		
		Target target = targets.porPeriodoTipo("DIARIO", "LUCRO", usuario.getEmpresa());
		if(target == null) {
			targetDiario = 0;
		} else {
			targetDiario = target.getValor().doubleValue();
		}
		
		target = targets.porPeriodoTipo("SEMANAL", "LUCRO", usuario.getEmpresa());
		if(target == null) {
			targetSemanal = 0;
		} else {
			targetSemanal = target.getValor().doubleValue();
		}
		
		target = targets.porPeriodoTipo("MENSAL", "LUCRO", usuario.getEmpresa());
		if(target == null) {
			targetMensal = 0;
		} else {
			targetMensal = target.getValor().doubleValue();
		}
		
		target = targets.porPeriodoTipo("ANUAL", "LUCRO", usuario.getEmpresa());
		if(target == null) {
			targetAnual = 0;
		} else {
			targetAnual = target.getValor().doubleValue();
		}

		Calendar calendar = Calendar.getInstance();
		Calendar calendarTemp = Calendar.getInstance();

		int semana01 = calendarTemp.get(Calendar.WEEK_OF_YEAR) - 3;
		if (semana01 >= 4) {

			String semanaTemp = String.valueOf(semana01);
			if (semana01 < 10) {
				semanaTemp = "0" + semana01;
			}

			this.semana01 = "W" + semanaTemp;

			calendar.add(Calendar.DAY_OF_MONTH, -3);

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

		// calendar.add(Calendar.DAY_OF_MONTH, -5);
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
			System.out.println(Arrays.asList(categoriasPorSemana) + "-" + produtosPorSemana.size());
		}

		createMixedModelPorSemana();
	}

	public void changeProdutoPorSemana() {

		createMixedModelPorSemana();
	}

	public void changeCategoriaPorMes() {
		produtosPorMes = new ArrayList<Produto>();
		produto03 = null;

		if (categoriasPorMes != null && categoriasPorMes.length > 0) {
			produtosPorMes = produtos.porCategoria(categoriasPorMes);
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

		if (categoriasPorAno != null && categoriasPorAno.length > 0) {
			produtosPorAno = produtos.porCategoria(categoriasPorAno);
		}

		createMixedModelPorAno();
	}

	public void changeProdutoPorAno() {

		createMixedModelPorAno();
	}
	
	
	public void changeVendedorPorDia() {

		createMixedModelPorDia();
	}
	
	public void changeVendedorPorMes() {

		createMixedModelPorMes();
	}
	
	public void changeVendedorPorSemana() {

		createMixedModelPorSemana();
	}
	
	public void changeVendedorPorAno() {

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
				Calendar calendarTemp = Calendar.getInstance();
				calendarTemp.setTime(calendarStartTemp.getTime());
				Calendar calendarStopTemp = calendarTemp;
				calendarStopTemp.set(Calendar.HOUR, 23);
				calendarStopTemp.set(Calendar.MINUTE, 59);
				calendarStopTemp.set(Calendar.SECOND, 59);

				List<Object[]> resultTemp = vendas.totalLucrosPorData(calendarStartTemp, calendarStopTemp,
						categoriaPorDia, categoriasPorDia, produto01, usuarioPorDia, true, usuario.getEmpresa());

				System.out.println("Data: " + calendarStartTemp.getTime() + " - " + resultTemp.size());

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

					object[3] = 0;
					object[4] = 0;
					object[5] = 0;

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

		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		for (Object[] object : result) {
			Double totalDeVendas = ((Number) object[3]).doubleValue();

			System.out.println("totalComprasPorData: " + object[4]);
			Double totalCompras = ((Number) object[4]).doubleValue();
			
			Double totalVendasComPrecoVenda = ((Number) object[5]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorData(Long.parseLong(object[0].toString()),
			 * Long.parseLong(object[1].toString()), Long.parseLong(object[2].toString()),
			 * categoriaPorDia, produto01, true) .doubleValue();
			 */

			if (categoriasPorDia == null || categoriasPorDia.length == 0 && (usuarioPorDia == null || usuarioPorDia.getId() == null)) {

				totalDeReceitas = contas
						.totalDeReceitasPorDia(Long.parseLong(object[0].toString()),
								Long.parseLong(object[1].toString()), Long.parseLong(object[2].toString()),
								usuario.getEmpresa())
						.doubleValue();

				System.out.println("Total vendas: " + totalDeVendas);
				System.out.println("Receitas: " + totalDeReceitas);

				totalDeDespesas = contas
						.totalDespesasPorData(Long.parseLong(object[0].toString()),
								Long.parseLong(object[1].toString()), Long.parseLong(object[2].toString()),
								usuario.getEmpresa())
						.doubleValue();

				System.out.println("Despesas: " + totalDeDespesas);

				// if (totalDeReceitas > 0 || totalDeDespesas > 0 || totalDeVendas > 0 ||
				// totalCompras > 0) {

				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) == 0 && totalDeDespesas > 0) {
					values2.add(-100.0);
					System.out.println("Valor Percentual: -100.0");
				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) > 0
						&& (totalCompras + totalDeDespesas) == 0) {
					values2.add(100.0);
					System.out.println("Valor Percentual: 100.0");

				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas == 0) && totalDeDespesas == 0) {
					values2.add(0);

				} else {
					values2.add(
							(((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
					System.out.println("Valor Percentual: "
							+ (((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
				}

				System.out.println("Percentual: "
						+ (((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
								* 100);

				labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
				// }

			} else {
				// if (totalDeVendas > 0 || totalCompras > 0) {
				values.add(totalDeVendas/* - totalDeCompras */);

				if (totalDeVendas > 0) {
					//values2.add((totalDeVendas / totalCompras) * 100);
					values2.add((totalVendasComPrecoVenda - totalCompras)/totalVendasComPrecoVenda * 100);
				} else {
					values2.add(0);
				}

				labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
				// }
			}

			values3.add(targetDiario);

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
		dataSet2.setBorderColor("rgba(255, 159, 64)");

		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");

		data.addChartDataSet(dataSet3);
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

				List<Object[]> resultTemp = vendas.totalLucrosPorSemana(ano01, semana01, semana01, categoriaPorSemana,
						categoriasPorSemana, produto02, usuarioPorSemana, true, usuario.getEmpresa());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[5];

					object[0] = i;
					object[1] = ano01;

					object[2] = 0;
					object[3] = 0;
					object[4] = 0;

					result.add(object);
				} else {
					for (Object[] object : resultTemp) {
						result.add(object);
					}
				}
			}

			System.out.println("result.size(): " + result.size());
		}

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeVendas = ((Number) object[2]).doubleValue();
			Double totalCompras = ((Number) object[3]).doubleValue();
			
			Double totalVendasComPrecoVenda = ((Number) object[4]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorSemana(Long.parseLong(object[0].toString()),
			 * Long.parseLong(object[1].toString()), categoriaPorSemana, produto02, true)
			 * .doubleValue();
			 */

			if (categoriasPorSemana == null || categoriasPorSemana.length == 0 && (usuarioPorSemana == null || usuarioPorSemana.getId() == null)) {

				totalDeReceitas = contas.totalDeReceitasPorSemana(Long.parseLong(object[0].toString()),
						Long.parseLong(object[1].toString()), usuario.getEmpresa()).doubleValue();

				totalDeDespesas = contas.totalDespesasPorSemana(Long.parseLong(object[0].toString()),
						Long.parseLong(object[1].toString()), usuario.getEmpresa()).doubleValue();

				// if (totalDeReceitas > 0 || totalDeDespesas > 0 || totalDeVendas > 0 ||
				// totalCompras > 0) {
				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) == 0 && totalDeDespesas > 0) {
					values2.add(-100.0);
					System.out.println("Valor Percentual: -100.0");
				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) > 0
						&& (totalCompras + totalDeDespesas) == 0) {
					values2.add(100.0);
					System.out.println("Valor Percentual: 100.0");

				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas == 0) && totalDeDespesas == 0) {
					values2.add(0);

				} else {
					values2.add(
							(((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
					System.out.println("Valor Percentual: "
							+ (((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
				}

				long semana = Long.parseLong(object[0].toString());
				String semanaTemp = String.valueOf(semana);
				if (semana < 10) {
					semanaTemp = "0" + semana;
				}
				labels.add("W" + semanaTemp);
				// }

			} else {
				// if (totalDeVendas > 0 || totalCompras > 0) {
				values.add(totalDeVendas/* - totalDeCompras */);

				if (totalDeVendas > 0) {
					//values2.add((totalDeVendas / totalCompras) * 100);
					values2.add((totalVendasComPrecoVenda - totalCompras)/totalVendasComPrecoVenda * 100);
				} else {
					values2.add(0);
				}

				long semana = Long.parseLong(object[0].toString());
				String semanaTemp = String.valueOf(semana);
				if (semana < 10) {
					semanaTemp = "0" + semana;
				}
				labels.add("W" + semanaTemp);
				// }
			}

			values3.add(targetSemanal);
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

		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");

		data.addChartDataSet(dataSet3);
		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

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
			if (Integer.parseInt(numberMes(mes01)) <= Integer.parseInt(numberMes(mes02))) {

				for (int i = Integer.parseInt(numberMes(mes01)); i <= Integer.parseInt(numberMes(mes02)); i++) {
					String mes01 = String.valueOf(i);
					if (i < 10) {
						mes01 = "0" + i;
					}
					List<Object[]> resultTemp = vendas.totalLucrosPorMes(ano02, mes01, mes01, categoriaPorMes,
							categoriasPorMes, produto03, usuarioPorMes, true, usuario.getEmpresa());

					if (resultTemp.size() == 0) {

						Object[] object = new Object[5];

						object[0] = i;
						object[1] = ano02;

						object[2] = 0;
						object[3] = 0;
						object[4] = 0;

						result.add(object);

					} else {
						for (Object[] object : resultTemp) {
							result.add(object);
						}
					}
				}

			}
		} else {

			if (Integer.parseInt(numberMes(mes01)) <= Integer.parseInt(numberMes(mes02))) {

				for (int i = Integer.parseInt(numberMes(mes01)); i <= Integer.parseInt(numberMes(mes02)); i++) {
					String mes01 = String.valueOf(i);
					if (i < 10) {
						mes01 = "0" + i;
					}
					List<Object[]> resultTemp = vendas.totalLucrosPorLote(ano02, mes01, mes01, categoriaPorMes,
							categoriasPorMes, produto03, true, usuario.getEmpresa());

					if (resultTemp.size() == 0) {

						Object[] object = new Object[6];

						object[0] = i;
						object[1] = ano02;

						object[2] = 0;
						object[3] = 0;
						object[4] = i;
						object[5] = 0;

						result.add(object);

					} else {
						for (Object[] object : resultTemp) {
							result.add(object);
						}
					}
				}

			}
		}

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeVendas = ((Number) object[2]).doubleValue();
			Double totalCompras = ((Number) object[3]).doubleValue();
			
			Double totalVendasComPrecoVenda = 0D;
			if (lucroPorLote != true) {
				totalVendasComPrecoVenda = ((Number) object[4]).doubleValue(); 
			} else {
				totalVendasComPrecoVenda = ((Number) object[5]).doubleValue(); 
			}

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorMes(Long.parseLong(object[0].toString()),
			 * Long.parseLong(object[1].toString()), categoriaPorMes, produto03, true)
			 * .doubleValue();
			 */

			if (categoriasPorMes == null || categoriasPorMes.length == 0 && (usuarioPorMes == null || usuarioPorMes.getId() == null)) {

				totalDeReceitas = contas.totalDeReceitasPorMes(Long.parseLong(object[0].toString()),
						Long.parseLong(object[1].toString()), usuario.getEmpresa()).doubleValue();

				totalDeDespesas = contas
						.totalDespesasPorMes(Long.parseLong(object[0].toString()), Long.parseLong(object[1].toString()), 
								usuario.getEmpresa())
						.doubleValue();

				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) == 0 && totalDeDespesas > 0) {
					values2.add(-100.0);
					System.out.println("Valor Percentual: -100.0");
				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) > 0
						&& (totalCompras + totalDeDespesas) == 0) {
					values2.add(100.0);
					System.out.println("Valor Percentual: 100.0");

				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas == 0) && totalDeDespesas == 0) {
					values2.add(0);

				} else {
					values2.add(
							(((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
					System.out.println("Valor Percentual: "
							+ (((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
				}

				if (lucroPorLote != true) {
					labels.add(nameMes((Integer.parseInt(object[0].toString()))));
				} else {
					labels.add(nameMes((Integer.parseInt(object[4].toString()))));
				}

			} else {

				values.add(totalDeVendas/* - totalDeCompras */);

				if (totalDeVendas > 0) {
					//values2.add((totalDeVendas / totalCompras) * 100);
					values2.add((totalVendasComPrecoVenda - totalCompras)/totalVendasComPrecoVenda * 100);
				} else {
					values2.add(0);
				}

				if (lucroPorLote != true) {
					labels.add(nameMes((Integer.parseInt(object[0].toString()))));
				} else {
					labels.add(nameMes((Integer.parseInt(object[4].toString()))));
				}
			}

			values3.add(targetMensal);
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

		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");

		data.addChartDataSet(dataSet3);
		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

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

		List<Object[]> result = new ArrayList<>();
		if (Integer.parseInt(ano03) <= Integer.parseInt(ano04)) {

			for (int i = Integer.parseInt(ano03); i <= Integer.parseInt(ano04); i++) {

				String ano03 = String.valueOf(i);

				List<Object[]> resultTemp = vendas.totalLucrosPorAno(ano03, ano03, categoriaPorAno, categoriasPorAno,
						produto04, usuarioPorAno, true, usuario.getEmpresa());

				if (resultTemp.size() == 0) {

					Object[] object = new Object[4];

					object[0] = i;

					object[1] = 0;
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

		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeVendas = ((Number) object[1]).doubleValue();
			Double totalCompras = ((Number) object[2]).doubleValue();
			
			Double totalVendasComPrecoVenda = ((Number) object[3]).doubleValue();

			Double totalDeReceitas = 0D;
			Double totalDeDespesas = 0D;

			/*
			 * Double totalDeCompras = compras
			 * .totalComprasPorAno(Long.parseLong(object[0].toString()), categoriaPorAno,
			 * produto04, true) .doubleValue();
			 */
			if (categoriasPorAno == null || categoriasPorAno.length == 0 && (usuarioPorAno == null || usuarioPorAno.getId() == null)) {

				totalDeReceitas = contas.totalDeReceitasPorAno(Long.parseLong(object[0].toString()), 
						usuario.getEmpresa()).doubleValue();

				totalDeDespesas = contas.totalDespesasPorAno(Long.parseLong(object[0].toString()), 
						usuario.getEmpresa()).doubleValue();

				values.add(((totalDeVendas + totalDeReceitas) - totalDeDespesas));

				if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) == 0 && totalDeDespesas > 0) {
					values2.add(-100.0);
					System.out.println("Valor Percentual: -100.0");
				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas) > 0
						&& (totalCompras + totalDeDespesas) == 0) {
					values2.add(100.0);
					System.out.println("Valor Percentual: 100.0");

				} else if (((totalDeVendas + totalDeReceitas) - totalDeDespesas == 0) && totalDeDespesas == 0) {
					values2.add(0);

				} else {
					values2.add(
							(((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
					System.out.println("Valor Percentual: "
							+ (((totalDeVendas + totalDeReceitas) - totalDeDespesas) / (totalCompras + totalDeDespesas))
									* 100);
				}

				labels.add(object[0].toString());
			} else {

				values.add(totalDeVendas/* - totalDeCompras */);

				if (totalDeVendas > 0) {
					//values2.add((totalDeVendas / totalCompras) * 100);
					values2.add((totalVendasComPrecoVenda - totalCompras)/totalVendasComPrecoVenda * 100);
				} else {
					values2.add(0);
				}

				labels.add(object[0].toString());
			}

			values3.add(targetAnual);
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

		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");

		data.addChartDataSet(dataSet3);
		data.addChartDataSet(dataSet2);
		data.addChartDataSet(dataSet);

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

		List<Object[]> result = vendas.totalLucrosPorData(calendarStart, calendarStop, categoriaPorDia,
				categoriasPorDia, produto01, usuarioPorDia, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void prepareDonutModelPorSemana() {

		List<Object[]> result = vendas.totalLucrosPorSemana(ano01, semana01, semana02, categoriaPorSemana,
				categoriasPorSemana, produto02, usuarioPorSemana, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void prepareDonutModelPorMes() {

		List<Object[]> result = vendas.totalLucrosPorMes(ano02, numberMes(mes01), numberMes(mes02), categoriaPorMes,
				categoriasPorMes, produto03, usuarioPorMes, false, usuario.getEmpresa());

		createDonutModel(result);
	}

	public void prepareDonutModelPorAno() {

		List<Object[]> result = vendas.totalLucrosPorAno(ano03, ano04, categoriaPorAno, categoriasPorAno, produto04,
				usuarioPorAno, false, usuario.getEmpresa());

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

	public String[] getCategoriasPorSemana() {
		return categoriasPorSemana;
	}

	public void setCategoriasPorSemana(String[] categoriasPorSemana) {
		this.categoriasPorSemana = categoriasPorSemana;
	}

	public String[] getCategoriasPorDia() {
		return categoriasPorDia;
	}

	public void setCategoriasPorDia(String[] categoriasPorDia) {
		this.categoriasPorDia = categoriasPorDia;
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

	public Integer categoriasPorMesSize() {

		if (categoriasPorMes != null) {
			return categoriasPorMes.length;
		}

		return 0;
	}

	public void definirTargetDiario() {
		targetMsg = "Target para lucro diário";
		target = targetDiario;
		option = "1";
	}

	public void definirTargetSemanal() {
		targetMsg = "Target para lucro semanal";
		target = targetSemanal;
		option = "2";
	}

	public void definirTargetMensal() {
		targetMsg = "Target para lucro mensal";
		target = targetMensal;
		option = "3";
	}

	public void definirTargetAnual() {
		targetMsg = "Target para lucro anual";
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
			
			Target targetTemp = targets.porPeriodoTipo("DIARIO", "LUCRO", usuario.getEmpresa());
			if(targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("DIARIO");
				targetTemp.setTipo("LUCRO");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}
			
			targetDiario = target;
			createMixedModelPorDia();
			
			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target diário salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-lucrosPorDia");
		}
		
		if (option.equalsIgnoreCase("2")) {
			
			Target targetTemp = targets.porPeriodoTipo("SEMANAL", "LUCRO", usuario.getEmpresa());
			if(targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("SEMANAL");
				targetTemp.setTipo("LUCRO");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}
			
			targetSemanal = target;
			createMixedModelPorSemana();
			
			targets.save(targetTemp);
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target semanal salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-lucrosPorSemana");
		}
		
		if (option.equalsIgnoreCase("3")) {
			
			Target targetTemp = targets.porPeriodoTipo("MENSAL", "LUCRO", usuario.getEmpresa());
			if(targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("MENSAL");
				targetTemp.setTipo("LUCRO");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}
			
			targetMensal = target;
			createMixedModelPorMes();
			
			targets.save(targetTemp);
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target mensal salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-lucrosPorMes");
		}
		
		if (option.equalsIgnoreCase("4")) {
			
			Target targetTemp = targets.porPeriodoTipo("ANUAL", "LUCRO", usuario.getEmpresa());
			if(targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("ANUAL");
				targetTemp.setTipo("LUCRO");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}
			
			targetAnual = target;
			createMixedModelPorAno();
			
			targets.save(targetTemp);
			
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target anual salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-lucrosPorAno");
		}
	}

	public Double getTarget() {
		return target;
	}

	public void setTarget(Double target) {
		this.target = target;
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
}
