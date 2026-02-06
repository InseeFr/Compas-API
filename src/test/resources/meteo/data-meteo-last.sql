INSERT INTO table_faits (id_module, id_indicateur, id_application,date, valeur, id_source)
VALUES
    (null, 401, 1, DATEADD('MONTH', -1, CURRENT_DATE), 3, 2),
    (null, 401, 2, DATEADD('MONTH', -2, CURRENT_DATE), 1, 2),
    (null, 401, 3, DATEADD('MONTH', -11, CURRENT_DATE), 4, 2);

