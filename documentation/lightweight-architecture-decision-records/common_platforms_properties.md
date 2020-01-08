# Common platforms properties

Dans le but de permettre aux utilisateurs de l'API REST de définir des propriétés communes aux plateformes d'une application.

<!-- toc -->
- [Besoin fonctionnel](#besoin-fonctionnel)
- [Design](#design)
- [Ressources REST](#ressources-rest)
    * [POST/PUT /applications/{application_name}/common_properties/](#post-applicationsapplication_namecommon_properties)
    * [GET /applications/{application_name}/common_properties/](#get-applicationsapplication_namecommon_properties)
- [Détails notables d'implémentation](#details-notables-dimplementation)
<!-- tocstop -->

## Besoin fonctionnel

- Exposer via l'API REST des ressources CRUD permettant de créer/lire/modifier des propriétés communes (comme pour les globales) partagées entre les plateformes d'une application
- Ajouter dans le frontend une section d'édition de ces propriétés dans la section supérieure de la page /#/properties/APP

## Design 

S'inspirer de globales properties au niveau platform existant, pour réaliser cette fonctionnalité (application common properties)

## Ressources REST
### POST/PUT /applications/{application_name}/common_properties/
**Input** :
     
     ```
     {
        ...
         "key_value_properties": [
             {
                 "value": "key1",
                 "name": "value1"
             },
             ...    
         ]
     }
     ```
 ### GET /applications/{application_name}/common_properties/
 **Output**:
      
      ```
      {
          "properties_version_id": 1,
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
 - Prévoir un mécanisme de versionId pour une meilleure gestion des modifications ultérieures de ces propriétés
 - Lors de la valorisation séparer la logique des propriétés communes aux propriétés globales, mais prévoir une information visuelle quand une propriété commune est utilisé au niveau plateforme par une propriété globale. 
 - Faire en sorte que ces nouvelles propriétés ne puissent avoir aucun impact sur les fonctionnalités get-global-properties-usage, purge-properties et restore-platforms.
 - Prévoir : ApplicationCommad, ApplicationQuery et réutiliser EventCommand pour les commands et events.
 - Ajout d'une nouvelle collection applications pour limiter les impacts et séparer la logique plateforme et la logique application.
