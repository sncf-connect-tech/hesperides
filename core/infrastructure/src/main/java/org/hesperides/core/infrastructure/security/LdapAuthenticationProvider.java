/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.infrastructure.security;

import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import com.google.gson.Gson;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.hesperides.core.domain.security.AuthorizationProjectionRepository;
import org.hesperides.core.domain.security.entities.springauthorities.ApplicationProdRole;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;
import org.hesperides.core.domain.security.entities.springauthorities.GlobalRole;
import org.hesperides.core.infrastructure.security.groups.CachedParentLdapGroupAuthorityRetriever;
import org.hesperides.core.infrastructure.security.groups.LdapSearchContext;
import org.hesperides.core.infrastructure.security.groups.LdapSearchMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hesperides.commons.SpringProfiles.LDAP;
import static org.hesperides.core.infrastructure.security.groups.LdapSearchContext.attributesToNative;
import static org.hesperides.core.infrastructure.security.groups.LdapSearchContext.extractDirectParentGroupDNs;

@Profile(LDAP)
@Component
@Slf4j
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider implements AuthenticationProvider, LdapCNSearcher {

    private static final String USERS_AUTHENTICATION_CACHE_NAME = "users-authentication";
    private static final String AUTHORIZATION_GROUPS_TREE_CACHE_NAME = "authorization-groups-tree";

    @Value("${ldap.max-number-of-tries}")
    Integer maxNumberOfTries;
    @Value("${ldap.delay-between-tries-in-seconds}")
    Long delayBetweenTriesInSeconds;
    private RetryConfig retryConfig;
    @Resource
    private LdapCNSearcher self; // On passe par un attribut pour que le cache fonctionne, cf. https://stackoverflow.com/a/48867068/636849
    @Autowired
    private Gson gson; // nécessaire uniquement pour les logs DEBUG
    @Autowired
    private LdapConfiguration ldapConfiguration;
    @Autowired
    private AuthorizationProjectionRepository authorizationProjectionRepository;
    @Autowired
    private MeterRegistry meterRegistry;
    private LdapSearchMetrics ldapSearchMetrics;
    @Autowired
    private CacheManager cacheManager;
    // Pour débuguer le contenus des caches:
    // Evaluate Expression: cacheManager.getEhcache(USERS_AUTHENTICATION_CACHE_NAME);
    // Evaluate Expression: cacheManager.getEhcache(AUTHORIZATION_GROUPS_TREE_CACHE_NAME);
    // Dans la méthode doAuthentication() par exemple
    private CachedParentLdapGroupAuthorityRetriever cachedParentLdapGroupAuthorityRetriever;

    @PostConstruct
    void init() {
        // Init en @PostConstruct pour avoir accès aux @Value valorisées :
        ldapSearchMetrics = new LdapSearchMetrics(meterRegistry);
        retryConfig = new RetryConfigBuilder()
                .retryOnSpecificExceptions(org.springframework.ldap.NamingException.class, NullPointerException.class)
                .withMaxNumberOfTries(maxNumberOfTries)
                .withDelayBetweenTries(delayBetweenTriesInSeconds, ChronoUnit.SECONDS)
                .withFixedBackoff()
                .build();
        cachedParentLdapGroupAuthorityRetriever = new CachedParentLdapGroupAuthorityRetriever(cacheManager.getCache(AUTHORIZATION_GROUPS_TREE_CACHE_NAME));
    }

    LdapSearchContext createLdapSearchContext(String username, String password) {
        return new LdapSearchContext(username, password, ldapConfiguration, meterRegistry, ldapSearchMetrics, retryConfig, gson);
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        String password = (String) auth.getCredentials();
        // L'objet retourné est directement passé à loadUserAuthorities par la classe parente :
        return self.searchCN(username, password);
    }

    @Override
    @Cacheable(cacheNames = USERS_AUTHENTICATION_CACHE_NAME)
    // Note: en cas d'exception levée dans cette méthode, rien ne sera mis en cache
    public DirContextOperations searchCN(String username, String password) {
        LdapSearchContext ldapSearchContext = createLdapSearchContext(username, password);
        try {
            return ldapSearchContext.searchUserCNWithRetry(username);
        } finally {
            ldapSearchContext.closeContext();
        }
    }

    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        // ici userData contient la grappe LDAP correspondant à l'utilisateur
        String userDN = userData.getNameInNamespace();
        log.debug("[loadUserAuthorities] userDN: {}", userDN);
        if (log.isDebugEnabled()) { // on évite ce traitement si ce n'est pas nécessaire
            try {
                log.debug("[loadUserAuthorities] userData: {}", gson.toJson(
                        attributesToNative(userData.getAttributes("").getAll())));
            } catch (NamingException e) {
                log.debug("[loadUserAuthorities] NamingException raised while serializing userData.attributes: {}", e);
            }
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> groupDNs = extractGroupAuthoritiesRecursivelyWithCache((DirContextAdapter) userData, username, password);

        // Rôles globaux
        String prodGroupDN = ldapConfiguration.getProdGroupDN();
        if (!isBlank(prodGroupDN) && groupDNs.contains(prodGroupDN)) {
            authorities.add(new GlobalRole(GlobalRole.IS_PROD));
        }
        String techGroupDN = ldapConfiguration.getTechGroupDN();
        if (!isBlank(techGroupDN) && groupDNs.contains(techGroupDN)) {
            authorities.add(new GlobalRole(GlobalRole.IS_TECH));
        }

        // Rôles associés aux groupes Active Directory
        groupDNs.stream()
                .map(DirectoryGroupDN::new)
                .forEach(authorities::add);

        // Applications avec droits de prod
        authorizationProjectionRepository
                .getApplicationsWithDirectoryGroups(new ArrayList<>(groupDNs))
                .stream()
                .map(ApplicationProdRole::new)
                .forEach(authorities::add);

        return authorities;
    }

    private Set<String> extractGroupAuthoritiesRecursivelyWithCache(DirContextAdapter userData, String username, String password) {
        Attributes attributes;
        try {
            attributes = userData.getAttributes("");
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
        LdapSearchContext ldapSearchContext = createLdapSearchContext(username, password);
        try {
            cachedParentLdapGroupAuthorityRetriever.setParentGroupsDNRetriever(ldapSearchContext);
            Set<String> groupAuthorities = new HashSet<>();
            HashSet<String> parentGroupsDN = extractDirectParentGroupDNs(attributes);
            for (String groupDN : parentGroupsDN) {
                groupAuthorities.addAll(cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(groupDN));
            }
            return groupAuthorities;
        } finally {
            ldapSearchContext.closeContext();
        }
    }

    // Public for testing
    public HashSet<String> getUserGroupsDN(String username, String password) {
        DirContextAdapter dirContextAdapter = (DirContextAdapter) self.searchCN(username, password);
        Attributes attributes;
        try {
            attributes = dirContextAdapter.getAttributes("");
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } finally {
            LdapUtils.closeContext(dirContextAdapter);
        }
        return extractDirectParentGroupDNs(attributes);
    }
}
