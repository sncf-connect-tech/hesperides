package org.hesperides.core.domain.events

import org.hesperides.core.domain.platforms.entities.Platform

// Query

data class GenericEventsByStreamQuery(val eventStream: String)
data class PlatformEventsByStreamQuery(val eventStream: Platform.Key)