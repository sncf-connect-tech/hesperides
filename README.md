# Hesperides Back

[![](https://travis-ci.org/voyages-sncf-technologies/hesperides.svg?branch=feature/springboot)](https://travis-ci.org/voyages-sncf-technologies/hesperides)

[![](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)](http://makeapullrequest.com)
[![](https://img.shields.io/github/issues/voyages-sncf-technologies/hesperides.svg)](https://github.com/voyages-sncf-technologies/hesperides/issues)
[![](https://img.shields.io/github/contributors/voyages-sncf-technologies/hesperides.svg)](https://img.shields.io/github/contributors/voyages-sncf-technologies/hesperides.svg)
[![](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Hesperides is an open source tool generating content from a template file (using mustache) in a given environment.

Go to https://github.com/voyages-sncf-technologies/hesperides-gui to handle hesperides frontend.

:exclamation: :exclamation: :exclamation:

**The project is currently being rework**

See branch `develop` for the new version under construction

See branch `master` for the production version 

:exclamation: :exclamation: :exclamation:

## Installation

Pour lancer l'application sur un poste de dev, nous utilisons IntelliJ Ultimate Edition et les plugins suivants :

* Maven
* Kotlin
* Lombok
* Spring Boot
* Spring Data
* Spring MVC
* Spring Security
* Cucumber for Java

Vous pourrez alors lancer l'application avec les profils Spring **noldap** et **fake_mongo**.


*Nous avons rencontré un problème qui empêche l'application de démarrer : le bouton Run se grise après avoir cliqué dessus sans que rien ne se passe. Il semblerait que le problème vienne du plugin Gradle, il suffit de le désactiver. Si le problème persiste, il faut désactiver tous les plugins d'IntelliJ, et ne réactiver que ceux listés ci-dessus (en acceptant leurs dépendences).*


*Cela semble être un problème de compatibilité de plugins, espérons temporaire.*

## Requirements

 * Java 8 (openjdk, sun)
 
 Choose between:

 * Docker (see docker-compose & Dockerfile files)
 
 And :
 
 * MongoDB

Or just launch the application with those Spring Profiles: noldap, fake_mongo

## Build

Build the whole project:
 
    mvnw package

Build Docker image

    docker build . -t hesperides/hesperides

## Run

Some variables are set as environment variables:
* SPRING_PROFILES_ACTIVE

* LDAP_URL
* LDAP_DOMAIN
* LDAP_USER_SEARCH_BASE
* LDAP_USERNAME_ATTRIBUTE
* LDAP_CONNECT_TIMEOUT
* LDAP_READ_TIMEOUT

* MONGO_HOST
* MONGO_PORT

* SPRING_BOOT_ADMIN_URL

See `boostrap/src/main/resources/application.yml`

Run backend via Docker

    docker-compose -f docker-compose-[BACKEND].yml up -d

Run backend manually

    java -jar bootstrap/target/hesperides.jar
    
Run backend using Docker

    docker run -d [-e ENV_VAR=ENV_VALUE] -p 8080:8080 --network hesperides_hesperides-network hesperides/hesperides
    
Run without ldap or mongodb

    java -jar bootstrap/target/hesperides.jar -Dspring.profiles.active=noldap,fake_mongo

## Documentation

Available online at <https://voyages-sncf-technologies.github.io/hesperides-gui/>

## License

Hesperides is licensed under the GPL V3 license

## Contributing

Do you have changes to contribute? Please see the [CONTRIBUTING](CONTRIBUTING.md) page.
We are open to pull requests. Please first discuss your intentions via [Issues](https://github.com/voyages-sncf-technologies/hesperides/issues).

This project includes a postman collection, check `documentation/postman` folder.
