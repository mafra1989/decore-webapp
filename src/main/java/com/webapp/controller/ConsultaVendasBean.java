package com.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.google.common.io.ByteSource;
import com.webapp.model.Caixa;
import com.webapp.model.Cliente;
import com.webapp.model.Conta;
import com.webapp.model.Devolucao;
import com.webapp.model.Entrega;
import com.webapp.model.EspelhoVenda;
import com.webapp.model.ItemCaixa;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemDevolucao;
import com.webapp.model.ItemEspelhoVendaPagamento;
import com.webapp.model.ItemEspelhoVendaPagamentos;
import com.webapp.model.ItemEspelhoVendaParcelamentos;
import com.webapp.model.ItemEspelhoVendaProdutos;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.Log;
import com.webapp.model.Pagamento;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.TipoAtividade;
import com.webapp.model.TipoOperacao;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Caixas;
import com.webapp.repository.Clientes;
import com.webapp.repository.Contas_;
import com.webapp.repository.Devolucoes;
import com.webapp.repository.Entregas;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensDevolucoes;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Logs;
import com.webapp.repository.Pagamentos;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@SessionScoped
public class ConsultaVendasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Venda> vendasFiltradas = new ArrayList<>();

	private List<Usuario> todosUsuarios;
	
	private List<Usuario> todosEntregadores;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuario entregador;
	
	@Inject
	private Usuario usuario_;

	@Inject
	private Vendas vendas;

	@Inject
	private Entregas entregas;

	@Inject
	private Entrega entrega;

	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private Contas_ contas;
	
	@Inject
	private Lancamentos lancamentos;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;

	@Inject
	private Produtos produtos;

	@Inject
	private ItensCompras itensCompras;

	private Venda vendaSelecionada;

	private Long numeroVenda;

	private Date dateStart = new Date();

	private Date dateStop = new Date();
	
	private static final Locale BRAZIL = new Locale("pt", "BR");

	private static final DecimalFormatSymbols REAL = new DecimalFormatSymbols(BRAZIL);

	private NumberFormat nf = new DecimalFormat("###,##0.00", REAL);

	private String totalVendas = "0,00";

	private Integer totalItens = 0;

	private boolean vendasNormais;

	private StatusPedido[] statusPedido;
	
	@Inject
	private Devolucoes devolucoes;
	
	@Inject
	private ItensDevolucoes itensDevolucoes;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	@Inject
	private Caixas caixas;
	
	@Inject
	private Pagamentos pagamentos;
	
	@Inject
	private Cliente cliente;
	
	@Inject
	private Clientes clientes;
	
	private List<Cliente> todosClientes = new ArrayList<Cliente>();
	
	@Inject
	private Logs logs;
	
	private boolean vendasPagas;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario_ = usuarios.porLogin(user.getUsername());
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
			
			todosClientes = clientes.todos(usuario_.getEmpresa());
			
			todosEntregadores = usuarios.todosEntregadores(usuario_.getEmpresa());
			
			numeroVenda = null;
		}
	}

	public void pesquisar() {
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		vendasFiltradas = vendas.vendasFiltradas(numeroVenda, dateStart, calendarioTemp.getTime(), vendasNormais,
				statusPedido, usuario, cliente, entregador, vendasPagas, usuario_.getEmpresa());

		double totalVendasTemp = 0;
		double totalDesconto = 0;
		totalItens = 0;
		for (Venda venda : vendasFiltradas) {		
			if(!venda.isAjuste()) {
				totalVendasTemp += venda.getValorTotal().doubleValue();				
				totalItens += venda.getQuantidadeItens().intValue();
				
				if(venda.getDesconto() != null) {
					totalDesconto += venda.getDesconto().doubleValue();
				}
			}
			/*
			Pagamento pagamento = pagamentos.porVenda(venda, venda.getEmpresa());
			if(pagamento != null) {
				venda.setPagamento(", " + pagamento.getFormaPagamento().getNome());
			}
			*/
			
			if(venda.isConta()) {
				venda.setVendaPaga(true);
				List<Conta> listaDeContas = contas.porCodigoOperacao(venda.getNumeroVenda(), "VENDA", usuario_.getEmpresa());
				for (Conta conta : listaDeContas) {
					if(!conta.isStatus()) {
						venda.setVendaPaga(false);
					}
				}
			}
			
		}

		totalVendas = nf.format(totalVendasTemp - totalDesconto);

		vendaSelecionada = null;
	}

	public void prepararEntrega() {
		entrega = entregas.porVenda(vendaSelecionada);
	}

	public void entregarVenda() {
		
		entrega = entregas.porVenda(vendaSelecionada);
		entrega.setStatus(StatusPedido.ENTREGUE.name());
		entrega = entregas.save(entrega);

		Venda venda = entrega.getVenda();
		venda.setStatus(true);
		
		if(venda.getTipoPagamento() == TipoPagamento.AVISTA && !venda.isConta()) {
			venda.setVendaPaga(true);
		}
		
		venda = vendas.save(venda);
		
		
		if(!venda.isConta()) {
			
			Caixa caixa = caixas.porUsuario(usuario_, usuario_.getEmpresa());
			
			if(caixa != null) {
				
				List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(venda.getNumeroVenda(), TipoOperacao.VENDA, usuario_.getEmpresa());
				
				if(itensCaixa.size() == 0) {
				
					ItemCaixa itemCaixa = new ItemCaixa();
					itemCaixa.setCaixa(caixa);
					itemCaixa.setCodigoOperacao(venda.getNumeroVenda());
					itemCaixa.setData(new Date());
					itemCaixa.setDescricao("Venda Nº " + venda.getNumeroVenda());
					itemCaixa.setFormaPagamento(venda.getFormaPagamento());
					itemCaixa.setOperacao(TipoOperacao.VENDA);
					itemCaixa.setTipoPagamento("Entrada");
					
					List<ItemVenda> itensVenda = itensVendas.porVenda(venda);
					
					Double valorTotal = 0D;
					for (ItemVenda itemVenda : itensVenda) {
						valorTotal += itemVenda.getTotal().doubleValue();
					}
					
					if(!venda.getFormaPagamento().getNome().equals("Dinheiro")) {
						
						if(venda.getDesconto() != null) {
							venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue() - venda.getDesconto().doubleValue()));
						} else {
							venda.setValorTotalComDesconto(new BigDecimal(venda.getValorTotal().doubleValue()));
						}
						
						itemCaixa.setValor(venda.getValorTotalComDesconto());
						
					} else {
						
						if(venda.getDesconto() != null) {
							itemCaixa.setValor(new BigDecimal(valorTotal - venda.getDesconto().doubleValue()));
						} else {
							itemCaixa.setValor(new BigDecimal(valorTotal));
						}
						
					}		
					
					itensCaixas.save(itemCaixa);
				}
			}
			
		}
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(vendaSelecionada.getNumeroVenda()));
		log.setOperacao(TipoAtividade.VENDA.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		log.setDescricao("Entregou venda, Nº " + vendaSelecionada.getNumeroVenda() + ", quantidade de itens " + vendaSelecionada.getQuantidadeItens() + ", valor total R$ " + nf.format(vendaSelecionada.getValorTotal()));
		log.setUsuario(usuario_);		
		logs.save(log);

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
				+ vendaSelecionada.getNumeroVenda() + " entregue com sucesso!' });");

		pesquisar();
	}

	public void desfazerEntrega() {
		entrega = entregas.porVenda(vendaSelecionada);
		entrega.setStatus(StatusPedido.PENDENTE.name());
		entrega = entregas.save(entrega);

		Venda venda = entrega.getVenda();
		venda.setStatus(false);
		venda = vendas.save(venda);
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(vendaSelecionada.getNumeroVenda()));
		log.setOperacao(TipoAtividade.VENDA.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		log.setDescricao("Desfez entrega, venda Nº " + vendaSelecionada.getNumeroVenda() + ", quantidade de itens " + vendaSelecionada.getQuantidadeItens() + ", valor total R$ " + nf.format(vendaSelecionada.getValorTotal()));
		log.setUsuario(usuario_);		
		logs.save(log);

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Entrega da venda N."
				+ vendaSelecionada.getNumeroVenda() + " desfeita com sucesso!' });");

		pesquisar();
	}
	
	public void desfazerEntregaPagamento() {
		entrega.setStatus(StatusPedido.PENDENTE.name());
		entrega = entregas.save(entrega);

		Venda venda = entrega.getVenda();
		venda.setStatus(false);
		venda.setVendaPaga(false);
		venda = vendas.save(venda);
		
		
		Log log = new Log();
		log.setDataLog(new Date());
		log.setCodigoOperacao(String.valueOf(vendaSelecionada.getNumeroVenda()));
		log.setOperacao(TipoAtividade.VENDA.name());
		
		NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
		
		log.setDescricao("Desfez entrega e pagamento, venda Nº " + vendaSelecionada.getNumeroVenda() + ", quantidade de itens " + vendaSelecionada.getQuantidadeItens() + ", valor total R$ " + nf.format(vendaSelecionada.getValorTotal()));
		log.setUsuario(usuario_);		
		logs.save(log);
		
		
		if(!venda.isVendaPaga()) {
			List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(venda.getNumeroVenda(), TipoOperacao.VENDA, usuario_.getEmpresa());
			
			if(itensCaixa.size() > 0) {
				for (ItemCaixa itemCaixa : itensCaixa) {
					itensCaixas.remove(itemCaixa);
				}				
			}
		}

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Entrega da venda N."
				+ vendaSelecionada.getNumeroVenda() + " desfeita com sucesso!' });");

		pesquisar();
	}

	public void verLocalizacao() {
		if (entrega.getLocalizacao() != null && !entrega.getLocalizacao().trim().equals("")) {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect("https://maps.google.com/maps?daddr=" + entrega.getLocalizacao());
			} catch (IOException e) {

			}
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Não existe informação de localização de entrega para esta venda!' });");
		}
	}

	public void emitirPedido() {

		vendaSelecionada = vendas.porId(vendaSelecionada.getId());
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		EspelhoVenda pedido = new EspelhoVenda();
		pedido.setVendaNum(vendaSelecionada.getNumeroVenda() + "");
		pedido.setTipoVenda(vendaSelecionada.getTipoVenda().getDescricao().toUpperCase());
		pedido.setBairro(vendaSelecionada.getBairro().getNome().toUpperCase());
		pedido.setDataVenda(sdf.format(vendaSelecionada.getDataVenda()));
		
		String vendedor = vendaSelecionada.getUsuario().getNome().split(" ")[0].toUpperCase();
		pedido.setVendedor(vendedor);
		
		if(vendaSelecionada.getCliente().getNome().toUpperCase().equals("NAO INFORMADO") || vendaSelecionada.getCliente().getNome().toUpperCase().equals("NÃO INFORMADO")) {
			Entrega entrega = entregas.porVenda(vendaSelecionada);
			if (entrega != null) {
				if(!vendaSelecionada.getBairro().getNome().toUpperCase().equals("NAO INFORMADO") && !vendaSelecionada.getBairro().getNome().toUpperCase().equals("NÃO INFORMADO")) {
					if(entrega.getNome() == null || entrega.getNome() != null && entrega.getNome().equals("")) {
						pedido.setCliente("Não Informado");
					} else {
						pedido.setCliente(entrega.getNome());
					}
					
					pedido.setEnderecoCliente(entrega.getLocalizacao() != null ? entrega.getLocalizacao() : "");
					//implementar observação/contato
					pedido.setTelefone(entrega.getContato() != null ? entrega.getContato() : "");
					
				} else {
					pedido.setCliente("Não Informado");
					pedido.setEnderecoCliente("");
					//implementar observação/contato
					pedido.setTelefone("");
				}						
			} else {
				pedido.setCliente("Não Informado");
				pedido.setEnderecoCliente("");	
				pedido.setTelefone("");
			}
			
			pedido.setCpfCnpj("");			
			pedido.setCodigoCliente("");
			pedido.setBairroCliente(vendaSelecionada.getBairro().getNome());
			
		} else {
			if(vendaSelecionada.getBairro().getNome().toUpperCase().equals("NAO INFORMADO") || vendaSelecionada.getBairro().getNome().toUpperCase().equals("NÃO INFORMADO")) {
				pedido.setCliente(vendaSelecionada.getCliente().getNome());
				pedido.setCpfCnpj(vendaSelecionada.getCliente().getDocumento());
				pedido.setTelefone(vendaSelecionada.getCliente().getContato());
				pedido.setCodigoCliente(String.valueOf(vendaSelecionada.getCliente().getId().longValue()));
				pedido.setEnderecoCliente(vendaSelecionada.getCliente().getEndereco());
				pedido.setBairroCliente(vendaSelecionada.getCliente().getBairro().getNome());
			} else {
				Entrega entrega = entregas.porVenda(vendaSelecionada);
				if (entrega != null) {
					if(entrega.getNome() == null || entrega.getNome() != null && entrega.getNome().equals("")) {
						pedido.setCliente(vendaSelecionada.getCliente().getNome());
					} else {
						pedido.setCliente(entrega.getNome());
					}
					
					pedido.setEnderecoCliente(entrega.getLocalizacao() != null ? entrega.getLocalizacao() : "");
					
					//implementar observação/contato
					if(entrega.getContato() == null || entrega.getContato() != null && entrega.getContato().equals("")) {
						pedido.setTelefone(vendaSelecionada.getCliente().getContato());
					} else {
						pedido.setTelefone(entrega.getContato());
					}
				}
								
				pedido.setCpfCnpj(vendaSelecionada.getCliente().getDocumento());
				pedido.setCodigoCliente(String.valueOf(vendaSelecionada.getCliente().getId().longValue()));
				pedido.setBairroCliente(vendaSelecionada.getBairro().getNome());
			}
		}

		/*
		Entrega entrega = entregas.porVenda(vendaSelecionada);
		if (entrega != null) {
			pedido.setResponsavel(entrega.getNome());
			pedido.setLocalizacao(entrega.getLocalizacao());
			pedido.setObservacao(entrega.getObservacao());
			pedido.setTelefone(entrega.getContato());
			
			if(entrega.getStatus() == StatusPedido.ENTREGUE.name()) {
				pedido.setEntrega("Y");
			}
		}*/
		
		NumberFormat nf_ = new DecimalFormat("###,##0.000", REAL);
		NumberFormat nf__ = new DecimalFormat("###,##0", REAL);
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemEspelhoVendaProdutos itemPedido = new ItemEspelhoVendaProdutos();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			itemPedido.setDescricao(itemVenda.getProduto().getDescricao().trim());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().doubleValue()));
			
			String quantidade = "0";
			if(itemVenda.getProduto().getUnidadeMedida().equals("Kg") || itemVenda.getProduto().getUnidadeMedida().equals("Lt"))  {
				quantidade = nf_.format(itemVenda.getQuantidade().doubleValue());
			} else {
				quantidade = nf__.format(itemVenda.getQuantidade());
			}
			
			itemPedido.setQuantidade(quantidade);
			
			itemPedido.setUN(itemVenda.getProduto().getUnidadeMedida());
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}
		
		Double troco = 0D;
		List<Pagamento> listaPagamentos = pagamentos.todosPorVenda(vendaSelecionada, usuario_.getEmpresa());
		for (Pagamento pagamento : listaPagamentos) {
			
			troco += pagamento.getTroco().doubleValue();
				
			ItemEspelhoVendaPagamentos pagamentosPedido = new ItemEspelhoVendaPagamentos();
			pagamentosPedido.setFormaPagamento(pagamento.getFormaPagamento().getNome());
			pagamentosPedido.setValor(nf.format(pagamento.getValor().doubleValue()));
			
			pedido.getItensPagamentos().add(pagamentosPedido);
			
		}
		
		pedido.setConta(false);
		List<Conta> listaDeContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario_.getEmpresa());
		if(listaDeContas.size() > 0) {
			pedido.setConta(true);
		}
		
		if(listaPagamentos.size() == 0) {
			
			if(pedido.getConta() && vendaSelecionada.getTipoPagamento() == TipoPagamento.AVISTA) {
				List<Conta> listaContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario_.getEmpresa());
				
				Optional<Conta> conta = listaContas.stream().findFirst();
				
				ItemEspelhoVendaPagamento pagamentosPedido = new ItemEspelhoVendaPagamento();
				pagamentosPedido.setValorPagar(nf.format(conta.get().getValor().doubleValue()));
				pagamentosPedido.setVencimento(sdf.format(conta.get().getVencimento()));
				
				pedido.getItensPagamento().add(pagamentosPedido);
				
			} else if(pedido.getConta() && vendaSelecionada.getTipoPagamento() == TipoPagamento.PARCELADO) {
				List<Conta> listaContas = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario_.getEmpresa());
				
				listaContas.forEach(f -> {
					ItemEspelhoVendaParcelamentos parcelamentosPedido = new ItemEspelhoVendaParcelamentos();
					parcelamentosPedido.setParcela(f.getParcela());
					parcelamentosPedido.setValor(nf.format(f.getValor().doubleValue()));
					parcelamentosPedido.setVencimento(sdf.format(f.getVencimento()));
					
					if(f.getParcela().equals("Entrada")) { parcelamentosPedido.setStatus("✓"); }
		            
		            pedido.getItensParcelamentos().add(parcelamentosPedido);
		        });
				
			}
		}	
		
		pedido.setTipoPagamento(vendaSelecionada.getTipoPagamento().name());
				
		pedido.setTroco(nf.format(troco.doubleValue()));
		
		if(usuario_.getEmpresa().getLogoRelatorio() != null) {
			
			InputStream targetStream;
			try {
				targetStream = ByteSource.wrap(usuario_.getEmpresa().getLogoRelatorio()).openStream();
				pedido.setLogo(targetStream);
				
			} catch (IOException e1) {
			}	
		}
		
		pedido.setxNome(usuario_.getEmpresa().getNome() != null ? usuario_.getEmpresa().getNome() : "");
		pedido.setCNPJ(usuario_.getEmpresa().getCnpj() != null ? usuario_.getEmpresa().getCnpj() : "");
		pedido.setxLgr(usuario_.getEmpresa().getEndereco() != null ? usuario_.getEmpresa().getEndereco().trim() : "");
		pedido.setNro(usuario_.getEmpresa().getNumero() != null ? usuario_.getEmpresa().getNumero().trim() : "");
		pedido.setxBairro(usuario_.getEmpresa().getBairro() != null ? usuario_.getEmpresa().getBairro().trim() : "");
		pedido.setxMun(usuario_.getEmpresa().getCidade() != null ? usuario_.getEmpresa().getCidade() : "");
		pedido.setUF(usuario_.getEmpresa().getUf() != null ? usuario_.getEmpresa().getUf() : "");
		pedido.setContato(usuario_.getEmpresa().getContato() != null ? usuario_.getEmpresa().getContato() : "");
		
		pedido.setObservacoes(vendaSelecionada.getObservacao());
		
		pedido.setTotalVenda(nf.format(new BigDecimal(vendaSelecionada.getValorTotal().doubleValue()- vendaSelecionada.getDesconto().doubleValue())));
		
		pedido.setFrete(nf.format(vendaSelecionada.getTaxaDeEntrega()));
		
		if(vendaSelecionada.getDesconto() != null) {
			//aleteração do subtotal para somar com a taxa de entrega
			pedido.setSubTotal(nf.format(vendaSelecionada.getValorTotal().doubleValue()/* - vendaSelecionada.getTaxaDeEntrega().doubleValue()*/));
			pedido.setDesconto(nf.format(vendaSelecionada.getDesconto()));
		} else {
			pedido.setSubTotal(nf.format(vendaSelecionada.getValorTotal().doubleValue()/* - vendaSelecionada.getTaxaDeEntrega().doubleValue()*/));
		}

		List<EspelhoVenda> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
		try {
			
			String path = "";
			if(vendaSelecionada.getEmpresa().getId().equals(7111L)) {
				path = "/relatorios/decore-vendas_RECIBO_DECORE.jasper";
			} else {
				path = "/relatorios/decore-vendas_RECIBO_OURO_DA_AMAZONIA.jasper";
			}
			
			report.getRelatorio_(pedidos, "Venda-N" + vendaSelecionada.getNumeroVenda().longValue(), path);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void excluir() {

		if (vendaSelecionada != null) {
			
			
			List<Conta> contasTemp = contas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), "VENDA", usuario_.getEmpresa());
			for (Conta conta : contasTemp) {
				/* Comentado para exclusão temporaria */
				//conta.setExclusao(true);
				//contas.save(conta);
				
				/* Comentado para exclusão temporaria - desfeito */
				contas.remove(conta);
			}
			
			List<Pagamento> pagamentosTemp = pagamentos.todosPorVenda(vendaSelecionada, usuario_.getEmpresa());
			for (Pagamento pagamento : pagamentosTemp) {
				pagamentos.remove(pagamento);
			}
			
			/*
			Lancamento lancamento = lancamentos.porValor(vendaSelecionada.getLucro().setScale(2, BigDecimal.ROUND_HALF_EVEN), usuario_.getEmpresa());
			if(lancamento != null) {
				lancamentos.remove(lancamento);
			}
			*/
			
			List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
			for (ItemVenda itemVenda : itensVenda) {
					
				Produto produto = itemVenda.getProduto();
				
				/* Comentado para exclusão temporaria - desfeito */	
				if(produto.isEstoque()) {
					
					produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + itemVenda.getQuantidade().doubleValue()));

					List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompra : itensCompra) {
						
						if(vendaSelecionada.isPdv()) {
							
							List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							
							for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {	
								
								if (itemCompra.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId().longValue()) {
									if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
										System.out.println("Quantidade Disponivel: "+itemCompra.getQuantidadeDisponivel());
										System.out.println("Quantidade Retornada: "+itemVenda.getQuantidade());
										
										itemCompra.setQuantidadeDisponivel(new BigDecimal(
												itemCompra.getQuantidadeDisponivel().doubleValue() + itemVendaCompra.getQuantidade().doubleValue()));
										itensCompras.save(itemCompra);
									}
								}
							}		
							
						} else {
							
							if (itemCompra.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
								if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
									System.out.println(itemCompra.getQuantidadeDisponivel());
									System.out.println(itemVenda.getQuantidade());
									itemCompra.setQuantidadeDisponivel(new BigDecimal(
											itemCompra.getQuantidadeDisponivel().doubleValue() + itemVenda.getQuantidade().doubleValue()));
									itensCompras.save(itemCompra);
								}
							}
						}
					}
				}
				/* Comentado para exclusão temporaria - desfeito */	
				
				
				/* Comentado para exclusão temporaria - defeito */
				List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
				
				for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {
					itensVendasCompras.remove(itemVendaCompra);
				}

				itensVendas.remove(itemVenda);	
				/* Comentado para exclusão temporaria - desfeito */	
				
				/* Comentado para exclusão temporaria 
				List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
				
				for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {
					itemVendaCompra.setExclusao(true);
					itensVendasCompras.save(itemVendaCompra);
				}
	
				itemVenda.setExclusao(true);
				itensVendas.save(itemVenda);
				*/
				
				
				
				
				if(vendaSelecionada.isPdv()) {
					
					if(vendaSelecionada.isAjuste()) {
						
						if(!vendaSelecionada.isRecuperarValores()) {
							
							//List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
							/* Comentado para exclusão temporaria - defeito */
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemVenda.getQuantidade().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));					
							//}
						}
												
					} else {					
							
						if(produto.isEstoque()) {
							
							/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
							/*
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));											
							
							Object[] result = itensCompras.porQuantidadeDisponivel(produto);
							
							if((BigDecimal) result[0] != null) {
							
								Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
								
								//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
								
								produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
								
								produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
								
								produto.setCustoTotal((BigDecimal) result[1]);	
							}
							*/

							
							/*
							if(produto.getQuantidadeAtual().doubleValue() <= 0) {
								produto.setCustoMedioUnitario(BigDecimal.ZERO);
								
								if(produto.getCustoTotal().doubleValue() > 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() - produto.getCustoTotal().doubleValue()));													
									
								} else if(produto.getCustoTotal().doubleValue() < 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + (-1 * produto.getCustoTotal().doubleValue())));								
								
								} else {
									produto.setEstorno(BigDecimal.ZERO);
								}
								
								produto.setCustoTotal(BigDecimal.ZERO);
							}
							*/
						}
						
						
						List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(vendaSelecionada.getNumeroVenda(), TipoOperacao.VENDA, vendaSelecionada.getEmpresa());

						if(itensCaixa.size() > 0) {
							
							for (ItemCaixa itemCaixa : itensCaixa) {
								/* Comentado para exclusão temporaria
								itemCaixa.setExclusao(true);
								itensCaixas.save(itemCaixa);
								*/
								/* Comentado para exclusão temporaria - desfeito */	
								itensCaixas.remove(itemCaixa); 
							}
							
						}
						
						Pagamento pagamento = pagamentos.porVenda(vendaSelecionada, vendaSelecionada.getEmpresa());
						if(pagamento != null) {
							/* Comentado para exclusão temporaria 
							pagamento.setExclusao(true);
							pagamentos.save(pagamento);
							*/
							/* Comentado para exclusão temporaria - defeito */
							pagamentos.remove(pagamento); 
						}
						
						
					}
					
				} else {
					
					if(vendaSelecionada.isAjuste()) {
						
						if(!vendaSelecionada.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());												
							/* Comentado para exclusão temporaria - desfeito */
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemVenda.getQuantidade().doubleValue() * produto.getCustoMedioUnitario().doubleValue())));											
						}
												
					} else {					
						
						if(produto.isEstoque()) {
							
							/* RE-CALCULAR CUSTO MEDIO UNITARIO DOS PRODUTOS DESSA COMPRA */
							/*
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));											
							
							Object[] result = itensCompras.porQuantidadeDisponivel(produto);
							
							if((BigDecimal) result[0] != null) {
							
								Double estorno = ((BigDecimal) result[1]).doubleValue() - produto.getCustoTotal().doubleValue();
								
								//Double estorno = (produto.getQuantidadeAtual().longValue() * produto.getCustoMedioUnitario().doubleValue()) - produto.getCustoTotal().doubleValue();
								
								produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + estorno));
								
								produto.setCustoMedioUnitario(new BigDecimal(((BigDecimal) result[1]).doubleValue() / produto.getQuantidadeAtual().doubleValue()));
								
								produto.setCustoTotal((BigDecimal) result[1]);	
							}

							
							
							if(produto.getQuantidadeAtual().doubleValue() <= 0) {
								produto.setCustoMedioUnitario(BigDecimal.ZERO);
								
								if(produto.getCustoTotal().doubleValue() > 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() - produto.getCustoTotal().doubleValue()));													
									
								} else if(produto.getCustoTotal().doubleValue() < 0) {
									produto.setEstorno(new BigDecimal(produto.getEstorno().doubleValue() + (-1 * produto.getCustoTotal().doubleValue())));								
								
								} else {
									produto.setEstorno(BigDecimal.ZERO);
								}
								
								produto.setCustoTotal(BigDecimal.ZERO);
							}
							*/
						}				
					}
				}
				
				//produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / produto.getQuantidadeAtual().intValue()));
				produtos.save(produto);
			}
			
			
			List<Devolucao> listaDeDevolucoes = new ArrayList<Devolucao>();
			List<ItemDevolucao> itensDevolucao = itensDevolucoes.porVenda(vendaSelecionada);
			for (ItemDevolucao itemDevolucao : itensDevolucao) {
				
				Devolucao devolucao = devolucoes.porId(itemDevolucao.getDevolucao().getId());
				
				if(!listaDeDevolucoes.contains(devolucao)) {
					listaDeDevolucoes.add(devolucao);
				}
				/*
				List<ItemCaixa> itensCaixa = itensCaixas.porCodigoOperacao(itemDevolucao.getId(), TipoOperacao.DEVOLUCAO, vendaSelecionada.getEmpresa());

				if(itensCaixa.size() > 0) {
					
					for (ItemCaixa itemVenda_ : itensCaixa) {
						itensCaixas.remove(itemVenda_);
					}
					
				}
				*/
				
				/* Comentado para exclusão temporaria */	
				//itemDevolucao.setExclusao(true);
				//itensDevolucoes.save(itemDevolucao);
				
				/* Comentado para exclusão temporaria - desfeito */
				itensDevolucoes.remove(itemDevolucao); 
			}
			
			for (Devolucao devolucao : listaDeDevolucoes) {
				System.out.println(devolucao);
				//devolucoes.remove(devolucao);
			}
			

			entrega = entregas.porVenda(vendaSelecionada);
			
			if (entrega.getId() != null) {
				/* Comentado para exclusão temporaria 
				entrega.setExclusao(true);
				entregas.save(entrega);
				*/
				/* Comentado para exclusão temporaria - desfeito */
				entregas.remove(entrega); 
			}

			/* Comentado para exclusão temporaria 
			vendaSelecionada.setExclusao(true);
			vendas.save(vendaSelecionada);
			*/
			/* Comentado para exclusão temporaria - desfeito */
			vendas.remove(vendaSelecionada);
			
			Log log = new Log();
			log.setDataLog(new Date());
			log.setCodigoOperacao(String.valueOf(vendaSelecionada.getNumeroVenda()));
			log.setOperacao(TipoAtividade.VENDA.name());
			
			NumberFormat nf = new DecimalFormat("###,##0.00", REAL);
			
			log.setDescricao("Deletou venda, Nº " + vendaSelecionada.getNumeroVenda() + ", quantidade de itens " + vendaSelecionada.getQuantidadeItens() + ", valor total R$ " + nf.format(vendaSelecionada.getValorTotal()));
			log.setUsuario(usuario_);		
			logs.save(log);

			vendaSelecionada = null;

			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Venda excluída com sucesso!' });");

		}/* else {

			for (Venda venda : vendasFiltradas) {

				venda = vendas.porId(venda.getId());

				List<ItemVenda> itensVenda = itensVendas.porVenda(venda);

				for (ItemVenda itemVenda : itensVenda) {
					Produto produto = itemVenda.getProduto();
					produto = produtos.porId(produto.getId());
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemVenda.getQuantidade());
					produtos.save(produto);

					List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
					for (ItemCompra itemCompra : itensCompra) {

						if (itemCompra.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
							if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId()
									.longValue()) {
								System.out.println(itemCompra.getQuantidadeDisponivel());
								System.out.println(itemVenda.getQuantidade());
								itemCompra.setQuantidadeDisponivel(
										itemCompra.getQuantidadeDisponivel() + itemVenda.getQuantidade());
								itensCompras.save(itemCompra);
							}
						}
					}

					itensVendas.remove(itemVenda);
				}

				vendas.remove(venda);
			}

			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Venda excluída com sucesso!' });");

		}*/

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

	public Venda getVendaSelecionada() {
		return vendaSelecionada;
	}

	public void setVendaSelecionada(Venda vendaSelecionada) {
		this.vendaSelecionada = vendaSelecionada;
	}

	public List<Venda> getVendasFiltradas() {
		return vendasFiltradas;
	}

	public void setVendasFiltradas(List<Venda> vendasFiltradas) {
		this.vendasFiltradas = vendasFiltradas;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public int getVendasFiltradasSize() {
		return vendasFiltradas.size();
	}

	public String getTotalVendas() {
		return totalVendas;
	}

	public Integer getTotalItens() {
		return totalItens;
	}

	public Long getNumeroVenda() {
		return numeroVenda;
	}

	public void setNumeroVenda(Long numeroVenda) {
		this.numeroVenda = numeroVenda;
	}

	public boolean isVendasNormais() {
		return vendasNormais;
	}

	public void setVendasNormais(boolean vendasNormais) {
		this.vendasNormais = vendasNormais;
	}

	public StatusPedido[] getStatusPedidos() {
		return StatusPedido.values();
	}

	public StatusPedido[] getStatusPedido() {
		return statusPedido;
	}

	public void setStatusPedido(StatusPedido[] statusPedido) {
		this.statusPedido = statusPedido;
	}

	public Entrega getEntrega() {
		return entrega;
	}

	public void setEntrega(Entrega entrega) {
		this.entrega = entrega;
	}

	public Usuario getEntregador() {
		return entregador;
	}

	public void setEntregador(Usuario entregador) {
		this.entregador = entregador;
	}

	public List<Usuario> getTodosEntregadores() {
		return todosEntregadores;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<Cliente> getTodosClientes() {
		return todosClientes;
	}

	public boolean isVendasPagas() {
		return vendasPagas;
	}

	public void setVendasPagas(boolean vendasPagas) {
		this.vendasPagas = vendasPagas;
	}

}
