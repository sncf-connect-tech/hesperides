# Endpoints spring-actuator de diagnostic

Ils incluent (mais ne se limitent pas à) :

* https://hesperides-back.herokuapp.com/rest/versions → toutes les infos sur la version du code déployée
* https://hesperides-back.herokuapp.com/rest/manage/configprops
* https://hesperides-back.herokuapp.com/rest/manage/env
* https://hesperides-back.herokuapp.com/rest/manage/health (inclus le healthcheck MongoDB)
* https://hesperides-back.herokuapp.com/rest/manage/heapdump & https://hesperides-back.herokuapp.com/rest/manage/threaddump
* https://hesperides-back.herokuapp.com/rest/manage/httptrace
* https://hesperides-back.herokuapp.com/rest/manage/loggers
* https://hesperides-back.herokuapp.com/rest/manage/metrics
    + Cache users-authentication
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.size?tag=name:users-authentication
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.puts?tag=name:users-authentication
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.evictions?tag=name:users-authentication
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.gets?tag=name:users-authentication&tag=result:hit
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.gets?tag=name:users-authentication&tag=result:miss
    + Cache authorization-groups-tree
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.size?tag=name:authorization-groups-tree
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.puts?tag=name:authorization-groups-tree
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.evictions?tag=name:authorization-groups-tree
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.gets?tag=name:authorization-groups-tree&tag=result:hit (ici l'unité est une mesure de taille mémoire, en octets je pense, sachant qu'un LdapGroupAuthority en cache = 120)
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/cache.gets?tag=name:authorization-groups-tree&tag=result:miss
    + "Cache metrics "custom" to Hesperides
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/totalCallsCounter
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/failedCallsCounter
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/unexpectedExceptionCounter
        - https://hesperides-back.herokuapp.com/rest/manage/metrics/retriesExhaustedExceptionCounter
