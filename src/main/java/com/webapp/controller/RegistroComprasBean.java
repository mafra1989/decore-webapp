package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import org.primefaces.PrimeFaces;

import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.FormaPagamento;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.PeriodoPagamento;
import com.webapp.model.Produto;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
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
	private FormasPagamentos formasPagamentos;

	private List<FormaPagamento> todasFormasPagamentos;

	@Inject
	private Contas contas;

	private List<Conta> todasContas = new ArrayList<>();

	private TipoPagamento tipoPagamento = TipoPagamento.AVISTA;

	@NotNull
	private Long parcelas = 3L;

	private PeriodoPagamento periodoPagamento = PeriodoPagamento.MESES;

	private boolean contaAPagar;
	
	private boolean compraPaga = true;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todosUsuarios = usuarios.todos();
			listarTodos();
		}
	}

	public void pesquisar() {
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

			todasContas = new ArrayList<>();
			PrimeFaces.current().executeScript("PF('confirmDialog').show();");

		} else {
			PrimeFaces.current().executeScript(
					"swal({ type: 'warning', title: 'Atenção!', text: 'Adicione pelo menos um item à compra!' });");
		}

	}

	public void gerarParcelas() {

		todasContas = new ArrayList<>();

		Calendar vencimento = Calendar.getInstance();
		vencimento.setTime(compra.getDataCompra());

		Double valorParcela = compra.getValorTotal().doubleValue() / parcelas;
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
			conta.setOperacao("COMPRA");
			conta.setParcela((i + 1) + "/" + parcelas);
			conta.setTipo("DEBITO");
			conta.setStatus(false);
			conta.setValor(new BigDecimal(valorParcela));
			conta.setVencimento(vencimento.getTime());

			todasContas.add(conta);
		}
	}

	public void confirmarCompra() {

		boolean contasPagas = false;

		if (compra.getId() != null) {
			//List<Conta> contasTemp = contas.porContasPagas(compra.getNumeroCompra(), "COMPRA");

			//if (contasTemp.size() == 0) {

			List<Conta> contasTemp = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA");
			for (Conta conta : contasTemp) {
				contas.remove(conta);
			}

			/*} else {
				contasPagas = true;
				PrimeFaces.current().executeScript(
						"stop();swal({ type: 'error', title: 'Erro!', text: 'Existe contas à pagar já registradas para essa compra!' });");
			}
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

			Compra compraTemp = compras.ultimoNCompra();

			if (compraTemp == null) {
				compra.setNumeroCompra(1L);
			} else {
				if (compra.getId() == null) {
					compra.setNumeroCompra(compraTemp.getNumeroCompra() + 1);
				}
			}

			compra = compras.save(compra);

			for (ItemCompra itemCompra : itensCompra) {
				itemCompra.setCompra(compra);
				itensCompras.save(itemCompra);

				Produto produto = produtos.porId(itemCompra.getProduto().getId());
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemCompra.getQuantidade());
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
				conta.setVencimento(compra.getDataCompra());

				contas.save(conta);

			} else {
				for (Conta conta : todasContas) {
					conta.setCodigoOperacao(compra.getNumeroCompra());
					conta = contas.save(conta);
				}
			}

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
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Produto já foi adicionado!' });");
		}

	}

	public void removeItem() {
		List<ItemVenda> itensVenda = itensVendas.porCompra(compra, itemSelecionado);

		if (itensVenda.size() == 0) {
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

		if (itensVenda.size() == 0) {
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

	private void listarTodos() {
		todasFormasPagamentos = formasPagamentos.todos();
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

	public List<FormaPagamento> getTodasFormasPagamentos() {
		return todasFormasPagamentos;
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
}
