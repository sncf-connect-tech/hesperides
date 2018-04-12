# Nouveau SGBD pour l'Event Store et l'état final

## Tests de performance

| |Mongo|Postgres|Maria|
|---|---|---|---|
|Insérer un million d'évènements|24s|125s|160s|
|Insérer 40 000 modules|1s|10s|7s|
|Récupérer 40 000 modules|605ms|11s|292ms|
|*Recherche de modules limitée à 30 résultats*|33ms|23ms|39ms|
|*Stocker des gros templates (5 et 50Mo)*|Limité à 16Mo|OK|Limité à 16Mo|
|*Récupérer des gros templates*|49ms|111ms|64ms|

## Conclusion

Il n'y a pas grandes différences de performance. Mongo et Maria sont limités en taille à l'insertion à 16Mo. Nous savons aujourd'hui qu'il existe des templates qui dépassent cette taille. Ce sont des cas marginaux qui seront gérés de manière particulière lors de la récupération des données existantes.

Mongo a au moins deux avantages :
* La définition des entités est plus simple que pour JPA
* Un driver ReactiveMongo existe et nous permettrait de faire des appels non bloquants

L'avantage d'une base de données compatible JPA donne un plus grand choix de base de données.

Dans les deux cas, le résultat du test de performance sur la recherche de modules nous permet de nous passer d'Elasticsearch.

Et le vainqueur est... **Mongo** !