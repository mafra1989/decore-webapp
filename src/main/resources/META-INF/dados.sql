INSERT INTO usuarios (id, nome, funcao, cpf, contato, foto, login, senha) VALUES (1,'Tácito','CEO','955.264.472.85','92 9-9999-9999',null,'tacito', '$2a$10$0yEvoqhIbxgkzMo7t9l35.nwGS53pM2zk1TLYD2MQkU7acTHx3K/.');
INSERT INTO grupos (id, nome, descricao) VALUES (1, 'ADMINISTRADOR', 'Administrador'), (2, 'USUARIO_AVANCADO','Usuário Avançado'), (3, 'USUARIO_COMUM','Usuário Comum'); 
INSERT INTO usuario_grupo (usuario_id, grupo_id) VALUES (1, 1);

INSERT INTO categoria_produtos (id, nome) VALUES (1, 'Porta'), (2, 'Janela'), (3, 'Fechadura');
INSERT INTO fornecedores (id, nome) VALUES (1, 'Acer'), (2, 'Samsung');

INSERT INTO tipos_vendas (id, descricao) VALUES (1, 'Olx'), (2, 'Face');

COPY bairros (id, nome, zona) FROM stdin;
1	Adrianápolis	CENTRO_SUL
2	Aleixo	CENTRO_SUL
3	Alvorada	CENTRO_OESTE
4	Armando Mendes	LESTE
5	Betânia	SUL
6	Cachoeirinha	SUL
7	Centro	SUL
8	Chapada	CENTRO_SUL
9	Cidade de Deus	NORTE
10	Cidade Nova	NORTE
11	Colônia Antônio Aleixo	LESTE
12	Colônia Oliveira Machado	SUL
13	Colônia Santo Antônio	NORTE
14	Colônia Terra Nova	NORTE
15	Compensa	OESTE
16	Coroado	LESTE
17	Crespo	SUL
18	Da Paz	CENTRO_OESTE
19	Distrito Industrial I	SUL
20	Distrito Industrial II	LESTE
21	Dom Pedro	CENTRO_OESTE
22	Educandos	SUL
23	Flores	CENTRO_SUL
24	Gilberto Mestrinho	LESTE
25	Glória	OESTE
26	Japiim	SUL
27	Jorge Teixeira	LESTE
28	Lago Azul	NORTE
29	Lírio do Vale	OESTE
30	Mauazinho	LESTE
31	Monte das Oliveiras	NORTE
32	Morro da Liberdade	SUL
33	Nossa Senhora Aparecida	SUL
34	Nossa Senhora das Graças	CENTRO_SUL
35	Nova Cidade	NORTE
36	Nova Esperança	OESTE
37	Novo Aleixo	NORTE
38	Novo Israel	NORTE
39	Parque 10 de Novembro	CENTRO_SUL
40	Petrópolis	SUL
41	Planalto	CENTRO_OESTE
42	Ponta Negra	OESTE
43	Praça 14 de Janeiro	SUL
44	Presidente Vargas	SUL
45	Puraquequara	LESTE
46	Raiz	SUL
47	Redençao	CENTRO_OESTE
48	Santa Etelvina	NORTE
49	Santa Luzia	SUL
50	Santo Agostinho	OESTE
51	Santo Antônio	OESTE
52	Sao Francisco	SUL
53	Sao Geraldo	CENTRO_SUL
54	Sao Jorge	OESTE
55	Sao José Operário	LESTE
56	Sao Lázaro	SUL
57	Sao Raimundo	OESTE
58	Tancredo Neves	LESTE
59	Taruma	OESTE
60	Taruma-Açu	OESTE
61	Vila Buriti	SUL
62	Vila da Prata	OESTE
63	Zumbi dos Palmares	LESTE
3008	Não Informado	NORTE
\.
