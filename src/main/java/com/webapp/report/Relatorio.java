package com.webapp.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
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

			InputStream relatorioStream = this.getClass().getResourceAsStream("/relatorios/decore-vendas.jasper");
			
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
}
