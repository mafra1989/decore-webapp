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

import com.webapp.model.Compra;
import com.webapp.model.ItemCompra;
import com.webapp.model.Produto;
import com.webapp.model.Usuario;
import com.webapp.repository.Compras;
import com.webapp.repository.ItensCompras;
import com.webapp.repository.Produtos;
import com.webapp.repository.Usuarios;
import com.webapp.util.jsf.FacesUtil;

@Named
@ViewScoped
public class ImportarDadosBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Compras comprasRepository;
	
	@Inject
	private ItensCompras itensComprasRepository;
	
	@Inject
	private Produtos produtosRepository;
	
	@Inject
	private Usuarios usuariosRepository;

	private UploadedFile file;

	private byte[] fileContent;

	public void inicializar() {
		if (FacesUtil.isNotPostback()) {

		}
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void importar() {

		if (file != null && file.getFileName() != null) {
			fileContent = file.getContents();

			System.out.println(file.getFileName());

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

							compra.setQuantidadeItens((long) (compra.getQuantidadeItens()
									+ Double.parseDouble(row.getCell(5).toString())));
							compra.setValorTotal(BigDecimal.valueOf(compra.getValorTotal().doubleValue()
									+ (Double.parseDouble(row.getCell(4).toString())
											* Double.parseDouble(row.getCell(5).toString()))));

							ItemCompra itemCompra = new ItemCompra();
							itemCompra.setQuantidade((long) Double.parseDouble(row.getCell(5).toString()));
							itemCompra.setQuantidadeDisponivel(itemCompra.getQuantidade());
							itemCompra.setValorUnitario(
									BigDecimal.valueOf(Double.parseDouble(row.getCell(4).toString())));
							itemCompra.setTotal(BigDecimal.valueOf(Double.parseDouble(row.getCell(4).toString())
									* Double.parseDouble(row.getCell(5).toString())));
							
							System.out.println(row.getCell(2).toString());
							String codigo = ((long) Double.parseDouble(row.getCell(2).toString())) + "";
							Produto produto = produtosRepository.porCodigo(codigo);
							
							if(produto != null) {
								itemCompra.setProduto(produto);	
							} else {
								produto = new Produto();
								produto.setCodeTemp(codigo);
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
						if(itemCompraTemp.getProduto().getId() == null) {
							produtoNaoEncontrado = true;
							codigos.add(itemCompraTemp.getProduto().getCodeTemp());
						}
					}
				}

				if(produtoNaoEncontrado != true) {
					
					for (Compra compraTemp : compras) {
						System.out.println("Quant. Itens: " + compraTemp.getQuantidadeItens() + " - Valor Total: "
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
						
					}
					
					System.out.println("Total de Compras: " + compras.size());

					PrimeFaces.current().executeScript(
							"swal({ type: 'success', title: 'Concluído!', text: 'Dados importados com sucesso!' });");
				} else {
					PrimeFaces.current().executeScript(
							"swal({ type: 'error', title: 'Erro!', text: 'Produtos não encontrados! Verifique os códigos: " + Arrays.asList(codigos) + " ' });");
				}
				

			} catch (IOException | IllegalArgumentException e) {
				PrimeFaces.current().executeScript(
						"swal({ type: 'error', title: 'Erro!', text: 'Selecione um arquivo válido!' });");
			}

			fileContent = null;

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

}
