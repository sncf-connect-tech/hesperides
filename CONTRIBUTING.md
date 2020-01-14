# Contribution

Nous fonctionnons en pull request. Pour contribuer, il suffit donc de forker le projet, puis :

1. Créer une nouvelle feature-branche (`git checkout -b feature/my-new-feature`)
1. Implémenter la fonctionnalité sans oublier les *tests*
1. Push
1. Créer une pull request

Pour qu'une pull request soit acceptée, il faut :

* Que le build Travis et les tests soient valides
* Qu'au moins une personne fasse une revue de code et approuve la pull request

## Où trouver la documentation ?

La documentation de l'application (choix techniques, architecture, etc.) se trouve dans le dossier [documentation](documentation).
Le modèle de domaine (`domain-model.mdj`) peut-être ouvert avec l'application StarUML.

## Installation d'un environnement de développement

Pour lancer l'application sur un poste de dev, nous utilisons IntelliJ Ultimate Edition et les plugins suivants :

* Maven
* Kotlin
* Lombok
* Spring Boot
* Spring Data
* Spring MVC
* Spring Security
* Cucumber for Java

Une fois installés vous pourrez alors lancer l'application avec les profils Spring **noldap** et **fake_mongo**.

Si le premier lancement ne fonctionne pas, il est nécessaire de faire un `mvn clean install` ou pour gagner du temps :

    mvn clean install -Dmaven.javadoc.skip=true -DskipTests 

*Nous avons rencontré un problème qui empêche l'application de démarrer : le bouton Run se grise après avoir cliqué dessus sans que rien ne se passe.
Il semblerait que le problème vienne du plugin Gradle, il suffit de le désactiver.
Si le problème persiste, il faut désactiver tous les plugins d'IntelliJ, et ne réactiver que ceux listés ci-dessus (en acceptant leurs dépendences).*

*Cela semble être un problème de compatibilité de plugins, espérons temporaire.*

### Requirements

 * Java 11 (correto, openjdk)

Choose between:

 * Docker (see docker-compose & `Dockerfile` file)

And :

 * MongoDB

Or just launch the application with those Spring Profiles: `noldap`, `fake_mongo`

### Build

Build the whole project:

    mvnw package

Build Docker image

    docker build . -t hesperides/hesperides

### Run

Some variables are set as environment variables:
* `SPRING_PROFILES_ACTIVE`
* `LDAP_URL`
* `LDAP_DOMAIN`
* `LDAP_USER_SEARCH_BASE`

See [`boostrap/src/main/resources/application.yml`](https://github.com/voyages-sncf-technologies/hesperides/blob/master/bootstrap/src/main/resources/application.yml)


## Tests manuels avec Postman

L'application étant une API REST, nous utilisons Postman pour effectuer des tests manuels.
Vous pouvez consulter la documentation sur cette [page](documentation/postman/postman.md).

## Tests automatiques, test-first, BDD

Chaque feature/bugfix doit être testé automatiquement. Cela peut être un test fonctionnel et/ou un test unitaire.
L'idéal est de créer un ou plusieurs tests avant d'implémenter une fonctionnalité ou de corriger un bug.

Nous utilisons le framework Cucumber pour les tests fonctionnels et privilégions les tests de fonctionnalités *end-to-end*.
Nous utilisons les tests unitaires lorsque cela nous semble pertinent. Pour plus de context, _cf._ [LADR - Stratégie de tests](documentation/lightweight-architecture-decision-records/tests-strategy.md)

Pour exécuter tous les tests unitaires:

    mvn test
