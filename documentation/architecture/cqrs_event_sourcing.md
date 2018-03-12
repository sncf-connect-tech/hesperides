# CQRS et Event Sourcing

Nous avons décidé d'utiliser le framework Axon pour faciliter l'aspect CQRS/Event Sourcing de l'application.

Le principe *CQRS* (Command Query Responsability Segregation) consiste à séparer strictement les accès en écriture (Command) des accès en lecture (Query). Le pattern *Event Sourcing* consiste à stocker les évènements plutôt que l'état de l'application.

Axon n'impose pas d'utiliser l'Event Sourcing mais le permet. L'application existante fonctionne en Event Sourcing. Pour assurer la *rétrocompatibilité* avec l'existant, nous avons dû utiliser ce pattern dans la nouvelle application.

Voici un schéma de l'architecture de la nouvelle application :

![Architecture Event Sourcing et CQRS](images/cqrs_event_sourcing.png)

## Command / Event / Aggregate

D'un côté, nous avons les *Command*. Par exemple, CreateModuleCommand.

La couche Application, qui contient les cas d'utilisation d'un agrégat, émet une Command sur le *Command Bus*, géré par Axon.

Un agrégat, défini dans la couche Domain, intercepte cette Command à l'aide d'un *Command Handler*, puis émet un Event. Par exemple : ModuleCreatedEvent.

Cet Event transite sur l'*Event Bus* géré par Axon, est intercepté par un *Event Handler* et stocké dans un *Event Store*.

Notre Event Store est un Redis. Axon ne gère pas nativement le stockage des Event dans Redis (il le fait pour Mongo, par exemple). Il faut donc indiquer à Axon comment stocker ces Event dans Redis. C'est le rôle de la classe *RedisStorageEngine*.

De plus, pour des raisons de rétrocompatibilité, nous devons transformer ceux qui sont générés par Axon au format Legacy (Json). C'est le rôle de la classe *LegacyCodec* (et de tout le package `org.hesperides.infrastructure.redis.eventstores.legacy`).

À chaque Event, l'état d'une entité est persisté sous forme de View dans un espace physique représenté par Redis et Elasticsearch.

À noter que les Event et les Command sont des objets *immuables*.

### Particularité de notre architecture

*Afin d'être compatible avec l'application actuelle, il est nécessaire de stocker certains états dans Elasticsearch. Mais on doit aussi stocker les autres états, ceux dont l'application actuelle n'a pas besoin, pour les Query.*

*On doit se mettre d'accord sur cet aspect : est-ce qu'on utilise Elasticsearch, Redis, ou les deux pour stocker les états ?*

## Query

De l'autre côté, nous avons les *Query* qui servent à récupérer des données qui seront envoyées au client de l'application.

La couche Application émet une Query sur le *Query Bus*. Elle est interceptée par un *Query Handler* déclaré dans la couche Domain et implémenté dans la couche Infrastructure.

Une Query retourne une *View*, définie dans la couche Domain, qui est la représentation des données sous forme d'objet Java.

La façon dont les données sont effectivement récupérées (d'une base de données par exemple), dépend de l'implémentation dans laquelle se trouve le Query Handler.

Cas particulier : historique