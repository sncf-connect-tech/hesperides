# Tests de non-régression

Nous avons mis en place les tests de non-régression dans le batch de migration
des données afin valider la rétrocompatibilité de la refonte.

Le principe était d'appeler les endpoints GET du legacy et de la refonte,
et de comparer les résultats afin d'en sortir des diffs.

Nous souhaitons inclure ces tests dans la refonte et pouvoir les déclencher ponctuellement. 

Le but est de les déclencher lorsqu'on modifie, comme c'est prévu, le coeur de
l'application existante. Par exemple, quand on touchera au modèle de propriétés.

## Spécifications

Le principe reste le même : comparer les résultats des endpoints GET sur les technos,
les modules et les plateformes entre la version la version en test et la version de production.
