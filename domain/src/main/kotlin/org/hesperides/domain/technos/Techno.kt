package org.hesperides.domain.technos

import org.hesperides.domain.security.User
import org.hesperides.domain.security.UserEvent
import org.hesperides.domain.technos.entities.Techno

// Command
data class CreateTechnoCommand(val techno: Techno, val user: User)

data class TechnoCreatedEvent(val techno: Techno, override val user: User) : UserEvent(user)