# MongoDB

_cf._ [lightweight-architecture-decision-records/database](../lightweight-architecture-decision-records/database.md) pour les détails du choix de base de donnée.

## Archi du cluster

Dans le cas d'Hesperides, nous recommendons l'utilisation d'un cluster de 2 noeuds Mongo en lecture/écriture sur 2 datacenters, plus un 3e servant d'arbitre en cas de _split brain_.

## Configuration du client

La configuration du client Mongo se fait via la variable d'environnement `MONGO_URI` qui contiennent
la liste des noeuds du cluster à utiliser et les options de connexion à employer.

## Consultation des options de connexion

Un _HealthIndicator_ Spring Boot expose ces informations dans un endpoint HTTP:

    curl -s http://localhost:8080/rest/manage/health

## Erreurs

### MongoWaitQueueFullException: Too many threads are already waiting for a connection. Max number of threads (maxWaitQueueSize) of 50 has been exceeded

_cf._ https://stackoverflow.com/a/54981196/636849 pour configurer cette limite.
