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

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.Lancamento;
import com.webapp.model.Log;
import com.webapp.model.OrigemConta;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoConta;
import com.webapp.model.TipoOperacao;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaContasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Contas contas;
	
	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private Compras compras;
	
	@Inject
	private Vendas vendas;
	
	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;

	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalContas = "0,00";

	private Long codigo;

	private Date vencimento = new Date();

	private boolean contasPagas;

	private OrigemConta[] origemConta;

	private TipoOperacao tipoOperacao;

	private List<Conta> contasFiltradas = new ArrayList<>();

	private Conta contaSelecionada;

	private Date mes = new Date();
	
	@Inject
	private Logs logs;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			// todosUsuarios = usuarios.todos();
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	
			usuario = usuarios.porLogin(user.getUsername());
		}
	}

	public void pesquisar() {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(vencimento);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		contasFiltradas = new ArrayList<>();
		contasFiltradas = contas.contasFiltradas(null, tipoOperacao, mes, calendarioTemp, origemConta, vencimento,
				contasPagas, usuario.getEmpresa());

		double totalContasTemp = 0;
		for (Conta conta : contasFiltradas) {
			totalContasTemp += conta.getValor().doubleValue();
			
			if(conta.getOperacao().equals(TipoOperacao.LANCAMENTO.toString())) {
				Lancamento lancamento = lancamentos.porNumeroLancamento(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao(lancamento.getDescricao());
			}
			
			if(conta.getOperacao().equals(TipoOperacao.COMPRA.toString())) {
				//Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				//conta.setDescricao("Compra realizada");
			}
			
			if(conta.getOperacao().equals(TipoOperacao.VENDA.toString())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				conta.setDescricao("Cliente: " + venda.getCliente().getNome());
			}
			
		}

		totalContas = nf.format(totalContasTemp);
		/*
		 * if(origemConta.length > 0 ) { for (Conta conta : contasFiltradas) {
		 * contas.remove(conta); } }
		 */

		contaSelecionada = null;
	}
	
	public void preparaDataVencimento() {
		contaSelecionada.setVencimento(new Date());
	}

	public void pagar() {
		
		Conta conta = contas.porId(contaSelecionada.getId());

		conta.setPagamento(contaSelecionada.getVencimento());
		conta.setStatus(true);

		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(conta.getPagamento());

		conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
		conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
		conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
		conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
		conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

		conta = contas.save(conta);
		
		
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(conta.getCodigoOperacao()));
	
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		String msg = "";
		if(conta.getOperacao().equals(TipoAtividade.VENDA.name())) {
			msg = "Recebeu venda";
			log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			
			if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
				Venda venda = vendas.porNumeroVenda(conta.getCodigoOperacao(), usuario.getEmpresa());
				venda.setVendaPaga(true);
				venda.setConta(false);
				vendas.save(venda);
			}
		}
		
		if(conta.getOperacao().equals(TipoAtividade.COMPRA.name())) {
			msg = "Pagou compra";
			log.setOperacao(TipoAtividade.PAGAMENTO.name());
			
			if(conta.getParcela().equals(TipoPagamento.AVISTA.name())) {
				Compra compra = compras.porNumeroCompra(conta.getCodigoOperacao(), usuario.getEmpresa());
				compra.setCompraPaga(true);
				compra.setConta(false);
				compras.save(compra);
			}
		}
		
		if(conta.getOperacao().equals(TipoAtividade.LANCAMENTO.name())) {
			if(conta.getTipo().equals(TipoConta.ARECEBER.name())) {
				msg = "Recebeu receita";
				log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			} else {
				msg = "Pagou despesa";
				log.setOperacao(TipoAtividade.PAGAMENTO.name());
			}
		}
		
		log.setDescricao(msg + ", Nº " + conta.getCodigoOperacao() + ", valor total R$ " + nf.format(conta.getValor()));
		log.setUsuario(usuario);		
		logs.save(log);
		

		
		
		
		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Pagamento realizado com sucesso!' });");
	}

	public void estornar() {

		contaSelecionada.setPagamento(null);
		contaSelecionada.setStatus(false);

		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(contaSelecionada.getVencimento());

		contaSelecionada.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
		contaSelecionada.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
		contaSelecionada.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
		contaSelecionada.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
		contaSelecionada.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

		contas.save(contaSelecionada);
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(contaSelecionada.getCodigoOperacao()));
		
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		String msg = "";
		if(contaSelecionada.getOperacao().equals(TipoAtividade.VENDA.name())) {
			msg = "Desfez Recebimento, venda Nº ";
			log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			
			if(contaSelecionada.getParcela().equals(TipoPagamento.AVISTA.name())) {
				Venda venda = vendas.porNumeroVenda(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
				venda.setVendaPaga(false);
				venda.setConta(true);
				vendas.save(venda);
			}
		}
		
		if(contaSelecionada.getOperacao().equals(TipoAtividade.COMPRA.name())) {
			msg = "Desfez Pagamento, compra Nº ";
			log.setOperacao(TipoAtividade.PAGAMENTO.name());
			
			if(contaSelecionada.getParcela().equals(TipoPagamento.AVISTA.name())) {
				Compra compra = compras.porNumeroCompra(contaSelecionada.getCodigoOperacao(), usuario.getEmpresa());
				compra.setCompraPaga(false);
				compra.setConta(true);
				compras.save(compra);
			}
		}
		
		if(contaSelecionada.getOperacao().equals(TipoAtividade.LANCAMENTO.name())) {
			if(contaSelecionada.getTipo().equals(TipoConta.ARECEBER.name())) {
				msg = "Desfez Recebimento, receita Nº ";
				log.setOperacao(TipoAtividade.RECEBIMENTO.name());
			} else {
				msg = "Desfez Pagamento, despesa Nº ";
				log.setOperacao(TipoAtividade.PAGAMENTO.name());
			}
		}
		
		log.setDescricao(msg + contaSelecionada.getCodigoOperacao() + ", valor total R$ " + nf.format(contaSelecionada.getValor()));
		log.setUsuario(usuario);		
		logs.save(log);
		

		pesquisar();

		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Concluído!', text: 'Pagamento estornado com sucesso!' });");

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

	public OrigemConta[] getOrigensContas() {
		return OrigemConta.values();
	}

	public TipoOperacao[] getTiposOperacoes() {
		return com.webapp.model.TipoOperacao.values();
	}

	public OrigemConta[] getOrigemConta() {
		return origemConta;
	}

	public void setOrigemConta(OrigemConta[] origemConta) {
		this.origemConta = origemConta;
	}

	public String getTotalContas() {
		return totalContas;
	}

	public TipoOperacao getTipoOperacao() {
		return tipoOperacao;
	}

	public void setTipoOperacao(TipoOperacao tipoOperacao) {
		this.tipoOperacao = tipoOperacao;
	}

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}

	public Date getVencimento() {
		return vencimento;
	}

	public void setVencimento(Date vencimento) {
		this.vencimento = vencimento;
	}

	public boolean isContasPagas() {
		return contasPagas;
	}

	public void setContasPagas(boolean contasPagas) {
		this.contasPagas = contasPagas;
	}

	public List<Conta> getContasFiltradas() {
		return contasFiltradas;
	}

	public Date getMes() {
		return mes;
	}

	public void setMes(Date mes) {
		this.mes = mes;
	}

	public int getContasFiltradasSize() {
		return contasFiltradas.size();
	}

	public Conta getContaSelecionada() {
		return contaSelecionada;
	}

	public void setContaSelecionada(Conta contaSelecionada) {
		this.contaSelecionada = contaSelecionada;
	}

}
