INSERT INTO usuarios (id, nome, funcao, cpf, contato, foto, login, senha) VALUES (1,'Tácito','CEO','955.264.472.85','92 9-9999-9999',null,'tacito', '$2a$10$0yEvoqhIbxgkzMo7t9l35.nwGS53pM2zk1TLYD2MQkU7acTHx3K/.');
INSERT INTO grupos (id, nome, descricao) VALUES (1, 'ADMINISTRADOR', 'Administrador'), (2, 'USUARIO_AVANCADO','Usuário Avançado'), (3, 'USUARIO_COMUM','Usuário Comum'); 
INSERT INTO usuario_grupo (usuario_id, grupo_id) VALUES (1, 1);

INSERT INTO categoria_produtos (id, nome) VALUES (1, 'Porta'), (2, 'Janela'), (3, 'Fechadura');
INSERT INTO fornecedores (id, nome) VALUES (1, 'Acer'), (2, 'Samsung');

INSERT INTO tipos_vendas (id, descricao) VALUES (1, 'Olx'), (2, 'Face');

INSERT INTO bairros (id,nome,zona) VALUES (1,'Adrianápolis','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (2,'Aleixo','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (3,'Alvorada','CENTRO_OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (4,'Armando Mendes','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (5,'Betânia','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (6,'Cachoeirinha','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (7,'Centro','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (8,'Chapada','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (9,'Cidade de Deus','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (10,'Cidade Nova','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (11,'Colônia Antônio Aleixo','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (12,'Colônia Oliveira Machado','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (13,'Colônia Santo Antônio','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (14,'Colônia Terra Nova','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (15,'Compensa','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (16,'Coroado','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (17,'Crespo','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (18,'Da Paz','CENTRO_OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (19,'Distrito Industrial I','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (20,'Distrito Industrial II','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (21,'Dom Pedro','CENTRO_OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (22,'Educandos','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (23,'Flores','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (24,'Gilberto Mestrinho','LESTE'); 
INSERT INTO bairros (id,nome,zona) VALUES (25,'Glória','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (26,'Japiim','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (27,'Jorge Teixeira','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (28,'Lago Azul','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (29,'Lírio do Vale','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (30,'Mauazinho','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (31,'Monte das Oliveiras','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (32,'Morro da Liberdade','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (33,'Nossa Senhora Aparecida','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (34,'Nossa Senhora das Graças','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (35,'Nova Cidade','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (36,'Nova Esperança','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (37,'Novo Aleixo','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (38,'Novo Israel','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (39,'Parque 10 de Novembro','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (40,'Petrópolis','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (41,'Planalto','CENTRO_OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (42,'Ponta Negra','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (43,'Praça 14 de Janeiro','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (44,'Presidente Vargas','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (45,'Puraquequara','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (46,'Raiz','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (47,'Redençao','CENTRO_OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (48,'Santa Etelvina','NORTE');
INSERT INTO bairros (id,nome,zona) VALUES (49,'Santa Luzia','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (50,'Santo Agostinho','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (51,'Santo Antônio','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (52,'Sao Francisco','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (53,'Sao Geraldo','CENTRO_SUL');
INSERT INTO bairros (id,nome,zona) VALUES (54,'Sao Jorge','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (55,'Sao José Operário','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (56,'Sao Lázaro','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (57,'Sao Raimundo','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (58,'Tancredo Neves','LESTE');
INSERT INTO bairros (id,nome,zona) VALUES (59,'Taruma','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (60,'Taruma-Açu','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (61,'Vila Buriti','SUL');
INSERT INTO bairros (id,nome,zona) VALUES (62,'Vila da Prata','OESTE');
INSERT INTO bairros (id,nome,zona) VALUES (63,'Zumbi dos Palmares','LESTE');