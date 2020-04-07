package org.hesperides.core.domain.events

import org.hesperides.core.domain.security.UserEvent

data class GetLastToFirstEventsQuery(val aggregateIdentifier: String, val eventTypes: Array<Class<UserEvent>>, val page: Integer, val size: Integer)
data class GetLastToFirstPlatformModulePropertiesUpdatedEvents(val aggregateIdentifier: String, val propertiesPath: String, val page: Integer, val size: Integer)
