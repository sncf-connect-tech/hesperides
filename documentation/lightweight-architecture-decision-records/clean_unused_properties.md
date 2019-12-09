# Nettoyage des propriétés inutilisées

Lorsqu'une propriété est valorisée mais ne correspond à aucune propriété du modèle (déclarée dans un template)
**et** qu'elle n'est référecée dans aucune autre valorisation, on considère qu'elle est inutilisée.

En effet, nous souhaitons conserver la possibilité de factoriser une valeur commune au sein d'une propriété "fictive".

Aujourd'hui le _front_ permet de supprimer ces propriétés au niveau d'un module déployé.
Nous souhaitons porter cette fonctionnalité dans le backend et permettre de nettoyer ces propriétés aussi pour l'ensemble des modules déployés d'une plateforme.

Voici le endpoint :

    DELETE /applications/{application_name}/platforms/{platform_name}/properties/clean_unused_properties?properties_path={properties_path}
    
Le paramètre de requête `properties_path` est facultatif.
Il permettra, entre autre, de décommissionner le code du _front_.

## exemple de propriété "fictive"

Soit un module définissant le fichier `env.yml` suivant
```yaml
facade:
  enpointA: {{url.a}}
  enpointB: {{url.b}}
```
et la plateforme `INT1` utilisant ce module comme suit :

| nom        | valeur                      |
|------------|-----------------------------|
| `base_url` | http://10.1.0.201           |
| `root`     | https://external.cc         |
| `url.a`    | `{{base_url}}`/a_path       |
| `url.b`    | `{{base_url}}`/another_path |
| `url.c`    | `{{root}}`/old_path         |

Son nettoyage provoquerait la disparition de `url.c` **uniquement**, car 
* `base_url`, bien que n'appartenant pas au modèle, est référencée dans la valorisation d'`url.a` et`url.b`
* `root` est également référencée, fut-ce dans une valorisation qui va être nettoyée (ce que le code ne sait pas)

On en déduit que dans un tel cas, il faudra plusieurs appels pour "tout nettoyer".
