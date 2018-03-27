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

See branch `feature/springboot` for the new version under construction

See branch `master` for the production version 

:exclamation: :exclamation: :exclamation:

## Requirements

 * Java 8 (openjdk, sun)
 
 Choose between:

 * Docker (see docker-compose & Dockerfile files)

and

 * elasticSearch 1.7.5

 * redis 3.0.3

## Build

Build the whole project:
 
    mvnw package

Build Docker image

    docker build . -t hesperides/hesperides-spring

## Run

Some variables are set as environment variables:
* SPRING_PROFILES_ACTIVE

* LDAP_URL
* LDAP_DOMAIN
* LDAP_USER_SEARCH_BASE
* LDAP_USERNAME_ATTRIBUTE
* LDAP_CONNECT_TIMEOUT
* LDAP_READ_TIMEOUT

* ELASTICSEARCH_HOST
* ELASTICSEARCH_PORT
* ELASTICSEARCH_INDEX

* REDIS_HOST
* REDIS_PORT

See `boostrap/src/main/resources/application.yml`

Run Elasticsearch and Redis via Docker

    docker-compose -f docker-compose-dev.yml up -d

Run backend manually

    java -jar bootstrap/target/hesperides-spring.jar
    
Run backend using Docker

    docker run -d [-e ENV_VAR=ENV_VALUE] -p 8080:8080 --network hesperides_hesperides-network hesperides/hesperides-spring
    
Run without redis, ldap or elasticsearch

    java -jar bootstrap/target/hesperides-spring.jar -Dspring.profiles.active=noldap,local

## Documentation

Available online at <https://voyages-sncf-technologies.github.io/hesperides-gui/>

## License

Hesperides is licensed under the GPL V3 license

## Contributing

Do you have changes to contribute? Please see the [CONTRIBUTING](CONTRIBUTING.md) page.
We are open to pull requests. Please first discuss your intentions via [Issues](https://github.com/voyages-sncf-technologies/hesperides/issues).

This project includes a postman collection, check `documentation/postman` folder.
