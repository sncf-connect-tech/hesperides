# Hesperides backend

[![Build, test and publish](https://github.com/voyages-sncf-technologies/hesperides/workflows/Build,%20test%20and%20publish/badge.svg)](https://github.com/voyages-sncf-technologies/hesperides/actions?query=branch%3Amaster)

[![](https://img.shields.io/github/contributors/voyages-sncf-technologies/hesperides.svg)](https://github.com/voyages-sncf-technologies/hesperides/graphs/contributors)
[![](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)](http://makeapullrequest.com)
[![first-timers-only Friendly](https://img.shields.io/badge/first--timers--only-friendly-blue.svg)](http://www.firsttimersonly.com/)
-> come look at
our [good first issues](https://github.com/voyages-sncf-technologies/hesperides/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22)

[![](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Hesperides is an open source tool generating configuration files from a given template with [mustaches](https://mustache.github.io)
and the properties it stores per environment.

The frontend lives in this repo: https://github.com/voyages-sncf-technologies/hesperides-gui

**Development status**: this project is currently maintained by [e-Voyageurs SNCF](https://fr.wikipedia.org/wiki/E.Voyageurs_SNCF).


## Live demo
<https://hesperides-back.herokuapp.com/rest/swagger-ui.html>

Credentials for the Basic Auth:

* **Username**: `user` or `prod`
* **Password**: `password`


## To test the app locally

    docker-compose -f docker/docker-compose.yml -f docker/docker-compose-mongo.yml up -d

## Requirements

 * Java 11 (correto, openjdk)
 
 Choose between:

 * Docker (see docker-compose & Dockerfile files)
 
 And :
 
 * MongoDB

Or just launch the application with those Spring Profiles: `noldap`, `fake_mongo`

## Build

Build the whole project:
 
    mvn package

Build Docker image

    docker build . -t hesperides/hesperides
    
This Java [Spring Boot](https://spring.io/projects/spring-boot) application uses [MongoDB](https://www.mongodb.com) for storage
and [Axon](https://axoniq.io) to implement event sourcing.

![Logo Spring Boot](documentation/architecture/images/spring-boot-logo.png)
![Logo MongoDB](documentation/architecture/images/mongodb-logo.png)
![Logo Axon](documentation/architecture/images/axon-iq-logo.png)

## Documentation

Available online at <https://voyages-sncf-technologies.github.io/hesperides-gui/>

### Changelog
All the last features & fixes are listed there: [CHANGELOG.md](https://github.com/voyages-sncf-technologies/hesperides/blob/master/CHANGELOG.md).

## License

Hesperides is licensed under the GPL V3 license

## Contributing & development environment installation

Please check the dedicated [CONTRIBUTING](CONTRIBUTING.md) page.
We are open to pull requests. Please first discuss your intentions via [Issues](https://github.com/voyages-sncf-technologies/hesperides/issues).
