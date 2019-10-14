package org.hesperides.core.domain.events

data class GenericEventsByStreamQuery(val aggregateIdentifier: String)
data class CleanAggregateEventsQuery(val aggregateIdentifier: String)
