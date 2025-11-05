DELETE FROM table_faits;

INSERT INTO table_faits (id_application, id_indicateur, id_source, date, valeur, commentaire)
VALUES (20, 401, 2, DATE '2025-01-20', 7, 'plus ancien id');

INSERT INTO table_faits (id_application, id_indicateur, id_source, date, valeur, commentaire)
VALUES (20, 401, 2, DATE '2025-01-20', 8, 'plus récent id');

INSERT INTO table_faits (id_application, id_indicateur, id_source, date, valeur, commentaire)
VALUES (21, 401, 2, TIMESTAMP '2025-01-01 10:15:00', 9, 'timestamp');

