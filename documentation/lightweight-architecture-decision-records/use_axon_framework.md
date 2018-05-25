# Why ?

Le framework [Axon](http://www.axonframework.org/) permet de gérer toute la plomberie eventsourcing / cqrs:

* event store
* transactions (détection de conflits)
* snapshots !

c'est clairement intéressant, on a pas envie de gérer ça à la main.

# Par où commencer ?

Tout d'abord, il faut avoir des notions sur les concepts d'Event Sourcing et CQRS.

Pour cela, on peut lire la doc Axon ici: https://docs.axonframework.org/part1/architecture-overview.html
Utile aussi: http://cqrs.nu/

Un fois qu'on à les bases, on peut commencer à coder: 

Chez spring, il y a un petit tutorial simple: http://www.baeldung.com/axon-cqrs-event-sourcing

Ensuite on passe au déploiement dans l'application.

# Spring autoconfigure

On va utiliser spring avec axon comme décrit dans la doc ici: https://docs.axonframework.org/part3/spring-boot-autoconfig.html

En gros:
* Les commandes, les aggrégats et les events font parties du domaine. On les déclare dans le module adhoc.
* Les eventbus, eventstore, etc. c'est de l'infrastructure. 

On va voir si on peut tuner chaque élément pour coller à l'existant hespéride.

## Query processing

On peut utiliser le framework pour faire les queries (la partie read de CQRS) faut voir comment ça marche.

# Migration des events actuels

Un problème qu'on va rapidement avoir sera la relecture des events legacy dans la nouvelle version.
cf. https://docs.axonframework.org/part3/repositories-and-event-stores.html#event-upcasting pour voir si ça peut aider.