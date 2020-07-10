package com.webapp.controller;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.FluxoDeCaixa;
import com.webapp.model.Grupo;
import com.webapp.model.Lancamento;
import com.webapp.model.ListaProduto;
import com.webapp.model.OrdenaTop5;
import com.webapp.model.Produto;
import com.webapp.model.Top5Despesa;
import com.webapp.model.Usuario;
import com.webapp.model.VendaPorCategoria;
import com.webapp.report.Relatorio;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
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

	private List<VendaPorCategoria> detalhesVendasPorCategoria = new ArrayList<>();

	private List<VendaPorCategoria> detalhesEstoqueParaVendaPorCategoria = new ArrayList<>();

	private List<Top5Despesa> top5Despesas = new ArrayList<>();

	private List<VendaPorCategoria> detalhesVendasPorProduto = new ArrayList<>();

	private List<VendaPorCategoria> detalhesEstoquePorProduto = new ArrayList<>();

	@Inject
	private VendaPorCategoria vendaPorCategoriaSelecionada;

	@Inject
	private VendaPorCategoria estoquePorCategoriaSelecionada;

	private String totalValorVenda = "0,00";
	private String totalItensVenda = "0";
	private String totalValorEstoque = "0,00";
	private String totalItensEstoque = "0";

	private String totalDespesasTop5 = "0,00";
	
	@Inject
	private Produto produto;

	private byte[] fileContent;
	
	private Long produtoId;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
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

			createPieModel();
			createPolarAreaModel();
			createBarModel();
			//createMixedModel();
			createDonutModel();
			
		}
	}

	private void createPieModel() {
		pieModel = new PieChartModel();
		ChartData data = new ChartData();

		PieChartDataSet dataSet = new PieChartDataSet();
		List<Number> values = new ArrayList<>();

		List<Object[]> despesasTemp = contas.totalDespesasPorCategoriaMesAtual();

		double totalDespesasTop5 = 0;

		top5Despesas = new ArrayList<>();
		for (Object[] object : despesasTemp) {

			Lancamento lancamento = lancamentos.porNumeroLancamento(Long.parseLong(object[0].toString()), usuario.getEmpresa());
			if (lancamento != null) {

				Top5Despesa top5Despesa = new Top5Despesa();
				top5Despesa.setItem(lancamento.getCategoriaLancamento().getNome());
				top5Despesa.setValue(Double.parseDouble(object[1].toString()));
				if (!top5Despesas.contains(top5Despesa)) {

					if (top5Despesas.size() < 5) {
						top5Despesas.add(top5Despesa);
						totalDespesasTop5 += top5Despesa.getValue().doubleValue();
					}

				} else {
					top5Despesas.get(top5Despesas.indexOf(top5Despesa))
							.setValue(top5Despesas.get(top5Despesas.indexOf(top5Despesa)).getValue().doubleValue()
									+ top5Despesa.getValue().doubleValue());

					totalDespesasTop5 += top5Despesa.getValue().doubleValue();
				}
			}
		}

		this.totalDespesasTop5 = nf.format(totalDespesasTop5);
		Collections.sort(top5Despesas, new OrdenaTop5().reversed());

		List<String> labels = new ArrayList<>();
		for (Top5Despesa top5Despesa : top5Despesas) {
			values.add(top5Despesa.getValue());
			labels.add(top5Despesa.getItem());
		}

		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		//bgColors.add("rgb(255, 99, 132)");
		//bgColors.add("rgb(54, 162, 235)");
		//bgColors.add("rgb(255, 205, 86)");
		//bgColors.add("rgb(201, 203, 207)");
		//bgColors.add("rgb(249, 24, 24)");
		
		bgColors.add("rgb(255, 99, 132)");
		bgColors.add("rgb(255, 159, 64)");
		bgColors.add("rgb(255, 205, 86)");
		bgColors.add("rgb(75, 192, 192)");
		bgColors.add("rgb(54, 162, 235)");
		bgColors.add("rgb(201, 203, 207)");
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

		detalhesEstoqueParaVendaPorCategoria = new ArrayList<>();

		int cont = 0;
		double totalValorEstoque = 0;
		long totalItensEstoque = 0;
		List<Object[]> totalParaVendasPorCategoria = vendas.totalParaVendasPorCategoria(usuario.getEmpresa());
		for (Object[] object : totalParaVendasPorCategoria) {

			if (cont < 5) {
				values.add((Number) object[1]);
				cont++;
			}

			VendaPorCategoria vendaPorCategoria = new VendaPorCategoria();
			vendaPorCategoria.setItem(object[0].toString());
			vendaPorCategoria.setValue((Number) object[1]);
			vendaPorCategoria.setQuantidade((Number) object[2]);

			totalValorEstoque += vendaPorCategoria.getValue().doubleValue();
			totalItensEstoque += vendaPorCategoria.getQuantidade().doubleValue();

			detalhesEstoqueParaVendaPorCategoria.add(vendaPorCategoria);
		}

		this.totalValorEstoque = nf.format(totalValorEstoque);
		this.totalItensEstoque = String.valueOf(totalItensEstoque);

		dataSet.setData(values);

		List<String> bgColors = new ArrayList<>();
		//bgColors.add("rgb(255, 99, 132)");
		//bgColors.add("rgb(75, 192, 192)");
		//bgColors.add("rgb(255, 205, 86)");
		//bgColors.add("rgb(201, 203, 207)");
		//bgColors.add("rgb(249, 24, 24)");
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
		for (Object[] object : totalParaVendasPorCategoria) {
			if (cont < 5) {
				labels.add((String) object[0]);
				cont++;
			}
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

		Number totalVendas = vendas.totalVendas(usuario.getEmpresa());
		Number totalCompras = compras.totalCompras(usuario.getEmpresa());

		Number totalDespesasPagas = contas.porContasPagas("DEBITO", "LANCAMENTO", usuario.getEmpresa()); /* errado */

		Number totalDebitosPagos = contas.porContasPagas("DEBITO", usuario.getEmpresa()); 
		Number totalCreditosPagos = contas.porContasPagas("CREDITO", usuario.getEmpresa());

		// Number totalDebitos = lancamentos.totalDebitos();
		// Number totalCreditos = lancamentos.totalCreditos();

		List<Number> values = new ArrayList<>();
		values.add((totalVendas.doubleValue() + totalCreditosPagos.doubleValue())
				- (totalDebitosPagos.doubleValue()/* + totalCompras.doubleValue()*/));// Em
		// Caixa
		values.add(totalVendas);// Vendas
		values.add(totalCreditosPagos);// Receitas
		// values.add(0);//Contas à Receber

		values.add(totalCompras);// Compras
		values.add(totalDespesasPagas);// Despesas
		// values.add(0);//Contas à Pagar
		Number avender = produtos.totalAVender(usuario.getEmpresa());
		values.add(avender);// À Vender

		barDataSet.setData(values);

		tabela = new ArrayList<FluxoDeCaixa>();
		FluxoDeCaixa fluxoDeCaixa = new FluxoDeCaixa();
		fluxoDeCaixa.setItem("Caixa");
		fluxoDeCaixa.setValue((totalVendas.doubleValue() + totalCreditosPagos.doubleValue())
				- (totalDebitosPagos.doubleValue()/* + totalCompras.doubleValue()*/));
		tabela.add(fluxoDeCaixa);

		double saldo = (totalVendas.doubleValue() + totalCreditosPagos.doubleValue())
				- (totalDebitosPagos.doubleValue()/* + totalCompras.doubleValue()*/);
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
		fluxoDeCaixa.setValue(avender.doubleValue());
		tabela.add(fluxoDeCaixa);

		List<String> bgColor = new ArrayList<>();
		bgColor.add("rgba(75, 192, 192, 0.2)");
		bgColor.add("rgba(255, 159, 64, 0.2)");
		bgColor.add("rgba(255, 205, 86, 0.2)");	
		bgColor.add("rgba(54, 162, 235, 0.2)");
		bgColor.add("rgba(255, 99, 132, 0.2)");
		bgColor.add("rgba(201, 203, 207, 0.2)");
		barDataSet.setBackgroundColor(bgColor);

		List<String> borderColor = new ArrayList<>();
		borderColor.add("rgb(75, 192, 192)");
		borderColor.add("rgb(255, 159, 64)");
		borderColor.add("rgb(255, 205, 86)");	
		borderColor.add("rgb(54, 162, 235)");
		borderColor.add("rgb(255, 99, 132)");
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

		values.add(vendas.totalVendasPorDiaDaSemana(1L, usuario.getEmpresa()));
		values.add(vendas.totalVendasPorDiaDaSemana(2L, usuario.getEmpresa()));
		values.add(vendas.totalVendasPorDiaDaSemana(3L, usuario.getEmpresa()));
		values.add(vendas.totalVendasPorDiaDaSemana(4L, usuario.getEmpresa()));
		values.add(vendas.totalVendasPorDiaDaSemana(5L, usuario.getEmpresa()));
		values.add(vendas.totalVendasPorDiaDaSemana(6L, usuario.getEmpresa()));
		values.add(vendas.totalVendasPorDiaDaSemana(7L, usuario.getEmpresa()));

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

		detalhesVendasPorCategoria = new ArrayList<>();

		int cont = 0;
		double totalValorVenda = 0;
		long totalItensVenda = 0;

		List<Object[]> result = vendas.totalVendasPorCategoria(usuario.getEmpresa());
		for (Object[] object : result) {

			if (cont < 5) {
				values.add((Number) object[1]);
				cont++;
			}

			VendaPorCategoria vendaPorCategoria = new VendaPorCategoria();

			vendaPorCategoria.setItem(object[0].toString());
			vendaPorCategoria.setValue((Number) object[1]);
			vendaPorCategoria.setQuantidade((Number) object[2]);

			totalValorVenda += vendaPorCategoria.getValue().doubleValue();
			totalItensVenda += vendaPorCategoria.getQuantidade().doubleValue();

			detalhesVendasPorCategoria.add(vendaPorCategoria);
		}

		this.totalValorVenda = nf.format(totalValorVenda);
		this.totalItensVenda = String.valueOf(totalItensVenda);

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

	public void buscarVendaPorCategoriaSelecionada() {
		detalhesVendasPorProduto = new ArrayList<>();

		List<Object[]> produtos = vendas.totalVendasPorProduto(vendaPorCategoriaSelecionada.getItem(), usuario.getEmpresa());
		for (Object[] object : produtos) {
			VendaPorCategoria vendaPorCategoria = new VendaPorCategoria();
			vendaPorCategoria.setItem(object[0].toString());
			vendaPorCategoria.setValue((Number) object[1]);
			vendaPorCategoria.setQuantidade((Number) object[2]);
			vendaPorCategoria.setCodigo(object[3].toString());

			detalhesVendasPorProduto.add(vendaPorCategoria);
		}
	}

	public void buscarEstoquePorCategoriaSelecionada() {
		detalhesEstoquePorProduto = new ArrayList<>();

		List<Object[]> produtos = vendas.totalParaVendasPorProduto(estoquePorCategoriaSelecionada.getItem(), usuario.getEmpresa());
		for (Object[] object : produtos) {
			VendaPorCategoria vendaPorCategoria = new VendaPorCategoria();
			vendaPorCategoria.setItem(object[0].toString());
			vendaPorCategoria.setValue((Number) object[1]);
			vendaPorCategoria.setQuantidade((Number) object[2]);
			vendaPorCategoria.setCodigo(object[3].toString());

			detalhesEstoquePorProduto.add(vendaPorCategoria);
		}
	}

	public void carregarProduto(String codigo) {
		produto = produtos.porCodigo(codigo, usuario.getEmpresa());
		//fileContent = produto.getFoto();
		produtoId = produto.getId();
	}
	
	public void baixarLista() {

		ListaProduto listaProduto = new ListaProduto();
		listaProduto.setCategoria(estoquePorCategoriaSelecionada.getItem());
		listaProduto.setValorEmEstoque(nf.format(estoquePorCategoriaSelecionada.getValue()));
		listaProduto.setTotalDeItens(String.valueOf(estoquePorCategoriaSelecionada.getQuantidade()));
		
		for (VendaPorCategoria produto : detalhesEstoquePorProduto) {
			
			Produto produtoTemp = new Produto();
			produtoTemp.setCodigo(produto.getCodigo());
			produtoTemp.setDescricao(produto.getItem());
			produtoTemp.setValor(nf.format(produto.getValue()));
			produtoTemp.setQuantidade(String.valueOf(produto.getQuantidade()));
			
			listaProduto.getListaDeItens().add(produtoTemp);
		}

		List<ListaProduto> listaProdutos = new ArrayList<>();
		listaProdutos.add(listaProduto);

		Relatorio<ListaProduto> report = new Relatorio<ListaProduto>();
		try {
			report.getRelatorioEstoque(listaProdutos, estoquePorCategoriaSelecionada.getItem());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public List<VendaPorCategoria> getDetalhesVendasPorCategoria() {
		return detalhesVendasPorCategoria;
	}

	public List<VendaPorCategoria> getDetalhesEstoqueParaVendaPorCategoria() {
		return detalhesEstoqueParaVendaPorCategoria;
	}

	public List<Top5Despesa> getTop5Despesas() {
		return top5Despesas;
	}

	public VendaPorCategoria getVendaPorCategoriaSelecionada() {
		return vendaPorCategoriaSelecionada;
	}

	public void setVendaPorCategoriaSelecionada(VendaPorCategoria vendaPorCategoriaSelecionada) {
		this.vendaPorCategoriaSelecionada = vendaPorCategoriaSelecionada;
	}

	public List<VendaPorCategoria> getDetalhesVendasPorProduto() {
		return detalhesVendasPorProduto;
	}

	public VendaPorCategoria getEstoquePorCategoriaSelecionada() {
		return estoquePorCategoriaSelecionada;
	}

	public void setEstoquePorCategoriaSelecionada(VendaPorCategoria estoquePorCategoriaSelecionada) {
		this.estoquePorCategoriaSelecionada = estoquePorCategoriaSelecionada;
	}

	public List<VendaPorCategoria> getDetalhesEstoquePorProduto() {
		return detalhesEstoquePorProduto;
	}

	public String getTotalValorVenda() {
		return totalValorVenda;
	}

	public String getTotalItensVenda() {
		return totalItensVenda;
	}

	public String getTotalValorEstoque() {
		return totalValorEstoque;
	}

	public String getTotalItensEstoque() {
		return totalItensEstoque;
	}

	public String getTotalDespesasTop5() {
		return totalDespesasTop5;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public String getImageContentsAsBase64() {
		return Base64.getEncoder().encodeToString(fileContent);
	}

	public Long getProdutoId() {
		return produtoId;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}
	
	public static <T> Object getObjectSession(String attribute){		
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();  
		HttpSession session = request.getSession(true);  
		return session.getAttribute(attribute);				
	}

}
