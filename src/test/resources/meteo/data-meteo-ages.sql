DELETE FROM table_faits;

INSERT INTO table_faits (id_application, id_indicateur, id_source, date, valeur, commentaire)
VALUES (10, 401, 2, DATE '2025-01-23', 3, 'seuil 23j');

INSERT INTO table_faits (id_application, id_indicateur, id_source, date, valeur, commentaire)
VALUES (11, 401, 2, DATE '2025-01-24', 2, '22j');

INSERT INTO table_faits (id_application, id_indicateur, id_source, date, valeur, commentaire)
VALUES (12, 401, 2, DATE '2024-12-31', 5, 'vieux');
