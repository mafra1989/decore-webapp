package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Caixa;
import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Conta;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.FormaPagamento;
import com.webapp.model.ItemCaixa;
import com.webapp.model.Lancamento;
import com.webapp.model.Log;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.PagamentoConta;
import com.webapp.model.PeriodoPagamento;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoConta;
import com.webapp.model.TipoLancamento;
import com.webapp.model.TipoOperacao;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.repository.Caixas;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Contas_;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.PagamentosContas;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RegistroLancamentosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Lancamento lancamento;

	@Inject
	private Lancamento despesa;

	@Inject
	private Lancamento receita;

	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuario usuario_;

	@Inject
	private CategoriasLancamentos categoriasLancamentos;

	private List<TipoLancamento> todosTiposDespesas;

	private List<CategoriaLancamento> todasCategoriasDespesas;

	private List<CategoriaLancamento> todasCategoriasReceitas;

	@Inject
	private CategoriaLancamento categoriaLancamentoDespesa;

	@Inject
	private CategoriaLancamento categoriaLancamentoReceita;

	@Inject
	private DestinosLancamentos destinosLancamentos;

	private List<DestinoLancamento> todosDestinosLancamentos;
	
	private List<Usuario> todosUsuarios;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private TipoConta tipoConta;

	private Integer activeIndex = 0;

	@Inject
	private Contas_ contas;

	private List<Conta> entradas = new ArrayList<>();
	
	private List<Conta> todasContas = new ArrayList<>();

	private List<Conta> todasContas_ = new ArrayList<>();

	private TipoPagamento tipoPagamento = TipoPagamento.AVISTA;

	@NotNull
	private Long parcelas = 2L;

	@NotNull
	private Long parcelas_ = 2L;

	private PeriodoPagamento periodoPagamento = PeriodoPagamento.MESES;

	private boolean contaAPagar = true;

	private boolean lancamentoPago = true;

	private boolean repetirLancamento;

	private Double valorEntrada;
	
	private boolean renderFavorecido;
	
	private String option;
	
	
	private boolean parcelasConfirmadas;
	
	private Date primeiraParcela;
	
	private String primeiraParcelaEmString;
	
	
	private Date pagamentoPara;
	
	private String pagamentoParaEmString;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	
	private boolean parcelasConfirmadas_;
	
	private Date vencimento;
	
	private String vencimentoEmString;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	@Inject
	private Caixas caixas;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	
	@Inject
	private Logs logs;

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);
	
	@Inject
	private PagamentosContas pagamentosContas;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();			
			usuario_ = usuarios.porLogin(user.getUsername());
			
			todasCategoriasDespesas = categoriasLancamentos.todasDespesas(usuario_.getEmpresa());
			todasCategoriasReceitas = categoriasLancamentos.todasReceitas(usuario_.getEmpresa());

			todosDestinosLancamentos = destinosLancamentos.todos(usuario_.getEmpresa());

			Calendar calendar = Calendar.getInstance(BRAZIL);
			despesa.setDataLancamento(calendar.getTime());
			receita.setDataLancamento(calendar.getTime());
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
			
			if(option != null) {
				if(option.equalsIgnoreCase("despesa")) {
					activeIndex = 0;
				} else if(option.equalsIgnoreCase("receita")) {
					activeIndex = 1;
				}
			}		
		}
	}

	public void buscar() {
		lancamento = lancamentos.porId(lancamento.getId());

		if (lancamento.getId() != null) {
			if (lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
				categoriaLancamentoDespesa = lancamento.getCategoriaLancamento();
				despesa = lancamento;
				activeIndex = 0;
				
				if(lancamento.getUsuario() != null) {
					usuario = lancamento.getUsuario();	
					renderFavorecido = true;
				}
				
			} else {
				categoriaLancamentoReceita = lancamento.getCategoriaLancamento();
				receita = lancamento;
				activeIndex = 1;
			}
		}

	}
	
	
	public void preparaNovoLancamentoReceita() {
		receita.setDataLancamento(new Date());
		receita.setDescricao("");
		receita.setValor(null);
		categoriaLancamentoReceita = new CategoriaLancamento();
	}

	public void preparaNovoLancamentoDespesa() {
		despesa.setDataLancamento(new Date());
		despesa.setDescricao("");
		despesa.setValor(null);
		categoriaLancamentoDespesa = new CategoriaLancamento();
	}
	
	
	public void confirmarParcelas() {
		
		parcelasConfirmadas = true;
	}
	
	
	public void confirmarParcelas_() {
		
		parcelasConfirmadas_ = true;
	}
	
	
	public void changeTipoPagamento() {
		
		if (tipoPagamento == TipoPagamento.AVISTA) {
			
			contaAPagar = true;
			lancamentoPago = true;
			repetirLancamento = false;
			PrimeFaces.current().executeScript("ocultar();");
			PrimeFaces.current().executeScript("mostrarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
			
		} else {
			
			parcelasConfirmadas = false;
			contaAPagar = true;
			parcelas = 2L;
			periodoPagamento = PeriodoPagamento.MESES;
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lancamento.getDataLancamento());
			calendar.add(Calendar.MONTH, 1);
			
			primeiraParcela = calendar.getTime();
			
			primeiraParcelaEmString = sdf.format(calendar.getTime());
					
			PrimeFaces.current().executeScript("mostrar();");
			PrimeFaces.current().executeScript("ocultarValores();");
			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		}
	}


	
	public void changeStatusPago() {
		
		if (!lancamentoPago) {
			
			PrimeFaces.current().executeScript("mostrarParcelaUnica();");
			
			if(pagamentoPara == null) {
				pagamentoPara = lancamento.getDataLancamento();
				pagamentoParaEmString = sdf.format(pagamentoPara);
			}				
			
		} else {

			PrimeFaces.current().executeScript("ocultarParcelaUnica();");
		}
	}
	
	
	
	public void confirmarPrimeiraParcela() {
		
		primeiraParcelaEmString = sdf.format(primeiraParcela);
	}
	
	public void preparaPrimeiraParcela() throws ParseException {
		
		primeiraParcela = sdf.parse(primeiraParcelaEmString);
	}

	public void confirmarPagamentoPara() {
		
		pagamentoParaEmString = sdf.format(pagamentoPara);
	}

	public void preparaPagamentoPara() throws ParseException {
		
		pagamentoPara = sdf.parse(pagamentoParaEmString);
	}
	
	
	public void confirmarVencimento() {
		parcelasConfirmadas_ = false;
		vencimentoEmString = sdf.format(vencimento);
	}
	
	public void preparaVencimento() throws ParseException {
		
		vencimento = sdf.parse(vencimentoEmString);
	}
	

	public void changeRepetir() {
		
		parcelasConfirmadas_ = false;
		
		if (repetirLancamento != true) {
			PrimeFaces.current().executeScript("ocultarRepetir();");
		} else {
			parcelas_ = 2L;
			periodoPagamento = PeriodoPagamento.MESES;
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(lancamento.getDataLancamento());
			calendar.add(Calendar.MONTH, 1);
			
			vencimento = calendar.getTime();
			
			vencimentoEmString = sdf.format(calendar.getTime());
			
			PrimeFaces.current().executeScript("mostrarRepetir();");
		}
	}

	public void zerarParcelas() {
		todasContas = new ArrayList<>();
		parcelasConfirmadas = false;
	}


	public void gerarParcelas() throws ParseException {
		
		parcelasConfirmadas = false;

		todasContas = new ArrayList<>();
		
		entradas = new ArrayList<>();

		Calendar calendario = Calendar.getInstance();
		Calendar vencimento = Calendar.getInstance();
		vencimento.setTime(primeiraParcela);
		vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
		vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
		vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
		
		

		Double valorVenda = lancamento.getValor().doubleValue();
		if (valorEntrada != null && valorEntrada > 0) {
			valorVenda = lancamento.getValor().doubleValue() - valorEntrada;

			Conta conta = new Conta();
			conta.setParcela("Entrada");
			conta.setValor(new BigDecimal(valorEntrada));
			conta.setVencimento(lancamento.getDataLancamento());
			
			conta.setPagamento(lancamento.getDataLancamento());
			 
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(conta.getVencimento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			entradas.add(conta);
		}

		Double valorParcela = valorVenda / parcelas;

		if (valorParcela > 0) {
			for (int i = 0; i < parcelas; i++) {
				
				if(i > 0) {
					
					long dias = parcelas;
					if (periodoPagamento == PeriodoPagamento.MESES) {
						// dias = 30;
						// vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
						vencimento.add(Calendar.MONTH, 1);
					}

					if (periodoPagamento == PeriodoPagamento.QUINZENAS) {
						dias = 15;
						vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					}

					if (periodoPagamento == PeriodoPagamento.SEMANAS) {
						dias = 7;
						vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					}

					/*if (periodoPagamento == PeriodoPagamento.DIAS) {
						dias = i + 1;
						vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					}*/
				}

				

				Conta conta = new Conta();
				conta.setParcela((i + 1) + "/" + parcelas);
				conta.setStatus(false);

				if (i == parcelas - 1) {

					Double valorTemp = 0D;
					DecimalFormat fmt = new DecimalFormat("0.00");
					for (int j = 0; j < i; j++) {
						valorTemp += Double.parseDouble(fmt.format(valorParcela).replace(",", "."));
					}

					conta.setValor(new BigDecimal(valorVenda - valorTemp));

				} else {
					conta.setValor(new BigDecimal(valorParcela));
				}

				conta.setVencimento(vencimento.getTime());
				
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getVencimento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				todasContas.add(conta);
			}
		}
	}

	public void gerarContas() {

		parcelasConfirmadas_ = false;

		todasContas_ = new ArrayList<>();

		Calendar calendario = Calendar.getInstance();
		Calendar vencimento = Calendar.getInstance();
		vencimento.setTime(this.vencimento);
		vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
		vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
		vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));

		Double valorParcela = lancamento.getValor().doubleValue();
		for (int i = 0; i < parcelas_; i++) {

			if(i > 0) {

				long dias = parcelas;
				if (periodoPagamento == PeriodoPagamento.MESES) {
					// dias = 30;
					// vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
					vencimento.add(Calendar.MONTH, 1);
				}
	
				if (periodoPagamento == PeriodoPagamento.QUINZENAS) {
					dias = 15;
					vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
				}
	
				if (periodoPagamento == PeriodoPagamento.SEMANAS) {
					dias = 7;
					vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
				}
	/*
				if (periodoPagamento == PeriodoPagamento.DIAS) {
					dias = i + 1;
					vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
				}
	*/
			}
			
			Conta conta = new Conta();
			conta.setParcela((i + 1) + "/" + parcelas_);
			conta.setValor(new BigDecimal(valorParcela));
			conta.setVencimento(vencimento.getTime());
			
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(conta.getVencimento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			todasContas_.add(conta);
			
		}
	}

	public void changeCategoriaDespesa() {
		despesa.setCategoriaLancamento(new CategoriaLancamento());
		despesa.setDestinoLancamento(new DestinoLancamento());

		if (categoriaLancamentoDespesa != null) {
			despesa.setCategoriaLancamento(categoriaLancamentoDespesa);
			despesa.setDestinoLancamento(categoriaLancamentoDespesa.getDestinoLancamento());
			
			if(categoriaLancamentoDespesa.getId() == 25835L || categoriaLancamentoDespesa.getId() == 5423L ||
					categoriaLancamentoDespesa.getId() == 5424L || categoriaLancamentoDespesa.getId() == 5425L ||
					categoriaLancamentoDespesa.getId() == 5426L || categoriaLancamentoDespesa.getId() == 5427L) {
				PrimeFaces.current().executeScript("mostrarFavorecido();");
				renderFavorecido = true;
			} else {
				PrimeFaces.current().executeScript("ocultarFavorecido();");
				renderFavorecido = false;
			}
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido();");
			renderFavorecido = false;
		}
		
		usuario = new Usuario();
	}

	public void changeCategoriaReceita() {
		receita.setCategoriaLancamento(new CategoriaLancamento());
		receita.setDestinoLancamento(new DestinoLancamento());

		if (categoriaLancamentoReceita != null) {
			receita.setCategoriaLancamento(categoriaLancamentoReceita);
			receita.setDestinoLancamento(categoriaLancamentoReceita.getDestinoLancamento());
		}
	}

	public void salvarDespesa() {
		
		activeIndex = 0;
		repetirLancamento = false;
		lancamentoPago = true;
		tipoPagamento = TipoPagamento.AVISTA;

		if (categoriaLancamentoDespesa != null) {
			
			lancamento = despesa;
			
			if(lancamento.isAjuste()) {		
				
				salvarAjuste();
				
			} else {
				PrimeFaces.current().executeScript("PF('confirmDialog').show();");
				todasContas = new ArrayList<>();
				todasContas_ = new ArrayList<>();
				
				entradas = new ArrayList<>();
				valorEntrada = null;
			}		

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Informe uma categoria de despesa!' });");
		}
	}

	public void salvarReceita() {
		
		activeIndex = 1;
		repetirLancamento = false;
		lancamentoPago = true;
		tipoPagamento = TipoPagamento.AVISTA;

		if (categoriaLancamentoReceita != null) {
			
			lancamento = receita;			
			
			if(lancamento.isAjuste()) {
				
				salvarAjuste();
				
			} else {
				PrimeFaces.current().executeScript("PF('confirmDialog').show();");
				todasContas = new ArrayList<>();
				todasContas_ = new ArrayList<>();
			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Informe uma categoria de receita!' });");
		}
	}
	
	
	private void salvarAjuste() {
		
		if (lancamento.getId() != null) {					
			List<Conta> contasTemp = contas.porCodigoOperacao(lancamento.getNumeroLancamento(), "LANCAMENTO", usuario_.getEmpresa());
			for (Conta conta : contasTemp) {
				contas.remove(conta);
			}				
		}
		
		Lancamento lancamentoTemp = lancamentos.ultimoNLancamento(usuario_.getEmpresa());

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
		
		if(activeIndex == 0) {
			lancamento.setUsuario(renderFavorecido ? usuario : null);
		}

		lancamento.setEmpresa(usuario_.getEmpresa());
		lancamentos.save(lancamento);
		
		if (lancamento.getId() == null) {

			if (lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {

				PrimeFaces.current()
						.executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Lançamento N."
								+ lancamento.getNumeroLancamento() + " registrado com sucesso!' });");
				PrimeFaces.current().ajax().update("form:tabView:panel-despesa");

				despesa = new Lancamento();
				categoriaLancamentoDespesa = new CategoriaLancamento();
				
				usuario = new Usuario();	
				renderFavorecido = false;
				PrimeFaces.current().executeScript("ocultarFavorecido();");

			} else {
				PrimeFaces.current()
						.executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Lançamento N."
								+ lancamento.getNumeroLancamento() + " registrado com sucesso!' });");
				PrimeFaces.current().ajax().update("form:tabView:panel-receita");

				receita = new Lancamento();
				categoriaLancamentoReceita = new CategoriaLancamento();
			}

			tipoPagamento = TipoPagamento.AVISTA;
			lancamentoPago = true;
			repetirLancamento = false;
			
			entradas = new ArrayList<>();
			valorEntrada = null;

		} else {

			PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Lançamento N."
					+ lancamento.getNumeroLancamento() + " atualizado com sucesso!' });");
		}
	}
	
	
	public void confirmarLancamentoViaDashboard() {
		confirmarLancamento();	
		
		if(!lancamento.isConta()) {
			if(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
				PrimeFaces.current().executeScript("PF('receita-dialog').hide();atualizaSaldo(" + lancamento.getNumeroLancamento() + ");");
			} else {
				PrimeFaces.current().executeScript("PF('despesa-dialog').hide();atualizaSaldo(" + lancamento.getNumeroLancamento() + ");");
			}		
		} else {
			if(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO) {
				PrimeFaces.current().executeScript("PF('receita-dialog').hide();");
			} else {
				PrimeFaces.current().executeScript("PF('despesa-dialog').hide();");
			}			
		}
		
		lancamento = new Lancamento();

	}

	
	public void confirmarLancamento() {

		boolean contasPagas = false;

		if (lancamento.getId() != null) {
			// List<Conta> contasTemp =
			// contas.porContasPagas(lancamento.getNumeroLancamento(), "LANCAMENTO");

			// if (contasTemp.size() == 0) {

			List<Conta> contasTemp = contas.porCodigoOperacao(lancamento.getNumeroLancamento(), "LANCAMENTO", usuario_.getEmpresa());
			for (Conta conta : contasTemp) {
				
				List<PagamentoConta> pagamentosConta = pagamentosContas.todosPorConta(conta, usuario_.getEmpresa());
				for (PagamentoConta pagamentoConta : pagamentosConta) {
					pagamentosContas.remove(pagamentoConta);
				}
				
				contas.remove(conta);
			}

			/*
			 * } else {
			 * 
			 * String tipoConta = ""; if (contasTemp.get(0).getTipo().equals("DEBITO")) {
			 * tipoConta = "contas à pagar"; } else { tipoConta = "contas à receber"; }
			 * 
			 * contasPagas = true; PrimeFaces.current().
			 * executeScript("stop();swal({ type: 'error', title: 'Erro!', text: 'Existe " +
			 * tipoConta + ", pagos já registrados para esse lançamento!' });"); }
			 */
		}

		if (contasPagas != true) {
			
			Lancamento lancamentoTemp = lancamentos.ultimoNLancamento(usuario_.getEmpresa());

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
			
			if(tipoPagamento == TipoPagamento.AVISTA) {
				lancamento.setConta(lancamentoPago != true ? true : false);
			} else {
				lancamento.setConta(true);
			}
			
			if(activeIndex == 0) {
				lancamento.setUsuario(renderFavorecido ? usuario : null);
			}

			lancamento.setTipoPagamento(tipoPagamento);
			lancamento.setLancamentoPago(lancamentoPago);
			
			if (tipoPagamento == TipoPagamento.PARCELADO) {
				lancamento.setConta(true);
			} else {
				lancamento.setConta(lancamentoPago != true ? true : false);
			}
			
			lancamento.setEmpresa(usuario_.getEmpresa());
			lancamentos.save(lancamento);
			
			
		
			if (tipoPagamento == TipoPagamento.AVISTA) {
				
				if(lancamento.isConta()) {
					
					Conta conta = new Conta();
					conta.setParcela(tipoPagamento.name());
					conta.setCodigoOperacao(lancamento.getNumeroLancamento());
					conta.setOperacao("LANCAMENTO");
					conta.setTipo(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem().name());
					conta.setStatus(lancamentoPago != true ? false : true);
					conta.setValor(lancamento.getValor());
					
					calendario = Calendar.getInstance();
					Calendar vencimento = Calendar.getInstance();
					vencimento.setTime(pagamentoPara);
					vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
					vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
					vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
					
					conta.setVencimento(vencimento.getTime());
					
					conta.setPagamento(lancamentoPago != true ? null : vencimento.getTime());
					
					calendarioTemp = Calendar.getInstance();
					calendarioTemp.setTime(conta.getVencimento());
					
					conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
					conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
					conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
					conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
					conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

					conta.setEmpresa(usuario_.getEmpresa());
					conta.setSaldo(conta.getValor());
					contas.save(conta);
				}

				if (repetirLancamento) {
					for (Conta contaTemp : todasContas_) {

						contaTemp.setCodigoOperacao(lancamento.getNumeroLancamento());
						contaTemp.setOperacao("LANCAMENTO");
						contaTemp.setTipo(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem().name());
						contaTemp.setStatus(false);
						contaTemp.setPagamento(null);

						contaTemp.setEmpresa(usuario_.getEmpresa());
						contas.save(contaTemp);
					}
				}						

			} else {
				
				for (Conta conta : entradas) {

					conta.setCodigoOperacao(lancamento.getNumeroLancamento());
					conta.setOperacao("LANCAMENTO");
					conta.setTipo(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem().name());
					conta.setStatus(true);

					conta.setEmpresa(usuario_.getEmpresa());
					conta.setSaldo(BigDecimal.ZERO);
					contas.save(conta);
				}

				for (Conta conta : todasContas) {

					conta.setCodigoOperacao(lancamento.getNumeroLancamento());
					conta.setOperacao("LANCAMENTO");
					conta.setTipo(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem().name());
					conta.setStatus(false);
					conta.setPagamento(null);

					conta.setEmpresa(usuario_.getEmpresa());
					conta.setSaldo(conta.getValor());
					contas.save(conta);
				}
			}
			

			if (lancamento.getId() == null) {
				
				if(!lancamento.isConta()) {
					
					Caixa caixa = caixas.porUsuario(usuario_, usuario_.getEmpresa());
					
					if(caixa != null) {
						
						ItemCaixa itemCaixa = new ItemCaixa();
						itemCaixa.setCaixa(caixa);
						itemCaixa.setCodigoOperacao(lancamento.getNumeroLancamento());
						itemCaixa.setData(new Date());
						itemCaixa.setDescricao("Lançamento Nº " + lancamento.getNumeroLancamento());					

						if(usuario_.getEmpresa().getId() == 7111 || usuario_.getEmpresa().getId() == 7112) {
							itemCaixa.setFormaPagamento(formasPagamentos.porId(13987L));
						} else {
							itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario_.getEmpresa())); 
						}
						
						itemCaixa.setOperacao(TipoOperacao.LANCAMENTO);
						itemCaixa.setTipoPagamento(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO ? "Entrada" : "Saída");
						
						itemCaixa.setValor(lancamento.getValor());
							
						itemCaixa.setCaixa(caixa);
						itensCaixas.save(itemCaixa);
					}
					
				}

				if (lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
					
					Log log = new Log();
					log.setDataLog(new Date());
					log.setCodigoOperacao(String.valueOf(lancamento.getNumeroLancamento()));
					log.setOperacao(TipoAtividade.LANCAMENTO.name());
					
					NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
					
					log.setDescricao("Registrou lançamento (Despesa), Nº " + lancamento.getNumeroLancamento() + ", valor total R$ " + nf.format(lancamento.getValor()));
					log.setUsuario(usuario_);		
					logs.save(log);

					PrimeFaces.current()
							.executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Lançamento N."
									+ lancamento.getNumeroLancamento() + " registrado com sucesso!' });");
					PrimeFaces.current().ajax().update("form:tabView:panel-despesa");

					despesa = new Lancamento();
					categoriaLancamentoDespesa = new CategoriaLancamento();
					
					usuario = new Usuario();	
					renderFavorecido = false;
					PrimeFaces.current().executeScript("ocultarFavorecido();");

				} else {
					
					Log log = new Log();
					log.setDataLog(new Date());
					log.setCodigoOperacao(String.valueOf(lancamento.getNumeroLancamento()));
					log.setOperacao(TipoAtividade.LANCAMENTO.name());
					
					NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
					
					log.setDescricao("Registrou lançamento (Receita), Nº " + lancamento.getNumeroLancamento() + ", valor total R$ " + nf.format(lancamento.getValor()));
					log.setUsuario(usuario_);		
					logs.save(log);
					
					PrimeFaces.current()
							.executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Lançamento N."
									+ lancamento.getNumeroLancamento() + " registrado com sucesso!' });");
					PrimeFaces.current().ajax().update("form:tabView:panel-receita");

					receita = new Lancamento();
					categoriaLancamentoReceita = new CategoriaLancamento();
				}

				tipoPagamento = TipoPagamento.AVISTA;
				lancamentoPago = true;
				repetirLancamento = false;
				
				entradas = new ArrayList<>();
				valorEntrada = null;

			} else {
				
				List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(lancamento.getNumeroLancamento(), TipoOperacao.LANCAMENTO, lancamento.getEmpresa());

				if(itensCaixa.size() > 0) {
					
					for (ItemCaixa itemCaixa : itensCaixa) {
						if(!lancamento.isConta()) {
							
							itemCaixa.setData(new Date());
							itemCaixa.setDescricao("Lançamento Nº" + lancamento.getNumeroLancamento());
							
							itemCaixa.setValor(lancamento.getValor());						
							
							if(usuario_.getEmpresa().getId() == 7111 || usuario_.getEmpresa().getId() == 7112) {
								itemCaixa.setFormaPagamento(formasPagamentos.porId(13987L));
							} else {
								itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario_.getEmpresa()));
							}
							
							itemCaixa.setTipoPagamento(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO ? "Entrada" : "Saída");
								
							itensCaixas.save(itemCaixa);
							
						} else {
							
							itensCaixas.remove(itemCaixa);
						}
					}
					
					
				} else if(!lancamento.isConta()) {
					
					Caixa caixa = caixas.porUsuario(usuario_, usuario_.getEmpresa());
					
					if(caixa != null) {
						
						ItemCaixa itemCaixa = new ItemCaixa();
						itemCaixa.setData(new Date());
						itemCaixa.setDescricao("Lançamento Nº" + lancamento.getNumeroLancamento());
						
						itemCaixa.setValor(lancamento.getValor());						
						
						if(usuario_.getEmpresa().getId() == 7111 || usuario_.getEmpresa().getId() == 7112) {
							itemCaixa.setFormaPagamento(formasPagamentos.porId(13987L));
						} else {
							itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro", usuario_.getEmpresa()));
						}
						
						
						itemCaixa.setTipoPagamento(lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.CREDITO ? "Entrada" : "Saída");
						
						itemCaixa.setCaixa(caixa);
						itensCaixas.save(itemCaixa);
					}
					
				}
				
				Log log = new Log();
				log.setDataLog(new Date());
				log.setCodigoOperacao(String.valueOf(lancamento.getNumeroLancamento()));
				log.setOperacao(TipoAtividade.LANCAMENTO.name());
				
				NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
				
				String tipoLancamento = "";
				if (lancamento.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
					tipoLancamento = "(Débito)";
				} else {
					tipoLancamento = "(Crédito)";
				}
				
				log.setDescricao("Alterou lançamento " + tipoLancamento + ", Nº " + lancamento.getNumeroLancamento() + ", valor total R$ " + nf.format(lancamento.getValor()));
				log.setUsuario(usuario_);		
				logs.save(log);

				PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Lançamento N."
						+ lancamento.getNumeroLancamento() + " atualizado com sucesso!' });");
			}
		}

	}

	public Lancamento getDespesa() {
		return despesa;
	}

	public void setDespesa(Lancamento despesa) {
		this.despesa = despesa;
	}

	public Lancamento getReceita() {
		return receita;
	}

	public void setReceita(Lancamento receita) {
		this.receita = receita;
	}

	public List<DestinoLancamento> getTodosDestinosLancamentos() {
		return todosDestinosLancamentos;
	}

	public List<TipoLancamento> getTodosTiposDespesas() {
		return todosTiposDespesas;
	}

	public List<CategoriaLancamento> getTodasCategoriasDespesas() {
		return todasCategoriasDespesas;
	}

	public List<CategoriaLancamento> getTodasCategoriasReceitas() {
		return todasCategoriasReceitas;
	}

	public CategoriaLancamento getCategoriaLancamentoDespesa() {
		return categoriaLancamentoDespesa;
	}

	public void setCategoriaLancamentoDespesa(CategoriaLancamento categoriaLancamentoDespesa) {
		this.categoriaLancamentoDespesa = categoriaLancamentoDespesa;
	}

	public CategoriaLancamento getCategoriaLancamentoReceita() {
		return categoriaLancamentoReceita;
	}

	public void setCategoriaLancamentoReceita(CategoriaLancamento categoriaLancamentoReceita) {
		this.categoriaLancamentoReceita = categoriaLancamentoReceita;
	}

	public OrigemLancamento[] getOrigensLancamentos() {
		return OrigemLancamento.values();
	}

	public TipoConta[] getTiposContas() {
		return TipoConta.values();
	}

	public TipoConta getTipoConta() {
		return tipoConta;
	}

	public void setTipoConta(TipoConta tipoConta) {
		this.tipoConta = tipoConta;
	}

	public Lancamento getLancamento() {
		return lancamento;
	}

	public void setLancamento(Lancamento lancamento) {
		this.lancamento = lancamento;
	}

	public Integer getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(Integer activeIndex) {
		this.activeIndex = activeIndex;
	}

	public TipoPagamento[] getTiposPagamentos() {
		return TipoPagamento.values();
	}

	public List<Conta> getTodasContas() {
		return todasContas;
	}

	public TipoPagamento getTipoPagamento() {
		return tipoPagamento;
	}

	public void setTipoPagamento(TipoPagamento tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}

	public PeriodoPagamento getPeriodoPagamento() {
		return periodoPagamento;
	}

	public void setPeriodoPagamento(PeriodoPagamento periodoPagamento) {
		this.periodoPagamento = periodoPagamento;
	}

	public boolean isContaAPagar() {
		return contaAPagar;
	}

	public void setContaAPagar(boolean contaAPagar) {
		this.contaAPagar = contaAPagar;
	}

	public Long getParcelas() {
		return parcelas;
	}

	public void setParcelas(Long parcelas) {
		this.parcelas = parcelas;
	}

	public PeriodoPagamento[] getPeriodosPagamentos() {
		return PeriodoPagamento.values();
	}

	public Integer getTodasContasSize() {
		return todasContas.size();
	}

	public Integer getTodasContasSize_() {
		return todasContas_.size();
	}

	public boolean isLancamentoPago() {
		return lancamentoPago;
	}

	public void setLancamentoPago(boolean lancamentoPago) {
		this.lancamentoPago = lancamentoPago;
	}

	public boolean isRepetirLancamento() {
		return repetirLancamento;
	}

	public void setRepetirLancamento(boolean repetirLancamento) {
		this.repetirLancamento = repetirLancamento;
	}

	public List<Conta> getTodasContas_() {
		return todasContas_;
	}

	public Long getParcelas_() {
		return parcelas_;
	}

	public void setParcelas_(Long parcelas_) {
		this.parcelas_ = parcelas_;
	}

	public Double getValorEntrada() {
		return valorEntrada;
	}

	public void setValorEntrada(Double valorEntrada) {
		this.valorEntrada = valorEntrada;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public boolean isRenderFavorecido() {
		return renderFavorecido;
	}

	public void setRenderFavorecido(boolean renderFavorecido) {
		this.renderFavorecido = renderFavorecido;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public Date getPagamentoPara() {
		return pagamentoPara;
	}

	public String getPagamentoParaEmString() {
		return pagamentoParaEmString;
	}

	public Date getPrimeiraParcela() {
		return primeiraParcela;
	}

	public void setPrimeiraParcela(Date primeiraParcela) {
		this.primeiraParcela = primeiraParcela;
	}

	public boolean isParcelasConfirmadas() {
		return parcelasConfirmadas;
	}

	public String getPrimeiraParcelaEmString() {
		return primeiraParcelaEmString;
	}

	public void setPagamentoPara(Date pagamentoPara) {
		this.pagamentoPara = pagamentoPara;
	}

	public boolean isParcelasConfirmadas_() {
		return parcelasConfirmadas_;
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public String getVencimentoEmString() {
		return vencimentoEmString;
	}

	public Usuario getUsuario_() {
		return usuario_;
	}
}
