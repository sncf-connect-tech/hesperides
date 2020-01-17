package org.hesperides.core.domain.events

import org.hesperides.core.domain.security.UserEvent

data class GenericEventsByStreamQuery(val aggregateIdentifier: String, val typeFilter: Array<Class<UserEvent>>)
data class CleanAggregateEventsQuery(val aggregateIdentifier: String)
