En regardant l'application legacy, on constate qu'elle utilise un système d'event sourcing / cqrs.

Le problème est que le code est complètement mélangé. 
Les informations/concepts "métiers" sont noyés dans du bruit technique (cache, virtual...)

Il a donc été décidé de refondre l'application en appliquant les principes du DDD

Par contre on peut également conserver le concept d'event sourcing, d'autant plus qu'on veut garder la base iso existant.

Pour simplifier l'implémentation event sourcing, on pourra se tourner vers un framework adhoc: https://docs.axonframework.org/v/3.1/part3/spring-boot-autoconfig.html
 