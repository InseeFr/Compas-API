INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (302,'nombre de déploiement en production depuis un mois','numeric','Brut')
    ON CONFLICT (id) DO NOTHING;