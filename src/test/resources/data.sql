INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (1, 'nombre de lignes', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (2, 'nombre de lignes testées', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (201, 'RAM allouée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (202, 'RAM maxi', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (203, 'Disque alloué', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (204, 'Disque consommé', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (205, 'Cpu allouée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (206, 'Cpu maxi', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (207, 'Conso', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (208, 'Nombre de Vm', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (209, 'RAM consommée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (210, 'temps Cpu', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (211, 'RAM allouée PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (212, 'RAM maxi PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (213, 'Disque alloué PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (214, 'Disque consommé PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (215, 'Cpu allouée PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (216, 'Cpu maxi PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (217, 'Conso PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (218, 'Nombre de Vm PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (219, 'RAM consommée PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (220, 'temps Cpu PD', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (301, 'Nombre de jours depuis le dernier déploiement en production', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (302, 'Nombre de déploiement en production depuis une date donnée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (303, 'Nombre de personnes ayant contribués à un projet depuis une date donnée', 'numeric', 'Brut');
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (401, 'Météo du dev (ressenti général)', 'numeric', 'Brut');


INSERT INTO module_oscar (id_module, actif)
VALUES (244, true);
INSERT INTO module_oscar (id_module, actif)
VALUES (243, true);

INSERT INTO source (id, libelle)
VALUES (0, 'Sonar');
INSERT INTO source (id, libelle)
VALUES (1, 'Oscar');
INSERT INTO source (id, libelle)
VALUES (2, 'Saisie manuelle');
INSERT INTO source (id, libelle)
VALUES (101, 'VmWare-fichiers');
INSERT INTO source (id, libelle)
VALUES (102, 'Kube-fichiers');

INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (244, 201, 130, '2024-12-04', 8, 1);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (244, 201, 130, '2024-12-02', 8, 1);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (243, 201, 130, '2024-12-04', 8, 1);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (248, 201, 131, '2024-12-04', 8, 1);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (248, 1, 131, '2024-12-02', 87736, 0);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (266, 1, 137, '2024-12-02', 572751, 0);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (483, 1, 196, '2024-12-02', 2242, 0);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (102, 1, 22, '2024-12-02', 19761, 0);

INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (1, 2, 1, '2024-12-02', 102, 0);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (2, 2, 1, '2024-12-02', 197, 0);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (2, 2, 1, '2024-12-02', 198, 0);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (null, 219, 201, '2024-12-04', 8, 2);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (null, 220, 201, '2024-12-04', 510, 2);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (null, 209, 201, '2024-12-04', 8, 2);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (null, 210, 201, '2024-12-04', 510, 2);
INSERT INTO table_faits (id_module, id_indicateur, id_application, date, valeur, id_source)
VALUES (null, 207, 201, '2024-12-04', 510, 2);


