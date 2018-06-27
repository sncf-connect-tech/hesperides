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

La documentation de l'application (choix techniques, architecture, etc.) se trouve dans le dossier [documentation](documentation). Le modèle de domaine (model.mdj) peut-être ouvert avec l'application StarUML.

## Tests manuellement avec Postman

L'application étant une API REST, nous utilisons Postman pour effectuer des tests manuels. Vous pouvez consulter la documentation sur cette [page](documentation/postman/postman.md).

## Tests automatiques, test-first, BDD

Chaque feature/bugfix doit être testé automatiquement. Cela peut être un test fonctionnel et/ou un test unitaire. L'idéal est de créer un ou plusieurs tests avant d'implémenter une fonctionnalité ou de corriger un bug.

Nous utilisons le framework Cucumber pour les tests fonctionnels et privilégions les tests de fonctionnalités *end-to-end*. Nous utilisons les tests unitaires lorsque cela nous semble pertinent.
