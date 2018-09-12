package org.hesperides.core.domain.events

import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer

// Query
data class EventsByStreamQuery(val eventStream: TemplateContainer.Key)