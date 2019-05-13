# CHANGELOG
Le changelog du frontend est ici: [hesperides-gui/CHANGELOG.md](https://github.com/voyages-sncf-technologies/hesperides-gui/blob/master/CHANGELOG.md)

Tous les changements notables sur ce projet sont documentés dans ce fichier.
Le format est basé sur [Keep a Changelog](http://keepachangelog.com).

Il est généré automatiquement à partir des commits dont le message débute par
`added:` / `changed:` / `deprecated:` / `removed:` / `fixed:` / `security:`
gâce à [gitchangelog](https://github.com/vaab/gitchangelog) :
```
pip install gitchangelog pystache
gitchangelog
```

Les messages de commit ne comprenant pas ces préfixes,
par exemple ceux suivant la convention [Conventional Commits](https://www.conventionalcommits.org)
débutant par `chore:` / `docs:` / `refactor:` / `style:` / `test:`,
ne seront simplement pas inclus dans ce changelog.

Pour automatiquement mettre à jour ce fichier à chaque commit,
placez le code suivant dans `.git/hooks/pre-commit` :
```
#!/bin/sh
git fetch --tags upstream && gitchangelog && git add CHANGELOG.md
```

<!-- gitchangelog START -->
## _(unreleased)_
### Fixed

- On permet la restauration de plateforme qui viennent d'être supprimées mais n'ont pas été modifiées dans les 7 derniers jours - close #638. [Lucas Cimon]



## 2019-05-03
### Added

- Paramètre booléen ?copy_instances_and_properties pour la copie de plateforme - close #634 (#635) [Lucas Cimon]

- /{application_name}/platforms/{platform_name}/restore - implementation backend pour #622 (#631) [Lucas Cimon]


### Changed

- Simplification de /rest/versions & correction timezone du "build time" (#626) [Lucas Cimon]


### Fixed

- Les platformes restaurés ne pouvaient plus être modifiées à cause d'un problème de cohérence dans l'aggrégat (#636) [Lucas Cimon]

- #632 Simplification du Swagger (#633) [Thomas L'Hostis]



## 2019-04-26
### Fixed

- Lors d'une copie de plateforme on ignore les deployed modules inactifs de l'historique (#624) [Lucas Cimon]



## 2019-04-24
### Deprecated

- POST /technos/perform_search (#618) [Lucas Cimon]


### Fixed

- Correction du GET /events/platforms/... & ajout tests BDD (#617) [Lucas Cimon]



## 2019-04-23
### Added

- Utilisation de `gitchangelog` pour générer ce fichier CHANGELOG.md (#616) [Lucas Cimon]



<!-- gitchangelog END -->


## 2019-04-18
### Changed
- le même ID ne peut plus être réutilisé pour plusieurs _deployed modules_ lors de requêtes `POST` & `PUT` sur `platforms` - _cf._ https://github.com/voyages-sncf-technologies/hesperides/issues/574

### Fixed
- un espace est désormais autorisé dans les version de plateforme


## v4.3
Refonte complète de l'application en préservant une rétro-compatibilité totale de l'API.

### Changed
Changement architecturaux majeurs :
- organisation du code selon le paradigme du **Domain Driven Design**
- utilisation du framework Axon pour implémenter les principes d'**Event Sourcing**
- **Spring Boot 2** remplace Dropwizard comme framework web
- l'application est désormas déployée comme conteneur **Docker**

Quelques changements fonctionnels mineurs :
- l'application est désormais complètement **stateless** (plus de cookie de session)
- les `technos` ne peuvent être supprimées tant qu'elles sont employées par au moins un module
- la requête des `technos`, `modules` & `platforms` est désormais insensible à la casse
- les mots de passe sont complètement cachés lors des requêtes `GET` sur les propriétés et fichiers pour les utilisateurs sans les droits "prod"
- le nombre de _deployed module_ préservés dans l'historique des plateformes est limité à 2
- la gestion des doublons de définitions de propriétés dans les templates, avec un même nom mais des annotations différentes,
est désormais déterministe et documentée

### Added
- de nouveaux _endpoints_ :
  * `GET /applications` qui est plus performant et remplace `GET /applications/perform_search?name=` employé auparavant
  * `GET /modules/using_techno/{techno_name}/{techno_version}/{techno_type}`
  * `GET /technos & /technos/{techno_name} & /technos/{techno_name}/{techno_version}`
  * `/manage/prometheus` qui fournit des métriques Prometheus
- des tests BDDs couvrent toutes les fonctionnalités
- un cache interne des demandes d'authentifcations à l'ActiveDirectory, pour limiter ces requêtes (avec un TTL de 5min TTl par défaut)
- des tests d'intégration avec véritable base Mongo & des stress tests Gatling exécutés dans la pipeline Travis

### Deprecated
- le _endpoint_ `/templates/packages`, remplacé par `technos`
- `POST /technos/search - /modules/search - /modules/perform_search - /applications/search - /applications/perform_search` remplacé par des _endpoints_ `GET` avec les même noms
