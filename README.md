[![](https://travis-ci.org/voyages-sncf-technologies/hesperides.svg?branch=master)](https://travis-ci.org/voyages-sncf-technologies/hesperides)

Backend Hesperides
========

Hesperides is an open source tool with a frontend (hesperides-gui) and a backend (hesperides).

It lets you easily generate content from a template file (using mustache) in a given environment.

Go to https://github.com/voyages-sncf-technologies/hesperides-gui to handle hesperides frontend.

Requirements:
=====

 * Docker

or :

 * Java 8 (openjdk, sun)

 * elasticSearch 1.7.5

 * redis 3.0.3

Build:
=====

Build the whole project :
```shell
$ mvn package
```

Run:
=====

Init hesperides configuration :
```shell
$ cp hesperides.yml.sample hesperides.yml
```
(Config file is ready to be used, be careful of the "overrides" value to point on the right path)

Run the backend :
```shell
$ java -jar hesperides-1.0.0-SNAPSHOT.jar server PATH_TO_YOUR_HESPERIDES_YML_FILE
```

Run integration test for cache
=====

To check if new cache working fine, run integration test.

Set environment variables:
```
HESPERIDES_USER=<your_ldap_username>
HESPERIDES_PASS=<your_ldap_password>
HESPERIDES_URL=<e.g. http://localhost:8080>
REDIS_URL=<e.g. 192.168.2.3:2600>
REDIS_CACHE_URL=<e.g. 192.168.2.59:3000>
```

Then run test class `integration.IntegrationTest`

Documentation:
=====

Available online at <https://voyages-sncf-technologies.github.io/hesperides-gui/>

Development:
=====

Do you have changes to contribute? Please see the Development page.
