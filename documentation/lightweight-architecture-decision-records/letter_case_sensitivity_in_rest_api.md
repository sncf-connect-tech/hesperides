# Casse des identifiants dans l'API REST

Voici les règles en vigueur en termes de casse des identifants (d'application, module, template...) :
- lors de création (`POST`) d'entités, Hesperides est **sensible** à la casse,
mais interdit la création d'entité ayant un nom identique mais une casse différente
- lors de consultation (`GET`), modification (`PUT`) ou suppression (`DELETE`) d'entités, Hesperides est **insensible** à la casse.

## Implémentation

Pour mettre cela en place techniquement, nous avons opté pour mettre en place des [collations](https://docs.mongodb.com/manual/reference/collation/)
sur les collections MongoDB concernées, afin de rendre leurs champs `key` insensible à la casse.

Comme `spring-data-mongodb` ne fournit aucune moyen d'affiner la création des collections,
ou de l'effectuer manuellement, nous nous sommes résolu à effectuer la création des collections **avant** le lancement de l'application,
dans un script d'[_entrypoint_ Docker](/docker_entrypoint.sh).

## Tests

Comme les tests BDD validant ce fonctionnement dépendent de l'utilisation d'un serveur MongoDB (
nous avons tagué ces tests en `@integ-test-only`, et ils ne sont pas exécuté avec les autres tests Cucumber
en mode "bouchonné" (avec `mongo-java-server`), uniquement en mode "tests d'intégration".
