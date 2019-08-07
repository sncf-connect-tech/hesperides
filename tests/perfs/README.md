# Stress testing scenarios for Hesperides API

## Utilisation

    mvn gatling:test -Pgatling -Dgatling.simulationClass=HesperidesApi -Dduration=20seconds -Dpercentile99ResponseTimeMax=12000

## Options

    -DbaseUrl=http://localhost:8080/rest            URL du serveur à tester
    -Dauth=tech:password                            Identifiants de connexion
    -DusersPerSecond=5                              Nombre d'utilisateurs à injecter par seconde
    -Dduration=60seconds                            Durée de la simulation
    -DpercentOkMin=99                               % d'appels qui doivent être OK pour que le test soit un succès
    -Dpercentile99ResponseTimeMax=5000              temps de réponse max acceptable pour l'ensemble des 1% des requêtes les plus lentes
    -Dverbose=true                                  Activation des io sur certains appels

## Trace
Pour avoir les log des requests, modifier le `resources/logback.xml`

     <!-- Uncomment for logging ALL HTTP request and responses -->
     <!--<logger name="io.gatling.http" level="TRACE" />   -->
     <!-- Uncomment for logging ONLY FAILED HTTP request and responses -->
     <!--<logger name="io.gatling.http" level="DEBUG" />-->

Les log sont envoyés sur la sortie standard (appender file pas activé)
