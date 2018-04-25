package org.hesperides.domain.security;


import lombok.Value;

import java.security.Principal;

/**
 * représente un utilisateur d'hesperide.
 */
@Value
public class User {
    String name;

    /**
     * @param currentUser le currentUser tel que la plateforme (i.e. tomcat) nous la renvoie
     * @return un objet User.
     */
    public static User fromPrincipal(Principal currentUser) {
        return new User(currentUser.getName());
    }
}
