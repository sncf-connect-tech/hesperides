# Liste des endpoints de plateformes

### GET /application/{application_name}

Récupère le détail d'une application à partir de son nom.

#### Input

Rien.

#### Output

Code HTTP 200

    {
      "name": "",
      "platforms": [
        {
          "platform_name": "",
          "application_name": "",
          "application_version": "",
          "production": false,
          "modules": [
            {
              "name": "",
              "version": "",
              "working_copy": false,
              "properties_path": "",
              "path": "",
              "instances": [],
              "id": 0
            }
          ],
          "version_id": 0
        }
      ]
    }
    
### POST /application/{application_name}/platforms

Crée la plateforme d'une application, potentiellement à partir d'une autre plateforme.

#### Input

Paramètres de requêtes facultatifs :

* from_application
* from_platform

Cors de la requête :

    {
      "application_name": "",
      "application_version": "",
      "platform_name": "",
      "production": false,
      "version_id": 0,
      "modules": [
        {
          "id": 0,
          "name": "",
          "version": "",
          "path": "",
          "working_copy": false,
          "properties_path": "",
          "instances": [
            {
              "name": "",
              "key_values": [
                {
                  "value": "",
                  "name": ""
                }
              ]
            }
          ]
        }
      ]
    }
    
#### Output

Code 200 + structure identique à l'input.

### PUT /application/{application_name}/platforms

Met à jour la plateforme d'une application, potentiellement à partir d'une autre plateforme.

#### Input

Paramètre de requête facultatif : copyPropertiesForUpgradedModules (?)

TODO : Expliquer à quoi sert ce paramètre

Structure identique à celle de la création.

#### Output

Code 200 +  structure identique à l'input.

### GET /application/{application_name}/platforms/{platform_name}

Récupère le détail d'une plateforme.

#### Input

Paramètre de requête facultatif : timestamp

#### Output

Code 200 + Structure identique à celle de la création.

### DELETE /application/{application_name}/platforms/{platform_name}

Supprime une plateforme.

#### Input

Rien.

#### Output

Code 200.

### GET /application/{application_name}/platforms/{platform_name}/global_properties_usage

Récupère la liste des utilisations des propriétés globales utilisées d'une plateforme.

#### Input

Rien.

#### Output

    {
      "globprop2": [],
      "globalprop1": [
        {
          "inModel": true,
          "path": "#groupe#tlh#1.0.7c#WORKINGCOPY"
        }
      ]
    }
    
Attention, l'output n'est pas un tableau...

### GET /application/{application_name}/platforms/{platform_name}/properties

Récupère la liste des propriétés *valorisées* et des propriétés itérables valorisées ou contenant elles-mêmes au moins une propriété itérable, pour un path donné, c'est-à-dire un module dans un groupe logique (?). 

#### Input

Paramètres de requête :
* path (obligatoire)
* timestamp (facultatif)

#### Output

Code 200

    {
      "key_value_properties": [
        {
          "name": "",
          "value": ""
        }
      ],
      "iterable_properties": [
        {
          "name": "",
          "iterableValorisationItems": [
            "title": "",
            "values": [
            ]
          ]
        }
      ]
    }

### POST /application/{application_name}/platforms/{platform_name}/properties

Enregistre la valorisation des propriétés et propriétés itérables pour un path donné.

#### Input

Paramètres de requête obligatoires :
* comment
* path
* platform_vid

+ Même structure que pour la récupération des propriétés valorisées.

#### Output

Code 200 + Structure identique à l'input.

### GET /application/{application_name}/platforms/{platform_name}/properties/instance_model

Récupère les propriétés d'instance à partir d'un path.

#### Input

Paramètre de requête obligatoire : path.

#### Output

Code 200

    {
      "keys": [
        {
          "name": "",
          "required": false,
          "comment": "",
          "defaultValue": null,
          "pattern": null,
          "password": false
        }
      ]
    }

### POST /application/{application_name}/platforms/{platform_name}/restore_snapshot

?

## GET /application/{application_name}/platforms/{platform_name}/snapshots

?

### POST /application/{application_name}/platforms/{platform_name}/take_snapshots

?

### POST /application/perform_search

Recherche une application à partir de son nom.

#### Input

Paramètre de requête obligatoire : name;

#### Output

Code 200

    [
      {
        "name": ""
      }
    ]

### GET /application/platforms/perform_search

Recherche une plateforme à partir d'un nom d'application et d'un nom de plateforme

#### Input

Paramètres de requête :
* applicationName (obligatoire)
* platformName (facultatif)

#### Output

    [
      {
        "name": ""
      }
    ]

### POST /applications/using_module/{module}/{version}/{type}

Récupère la liste des plateformes qui utilisent un module.

#### Input

Rien.

#### Output

    [
      {
        "application_name": "",
        "platform_name": ""
      }
    ]