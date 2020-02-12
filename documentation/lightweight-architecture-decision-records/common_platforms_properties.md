# Common platforms properties

Dans le but de permettre aux utilisateurs de l'API REST de définir des propriétés d'application aux plateformes d'une application.

<!-- toc -->
- [Besoin fonctionnel](#besoin-fonctionnel)
- [Design](#design)
- [Ressources REST](#ressources-rest)
    * [POST/PUT /applications/{application_name}/properties/](#postput-applicationsapplication_nameproperties)
    * [GET /applications/{application_name}/properties/](#get-applicationsapplication_nameproperties)
- [Détails notables d'implémentation](#détails-notables-d'implémentation)
<!-- tocstop -->

## Besoin fonctionnel

- Exposer via l'API REST des ressources CRUD permettant de créer/lire/modifier des propriétés d'application (comme pour les globales) partagées entre les plateformes d'une application
- Ajouter dans le frontend une section d'édition de ces propriétés dans la section supérieure de la page /#/properties/APP

## Design 

S'inspirer des globales properties au niveau platform existant, pour réaliser cette fonctionnalité (application common properties)

## Ressources REST
### POST/PUT /applications/{application_name}/properties/
**Input** :
     
     ```
     {
        ...
        application_properties_version_id": 1,
         "key_value_properties": [
             {
                 "value": "key1",
                 "name": "value1"
             },
             ...    
         ]
     }
     ```
 ### GET /applications/{application_name}/properties/
 **Output**:
 
      ```
      {
          "application_properties_version_id": 1,
          "key_value_properties": [
              {
                  "value": "key1",
                  "name": "value1"
              },
              ...    
          ]
      }
      ```
  ## Détails notables d'implémentation
   - Le paramètre application_properties_version_id à inclure systématiquement dans le put
   - Incrémenter cet application_properties_version_id à chaque création de ces propriétés
   - Informer de façon visuelle quand une propriété d'application est utilisé par une propriété globale/de module/d'instance, pour cela :
     * Indiquer au niveau module et/ou globale, avec un tooltip ou une icône lorsqu'une propriété fait référence à une propriété commune  (cf. voyages-sncf-technologies/hesperides-gui#329). Ceci nessecitera la mise en place d'une méthode comme global_properties_usage pour les propriétés globales.
     * Au niveau de la nouvelle section "propriétés d'application", indiquer où elles sont employées, de la même manière que c'est fait actuellement pour les globales.
   - Faire en sorte que ces nouvelles propriétés ne puissent avoir aucun impact sur les fonctionnalités get-global-properties-usage, purge-properties et restore-platforms.
   - Prévoir : ApplicationCommad, ApplicationQuery et réutiliser EventCommand pour les commands et events.
   - Ajout d'une nouvelle collection applications pour limiter les impacts et séparer la logique plateforme et la logique application.
   - Prévoir des tests BDD Validant:
     * qu'on peut faire référence à ces propriétés dans: des propriétés de module, de plateforme (globales), d'instance ou d'autres propriétés d'application
     * qu'une propriété d'application est bien écrasée par une propriété du même nom au niveau plateforme / module / instance  