Those tests require a real MongoDB instance **and** an external Hesperides instance to connect to.

They use the BDD _features_ defined in the `tests/bdd` module.

## Execution

    mvn verify -pl tests/mongo-integration -Dexec.mainClass=org.hesperides.test.mongo_integration.CucumberMongoIntegTests \
                                           -Dmongo-integration.remote-base-url=http://localhost:8080/rest \
                                           -Dauth.lambdaUserName=... -Dauth.lambdaUserPassword=... \
                                           [-Dmongo-integration.proxy-host=... -Dmongo-integration.proxy-port=...]
