package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Conta;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.ItemCaixa;
import com.webapp.model.Lancamento;
import com.webapp.model.Log;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.PagamentoConta;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoOperacao;
import com.webapp.model.Usuario;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Contas_;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.PagamentosContas;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class ConsultaLancamentosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Lancamento lancamento;

	private List<Lancamento> lancamentosFiltrados = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuario usuario_;
	
	@Inject
	private Usuarios usuarios;

	@Inject
	private Lancamentos lancamentos;

	private Lancamento lancamentoSelecionado;
	
	private Long numeroLancamento;

	private Date dateStart = new Date();

	private Date dateStop = new Date();

	private OrigemLancamento[] origemLancamento;

	@Inject
	private CategoriasLancamentos categoriasDespesas;

	private List<CategoriaLancamento> todasCategoriasDespesas;

	@Inject
	private DestinosLancamentos destinosLancamentos;

	private List<DestinoLancamento> todosDestinosLancamentos;

	@Inject
	private CategoriaLancamento categoriaLancamento;

	@Inject
	private DestinoLancamento destinoLancamento;
	
	@Inject
	private Contas_ contas;
	
	@Inject
	private PagamentosContas pagamentosContas;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalLancamentos = "0,00";
	
	private boolean renderFavorecido;
	
	private String[] categorias;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	@Inject
	private Logs logs;

	private boolean lancamentosPagos;
	
	@Inject
	private Usuario vendedor;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario_ = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
			todasCategoriasDespesas = categoriasDespesas.todos(usuario_.getEmpresa());
			todosDestinosLancamentos = destinosLancamentos.todos(usuario_.getEmpresa());
			
			numeroLancamento = null;
		}
	}

	public void pesquisar() {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);
		
		lancamentoSelecionado = null;
		
		boolean favorecido = false;
		
		for (String categoria : categorias) {
			categoriaLancamento = categoriasDespesas.porNome(categoria, usuario_.getEmpresa());
			if(categoriaLancamento.getId() == 25835L || categoriaLancamento.getId() == 5423L ||
					categoriaLancamento.getId() == 5424L || categoriaLancamento.getId() == 5425L ||
							categoriaLancamento.getId() == 5426L || categoriaLancamento.getId() == 5427L) {
				favorecido = true;		
			}
		}	
		
		if(favorecido) {
			PrimeFaces.current().executeScript("mostrarFavorecido();");
			renderFavorecido = true;
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido();");
			renderFavorecido = false;
		}
		
		Usuario usuarioTemp = favorecido ? usuario : new Usuario();

		lancamentosFiltrados = new ArrayList<>();
		lancamentosFiltrados = lancamentos.lancamentosFiltrados(numeroLancamento, dateStart, calendarioTemp.getTime(), origemLancamento,
				categoriaLancamento, destinoLancamento, usuarioTemp, categorias, lancamentosPagos, usuario_.getEmpresa(), vendedor);

		double totalLancamentosTemp = 0;
		for (Lancamento lancamento : lancamentosFiltrados) {
				
			if(!lancamento.isAjuste()) {
				totalLancamentosTemp += lancamento.getValor().doubleValue();
			}
			
			if(lancamento.isConta()) {
				lancamento.setLancamentoPago(true);
				List<Conta> listaDeContas = contas.porCodigoOperacao(lancamento.getNumeroLancamento(), "LANCAMENTO", usuario_.getEmpresa());
				for (Conta conta : listaDeContas) {
					if(!conta.isStatus()) {
						lancamento.setLancamentoPago(false);
					}
				}
			}
			
			/*if(!lancamento.getCategoriaLancamento().getNome().contains("Salário")) {
				
				lancamento.setUsuario(null);
				lancamentos.save(lancamento);
			}*/
			
			/*List<Conta> listaDeContas = contas.porCodigoOperacao(lancamento.getNumeroLancamento(), "LANCAMENTO");
			if(listaDeContas.size() == 0) {
				
				Conta conta = new Conta();
				conta.setCodigoOperacao(lancamento.getNumeroLancamento());
				conta.setOperacao("LANCAMENTO");
				conta.setParcela(TipoPagamento.AVISTA.name());
				conta.setTipo("DEBITO");
				conta.setStatus(true);
				conta.setValor(lancamento.getValor());
				
				conta.setVencimento(lancamento.getDataLancamento());
				conta.setPagamento(lancamento.getDataLancamento());
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getVencimento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				contas.save(conta);			
			}*/
		}

		totalLancamentos = nf.format(totalLancamentosTemp);
