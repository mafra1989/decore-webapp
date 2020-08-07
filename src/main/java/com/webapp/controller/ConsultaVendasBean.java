package com.webapp.controller;

import java.io.IOException;
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

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Entrega;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemEspelhoVenda;
import com.webapp.model.ItemVenda;
import com.webapp.model.ItemVendaCompra;
import com.webapp.model.EspelhoVenda;
import com.webapp.model.Produto;
import com.webapp.model.StatusPedido;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.report.Relatorio;
import com.webapp.repository.Entregas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.ItensVendasCompras;
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

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
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
	
	private String empresa = "";
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			
			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();		
			usuario_ = usuarios.porNome(user.getUsername());
			
			List<Grupo> grupos = usuario_.getGrupos();
			
			if(grupos.size() > 0) {
				for (Grupo grupo : grupos) {
					if(grupo.getNome().equals("ADMINISTRADOR")) {
						EmpresaBean empresaBean = (EmpresaBean) FacesUtil.getObjectSession("empresaBean");
						if(empresaBean != null && empresaBean.getEmpresa() != null) {
							usuario_.setEmpresa(empresaBean.getEmpresa());
						}
					}
				}
			}
			
			todosUsuarios = usuarios.todos(usuario_.getEmpresa());
			
			if(!empresa.equals(usuario_.getEmpresa())) {
				
				if(!empresa.equals("")) {
					pesquisar();
				} 
			}
		}
	}

	public void pesquisar() {
		
		if(!empresa.equals(usuario_.getEmpresa())) {			
			empresa = usuario_.getEmpresa();
		}
		
		Calendar calendarioTemp = Calendar.getInstance();
		calendarioTemp.setTime(dateStop);
		calendarioTemp.set(Calendar.HOUR, 23);
		calendarioTemp.set(Calendar.MINUTE, 59);
		calendarioTemp.set(Calendar.SECOND, 59);

		vendasFiltradas = vendas.vendasFiltradas(numeroVenda, dateStart, calendarioTemp.getTime(), vendasNormais,
				statusPedido, usuario, usuario_.getEmpresa());

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

		EspelhoVenda pedido = new EspelhoVenda();
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
			
			ItemEspelhoVenda itemPedido = new ItemEspelhoVenda();
			itemPedido.setCodigo(itemVenda.getProduto().getCodigo());
			itemPedido.setDescricao(itemVenda.getProduto().getDescricao());
			itemPedido.setValorUnitario(nf.format(itemVenda.getValorUnitario().intValue()));
			itemPedido.setQuantidade(String.valueOf(itemVenda.getQuantidade()));
			itemPedido.setSubTotal(nf.format(itemVenda.getTotal()));
			
			pedido.getItensPedidos().add(itemPedido);
		}

		pedido.setTotalVenda(nf.format(vendaSelecionada.getValorTotal()));

		List<EspelhoVenda> pedidos = new ArrayList<>();
		pedidos.add(pedido);

		Relatorio<EspelhoVenda> report = new Relatorio<EspelhoVenda>();
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
				
				if(vendaSelecionada.isPdv()) {
					
					if(vendaSelecionada.isAjuste()) {
						
						if(!vendaSelecionada.isRecuperarValores()) {
							
							//List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
							//}
						}
												
					} else {					
					
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							//List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
							//}
						} else {
							
							//List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
							//for (ItemVendaCompra itemVendaCompra : itensVendaCompra) {
								
								//BigDecimal subtotal = BigDecimal.valueOf( 
											//itemVenda.getValorUnitario().doubleValue() * itemVendaCompra.getQuantidade().longValue());					
								//BigDecimal total = new BigDecimal(subtotal.doubleValue() - (subtotal.doubleValue() * (itemVenda.getDesconto().doubleValue() / 100)));		
								
								BigDecimal total = itemVenda.getTotal();
								produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + total.doubleValue()));					
								
							//}
						}
					}
					
				} else {
					
					if(vendaSelecionada.isAjuste()) {
						
						if(!vendaSelecionada.isRecuperarValores()) {
							//ItemCompra itemCompra = itensCompras.porCompra(itemVenda.getCompra(), itemVenda.getProduto());						
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
						}
												
					} else {					
					
						/* Deseja recuperar esses valores ? Se sim, Então os valores sub-totais de cada produto dessa venda
						 * serão somados 
						 * aos valores totais das próximas entradas de cada um desses produtos. Obs: O custo médio
						 * desses produtos sofrerão aumento proporcional aos seus respectivos valores sub-totais dessa venda. 
						 * */
						if(itemVenda.getLucro().doubleValue() >= 0) {
							
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + itemVenda.getValorCompra().doubleValue()));					
							
						} else {
							
							//BigDecimal total = BigDecimal.valueOf( 
											//itemVenda.getValorUnitario().doubleValue() * itemVenda.getQuantidade().longValue());					
							
							BigDecimal total = itemVenda.getTotal();
							produto.setCustoTotal(new BigDecimal(produto.getCustoTotal().doubleValue() + total.doubleValue()));					
																		
						}
					}
				}
				
				produtos.save(produto);

				List<ItemCompra> itensCompra = itensCompras.porProduto(itemVenda.getProduto());
				for (ItemCompra itemCompra : itensCompra) {
					
					if(vendaSelecionada.isPdv()) {
						
						List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
						
						for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {	
							
							if (itemCompra.getCompra().getId().longValue() == itemVendaCompra.getCompra().getId().longValue()) {
								if (itemCompra.getProduto().getId().longValue() == itemVenda.getProduto().getId().longValue()) {
									System.out.println("Quantidade Disponivel: "+itemCompra.getQuantidadeDisponivel());
									System.out.println("Quantidade Retornada: "+itemVenda.getQuantidade());
									
									itemCompra.setQuantidadeDisponivel(
											itemCompra.getQuantidadeDisponivel() + itemVendaCompra.getQuantidade());
									itensCompras.save(itemCompra);
								}
							}
						}		
						
					} else {
						
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
				}
				
				List<ItemVendaCompra> itensVendaCompra = itensVendasCompras.porItemVenda(itemVenda);
				
				for(ItemVendaCompra itemVendaCompra : itensVendaCompra) {
					itensVendasCompras.remove(itemVendaCompra);
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

}
