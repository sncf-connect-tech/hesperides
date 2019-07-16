package org.hesperides.core.domain.security


/**
 * tout les events de l'application sont déclenchés par un utilisateur.
 *
 * Rendre cette classe abstraite pose des problèmes de deserialization par Axon
 * de org.hesperides.core.domain.events.queries.EventView
 *
 * On marque le champ user @Transient pour éviter des erreurs de champ dupliqué
 * lors de la déserialisation via Gson des events
 */
open class UserEvent(@Transient open val user: String)

data class GetUserQuery(val username: String)