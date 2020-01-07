# Common platforms properties

Dans le but de permettre aux utilisateurs de l'API REST de définir des propriétés communes aux plateformes d'une application.

<!-- toc -->
 [Besoin fonctionnel](#besoin-fonctionnel)
- [Design](#design)
- [Ressources REST](#ressources-rest)
    * [POST/PUT /applications/{application_name}/common_properties/](#post-applicationsapplication_namecommon_properties)
    * [GET /applications/{application_name}/common_properties/](#get-applicationsapplication_namecommon_properties)
    * [DELETE /applications/{application_name}/common_properties/{common_properties_name/}](#delete-applicationsapplication_namecommon_properties_commonpropertiesname)
<!-- tocstop -->

## Besoin fonctionnel

- Exposer via l'API REST des ressources CRUD permettant de créer/lire/modifier/supprimer de propriétés communes (globales) aux plateformes d'une application
- Apeler ces resources dans le front end suite à une action utilisateur

## Design 

S'inspirer de platform_global_properties existant pour réaliser application_common_properties

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
         ],
         "iterable_properties": [
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
          ],
          "iterable_properties": [
              ...
          ]
      }
      ```
  ### DELETE /applications/{application_name}/common_properties/{common_properties_name}
  