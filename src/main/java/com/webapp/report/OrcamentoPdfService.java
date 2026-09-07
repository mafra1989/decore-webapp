package com.webapp.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.webapp.model.ItemVenda;
import com.webapp.model.Venda;
import com.webapp.report.dto.ClienteDTO;
import com.webapp.report.dto.EmpresaDTO;
import com.webapp.report.dto.ItemOrcamentoDTO;
import com.webapp.report.dto.OrcamentoDTO;

@ApplicationScoped
public class OrcamentoPdfService {

    private static final String REPORTS = "templates/assets/";

    @Inject
    private TemplateEngine templateEngine;

    public byte[] gerarPdf(Venda venda, List<ItemVenda> itensVenda) {
    	
    	OrcamentoDTO orcamento = new OrcamentoDTO();

    	orcamento.setNumero(StringUtils.leftPad(String.valueOf(venda.getNumeroVenda()), 4, "0") + "-" + String.valueOf(venda.getAno()));
    	orcamento.setData(LocalDate.now());
    	orcamento.setCidade("Manaus");


    	// ======================================================
    	// CLIENTE
    	// ======================================================

    	ClienteDTO cliente = new ClienteDTO();
    	cliente.setNome(venda.getCliente().getNome());
    	cliente.setTelefone(venda.getCliente().getContato());

    	orcamento.setCliente(cliente);


    	// ======================================================
    	// SERVIÇOS
    	// ======================================================

    	ItemOrcamentoDTO servico = new ItemOrcamentoDTO();

    	servico.setTitulo(venda.getDescricaoServico());
    	servico.setDetalhe(null);
    	servico.setUnidade("");
    	servico.setValorUnitario(venda.getValorServico());
    	servico.setQuantidade(venda.getQuantidadeServico().intValue());
    	servico.setValorTotal(new BigDecimal(venda.getValorServico().doubleValue() * venda.getQuantidadeServico().doubleValue()));

    	orcamento.setServicos(
    	        Arrays.asList(servico)
    	);


    	// ======================================================
    	// MATERIAIS
    	// ======================================================
    	
    	for (ItemVenda itemVenda : itensVenda) {
    		
    		ItemOrcamentoDTO material = new ItemOrcamentoDTO();

    		material.setTitulo(itemVenda.getProduto().getNome());
    		material.setDetalhe(itemVenda.getProduto().getDescricao());
    		material.setUnidade("");
    		material.setValorUnitario(itemVenda.getValorUnitario());
    		material.setQuantidade(itemVenda.getQuantidade().intValue());
    		material.setValorTotal(itemVenda.getTotal());
    		
        	orcamento.getMateriais().add(material);
		}

    	// ======================================================
    	// TOTAIS
    	// ======================================================

    	orcamento.setTotalServicos(servico.getValorTotal());

    	orcamento.setTotalMateriais(new BigDecimal(venda.getValorTotal().doubleValue() - servico.getValorTotal().doubleValue()));

    	orcamento.setTotal(venda.getValorTotal());


    	// ======================================================
    	// FORMAS DE PAGAMENTO
    	// ======================================================

    	orcamento.setFormasPagamento(
    	        "Transferência bancária, dinheiro, cartão de crédito, " +
    	        "cartão de débito, pix, picpay ou link de pagamento."
    	);
    	
    	// ======================================================
    	// EMPRESA
    	// ======================================================

    	EmpresaDTO empresa = criarEmpresa();
    	
    	// ======================================================
    	
        normalizarDetalhes(orcamento);

        byte[] pdf = gerarHtmlPdf(orcamento, empresa);

        return finalizarPdf(
                pdf,
                empresa
        );
    }

