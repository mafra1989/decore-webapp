package com.webapp.controller;

import java.io.IOException;
import java.io.Serializable;
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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Entrega;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemPedido;
import com.webapp.model.ItemVenda;
import com.webapp.model.Pedido;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Entregas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaVendasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Venda> vendasFiltradas = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;

	@Inject
	private Vendas vendas;

	@Inject
	private Entregas entregas;

	@Inject
	private Entrega entrega;

	@Inject
	private ItensVendas itensVendas;

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

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			todosUsuarios = usuarios.todos();
		}
	}

	public void pesquisar() {
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		vendasFiltradas = vendas.vendasFiltradas(numeroVenda, dateStart, calendarioTemp.getTime(), vendasNormais,
				statusPedido, usuario);

		double totalVendasTemp = 0;
		totalItens = 0;
		for (Venda venda : vendasFiltradas) {		
			if(!venda.isAjuste()) {
				totalVendasTemp += venda.getValorTotal().doubleValue();
				totalItens += venda.getQuantidadeItens().intValue();
			}
		}

		totalVendas = nf.format(totalVendasTemp);

		vendaSelecionada = null;
	}

	public void prepararEntrega() {
		entrega = entregas.porVenda(vendaSelecionada);
	}

	public void entregarVenda() {
		entrega.setStatus(StatusPedido.ENTREGUE.name());
		entrega = entregas.save(entrega);

		Venda venda = entrega.getVenda();
		venda.setStatus(true);
		vendas.save(venda);

		PrimeFaces.current().executeScript("swal({ type: 'success', title: 'Concluído!', text: 'Venda N."
				+ vendaSelecionada.getNumeroVenda() + " entregue com sucesso!' });");

		pesquisar();
	}

	public void desfazerEntrega() {
		entrega.setStatus(StatusPedido.PENDENTE.name());
		entrega = entregas.save(entrega);

		Venda venda = entrega.getVenda();
		venda.setStatus(false);
		vendas.save(venda);

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

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		Pedido pedido = new Pedido();
		pedido.setVendaNum(vendaSelecionada.getNumeroVenda() + "");
		pedido.setTipoVenda(vendaSelecionada.getTipoVenda().getDescricao().toUpperCase());
		pedido.setBairro(vendaSelecionada.getBairro().getNome().toUpperCase());
		pedido.setDataVenda(sdf.format(vendaSelecionada.getDataVenda()));
		pedido.setVendedor(vendaSelecionada.getUsuario().getNome().toUpperCase());

		Entrega entrega = entregas.porVenda(vendaSelecionada);
		if (entrega != null) {
			pedido.setResponsavel(entrega.getNome());
			pedido.setLocalizacao(entrega.getLocalizacao());
			pedido.setObservacao(entrega.getObservacao());
		}
		
		List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
		for (ItemVenda itemVenda : itensVenda) {
			
			ItemPedido itemPedido = new ItemPedido();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			itemPedido.setDescricao(itemVenda.getProduto().getDescricao());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().intValue()));
			itemPedido.setQuantidade(String.valueOf(itemVenda.getQuantidade()));
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}

		pedido.setTotalVenda(nf.format(vendaSelecionada.getValorTotal()));

		List<Pedido> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<Pedido> report = new Relatorio<Pedido>();
		try {
			report.getRelatorio(pedidos, "Venda-N" + vendaSelecionada.getNumeroVenda().longValue());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void excluir() {

		if (vendaSelecionada != null) {

			List<ItemVenda> itensVenda = itensVendas.porVenda(vendaSelecionada);
			for (ItemVenda itemVenda : itensVenda) {
				Produto produto = itemVenda.getProduto();
				produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemVenda.getQuantidade());
				produtos.save(produto);

				List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
				for (ItemCompra itemCompra : itensCompra) {

					if (itemCompra.getCompra().getId().longValue() == itemVenda.getCompra().getId().longValue()) {
						if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
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

			if (entrega.getId() != null) {
				entregas.remove(entrega);
			}

			vendas.remove(vendaSelecionada);

			vendaSelecionada = null;

			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Venda excluída com sucesso!' });");

		} else {

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

}
