package com.webapp.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class Relatorio<T> {

	private HttpServletResponse response;
	private FacesContext context;

	public Relatorio() {
		context = FacesContext.getCurrentInstance();
		response = (HttpServletResponse) context.getExternalContext().getResponse();
	}

	public void getRelatorio(List<T> lista, String filename) throws SQLException {
		try {

			//InputStream relatorioStream = this.getClass().getResourceAsStream("/relatorios/decore-vendas.jasper");
			InputStream relatorioStream = this.getClass().getResourceAsStream("/relatorios/danfeNfce.jasper");
			
			System.out.println(relatorioStream);

			//ByteArrayOutputStream baos = new ByteArrayOutputStream();

			JRDataSource jrds = new JRBeanCollectionDataSource(lista, false);

			JasperPrint print = JasperFillManager.fillReport(relatorioStream, null, jrds);

			//JasperExportManager.exportReportToPdfStream(print, baos);
			
			JasperPrintManager.printReport(print, false);
			
			

			String path = "C:/xampp/tomcat/webapps/cupons/" + filename + ".pdf";
			
			new File(path).delete();
			
			File file = new File(path);
			
			try {
				FileOutputStream fos = new FileOutputStream(file);
				try {
					fos.write(JasperExportManager.exportReportToPdf(print));
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			/*
			PDDocument documento = PDDocument.load(JasperExportManager.exportReportToPdf(print));
			PrintService servico = PrintServiceLookup.lookupDefaultPrintService();
	
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PDFPageable(documento));
			job.setPrintService(servico);
			job.print();
			documento.close();		
			*/

			/*response.reset();

			response.setContentType("application/pdf");

			response.setContentLength(baos.size());

			response.setHeader("Content-disposition", "inline; filename=" + filename + ".pdf");
			//response.setHeader("Content-disposition", "attachment; filename=" + filename + ".pdf");

			response.getOutputStream().write(baos.toByteArray());

			response.getOutputStream().flush();

			response.getOutputStream().close();

			context.responseComplete();
			*/

		} catch (Exception e) {
			throw new SQLException("Erro ao executar relatório", e);
		}

	}
	
	
	public void getRelatorio_(List<T> lista, String filename, String path) throws SQLException {
		try {

			InputStream relatorioStream = this.getClass().getResourceAsStream(path);
			
			System.out.println(relatorioStream);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			JRDataSource jrds = new JRBeanCollectionDataSource(lista, false);

			JasperPrint print = JasperFillManager.fillReport(relatorioStream, null, jrds);

			JasperExportManager.exportReportToPdfStream(print, baos);

			response.reset();

			response.setContentType("application/pdf");

			response.setContentLength(baos.size());

			response.setHeader("Content-disposition", "inline; filename=" + filename + ".pdf");
			//response.setHeader("Content-disposition", "attachment; filename=" + filename + ".pdf");

			response.getOutputStream().write(baos.toByteArray());

			response.getOutputStream().flush();

			response.getOutputStream().close();

			context.responseComplete();
			

		} catch (Exception e) {
			throw new SQLException("Erro ao executar relatório", e);
		}

	}
	
	
	public void getOrcamento(List<T> lista, String filename) throws SQLException {
		try {

			InputStream relatorioStream = this.getClass().getResourceAsStream("/relatorios/decore-vendas_ORCAMENTO.jasper");
			
			System.out.println(relatorioStream);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			JRDataSource jrds = new JRBeanCollectionDataSource(lista, false);

			JasperPrint print = JasperFillManager.fillReport(relatorioStream, null, jrds);

			JasperExportManager.exportReportToPdfStream(print, baos);

			response.reset();

			response.setContentType("application/pdf");

			response.setContentLength(baos.size());

			response.setHeader("Content-disposition", "inline; filename=" + filename + ".pdf");
			//response.setHeader("Content-disposition", "attachment; filename=" + filename + ".pdf");

			response.getOutputStream().write(baos.toByteArray());

			response.getOutputStream().flush();

			response.getOutputStream().close();

			context.responseComplete();
			

		} catch (Exception e) {
			throw new SQLException("Erro ao executar relatório", e);
		}

	}
	
	public byte[] getRelatorio__(List<T> lista, String filename) throws SQLException {
		try {

			InputStream relatorioStream = this.getClass().getResourceAsStream("/relatorios/decore-vendas.jasper");
			
			System.out.println(relatorioStream);

			JRDataSource jrds = new JRBeanCollectionDataSource(lista, false);

			JasperPrint print = JasperFillManager.fillReport(relatorioStream, null, jrds);
			
			return JasperExportManager.exportReportToPdf(print);			

		} catch (Exception e) {
			throw new SQLException("Erro ao executar relatório", e);
		}
	}
	
	
	public void getRelatorioEstoque(List<T> lista, String filename) throws SQLException {
		try {

			InputStream relatorioStream = this.getClass().getResourceAsStream("/relatorios/lista-itens.jasper");
			
			System.out.println(relatorioStream);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			JRDataSource jrds = new JRBeanCollectionDataSource(lista, false);

			JasperPrint print = JasperFillManager.fillReport(relatorioStream, null, jrds);

			JasperExportManager.exportReportToPdfStream(print, baos);

			response.reset();

			response.setContentType("application/pdf");

			response.setContentLength(baos.size());

			//response.setHeader("Content-disposition", "inline; filename=" + filename + ".pdf");
			response.setHeader("Content-disposition", "attachment; filename=" + filename + ".pdf");

			response.getOutputStream().write(baos.toByteArray());

			response.getOutputStream().flush();

			response.getOutputStream().close();

			context.responseComplete();

		} catch (Exception e) {
			throw new SQLException("Erro ao executar relatório", e);
		}

	}
	
	public byte[] getExtratoMovimentacoes(List<T> lista, String filename, String jasperFile) throws SQLException {
		try {

			InputStream relatorioStream = this.getClass().getResourceAsStream(jasperFile);
			
			System.out.println(relatorioStream);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			JRDataSource jrds = new JRBeanCollectionDataSource(lista, false);

			JasperPrint print = JasperFillManager.fillReport(relatorioStream, null, jrds);

			JasperExportManager.exportReportToPdfStream(print, baos);			

			//response.reset();

			//response.setContentType("application/pdf");

			//response.setContentLength(baos.size());

			//response.setHeader("Content-disposition", "inline; filename=" + filename + ".pdf");
			//response.setHeader("Content-disposition", "attachment; filename=" + filename + ".pdf");

			//response.getOutputStream().write(baos.toByteArray());

			//response.getOutputStream().flush();

			//response.getOutputStream().close();

			//context.responseComplete();	
			
			return baos.toByteArray();

		} catch (Exception e) {
			throw new SQLException("Erro ao executar relatório", e);
		}

	}
}
