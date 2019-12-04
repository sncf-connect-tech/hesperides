package org.hesperides.core.domain.keyvalues

import org.axonframework.commandhandling.TargetAggregateIdentifier
import org.hesperides.core.domain.keyvalues.entities.KeyValue
import org.hesperides.core.domain.security.UserEvent
import org.hesperides.core.domain.security.entities.User

// Command

data class CreateKeyValueCommand(val keyValue: KeyValue, val user: User)
data class UpdateKeyValueCommand(@TargetAggregateIdentifier val id: String, val keyValue: KeyValue, val user: User)
data class DeleteKeyValueCommand(@TargetAggregateIdentifier val id: String, val user: User)

// Event

data class KeyValueCreatedEvent(val id: String, val keyValue: KeyValue, override val user: String) : UserEvent(user)
data class KeyValueUpdatedEvent(val id: String, val keyValue: KeyValue, override val user: String) : UserEvent(user)
data class KeyValueDeletedEvent(val id: String, override val user: String) : UserEvent(user)

// Query

data class KeyValueExistsQuery(val id: String)
data class GetKeyValueQuery(val id: String)
