package com.webapp.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoDTO {
    private String numero;
    private LocalDate data;
    private ClienteDTO cliente;
    private List<ItemOrcamentoDTO> servicos = new ArrayList<>();
    private List<ItemOrcamentoDTO> materiais = new ArrayList<>();
    private BigDecimal totalServicos;
    private BigDecimal totalMateriais;
    private BigDecimal total;
    private String formasPagamento;
    private String cidade;

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public ClienteDTO getCliente() { return cliente; }
    public void setCliente(ClienteDTO cliente) { this.cliente = cliente; }

    public List<ItemOrcamentoDTO> getServicos() { return servicos; }
    public void setServicos(List<ItemOrcamentoDTO> servicos) { this.servicos = servicos; }

    public List<ItemOrcamentoDTO> getMateriais() { return materiais; }
    public void setMateriais(List<ItemOrcamentoDTO> materiais) { this.materiais = materiais; }

    public BigDecimal getTotalServicos() { return totalServicos; }
    public void setTotalServicos(BigDecimal totalServicos) { this.totalServicos = totalServicos; }

    public BigDecimal getTotalMateriais() { return totalMateriais; }
    public void setTotalMateriais(BigDecimal totalMateriais) { this.totalMateriais = totalMateriais; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getFormasPagamento() { return formasPagamento; }
    public void setFormasPagamento(String formasPagamento) { this.formasPagamento = formasPagamento; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
}
