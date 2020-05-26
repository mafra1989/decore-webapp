INSERT INTO usuarios(id,contato,cpf,funcao,login,nome,senha,urlimagem) VALUES (32,'(92) 9-9470-0202','873.420.062-20','Analista de Vendas','Tiago','Tiago da Silva Dantas','$2a$10$3qx8zRtYRtThYCE7GyPRzOPGDNV8TU5cy7C3lNK24AGbCbUmTAlQC',NULL);
INSERT INTO usuarios(id,contato,cpf,funcao,login,nome,senha,urlimagem) VALUES (1,'(92) 9-9427-9086','739.187.842-15','CEO','Tacito','Tácito Alfredo Dantas Neto','$2a$10$hjcMMhXTprbEcMfzX98J/.wsRexGLfQSq6meH.AgvirhLg5nIufaS','https://i.imgur.com/wRWBDZa.jpg');
INSERT INTO usuarios(id,contato,cpf,funcao,login,nome,senha,urlimagem) VALUES (37569,'(92) 9-9608-2896','644.828.982-87','Analista de Vendas','Nilton','Nilton César da Silva Dantas','$2a$10$yp6XFpu59Ba1mTNWArebBu4Dskq5PsoDhAimyj350V3dIG2JuYG/K',NULL);
INSERT INTO usuarios(id,contato,cpf,funcao,login,nome,senha,urlimagem) VALUES (37570,'(92) 9-9436-2926','935.285.312-15','Analista de Vendas','Risonildo','Risonildo da Silva Dantas','$2a$10$9k1zRolkRIn7HBom1NGbeubjMCGCcfyPkXjP6vWlihY0HLRxTKkGC',NULL);

INSERT INTO grupos (id, nome, descricao) VALUES (1, 'ADMINISTRADOR', 'Administrador'), (2, 'USUARIO_AVANCADO','Usuário Avançado'), (3, 'USUARIO_COMUM','Usuário Comum'); 

INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES (32,3);
INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES (1,1);
INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES (37569,3);
INSERT INTO usuario_grupo(usuario_id,grupo_id) VALUES (37570,3);

INSERT INTO categoria_produtos(id,nome) VALUES (1,'Porta');
INSERT INTO categoria_produtos(id,nome) VALUES (3,'Fechadura');
INSERT INTO categoria_produtos(id,nome) VALUES (2,'Aduelas');
INSERT INTO categoria_produtos(id,nome) VALUES (4,'Molduras');
INSERT INTO categoria_produtos(id,nome) VALUES (6,'Espuma');
INSERT INTO categoria_produtos(id,nome) VALUES (8,'Trilho');
INSERT INTO categoria_produtos(id,nome) VALUES (10,'Puxador');
INSERT INTO categoria_produtos(id,nome) VALUES (5,'Dobradiças');
INSERT INTO categoria_produtos(id,nome) VALUES (7,'Kit Padrão');
INSERT INTO categoria_produtos(id,nome) VALUES (153,'Batedor');
INSERT INTO categoria_produtos(id,nome) VALUES (154,'Vista');
INSERT INTO categoria_produtos(id,nome) VALUES (9,'Roudana');

INSERT INTO fornecedores(id,contato,nome) VALUES (1,'(92) 9-9252-5903','Madeforming');
INSERT INTO fornecedores(id,contato,nome) VALUES (2,'(22) 9-9229-7261','Stam');
INSERT INTO fornecedores(id,contato,nome) VALUES (11,NULL,'Marcelo');
INSERT INTO fornecedores(id,contato,nome) VALUES (12,NULL,'Avelino');

INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (161,76003,'76003 - DOBRADIÇAS COM ROLAMENTO INOX FOSCO','Loja',100,'DOBRADIÇAS',0,5,12,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (162,76008,'76008 - DOBRADIÇAS SEM ROLAMENTO INOX POLIDO','Loja',100,'DOBRADIÇAS',6,5,12,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (159,76013,'76013 - DOBRADIÇAS COM ROLAMENTO INOX','Loja',100,'DOBRADIÇAS',12,5,12,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (98,36758,'36758 - FECHADURA 803/21 - ROSETA QUADRADA INOX (G','Loja',42,'EXTERNA INOX',0,3,2,'https://i.imgur.com/QZluxul.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (100,11115,'11115 - FECHADURA 1820/10 - ESP. OXIDADO','Loja',80,'BANHEIRO OXIDADA',1,3,2,'https://i.imgur.com/nhHqaZC.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (96,11352,'11352 - FECHADURA 1820/21 - ESP. OXIDADO','Loja',80,'BANHEIRO OXIDADA',3,3,2,'https://i.imgur.com/EMylK3H.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (122,784,'784 - PORTA BRANCA FRISO 04 210X60','Loja',36,'PORTA DE 60',5,1,1,'https://i.imgur.com/P7Jnv4i.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (103,34950,'34950 - FECHADURA 803/21 ROSETA OXIDADO E COAT (G)','Loja',40,'EXTERNA OXIDADA',36,3,2,'https://i.imgur.com/dW9ANzQ.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (120,819,'819 - PORTA MOGNO LISA 210X60','Loja',36,'PORTA DE 60',3,1,1,'https://i.imgur.com/khitNVg.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (88,40481,'40481 - FECHADURA 824/33 - ESP. OXIDADO','Loja',40,'BANHEIRO OXIDADA',0,3,2,'https://i.imgur.com/VNJxST9.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (91,40467,'40467 - FECHADURA 804/33 - ESP. INOX (G)','Loja',60,'EXTERNA INOX',199,3,2,'https://i.imgur.com/RHKivNC.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (87,40469,'40469 - FECHADURA 804/33 - ESP. OXIDADO (G)','Loja',40,'EXTERNA OXIDADA',0,3,2,'https://i.imgur.com/xijEjUX.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (115,783,'783 - PORTA BRANCA FRISO 03 210X90','Loja',36,'PORTA DE 90',5,1,1,'https://i.imgur.com/LD5cmea.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (76,36772,'36772 - FECHADURA 823/21 ROSETA QUADRADA BANHEIRO OXIDADO','Loja',50,'BANHEIRO OXIDADA',1,3,2,'https://i.imgur.com/aCVQSEt.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (90,40443,'40443 - FECHADURA 823/33 - ESP. INOX','Loja',95,'BANHEIRO INOX',0,3,2,'https://i.imgur.com/zsO1pJ8.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (118,785,'785 - PORTA BRANCA FRISO 04 210X70','Loja',36,'PORTA DE 70',1,1,1,'https://i.imgur.com/5hk2DHG.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (104,35166,'35166 - FECHADURA 823/21 ROSETA OXIDADO E COAT','Loja',70,'BANHEIRO INOX',9,3,2,'https://i.imgur.com/J7ZBLJJ.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (92,40480,'40480 - FECHADURA 824/33 - ESP. INOX','Loja',95,'BANHEIRO INOX',1,3,2,'https://i.imgur.com/B3cJ2Xi.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (102,11491,'11491 - FECHADURA 823/21 ROSETA ESP INOX','Loja',100,'BANHEIRO INOX',10,3,2,'https://i.imgur.com/9W6QYO3.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (85,35111,'35111 - FECHADURA 803/21 - ESP. OXIDADO (G)','Loja',40,'EXTERNA OXIDADA',0,3,2,'https://i.imgur.com/UFVdj0L.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (121,787,'787 - PORTA BRANCA FRISO 04 210X90','Loja',36,'PORTA DE 90',0,1,1,'https://i.imgur.com/chA2qAD.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (99,35093,'35093 - FECHADURA 1801/10 - ESP. OXIDADO (G)','Loja',60,'EXTERNA OXIDADA',10,3,2,'https://i.imgur.com/mzghRCv.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (119,789,'789 - PORTA BRANCA FRISO 05 210X70','Loja',36,'PORTA DE 70',4,1,1,'https://i.imgur.com/dsHWrjM.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (116,780,'780 - PORTA BRANCA FRISO 03 210X60','Loja',36,'PORTA DE 60',7,1,1,'https://i.imgur.com/2F67ohc.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (152,226,'226 - OGO DE MOLDURA BRANCO','Loja',100,'MOLDURA BRANCO',3,4,1,'https://i.imgur.com/oNg85TQ.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (156,12059,'12059 - ESPUMA EXPANSIVA SPRAY','Loja',100,'ESPUMA',21,6,12,'https://i.imgur.com/SkazMa8.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (146,223,'223 - JOGO DE ADUELA MOGNO','Loja',35,'ADUELA MOGNO',4,2,1,'https://i.imgur.com/5koqW8y.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (135,803,'803-PORTA MOGNO FRISO 02 210X60','Loja',36,'PORTA DE 60',7,1,1,'https://i.imgur.com/McK5F3G.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (145,200,'200 - ADUELAS PERSONALIZADAS','Loja',100,'ADUELAS PERSONALIZADAS',0,2,11,'https://i.imgur.com/Fxmxa8o.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (150,50,'50 - MOLDURA DE 10cm','Loja',100,'MOLDURA DE 10cm',0,4,11,'https://i.imgur.com/XLw8qzU.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (128,814,'814 - PORTA MOGNO FRISO 04 210X90','Loja',36,'PORTA DE 90',1,1,1,'https://i.imgur.com/HQxfEEE.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (132,833,'833 - PORTA BRANCA FRISO 08 210X80','Loja',36,'PORTA DE 80',5,1,1,'https://i.imgur.com/55ZtbNi.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (155,700,'700 - BATEDOR PORTA DE CORRER','Loja',100,'BATEDOR',18,153,11,'https://i.imgur.com/57xc0a7.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (133,776,'776 - PORTA BRANCA FRISO 02 210X60','Loja',36,'PORTA DE 60',4,1,1,'https://i.imgur.com/9XLrJVt.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (127,788,'788 - PORTA BRANCA FRISO 05 210X60','Loja',36,'PORTA DE 60',4,1,1,'https://i.imgur.com/FOLQ5TN.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (140,844,'844 - PORTA MOGNO FRISO 08 210X70','Loja',36,'PORTA DE 70',7,1,1,'https://i.imgur.com/edku7wz.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (166,900,'900 - VISTA PORTA DE CORRER MOGNO','Loja',100,'VISTA',11,154,12,'https://i.imgur.com/CRgn7LO.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (75,40391,'40391 - FECHADURA 803/33 GORJE EXTERNA OXIDADO','Loja',50,'EXTERNA OXIDADA',0,3,2,'https://i.imgur.com/yI0mXUL.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (137,832,'832 - PORTA BRANCA FRISO 08 210X70','Loja',36,'PORTA DE 70',6,1,1,'https://i.imgur.com/mjihY4r.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (134,816,'816 - PORTA MOGNO FRISO 05 210X70','Loja',36,'PORTA DE 70',7,1,1,'https://i.imgur.com/P1emPBN.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (158,400,'400 - TRILHO POR DA CORRER','Loja',100,'TRILHO',1,8,12,'https://i.imgur.com/5ll09TF.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (147,224,'224 - JOGO DE ADUELA BRANCO','Loja',35,'ADUELA BRANCO',3,2,1,'https://i.imgur.com/N6m4WrN.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (149,300,'300 - MOLDURA DE 7cm','Loja',100,'MOLDURA DE 7cm',41,4,11,'https://i.imgur.com/z0MECKs.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (151,225,'225 - JOGO DE MOLDURA MOGNO','Loja',100,'MOLDURA MOGNO',4,4,1,'https://i.imgur.com/z2RX7b0.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (125,793,'793 - PORTA BRANCA LISA 210X70','Loja',36,'PORTA DE 70',4,1,1,'https://i.imgur.com/cMXSetz.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (143,843,'843 - PORTA MOGNO FRISO 08 210X60','Loja',36,'PORTA DE 60',3,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (126,792,'792 - PORTA BRANCA LISA 210X60','Loja',36,'PORTA DE 60',5,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (130,822,'822 - PORTA MOGNO LISA 210X90','Loja',36,'PORTA DE 90',0,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (142,846,'846 - PORTA MOGNO FRISO 08 210X90','Loja',36,'PORTA DE 90',1,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (164,818,'818 - PORTA MOGNO FRISO 05 210X90','Loja',36,'PORTA DE 90',1,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (136,806,'806 - PORTA MOGNO FRISO 02 210X90','Loja',36,'PORTA DE 90',2,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (139,845,'845 - PORTA MOGNO FRISO 08 210X80','Loja',36,'PORTA DE 80',9,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (157,600,'600 - PUXADOR PORTA DE CORRER','Loja',100,'PUXADOR',7,10,12,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (141,815,'815 - PORTA MOGNO FRISO 05 210X60','Loja',36,'PORTA DE 60',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (144,834,'834 - PORTA BRANCA FRISO 08 210X90','Loja',36,'PORTA DE 90',2,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (131,795,'795 - PORTA BRANCA LISA 210X90','Loja',36,'PORTA DE 90',2,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (124,794,'794 - PORTA BRANCA LISA 210X80','Loja',36,'PORTA DE 80',6,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (13,805,'805 - PORTA MOGNO FRISO 02 210X80','Loja',36,'PORTA DE 80',14,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (110,777,'777 - PORTA BRANCA FRISO 02 210X70','Loja',36,'PORTA DE 70',5,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (64,778,'778 - PORTA BRANCA FRISO 02 210X80','LOJA',36,'PORTA DE 80',10,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (106,817,'817 - PORTA MOGNO FRISO 05 210X80','Loja',36,'PORTA DE 80',6,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (108,812,'812 - PORTA MOGNO FRISO 04 210X70','Loja',36,'PORTA DE 70',9,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (112,810,'810 - PORTA MOGNO FRISO 03 210X90','Loja',36,'PORTA DE 90',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (63,808,'808 - PORTA MOGNO FRISO 03 210X70','LOJA',36,'PORTA DE 70',18,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (107,804,'804 - PORTA MOGNO FRISO 02 210X70','Loja',36,'PORTA DE 70',7,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (65,782,'782 - PORTA BRANCA FRISO 03 210X80','LOJA',36,'PORTA DE 80',19,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (14,809,'809 - PORTA MOGNO FRISO 03 210X80','Loja',36,'PORTA DE 80',9,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (111,821,'821 - PORTA MOGNO LISA 210X80','Loja',36,'PORTA DE 80',11,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (160,8556,'8556 - DOBRADIÇAS SILVANA','Loja',100,'DOBRADIÇAS',75,5,12,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (105,786,'786 - PORTA BRANCA FRISO 04 210X80','Loja',36,'PORTA DE 80',1,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (109,781,'781 - PORTA BRANCA FRISO 03 210X70','Loja',36,'PORTA DE 70',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (62,813,'813 - PORTA MOGNO FRISO 04 210X80','LOJA',36,'PORTA DE 80',14,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (117,790,'790 - PORTA BRANCA FRISO 05 210X80','Loja',36,'PORTA DE 80',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (114,807,'807 - PORTA MOGNO FRISO 03 210X60','Loja',36,'PORTA DE 60',12,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (113,820,'820 - PORTA MOGNO LISA 210X70','Loja',36,'PORTA DE 70',10,1,1,'https://i.imgur.com/xLXLVUk.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (123,800,'800 - PORTA MOGNO FRISO 01 210X70','Loja',36,'PORTA DE 70',0,1,1,'https://i.imgur.com/4clD5fp.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (129,811,'811 - PORTA MOGNO FRISO 04 210X60','Loja',36,'PORTA DE 60',7,1,1,'https://i.imgur.com/UOkFi5b.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (138,831,'831 - PORTA BRANCA FRISO 08 210X60','Loja',36,'PORTA DE 60',8,1,1,'https://i.imgur.com/xcFAcXd.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (19,103,'103 - DOBRADIÇAS','Loja',100,'DOBRADIÇAS',0,5,12,'https://i.imgur.com/qDRxn1p.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (163,1653,'1653 - DOBRADIÇAS COLONIAL','Loja',100,'DOBRADIÇAS',24,5,12,'https://i.imgur.com/Z7R5Mi3.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (77,36771,'36771 - FECHADURA 823/21 ROSETA QUADRADA BANHEIRO INOX','Loja',50,'BANHEIRO INOX',0,3,2,'https://i.imgur.com/QSOQnu7.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (82,11350,'11350 - FECHADURA 1820/21 BANHEIRO INOX','Loja',75,'BANHEIRO INOX',8,3,2,'https://i.imgur.com/oG9zqeE.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (84,11349,'11349 - FECHADURA 1820/21 BANHEIRO ANTIQUE','Loja',60,'BANHEIRO ANTIQUE',6,3,2,'https://i.imgur.com/OT0eeSV.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (97,35040,'35040 - FECHADURA 940 - ESP. INOX (G)','Loja',100,'EXTERNA INOX',11,3,2,'https://i.imgur.com/iRRfOfc.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (16,40389,'40389 - FECHADURA 803/33 GORJE EXTERNA INOX','loja',60,'EXTERNA INOX',0,3,2,'https://i.imgur.com/DenzlA4.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (83,35100,'35100 - FECHADURA 1801/21 GORJE EXTERNA ANTIQUE','Loja',40,'EXTERNA ANTIQUE',0,3,2,'https://i.imgur.com/288YtOg.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (81,35274,'35274 - FECHADURA 1821/01 GORJE EXTERNA INOX','Loja',50,'EXTERNA INOX',59,3,2,'https://i.imgur.com/SIQLhix.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (80,10356,'10356 - FECHADURA POP LINE SOPRANO','Loja',60,'EXTERNA INOX',9,3,12,'https://i.imgur.com/uCfjQpO.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (101,34948,'34948 - FECHADURA 803/21 ROSETA INOX(G)','Loja',80,'EXTERNA INOX',46,3,2,'https://i.imgur.com/IdKmdij.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (95,35086,'35086 - FECHADURA 1801/21 - ESP. OXIDADO (G)','Loja',60,'EXTERNA OXIDADA',0,3,2,'https://i.imgur.com/tNfRVp4.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (89,40444,'40444 - FECHADURA 823/33 - ESP. OXIDADO','Loja',40,'BANHEIRO OXIDADA',0,3,2,'https://i.imgur.com/jLwjP5l.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (93,35038,'35038 - FECHADURA 804/11 - ESP. INOX (G)','Loja',30,'EXTERNA INOX',0,3,2,'https://i.imgur.com/1p3P9ww.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (86,11494,'11494 - FECHADURA 823/21 - ESP. OXIDADO','Loja',50,'BANHEIRO OXIDADA',0,3,2,'https://i.imgur.com/0N6LKn3.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (94,35039,'35039 - FECHADURA 804/11 - ESP. OXIDADO (G)','Loja',30,'EXTERNA OXIDADA',1,3,2,'https://i.imgur.com/mmBy4Kr.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (18,102,'102 - MOLDURA PADRÃO 5CM','Loja',100,'MOLDURA PADRÃO',222,4,11,'https://i.imgur.com/5Mn3RFg.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (165,500,'500 - ROUDANA','Loja',100,'ROUDANA',0,9,12,'https://i.imgur.com/ySb9Bdp.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (17,101,'101 - ADUELA PADRÃO 13CM','Loja',100,'ADUELAS',76,2,11,'https://i.imgur.com/UpR2zZc.jpg');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (78,38403,'38403 - FECHADURA 803/21 ROSETA QUADRADA 1 GORJE EXTERNA INOX','Loja',50,'EXTERNA INOX',0,3,2,'https://i.imgur.com/iGSctn0.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (79,36760,'36760 - FECHADURA 803/21 ROSETA QUADRADA GORJE EXTERNA OXIDADO','Loja',40,'EXTERNA OXIDADA',37,3,2,'https://i.imgur.com/lhzt5FF.png');
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (15,36806,'36806 - FECHADURA 803/25 ROSETA QUADRADA GORJE EXTERNA INOX','Loja',60,'EXTERNA INOX',0,3,2,'https://i.imgur.com/gGh8RI1.png');

INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (165,500,'850','Loja',20,'PORTAS',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (17,101,'1000','Loja',20,'PORTAS',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (78,38403,'1420','Loja',20,'PORTAS',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (79,36760,'1421','Loja',20,'PORTAS',4,1,1,NULL);
INSERT INTO produtos(id,codigo,descricao,locacao,margemlucro,nome,quantidadeatual,categoriaproduto_id,fornecedor_id,urlimagem) VALUES (15,36806,'1422','Loja',20,'ADUELAS',0,2,11,null);


INSERT INTO bairros(id,nome,zona) VALUES (1,'Adrianápolis','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (2,'Aleixo','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (3,'Alvorada','CENTRO_OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (4,'Armando Mendes','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (5,'Betânia','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (6,'Cachoeirinha','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (7,'Centro','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (8,'Chapada','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (9,'Cidade de Deus','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (10,'Cidade Nova','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (11,'Colônia Antônio Aleixo','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (12,'Colônia Oliveira Machado','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (13,'Colônia Santo Antônio','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (14,'Colônia Terra Nova','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (15,'Compensa','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (16,'Coroado','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (17,'Crespo','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (18,'Da Paz','CENTRO_OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (19,'Distrito Industrial I','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (20,'Distrito Industrial II','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (21,'Dom Pedro','CENTRO_OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (22,'Educandos','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (23,'Flores','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (24,'Gilberto Mestrinho','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (25,'Glória','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (26,'Japiim','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (27,'Jorge Teixeira','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (28,'Lago Azul','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (29,'Lírio do Vale','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (30,'Mauazinho','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (31,'Monte das Oliveiras','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (32,'Morro da Liberdade','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (33,'Nossa Senhora Aparecida','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (34,'Nossa Senhora das Graças','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (35,'Nova Cidade','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (36,'Nova Esperança','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (37,'Novo Aleixo','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (38,'Novo Israel','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (39,'Parque 10 de Novembro','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (40,'Petrópolis','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (41,'Planalto','CENTRO_OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (42,'Ponta Negra','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (43,'Praça 14 de Janeiro','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (44,'Presidente Vargas','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (45,'Puraquequara','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (46,'Raiz','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (47,'Redençao','CENTRO_OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (48,'Santa Etelvina','NORTE');
INSERT INTO bairros(id,nome,zona) VALUES (49,'Santa Luzia','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (50,'Santo Agostinho','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (51,'Santo Antônio','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (52,'Sao Francisco','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (53,'Sao Geraldo','CENTRO_SUL');
INSERT INTO bairros(id,nome,zona) VALUES (54,'Sao Jorge','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (55,'Sao José Operário','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (56,'Sao Lázaro','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (57,'Sao Raimundo','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (58,'Tancredo Neves','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (59,'Taruma','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (60,'Taruma-Açu','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (61,'Vila Buriti','SUL');
INSERT INTO bairros(id,nome,zona) VALUES (62,'Vila da Prata','OESTE');
INSERT INTO bairros(id,nome,zona) VALUES (63,'Zumbi dos Palmares','LESTE');
INSERT INTO bairros(id,nome,zona) VALUES (3008,'Não Informado','NORTE');

INSERT INTO tipos_vendas(id,descricao) VALUES (1,'Olx');
INSERT INTO tipos_vendas(id,descricao) VALUES (2,'Face');
INSERT INTO tipos_vendas(id,descricao) VALUES (33,'Loja');
INSERT INTO tipos_vendas(id,descricao) VALUES (34,'Cliente');
INSERT INTO tipos_vendas(id,descricao) VALUES (35,'Indicação');
INSERT INTO tipos_vendas(id,descricao) VALUES (36,'Instagran');
INSERT INTO tipos_vendas(id,descricao) VALUES (37,'Site');
INSERT INTO tipos_vendas(id,descricao) VALUES (3009,'Não Informado');

INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (42,'Receitas',1);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (40,'Custos',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (38,'Despesa fixa',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (39,'Despesa variável',0);
INSERT INTO tipos_lancamentos(id,descricao,origem) VALUES (41,'Investimentos',0);

INSERT INTO destino_lancamentos(id,descricao) VALUES (43,'Empresa');
INSERT INTO destino_lancamentos(id,descricao) VALUES (45,'Nilton Dantas');
INSERT INTO destino_lancamentos(id,descricao) VALUES (44,'Tiago Dantas');
INSERT INTO destino_lancamentos(id,descricao) VALUES (46,'Risonildo Dantas');
INSERT INTO destino_lancamentos(id,descricao) VALUES (47,'Tácito Dantas');
INSERT INTO destino_lancamentos(id,descricao) VALUES (35913,'Adriana');

INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (176,'Água',43,38);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (177,'Luz',43,38);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (37565,'Aporte',47,42);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (180,'Aluguel',43,38);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (183,'Manutenção de Veículos',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (186,'Manutenção de Equipamentos',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (185,'Despesas com alimentação e mantimentos',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (190,'Aquisição de bens e equipamentos',43,40);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25832,'Avarias',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25833,'Combustível',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25834,'Custos e viagens a negócios',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25836,'Despesas com saúde',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25837,'Empréstimos',43,41);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25838,'Frete de veículos',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25839,'Impostos',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25840,'Limpeza e serviços gerais',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25841,'Mão de obra com entregas',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25842,'Mão de obra em aduelas e alisares',43,40);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25843,'Material e serviço de construção',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25844,'Material indireto',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25845,'Material para acabamento em aduelas e alisares',43,40);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25846,'Material para serviços em MDF',43,40);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25847,'Material para acabamento em portas',43,40);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25848,'Material para serviços em vidro',43,40);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25849,'Material para uso direto',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25850,'Outras receitas',43,42);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25851,'Propaganda e marketing',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25853,'Segurança',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25854,'Serviço de manutenção em geral',43,39);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (178,'Telefone, internet e informática',43,38);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25855,'Transporte',43,38);
INSERT INTO categoria_lancamentos(id,nome,destinolancamento_id,tipolancamento_id) VALUES (25835,'Salários  / Décimo / Férias / Bônus / PLR',43,40);
