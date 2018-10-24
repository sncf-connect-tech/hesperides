# xAlreadyExists(...)

Pour nos requêtes de vérification de l'existence d'une entité à partir de son identifiant, plutôt que de tenter de récupérer l'entité elle-même et de tester son existence, il est préférable de compter le nombre d'entités correspondant à cet identifiant.

Comme ceci :

    return xRepository.countByKey(key) > 0;

C'est une optimisation qui n'est pas négligeable étant donné le nombre de requêtes de ce type.
