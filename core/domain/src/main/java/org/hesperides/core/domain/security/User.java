package org.hesperides.core.domain.security;


import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * représente un utilisateur d'hesperide.
 */
@Value
public class User {

    String name;
    boolean isProd;
    boolean isTech;

    public static User fromAuthentication(Authentication authentication) {
        return new User(authentication.getName(), isProd(authentication.getAuthorities()), isTech(authentication.getAuthorities()));
    }

    private static boolean isProd(Collection<? extends GrantedAuthority> authorities) {
        return hasAuthority(authorities, UserRole.PROD);
    }

    private static boolean isTech(Collection<? extends GrantedAuthority> authorities) {
        return hasAuthority(authorities, UserRole.TECH);
    }

    private static boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String userRole) {
        boolean hasAuthority = false;
        if (authorities != null && userRole != null) {
            for (GrantedAuthority authority : authorities) {
                if (userRole.equalsIgnoreCase(authority.getAuthority())) {
                    hasAuthority = true;
                    break;
                }
            }
        }
        return hasAuthority;
    }
}
