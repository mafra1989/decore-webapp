package com.webapp.controller;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Produto;
import com.webapp.model.TipoPagamento;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ConsultaComprasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Compra> comprasFiltradas = new ArrayList<>();

	private List<Usuario> todosUsuarios;

	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;

	@Inject
	private Compras compras;

	@Inject
	private ItensVendas itensVendas;

	@Inject
	private ItensCompras itensCompras;

	@Inject
	private Produtos produtos;
	
	@Inject
	private Contas contas;
	
	private Long numeroCompra;

	private Compra compraSelecionada;

	private Date dateStart = new Date();

	private Date dateStop = new Date();

	private NumberFormat nf = new DecimalFormat("###,##0.00");

	private String totalCompras = "0,00";

	private Integer totalItens = 0;

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

		comprasFiltradas = compras.comprasFiltradas(numeroCompra, dateStart, calendarioTemp.getTime(), usuario);

		double totalComprasTemp = 0; double valorTotal = 0; double valorTotalTemp = 0;
		totalItens = 0;
		for (Compra compra : comprasFiltradas) {
			totalComprasTemp += compra.getValorTotal().doubleValue();
			totalItens += compra.getQuantidadeItens().intValue();
			
			if(compra.getNumeroCompra().intValue() == 12 && compra.getId() == 28592) {
				compra.setNumeroCompra(7L);
				compras.save(compra);
			}
			
			List<Conta> listaDeContas = contas.porCodigoOperacao(compra.getNumeroCompra(), "COMPRA");
			if(listaDeContas.size() > 0) {

				for (Conta contaTemp : listaDeContas) {
					valorTotalTemp += contaTemp.getValor().doubleValue(); 
					System.out.println("Valor: " + valorTotalTemp);
					contas.remove(contaTemp);
				}
			}
				
			
			if(!compra.isAjuste()) {
				
				Conta conta = new Conta();
				conta.setCodigoOperacao(compra.getNumeroCompra());
				conta.setOperacao("COMPRA");
				conta.setParcela(TipoPagamento.AVISTA.name());
				conta.setTipo("DEBITO");
				conta.setStatus(true);
				conta.setValor(compra.getValorTotal());
				
				conta.setVencimento(compra.getDataCompra());
				conta.setPagamento(compra.getDataCompra());
				
				calendarioTemp = Calendar.getInstance();
				calendarioTemp.setTime(conta.getVencimento());
				
				conta.setDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendarioTemp.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendarioTemp.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendarioTemp.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendarioTemp.get(Calendar.YEAR))));

				conta = contas.save(conta);	
				System.out.println(conta.getId());
				valorTotal += conta.getValor().doubleValue(); 
				System.out.println(valorTotal);
			}
					
		}

		totalCompras = nf.format(totalComprasTemp);
	}

	public void excluir() {

		if (compraSelecionada != null) {

			List<ItemVenda> itensVenda = itensVendas.porCompra(compraSelecionada);

			if (itensVenda.size() == 0) {

				boolean contasPagas = false;
				//List<Conta> contasTemp = contas.porContasPagas(compraSelecionada.getNumeroCompra(), "COMP");

				//if (contasTemp.size() == 0) {

				List<Conta> contasTemp = contas.porCodigoOperacao(compraSelecionada.getNumeroCompra(), "COMPRA");
				for (Conta conta : contasTemp) {
					contas.remove(conta);
				}

				/*} else {
					contasPagas = true;
					PrimeFaces.current().executeScript(
							"stop();swal({ type: 'error', title: 'Erro!', text: 'Existe contas à pagar já registradas para essa compra!' });");
				}*/

				if (contasPagas != true) {
					
					List<ItemCompra> itensCompra = itensCompras.porCompra(compraSelecionada);
					for (ItemCompra itemCompra : itensCompra) {
						Produto produto = itemCompra.getProduto();
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
						produtos.save(produto);

						itensCompras.remove(itemCompra);
					}

					compras.remove(compraSelecionada);

					compraSelecionada = null;
					pesquisar();
					PrimeFaces.current().executeScript(
							"swal({ type: 'success', title: 'Concluído!', text: 'Compra excluída com sucesso!' });");
				}
				
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Existem itens dessa compra já vinculados a uma ou mais vendas!' });");
			}

		} else {

			for (Compra compra : comprasFiltradas) {
				List<ItemVenda> itensVenda = itensVendas.porCompra(compra);

				if (itensVenda.size() == 0) {

					List<ItemCompra> itensCompra = itensCompras.porCompra(compra);
					for (ItemCompra itemCompra : itensCompra) {
						Produto produto = itemCompra.getProduto();
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemCompra.getQuantidade());
						produtos.save(produto);

						itensCompras.remove(itemCompra);
					}

					compras.remove(compra);
				}
			}

			pesquisar();
			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Compras excluídas com sucesso!' });");
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

	public Compra getCompraSelecionada() {
		return compraSelecionada;
	}

	public void setCompraSelecionada(Compra compraSelecionada) {
		this.compraSelecionada = compraSelecionada;
	}

	public List<Compra> getComprasFiltradas() {
		return comprasFiltradas;
	}

	public void setComprasFiltradas(List<Compra> comprasFiltradas) {
		this.comprasFiltradas = comprasFiltradas;
	}

	public int getComprasFiltradasSize() {
		return comprasFiltradas.size();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getTotalCompras() {
		return totalCompras;
	}

	public Integer getTotalItens() {
		return totalItens;
	}

	public Long getNumeroCompra() {
		return numeroCompra;
	}

	public void setNumeroCompra(Long numeroCompra) {
		this.numeroCompra = numeroCompra;
	}

}
