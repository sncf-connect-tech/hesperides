package org.hesperides.core.domain.security;


import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * représente un utilisateur d'hesperide.
 */
@Value
public class User {

    String name;
    boolean isGlobalProd;
    boolean isGlobalTech;
    List<String> authorizations;

    public static User fromAuthentication(Authentication authentication) {
        return new User(authentication.getName(),
                isGlobalProd(authentication.getAuthorities()),
                isGlobalTech(authentication.getAuthorities()),
                getAuthorizations(authentication));
    }

    private static List<String> getAuthorizations(Authentication authentication) {
        return null;
    }

    private static boolean isGlobalProd(Collection<? extends GrantedAuthority> authorities) {
        return hasAuthority(authorities, UserRole.GLOBAL_IS_PROD);
    }

    private static boolean isGlobalTech(Collection<? extends GrantedAuthority> authorities) {
        return hasAuthority(authorities, UserRole.GLOBAL_IS_TECH);
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
