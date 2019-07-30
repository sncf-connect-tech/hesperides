package org.hesperides.core.domain.security

// Tous les événements de l'application sont déclenchés par un utilisateur.
// Rendre cette classe abstraite pose des problèmes de désérialisation de
// la classe `org.hesperides.core.domain.events.queries.EventView` par Axon.
// On marque le champ user `@Transient` pour éviter des erreurs de champ
// dupliqué lors de la désérialisation via Gson des événements.
open class UserEvent(@Transient open val user: String)

// Command

// Event

// Queries

data class GetUserQuery(val username: String)
