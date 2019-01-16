## Execution

    mvn verify -pl tests/integration -Dexec.mainClass=org.hesperides.test.integration.CucumberIntegTests \
                                     -Dintegration-bdd.remote-base-url=http://localhost:8080/rest \
                                     -Dauth.lambdaUserName=... -Dauth.lambdaUserPassword=... \
                                     [-Dintegration-bdd.proxy-host=... -Dintegration-bdd.proxy-port=...]
