package org.hesperides.core.domain.events

import org.hesperides.core.domain.security.UserEvent

data class GetEventsByAggregateIdentifierQuery(val aggregateIdentifier: String, val eventTypes: Array<Class<UserEvent>>)
data class CleanAggregateEventsQuery(val aggregateIdentifier: String)
