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

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Grupo;
import com.webapp.model.Lancamento;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.Produto;
import com.webapp.model.Target;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Contas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Targets;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RelatorioLancamentosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private Usuario usuario01;
	
	@Inject
	private Usuario usuario02;
	
	@Inject
	private Usuario usuario03;
	
	@Inject
	private Usuario usuario04;
	
	@Inject
	private Usuarios usuarios;

	private BarChartModel mixedModel;

	private BarChartModel mixedModelPorDia;

	private BarChartModel mixedModelPorSemana;

	private BarChartModel mixedModelPorMes;

	private BarChartModel mixedModelPorAno;

	private List<CategoriaLancamento> todasCategoriasLancamento;

	@Inject
	private Contas contas;
	
	@Inject
	private Targets targets;

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
	
	private String targetMsg;

	private Double target;
	private String option;

	private double targetDiario;
	private double targetSemanal;
	private double targetMensal;
	private double targetAnual;

	private boolean renderFavorecido01;
	private boolean renderFavorecido02;
	private boolean renderFavorecido03;
	private boolean renderFavorecido04;
	
	private List<Usuario> todosUsuarios;
	
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
		
		todosUsuarios = usuarios.todos(usuario.getEmpresa());
		listarTodasCategoriasLancamentos();
		
		Target target = targets.porPeriodoTipo("DIARIO", "DESPESA", usuario.getEmpresa());
		if (target == null) {
			targetDiario = 0;
		} else {
			targetDiario = target.getValor().doubleValue();
		}

		target = targets.porPeriodoTipo("SEMANAL", "DESPESA", usuario.getEmpresa());
		if (target == null) {
			targetSemanal = 0;
		} else {
			targetSemanal = target.getValor().doubleValue();
		}

		target = targets.porPeriodoTipo("MENSAL", "DESPESA", usuario.getEmpresa());
		if (target == null) {
			targetMensal = 0;
		} else {
			targetMensal = target.getValor().doubleValue();
		}

		target = targets.porPeriodoTipo("ANUAL", "DESPESA", usuario.getEmpresa());
		if (target == null) {
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

		ano03 = "2019";

		ano04 = String.valueOf(calendarTemp.get(Calendar.YEAR));

		createMixedModelPorAno();

		createDonutModel(new ArrayList<Object[]>());
	}

	private void listarTodasCategoriasLancamentos() {
		todasCategoriasLancamento = categoriasLancamentos.todos(usuario.getEmpresa());
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
		
		boolean favorecido = false;
		
		if (categoriasPorDia != null && categoriasPorDia.length > 0) {
			
			for (String categoria : categoriasPorDia) {
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				if(categoriaLancamento.getId() == 25835L || categoriaLancamento.getId() == 5423L ||
						categoriaLancamento.getId() == 5424L || categoriaLancamento.getId() == 5425L ||
								categoriaLancamento.getId() == 5426L || categoriaLancamento.getId() == 5427L) {
					favorecido = true;		
				}
			}			
		}	
		
		if(favorecido) {
			PrimeFaces.current().executeScript("mostrarFavorecido01();");
			renderFavorecido01 = true;
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido01();");
			renderFavorecido01 = false;
		}
		
		mixedModelPorDia = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();
		
		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.setTime(dateStart);
		calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);

		Calendar calendarStop = Calendar.getInstance();
		calendarStop.setTime(dateStop);
		calendarStop.add(Calendar.DAY_OF_MONTH, 1);
		calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);

		List<Object[]> result = new ArrayList<>();

		List<String> labels = new ArrayList<>();
		
		boolean debito = false, credito = false;

		if (calendarStart.before(calendarStop)) {

			Calendar calendarStartTemp = (Calendar) calendarStart.clone();

			do {
				Calendar calendarTemp = Calendar.getInstance();
				calendarTemp.setTime(calendarStartTemp.getTime());
				Calendar calendarStopTemp = calendarTemp;
				calendarStopTemp.set(Calendar.HOUR, 23);
				calendarStopTemp.set(Calendar.MINUTE, 59);
				calendarStopTemp.set(Calendar.SECOND, 59);

				List<Object[]> resultTemp = contas.totalLancamentosPorData(calendarStartTemp, calendarStopTemp, usuario.getEmpresa());

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

						objectTemp[0] = (long) object[0] < 10 ? "0" + object[0] : object[0];
						objectTemp[1] = (long) object[1] < 10 ? "0" + object[1] : object[1];
						objectTemp[2] = object[2];
						objectTemp[4] = object[4];

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[3].toString()), usuario.getEmpresa());
						if (lancamento != null) {
							if (categoriasPorDia != null && categoriasPorDia.length > 0) {
									
								if(favorecido) {
									
									if(usuario01 != null) {
										if(usuario01.getId() != null) {
											for (String categoria : categoriasPorDia) {
												if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria) && usuario01.getNome().equals(lancamento.getUsuario().getNome())) {
													valor += new BigDecimal(object[5].toString()).doubleValue();
												}
											}
										}
									} else {
										for (String categoria : categoriasPorDia) {
											if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
												valor += new BigDecimal(object[5].toString()).doubleValue();
											}
										}
									}
									
								} else {
									for (String categoria : categoriasPorDia) {
										if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
											valor += new BigDecimal(object[5].toString()).doubleValue();
										}
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
		
		
		String date = "";
		for (Object[] object : result) {

			if (!date.equals(object[0] + "/" + object[1])) {
				date = object[0] + "/" + object[1];		
				labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
				values3.add(targetDiario);
			}

			if (object[4].toString().equals("DEBITO")) {
				values.add((Number) object[5]);

			} else if (object[4].toString().equals("CREDITO")) {
				values2.add((Number) object[5]);

			} else if (object[4].toString().equals("")) {
				values.add((Number) object[5]);
				values2.add((Number) object[5]);
			}
				
		}
		
		if (categoriasPorDia != null && categoriasPorDia.length > 0) {
			for (String categoria : categoriasPorDia) {
				
				System.out.println(categoria);
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				System.out.println(categoriaLancamento.getTipoLancamento().getOrigem());
				if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
					debito = true;
					
				} else if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
					credito = true;
				}
			}								
		}
		
		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");
		data.addChartDataSet(dataSet3);

		if (categoriasPorDia == null || categoriasPorDia.length == 0
				|| debito) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriasPorDia == null || categoriasPorDia.length == 0
				|| credito) {
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
		
		boolean favorecido = false;
		
		if (categoriasPorSemana != null && categoriasPorSemana.length > 0) {
			
			for (String categoria : categoriasPorSemana) {
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				if(categoriaLancamento.getId() == 25835L || categoriaLancamento.getId() == 5423L ||
						categoriaLancamento.getId() == 5424L || categoriaLancamento.getId() == 5425L ||
								categoriaLancamento.getId() == 5426L || categoriaLancamento.getId() == 5427L) {
					favorecido = true;		
				}
			}			
		}	
		
		if(favorecido) {
			PrimeFaces.current().executeScript("mostrarFavorecido02();");
			renderFavorecido02 = true;
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido02();");
			renderFavorecido02 = false;
		}
		
		mixedModelPorSemana = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();
		
		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		List<String> labels = new ArrayList<>();
		
		boolean debito = false, credito = false;

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

				List<Object[]> resultTemp = contas.totalLancamentosPorSemana(ano01, semana01, semana01, true, usuario.getEmpresa());

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

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[2].toString()), usuario.getEmpresa());
						if (lancamento != null) {							
							if (categoriasPorSemana != null && categoriasPorSemana.length > 0) {
					
								if(favorecido) {
									
									if(usuario02 != null) {
										if(usuario02.getId() != null) {
										
											for (String categoria : categoriasPorSemana) {
												if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria) && usuario02.getNome().equals(lancamento.getUsuario().getNome())) {
													valor += new BigDecimal(object[4].toString()).doubleValue();
												}
											}										
										}										
									} else {
										for (String categoria : categoriasPorSemana) {
											if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
												valor += new BigDecimal(object[4].toString()).doubleValue();
											}
										}
									}
									
								} else {
									for (String categoria : categoriasPorSemana) {
										if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
											valor += new BigDecimal(object[4].toString()).doubleValue();
										}
									}
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
				values3.add(targetSemanal);
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

		
		if (categoriasPorSemana != null && categoriasPorSemana.length > 0) {
			for (String categoria : categoriasPorSemana) {
				
				System.out.println(categoria);
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				System.out.println(categoriaLancamento.getTipoLancamento().getOrigem());
				if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
					debito = true;
					
				} else if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
					credito = true;
				}
			}								
		}
		
		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");
		data.addChartDataSet(dataSet3);

		if (categoriasPorSemana == null || categoriasPorSemana.length == 0
				|| debito) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriasPorSemana == null || categoriasPorSemana.length == 0
				|| credito) {
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
		
		boolean favorecido = false;
		
		if (categoriasPorMes != null && categoriasPorMes.length > 0) {
			
			for (String categoria : categoriasPorMes) {
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				if(categoriaLancamento.getId() == 25835L || categoriaLancamento.getId() == 5423L ||
						categoriaLancamento.getId() == 5424L || categoriaLancamento.getId() == 5425L ||
								categoriaLancamento.getId() == 5426L || categoriaLancamento.getId() == 5427L) {
					favorecido = true;		
				}
			}			
		}	
		
		if(favorecido) {
			PrimeFaces.current().executeScript("mostrarFavorecido03();");
			renderFavorecido03 = true;
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido03();");
			renderFavorecido03 = false;
		}
		
		mixedModelPorMes = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();
		
		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();

		List<String> labels = new ArrayList<>();
		
		boolean debito = false, credito = false;

		if (Integer.parseInt(numberMes(mes01)) <= Integer.parseInt(numberMes(mes02))) {

			for (int i = Integer.parseInt(numberMes(mes01)); i <= Integer.parseInt(numberMes(mes02)); i++) {
				String mes01 = String.valueOf(i);
				if (i < 10) {
					mes01 = "0" + i;
				}

				List<Object[]> resultTemp = contas.totalDespesasPorMes(ano02, mes01, mes01, true, usuario.getEmpresa());

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

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[2].toString()), usuario.getEmpresa());
						if (lancamento != null) {							
							if (categoriasPorMes != null && categoriasPorMes.length > 0) {
					
								if(favorecido) {
									
									if(usuario03 != null) {
										if(usuario03.getId() != null) {
										
											for (String categoria : categoriasPorMes) {
												if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria) && usuario03.getNome().equals(lancamento.getUsuario().getNome())) {
													valor += new BigDecimal(object[4].toString()).doubleValue();
												}
											}
										}										
									} else {
										for (String categoria : categoriasPorMes) {
											if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
												valor += new BigDecimal(object[4].toString()).doubleValue();
											}
										}
									}
									
								} else {
									for (String categoria : categoriasPorMes) {
										if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
											valor += new BigDecimal(object[4].toString()).doubleValue();
										}
									}
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
				values3.add(targetMensal);
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

		if (categoriasPorMes != null && categoriasPorMes.length > 0) {
			for (String categoria : categoriasPorMes) {
				
				System.out.println(categoria);
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				System.out.println(categoriaLancamento.getTipoLancamento().getOrigem());
				if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
					debito = true;
					
				} else if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
					credito = true;
				}
			}								
		}
		
		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");
		data.addChartDataSet(dataSet3);

		if (categoriasPorMes == null || categoriasPorMes.length == 0
				|| debito) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriasPorMes == null || categoriasPorMes.length == 0
				|| credito) {
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
		
		boolean favorecido = false;
		
		if (categoriasPorAno != null && categoriasPorAno.length > 0) {
			
			for (String categoria : categoriasPorAno) {
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				if(categoriaLancamento.getId() == 25835L || categoriaLancamento.getId() == 5423L ||
						categoriaLancamento.getId() == 5424L || categoriaLancamento.getId() == 5425L ||
								categoriaLancamento.getId() == 5426L || categoriaLancamento.getId() == 5427L) {
					favorecido = true;		
				}
			}			
		}	
		
		if(favorecido) {
			PrimeFaces.current().executeScript("mostrarFavorecido04();");
			renderFavorecido04 = true;
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido04();");
			renderFavorecido04 = false;
		}
		
		mixedModelPorAno = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();
		
		BarChartDataSet dataSet2 = new BarChartDataSet();
		List<Number> values2 = new ArrayList<>();
		
		LineChartDataSet dataSet3 = new LineChartDataSet();
		List<Number> values3 = new ArrayList<>();
		
		List<String> labels = new ArrayList<>();
		
		boolean debito = false, credito = false;
		
		List<Object[]> result = new ArrayList<>();
		if(Integer.parseInt(ano03) <= Integer.parseInt(ano04)) {
			
			for (int i = Integer.parseInt(ano03); i <= Integer.parseInt(ano04); i++) {
				
				String ano03 = String.valueOf(i);
				
				List<Object[]> resultTemp = contas.totalDespesasPorAno(ano03, ano03, true, usuario.getEmpresa());
				
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

						Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[1].toString()), usuario.getEmpresa());
						if (lancamento != null) {
							if (categoriasPorAno != null && categoriasPorAno.length > 0) {
						
								if(favorecido) {
									
									if(usuario04 != null) {
										if(usuario04.getId() != null) {
										
											for (String categoria : categoriasPorAno) {
												if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria) && usuario04.getNome().equals(lancamento.getUsuario().getNome())) {
													valor += new BigDecimal(object[3].toString()).doubleValue();
												}
											}
										}										
									} else {
										for (String categoria : categoriasPorAno) {
											if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
												valor += new BigDecimal(object[3].toString()).doubleValue();
											}
										}
									}
									
								} else {
									for (String categoria : categoriasPorAno) {
										if (lancamento.getCategoriaLancamento().getNome().equalsIgnoreCase(categoria)) {
											valor += new BigDecimal(object[3].toString()).doubleValue();
										}
									}
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
				values3.add(targetAnual);
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

		if (categoriasPorAno != null && categoriasPorAno.length > 0) {
			for (String categoria : categoriasPorAno) {
				
				System.out.println(categoria);
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome(categoria, usuario.getEmpresa());
				System.out.println(categoriaLancamento.getTipoLancamento().getOrigem());
				if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
					debito = true;
					
				} else if(categoriaLancamento.getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
					credito = true;
				}
			}								
		}
		
		dataSet3.setData(values3);
		dataSet3.setLabel("Target");
		dataSet3.setBorderColor("rgba(75, 192, 192)");
		data.addChartDataSet(dataSet3);

		if (categoriasPorAno == null || categoriasPorAno.length == 0
				|| debito) {
			dataSet.setData(values);
			dataSet.setLabel("Débito");
			dataSet.setBorderColor("rgba(54, 162, 235)");
			dataSet.setBackgroundColor("rgba(54, 162, 235)");
			data.addChartDataSet(dataSet);
		}

		if (categoriasPorAno == null || categoriasPorAno.length == 0
				|| credito) {
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
	
	public void definirTargetDiario() {
		targetMsg = "Target para lançamento diário";
		target = targetDiario;
		option = "1";
	}

	public void definirTargetSemanal() {
		targetMsg = "Target para lançamento semanal";
		target = targetSemanal;
		option = "2";
	}

	public void definirTargetMensal() {
		targetMsg = "Target para lançamento mensal";
		target = targetMensal;
		option = "3";
	}

	public void definirTargetAnual() {
		targetMsg = "Target para lançamento anual";
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

			Target targetTemp = targets.porPeriodoTipo("DIARIO", "DESPESA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("DIARIO");
				targetTemp.setTipo("DESPESA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetDiario = target;
			createMixedModelPorDia();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target diário salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-despesasPorDia");
		}

		if (option.equalsIgnoreCase("2")) {

			Target targetTemp = targets.porPeriodoTipo("SEMANAL", "DESPESA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("SEMANAL");
				targetTemp.setTipo("DESPESA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetSemanal = target;
			createMixedModelPorSemana();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target semanal salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-despesasPorSemana");
		}

		if (option.equalsIgnoreCase("3")) {

			Target targetTemp = targets.porPeriodoTipo("MENSAL", "DESPESA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("MENSAL");
				targetTemp.setTipo("DESPESA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetMensal = target;
			createMixedModelPorMes();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target mensal salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-despesasPorMes");
		}

		if (option.equalsIgnoreCase("4")) {

			Target targetTemp = targets.porPeriodoTipo("ANUAL", "DESPESA", usuario.getEmpresa());
			if (targetTemp == null) {
				targetTemp = new Target();
				targetTemp.setPeriodo("ANUAL");
				targetTemp.setTipo("DESPESA");
				targetTemp.setValor(new BigDecimal(target));
			} else {
				targetTemp.setValor(new BigDecimal(target));
			}

			targetAnual = target;
			createMixedModelPorAno();

			targets.save(targetTemp);

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Target anual salvo com sucesso!' });");
			PrimeFaces.current().ajax().update("form:barChart-despesasPorAno");
		}
	}

	public Double getTarget() {
		return target;
	}

	public void setTarget(Double target) {
		this.target = target;
	}

	public boolean isRenderFavorecido01() {
		return renderFavorecido01;
	}

	public void setRenderFavorecido01(boolean renderFavorecido01) {
		this.renderFavorecido01 = renderFavorecido01;
	}

	public Usuario getUsuario01() {
		return usuario01;
	}

	public void setUsuario01(Usuario usuario01) {
		this.usuario01 = usuario01;
	}

	public boolean isRenderFavorecido02() {
		return renderFavorecido02;
	}

	public void setRenderFavorecido02(boolean renderFavorecido02) {
		this.renderFavorecido02 = renderFavorecido02;
	}

	public Usuario getUsuario02() {
		return usuario02;
	}

	public void setUsuario02(Usuario usuario02) {
		this.usuario02 = usuario02;
	}

	public boolean isRenderFavorecido03() {
		return renderFavorecido03;
	}

	public void setRenderFavorecido03(boolean renderFavorecido03) {
		this.renderFavorecido03 = renderFavorecido03;
	}

	public Usuario getUsuario03() {
		return usuario03;
	}

	public void setUsuario03(Usuario usuario03) {
		this.usuario03 = usuario03;
	}

	public boolean isRenderFavorecido04() {
		return renderFavorecido04;
	}

	public Usuario getUsuario04() {
		return usuario04;
	}

	public void setUsuario04(Usuario usuario04) {
		this.usuario04 = usuario04;
	}

	public void setRenderFavorecido04(boolean renderFavorecido04) {
		this.renderFavorecido04 = renderFavorecido04;
	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}
}