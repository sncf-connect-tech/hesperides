# Recherche de propriétés

Recherche de propriétés par nom et/ou valeur *sur l'ensemble des applications*.

LADR frontend: [Recherches de propriétés](https://github.com/voyages-sncf-technologies/hesperides-gui/blob/master/tech_docs/lightweight-architecture-decision-records/properties_search.md)

## Règles spécifiques

* On ne tient compte que des propriétés des modules déployés non archivés
* Pour une question de sécurité, les mots de passe sont gérés de la manière suivante :
  * Si l'utilisateur a les droits de prod, on affiche la propriété normalement dans tous les cas
  * Si l'utilisateur n'a pas les droits de prod et que la recherche est faite sur le nom des propriétés, on remplace la valeur des mots de passe de prod ayant ce nom par des étoiles
  * Si l'utilisateur n'a pas les droits de prod et que la recherche est faite sur la valeur des propriétés, on ne retourne pas les mots de passe de prod ayant cette valeur
  
 ### Évolutions envisagées
 
* Vérification préalable du nombre de résultats à retourner pour potentiellement demander à l'utilisateur d'être plus
  précis dans sa recherche si ce nombre de résultats est trop grand => *Les tests de performance actuels ne nécessitent
  pas de faire cette vérification, à voir ce que ça donne à l'avenir*
* La possibilité de filtrer les résultats par application
* Indiquer dans le retour de l'API, par propriété : son type (globale / de module / d'instance), ses annotations et s'il
  s'agit d'une propriété supprimée
* Lorsqu'un utilisateur n'a pas les droits de prod globaux mais seulement sur certaines applications, afficher les mots
  de passe de prod en clair pour ces applications

## Nouveau endpoint

    /applications/search_properties?property_name={property_name}&property_value={property_value}
    
## Output

    [
      {
        property_name: String,
        property_value: String,
        application_name: String,
        platform_name: String,
        properties_path: String
      },
      ...
    ]

## Solutions étudiées

La première solution testée pour effectuer la recherche est la requête suivante :

    @Query(
            value = "{ " +
                    "'deployedModules.valuedProperties': { $elemMatch: { 'name': { '$regex': ?0, '$options': 'i' }, 'value': { '$regex': ?1, '$options': 'i' } } } " +
                    "}",
            fields = "{ " +
                    "'key': 1, " +
                    "'deployedModules.propertiesPath': 1, " +
                    "'deployedModules.valuedProperties._class': 1, " +
                    "'deployedModules.valuedProperties': { $elemMatch: { 'name': { '$regex': ?0, '$options': 'i' }, 'value': { '$regex': ?1, '$options': 'i' } } } " +
                    "}"
    )
    List<PlatformDocument> searchProperties(String propertyName, String propertyValue);

Le but était de filtrer les documents (via la clause `value`) contenant *des propriétés* recherchées mais aussi de ne retourner *que les propriétés* correspondant à la recherche (via la clause `fields`) pour ne pas avoir à re-filtrer ensuite en Java.

Cette solution ne fonctionne pas car MongoDB (version 4.x) ne permet pas dans la clause `fields` d'utiliser `$elemMatch` sur un tableau imbriqué dans un tableau lui-même imbriqué dans un document.

À partir de là, nous avons listé 4 solutions possibles.

### L'agrégation

Voici un exemple de requête testée ayant de bonnes performances :

    db.getCollection("platform").aggregate([
      { "$match": {
        "deployedModules": {
          "$elemMatch": {
             "valuedProperties": {
               "$elemMatch": {
                 "name": "platform_url",
                 "value": "{{platform_subdomain}}www.{{domain_name}}"
               }
             }
           }
        }
      }},
      { "$addFields": {
        "deployedModules": {
          "$filter": {
            "input": {
              "$map": {
                "input": "$deployedModules",
                "as": "deployedModulesProjection",
                "in": {
                  "propertiesPath": "$$deployedModulesProjection.propertiesPath",
                  "valuedProperties": {
                    "$filter": {
                      "input": "$$deployedModulesProjection.valuedProperties",
                      "as": "valuedPropertiesProjection",
                      "cond": {
                        "$and": [
                          { "$eq": [ "$$valuedPropertiesProjection.name", "platform_url" ] },
                          { "$eq": [ "$$valuedPropertiesProjection.value", "{{platform_subdomain}}www.{{domain_name}}" ] }
                        ]
                      }
                    }
                  }             
                }
              },
            },
            "as": "deployedModulesProjection",
            "cond": { "$gt": [ { "$size": "$$deployedModulesProjection.valuedProperties" }, 0 ] }
          }
        }
      }
    }
    ])

Requête inspirée de https://stackoverflow.com/a/29072062/2430043

En termes de performances, ce type de requête prend généralement moins d'une seconde à s'exécuter.

Les inconvénients de cette solution :
* Nécessite la connaissance du framework d'agrégation donc diminue sa maintenabilité
* Potentiellement coûteux en mémoire car le framework d'agrégation MongoDB crée sa propre vue en mémoire avant d'appliquer le filtre définitif
* Testable uniquement avec une vraie base de données MongoDB mais pas avec la librairie FakeMongo

Si cette solution est choisie, il reste plusieurs choses à faire :
* Reproduire cette requête en Spring Data Mongo => https://stackoverflow.com/a/55117298/2430043

Si par la suite il faut pouvoir filtrer par expression régulière, il faudra alors passer à la version 4.2 de MongoDB : https://docs.mongodb.com/manual/reference/operator/aggregation/regexMatch/

### Filtre Java

L'idée est de ne récupérer que les documents contenant des propriétés recherchées et de ne rapatrier que les champs dont on a besoin puis de faire le filtre en Java.

Cette solution est envisagée pour sa facilité de mise en œuvre et donc sa maintenabilité.

Les inconvénients sont :

* Des performances inférieures à la solution précédente
* Le filtre sur les propriétés recherchées (par nom et/ou valeur) est appliqué sur la requête qui nous retourne les
  plateformes contenant ces propriétés, il faut ensuite filtrer en Java les propriétés pour ne retourner que celles qui
  sont concernées par la recherche

En termes de performances et à titre d'exemple, une recherche de propriétés ayant le nom "platform" (environ 400
résultats sans autre filtre) prend environ une seconde à s'exécuter de bout en bout.

C'est cette méthode qui, pour l'instant, a été implémentée.

#### Modification du 03/02/2021

Mise en place de la recherche par nom et/ou valeur partielle. La requête MongoDB utilise désormais la méthode `$regex`
et le filtre Java la méthode `.contains`.

+ Mise en place d'index sur le nom et la valeur des propriétés.

### Créer une vue à l'aide des évènements de l'application

L'EventSourcing permet typiquement de créer une collection ne contenant que les données dont on a besoin pour effectuer
une recherche de propriétés :

* Application
* Plateforme
* Module déployé (properties path)
* Nom et valeur de la propriété

Cette solution serait la plus efficace en termes de performances et la moins coûteuse en mémoire vive.

Les principaux inconvénients sont :
* Une mise en place coûteuse en temps
* La redondance des données (répliquées) implique de prendre plus d'espace disque
* Cela complexifie l'application bien qu'elle soit prévue pour

Cette solution est envisagée à plus long terme, par exemple dans le cadre d'une contribution externe si la demande est forte.

### Solution dégradée

Si aucune des deux premières solutions n'est acceptable au niveau des performances, nous envisageons de forcer la sélection d'une application sur laquelle faire la recherche dans un premier temps afin d'employer un filtrage par l'application en mémoire (= en Java, solution n°2), en limitant le risque de consommer trop de RAM.
