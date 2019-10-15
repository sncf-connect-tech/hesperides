# Identifiants d'agrégats

Les agrégats Axon sont identifiés par un attribut ayant l'annotation `@AggregateIdentifier` qui est unique et lui permet de les distinguer.

Lors de la refonte, nous avons dans un premier temps décidé de l'appliquer sur la clé fonctionnelle (l'objet `Key`) des technos, modules et plateformes.

L'application existante permettant de supprimer une entité puis de la créer à nouveau en utilisant la même clé, nous avons résolu cette problématique en utilisant un identifiant généré aléatoirement (`UUID`).

Mais un bug ([#779](https://github.com/voyages-sncf-technologies/hesperides/issues/779)) a révélé que cela permettait la création de doublons lorsque plusieurs requêtes de création identiques était déclenchées au même moment.

La solution a été d'utiliser un hash de la clé fonctionnelle comme identifiant d'agrégat et de supprimer les événements passés de cet agrégrat lorsqu'il est *recréé*.