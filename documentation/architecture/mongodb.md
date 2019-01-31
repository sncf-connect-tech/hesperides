# MongoDB

_cf._ [lightweight-architecture-decision-records/database](../lightweight-architecture-decision-records/database.md) pour les détails du choix de base de donnée.

## Archi du cluster

Dans le cas d'Hesperides, nous recommendons l'utilisation d'un cluster de 2 noeuds Mongo en lecture/écriture sur 2 datacenters, plus un 3e servant d'arbitre en cas de _split brain_.

## Configuration du client

La configuration du client Mongo se fait via les variables d'environnement `EVENT_STORE_MONGO_URI` && `PROJECTION_REPOSITORY_MONGO_URI`
qui contiennent la liste des noeuds du cluster à utiliser et les options de connexion à employer.

## Write concern

_cf._ <https://docs.mongodb.com/manual/reference/write-concern/>

Nous recommendons l'emploi de ces paramètres de connexion :

    ?maxPoolSize=10&maxIdleTimeMS=2000&w=2&j=true&wtimeout=5000

## Consultation des options de connexion

Un _HealthIndicator_ Spring Boot expose ces informations dans un endpoint HTTP:

    curl -s http://localhost:8080/rest/manage/health
