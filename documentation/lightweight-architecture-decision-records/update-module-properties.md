# Mise à jour de propriétés d'un module déployé

## Résumé du besoin

L'idée est de permettre la modification des valorisations des propriétés d'un module de manière indépendente.

## Objectif

Pour faire simple, il faut permettre aux utlisateurs de pouvoir modifier simultanément plusieurs modules distincts sur une même plateforme.

## Limitations

On ne pourra pas modifier simultanément le même module déployé (avec un path identique).
Cela provoque un conflit détecté via le version_id.

## Comment on veut faire ?

* Nouveau version_id au niveau du module déployé

### Modification des propriétés d'un module (nouveau endpoint)

* Vérifier le version_id de ce module et l'incrémenter
* Incrémenter le version_id de la platforme mais ne pas le vérifier
* Mettre à jour les propriétés

    PUT /applications/xxx/platforms/abc/deployed_module/properties?path=###

    {
	"deployed_module_version_id": 1,
  "key_value_properties": [
    {
      "value": "PhpRedis",
      "name": "vsc_redis_interface"
    },
    {
      "value": "7d940810-3a69-4482-b646-d8d5ce35082d",
      "name": "vsc_api_expedia_header_key"
    }
	],
  "iterable_properties": [
    {
      "iterable_valorisation_items": [
        {
          "title": "",
          "values": [
            {
              "value": "vslcms{{platform.env}}{{platform.id}}",
              "name": "username"
            },
            {
              "value": "Gra7JKzu4FH9s",
              "name": "mysql_slave.password"
            }
          ]
        },
      "name": "mysql_slaves"
    }
  ]
}

### Impacts IO

POST properties => vérifie le version_id de la plateforme mais pas celui du deployed module, puis appelle le put
GET properties => Ajoute le version_id du deployed_module

Renommer ces 2 endpoints (deprecated etc.) et les tests

Créer un sous-groupes swagger Deployed modules

Impacter le front et communiquer client pour le deprecated 