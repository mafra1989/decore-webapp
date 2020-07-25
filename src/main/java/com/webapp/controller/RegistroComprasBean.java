package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.PeriodoPagamento;
import com.webapp.model.Produto;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.filter.ProdutoFilter;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class RegistroComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Compra compra;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Produtos produtos;

	@Inject
	private Compras compras;

	@Inject
	private ItensCompras itensCompras;

	private List<Usuario> todosUsuarios;

	private List<Produto> produtosFiltrados;

	@Inject
	private ItemCompra itemCompra;

	private List<ItemCompra> itensCompra = new ArrayList<ItemCompra>();

	private ProdutoFilter filter = new ProdutoFilter();

	private ItemCompra itemSelecionado;

	@Inject
	private ItensVendas itensVendas;
	
	@Inject
	private ItensVendasCompras itensVendasCompras;

	@Inject
	private Contas contas;

	private List<Conta> todasContas = new ArrayList<>();

	private TipoPagamento tipoPagamento = TipoPagamento.AVISTA;

	@NotNull
	private Long parcelas = 3L;

	private PeriodoPagamento periodoPagamento = PeriodoPagamento.MESES;

	private boolean contaAPagar;

	private boolean compraPaga = true;

	private Double valorEntrada;
	
	private List<Conta> entradas = new ArrayList<>();
	
	@Inject
	private Usuario usuario;

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
			
			todosUsuarios = usuarios.todos(usuario.getEmpresa());
		}
	}

	public void pesquisar() {
		filter.setEmpresa(usuario.getEmpresa());
		produtosFiltrados = produtos.filtrados(filter);
		System.out.println(produtosFiltrados.size());
	}

	public void buscar() {
		compra = compras.porId(compra.getId());
		itensCompra = itensCompras.porCompra(compra);

		for (ItemCompra itemCompra : itensCompra) {
			itemCompra.setCode(itemCompra.getProduto().getCodigo());
		}
	}

	public void changePagamento() {
		if (tipoPagamento == TipoPagamento.AVISTA) {
			contaAPagar = false;
			PrimeFaces.current().executeScript("ocultar();");
		} else {
			contaAPagar = true;
			PrimeFaces.current().executeScript("mostrar();");
		}
	}

	public void salvar() {

		if (itensCompra.size() > 0) {
			
			if(compra.isAjuste()) {
				
				Long totalDeItens = 0L;
				double valorTotal = 0;
				
				if (compra.getId() != null) {
					List<Conta> contasTemp = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA", usuario.getEmpresa());
					for (Conta conta : contasTemp) {
						contas.remove(conta);
					}
				}
				
				Calendar calendario = Calendar.getInstance();
				Calendar calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(compra.getDataCompra());
				calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
				calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
				calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
				compra.setDataCompra(calendarioTemp.getTime());

				boolean edit = false;
				if (compra.getId() != null) {
					edit = true;

					List<ItemCompra> itemCompraTemp = itensCompras.porCompra(compra);

					for (ItemCompra itemCompra : itemCompraTemp) {
						Produto produto = produtos.porId(itemCompra.getProduto().getId());
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
						produtos.save(produto);

						itensCompras.remove(itemCompra);
					}
				}

				compra.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				compra.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				compra.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				compra.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				compra.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				Compra compraTemp = compras.ultimoNCompra(usuario.getEmpresa());

				if (compraTemp == null) {
					compra.setNumeroCompra(1L);
				} else {
					if (compra.getId() == null) {
						compra.setNumeroCompra(compraTemp.getNumeroCompra() + 1);
					}
				}
				
				compra.setEmpresa(usuario.getEmpresa());
				compra = compras.save(compra);

				for (ItemCompra itemCompra : itensCompra) {
					itemCompra.setCompra(compra);
					itensCompras.save(itemCompra);

					Produto produto = produtos.porId(itemCompra.getProduto().getId());
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemCompra.getQuantidade());
					
					System.out.println("Custo médio Un.: " + produto.getCustoMedioUnitario().doubleValue());
					if(produto.getCustoMedioUnitario().doubleValue() > 0) {
						/* Atualizar custo total */
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * produto.getCustoMedioUnitario().doubleValue())));
					} else {
						/* Atualizar custo total e custo medio un. */
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * itemCompra.getValorUnitario().doubleValue())));					
						
						Long saldo = (Long) itensCompras.saldoPorProduto(produto);
						System.out.println(produto.getCustoTotal().doubleValue() + " / " + saldo.longValue());
						produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / saldo.longValue()));
					}
					
					produtos.save(produto);

					totalDeItens += itemCompra.getQuantidade();
					valorTotal += itemCompra.getTotal().doubleValue();
				}

				compra.setValorTotal(BigDecimal.valueOf(valorTotal));
				compra.setQuantidadeItens(totalDeItens);
				compra = compras.save(compra);
				
				if (!edit) {
					PrimeFaces.current().executeScript(
							"stop();PF('confirmDialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
									+ compra.getNumeroCompra() + " registrada com sucesso!' });");

					compra = new Compra();
					itensCompra = new ArrayList<ItemCompra>();
					itemCompra = new ItemCompra();
					itemSelecionado = null;
					todasContas = new ArrayList<>();
					tipoPagamento = TipoPagamento.AVISTA;
					parcelas = 2L;
					periodoPagamento = PeriodoPagamento.MESES;
					contaAPagar = false;
					compraPaga = true;
					
					entradas = new ArrayList<>();
					valorEntrada = null;

				} else {
					PrimeFaces.current().executeScript(
							"stop();PF('confirmDialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
									+ compra.getNumeroCompra() + " atualizada com sucesso!' });");
				}
				
			} else {
				todasContas = new ArrayList<>();
				
				entradas = new ArrayList<>();
				valorEntrada = null;
				
				PrimeFaces.current().executeScript("PF('confirmDialog').show();");
			}

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à compra!' });");
		}

	}

	public void zerarParcelas() {
		todasContas = new ArrayList<>();
	}

	public void gerarParcelas() {

		todasContas = new ArrayList<>();
		
		entradas = new ArrayList<>();

		Calendar calendario = Calendar.getInstance();
		Calendar vencimento = Calendar.getInstance();
		vencimento.setTime(compra.getDataCompra());
		vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
		vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
		vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));

		Double valorCompra = compra.getValorTotal().doubleValue();
		if (valorEntrada != null && valorEntrada > 0) {
			valorCompra = compra.getValorTotal().doubleValue() - valorEntrada;

			Conta conta = new Conta();
			conta.setParcela("Entrada");
			conta.setValor(new BigDecimal(valorEntrada));
			conta.setVencimento(vencimento.getTime());
			
			conta.setPagamento(vencimento.getTime());
			 
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(conta.getVencimento());
			
			conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			entradas.add(conta);
		}

		Double valorParcela = valorCompra / parcelas;

		if (valorParcela > 0) {
			for (int i = 0; i < parcelas; i++) {

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

				if (periodoPagamento == PeriodoPagamento.DIAS) {
					dias = i + 1;
					vencimento.add(Calendar.DAY_OF_MONTH, (int) dias);
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

					conta.setValor(new BigDecimal(valorCompra - valorTemp));

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

	public void confirmarCompra() {

		boolean contasPagas = false;

		if (compra.getId() != null) {
			// List<Conta> contasTemp = contas.porContasPagas(compra.getNumeroCompra(),
			// "COMPRA");

			// if (contasTemp.size() == 0) {

			List<Conta> contasTemp = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA", usuario.getEmpresa());
			for (Conta conta : contasTemp) {
				contas.remove(conta);
			}

			/*
			 * } else { contasPagas = true; PrimeFaces.current().executeScript(
			 * "stop();swal({ type: 'error', title: 'Erro!', text: 'Existe contas à pagar já registradas para essa compra!' });"
			 * ); }
			 */

		}

		if (contasPagas != true) {

			Long totalDeItens = 0L;
			double valorTotal = 0;

			Calendar calendario = Calendar.getInstance();
			Calendar calendarioTemp = Calendar.getInstance();
			calendarioTemp.setTime(compra.getDataCompra());
			calendarioTemp.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
			calendarioTemp.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
			calendarioTemp.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
			compra.setDataCompra(calendarioTemp.getTime());

			boolean edit = false;
			if (compra.getId() != null) {
				edit = true;

				List<ItemCompra> itemCompraTemp = itensCompras.porCompra(compra);

				for (ItemCompra itemCompra : itemCompraTemp) {
					Produto produto = produtos.porId(itemCompra.getProduto().getId());
					produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
					produtos.save(produto);

					itensCompras.remove(itemCompra);
				}
			}

			compra.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
			compra.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
			compra.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
			compra.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
			compra.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

			Compra compraTemp = compras.ultimoNCompra(usuario.getEmpresa());

			if (compraTemp == null) {
				compra.setNumeroCompra(1L);
			} else {
				if (compra.getId() == null) {
					compra.setNumeroCompra(compraTemp.getNumeroCompra() + 1);
				}
			}

			compra.setEmpresa(usuario.getEmpresa());
			compra = compras.save(compra);

			for (ItemCompra itemCompra : itensCompra) {
				System.out.println(itemCompra.getId());
				itemCompra.setCompra(compra);
				itensCompras.save(itemCompra);

				Produto produto = produtos.porId(itemCompra.getProduto().getId());
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemCompra.getQuantidade());
						
				System.out.println("Ajuste.: " + !compra.isAjuste());
				if(!compra.isAjuste()) {
					/* Atualizar custo total e custo medio un. */
					produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * itemCompra.getValorUnitario().doubleValue())));					
					
					Long saldo = (Long) itensCompras.saldoPorProduto(produto);
					System.out.println(produto.getCustoTotal().doubleValue() + " / " + saldo.longValue());
					produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / saldo.longValue()));
				
				} else {
					System.out.println("Custo médio Un.: " + produto.getCustoMedioUnitario().doubleValue());
					if(produto.getCustoMedioUnitario().doubleValue() > 0) {
						/* Atualizar custo total */
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * produto.getCustoMedioUnitario().doubleValue())));
					} else {
						/* Atualizar custo total e custo medio un. */
						produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + (itemCompra.getQuantidade().longValue() * itemCompra.getValorUnitario().doubleValue())));					
						
						Long saldo = (Long) itensCompras.saldoPorProduto(produto);
						System.out.println(produto.getCustoTotal().doubleValue() + " / " + saldo.longValue());
						produto.setCustoMedioUnitario(new BigDecimal(produto.getCustoTotal().doubleValue() / saldo.longValue()));
					}					
				}
				/**/
				produtos.save(produto);

				totalDeItens += itemCompra.getQuantidade();
				valorTotal += itemCompra.getTotal().doubleValue();
			}

			compra.setValorTotal(BigDecimal.valueOf(valorTotal));
			compra.setQuantidadeItens(totalDeItens);
			compra = compras.save(compra);

			if (tipoPagamento == TipoPagamento.AVISTA) {

				Conta conta = new Conta();
				conta.setCodigoOperacao(compra.getNumeroCompra());
				conta.setOperacao("COMPRA");
				conta.setParcela(tipoPagamento.name());
				conta.setTipo("DEBITO");
				conta.setStatus(compraPaga != true ? false : true);
				conta.setValor(compra.getValorTotal());
				
				calendario = Calendar.getInstance();
				Calendar vencimento = Calendar.getInstance();
				vencimento.setTime(compra.getDataCompra());
				vencimento.set(Calendar.HOUR, calendario.get(Calendar.HOUR));
				vencimento.set(Calendar.MINUTE, calendario.get(Calendar.MINUTE));
				vencimento.set(Calendar.SECOND, calendario.get(Calendar.SECOND));
				
				conta.setVencimento(vencimento.getTime());

				conta.setPagamento(compraPaga != true ? null : vencimento.getTime());
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getVencimento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				conta.setEmpresa(usuario.getEmpresa());
				contas.save(conta);

			} else {
				
				for (Conta conta : entradas) {

					conta.setCodigoOperacao(compra.getNumeroCompra());
					conta.setOperacao("COMPRA");
					conta.setTipo("DEBITO");
					conta.setStatus(true);

					conta.setEmpresa(usuario.getEmpresa());
					contas.save(conta);
				}
				
				for (Conta conta : todasContas) {
					conta.setCodigoOperacao(compra.getNumeroCompra());
					conta.setOperacao("COMPRA");
					conta.setTipo("DEBITO");
					conta.setPagamento(null);
					
					conta.setEmpresa(usuario.getEmpresa());
					conta = contas.save(conta);
				}
			}

			if (!edit) {
				PrimeFaces.current().executeScript(
						"stop();PF('confirmDialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
								+ compra.getNumeroCompra() + " registrada com sucesso!' });");
				
				Compra compraTemp_ = new Compra();
				compraTemp_.setUsuario(compra.getUsuario());
				
				compra = new Compra();
				itensCompra = new ArrayList<ItemCompra>();
				itemCompra = new ItemCompra();
				itemSelecionado = null;
				todasContas = new ArrayList<>();
				tipoPagamento = TipoPagamento.AVISTA;
				parcelas = 2L;
				periodoPagamento = PeriodoPagamento.MESES;
				contaAPagar = false;
				compraPaga = true;
				
				entradas = new ArrayList<>();
				valorEntrada = null;
				
				compra = compraTemp_;

			} else {
				PrimeFaces.current().executeScript(
						"stop();PF('confirmDialog').hide(); swal({ type: 'success', title: 'Concluído!', text: 'Compra N."
								+ compra.getNumeroCompra() + " atualizada com sucesso!' });");
			}
		}
	}

	public void selecionarProduto(Produto produto) {
		itemCompra = new ItemCompra();
		itemCompra.setProduto(produto);
		itemCompra.setCode(produto.getCodigo());
	}

	public void adicionarItem() {
		
		if (!itensCompra.contains(itemCompra)) {
			
			if(itemCompra.getQuantidade() > 0) {
				
				if(itemCompra.getValorUnitario().doubleValue() >= 0) {
					itemCompra.setTotal(BigDecimal
							.valueOf(itemCompra.getValorUnitario().doubleValue() * itemCompra.getQuantidade().longValue()));
					itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade());
					itemCompra.setCompra(compra);
					itensCompra.add(itemCompra);

					compra.setValorTotal(
							BigDecimal.valueOf(compra.getValorTotal().doubleValue() + itemCompra.getTotal().doubleValue()));

					itemCompra = new ItemCompra();
				} else {
					
					PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Valor unitário não pode ser menor que zero!' });");
				}
				
			} else {
				PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Quantidade não pode ser menor ou igual a zero!' });");
			}

		} else {
			PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto já foi adicionado!' });");
		}
	}

	public void removeItem() {
		List<ItemVenda> itensVenda = itensVendas.porCompra(compra, itemSelecionado);
		List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porCompra(itemSelecionado.getCompra());

		if (itensVenda.size() == 0 && itensVendaCompra.size() == 0) {

			compra.setValorTotal(BigDecimal
					.valueOf(compra.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensCompra.remove(itemSelecionado);
			itemSelecionado = null;
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Este item já está vinculado a uma ou mais vendas!' });");
		}

	}

	public void editarItem() {
		List<ItemVenda> itensVenda = itensVendas.porCompra(compra, itemSelecionado);
		List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porCompra(itemSelecionado.getCompra());

		if (itensVenda.size() == 0 && itensVendaCompra.size() == 0) {

			itemCompra = itemSelecionado;
			compra.setValorTotal(BigDecimal
					.valueOf(compra.getValorTotal().doubleValue() - itemSelecionado.getTotal().doubleValue()));
			itensCompra.remove(itemSelecionado);
			itemSelecionado = null;
		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'error', title: 'Erro!', text: 'Este item já está vinculado a uma ou mais vendas!' });");
		}

	}

	public List<Usuario> getTodosUsuarios() {
		return todosUsuarios;
	}

	public List<ItemCompra> getItensCompra() {
		return itensCompra;
	}

	public Compra getCompra() {
		return compra;
	}

	public ItemCompra getItemCompra() {
		return itemCompra;
	}

	public void setItemCompra(ItemCompra itemCompra) {
		this.itemCompra = itemCompra;
	}

	public void setCompra(Compra compra) {
		this.compra = compra;
	}

	public ProdutoFilter getFilter() {
		return filter;
	}

	public void setFilter(ProdutoFilter filter) {
		this.filter = filter;
	}

	public List<Produto> getProdutosFiltrados() {
		return produtosFiltrados;
	}

	public ItemCompra getItemSelecionado() {
		return itemSelecionado;
	}

	public void setItemSelecionado(ItemCompra itemSelecionado) {
		this.itemSelecionado = itemSelecionado;
	}

	public int getItensCompraSize() {
		return itensCompra.size();
	}

	public List<Conta> getTodasContas() {
		return todasContas;
	}

	public boolean isContaAPagar() {
		return contaAPagar;
	}

	public void setContaAPagar(boolean contaAPagar) {
		this.contaAPagar = contaAPagar;
	}

	public TipoPagamento[] getTiposPagamentos() {
		return TipoPagamento.values();
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

	public boolean isCompraPaga() {
		return compraPaga;
	}

	public void setCompraPaga(boolean compraPaga) {
		this.compraPaga = compraPaga;
	}

	public Double getValorEntrada() {
		return valorEntrada;
	}

	public void setValorEntrada(Double valorEntrada) {
		this.valorEntrada = valorEntrada;
	}
}
