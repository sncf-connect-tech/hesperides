# Pourquoi Kotlin ?

Le framework Axon nécessite de créer une classe par Command/Event/Query.

Ces classes ne contiennent que des propriétés, ce qui fait beaucoup de classes pour pas grand chose,
même avec l'utilisation de Lombok.

Kotlin permet de palier à ça, sans ajouter de complexité (juste une nouvelle syntaxe).
Pour l'instant, on ne l'utilise que pour ces 3 cas (Command/Event/Query), mais à voir pour les Exception.