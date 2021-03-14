INSERT INTO empresas(id, nome) VALUES (1, 'Minha Empresa');
	
INSERT INTO usuarios(id,contato,cpf,funcao,login,nome,senha,foto,urlimagem,empresa_id,entregador) VALUES (1,'(99) 9-9999-9999','999.999.999-99','CEO','admin','Administrador','$2a$10$6vJAj/du3jPhm9oarUEJPODuh6gjaKAb2ExQAP8puibUmsBNVOxBa',NULL,NULL,1,'N');
INSERT INTO usuarios(id,contato,cpf,funcao,login,nome,senha,foto,urlimagem,empresa_id,entregador) VALUES (2,'(99) 9-9999-9999','999.999.999-99','Atendente','user','Usuario PDV','$2a$10$6vJAj/du3jPhm9oarUEJPODuh6gjaKAb2ExQAP8puibUmsBNVOxBa',NULL,NULL,1,'N');

INSERT INTO grupos(id,descricao,nome) VALUES (1,'Administrador','ADMINISTRADOR');
INSERT INTO grupos(id,descricao,nome) VALUES (2,'Usuario Avancado','USUARIO_AVANCADO');
INSERT INTO grupos(id,descricao,nome) VALUES (3,'Usuario Comum','USUARIO_COMUM');
INSERT INTO grupos(id,descricao,nome) VALUES (4,'Usuario PDV','VENDEDOR');

INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES (1,1);
INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES (2,4);

INSERT INTO bairros(id,nome,zona,taxadeentrega) VALUES (1,'Nao Informado',null,0.00);

INSERT INTO clientes(id,nome,contato,bairro_id) VALUES (1,'Nao Informado','(99) 9-9999-9999',1);

INSERT INTO tipos_vendas(id,descricao) VALUES (1,'Olx');
INSERT INTO tipos_vendas(id,descricao) VALUES (2,'Face');
INSERT INTO tipos_vendas(id,descricao) VALUES (3,'Loja');
INSERT INTO tipos_vendas(id,descricao) VALUES (4,'Cliente');
INSERT INTO tipos_vendas(id,descricao) VALUES (5,'Indicacao');
INSERT INTO tipos_vendas(id,descricao) VALUES (6,'Instagran');
INSERT INTO tipos_vendas(id,descricao) VALUES (7,'Site');
INSERT INTO tipos_vendas(id,descricao) VALUES (8,'Nao Informado');

INSERT INTO formas_pagamentos(id, acrescimo, nome, clientePaga) VALUES (1, 0.00, 'Dinheiro', 'Y');
INSERT INTO formas_pagamentos(id, acrescimo, nome, clientePaga) VALUES (2, 0.00, 'Debito', 'Y');
INSERT INTO formas_pagamentos(id, acrescimo, nome, clientePaga) VALUES (3, 0.00, 'Credito', 'Y');

INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (1,'Receitas',1);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (2,'Custos',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (3,'Despesa fixa',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (4,'Despesa variavel',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (5,'Investimentos',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (6,'Ajuste de Caixa',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (7,'Lucros distribuidos',0);

INSERT INTO destino_lancamentos(id,descricao) VALUES (1,'Minha Empresa');

INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (1,'Agua',1,3,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (2,'Luz',1,3,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (3,'Aporte',1,1,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (4,'Aluguel',1,3,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (5,'Manutencao de Veiculos',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (6,'Manutencao de Equipamentos',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (7,'Despesas com alimentacao e mantimentos',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (8,'Aquisicao de bens e equipamentos',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (9,'Avarias',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (10,'Combustivel',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (11,'Custos e viagens a negocios',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (12,'Despesas com saude',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (13,'Emprestimos',1,41,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (14,'Frete de veiculos',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (15,'Impostos',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (16,'Limpeza e servicos gerais',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (17,'Mao de obra com entregas',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (18,'Material e servico de construcao',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (19,'Material indireto',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (20,'Material para uso direto',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (21,'Outras receitas',1,1,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (22,'Propaganda e marketing',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (23,'Seguranca',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (24,'Servico de manutencao em geral',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (25,'Telefone, internet e informatica',1,3,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (26,'Transporte',1,3,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (27,'Salarios',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (28,'Outras despesas',1,4,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (29,'Ajuste de Caixa',1,6,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (30,'Refeicao',1,3,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (31,'Decimo',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (32,'Ferias',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (33,'Bonus',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (34,'PLR',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (35,'Hora Extra',1,2,null);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id,empresa_id) VALUES (36,'Retirada de lucro',1,7,null);

insert into configuracoes (id, leitorpdv, leitorcompra, cupomativado, tipoimpressao, abapdv, categoriaproduto01_id, categoriaproduto02_id, layoutmode, lightmenu, tamanho01, tamanho02, unidade01, unidade02, vias, pdvrapido, popupCliente) values (1, 'N', 'N', 'Y', 'IMPRESSORA01', 1, null, null, 'STATIC', 'N', null, null, null, null, 1, 'N', 'Y');

