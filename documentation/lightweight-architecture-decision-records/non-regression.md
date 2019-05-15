# Tests de non-régression

Nous avons mis en place les tests de non-régression dans le batch de migration
des données afin valider l'intégrité des données.

Le principe était d'appeler les endpoints GET du legacy et de la refonte,
et de comparer les résultats afin d'en sortir des diffs.

Nous souhaitons inclure ces tests dans la refonte et pouvoir les déclencher ponctuellement. 

Le but est de les déclencher lorsqu'on modifie, comme c'est prévu, le coeur de
l'application existante. Par exemple, quand on touchera au modèle de propriétés.

## Spécifications

Le principe reste le même : comparer les résultats de la version modifiée avec ceux de la production.

* Rendre configurable les URL des endpoints à appeler (INT1 et REL1)
* Permettre de lancer les tests sur tous les endpoints GET ou uniquement sur les fichiers
* Afficher un rapport de diffs pour faciliter leur analyse à la fin de l'exécution

Récupérer la liste des technos
Pour chaque techno, appeler chaque endpoint et comparer le résultat

Import des données sur la version modifiée ou blue-green