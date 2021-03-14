package com.webapp.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.webapp.manhattan.view.GuestPreferences.LayoutMode;

@Entity
@Table(name = "configuracoes")
@SequenceGenerator(name="Configuracao_Seq", sequenceName="configuracoes_sequence", allocationSize=1,initialValue = 2)
public class Configuracao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="Configuracao_Seq")
	private Long id;
	
	@Type(type = "yes_no")
	@Column
	private boolean leitorPDV;
	
	@Type(type = "yes_no")
	@Column
	private boolean leitorCompra;
	
	@Type(type = "yes_no")
	@Column
	private boolean cupomAtivado;
	
	@Type(type = "yes_no")
	@Column
	private boolean pdvRapido;
	
	@Enumerated(EnumType.STRING)
	@Column
	private TipoImpressao tipoImpressao = TipoImpressao.IMPRESSORA01;
	
	@Column
	private Integer abaPDV;
	
	@Column
	private Integer vias;
	
	@ManyToOne
	@JoinColumn
	private CategoriaProduto categoriaProduto01;
	
	@ManyToOne
	@JoinColumn
	private CategoriaProduto categoriaProduto02;
	
	
	@Enumerated(EnumType.STRING)
	@Column
	private LayoutMode layoutMode;
	
	
	@Type(type = "yes_no")
	@Column
	private boolean lightMenu;
	
	
	
	@Column
	private String tamanho01;
	
	@Column
	private String unidade01;
	
	@Column
	private String tamanho02;
	
	@Column
	private String unidade02;
	
	@Type(type = "yes_no")
	@Column
	private boolean popupCliente;
	
	@Type(type = "yes_no")
	@Column
	private boolean produtosUrlNuvem;
	
	@Type(type = "yes_no")
	@Column
	private boolean controleMesas;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isLeitorPDV() {
		return leitorPDV;
	}

	public void setLeitorPDV(boolean leitorPDV) {
		this.leitorPDV = leitorPDV;
	}

	public boolean isLeitorCompra() {
		return leitorCompra;
	}

	public void setLeitorCompra(boolean leitorCompra) {
		this.leitorCompra = leitorCompra;
	}

	public boolean isCupomAtivado() {
		return cupomAtivado;
	}

	public void setCupomAtivado(boolean cupomAtivado) {
		this.cupomAtivado = cupomAtivado;
	}

	public boolean isPdvRapido() {
		return pdvRapido;
	}

	public void setPdvRapido(boolean pdvRapido) {
		this.pdvRapido = pdvRapido;
	}

	public TipoImpressao getTipoImpressao() {
		return tipoImpressao;
	}

	public void setTipoImpressao(TipoImpressao tipoImpressao) {
		this.tipoImpressao = tipoImpressao;
	}

	public Integer getAbaPDV() {
		return abaPDV;
	}

	public void setAbaPDV(Integer abaPDV) {
		this.abaPDV = abaPDV;
	}

	public Integer getVias() {
		return vias;
	}

	public void setVias(Integer vias) {
		this.vias = vias;
	}

	public CategoriaProduto getCategoriaProduto01() {
		return categoriaProduto01;
	}

	public void setCategoriaProduto01(CategoriaProduto categoriaProduto01) {
		this.categoriaProduto01 = categoriaProduto01;
	}

	public CategoriaProduto getCategoriaProduto02() {
		return categoriaProduto02;
	}

	public void setCategoriaProduto02(CategoriaProduto categoriaProduto02) {
		this.categoriaProduto02 = categoriaProduto02;
	}

	public LayoutMode getLayoutMode() {
		return layoutMode;
	}

	public void setLayoutMode(LayoutMode layoutMode) {
		this.layoutMode = layoutMode;
	}

	public boolean isLightMenu() {
		return lightMenu;
	}

	public void setLightMenu(boolean lightMenu) {
		this.lightMenu = lightMenu;
	}

	public String getTamanho01() {
		return tamanho01;
	}

	public void setTamanho01(String tamanho01) {
		this.tamanho01 = tamanho01;
	}

	public String getUnidade01() {
		return unidade01;
	}

	public void setUnidade01(String unidade01) {
		this.unidade01 = unidade01;
	}

	public String getTamanho02() {
		return tamanho02;
	}

	public void setTamanho02(String tamanho02) {
		this.tamanho02 = tamanho02;
	}

	public String getUnidade02() {
		return unidade02;
	}

	public void setUnidade02(String unidade02) {
		this.unidade02 = unidade02;
	}

	public boolean isPopupCliente() {
		return popupCliente;
	}

	public void setPopupCliente(boolean popupCliente) {
		this.popupCliente = popupCliente;
	}

	public boolean isProdutosUrlNuvem() {
		return produtosUrlNuvem;
	}

	public void setProdutosUrlNuvem(boolean produtosUrlNuvem) {
		this.produtosUrlNuvem = produtosUrlNuvem;
	}

	public boolean isControleMesas() {
		return controleMesas;
	}

	public void setControleMesas(boolean controleMesas) {
		this.controleMesas = controleMesas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Configuracao other = (Configuracao) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}