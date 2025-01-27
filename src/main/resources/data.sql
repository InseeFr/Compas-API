INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (1,'nombre de lignes','numeric','Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (2,'nombre de lignes testées','numeric','Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (201,'RAM allouée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (202,'RAM maxi', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (203,'Disque alloué', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (204,'Disque consommé', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (205,'Cpu allouée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (206,'Cpu maxi', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (207,'Conso', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (208,'Nombre de Vm', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id,nom,type,brut_final) VALUES (401,'Météo du dev (ressenti général)', 'numeric', 'Brut');
	 
INSERT INTO module_oscar (id_module,actif) VALUES (244,true);
INSERT INTO module_oscar (id_module,actif) VALUES (243,true);

INSERT INTO source (id,libelle) VALUES (0,'Sonar');
INSERT INTO source (id,libelle) VALUES (1,'Oscar');
INSERT INTO source (id,libelle) VALUES (2,'Saisie manuelle');
INSERT INTO source (id,libelle) VALUES (101,'VmWare-fichiers');

	 
INSERT INTO table_faits (id_module, id_indicateur, id_application,date, valeur, id_source) VALUES(244, 201, 130, '2024-12-04', 8, 1);
INSERT INTO table_faits (id_module,id_indicateur, id_application,date,valeur,id_source) VALUES (248,1, 131,'2024-12-02',87736,0);
INSERT INTO table_faits (id_module,id_indicateur, id_application,date,valeur,id_source) VALUES (266,1,137,'2024-12-02',572751,0);
INSERT INTO table_faits (id_module,id_indicateur, id_application,date,valeur,id_source) VALUES (483,1,196,'2024-12-02',2242,0);
INSERT INTO table_faits (id_module,id_indicateur, id_application,date,valeur,id_source) VALUES (102,1,22,'2024-12-02',19761,0);
