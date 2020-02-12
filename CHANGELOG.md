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
## 2020-02-11
### Fixed

- Sub-optimal cache that systematically made LDAP connections (#835) [Lucas Cimon]

- #832: Impossible de purger les propriétés inutilisées d'un module dont le `propertiesVersionId` est à `null` (#833) [Thomas L'Hostis]



## 2020-01-28
### Fixed

- Error 500 when specifying an empty from_name or from_version to POST /technos (#829) [Lucas Cimon]



## 2020-01-22
### Changed

- Now using Java 11 + fix #818 (#819) [Lucas Cimon]


### Fixed

- Runtime OpenJDK version in Dockerfile (#823) [Lucas Cimon]

- Maven Docker image version used. [Lucas Cimon]



## 2019-12-18
### Fixed

- #813: Sauvegarde des commentaires lors des mises à jour de propriétés (oops...) [Thomas L'Hostis]



## 2019-12-09
### Fixed

- #803 : Permettre le nettoyage des propriétés inutilisées (#815) [baaroth]



## 2019-11-19
### Fixed

- #810: Comparaison d'une propriété non définie à une propriété non valorisée mais avec valeur par défaut. [Thomas L'Hostis]



## 2019-11-14
### Fixed

- #782: Détails des propriétés valorisées. [Thomas L'Hostis]



## 2019-10-23
### Fixed

- #792: Le diff tient désormais compte des propriétés globales dans la comparaison des valeurs finales. [Thomas L'Hostis]



## 2019-10-15
### Fixed

- #779: Empêche la création de doublons à la réception d'une requête dupliquée (#789) [Thomas L'Hostis]



## 2019-10-14
### Changed

- $SERVER_PORT configuration env var to simply $PORT (#790) [Lucas Cimon]


### Fixed

- #783: Ne pas inclure pour les plateforme le champ "has_passwords" si "with_password_info" n'est pas fourni. [Thomas L'Hostis]



## 2019-10-11
### Fixed

- Fix #784: Sauvegarder une propriété @required non valorisée ayant le même non qu'une propriété globale valorisée. [Thomas L'Hostis]



## 2019-10-10
### Fixed

- #502: Valorisation conservées lors d'un rollback de version (#774) [Bhoye05]

- #378: Un module ne peut être releasé s'il a au moins une techno non releasée (#773) [Thomas L'Hostis]

- #530: Test fonctionnel de fichier avec propriété par défaut sans valorisation. [Thomas L'Hostis]

- #651: Versions de modules sans duplicat. [Thomas L'Hostis]

- #735: Tests fonctionnels du diff avec timestamp. [Thomas L'Hostis]

- #66 : Global property usage in iterable properties. [Bhoye05]



## 2019-09-30
### Fixed

- #767: Empêche l'accès aux fichiers d'instances présentes uniquement dans un module archivé (#768) [Thomas L'Hostis]



## 2019-09-27
### Added

- Documentation des aspects *implicites* de la glue (#763) [Thomas L'Hostis]


### Fixed

- /applications/using_module/{module_name}/{module_version}/{version_type} does not return anymore platforms that used to contain the target module - close #765 (#766) [Lucas Cimon]



## 2019-09-25
### Added

- Documentation des en-têtes de endpoints dépréciés (#761) [Thomas L'Hostis]

- HTTP headers in responses for deprecated resources + fix FilesController.getFile resource path (#760) [Lucas Cimon]


### Fixed

- #648 cas de MAJ de propriété avec le même nom mais 2 valeurs différentes dans la playload revue. [Bhoye05]

- 648: lever une exception lors de la sauvegarde d'un properties avec une clé dupliqué. [Bhoye05]



## 2019-09-24
### Deprecated

- Les ressources débutant par /files et sont renommée en /applications/.../files - close #699. [Bhoye05]



## 2019-09-23
### Changed

- /technos/create_release query params names to remove deprecated "package" term. [Lucas Cimon]



## 2019-09-20
### Fixed

- #350  Refuser la suppression des modules/technos utilisés par des plateformes. [Bhoye05]



## 2019-09-19
### Fixed

- Unused global properties appearing in /diff - close #752 (#753) [Lucas Cimon]



## 2019-09-04
### Fixed

- Fixing AD integration tests (#749) [Lucas Cimon]



## 2019-08-16
### Fixed

- Bug lorsque propriété simple & itérable ont le même nom - close #701 (#708) [Lucas Cimon]



## 2019-08-13
### Added

- Un champ .transformations dans la sortie du /diff permet désormais d'identifier si la valeur d'une propriété est issue d'une globale (#743) [Lucas Cimon]


### Fixed

- We require property to have a name when saving them through a PUT (#742) [Lucas Cimon]



## 2019-08-07
### Added

- Timestamp query parameter description. [Lucas Cimon]


### Fixed

- #710 #381 : Diff de propriétés globales, de modules, itérables d'instance. [Thomas L'Hostis]



## 2019-08-02
### Added

- Retry logic when requesting LDAP to be more resilient. [Lucas Cimon]


### Fixed

- ClassCastException in GetUserQuery (#731) [Lucas Cimon]

- #39 : Update properties concurrently (#703) [Lucas Cimon]



## 2019-07-31
### Added

- $HOSTNAME displayed in /versions to allow easier identification of backend instance (#726) [Lucas Cimon]


### Fixed

- Correction des règles de restriction des actions "de prod" vis à vis des ACLs - fix #721 (#722) [Lucas Cimon]



## 2019-07-26
### Fixed

- Maven build. [Lucas Cimon]

- Syntaxe du application.yml pour prendre en compte $AGGREGATES_LOG_LEVEL pour contrôler le niveau de logs de PlatformAggregate. [Lucas Cimon]



## 2019-07-22
### Fixed

- ModuleAggregate / PlatformAggregate logs + réduction de leur verbosité pour les tests (#707) [Lucas Cimon]



## 2019-07-11
### Fixed

- Implémentation de la restriction sur les modifications de propriétés de plateforme de production (#694) [Lucas Cimon]



## 2019-07-09
### Fixed

- /applications/using_module renvoie seulement les plateformes employant le module avec exactement la version indiquée - close #685 (#688) [Lucas Cimon]



## 2019-07-02
### Added

- APPLICATION_BOOT_TIME to /versions + removed BUILD_TIME from SENTRY_TAGS as it had an invalid format (#686) [Lucas Cimon]



## 2019-06-28
### Fixed

- #675 Get all applications (#682) [Thomas L'Hostis]



## 2019-06-27
### Added

- Support Sentry (#681) [Lucas Cimon]



## 2019-06-20
### Fixed

- Pour modules/perform_search, en cas de match exact, il est toujours inclus en 1er résultat - cf. #595 & Fix: stress tests en upgradant gatling-maven-plugin (#676) [Lucas Cimon]



## 2019-06-13
### Fixed

- /manage/mappings - close #414 (#673) [Lucas Cimon]

- Il est désormais possible de fournir un template_name pour les GET/DELETE via Swagger - close #639 (#671) [Lucas Cimon]



## 2019-06-05
### Fixed

- Les caractères HTML échappés dans le JSON de la sortie de GET /files posent problème - close #662 (#663) [Lucas Cimon]



## 2019-05-27
### Fixed

- 404 en cas de _version type_ invalide dans /properties?path= (#656) [Lucas Cimon]



## 2019-05-20
### Fixed

- #644 Paramétrage du nombre de résultats retournés par la recherche de modules et de technos (#645) [Thomas L'Hostis]



## 2019-05-16
### Fixed

- Get properties of a platform with valued properties at a specific time in the past - part of the resolution of #229 (#647) [Lucas Cimon]



## 2019-05-15
### Added

- Description du format des query params "terms" dans le Swagger pour les recherches de modules & technos (#643) [Lucas Cimon]



## 2019-05-14
### Added

- /users/auth?logout=true pour permettre la déconnexion d'utilisateurs via leur navigateur (#642) [Lucas Cimon]


### Fixed

- Le conteneur Docker peut être lancé sans $PROJECTION_REPOSITORY_MONGO_URI si le profil Spring "fake_mongo" est défini (#641) [Lucas Cimon]



## 2019-05-13
### Fixed

- On permet la restauration de plateforme qui viennent d'être supprimées mais n'ont pas été modifiées dans les 7 derniers jours - close #638 (#640) [Lucas Cimon]



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
