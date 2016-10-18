# Scripts pour hesperides

Ensemble de scripts pour interroger hesperides.

Les scripts groovy peuvent être lancés de deux façons:

1. `$ ./export.groovy [options]` . Ca part de l'hypothèse que groovy est accessible dans le path du shell.
1. `$ ${GROOVY_HOME}/bin/groovy export.groovy [options]`.

## Export

Le script `export.groovy` interroge hesperides pour avoir la liste des fichiers pour une instance donnée et les export
en `scp` sur le serveur cible.

Les options possibles:

```prompt

$ ./export.groovy -h

usage: ./export.groovy [options]
 -application <application name>          ex. UNI.
 -dry                                     dry run
 -h                                       help
 -hes,--hesperides-url <hesperides url>   Ex. http://daxmort:50000. Default system property: HESPERIDES_URL
 -instance <instance>                     name of the instance stored in hesperides. Ex. UNIFIRE11REA
 -key,--key-ssh <ssh key>                 path to private ssh key. Ex. /path/to/.ssh/id_rsa . Default system property:
                                          SSH_KEY_PATH
 -platform <platform>                     version of the platform stored in hesperides. Ex. performance1
 -ssh,--connexion-ssh <ssh connexion>     connexion ssh to use (ex. user@host)
 -unit <unit>                             name of the unit stored in hesperides. Ex. Realtime
 -v,--verbose                             verbose
 -version <version>                       version of the application in hesperides (not the application number). Ex. 1.0
```

Certaines options peuvent être passées en variables systèmes (`export VARIABLE=...`).