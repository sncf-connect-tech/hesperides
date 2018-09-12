package org.hesperides.core.domain.security

/**
 * tout les events de l'application sont déclenchés par un utilisateur.
 */
abstract class UserEvent(user: User) {
    abstract val user: User
}