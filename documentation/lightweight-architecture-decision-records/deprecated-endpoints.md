# Points d'accès dépréciés

Nous mettons à jour l'API petit à petit. Cela implique parfois de déprécier
certains endpoints afin de les remplacer sans les supprimer.

Nous tenons par la même occasion à prévenir les différents clients qui
appellent l'API. Pour cela, nous utilisons des en-têtes de réponse HTTP
définies selon le standard RFC : https://tools.ietf.org/html/draft-dalal-deprecation-header-00

Exemple :

    return ResponseEntity.ok()
        .header("Deprecation", "version=\"2019-04-23\"")    // Version du build
        .header("Sunset", "Wed Apr 24 00:00:00 CEST 2020")  // Date de suppression du endpoint
        .header("Link", "/nouveau-endpoint")                // Où trouver la nouvelle ressource
        .body(...);                                         // Corps de la réponse
