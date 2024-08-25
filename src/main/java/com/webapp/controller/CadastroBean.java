package com.webapp.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.primefaces.PrimeFaces;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.webapp.manhattan.view.GuestPreferences.LayoutMode;
import com.webapp.model.Bairro;
import com.webapp.model.CategoriaLancamento;
import com.webapp.model.Cliente;
import com.webapp.model.Configuracao;
import com.webapp.model.DestinoLancamento;
import com.webapp.model.Empresa;
import com.webapp.model.FormaPagamento;
import com.webapp.model.Grupo;
import com.webapp.model.OrigemLancamento;
import com.webapp.model.TipoImpressao;
import com.webapp.model.TipoLancamento;
import com.webapp.model.TipoVenda;
import com.webapp.model.Usuario;
import com.webapp.repository.Bairros;
import com.webapp.repository.CategoriasLancamentos;
import com.webapp.repository.Clientes;
import com.webapp.repository.Configuracoes;
import com.webapp.repository.DestinosLancamentos;
import com.webapp.repository.Empresas;
import com.webapp.repository.FormasPagamentos;
import com.webapp.repository.Grupos;
import com.webapp.repository.TiposLancamentos;
import com.webapp.repository.TiposVendas;
import com.webapp.repository.Usuarios;

@Named
@SessionScoped
public class CadastroBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Usuarios usuarios;
	
	@Inject
	private Empresas empresas;
	
	@Inject
	private Grupos grupos;
	
	@Inject
	private Bairros bairros;
	
	@Inject
	private Clientes clientes;
	
	@Inject
	private TiposVendas tiposVendas;
	
	@Inject
	private FormasPagamentos formasPagamentos;
	
	@Inject
	private TiposLancamentos tiposLancamentos;
	
	@Inject
	private DestinosLancamentos destinosLancamentos;
	
	@Inject
	private CategoriasLancamentos categoriasLancamentos;
	
	@Inject
	private Configuracoes configuracoes;
	
	@Inject
	private Usuario usuario;
	
	@NotBlank
	@Email
	private String email;

	
	public void cadastrar() {
		
		Usuario usuario = usuarios.porLogin(email);
		
		if(usuario == null) {
			
			String password = new Random().ints(10, 33, 122).collect(StringBuilder::new,
			        StringBuilder::appendCodePoint, StringBuilder::append)
			        .toString();
			
			Empresa empresa = new Empresa();
			empresa.setDataCadastro(new Date());
			empresa.setNome("Minha Empresa");
			empresa.setChaveDeAcesso(password);
			empresa = empresas.save(empresa);
			
			usuario = new Usuario();
			usuario.setNome("Administrador");
			usuario.setFuncao("Administrador");
			usuario.setLogin(email);
	
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String hashedPassword = passwordEncoder.encode(password);
			
			//String hashedPassword = passwordEncoder.encode(this.usuario.getSenha());

			usuario.setSenha(hashedPassword);
			usuario.setEmpresa(empresa);
			usuario = usuarios.save(usuario);
			
			Grupo grupo = new Grupo();			
			grupo.setNome("ADMINISTRADOR");
			grupo.setDescricao("Administrador");
			grupo.setEmpresa(empresa);
			grupo = grupos.save(grupo);

			grupos.saveUsuarioGrupo("INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES ("+usuario.getId()+","+grupo.getId()+");");
			
			grupo = new Grupo();			
			grupo.setNome("VENDEDOR");
			grupo.setDescricao("Vendedor");
			grupo.setEmpresa(empresa);
			grupo = grupos.save(grupo);
	
			
			Bairro bairro = new Bairro();
			bairro.setNome("Nao Informado");
			bairro.setTaxaDeEntrega(BigDecimal.ZERO);
			bairro.setEmpresa(empresa);
			bairro = bairros.save(bairro);
			
			Cliente cliente = new Cliente();
			cliente.setNome("Nao Informado");
			cliente.setContato("(99) 9-9999-9999");
			cliente.setBairro(bairro);
			cliente.setEmpresa(empresa);
			clientes.save(cliente);
			
			TipoVenda tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Olx");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Face");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Loja");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Cliente");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Indicacao");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Instagran");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Site");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			tipoVenda = new TipoVenda();
			tipoVenda.setDescricao("Nao Informado");
			tipoVenda.setEmpresa(empresa);
			tiposVendas.save(tipoVenda);
			
			FormaPagamento formaPagamento = new FormaPagamento();
			formaPagamento.setNome("Dinheiro");
			formaPagamento.setAcrescimo(BigDecimal.ZERO);
			formaPagamento.setClientePaga(false);
			formaPagamento.setEmpresa(empresa);
			formasPagamentos.save(formaPagamento);
			
			formaPagamento = new FormaPagamento();
			formaPagamento.setNome("Debito");
			formaPagamento.setAcrescimo(BigDecimal.ZERO);
			formaPagamento.setClientePaga(false);
			formaPagamento.setEmpresa(empresa);
			formasPagamentos.save(formaPagamento);
			
			formaPagamento = new FormaPagamento();
			formaPagamento.setNome("Credito");
			formaPagamento.setAcrescimo(BigDecimal.ZERO);
			formaPagamento.setClientePaga(false);
			formaPagamento.setEmpresa(empresa);
			formasPagamentos.save(formaPagamento);
			
			DestinoLancamento destinoLancamento = new DestinoLancamento();
			destinoLancamento.setDescricao("Minha Empresa");
			destinoLancamento.setEmpresa(empresa);
			destinoLancamento = destinosLancamentos.save(destinoLancamento);
			
			TipoLancamento tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Receitas");
			tipoLancamento.setOrigem(OrigemLancamento.CREDITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			CategoriaLancamento categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Aporte");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Outras receitas");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Custos");
			tipoLancamento.setOrigem(OrigemLancamento.DEBITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Aquisicao de bens e equipamentos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Decimo");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);

			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Ferias");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Bonus");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("PLR");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Hora Extra");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Salarios");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Despesa fixa");
			tipoLancamento.setOrigem(OrigemLancamento.DEBITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Agua");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Luz");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Aluguel");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Telefone, internet e informatica");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Transporte");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Refeicao");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Despesa variavel");
			tipoLancamento.setOrigem(OrigemLancamento.DEBITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Manutencao de Veiculos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Manutencao de Equipamentos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Despesas com alimentacao e mantimentos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Avarias");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Combustivel");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Custos e viagens a negocios");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Despesas com saude");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Emprestimos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Frete de veiculos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Impostos");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Limpeza e servicos gerais");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Mao de obra com entregas");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Material e servico de construcao");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Material indireto");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Material para uso direto");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Propaganda e marketing");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Seguranca");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Servico de manutencao em geral");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Outras despesas");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Comissões Pagas");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Investimentos");
			tipoLancamento.setOrigem(OrigemLancamento.DEBITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Ajuste de Caixa");
			tipoLancamento.setOrigem(OrigemLancamento.DEBITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Ajuste de Caixa");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
	
			tipoLancamento = new TipoLancamento();
			tipoLancamento.setDescricao("Lucros distribuidos");
			tipoLancamento.setOrigem(OrigemLancamento.DEBITO);
			tipoLancamento.setEmpresa(empresa);
			tipoLancamento = tiposLancamentos.save(tipoLancamento);
			
			categoriaLancamento = new CategoriaLancamento();
			categoriaLancamento.setNome("Retirada de lucro");
			categoriaLancamento.setDestinoLancamento(destinoLancamento);
			categoriaLancamento.setTipoLancamento(tipoLancamento);
			categoriaLancamento.setEmpresa(empresa);
			categoriasLancamentos.save(categoriaLancamento);
			
			Configuracao configuracao = new Configuracao();
			configuracao.setLeitorPDV(false);
			configuracao.setLeitorCompra(false);
			configuracao.setCupomAtivado(true);
			configuracao.setTipoImpressao(TipoImpressao.IMPRESSORA01);
			configuracao.setAbaPDV(1);
			configuracao.setLayoutMode(LayoutMode.STATIC);
			configuracao.setLightMenu(false);
			configuracao.setVias(1);
			configuracao.setPdvRapido(false);
			configuracao.setPopupCliente(false);
			configuracao.setControleMesas(false);
			configuracao.setProdutosUrlNuvem(true);
			configuracao.setUsuario(usuario);
			configuracao.setQuantidadeMesas(10);
			
			configuracoes.save(configuracao);
			
			PrimeFaces.current().executeScript("redirectToLogin();");
			//"swal({ type: 'success', title: 'Concluído!', text: 'Usuário cadastrado com sucesso!', footer: '<a href=\"Login.xhtml\">Click aqui para entrar!</a>' });"
			
			
		} else {
			
			PrimeFaces.current().executeScript("swal({ type: 'error', title: 'Ops!', text: 'Esse e-mail já foi cadastrado, tente outro e-mail.' });");
		}
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}