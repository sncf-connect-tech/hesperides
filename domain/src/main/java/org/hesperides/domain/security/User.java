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
     * @param principal le principal tel que la plateforme (i.e. tomcat) nous la renvoi
     * @return un objet User.
     */
    public static User fromPrincipal(Principal principal) {
        return new User(principal.getName());
    }
}
