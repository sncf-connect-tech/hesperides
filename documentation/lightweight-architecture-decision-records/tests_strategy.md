# Notre stratégie de tests

Il y a 4 niveaux de tests automatisés :
* Tests unitaires
* Tests end-to-end
* Tests d'intégration
* Tests de non régression

Les tests unitaires sont utiles dans des cas particuliers comme, typiquement, le mapping entre les évènements de la nouvelle application et ceux de l'ancienne application (stockés dans le Redis). Ce sont des tests JUnit classiques.

Les tests end-to-end et d'intégration ont la même base de tests. La seule différence est que la couche infrastructure est mockée pour les tests end-to-end, contrairement aux tests d'intégration. Cette base de tests est rédigée sous forme de tests fonctionnels à l'aide du framework Cucumber.

Les tests end-to-end couvrent l'application à partir de l'appel d'un endpoint jusqu'à la couche infrastructure. La couche infrastructure est mockée. Cette partie de l'application est couverte par certains TU et par les tests d'intégration.

Les tests d'intégration sont faits sur une plateforme d'intégration et couvrent l'application dans son intégralité, incluant les partenaires externes dont Redis et Elasticsearch.

Les tests de non-régression s'effectuent sur une plateforme à l'extérieur de VSCT (par exemple Heroku) et en mode boîte noire.

*Dans le but d'avoir une approche BDD, nous prévoyons de rédiger les tests fonctionnels correspondant à chaque fonctionnalité, avant leur implémentation.*