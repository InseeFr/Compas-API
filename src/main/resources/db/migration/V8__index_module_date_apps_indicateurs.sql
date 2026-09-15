DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_table_faits_module_indicateur_date_desc_covering'
    ) THEN
        CREATE INDEX idx_table_faits_module_indicateur_date_desc_covering
            ON table_faits (id_module, id_indicateur, date DESC)
            INCLUDE (valeur)
            WHERE id_module IS NOT NULL;
    END IF;

    -- 1. Pour GreenIT: date = :dateReference (egalite exacte)
    -- Cible: getGreenItAppVm, getGreenItAppKube, countGreenItValuesByDate
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_table_faits_app_date_eq_covering'
    ) THEN
        CREATE INDEX idx_table_faits_app_date_eq_covering
            ON table_faits (date, id_application, id_indicateur)
            INCLUDE (valeur)
            WHERE id_application IS NOT NULL AND id_module IS NULL;
    END IF;

    -- 2. Pour date <= :dateReference + PARTITION BY (application, indicateur) ORDER BY date DESC
    -- Cible: Qualite Application, Devops Application, Securite Application
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_table_faits_app_indicateur_date_desc_covering'
    ) THEN
        CREATE INDEX idx_table_faits_app_indicateur_date_desc_covering
            ON table_faits (id_application, id_indicateur, date DESC)
            INCLUDE (valeur, id)
            WHERE id_application IS NOT NULL AND id_module IS NULL;
    END IF;

    -- 3. Pour Maturite Cloud: PARTITION BY (application, indicateur) ORDER BY date DESC, id DESC
    -- Cible: findAllLatestMatRobAndScores
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_table_faits_app_indic_full_covering'
    ) THEN
        CREATE INDEX idx_table_faits_app_indic_full_covering
            ON table_faits (id_application, id_indicateur, date DESC, id DESC)
            INCLUDE (valeur)
            WHERE id_application IS NOT NULL AND id_module IS NULL;
    END IF;
END
$$;