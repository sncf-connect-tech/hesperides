# Contribuer

## Environnement de dev

Nous utilisons IntelliJ avec le JDK 8 et les plugins :

* Maven
* Kotlin
* Lombok
* Spring (Boot, Data, MVC, Security)
* Cucumber

## Démarche

Toute contribution est la bienvenue. Nous fonctionnons en pull request. Voici la démarche :

1. Forker et cloner le projet
1. Créer une nouvelle feature-branche (`git checkout -b feature/my-new-feature`)
1. Implémenter la fonctionnalité sans oublier les *tests fonctionnels*
1. Commit + Push
1. Créer une pull request

Pour qu'une pull request soit acceptée, il faut :

* Que le build (+ tests) Travis soit un succès
* Qu'au moins une personne de la core-team approuve la pull request après un code-review

## Documentation

La documentation de l'application (choix techniques, architecture, etc.) se trouve dans le dossier [documentation](documentation). Le modèle de domaine (documentation/model.mdj) peut-être ouvert avec l'application StarUML.

## Postman

L'application étant une API REST, nous utilisons Postman pour effectuer des tests manuels. Veuillez consulter la documentation sur cette [page](documentation/postman/postman.md). 