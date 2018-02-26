# Hesperides Back

[![](https://travis-ci.org/voyages-sncf-technologies/hesperides.svg?branch=feature/springboot)](https://travis-ci.org/voyages-sncf-technologies/hesperides)

Hesperides is an open source tool with a frontend (hesperides-gui) and a backend (hesperides).

It lets you easily generate content from a template file (using mustache) in a given environment.

Go to https://github.com/voyages-sncf-technologies/hesperides-gui to handle hesperides frontend.

## Requirements

 * Java 8 (openjdk, sun)
 
 Choose between

 * Docker (see docker-compose files inside `docker` folder)

or

 * elasticSearch 1.7.5

 * redis 3.0.3

## Build

Build the whole project:
 
    mvn package


## Run

Run the backend

    java -jar hesperides.jar


## Documentation

Available online at <https://voyages-sncf-technologies.github.io/hesperides-gui/>

## Development

Do you have changes to contribute? Please see the Development page.

This project includes a postman collection, check `documentation/postman` folder.
