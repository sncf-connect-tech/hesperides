package org.hesperides.core.domain.security;


import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

/**
 * représente un utilisateur d'hesperide.
 */
@Value
public class User {

    String name;
    boolean isGlobalProd;
    boolean isGlobalTech;
    Map<String, String> authorities;

    public static User fromAuthentication(Authentication authentication) {
        return new User(authentication.getName(),
                isGlobalProd(authentication.getAuthorities()),
                isGlobalTech(authentication.getAuthorities()),
                getAuthorities(authentication));
    }

    private static Map<String, String> getAuthorities(Authentication authentication) {
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
