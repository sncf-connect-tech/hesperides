# Contribuer

Toute contribution est la bienvenue. Commencer par forker puis cloner le projet. Pour le faire fonctionner sur IntelliJ, il vous faut le jdk 8 et les plugins :

* Maven
* Kotlin
* Lombok
* Spring (Boot, Data, MVC, Security)
* Cucumber

Vous pourrez alors lancer l'application avec les profils **noldap** et **fake_mongo**.

## PR

Nous fonctionnons en pull request :

1. Créer une nouvelle feature-branche (`git checkout -b feature/my-new-feature`)
1. Implémenter la fonctionnalité sans oublier les *tests fonctionnels*
1. Commit-Push
1. Créer une pull request

Pour qu'une pull request soit acceptée, il faut :

* Que le build (+ tests) Travis soit un succès
* Qu'au moins une personne de la core-team approuve la pull request après un code-review

## Où trouver la documentation ?

La documentation de l'application (choix techniques, architecture, etc.) se trouve dans le dossier [documentation](documentation). Le modèle de domaine (model.mdj) peut-être ouvert avec l'application StarUML.

## Tester manuellement avec Postman

L'application étant une API REST, nous utilisons Postman pour effectuer des tests manuels. Veuillez consulter la documentation sur cette [page](documentation/postman/postman.md).

## Tester automatiquement