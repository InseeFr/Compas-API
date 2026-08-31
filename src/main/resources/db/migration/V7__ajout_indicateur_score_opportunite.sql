INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (615,'score opportunite','numeric','Brut')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (616, 'delta maturite', 'numeric', 'Brut')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO indicateur (id, nom, type, brut_final)
VALUES (617, 'delta opportunite', 'numeric', 'Brut')
    ON CONFLICT (id) DO NOTHING;
