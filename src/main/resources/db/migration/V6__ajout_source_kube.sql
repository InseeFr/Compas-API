INSERT INTO SOURCE (id, libelle)
VALUES (102, 'Kube-fichiers') ON CONFLICT (id) DO NOTHING;
