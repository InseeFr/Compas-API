INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (613,'env_cible_prod','lettre','Brut')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (614,'avancement_strategie_cloud','lettre','Brut')
    ON CONFLICT (id) DO NOTHING;
