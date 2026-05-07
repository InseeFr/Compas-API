INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (209, 'RAM consommée', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (210, 'temps Cpu', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (219, 'RAM consommée PD', 'numeric', 'Brut')  ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (220, 'temps Cpu PD', 'numeric', 'Brut')  ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (221, 'S3 consommé', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (222, 'Pvc consommé', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (223, 'Nombre de Pods maxi', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (224, 'S3 consommé PD', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (225, 'Pvc consommé PD', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (226, 'nb Pod maxi PD', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (227, 'Applishare consommé', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (228, 'Applishare consommé PD', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (229, 'Applishare alloué', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;
INSERT INTO INDICATEUR (id, nom, type, brut_final)
VALUES (230, 'Applishare alloué PD', 'numeric', 'Brut') ON CONFLICT (id) DO NOTHING;