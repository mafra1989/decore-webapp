package com.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.model.UploadedFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.Grupo;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Lancamento;
import com.webapp.model.Produto;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Bairros;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.ItensVendas;
import com.webapp.repository.Lancamentos;
import com.webapp.repository.Produtos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;
import com.webapp.repository.Vendas;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ImportarDadosBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Compras comprasRepository;

	@Inject
	private Vendas vendasRepository;

	@Inject
	private ItensCompras itensComprasRepository;

	@Inject
	private ItensVendas itensVendasRepository;

	@Inject
	private Produtos produtosRepository;

	@Inject
	private Usuarios usuariosRepository;
	
	@Inject
	private Lancamentos lancamentosRepository;
	
	@Inject
	private Contas contasRepository;
	
	@Inject
	private CategoriasLancamentos categoriasLancamentosRepository;
	
	@Inject
	private DestinosLancamentos destinosLancamentosRepository;

	@Inject
	private Bairros bairros;

	@Inject
	private TiposVendas tiposVendas;

	private UploadedFile file;

	private byte[] fileContent;

	private String opcao = "";

	private boolean upload = false;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;
	
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
		}
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void importarCompras(UploadedFile file) {

		Compra compra = null;
		List<ItemCompra> itens = null;
		Iterator<Row> rowIterator = null;

		List<Compra> compras = new ArrayList<>();

		Workbook workbook;
		try {
			workbook = create(file.getInputstream());

			Sheet sheet = workbook.getSheetAt(0);
			rowIterator = sheet.iterator();

			String comprasNum = "";
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() > 0) {

					if (!row.getCell(0).toString().equals(comprasNum)) {

						comprasNum = row.getCell(0).toString();

						compra = new Compra();
						compra.setNumeroCompra(Long.parseLong(comprasNum.replace(".0", "")));
						
						itens = new ArrayList<>();
						Calendar calendar = Calendar.getInstance();

						try {
							calendar.setTime(new SimpleDateFormat("dd MMM yyyy")
									.parse(row.getCell(1).toString().replace("-", " ")));
							compra.setDataCompra(calendar.getTime());
						} catch (ParseException e) {

						}

						System.out.println(compra.getDataCompra());
						compra.setDia(Long.valueOf((calendar.get(Calendar.DAY_OF_MONTH))));
						compra.setNomeDia(Long.valueOf((calendar.get(Calendar.DAY_OF_WEEK))));
						compra.setSemana(Long.valueOf((calendar.get(Calendar.WEEK_OF_YEAR))));
						compra.setMes(Long.valueOf((calendar.get(Calendar.MONTH))) + 1);
						compra.setAno(Long.valueOf((calendar.get(Calendar.YEAR))));

						compra.setItensCompra(itens);
						compras.add(compra);
					}

					if (row.getCell(0).toString().equals(comprasNum)) {

						compra.setQuantidadeItens(
								(long) (compra.getQuantidadeItens() + Double.parseDouble(row.getCell(5).toString())));
						compra.setValorTotal(BigDecimal.valueOf(
								compra.getValorTotal().doubleValue() + (Double.parseDouble(row.getCell(4).toString())
										* Double.parseDouble(row.getCell(5).toString()))));

						ItemCompra itemCompra = new ItemCompra();
						itemCompra.setQuantidade((long) Double.parseDouble(row.getCell(5).toString()));
						itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade());
						itemCompra.setValorUnitario(BigDecimal.valueOf(Double.parseDouble(row.getCell(4).toString())));
						itemCompra.setTotal(BigDecimal.valueOf(Double.parseDouble(row.getCell(4).toString())
								* Double.parseDouble(row.getCell(5).toString())));

						System.out.println(row.getCell(2).toString());
						String codigo = ((long) Double.parseDouble(row.getCell(2).toString())) + "";
						Produto produto = produtosRepository.porCodigo(codigo, usuario.getEmpresa());

						if (produto != null) {
							itemCompra.setProduto(produto);
						} else {
							produto = new Produto(); // Producao
							produto.setCodeTemp(codigo);  // Producao
							//produto = produtosRepository.porCodigo("0"); // Teste
							itemCompra.setProduto(produto);
						}

						itens.add(itemCompra);
						System.out.println(itemCompra.getValorUnitario() + " - " + itemCompra.getQuantidade());
					}
				}
			}

			List<String> codigos = new ArrayList<>();

			boolean produtoNaoEncontrado = false;
			for (Compra compraTemp : compras) {
				for (ItemCompra itemCompraTemp : compraTemp.getItensCompra()) {
					if (itemCompraTemp.getProduto().getId() == null) {
						produtoNaoEncontrado = true;
						codigos.add(itemCompraTemp.getProduto().getCodeTemp());
					}
				}
			}

			if (produtoNaoEncontrado != true) {
				
				for (Compra compraTemp : compras) {
					
					Compra compraTemp_ = comprasRepository.ultimoNCompra(usuario.getEmpresa());

					if (compraTemp_ == null) {
						compraTemp.setNumeroCompra(1L);
					} else {						
						compraTemp.setNumeroCompra(compraTemp_.getNumeroCompra() + 1);
					}
					
					System.out.println("Compra N.:" + compraTemp.getNumeroCompra() + " Quant. Itens: " + compraTemp.getQuantidadeItens() + " - Valor Total: "
							+ compraTemp.getValorTotal() + " _ " + compraTemp.getItensCompra().size());

					List<ItemCompra> itensTemp = new ArrayList<>();
					itensTemp.addAll(compraTemp.getItensCompra());

					Usuario usuario = usuariosRepository.porId(1L);
					compraTemp.setUsuario(usuario);
					compraTemp = comprasRepository.save(compraTemp);

					for (ItemCompra itemCompraTemp : itensTemp) {
						itemCompraTemp.setCompra(compraTemp);

						Produto produto = itemCompraTemp.getProduto();
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() + itemCompraTemp.getQuantidade());
						produtosRepository.save(produto);

						itensComprasRepository.save(itemCompraTemp);
					}	
					
					/*
					Conta conta = new Conta();
					conta.setOperacao("COMPRA");
					conta.setCodigoOperacao(compraTemp.getNumeroCompra());
					conta.setVencimento(compraTemp.getDataCompra());
					conta.setPagamento(compraTemp.getDataCompra());
					conta.setValor(compraTemp.getValorTotal());
					conta.setParcela("AVISTA");
					conta.setTipo(OrigemLancamento.DEBITO.name());
					conta.setStatus(true);
					 
					Calendar calendario = Calendar.getInstance();
					calendario.setTime(compraTemp.getDataCompra());
					
					conta.setDia(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
					conta.setNomeDia(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
					conta.setSemana(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
					conta.setMes(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
					conta.setAno(Long.valueOf((calendario.get(Calendar.YEAR))));
					
					contasRepository.save(conta);
					*/
				}

				System.out.println("Total de Compras: " + compras.size());

				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Dados importados com sucesso!' });");
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Produtos não encontrados! Verifique os códigos: "
								+ Arrays.asList(codigos) + " ' });");
			}

		} catch (IOException | IllegalArgumentException e) {
			PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
		}

		fileContent = null;
	}

	public void importarVendas(UploadedFile file) {

		Venda venda = null;
		List<ItemVenda> itens = null;
		Iterator<Row> rowIterator = null;

		List<Venda> vendas = new ArrayList<>();

		Workbook workbook;
		try {
			workbook = create(file.getInputstream());

			Sheet sheet = workbook.getSheetAt(0);
			rowIterator = sheet.iterator();

			String vendasNum = "";
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() > 0) {

					if (!row.getCell(0).toString().equals(vendasNum)) {

						vendasNum = row.getCell(0).toString();

						venda = new Venda();
						venda.setNumeroVenda(Long.parseLong(vendasNum.replace(".0", ""))); 
						
						itens = new ArrayList<>();
						Calendar calendar = Calendar.getInstance();

						/* Venda */
						/* Data da Venda */
						try {
							calendar.setTime(new SimpleDateFormat("dd MMM yyyy")
									.parse(row.getCell(1).toString().replace("-", " ")));
							venda.setDataVenda(calendar.getTime());
						} catch (ParseException e) {

						}

						/* Venda */
						/* Dia, Nome do dia, Semana, Mês e Ano */
						System.out.println(venda.getDataVenda());
						venda.setDia(Long.valueOf((calendar.get(Calendar.DAY_OF_MONTH))));
						venda.setNomeDia(Long.valueOf((calendar.get(Calendar.DAY_OF_WEEK))));
						venda.setSemana(Long.valueOf((calendar.get(Calendar.WEEK_OF_YEAR))));
						venda.setMes(Long.valueOf((calendar.get(Calendar.MONTH))) + 1);
						venda.setAno(Long.valueOf((calendar.get(Calendar.YEAR))));

						/* Venda */
						/* Tipo de Venda e Bairro */
						TipoVenda tipoVenda = tiposVendas.porDescricao(row.getCell(2).toString());
						if(tipoVenda != null) {
							venda.setTipoVenda(tipoVenda);// Producao
						} else {
							venda.setTipoVenda(tiposVendas.porId(3009L));// Producao
						}
						
						venda.setStatus(true);
						venda.setBairro(bairros.porId(3008L));// Producao
						//venda.setTipoVenda(tiposVendas.porId(859L)); // Teste
						//venda.setBairro(bairros.porId(860L)); // Teste

						/* Venda */
						/* Usuario */
						Usuario usuario = usuariosRepository.porId(1L);
						venda.setUsuario(usuario);

						venda.setItensVenda(itens);
						vendas.add(venda);
					}

					if (row.getCell(0).toString().equals(vendasNum)) {

						System.out.println((long) Double.parseDouble(row.getCell(4).toString()) + " - "
								+ Double.parseDouble(row.getCell(7).toString()));
						String codigo = ((long) Double.parseDouble(row.getCell(4).toString())) + "";
						Produto produto = produtosRepository.porCodigo(codigo, usuario.getEmpresa());// Producao
						//Produto produto = produtosRepository.porCodigo("0"); // Teste
						
						long saldo = (long) Double.parseDouble(row.getCell(7).toString());

						do {

							ItemVenda itemVenda = new ItemVenda();

							ItemCompra itemCompraTemp = itensComprasRepository.porProdutoDisponivel(produto);

							System.out.println(produto + " - " + itemCompraTemp);

							if (itemCompraTemp != null) {

								System.out.println(itemCompraTemp.getQuantidadeDisponivel() + " >= " + saldo);
								if (itemCompraTemp.getQuantidadeDisponivel().longValue() >= saldo) {

									/* Item */
									/* Quantidade, ValorUnitario e Total */
									itemVenda.setQuantidade(saldo);
									itemVenda.setValorUnitario(
											BigDecimal.valueOf(Double.parseDouble(row.getCell(6).toString())));
									itemVenda.setTotal(
											BigDecimal.valueOf(itemVenda.getValorUnitario().doubleValue() * saldo));

									/* Item */
									/* Lucro, PercentualLucro, ValorCompra e Compra */
									itemVenda.setLucro(
											BigDecimal.valueOf((saldo * itemVenda.getValorUnitario().doubleValue())
													- (saldo * itemCompraTemp.getValorUnitario().doubleValue())));
									itemVenda.setPercentualLucro(BigDecimal.valueOf((itemVenda.getLucro().doubleValue()
											/ (saldo * itemCompraTemp.getValorUnitario().doubleValue())) * 100));
									itemVenda.setValorCompra(BigDecimal
											.valueOf(saldo * itemCompraTemp.getValorUnitario().doubleValue()));

									itemVenda.setCompra(itemCompraTemp.getCompra());

									/* Venda */
									/* Total de Itens e Valor Total */
									venda.setQuantidadeItens(venda.getQuantidadeItens() + itemVenda.getQuantidade());
									venda.setValorTotal(BigDecimal.valueOf(
											venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));

									/* Lucro, PercentualLucro e ValorCompra */
									venda.setLucro(BigDecimal.valueOf(
											venda.getLucro().doubleValue() + itemVenda.getLucro().doubleValue()));
									venda.setPercentualLucro(BigDecimal.valueOf(venda.getPercentualLucro().doubleValue()
											+ itemVenda.getLucro().doubleValue()));
									venda.setValorCompra(BigDecimal.valueOf(venda.getValorCompra().doubleValue()
											+ itemVenda.getValorCompra().doubleValue()));

									/* Item */
									/* Produto */
									if (produto != null) {
										itemVenda.setProduto(produto);
									} else {
										produto = new Produto(); //Producao
										produto.setCodeTemp(codigo); //Producao
										//produto = produtosRepository.porCodigo("0"); // Teste
										itemVenda.setProduto(produto);
									}

									itens.add(itemVenda);
									System.out
											.println(itemVenda.getValorUnitario() + " - " + itemVenda.getQuantidade());									
									
									long quantidadeDisponivel = saldo;
									saldo -= itemCompraTemp.getQuantidadeDisponivel().longValue();
									quantidadeDisponivel = itemCompraTemp.getQuantidadeDisponivel().longValue() - quantidadeDisponivel;
									
									itemCompraTemp.setQuantidadeDisponivel(quantidadeDisponivel);								
									itensComprasRepository.save(itemCompraTemp);

								} else {

									/* Item */
									/* Quantidade, ValorUnitario e Total */
									itemVenda.setQuantidade(itemCompraTemp.getQuantidadeDisponivel());
									itemVenda.setValorUnitario(
											BigDecimal.valueOf(Double.parseDouble(row.getCell(6).toString())));
									itemVenda.setTotal(BigDecimal.valueOf(itemVenda.getValorUnitario().doubleValue()
											* itemCompraTemp.getQuantidadeDisponivel()));

									/* Item */
									/* Lucro, PercentualLucro, ValorCompra e Compra */
									itemVenda.setLucro(BigDecimal.valueOf((itemCompraTemp.getQuantidadeDisponivel()
											* itemVenda.getValorUnitario().doubleValue())
											- (itemCompraTemp.getQuantidadeDisponivel()
													* itemCompraTemp.getValorUnitario().doubleValue())));
									itemVenda.setPercentualLucro(BigDecimal.valueOf((itemVenda.getLucro().doubleValue()
											/ (itemCompraTemp.getQuantidadeDisponivel()
													* itemCompraTemp.getValorUnitario().doubleValue()))
											* 100));
									itemVenda.setValorCompra(BigDecimal.valueOf(itemCompraTemp.getQuantidadeDisponivel()
											* itemCompraTemp.getValorUnitario().doubleValue()));

									itemVenda.setCompra(itemCompraTemp.getCompra());

									/* Venda */
									/* Total de Itens e Valor Total */
									venda.setQuantidadeItens(venda.getQuantidadeItens() + itemVenda.getQuantidade());
									venda.setValorTotal(BigDecimal.valueOf(
											venda.getValorTotal().doubleValue() + itemVenda.getTotal().doubleValue()));

									/* Lucro, PercentualLucro e ValorCompra */
									venda.setLucro(BigDecimal.valueOf(
											venda.getLucro().doubleValue() + itemVenda.getLucro().doubleValue()));
									venda.setPercentualLucro(BigDecimal.valueOf(venda.getPercentualLucro().doubleValue()
											+ itemVenda.getLucro().doubleValue()));
									venda.setValorCompra(BigDecimal.valueOf(venda.getValorCompra().doubleValue()
											+ itemVenda.getValorCompra().doubleValue()));

									/* Item */
									/* Produto */
									if (produto != null) {
										itemVenda.setProduto(produto);
									} else {
										produto = new Produto();
										produto.setCodeTemp(codigo);
										// produto = produtosRepository.porCodigo("0"); // Teste
										itemVenda.setProduto(produto);
									}

									itens.add(itemVenda);
									System.out
											.println(itemVenda.getValorUnitario() + " - " + itemVenda.getQuantidade());

									saldo -= itemCompraTemp.getQuantidadeDisponivel().longValue();
									
									itemCompraTemp.setQuantidadeDisponivel(0L);
									itensComprasRepository.save(itemCompraTemp);

								}

							} else {
								
								break;
							}

						} while (saldo > 0);

					}
				}
			}

			List<String> codigos = new ArrayList<>();

			boolean produtoNaoEncontrado = false;
			for (Venda vendaTemp : vendas) {
				for (ItemVenda itemVendaTemp : vendaTemp.getItensVenda()) {
					if (itemVendaTemp.getProduto().getId() == null) {
						produtoNaoEncontrado = true;
						codigos.add(itemVendaTemp.getProduto().getCodeTemp());
					}
				}
			}

			if (produtoNaoEncontrado != true) {

				Long quantidade = 0L;
				Double valorTotal = 0D;

				for (Venda vendaTemp : vendas) {
					
					Venda vendaTemp_ = vendasRepository.ultimoNVenda(usuario.getEmpresa());

					if (vendaTemp_ == null) {
						vendaTemp.setNumeroVenda(1L);
					} else {						
						vendaTemp.setNumeroVenda(vendaTemp_.getNumeroVenda() + 1);
					}
					
					System.out.println("Venda N.:" + vendaTemp.getNumeroVenda() + " Quant. Itens: " + vendaTemp.getQuantidadeItens() + " - Valor Total: "
							+ vendaTemp.getValorTotal() + " _ " + vendaTemp.getItensVenda().size());

					List<ItemVenda> itensTemp = new ArrayList<>();
					itensTemp.addAll(vendaTemp.getItensVenda());

					vendaTemp = vendasRepository.save(vendaTemp);

					for (ItemVenda itemVendaTemp : itensTemp) {
						/* Item */
						/* Venda */
						itemVendaTemp.setVenda(vendaTemp);

						quantidade += itemVendaTemp.getQuantidade();
						valorTotal += itemVendaTemp.getTotal().doubleValue();

						Produto produto = itemVendaTemp.getProduto();
						produto = produtosRepository.porId(produto.getId());
						produto.setQuantidadeAtual(produto.getQuantidadeAtual() - itemVendaTemp.getQuantidade());

						System.out.println(produto.getCodigo() + " - " + produto.getQuantidadeAtual());

						produtosRepository.save(produto);

						itensVendasRepository.save(itemVendaTemp);
					}	

				}

				System.out.println("Total de Vendas: " + vendas.size());
				System.out.println("Quantidade: " + quantidade);
				System.out.println("Valor Total: " + valorTotal);

				PrimeFaces.current().executeScript(
						"swal({ type: 'success', title: 'Concluído!', text: 'Dados importados com sucesso!' });");
			} else {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Produtos não encontrados! Verifique os códigos: "
								+ Arrays.asList(codigos) + " ' });");
			}

		} catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
			PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
		}

		fileContent = null;
	}
	
	
	public void importarLancamentos(UploadedFile file) {

		Lancamento lancamento = null;
		Iterator<Row> rowIterator = null;

		List<Lancamento> lancamentos = new ArrayList<>();

		Workbook workbook;
		try {
			workbook = create(file.getInputstream());

			Sheet sheet = workbook.getSheetAt(0);
			rowIterator = sheet.iterator();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() > 0) {

					lancamento = new Lancamento();
					
					Calendar calendar = Calendar.getInstance();

					try {
						calendar.setTime(new SimpleDateFormat("dd MMM yyyy")
								.parse(row.getCell(0).toString().replace("-", " ")));
						lancamento.setDataLancamento(calendar.getTime());
					} catch (ParseException e) {

					}

					System.out.println(lancamento.getDataLancamento());
					lancamento.setDia(Long.valueOf((calendar.get(Calendar.DAY_OF_MONTH))));
					lancamento.setNomeDia(Long.valueOf((calendar.get(Calendar.DAY_OF_WEEK))));
					lancamento.setSemana(Long.valueOf((calendar.get(Calendar.WEEK_OF_YEAR))));
					lancamento.setMes(Long.valueOf((calendar.get(Calendar.MONTH))) + 1);
					lancamento.setAno(Long.valueOf((calendar.get(Calendar.YEAR))));

					/* Usuario */
					Usuario usuario = usuariosRepository.porId(1L);
					lancamento.setUsuario(usuario);
					
					lancamento.setCategoriaLancamento(categoriasLancamentosRepository.porNome(row.getCell(5).toString(), usuario.getEmpresa()));
					lancamento.setDestinoLancamento(destinosLancamentosRepository.porDescricao(row.getCell(2).toString()));
					lancamento.setDescricao(row.getCell(3).toString());
					lancamento.setValor(BigDecimal.valueOf(Double.parseDouble(row.getCell(4).toString())));
					
					lancamentos.add(lancamento);	
				}
			}

				
			for (Lancamento lancamentoTemp : lancamentos) {
				
				Lancamento lancamentoTemp_ = lancamentosRepository.ultimoNLancamento(usuario.getEmpresa());

				if (lancamentoTemp_ == null) {
					lancamentoTemp.setNumeroLancamento(1L);
				} else {						
					lancamentoTemp.setNumeroLancamento(lancamentoTemp_.getNumeroLancamento() + 1);
				}
				
				System.out.println("Lancamento N.:" + lancamentoTemp.getNumeroLancamento());

				lancamentoTemp = lancamentosRepository.save(lancamentoTemp);
				
				Conta conta = new Conta();
				conta.setOperacao("LANCAMENTO");
				conta.setCodigoOperacao(lancamentoTemp.getNumeroLancamento());
				conta.setVencimento(lancamentoTemp.getDataLancamento());
				conta.setPagamento(lancamentoTemp.getDataLancamento());
				conta.setValor(lancamentoTemp.getValor());
				conta.setParcela("AVISTA");
				conta.setTipo(lancamentoTemp.getCategoriaLancamento().getTipoLancamento().getOrigem().name());
				conta.setStatus(true);
				 
				Calendar calendario = Calendar.getInstance();
				calendario.setTime(lancamentoTemp.getDataLancamento());
				
				conta.setDia(Long.valueOf((calendario.get(Calendar.DAY_OF_MONTH))));
				conta.setNomeDia(Long.valueOf((calendario.get(Calendar.DAY_OF_WEEK))));
				conta.setSemana(Long.valueOf((calendario.get(Calendar.WEEK_OF_YEAR))));
				conta.setMes(Long.valueOf((calendario.get(Calendar.MONTH))) + 1);
				conta.setAno(Long.valueOf((calendario.get(Calendar.YEAR))));
				
				contasRepository.save(conta);
			}

			System.out.println("Total de Lancamentos: " + lancamentos.size());

			PrimeFaces.current().executeScript(
					"swal({ type: 'success', title: 'Concluído!', text: 'Dados importados com sucesso!' });");

		} catch (IOException | IllegalArgumentException e) {
			PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
		}

		fileContent = null;
	}
	

	public void importar() { 
		
		System.out.println("Iniciando importação . . . upload: " + upload);

		if (file != null && file.getFileName() != null) {
			fileContent = file.getContents();
			
			if(upload == false) {
				upload = true;
				
				System.out.println(file.getFileName());
				System.out.println("Opção: " + opcao);
				System.out.println("Upload: " + upload);

				if (!opcao.equalsIgnoreCase("")) {

					if (opcao.equalsIgnoreCase("compras")) {
						importarCompras(file);
						upload = false;
					}

					if (opcao.equalsIgnoreCase("vendas")) {
						importarVendas(file);
						upload = false;
					}

					if (opcao.equalsIgnoreCase("lançamentos")) {
						importarLancamentos(file);
						upload = false;
					}

				} else {
					PrimeFaces.current().executeScript(
							"swal({ type: 'warning', title: 'Atenção!', text: 'Selecione uma opção antes de importar o arquivo!' });");
				}

			}			

		} else {

			PrimeFaces.current()
					.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
		}

		System.out.println(fileContent);

	}

	public static Workbook create(InputStream in) {
		try {
			if (!in.markSupported()) {
				in = new PushbackInputStream(in, 8);
			}
			if (POIFSFileSystem.hasPOIFSHeader(in)) {
				return new HSSFWorkbook(in);
			}
			if (POIXMLDocument.hasOOXMLHeader(in)) {
				return new XSSFWorkbook(OPCPackage.open(in));
			}
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("你的excel版本目前poi解析不了");
	}

	public String getOpcao() {
		return opcao;
	}

	public void setOpcao(String opcao) {
		this.opcao = opcao;
	}

}
