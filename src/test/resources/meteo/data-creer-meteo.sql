DELETE FROM table_faits;

INSERT INTO table_faits (
    id,
    id_module,
    id_indicateur,
    valeur,
    id_application,
    commentaire,
    date
) VALUES
      (6L,null,401, 3, 7, 'comm1',DATE '2026-01-12');