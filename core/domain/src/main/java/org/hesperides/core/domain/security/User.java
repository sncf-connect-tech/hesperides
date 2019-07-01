package org.hesperides.core.domain.security;


import lombok.Value;
import org.hesperides.core.domain.security.authorities.ActiveDirectoryGroup;
import org.hesperides.core.domain.security.authorities.ApplicationRole;
import org.hesperides.core.domain.security.authorities.GlobalRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * représente un utilisateur d'hesperide.
 */
@Value
public class User {

    private static final String KEY_ROLE = "role";
    private static final String KEY_GROUP = "groupCN";

    String name;
    boolean isGlobalProd;
    boolean isGlobalTech;
    Map<String, String> customAuthorities;

    public static User fromAuthentication(Authentication authentication) {
        final Collection<? extends GrantedAuthority> springAuthorities = authentication.getAuthorities();
        return new User(authentication.getName(),
                isGlobalProd(springAuthorities),
                isGlobalTech(springAuthorities),
                getCustomAuthorities(springAuthorities));
    }

    /**
     * Retourne les "authorities" sous forme de Map :
     * [
     * {"role": "GLOBAL_IS_PROD"},
     * {"role": "ABC_PROD_USER"},
     * {"role": "DEF_PROD_USER"},
     * {"groupCN": "GG_XX"},
     * {"groupCN": "GG_YY"}
     * ]
     */
    private static Map<String, String> getCustomAuthorities(Collection<? extends GrantedAuthority> springAuthorities) {
        Map<String, String> authorities = new HashMap<>();
        if (springAuthorities != null) {
            springAuthorities.forEach(springAuthority -> {
                if (springAuthority instanceof GlobalRole || springAuthority instanceof ApplicationRole) {
                    authorities.put(KEY_ROLE, springAuthority.getAuthority());
                } else if (springAuthority instanceof ActiveDirectoryGroup) {
                    authorities.put(KEY_GROUP, springAuthority.getAuthority());
                }
            });
        }
        return authorities;
    }

    private static boolean isGlobalProd(Collection<? extends GrantedAuthority> authorities) {
        return hasGlobalAuthority(authorities, GlobalRole.IS_PROD);
    }

    private static boolean isGlobalTech(Collection<? extends GrantedAuthority> authorities) {
        return hasGlobalAuthority(authorities, GlobalRole.IS_TECH);
    }

    private static boolean hasGlobalAuthority(Collection<? extends GrantedAuthority> authorities, String userRole) {
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
