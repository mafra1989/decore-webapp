package com.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import org.primefaces.model.file.UploadedFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.webapp.model.Bairro;
import com.webapp.model.CategoriaProduto;
import com.webapp.model.Cliente;
import com.webapp.model.Compra;
import com.webapp.model.Conta;
import com.webapp.model.ItemCompra;
import com.webapp.model.ItemVenda;
import com.webapp.model.Lancamento;
import com.webapp.model.Produto;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.model.Venda;
import com.webapp.repository.Bairros;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.CategoriasProdutos;
import com.webapp.repository.Clientes;
import com.webapp.repository.Compras;
import com.webapp.repository.Contas_;
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
	private Contas_ contasRepository;
	
	@Inject
	private CategoriasLancamentos categoriasLancamentosRepository;
	
	
	@Inject
	private CategoriasProdutos categoriasProdutosRepository;
	
	@Inject
	private DestinosLancamentos destinosLancamentosRepository;

	@Inject
	private Bairros bairros;

	@Inject
	private TiposVendas tiposVendas;

	private UploadedFile file;

	private byte[] fileContent;

	private String opcao = "Produtos";

	private boolean upload = false;

	@Inject
	private Usuario usuario;
	
	@Inject
	private Usuarios usuarios;
	
	private List<Produto> produtos = new ArrayList<>();
	
	
	@Inject
	private Cliente cliente;
	
	@Inject
	private Clientes clientesRepository;
	
	private List<Cliente> clientes = new ArrayList<>();
	
	@Inject
	private Bairros bairrosRepository;
	
	
	
	public void inicializar() {
		if (FacesUtil.isNotPostback()) {

			User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();	
			usuario = usuarios.porLogin(user.getUsername());
		}
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void importarCompras(UploadedFile file) {

		Compra compra = null;
		List<ItemCompra> itens = null;
		Iterator<Row> rowIterator = null;

		List<Compra> compras = new ArrayList<>();

		Workbook workbook;
		try {
			workbook = create(file.getInputStream());

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
						itemCompra.setQuantidade(new BigDecimal(Double.parseDouble(row.getCell(5).toString())));
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
						produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().doubleValue() + itemCompraTemp.getQuantidade().doubleValue()));
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
			workbook = create(file.getInputStream());

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
						TipoVenda tipoVenda = tiposVendas.porDescricao(row.getCell(2).toString(), usuario.getEmpresa());
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
									itemVenda.setQuantidade(new BigDecimal(saldo));
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
									venda.setQuantidadeItens(venda.getQuantidadeItens().longValue() + itemVenda.getQuantidade().longValue());
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
									
									itemCompraTemp.setQuantidadeDisponivel(new BigDecimal(quantidadeDisponivel));								
									itensComprasRepository.save(itemCompraTemp);

								} else {

									/* Item */
									/* Quantidade, ValorUnitario e Total */
									itemVenda.setQuantidade(itemCompraTemp.getQuantidadeDisponivel());
									itemVenda.setValorUnitario(
											BigDecimal.valueOf(Double.parseDouble(row.getCell(6).toString())));
									itemVenda.setTotal(BigDecimal.valueOf(itemVenda.getValorUnitario().doubleValue()
											* itemCompraTemp.getQuantidadeDisponivel().longValue()));

									/* Item */
									/* Lucro, PercentualLucro, ValorCompra e Compra */
									itemVenda.setLucro(BigDecimal.valueOf((itemCompraTemp.getQuantidadeDisponivel().doubleValue()
											* itemVenda.getValorUnitario().doubleValue())
											- (itemCompraTemp.getQuantidadeDisponivel().doubleValue()
													* itemCompraTemp.getValorUnitario().doubleValue())));
									itemVenda.setPercentualLucro(BigDecimal.valueOf((itemVenda.getLucro().doubleValue()
											/ (itemCompraTemp.getQuantidadeDisponivel().doubleValue()
													* itemCompraTemp.getValorUnitario().doubleValue()))
											* 100));
									itemVenda.setValorCompra(BigDecimal.valueOf(itemCompraTemp.getQuantidadeDisponivel().doubleValue()
											* itemCompraTemp.getValorUnitario().doubleValue()));

									itemVenda.setCompra(itemCompraTemp.getCompra());

									/* Venda */
									/* Total de Itens e Valor Total */
									venda.setQuantidadeItens(venda.getQuantidadeItens().longValue() + itemVenda.getQuantidade().longValue());
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
									
									itemCompraTemp.setQuantidadeDisponivel(BigDecimal.ZERO);
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

						quantidade += itemVendaTemp.getQuantidade().longValue();
						valorTotal += itemVendaTemp.getTotal().doubleValue();

						Produto produto = itemVendaTemp.getProduto();
						produto = produtosRepository.porId(produto.getId());
						produto.setQuantidadeAtual(new BigDecimal(produto.getQuantidadeAtual().longValue() - itemVendaTemp.getQuantidade().longValue()));

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
			workbook = create(file.getInputStream());

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
					
					lancamento.setCategoriaLancamento(categoriasLancamentosRepository.porNome(row.getCell(5).toString(), null));
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
	
	
	public void importarProdutos(UploadedFile file) {

		Produto produto = null;
		Iterator<Row> rowIterator = null;

		produtos = new ArrayList<>();
		
		usuario = usuarios.porId(1L);

		Workbook workbook;
		try {
			workbook = create(file.getInputStream());

			Sheet sheet = workbook.getSheetAt(0);
			rowIterator = sheet.iterator();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() > 0) {

					if(row.getCell(3) != null) {
						
						produto = new Produto();
						
						CategoriaProduto categoriaProduto = categoriasProdutosRepository.porNome(row.getCell(3).toString(), usuario.getEmpresa());
						
						if(categoriaProduto == null) {
							categoriaProduto = new CategoriaProduto();
							categoriaProduto.setNome(convertToTitleCaseIteratingChars(row.getCell(3).toString()));
							categoriaProduto.setEmpresa(usuario.getEmpresa());
						}
						
						produto.setCodigo(row.getCell(0) != null ? row.getCell(0).toString().trim().replace(".0", "") : "");
						produto.setNome(row.getCell(1) != null ? convertToTitleCaseIteratingChars(row.getCell(1).toString()) : "");
						produto.setDescricao(row.getCell(2) != null ? convertToTitleCaseIteratingChars(row.getCell(2).toString()) : "");
						produto.setCategoriaProduto(categoriaProduto);
						produto.setNumeracao(row.getCell(4) != null ? row.getCell(4).toString() : "");
						produto.setCustoMedioUnitario((row.getCell(5) != null && !row.getCell(5).toString().trim().equals("")) ? new BigDecimal(row.getCell(5).toString()) : null);						
						produto.setPrecoDeVenda((row.getCell(6) != null && !row.getCell(6).toString().trim().equals("")) ? new BigDecimal(row.getCell(6).toString()) : null);
						
						
						if(row.getCell(0) == null || row.getCell(0).toString().trim().equals("")) {
							produto.setValid(false);
						} else {
							Produto produtoTemp = produtosRepository.porCodigo(row.getCell(0).toString().trim().replace(".0", ""), usuario.getEmpresa());
							if(produtoTemp != null) {
								produto.setValid(false);
							}
						}
						
						if(row.getCell(1) == null || row.getCell(1).toString().trim().equals("")) {
							produto.setValid(false);
						}
						
						if(row.getCell(2) == null || row.getCell(2).toString().trim().equals("")) {
							produto.setValid(false);
						}
						
						if(row.getCell(3).toString().trim().equals("")) {
							produto.setValid(false);
						}	
						
						if(row.getCell(5) == null || row.getCell(5).toString().trim().equals("")) {
							produto.setValid(false);
						}
						
						if(row.getCell(6) == null || row.getCell(6).toString().trim().equals("")) {
							produto.setValid(false);
						}
						
						produtos.add(produto);					
					} 
				}
			}


			System.out.println("Total de Produtos: " + produtos.size());


		} catch (IOException | IllegalArgumentException e) {
			if(produtos.size() == 0) {
				PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
			}
		}

		//fileContent = null;
	}
	
	
	
	public void importarClientes(UploadedFile file) {

		Cliente cliente = null;
		Iterator<Row> rowIterator = null;

		clientes = new ArrayList<>();
		
		usuario = usuarios.porId(1L);
		
		NumberFormat nf = new DecimalFormat("#");

		Workbook workbook;
		try {
			workbook = create(file.getInputStream());

			Sheet sheet = workbook.getSheetAt(0);
			rowIterator = sheet.iterator();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (row.getRowNum() > 0) {

					if(row.getCell(3) != null) {
						
						cliente = new Cliente();
						
						Bairro bairro = bairrosRepository.porNome(row.getCell(3).toString(), usuario.getEmpresa());
						
						if(bairro == null) {
							bairro = new Bairro();
							bairro.setNome(convertToTitleCaseIteratingChars(row.getCell(3).toString()));
							bairro.setTaxaDeEntrega(BigDecimal.ZERO);
						}
						
						cliente.setNome(row.getCell(2) != null ? convertToTitleCaseIteratingChars(row.getCell(2).toString()) : "");
						cliente.setBairro(bairro);
						
						if(row.getCell(1) == null || row.getCell(1).toString().trim().equals("")) {
							cliente.setValid(false);
						} else {
							
							if (row.getCell(1).getCellType() == 0) {
								cliente.setContato(nf.format(row.getCell(1).getNumericCellValue()).trim());
							} else {
								cliente.setContato(row.getCell(1).toString());
							}
						}
						
						if(row.getCell(2) == null || row.getCell(2).toString().trim().equals("")) {
							cliente.setValid(false);
						}
						
						clientes.add(cliente);					
					} 
				}
			}


			System.out.println("Total de Clientes: " + clientes.size());


		} catch (IOException | IllegalArgumentException e) {
			if(clientes.size() == 0) {
				PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
			}
		}

		//fileContent = null;
	}
	
	

	public void processarArquivo() { 
		
		System.out.println("Iniciando importação . . . upload: " + upload);

		if (file != null && file.getFileName() != null) {
			fileContent = file.getContent();
			
			if(upload == false) {
				//upload = true;
				
				System.out.println(file.getFileName());
				System.out.println("Opção: " + opcao);
				System.out.println("Upload: " + upload);

				if (!opcao.equalsIgnoreCase("")) {
					
					if (opcao.equalsIgnoreCase("produtos")) {
						importarProdutos(file);
						upload = false;
					}
					
					if (opcao.equalsIgnoreCase("clientes")) {
						importarClientes(file);
						upload = false;
					}

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
			
			if(produtos.size() == 0) {
				PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
			}
		}

		System.out.println(fileContent);

	}
	
	
	public void importarDados() {

		if (!opcao.equalsIgnoreCase("")) {
			
			if (opcao.equalsIgnoreCase("produtos")) {
				
				int cont = 0;
				for (Produto produto : produtos) {	
					if(produto.isValid()) {
						
						Produto produtoTemp = produtosRepository.porCodigo(produto.getCodigo(), usuario.getEmpresa());
						if(produtoTemp == null) {
							
							CategoriaProduto categoriaProduto = categoriasProdutosRepository.porNome(produto.getCategoriaProduto().getNome(), usuario.getEmpresa());			
							
							if(categoriaProduto == null) {
								categoriaProduto = produto.getCategoriaProduto();
								categoriaProduto = categoriasProdutosRepository.save(categoriaProduto);
								produto.setCategoriaProduto(categoriaProduto);
							} else {
								produto.setCategoriaProduto(categoriaProduto);
							}
							
							produtosRepository.save(produto);
							cont++;
						}	
					}
				}	
				
				produtos = new ArrayList<Produto>();
				
				if(cont > 0) {
					
					String qtde = "" + cont;
					if(cont < 10) {
						qtde = "0" + cont;
					}
					
					PrimeFaces.current()
						.executeScript("swal({ type: 'success', title: 'Concluído!', text: '" + qtde + " produto(s) salvo(s) com sucesso!' });");
				} else {
					
					PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Nenhum produto foi salvo!' });");
					
				}
				
			}
			
			
			if (opcao.equalsIgnoreCase("clientes")) {
				
				int cont = 0;
				for (Cliente cliente : clientes) {	
					if(cliente.isValid()) {
						
						Bairro bairro = bairrosRepository.porNome(cliente.getBairro().getNome(), usuario.getEmpresa());			
						
						if(bairro == null) {
							bairro = cliente.getBairro();
							bairro = bairrosRepository.save(bairro);
							cliente.setBairro(bairro);
						} else {
							cliente.setBairro(bairro);
						}
						
						clientesRepository.save(cliente);
						cont++;	
					}
				}	
				
				clientes = new ArrayList<Cliente>();
				
				if(cont > 0) {
					
					String qtde = "" + cont;
					if(cont < 10) {
						qtde = "0" + cont;
					}
					
					PrimeFaces.current()
						.executeScript("swal({ type: 'success', title: 'Concluído!', text: '" + qtde + " cliente(s) salvo(s) com sucesso!' });");
				} else {
					
					PrimeFaces.current()
						.executeScript("swal({ type: 'error', title: 'Erro!', text: 'Nenhum cliente foi salvo!' });");
					
				}
				
			}
		}	
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
	
	public String convertToTitleCaseIteratingChars(String text) {
	    if (text == null || text.isEmpty()) {
	        return text;
	    }
	 
	    StringBuilder converted = new StringBuilder();
	 
	    boolean convertNext = true;
	    for (char ch : text.toCharArray()) {
	        if (Character.isSpaceChar(ch)) {
	            convertNext = true;
	        } else if (convertNext) {
	            ch = Character.toTitleCase(ch);
	            convertNext = false;
	        } else {
	            ch = Character.toLowerCase(ch);
	        }
	        converted.append(ch);
	    }
	 
	    return converted.toString();
	}

	public String getOpcao() {
		return opcao;
	}

	public void setOpcao(String opcao) {
		this.opcao = opcao;
	}

	public List<Produto> getProdutos() {
		return produtos;
	}
	
	public Integer getProdutosSize() {
		return produtos.size();
	}
	
	public Integer getClientesSize() {
		return clientes.size();
	}
	
	
	public void changeImport() {		
		System.out.println(opcao);
		produtos = new ArrayList<Produto>();
		clientes = new ArrayList<Cliente>();
	}

}