/*	
		if(origemLancamento.length > 0) {
			
			for (Lancamento lancamento : lancamentosFiltrados) {
				Conta conta = new Conta();
				conta.setOperacao("LANCAMENTO");
				conta.setCodigoOperacao(lancamento.getNumeroLancamento());
				conta.setVencimento(lancamento.getDataLancamento());
				conta.setPagamento(lancamento.getDataLancamento());
				conta.setValor(lancamento.getValor());
				conta.setParcela("AVISTA");
				conta.setTipo("DEBITO");
				conta.setStatus(true);
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(lancamento.getDataLancamento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));
				
				contas.save(conta);
				
				System.out.println(lancamento.getNumeroLancamento());
			}
		}
*/
		
	}

	public void changeCategoria() {
		
		boolean favorecido = false;
		
		for (String categoria : categorias) {
			categoriaLancamento = categoriasDespesas.porNome(categoria, usuario_.getEmpresa());
			if(categoriaLancamento.getId() == 25835L || categoriaLancamento.getId() == 5423L ||
					categoriaLancamento.getId() == 5424L || categoriaLancamento.getId() == 5425L ||
							categoriaLancamento.getId() == 5426L || categoriaLancamento.getId() == 5427L) {
				favorecido = true;		
			}
		}	
		
		if(favorecido) {
			PrimeFaces.current().executeScript("mostrarFavorecido();");
			renderFavorecido = true;
		} else {
			PrimeFaces.current().executeScript("ocultarFavorecido();");
			renderFavorecido = false;
		}
		
	}

	public void excluir() {

		boolean contasPagas = false;
		//List<Conta> contasTemp = contas.porContasPagas(lancamentoSelecionado.getNumeroLancamento(), "LANCAMENTO");

		//if (contasTemp.size() == 0) {

		List<Conta> contasTemp = contas.porCodigoOperacao(lancamentoSelecionado.getNumeroLancamento(), "LANCAMENTO", usuario_.getEmpresa());
		for (Conta conta : contasTemp) {
			
			List<PagamentoConta> pagamentosContaTemp = pagamentosContas.todosPorConta(conta, usuario_.getEmpresa());
			for (PagamentoConta pagamentoConta : pagamentosContaTemp) {
				pagamentosContas.remove(pagamentoConta);
			}
			
			contas.remove(conta);
		}

		/*} else {

			String tipoConta = "";
			if (contasTemp.get(0).getTipo().equals("DEBITO")) {
				tipoConta = "contas à pagar";
			} else {
				tipoConta = "contas à receber";
			}

			contasPagas = true;
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Erro!', text: 'Existe " + tipoConta
					+ " já registradas para esse lançamento!' });");
		}*/

		if (contasPagas != true) {
			lancamentos.remove(lancamentoSelecionado);
			
			
			List <ItemCaixa> itemCaixas = itensCaixas.porCodigoOperacao(lancamentoSelecionado.getNumeroLancamento(), TipoOperacao.LANCAMENTO, lancamentoSelecionado.getEmpresa());

			if(itemCaixas.size() > 0) {
				
				for (ItemCaixa itemCaixa : itemCaixas) {
					itensCaixas.remove(itemCaixa);
				}
				
			}
			
			Log log = new Log();
			log.setDataLog(new Date());
			log.setCodigoOperacao(String.valueOf(lancamento.getNumeroLancamento()));
			log.setOperacao(TipoAtividade.LANCAMENTO.name());
			
			NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
			
			String tipoLancamento = "";
			if (lancamentoSelecionado.getCategoriaLancamento().getTipoLancamento().getOrigem() == OrigemLancamento.DEBITO) {
				tipoLancamento = "(Débito)";
			} else {
				tipoLancamento = "(Crédito)";
			}
			
			log.setDescricao("Deletou lançamento " + tipoLancamento + ", Nº " + lancamentoSelecionado.getNumeroLancamento() + ", valor total R$ " + nf.format(lancamentoSelecionado.getValor()));
			log.setUsuario(usuario_);		
			logs.save(log);

			lancamentoSelecionado = null;
			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Lançamento excluído com sucesso!' });");
		}
	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
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

	public Lancamento getLancamentoSelecionado() {
		return lancamentoSelecionado;
	}

	public void setLancamentoSelecionado(Lancamento lancamentoSelecionado) {
		this.lancamentoSelecionado = lancamentoSelecionado;
	}

	public List<Lancamento> getLancamentosFiltrados() {
		return lancamentosFiltrados;
	}

	public void setLancamentosFiltrados(List<Lancamento> comprasFiltradas) {
		this.lancamentosFiltrados = comprasFiltradas;
	}

	public int getLancamentosFiltradosSize() {
		return lancamentosFiltrados.size();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public OrigemLancamento[] getOrigensLancamentos() {
		return OrigemLancamento.values();
	}

	public OrigemLancamento[] getOrigemLancamento() {
		return origemLancamento;
	}

	public void setOrigemLancamento(OrigemLancamento[] origemLancamento) {
		this.origemLancamento = origemLancamento;
	}

	public List<CategoriaLancamento> getTodasCategoriasDespesas() {
		return todasCategoriasDespesas;
	}

	public List<DestinoLancamento> getTodosDestinosLancamentos() {
		return todosDestinosLancamentos;
	}

	public CategoriaLancamento getCategoriaLancamento() {
		return categoriaLancamento;
	}

	public void setCategoriaLancamento(CategoriaLancamento categoriaLancamento) {
		this.categoriaLancamento = categoriaLancamento;
	}

	public Lancamento getLancamento() {
		return lancamento;
	}

	public void setLancamento(Lancamento lancamento) {
		this.lancamento = lancamento;
	}

	public DestinoLancamento getDestinoLancamento() {
		return destinoLancamento;
	}

	public void setDestinoLancamento(DestinoLancamento destinoLancamento) {
		this.destinoLancamento = destinoLancamento;
	}

	public String getTotalLancamentos() {
		return totalLancamentos;
	}

	public Long getNumeroLancamento() {
		return numeroLancamento;
	}

	public void setNumeroLancamento(Long numeroLancamento) {
		this.numeroLancamento = numeroLancamento;
	}

	public boolean isRenderFavorecido() {
		return renderFavorecido;
	}

	public void setRenderFavorecido(boolean renderFavorecido) {
		this.renderFavorecido = renderFavorecido;
	}

	public String[] getCategorias() {
		return categorias;
	}

	public void setCategorias(String[] categorias) {
		this.categorias = categorias;
	}

	public boolean isLancamentosPagos() {
		return lancamentosPagos;
	}

	public void setLancamentosPagos(boolean lancamentosPagos) {
		this.lancamentosPagos = lancamentosPagos;
	}

	public Usuario getVendedor() {
		return vendedor;
	}

	public void setVendedor(Usuario vendedor) {
		this.vendedor = vendedor;
	}

}
