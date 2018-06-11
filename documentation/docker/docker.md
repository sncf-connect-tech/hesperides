# Docker

Hespérides est dockerisée à chaque build via [Travis](https://travis-ci.org/voyages-sncf-technologies/hesperides/) et pushée sur le [hub Docker](https://hub.docker.com/r/hesperides).
Les noms des tags des images docker correspondent aux noms des branches sur GitHub.


# Dockerfile

Pour construire une image docker pour Hespérides nous utilisons un fichier de type `Dockerfile`.

Ce fichier indique :
- l'image de base : `openjdk:8-jre-alpine`
- nous ajoutons notre jar construit suite au build maven
- la commande pour lancer la jar
- nous exposons les ports d'écoutés par le jar


# Docker Compose

Chaque partenaire externe a son fichier docker-compose dans le dossier `docker` permettant de se lancer en local :

    docker-compose -f [docker-compose-file] up
