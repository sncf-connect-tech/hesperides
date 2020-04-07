package org.hesperides.core.domain.events

import org.hesperides.core.domain.security.UserEvent

data class GetLastToFirstEventsQuery(val aggregateIdentifier: String, val eventTypes: Array<Class<UserEvent>>, val page: Int, val size: Int)
data class GetLastToFirstPlatformModulePropertiesUpdatedEvents(val aggregateIdentifier: String, val propertiesPath: String, val page: Int, val size: Int)
