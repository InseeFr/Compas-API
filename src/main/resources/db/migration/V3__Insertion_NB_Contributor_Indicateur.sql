INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (303,'Nombre de personnes ayant contribués à un projet depuis une date donnée','numeric','Brut')
    ON CONFLICT (id) DO NOTHING;