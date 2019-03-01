# Stress testing scenarios for Hesperides API

## Utilisation

    mvn gatling:execute -Pgatling -Dgatling.simulationClass=HesperidesApi -Dauth=tech:password -DusersPerSecond=4 -Dduration=10seconds

## Options

    -DbaseUrl=http://localhost:8080/rest            URL du serveur à tester
    -DusersPerSecond=1                              Nombre d'utilisateurs à injecter par seconde
    -Dduration=1second                              Durée de la simulation
    -Dverbose=true                                  Activation des io sur certains appels
    -DpercentOkMin=99                               % d'appels qui doivent être OK pour que le test soit un succès
    -Dpercentile99ResponseTimeMax=500               temps de réponse max acceptable pour l'ensemble des 1% des requêtes les plus lentes

## Trace
Pour avoir les log des requests, modifier le `resources/logback.xml`

     <!-- Uncomment for logging ALL HTTP request and responses -->
     <!--<logger name="io.gatling.http" level="TRACE" />   -->
     <!-- Uncomment for logging ONLY FAILED HTTP request and responses -->
     <!--<logger name="io.gatling.http" level="DEBUG" />-->

Les log sont envoyés sur la sortie standard (appender file pas activé)
