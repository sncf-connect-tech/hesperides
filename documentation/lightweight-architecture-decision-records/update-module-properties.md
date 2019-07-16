# Mise à jour de propriétés d'un module déployé

## Résumé du besoin

Permettre la modification des valorisations des propriétés d'un module déployé de manière indépendante.

## Objectif

Permettre aux utlisateurs de modifier simultanément plusieurs modules distincts sur une même plateforme.

## Limitations

On ne pourra pas modifier simultanément le même module déployé (avec un path identique).

## Solution

Créer un nouveau version_id au niveau du module déployé.

### Modification des propriétés d'un module

Nouveau endpoint :

    PUT /applications/{application_name}/platforms/{platform_name}/deployed_modules/properties?path={request_parameter}

* Vérifier le version_id de ce module et l'incrémenter
* Incrémenter le version_id de la platforme mais ne pas le vérifier
* Mettre à jour les propriétés

```
{
    "deployed_module_version_id": 1,
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

* `POST properties` : vérifier le version_id de la plateforme mais pas celui du deployed module puis appeler le `PUT`
* `GET properties` : ajouter le champ `deployed_module_version_id`
* Passer ces 2 endpoints en Deprecated et rediriger vers les nouveaux endpoints contenant `.../deployed_modules/properties...`
* Mettre à jour les tests fonctionnels, notamment `PlatformClient`
* Créer un Controller `DeployedModulesController` et un sous-groupe Swagger `Deployed modules`
* Impacter le front
* Communiquer le Deprecated et la nouvelle ressource aux utilisateurs
* Impacter la sharedLib ? 