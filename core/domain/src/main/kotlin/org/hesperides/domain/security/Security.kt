package org.hesperides.domain.security

/**
 * tout les events de l'application sont déclenchés par un utilisateur.
 */
open class UserEvent(open val user: User) {
}