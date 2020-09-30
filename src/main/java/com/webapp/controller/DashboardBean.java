package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.polar.PolarAreaChartDataSet;
import org.primefaces.model.charts.polar.PolarAreaChartModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.FluxoDeCaixa;
import com.webapp.model.Grupo;
import com.webapp.model.Lancamento;
import com.webapp.model.ListaProduto;
import com.webapp.model.OrdenaTop5;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Top5Despesa;
import com.webapp.model.Usuario;
import com.webapp.model.VendaPorCategoria;
import com.webapp.report.Relatorio;
import com.webapp.repository.CategoriasLancamentos;
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
	private CategoriasLancamentos categoriasLancamentos;

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
	
	private double saldo = 0;

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

	private FluxoDeCaixa caixa;

	private FluxoDeCaixa vendas_;

	private FluxoDeCaixa receitas;

	private FluxoDeCaixa compras_;

	private FluxoDeCaixa despesas;

	private FluxoDeCaixa avender_;
	
	
	private Number saldoLucroEmVendas;
	
	private Date dateStart = new Date();

	private Date dateStop = new Date();
	
	private Number totalVendasHojeValor;
	
	private Number totalVendasHojeQuantidade;
	
	private Number totalLucrosHoje;
	
	private Number totalEntregasPendentesValor;
	
	private Number totalEntregasPendentesQuantidade;
	
	private Number totalEntregasPendentesAReceber;
	
	private Number totalEntregasPendentesPagas;
	
	private Number alucrar;
	
	private String hoje;
	
	private String linkEntregas;
	
	private String totalEntregas;
	
	
	private Number totalDespesasHojeValor;
	
	private Number totalLancamentosDespesasHojeValor;
	
	private Number totalDespesasPagasHojeValor;
	
	private Number totalAPagarHojeValor;
	
	private Number totalAPagarValor;
	
	private Number totalAPagarEmAtrasoValor;
	
	
	private Number totalReceitasHojeValor;
	
	private Number totalLancamentosReceitasHojeValor;
	
	private Number totalReceitasPagasHojeValor;
	
	private Number totalVendasPagasHojeValor;
	
	private Number totalContasRecebidasHojeValor;
	
	private Number totalAReceberHojeValor;
	
	private Number totalAReceberValor;
	
	private Number totalAReceberEmAtrasoValor;
	
	
	private BarChartModel mixedModelPorDia;
	
	private BarChartModel mixedModelPorDia_;
	
	private BarChartModel mixedModelPorSemana;
	
	
	private String ano01;
	
	private String semana01;

	private String semana02;
	
	
	private Double valorRetirada;
	
	

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
			
			createMixedModelPorDia();
			
			createMixedModelPorDia_();
			
			createMixedModelPorSemana();
			
			
			CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome("Retirada de lucro", null);
			Number totalDeRetiradas = lancamentos.totalDeRetiradas(usuario.getEmpresa(), categoriaLancamento);
			
			Number saldoLucroEmVendasAPagarPagas = contas.lucroEmVendasAPagarPagas("CREDITO", "VENDA", usuario.getEmpresa());
			Number saldoLucroEmVendasAVistaPagas = vendas.totalLucros(usuario.getEmpresa()).doubleValue();
			Number saldoEstorno = produtos.saldoEstorno(usuario.getEmpresa());
			
			
			saldoLucroEmVendas = ((saldoLucroEmVendasAVistaPagas.doubleValue() + saldoLucroEmVendasAPagarPagas.doubleValue()) + saldoEstorno.doubleValue()) - totalDeRetiradas.doubleValue();
			
			if(usuario.getEmpresa().equals("Decore")) {
				saldoLucroEmVendas = 0D;
			}
			
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(dateStart);
			calendarStart = DateUtils.truncate(calendarStart, Calendar.DAY_OF_MONTH);
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
			hoje = sdf.format(calendarStart.getTime());


			Calendar calendarStop = Calendar.getInstance();
			calendarStop.setTime(dateStop);
			calendarStop.add(Calendar.DAY_OF_MONTH, 1);
			calendarStop = DateUtils.truncate(calendarStop, Calendar.DAY_OF_MONTH);
			
			
			
			totalVendasHojeValor = vendas.totalVendasPorDiaValor(calendarStart.getTime(), calendarStop.getTime(), usuario.getEmpresa());
			
			totalVendasHojeQuantidade = vendas.totalVendasPorDiaQuantidade(calendarStart.getTime(), calendarStop.getTime(), usuario.getEmpresa());
			
			Number totalEstornosHoje = vendas.totalEstornosPorDia(calendarStart.getTime(), calendarStop.getTime(), usuario.getEmpresa());
			
			Number totalLucrosHojeTemp = vendas.totalLucrosPorDia(calendarStart.getTime(), calendarStop.getTime(), usuario.getEmpresa());
			
			totalLucrosHoje = totalLucrosHojeTemp.doubleValue() + totalEstornosHoje.doubleValue();
			
			
			
			totalEntregasPendentesValor = vendas.totalEntregasPendentesValor(usuario.getEmpresa());
			
			totalEntregasPendentesQuantidade = vendas.totalEntregasPendentesQuantidade(usuario.getEmpresa());
			
			totalEntregasPendentesAReceber = vendas.totalEntregasPendentesAReceber(usuario.getEmpresa());
			
			totalEntregasPendentesPagas = vendas.totalEntregasPendentesPagas(usuario.getEmpresa());
			
			
			linkEntregas = "";
			totalEntregas = "";
			
			if(totalEntregasPendentesQuantidade.doubleValue() > 0) {
				
				
				if(totalEntregasPendentesQuantidade.doubleValue() > 1) {
					linkEntregas = totalEntregasPendentesQuantidade.intValue() + " entregas";
				} else {
					linkEntregas = totalEntregasPendentesQuantidade.intValue() + " entrega";
				}
				
				
				String aReceber = "";
				
				if(totalEntregasPendentesAReceber.doubleValue() > 0) {
					aReceber = totalEntregasPendentesAReceber.intValue() + " à receber";
				}
				
				
				String paga = "";
				
				if(totalEntregasPendentesPagas.doubleValue() > 0) {
					paga = totalEntregasPendentesPagas.intValue() + " paga";
				}
				
				String separador = (!aReceber.equals("") && !paga.equals("")) ? " / " : "";
				
				totalEntregas = "( " + aReceber + separador + paga + " )";
							
			} else {
				linkEntregas = "0 entrega";
			}
			
			
			totalEntregas = totalEntregas.equals("(  )") ? "" : totalEntregas;
			
						
			
			
			calendarStop.add(Calendar.DAY_OF_MONTH, -1);
			calendarStop.set(Calendar.HOUR, 23);
			calendarStop.set(Calendar.MINUTE, 59);
			calendarStop.set(Calendar.SECOND, 59);
			
			
			totalVendasPagasHojeValor = contas.totalVendasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalReceitasPagasHojeValor = contas.totalReceitasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			
			totalContasRecebidasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalVendasPagasHojeValor.doubleValue();
			
			
			totalLancamentosReceitasHojeValor = lancamentos.totalLancamentosReceitasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalReceitasHojeValor = totalReceitasPagasHojeValor.doubleValue() + totalLancamentosReceitasHojeValor.doubleValue();
			
			
			totalAReceberHojeValor = contas.totalAReceberPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalAReceberValor = contas.totalAReceberValor(usuario.getEmpresa());
			
			
			totalAReceberEmAtrasoValor = contas.totalAReceberEmAtrasoValor(usuario.getEmpresa(), calendarStart);
			
			
			
			
			
			totalDespesasPagasHojeValor = contas.totalDespesasPagasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalLancamentosDespesasHojeValor = lancamentos.totalLancamentosDespesasPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalDespesasHojeValor = totalDespesasPagasHojeValor.doubleValue() + totalLancamentosDespesasHojeValor.doubleValue();
			
			
			totalAPagarHojeValor = contas.totalAPagarPorDiaValor(calendarStart, calendarStop, usuario.getEmpresa());
			
			totalAPagarValor = contas.totalAPagarValor(usuario.getEmpresa());
			
			
			totalAPagarEmAtrasoValor = contas.totalAPagarEmAtrasoValor(usuario.getEmpresa(), calendarStart);
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
		
		
		despesasTemp = lancamentos.totalDespesasPorCategoriaMesAtual();

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

		
		Number vendasAvistaPagas = vendas.vendasAvistaPagas(usuario.getEmpresa());
		Number vendasAPagarPagas = contas.vendasAPagarPagas("CREDITO", "VENDA", usuario.getEmpresa());
		
		Number totalVendas_ = vendasAvistaPagas.doubleValue() + vendasAPagarPagas.doubleValue();
		Number totalVendas = totalVendas_;//vendas.totalVendas(usuario.getEmpresa());
		
		
		Number comprasAvistaPagas = compras.comprasAvistaPagas(usuario.getEmpresa());
		Number comprasAPagarPagas = contas.porContasPagas("DEBITO", "COMPRA", usuario.getEmpresa());
		
		Number totalCompras_ = comprasAvistaPagas.doubleValue() + comprasAPagarPagas.doubleValue();	
		Number totalCompras = totalCompras_;//compras.totalCompras(usuario.getEmpresa());
		
		
		Number totalComprasVendidas = vendas.totalComprasVendidas(usuario.getEmpresa());
		Number totalLucros = vendas.totalLucros(usuario.getEmpresa());

		
		
		Number despesasAvistaPagas = lancamentos.totalDespesasAvistaPagas(usuario.getEmpresa()); 
		Number despesasAPagarPagas = contas.porContasPagas("DEBITO", "LANCAMENTO", usuario.getEmpresa());
		Number totalDespesasPagas = despesasAvistaPagas.doubleValue() + despesasAPagarPagas.doubleValue();		

		Number totalDebitosPagos = totalDespesasPagas.doubleValue() + totalCompras.doubleValue();//contas.porContasPagas("DEBITO", usuario.getEmpresa()); 
		
		
		Number receitasAvistaPagas = lancamentos.totalReceitasAvistaPagas(usuario.getEmpresa()); 
		Number receitasAReceberPagas = contas.porContasPagas("CREDITO", "LANCAMENTO", usuario.getEmpresa());
		Number totalReceitasPagas = receitasAReceberPagas.doubleValue() + receitasAvistaPagas.doubleValue();
		
		Number totalReceitas = totalReceitasPagas;

		
		
		List<Number> values = new ArrayList<>();
		
		
		if(usuario.getEmpresa().equals("Lucro")) {			
			values.add((totalVendas.doubleValue() + totalReceitas.doubleValue()) - totalComprasVendidas.doubleValue()
					- (totalDespesasPagas.doubleValue()/*totalDebitosPagos.doubleValue()*//* + totalCompras.doubleValue()*/));
		} else {
			values.add((totalVendas.doubleValue() + totalReceitas.doubleValue()) 
					- (totalDebitosPagos.doubleValue()/* + totalCompras.doubleValue()*/));		
		}
		
		// Em Caixa
		values.add(totalVendas);// Vendas
		values.add(totalReceitas);// Receitas
		// values.add(0);//Contas à Receber

		values.add(totalCompras);// Compras
		
		CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome("Retirada de lucro", null);
		Number totalDeRetiradas = lancamentos.totalDeRetiradas(usuario.getEmpresa(), categoriaLancamento);
		values.add(totalDespesasPagas.doubleValue() - totalDeRetiradas.doubleValue());// Despesas
		
		// values.add(0);//Contas à Pagar
		Number avender = produtos.totalAVender(usuario.getEmpresa());
		values.add(avender);// À Vender
		
		
		alucrar = avender.doubleValue() - produtos.totalCustoMedio(usuario.getEmpresa()).doubleValue();
		

		barDataSet.setData(values);

		tabela = new ArrayList<FluxoDeCaixa>();
		caixa = new FluxoDeCaixa();
		caixa.setItem("Caixa");
		
		if(usuario.getEmpresa().equals("Lucro")) {			
			caixa.setValue((totalVendas.doubleValue() + totalReceitas.doubleValue()) - totalComprasVendidas.doubleValue()
					- (totalDespesasPagas.doubleValue()/*totalDebitosPagos.doubleValue()*//* + totalCompras.doubleValue()*/));			
		} else {
			caixa.setValue((totalVendas.doubleValue() + totalReceitas.doubleValue())
					- (totalDebitosPagos.doubleValue()/* + totalCompras.doubleValue()*/));		
		}
		
		tabela.add(caixa);

		saldo = 0;
		
		if(usuario.getEmpresa().equals("Lucro")) {			
			saldo = (totalVendas.doubleValue() + totalReceitas.doubleValue()) - totalComprasVendidas.doubleValue()
					- (totalDespesasPagas.doubleValue()/*totalDebitosPagos.doubleValue()*//* + totalCompras.doubleValue()*/);			
		} else {
			saldo = (totalVendas.doubleValue() + totalReceitas.doubleValue())
					- (totalDebitosPagos.doubleValue()/* + totalCompras.doubleValue()*/);			
		}
		
		saldoGeral = nf.format(saldo);

		vendas_ = new FluxoDeCaixa();
		vendas_.setItem("Vendas");
		vendas_.setValue(totalVendas.doubleValue());
		tabela.add(vendas_);

		receitas = new FluxoDeCaixa();
		receitas.setItem("Receitas");
		receitas.setValue(totalReceitas.doubleValue());
		tabela.add(receitas);
		/*
		 * fluxoDeCaixa = new FluxoDeCaixa(); fluxoDeCaixa.setItem("Contas à Receber");
		 * fluxoDeCaixa.setValue(0D); tabela.add(fluxoDeCaixa);
		 */

		compras_ = new FluxoDeCaixa();
		compras_.setItem("Compras");
		compras_.setValue(totalCompras.doubleValue());
		tabela.add(compras_);

		despesas = new FluxoDeCaixa();
		despesas.setItem("Despesas");
		despesas.setValue(totalDespesasPagas.doubleValue());
		tabela.add(despesas);
		/*
		 * fluxoDeCaixa = new FluxoDeCaixa(); fluxoDeCaixa.setItem("Contas à Pagar");
		 * fluxoDeCaixa.setValue(0D); tabela.add(fluxoDeCaixa);
		 */

		avender_ = new FluxoDeCaixa();
		avender_.setItem("À Vender");
		avender_.setValue(avender.doubleValue());
		tabela.add(avender_);

		List<String> bgColor = new ArrayList<>();
		
		if(saldo < 0) {
			bgColor.add("rgba(255, 99, 132, 0.2)");
		} else {
			bgColor.add("rgba(75, 192, 192, 0.2)");
		}
		
		bgColor.add("rgba(255, 159, 64, 0.2)");
		bgColor.add("rgba(255, 205, 86, 0.2)");	
		bgColor.add("rgba(54, 162, 235, 0.2)");
		bgColor.add("rgba(255, 99, 132, 0.2)");
		bgColor.add("rgba(201, 203, 207, 0.2)");
		barDataSet.setBackgroundColor(bgColor);

		List<String> borderColor = new ArrayList<>();
		
		if(saldo < 0) { 
			borderColor.add("rgb(255, 99, 132)");
		} else {
			borderColor.add("rgb(75, 192, 192)");
		}
		
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
	

	public FluxoDeCaixa getCaixa() {
		return caixa;
	}

	public FluxoDeCaixa getVendas_() {
		return vendas_;
	}

	public FluxoDeCaixa getReceitas() {
		return receitas;
	}

	public FluxoDeCaixa getCompras_() {
		return compras_;
	}

	public FluxoDeCaixa getDespesas() {
		return despesas;
	}

	public FluxoDeCaixa getAvender_() {
		return avender_;
	}

	public Number getSaldoLucroEmVendas() {
		return saldoLucroEmVendas;
	}

	public Number getTotalVendasHojeValor() {
		return totalVendasHojeValor;
	}

	public Number getTotalVendasHojeQuantidade() {
		return totalVendasHojeQuantidade;
	}

	public Number getTotalLucrosHoje() {
		return totalLucrosHoje;
	}

	public Number getAlucrar() {
		return alucrar;
	}

	public String getHoje() {
		return hoje;
	}

	public Number getTotalEntregasPendentesValor() {
		return totalEntregasPendentesValor;
	}

	public Number getTotalEntregasPendentesQuantidade() {
		return totalEntregasPendentesQuantidade;
	}

	public Number getTotalEntregasPendentesAReceber() {
		return totalEntregasPendentesAReceber;
	}

	public Number getTotalEntregasPendentesPagas() {
		return totalEntregasPendentesPagas;
	}

	public String getLinkEntregas() {
		return linkEntregas;
	}

	public String getTotalEntregas() {
		return totalEntregas;
	}

	public Number getTotalDespesasHojeValor() {
		return totalDespesasHojeValor;
	}

	public Number getTotalLancamentosDespesasHojeValor() {
		return totalLancamentosDespesasHojeValor;
	}

	public Number getTotalDespesasPagasHojeValor() {
		return totalDespesasPagasHojeValor;
	}

	public Number getTotalAPagarEmAtrasoValor() {
		return totalAPagarEmAtrasoValor;
	}

	public Number getTotalAPagarHojeValor() {
		return totalAPagarHojeValor;
	}

	public Number getTotalAPagarValor() {
		return totalAPagarValor;
	}

	public Number getTotalReceitasHojeValor() {
		return totalReceitasHojeValor;
	}

	public Number getTotalLancamentosReceitasHojeValor() {
		return totalLancamentosReceitasHojeValor;
	}

	public Number getTotalReceitasPagasHojeValor() {
		return totalReceitasPagasHojeValor;
	}

	public Number getTotalAReceberHojeValor() {
		return totalAReceberHojeValor;
	}

	public Number getTotalAReceberValor() {
		return totalAReceberValor;
	}

	public Number getTotalAReceberEmAtrasoValor() {
		return totalAReceberEmAtrasoValor;
	}
	
	
	public BarChartModel getMixedModelPorDia() {
		return mixedModelPorDia;
	}
	

	public void createMixedModelPorDia() {	
		
		mixedModelPorDia = new BarChartModel();
		ChartData data = new ChartData();

		LineChartDataSet dataSet = new LineChartDataSet();
		List<Number> values = new ArrayList<>();

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.add(Calendar.DAY_OF_MONTH, -3);
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
				Calendar calendarTemp = Calendar.getInstance();
				calendarTemp.setTime(calendarStartTemp.getTime());
				Calendar calendarStopTemp = calendarTemp;
				calendarStopTemp.set(Calendar.HOUR, 23);
				calendarStopTemp.set(Calendar.MINUTE, 59);
				calendarStopTemp.set(Calendar.SECOND, 59);

				List<Object[]> resultTemp = new ArrayList<>();
				List<Object[]> resultDespesasPagas = contas.totalDespesasPagasPorData(calendarStartTemp, calendarStopTemp, usuario.getEmpresa());
				List<Object[]> resultReceitasPagas = contas.totalReceitasPagasPorData(calendarStartTemp, calendarStopTemp, usuario.getEmpresa());
				
				List<Object[]> resultLancamentoDespesas = lancamentos.totalLancamentosDespesasPorData(calendarStartTemp, calendarStopTemp, usuario.getEmpresa());
				List<Object[]> resultLancamentoReceitas = lancamentos.totalLancamentosReceitasPorData(calendarStartTemp, calendarStopTemp, usuario.getEmpresa());

				resultTemp.addAll(resultDespesasPagas);
				resultTemp.addAll(resultLancamentoDespesas);
				resultTemp.addAll(resultReceitasPagas);
				resultTemp.addAll(resultLancamentoReceitas);
				
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
							valor += new BigDecimal(object[5].toString()).doubleValue();
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
		


		dataSet.setData(values);
		dataSet.setLabel("Despesas");
		dataSet.setBorderColor("rgb(201, 203, 207)");
		dataSet.setBackgroundColor("rgba(255, 99, 132, 0.2)");
		data.addChartDataSet(dataSet);

		dataSet2.setData(values2);
		dataSet2.setLabel("Receitas");
		dataSet2.setBorderColor("rgb(75, 192, 192)");
		dataSet2.setBackgroundColor("rgba(75, 192, 192, 0.2)");
		data.addChartDataSet(dataSet2);

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

		mixedModelPorDia.setExtender("percentExtender2_");
	}
	
	
	public void createMixedModelPorDia_() {
		mixedModelPorDia_ = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.add(Calendar.DAY_OF_MONTH, -3);
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
						null, null, null, null, true, usuario.getEmpresa());
				
				Number totalEstornosHoje = vendas.totalEstornosPorDia(calendarStartTemp.getTime(), calendarStopTemp.getTime(), usuario.getEmpresa());

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
						object[3] = ((BigDecimal)object[3]).doubleValue() + totalEstornosHoje.doubleValue();
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
		
		boolean percentualDiario = false;

		for (Object[] object : result) {
			Double totalDeLucroEmVendas = ((Number) object[3]).doubleValue();

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

			
			percentualDiario = true;
			// if (totalDeVendas > 0 || totalCompras > 0) {
			values.add(totalVendasComPrecoVenda/* - totalDeCompras */);

			values2.add(totalDeLucroEmVendas);

			labels.add(object[0] + "/" + object[1]/* + "/" + object[2] */);
			// }

			System.out.println(object[3]);
		}

		dataSet.setData(values);
		dataSet.setLabel("Vendas");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235, 0.6)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Lucro");
		dataSet2.setYaxisID("right-y-axis");
		// dataSet2.setFill(false);
		dataSet2.setBorderColor("rgba(255, 159, 64)");
		
		
		data.addChartDataSet(dataSet);
		
		data.addChartDataSet(dataSet2);		
		

		data.setLabels(labels);

		mixedModelPorDia_.setData(data);

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

		mixedModelPorDia_.setOptions(options);

		mixedModelPorDia_.setExtender("percentExtender_");
	}
	
	public void createMixedModelPorSemana() {
		
		Calendar calendar = Calendar.getInstance();
		Calendar calendarTemp = Calendar.getInstance();

		ano01 = String.valueOf(calendar.get(Calendar.YEAR));
		
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
		
		mixedModelPorSemana = new BarChartModel();
		ChartData data = new ChartData();

		BarChartDataSet dataSet = new BarChartDataSet();
		List<Number> values = new ArrayList<>();

		List<String> labels = new ArrayList<>();

		List<Object[]> result = new ArrayList<>();
		if (Integer.parseInt(this.semana01.replace("W", "")) <= Integer.parseInt(semana02.replace("W", ""))) {

			for (int i = Integer.parseInt(this.semana01.replace("W", "")); i <= Integer
					.parseInt(semana02.replace("W", "")); i++) {

				String semana01_ = "W";
				if (i < 10) {
					semana01_ += "0" + i;
				} else {
					semana01_ += i;
				}

				System.out.println(semana01);

				List<Object[]> resultTemp = vendas.totalLucrosPorSemana(ano01, semana01_, semana01_, null,
						null, null, null, true, usuario.getEmpresa());
				
				Number totalEstornos = vendas.totalEstornosPorSemana(ano01, semana01_, semana01_, usuario.getEmpresa());

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
						object[2] = ((BigDecimal)object[2]).doubleValue() + totalEstornos.doubleValue();
						result.add(object);
					}
				}
			}

			System.out.println("result.size(): " + result.size());
		}

		LineChartDataSet dataSet2 = new LineChartDataSet();
		List<Number> values2 = new ArrayList<>();

		for (Object[] object : result) {

			Double totalDeLucroEmVendas = ((Number) object[2]).doubleValue();
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
		
			// if (totalDeVendas > 0 || totalCompras > 0) {
			values.add(totalVendasComPrecoVenda/* - totalDeCompras */);

			values2.add(totalDeLucroEmVendas);
			

			long semana = Long.parseLong(object[0].toString());
			String semanaTemp = String.valueOf(semana);
			if (semana < 10) {
				semanaTemp = "0" + semana;
			}
			labels.add("W" + semanaTemp);
			// }
		}

		dataSet.setData(values);
		dataSet.setLabel("Vendas");
		dataSet.setYaxisID("left-y-axis");
		dataSet.setBorderColor("rgb(54, 162, 235)");
		dataSet.setBackgroundColor("rgba(54, 162, 235, 0.6)");

		dataSet2.setData(values2);
		dataSet2.setLabel("Lucro");
		dataSet2.setYaxisID("right-y-axis");
		// dataSet2.setFill(false);
		dataSet2.setBorderColor("rgba(255, 159, 64)");

		data.addChartDataSet(dataSet);
		data.addChartDataSet(dataSet2);

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

		mixedModelPorSemana.setExtender("percentExtender_");
	}

	public BarChartModel getMixedModelPorDia_() {
		return mixedModelPorDia_;
	}

	public BarChartModel getMixedModelPorSemana() {
		return mixedModelPorSemana;
	}

	public Number getTotalVendasPagasHojeValor() {
		return totalVendasPagasHojeValor;
	}

	public Number getTotalContasRecebidasHojeValor() {
		return totalContasRecebidasHojeValor;
	}
	
	public Double getValorRetirada() {
		return valorRetirada;
	}

	public void setValorRetirada(Double valorRetirada) {
		this.valorRetirada = valorRetirada;
	}

	public void retirarLucro() {
		
		if(valorRetirada != null && valorRetirada > 0) {
			if(valorRetirada.doubleValue() <= saldoLucroEmVendas.doubleValue()) {
				
				Lancamento lancamento = new Lancamento();
				Lancamento lancamentoTemp = lancamentos.ultimoNLancamento(usuario.getEmpresa());

				if (lancamentoTemp == null) {
					lancamento.setNumeroLancamento(1L);
				} else {
					if (lancamento.getId() == null) {
						lancamento.setNumeroLancamento(lancamentoTemp.getNumeroLancamento() + 1);
					}
				}

				Calendar calendario = Calendar.getInstance();
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(lancamento.getDataLancamento());
				calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
				calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
				calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
				lancamento.setDataLancamento(calendarioTemp.getTime());

				lancamento.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				lancamento.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				lancamento.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				lancamento.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				lancamento.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));			
				
				lancamento.setConta(false);
				
				lancamento.setUsuario(usuario);
				
				CategoriaLancamento categoriaLancamento = categoriasLancamentos.porNome("Retirada de lucro", null);
				lancamento.setCategoriaLancamento(categoriaLancamento);
				
				lancamento.setDescricao(categoriaLancamento.getNome());
				lancamento.setValor(new BigDecimal(valorRetirada));
				
				lancamento.setDestinoLancamento(null);

				lancamento.setEmpresa(usuario.getEmpresa());
				lancamentos.save(lancamento);
				
				
				
				Number totalDeRetiradas = lancamentos.totalDeRetiradas(usuario.getEmpresa(), categoriaLancamento);
				
				Number saldoLucroEmVendasAPagarPagas = contas.lucroEmVendasAPagarPagas("CREDITO", "VENDA", usuario.getEmpresa());
				Number saldoLucroEmVendasAVistaPagas = vendas.totalLucros(usuario.getEmpresa()).doubleValue();
				Number saldoEstorno = produtos.saldoEstorno(usuario.getEmpresa());

				saldoLucroEmVendas = ((saldoLucroEmVendasAVistaPagas.doubleValue() + saldoLucroEmVendasAPagarPagas.doubleValue()) + saldoEstorno.doubleValue()) - totalDeRetiradas.doubleValue();
				
				saldo = saldo - valorRetirada;
				saldoGeral = nf.format(saldo);
				
				valorRetirada = 0D;
				
				createBarModel();
				
				PrimeFaces.current().executeScript(
						"PF('downloadLoading').hide();PF('btn-save').enable();PF('retira-dialog').hide();swal({ type: 'success', title: 'Parabéns!', text: 'Retirada realizada com sucesso!' });");
				valorRetirada = null;
			} else {
				
				PrimeFaces.current().executeScript(
						"PF('downloadLoading').hide();PF('btn-save').enable();swal({ type: 'error', title: 'Algo deu errado!', text: 'Saldo insuficiente para retirada!' });");
				valorRetirada = null;
			}
		} else {
			
			PrimeFaces.current().executeScript(
					"PF('downloadLoading').hide();PF('btn-save').enable();swal({ type: 'error', title: 'Algo deu errado!', text: 'Por favor, informe um valor maior que 0!' });");
			valorRetirada = null;
		}

	}

	public Usuario getUsuario() {
		return usuario;
	}

}
