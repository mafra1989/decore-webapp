package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Caixa;
import com.webapp.model.FormaPagamento;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCaixa;
import com.webapp.model.TipoOperacao;
import com.webapp.model.Usuario;
import com.webapp.repository.Caixas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.ItensCaixas;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class CaixaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Usuarios usuarios;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Caixa caixa;
	
	@Inject
	private Caixas caixas;
	
	@Inject
	private ItensCaixas itensCaixas;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	@Inject
	private FormaPagamento formaPagamento;
	
	private List<FormaPagamento> todasFormasPagamentos = new ArrayList<FormaPagamento>();
	
	private List<ItemCaixa> itensCaixaFiltrados = new ArrayList<>();

	private ItemCaixa itemCaixaSelecionado;
	
	private boolean caixaAberto = false;
	
	
	private Double totalEmDinheiro = 0D;
	
	private Double totalEmVendas = 0D;
	
	private Double totalEmVendasEmDinheiro = 0D;
	
	private Double totalEmVendasDebitoCredito = 0D;
	
	private Double totalEmDevolucaoTrocas = 0D;
	
	private Double totalDespesas = 0D;
	
	private Double totalReceitas = 0D;
	

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {
			// todosUsuarios = usuarios.todos();
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
			
			caixa = caixas.porUsuario(usuario, usuario.getEmpresa());
			if(caixa != null) {
				itensCaixaFiltrados = itensCaixas.porCaixa(caixa);
				caixaAberto = true;
				
				Double saldoInicial = 0D;
				totalEmDinheiro = 0D;
				totalEmVendas = 0D;
				totalEmVendasEmDinheiro = 0D;
				totalEmVendasDebitoCredito = 0D;
				totalEmDevolucaoTrocas = 0D;
				totalDespesas = 0D;
				totalReceitas = 0D;
				
				for (ItemCaixa itemCaixa : itensCaixaFiltrados) {
					
					if(itemCaixa.getOperacao() == TipoOperacao.VENDA) {
						
						totalEmVendas += itemCaixa.getValor().doubleValue();
						
						if(itemCaixa.getFormaPagamento().getNome().equals("Dinheiro")) {
							totalEmVendasEmDinheiro += itemCaixa.getValor().doubleValue();
							
						} else {
							totalEmVendasDebitoCredito += itemCaixa.getValor().doubleValue();
						}
						
					} else if(itemCaixa.getOperacao() == TipoOperacao.DEVOLUCAO) {
						
						totalEmDevolucaoTrocas += itemCaixa.getValor().doubleValue();
						
					} else if(itemCaixa.getOperacao() == TipoOperacao.LANCAMENTO) {
						
						if(itemCaixa.getTipoPagamento().equals("Entrada")) {
							totalReceitas += itemCaixa.getValor().doubleValue();
						} else {
							totalDespesas += itemCaixa.getValor().doubleValue();
						}
					} else {
						
						saldoInicial += itemCaixa.getValor().doubleValue();
					}
				}
				
				totalEmDinheiro = saldoInicial.doubleValue() + (totalEmVendasEmDinheiro.doubleValue() + totalReceitas.doubleValue())  - (totalEmDevolucaoTrocas.doubleValue() + totalDespesas.doubleValue());
			
			} else {
				
				caixa = new Caixa();
			}
			
			todasFormasPagamentos = formasPagamentos.todos();
			
		}
	}
	
	
	

	
	public void filtrarRegistrosCaixa() {
		
		itensCaixaFiltrados = caixas.porFormaPagamento(usuario, formaPagamento, usuario.getEmpresa());
		
		List<ItemCaixa> itensCaixa = new ArrayList<ItemCaixa>();
		
		if(formaPagamento != null && formaPagamento.getNome().equals("Dinheiro")) {
			for (ItemCaixa itemCaixa : itensCaixaFiltrados) {
	
				if(itemCaixa.getOperacao() != TipoOperacao.DEVOLUCAO && itemCaixa.getOperacao() != TipoOperacao.LANCAMENTO) {
					
					itensCaixa.add(itemCaixa);
				}
			}
			
			itensCaixaFiltrados = new ArrayList<ItemCaixa>();
			itensCaixaFiltrados.addAll(itensCaixa);
		}	
		
		itemCaixaSelecionado = null;
	}
	
	
	public void fecharCaixa() {
		
		caixa.setSaldoFinal(new BigDecimal(totalEmDinheiro));
		caixa.setDataFechamento(new Date());
		caixa.setStatus(true);
		
		caixas.save(caixa);
		
		caixa = new Caixa();
		itensCaixaFiltrados = new ArrayList<ItemCaixa>();
		
		totalEmDinheiro = 0D;
		totalEmVendas = 0D;
		totalEmVendasEmDinheiro = 0D;
		totalEmVendasDebitoCredito = 0D;
		totalEmDevolucaoTrocas = 0D;
		totalDespesas = 0D;
		totalReceitas = 0D;
		
		totalEmDinheiro = 0D;
		
		caixaAberto = false;
		
		
		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Pronto!', text: 'O caixa foi fechado com sucesso!' });");
	}
	
	public void abrirCaixa() {
		System.out.println(caixa.getSaldoInicial());
		
		caixa.setDataAbertura(new Date());
				
		if(caixa.getSaldoInicial() == null) {
			caixa.setSaldoInicial(BigDecimal.ZERO);
		}
		
		caixa.setUsuario(usuario);
		caixa.setEmpresa(usuario.getEmpresa());
		
		caixa = caixas.save(caixa);
		
		caixaAberto = true;
		
		totalEmDinheiro = caixa.getSaldoInicial().doubleValue();
		
		ItemCaixa itemCaixa = new ItemCaixa();
		itemCaixa.setCaixa(caixa);
		itemCaixa.setData(caixa.getDataAbertura());
		itemCaixa.setDescricao("SALDO INICIAL");
		itemCaixa.setValor(caixa.getSaldoInicial());
		itemCaixa.setFormaPagamento(formasPagamentos.porNome("Dinheiro"));
		itemCaixa.setTipoPagamento("Entrada");
		
		itemCaixa = itensCaixas.save(itemCaixa);
		
			
		itensCaixaFiltrados = itensCaixas.porCaixa(caixa);
		
		
		PrimeFaces.current().executeScript(
				"swal({ type: 'success', title: 'Pronto!', text: 'O caixa foi aberto com sucesso!' });");
	}
	

	public List<FormaPagamento> getTodasFormasPagamentos() {
		return todasFormasPagamentos;
	}

	public List<ItemCaixa> getItensCaixaFiltrados() {
		return itensCaixaFiltrados;
	}

	public ItemCaixa getItemCaixaSelecionado() {
		return itemCaixaSelecionado;
	}

	public void setItemCaixaSelecionado(ItemCaixa itemCaixaSelecionado) {
		this.itemCaixaSelecionado = itemCaixaSelecionado;
	}

	public boolean isCaixaAberto() {
		return caixaAberto;
	}

	public Caixa getCaixa() {
		return caixa;
	}

	public void setCaixa(Caixa caixa) {
		this.caixa = caixa;
	}

	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}

	public void setFormaPagamento(FormaPagamento formaPagamento) {
		this.formaPagamento = formaPagamento;
	}





	public Double getTotalEmVendas() {
		return totalEmVendas;
	}





	public Double getTotalEmDinheiro() {
		return totalEmDinheiro;
	}





	public Double getTotalEmVendasEmDinheiro() {
		return totalEmVendasEmDinheiro;
	}





	public Double getTotalEmVendasDebitoCredito() {
		return totalEmVendasDebitoCredito;
	}





	public Double getTotalEmDevolucaoTrocas() {
		return totalEmDevolucaoTrocas;
	}





	public Double getTotalDespesas() {
		return totalDespesas;
	}





	public Double getTotalReceitas() {
		return totalReceitas;
	}

}
