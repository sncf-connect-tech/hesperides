# Diff

Dans le but de permettre à tous les clients de l'API REST Hesperides de visualiser
les différences de propriétés entres plateformes / modules / à des instants donnés,
nous y ajoutons une fonctionnalité de _diff_.

<!-- Pour mettre à jour ce sommaire: 
    markdown-toc --indent "    " -i diff.md
-->

<!-- toc -->

- [Besoin fonctionnel](#besoin-fonctionnel)
- [Design](#design)
- [Ressources REST](#ressources-rest)
    * [GET /applications/{application_name}/platforms/{platform_name}/properties/diff](#get-applicationsapplication_nameplatformsplatform_namepropertiesdiff)
- [Détails notables d'implémentation](#details-notables-dimplementation)

<!-- tocstop -->

## Besoin fonctionnel

- Exposer via l'API REST une ressource permettant de comparer des propriétés, pour les clients HTTP n'aient pas à réimplémenter cette fonctionnalité
- Appeler cette ressource REST dans le _fontend_ pour remplacer l'implémentation JS actuelle, difficile à maintenir


## Design

Plutôt que d'employer une lib existante comme [`Maps.difference` de Guava](https://guava.dev/releases/23.0/api/docs/com/google/common/collect/Maps.html#difference-java.util.Map-java.util.Map-),
qui introduirait une importante dépendance supplémentaire et nous astreindrait à employer des classes natives (`Map`, `List`...),
nous avons décider d'implémenter la logique de "diff" nous-même sur les classes du domaine comme [AbstractProperty](https://github.com/voyages-sncf-technologies/hesperides/blob/access-control/core/domain/src/main/java/org/hesperides/core/domain/templatecontainers/entities/AbstractProperty.java).


## Ressources REST

### GET /applications/{application_name}/platforms/{platform_name}/properties/diff
**Output**:
```
{
  "only_left": [
    {
      "name": "appenders.file-rolling-vsctlayout"
      "iterable_valorisation_items": [
        {
          "title": "",
          "values": [
            { "name": "appendername", "value": "nom_de_mon_appender" },
            { "name": "roll.max-index", "value": "1" },
            { "name": "roll.threshold", "value": "1" },
            { "name": "filename", "value": "nom_de_mon_fichier" }
          ]
        }
      ],
    }
  ],
  "only_right": [],
  "common": [
    { "name": "titi", "value": "version1-titi" }
  ],
  "differing": [
    {
      "left": "version1-tata",
      "right": "version1-tata-different",
      "name": "tata"
    },
    {
      "left": "\"{{prout}}\"",
      "right": "version1-toto-different",
      "name": "toto"
    },
    {
      "left": "version1-tete",
      "right": "",
      "name": "tete"
    }
  ]
}
```


## Détails notables d'implémentation

On traite le cas des _iterable items_ en nombre différent dans les propriétés "left" & "right"
comme des cas `only_left` / `only_right`.
