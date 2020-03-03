# Common platforms properties

Dans le but de permettre aux utilisateurs de l'API REST de définir des propriétés d'application aux plateformes d'une application.

<!-- toc -->
- [Besoin fonctionnel](#besoin-fonctionnel)
- [Design](#design)
- [Ressources REST](#ressources-rest)
    * [POST/PUT /applications/{application_name}/properties/](#postput-applicationsapplication_nameproperties)
    * [GET /applications/{application_name}/properties/](#get-applicationsapplication_nameproperties)
- [Détails notables d'implémentation](#détails-notables-d'implémentation)
- [global_properties_usage](#global-properties-usage)
- [application_properties_usage](#application-properties-usage)
- [Purge properties](#purge-properties)

<!-- tocstop -->

## Besoin fonctionnel

- Exposer via l'API REST des ressources CRUD permettant de créer/lire/modifier des propriétés d'application 
(comme pour les globales) partagées entre les plateformes d'une application
- Ajouter dans le frontend une section d'édition de ces propriétés dans la section supérieure de la page /#/properties/APP

## Design 

S'inspirer des globales properties au niveau platform existant, pour réaliser cette fonctionnalité (application properties)

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
   - Informer de façon visuelle quand une propriété d'application est utilisé par une propriété 
     globale/de module/d'instance, pour cela :
     * Indiquer au niveau module et/ou globale, avec un tooltip ou une icône, lorsqu'une propriété fait référence à une 
     propriété d'application  (cf. voyages-sncf-technologies/hesperides-gui#329). Ceci nécessitera la mise en place d'une 
     méthode comme global_properties_usage pour les propriétés globales.
     * Au niveau de la nouvelle section "propriétés d'application", indiquer où elles sont employées, de la même manière
      que c'est fait actuellement pour les globales.
   - Faire en sorte que ces nouvelles propriétés ne puissent avoir aucun impact sur les fonctionnalités 
   get_global_properties_usage, purge_properties et restore_platforms.
   - Prévoir : ApplicationCommad, ApplicationQuery et réutiliser EventCommand pour les commands et events.
   - Ajout d'une nouvelle collection applications pour limiter les impacts et séparer la logique plateforme et la 
   logique application.
   - Prévoir des tests BDD Validant:
     * qu'on peut faire référence à ces propriétés dans: des propriétés de module, de plateforme (globales), 
     d'instance ou d'autres propriétés d'application
     * qu'une propriété d'application est bien écrasée par une propriété du même nom au niveau plateforme/module/instance  
    
 ## global properties usage
   global_properties_usage est une méthode qui permet de savoir le nombre de propriétés (propriétés de modules) qui 
   référencent une propriété globale (proprité de plateforme), 
   prévoir [application_properties_usage](#application-properties-usage) éviterait les impacts avec cette fonctionnalité.
 
 ## application properties usage
   Comme pour global_properties_usage, c'est une methode qui aura pour but de compter le nombre d'utilisation d'une 
   propriété d'application en propriétés globales (propriétés de plateformes), de modules, et d'instances.
 
 ## purge properties
   Cette fonctionnalité permet de supprimer les propriétés globales non référencées 
   au niveau module/instance, ou les propriétés de modules supprimées dans le template. 
   Prévoir une méthode de ce type pour réaliser la même chose pour les propriétés d'application. 
   **Output**: 
        ```
        DELETE /applications/{application_name}/clean_unused_properties
        ```
  L'appel à cette méthode n'aura aucun impact sur les propriétés de plateforme et supprimera
   les propriétés d'application non référencées au niveau plateforme/module ou instance.
   
 ## restore platform
   Permet de restaurer une plateforme supprimé.
   Quand on restore une plateforme supprimé qui fait référence à des 
   propriétés d'application, on peut avoir des propriétés de plateforme
   qui référencent des propriétés d'applications qui n'existent plus. C'est un cas valide.
    