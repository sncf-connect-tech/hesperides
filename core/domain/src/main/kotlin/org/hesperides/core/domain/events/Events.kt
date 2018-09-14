package org.hesperides.core.domain.events

import org.hesperides.core.domain.platforms.entities.Platform
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Query
data class GenericEventsByStreamQuery(val eventStream: TemplateContainer.Key)
data class PlatformEventsByStreamQuery(val eventStream: Platform.Key)