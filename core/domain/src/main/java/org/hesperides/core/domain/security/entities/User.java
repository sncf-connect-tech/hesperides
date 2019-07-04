package org.hesperides.core.domain.security.entities;


import lombok.Value;
import org.hesperides.core.domain.security.entities.authorities.ActiveDirectoryGroup;
import org.hesperides.core.domain.security.entities.authorities.ApplicationRole;
import org.hesperides.core.domain.security.entities.authorities.GlobalRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * représente un utilisateur d'hesperide.
 */
@Value
public class User {

    String name;
    boolean isGlobalProd;
    boolean isGlobalTech;
    List<String> roles;
    List<String> groupAuthorities;

    public static User fromAuthentication(Authentication authentication) {
        final Collection<? extends GrantedAuthority> springAuthorities = authentication.getAuthorities();
        return new User(authentication.getName(),
                isGlobalProd(springAuthorities),
                isGlobalTech(springAuthorities),
                getRoles(springAuthorities),
                getGroupAuthorities(springAuthorities));
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

    private static List<String> getRoles(Collection<? extends GrantedAuthority> springAuthorities) {
        return springAuthorities.stream()
                .filter(springAuthority -> springAuthority instanceof GlobalRole || springAuthority instanceof ApplicationRole)
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private static List<String> getGroupAuthorities(Collection<? extends GrantedAuthority> springAuthorities) {
        return springAuthorities.stream()
                .filter(springAuthority -> springAuthority instanceof ActiveDirectoryGroup)
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public boolean hasAtLeastOneAuthority(Map<String, List<String>> authorities) {
        boolean hasAuthority = false;
        if (!CollectionUtils.isEmpty(authorities)) {
            List<String> flatAuthorities = authorities.values().stream().flatMap(List::stream).collect(Collectors.toList());
            hasAuthority = !Collections.disjoint(flatAuthorities, this.getGroupAuthorities());
        }
        return hasAuthority;
    }
}