    private byte[] gerarHtmlPdf(
            OrcamentoDTO orcamento,
            EmpresaDTO empresa) {

        Context context = new Context();

        context.setVariable("orcamento", orcamento);
        context.setVariable("empresa", empresa);

        /*
         * Imagens principais
         */
        context.setVariable(
                "logo",
                carregarImagemBase64(REPORTS + "logo-newdecor.png")
        );

        context.setVariable(
                "assinatura",
                carregarImagemBase64(REPORTS + "assinatura-newdecor.png")
        );

        /*
         * Ícones extraídos/reproduzidos a partir do modelo original.
         */
        context.setVariable(
                "iconEmail",
                carregarImagemBase64(REPORTS + "icon_email.png")
        );

        context.setVariable(
                "iconPhone",
                carregarImagemBase64(REPORTS + "icon_phone.png")
        );

        context.setVariable(
                "iconWhatsapp",
                carregarImagemBase64(REPORTS + "icon_whatsapp.png")
        );

        context.setVariable(
                "iconInstagram",
                carregarImagemBase64(REPORTS + "icon_instagram.png")
        );

        context.setVariable(
                "iconFacebook",
                carregarImagemBase64(REPORTS + "icon_facebook.png")
        );

        context.setVariable(
                "iconCalendar",
                carregarImagemBase64(REPORTS + "icon_calendar.png")
        );

        String html =
                templateEngine.process(
                        "orcamento",
                        context
                );

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.useFastMode();

            /*
             * O template usa Arial como primeira opção e Helvetica como
             * fallback. Para equivalência tipográfica 100% exata em Linux,
             * registre uma Arial licenciada da própria aplicação.
             */
            builder.withHtmlContent(
                    html,
                    null
            );

            builder.toStream(output);
            builder.run();

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao gerar PDF do orçamento.",
                    e
            );
        }
    }

    /**
     * Finaliza o PDF depois que o OpenHTMLToPDF concluiu a paginação.
     *
     * - numeração em todas as páginas;
     * - rodapé institucional somente na última página;
     * - rodapé sempre no fundo físico da folha.
     */
    private byte[] finalizarPdf(
            byte[] pdf,
            EmpresaDTO empresa) {

        try (
                PDDocument document = PDDocument.load(pdf);
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {

            int totalPaginas =
                    document.getNumberOfPages();

            PDImageXObject iconEmail =
                    criarImagemPdf(
                            document,
                            REPORTS + "icon_email.png",
                            "icon-email"
                    );

            PDImageXObject iconPhone =
                    criarImagemPdf(
                            document,
                            REPORTS + "icon_phone.png",
                            "icon-phone"
                    );

            PDImageXObject iconWhatsapp =
                    criarImagemPdf(
                            document,
                            REPORTS + "icon_whatsapp.png",
                            "icon-whatsapp"
                    );

            PDImageXObject iconInstagram =
                    criarImagemPdf(
                            document,
                            REPORTS + "icon_instagram.png",
                            "icon-instagram"
                    );

            PDImageXObject iconFacebook =
                    criarImagemPdf(
                            document,
                            REPORTS + "icon_facebook.png",
                            "icon-facebook"
                    );

            for (int i = 0; i < totalPaginas; i++) {

                PDPage pagina =
                        document.getPage(i);

                int numeroPagina =
                        i + 1;

                try (
                        PDPageContentStream content =
                                new PDPageContentStream(
                                        document,
                                        pagina,
                                        PDPageContentStream.AppendMode.APPEND,
                                        true,
                                        true
                                )
                ) {

                    adicionarNumeroPagina(
                            content,
                            pagina,
                            numeroPagina,
                            totalPaginas
                    );

                    if (numeroPagina == totalPaginas) {

                        adicionarRodape(
                                content,
                                pagina,
                                empresa,
                                iconEmail,
                                iconPhone,
                                iconWhatsapp,
                                iconInstagram,
                                iconFacebook
                        );
                    }
                }
            }

            document.save(output);

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erro ao finalizar PDF do orçamento.",
                    e
            );
        }
    }

    /**
     * A numeração aparece em todas as páginas.
     */
    private void adicionarNumeroPagina(
            PDPageContentStream content,
            PDPage pagina,
            int numeroPagina,
            int totalPaginas) throws IOException {

        float largura =
                pagina
                        .getMediaBox()
                        .getWidth();

        String texto =
                "Página "
                        + numeroPagina
                        + "/"
                        + totalPaginas;

        content.setNonStrokingColor(
                25,
                25,
                25
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                8.5f,
                largura - 99f,
                14f,
                texto
        );

        restaurarCorTexto(content);
    }

    /**
     * Rodapé institucional exclusivamente da última página.
     */
    private void adicionarRodape(
            PDPageContentStream content,
            PDPage pagina,
            EmpresaDTO empresa,
            PDImageXObject iconEmail,
            PDImageXObject iconPhone,
            PDImageXObject iconWhatsapp,
            PDImageXObject iconInstagram,
            PDImageXObject iconFacebook) throws IOException {

        float largura =
                pagina
                        .getMediaBox()
                        .getWidth();

        float margemEsquerda =
                60.7f;

        float margemDireita =
                largura - 60.7f;

        /*
         * Valores calibrados visualmente para aproximar o documento original.
         */
        float linhaY =
                108f;

        float colunaEsquerdaX =
                margemEsquerda;

        float colunaDireitaX =
                301.6f;

        float primeiraLinhaY =
                93f;

        float passo =
                10.5f;

        float tamanhoRodape =
                8.5f;

        /*
         * Linha superior clara.
         */
        content.setLineWidth(
                0.45f
        );

        content.setStrokingColor(
                180,
                180,
                180
        );

        content.moveTo(
                margemEsquerda,
                linhaY
        );

        content.lineTo(
                margemDireita,
                linhaY
        );

        content.stroke();

        /*
         * Dados institucionais em cinza.
         */
        content.setNonStrokingColor(
                100,
                100,
                100
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                colunaEsquerdaX,
                primeiraLinhaY,
                empresa.getRazaoSocial()
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                colunaEsquerdaX,
                primeiraLinhaY - passo,
                "CNPJ: " + valor(empresa.getCnpj())
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                colunaEsquerdaX,
                primeiraLinhaY - (passo * 2),
                empresa.getEndereco()
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                colunaEsquerdaX,
                primeiraLinhaY - (passo * 3),
                empresa.getBairroCidade()
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                colunaEsquerdaX,
                primeiraLinhaY - (passo * 4),
                "CEP " + valor(empresa.getCep())
        );

        float textoContatoX =
                colunaDireitaX + 14f;

        desenharIcone(
                content,
                iconEmail,
                colunaDireitaX,
                primeiraLinhaY - 1f,
                10f,
                8f
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                textoContatoX,
                primeiraLinhaY,
                empresa.getEmail()
        );

        desenharIcone(
                content,
                iconPhone,
                colunaDireitaX,
                primeiraLinhaY - passo - 1f,
                10f,
                10f
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                textoContatoX,
                primeiraLinhaY - passo,
                empresa.getTelefone1()
        );

        desenharIcone(
                content,
                iconPhone,
                colunaDireitaX,
                primeiraLinhaY - (passo * 2) - 1f,
                10f,
                10f
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                textoContatoX,
                primeiraLinhaY - (passo * 2),
                empresa.getTelefone2()
        );

        desenharIcone(
                content,
                iconWhatsapp,
                colunaDireitaX,
                primeiraLinhaY - (passo * 3) - 1f,
                10f,
                10f
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                textoContatoX,
                primeiraLinhaY - (passo * 3),
                empresa.getWhatsapp()
        );

        /*
         * Redes sociais mais escuras.
         */
        content.setNonStrokingColor(
                25,
                25,
                25
        );

        float socialY =
                27f;

        desenharIcone(
                content,
                iconInstagram,
                colunaEsquerdaX,
                socialY - 1f,
                10f,
                10f
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                colunaEsquerdaX + 14f,
                socialY,
                empresa.getInstagram()
        );

        float facebookX =
                colunaEsquerdaX + 97f;

        desenharIcone(
                content,
                iconFacebook,
                facebookX,
                socialY - 1f,
                10f,
                10f
        );

        escreverTexto(
                content,
                PDType1Font.HELVETICA,
                tamanhoRodape,
                facebookX + 14f,
                socialY,
                empresa.getFacebook()
        );

        restaurarCorTexto(content);
    }

    private void restaurarCorTexto(
            PDPageContentStream content) throws IOException {

        content.setNonStrokingColor(
                0,
                0,
                0
        );

        content.setStrokingColor(
                0,
                0,
                0
        );
    }

    private void escreverTexto(
            PDPageContentStream content,
            PDType1Font fonte,
            float tamanho,
            float x,
            float y,
            String texto) throws IOException {

        if (texto == null || texto.isBlank()) {
            return;
        }

        content.beginText();

        content.setFont(
                fonte,
                tamanho
        );

        content.newLineAtOffset(
                x,
                y
        );

        /*
         * Os textos do rodapé deste layout são compatíveis com WinAnsi.
         * Caso passe a receber caracteres fora dessa faixa, utilize uma
         * fonte TrueType com PDType0Font.
         */
        content.showText(texto);

        content.endText();
    }

    private void desenharIcone(
            PDPageContentStream content,
            PDImageXObject imagem,
            float x,
            float y,
            float largura,
            float altura) throws IOException {

        content.drawImage(
                imagem,
                x,
                y,
                largura,
                altura
        );
    }

    private PDImageXObject criarImagemPdf(
            PDDocument document,
            String classpath,
            String nome) throws IOException {

        byte[] bytes =
                carregarImagemBytes(classpath);

        return PDImageXObject.createFromByteArray(
                document,
                bytes,
                nome
        );
    }

    private String carregarImagemBase64(
            String classpath) {

        byte[] bytes =
                carregarImagemBytes(classpath);

        return "data:image/png;base64,"
                + Base64
                .getEncoder()
                .encodeToString(bytes);
    }

    private byte[] carregarImagemBytes(
            String classpath) {

        try (
                InputStream input =
                        new ClassPathResource(
                                classpath
                        ).getInputStream()
        ) {

            return input.readAllBytes();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao carregar imagem: "
                            + classpath,
                    e
            );
        }
    }

    private void normalizarDetalhes(
            OrcamentoDTO orcamento) {

        if (orcamento == null) {
            return;
        }

        if (orcamento.getServicos() != null) {
            orcamento
                    .getServicos()
                    .forEach(item -> {

                        if (item != null) {
                            item.setDetalhe(
                                    normalizarQuebrasLinha(
                                            item.getDetalhe()
                                    )
                            );
                        }
                    });
        }

        if (orcamento.getMateriais() != null) {
            orcamento
                    .getMateriais()
                    .forEach(item -> {

                        if (item != null) {
                            item.setDetalhe(
                                    normalizarQuebrasLinha(
                                            item.getDetalhe()
                                    )
                            );
                        }
                    });
        }
    }

    private String normalizarQuebrasLinha(
            String texto) {

        if (texto == null) {
            return null;
        }

        return texto.replace(
                "\\n",
                "\n"
        );
    }

    private String valor(
            String valor) {

        return valor == null
                ? ""
                : valor;
    }
    
    private EmpresaDTO criarEmpresa() {
    	
        EmpresaDTO e = new EmpresaDTO();
        e.setNomeFantasia("New Decor Portas");
        e.setRazaoSocial("TACITO ALFREDO DANTAS NETO");
        e.setTitular("Tácito Dantas");
        e.setCnpj("32.783.038/0001-74");
        e.setEndereco("Rua João Câmara, 348");
        e.setBairroCidade("Novo Aleixo, Manaus-AM");
        e.setCep("69098-320");
        e.setEmail("newdecorportaseacabamentos@gmail.com");
        e.setTelefone1("+55 (92) 98209-8511");
        e.setTelefone2("+55 (92) 98613-3006");
        e.setWhatsapp("92982098511");
        e.setInstagram("newdecor.portas");
        e.setFacebook("newdecor.portas");
        e.setPix("32 783 038 0001 74");
        e.setBanco("Itaú");
        e.setAgencia("6959");
        e.setConta("99015-8");
        e.setTipoConta("Corrente");
        e.setTitularConta("32.783.038/0001-74");
        
        return e;
    }
    
}