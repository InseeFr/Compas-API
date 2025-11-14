# Compas API

## Installation en local

Le projet est fait pour démarrer avec le javaagent Agent-Secret.   
Il faut être développeur du quartier ut pour accéder au secret du projet.
En local si on veut démarrer sur une base PG il faut ajouter la variable d'environnement **COMPAS_SPRING_DATASOURCE_URL** valant par exemple **jdbc:postgresql://dvcompaslg001.ad.insee.intra:1983/di_pg_compas_dv01?currentSchema=compas_dev1** 


## Formatage du code

Utilisation de [Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven)

- plugin Maven qui s'assure que le code respecte des règles de formatage et fait échouer la compilation sinon
- possibilité de vérifier si les règles sont respectées avec la commande `mvn spotless:check`
- possibilité d'appliquer les règles de formatage avec la commande `mvn spotless:apply`

## Rafraichissement schema dv par prod 

Dans ut-pd-rundeck.insee.fr, dans les jobs COMMUN, lancer le job sauvegarde_schema_api en renseignant :
- bdd : pdcompaslg001 
- schema : compas 

Dans ut-hp-rundeck.insee.fr, dans les jobs COMMUN, lancer le job sauvegarde_schema_api en renseignant :
- bdd : dvcompaslg001
- empalcement_dump : récupérer le lien du job précédent Attention ajouter prod dans archive-full dans le nom récupérer
- schema : compas
- schema_renomme : là ou on veut copier le dump 
- stop_cluster : true coupe les connexions ce qui n'est pas forcément nécessaire mais peut etre utile
