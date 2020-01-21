# Mise à jour de propriétés d'un module déployé

## Résumé du besoin

Permettre la modification des valorisations des propriétés d'un module
déployé et des propriétés globales indépendamment de la plateforme.

## Objectif

Permettre aux utlisateurs de modifier simultanément les propriétés de
modules distincts et les propriétés globales d'une platforme.

## Limitations

On ne pourra pas modifier simultanément le même module déployé (avec un path identique).

## Solution

Créer un nouveau version_id associé aux propriétés d'un module déployé : `properties_version_id`.

### Modification des propriétés d'un module

Nouveau endpoint :

    PUT /applications/{application_name}/platforms/{platform_name}/properties?path={request_parameter}

* Vérifier le `properties_version_id` de ce module et l'incrémenter
* Incrémenter le `version_id` de la platforme mais ne pas le vérifier
* Mettre à jour les propriétés

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

### Impacts

* `POST properties` : vérifier le `version_id` de la plateforme mais pas le `properties_version_id` puis appeler le `PUT`
* `GET properties` : ajouter le champ `properties_version_id`
* Passer ces cet endpoint en Deprecated et rediriger vers le nouveau endpoints en  `PUT`
* Mettre à jour les tests fonctionnels, notamment `PlatformClient`
* Créer un Controller `DeployedModulesController` et un sous-groupe Swagger `Deployed modules`
* Impacter le front
* Communiquer le Deprecated et la nouvelle ressource aux utilisateurs
* Impacter la sharedLib ? 
