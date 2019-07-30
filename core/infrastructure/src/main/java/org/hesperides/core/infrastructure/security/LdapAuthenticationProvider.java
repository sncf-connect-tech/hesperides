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

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.hesperides.core.domain.security.AuthenticationProvider;
import org.hesperides.core.domain.security.AuthorizationProjectionRepository;
import org.hesperides.core.domain.security.entities.springauthorities.ApplicationProdRole;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;
import org.hesperides.core.domain.security.entities.springauthorities.GlobalRole;
import org.hesperides.core.infrastructure.security.groups.CachedParentLdapGroupAuthorityRetriever;
import org.hesperides.core.infrastructure.security.groups.ParentGroupsDNRetrieverFromLdap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hesperides.commons.SpringProfiles.LDAP;
import static org.hesperides.core.infrastructure.security.groups.ParentGroupsDNRetrieverFromLdap.extractDirectParentGroupDNs;

@Profile(LDAP)
@Component
@Slf4j
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider implements AuthenticationProvider, LdapCNSearcher {

    private static final String USERS_AUTHENTICATION_CACHE_NAME = "users-authentication";
    private static final String AUTHORIZATION_GROUPS_TREE_CACHE_NAME = "authorization-groups-tree";

    @Resource
    private LdapCNSearcher self;
    @Autowired
    private Gson gson; // nécessaire uniquement pour les logs DEBUG
    @Autowired
    private LdapConfiguration ldapConfiguration;
    @Autowired
    private AuthorizationProjectionRepository authorizationProjectionRepository;
    @Autowired
    private CacheManager cacheManager;
    // Pour débuguer le contenus des caches:
    //   Evaluate Expression: cacheManager.ehcaches.get(USERS_AUTHENTICATION_CACHE_NAME).compoundStore.map
    //   Evaluate Expression: cacheManager.ehcaches.get(AUTHORIZATION_GROUPS_TREE_CACHE_NAME).compoundStore.map
    private CachedParentLdapGroupAuthorityRetriever cachedParentLdapGroupAuthorityRetriever;

    @PostConstruct
    void init() {
        cachedParentLdapGroupAuthorityRetriever = new CachedParentLdapGroupAuthorityRetriever(cacheManager.getCache(AUTHORIZATION_GROUPS_TREE_CACHE_NAME));
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        DirContext dirContext = buildSearchContext(auth);
        // On passe par un attribut pour que le cache fonctionne, cf. https://stackoverflow.com/a/48867068/636849
        // L'objet retourné est directement passé à loadUserAuthorities par la classe parente :
        try {
            return self.searchCN(dirContext, auth.getName());
        } finally {
            LdapUtils.closeContext(dirContext); // implique la suppression de l'env créé dans .buildSearchContext
        }
    }

    @Override
    @Cacheable(cacheNames = USERS_AUTHENTICATION_CACHE_NAME, key = "#username")
    // Note: en cas d'exception levée dans cette méthode, rien ne sera mis en cache
    public DirContextOperations searchCN(DirContext dirContext, String username) {
        return ParentGroupsDNRetrieverFromLdap.searchCN(dirContext, username,
                ldapConfiguration.getUserSearchBase(),
                ldapConfiguration.getSearchFilterForCN(username));
    }

    DirContext buildSearchContext(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        String password = (String) auth.getCredentials();
        return buildSearchContext(username, password);
    }

    private DirContext buildSearchContext(String username, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConfiguration.getUrl());
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());
        env.put("com.sun.jndi.ldap.connect.timeout", ldapConfiguration.getConnectTimeout());
        env.put("com.sun.jndi.ldap.read.timeout", ldapConfiguration.getReadTimeout());
        env.put(Context.SECURITY_PRINCIPAL, String.format("%s\\%s", ldapConfiguration.getDomain(), username));
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext dirContext = new InitialLdapContext(env, null);
            // ici dirContext ne contient que des infos relatives au serveur avec lequel la connexion vient d'être établie
            if (log.isDebugEnabled()) { // on évite ce traitement si ce n'est pas nécessaire
                log.debug("[buildSearchContext] dirContext: {}", gson.toJson(attributesToNative(dirContext.getAttributes("").getAll())));
            }
            return dirContext;
        } catch (AuthenticationException | OperationNotSupportedException cause) {
            throw new BadCredentialsException(messages.getMessage(
                    "LdapAuthenticationProvider.badCredentials", "Bad credentials"), cause);
        } catch (NamingException e) {
            log.error(e.getExplanation() + (e.getCause() != null ? (" : " + e.getCause().getMessage()) : ""));
            throw LdapUtils.convertLdapException(e);
        }
    }

    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        // ici userData contient la grappe LDAP correspondant à l'utilisateur
        String userDN = userData.getNameInNamespace();
        log.debug("[loadUserAuthorities] userDN: {}", userDN);
        if (log.isDebugEnabled()) { // on évite ce traitement si ce n'est pas nécessaire
            try {
                log.debug("[loadUserAuthorities] userData: {}", gson.toJson(attributesToNative(userData.getAttributes("").getAll())));
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
        DirContext dirContext = buildSearchContext(username, password);
        try {
            cachedParentLdapGroupAuthorityRetriever.setParentGroupsDNRetriever(new ParentGroupsDNRetrieverFromLdap(dirContext, ldapConfiguration));
            Set<String> groupAuthorities = new HashSet<>();
            HashSet<String> parentGroupsDN = extractDirectParentGroupDNs(attributes);
            for (String groupDN : parentGroupsDN) {
                groupAuthorities.addAll(cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(groupDN));
            }
            return groupAuthorities;
        } finally {
            LdapUtils.closeContext(dirContext); // implique la suppression de l'env créé dans .buildSearchContext
        }
    }

    // Public for testing
    public HashSet<String> getUserGroupsDN(String username, String password) {
        DirContext dirContext = this.buildSearchContext(username, password);
        // On passe par un attribut pour que le cache fonctionne, cf. https://stackoverflow.com/a/48867068/636849
        DirContextAdapter dirContextAdapter = (DirContextAdapter) self.searchCN(dirContext, username);
        Attributes attributes;
        try {
            attributes = dirContextAdapter.getAttributes("");
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } finally {
            LdapUtils.closeContext(dirContext); // implique la suppression de l'env créé dans .buildSearchContext
        }
        return extractDirectParentGroupDNs(attributes);
    }

    /*****************************************************************/
    /**************** Pretty-printing debug methods ******************/
    /*****************************************************************/

    // ATTENTION: cette méthode CONSOMME l'énumération searchResults, elle a donc un effet de bord
    private static List<Map<String, Object>> searchResultToNative(NamingEnumeration<SearchResult> searchResults) throws NamingException {
        List<Map<String, Object>> output = new ArrayList<>();
        while (searchResults.hasMore()) {
            output.add(attributesToNative(searchResults.next().getAttributes().getAll()));
        }
        return output;
    }

    // ATTENTION: cette méthode CONSOMME l'énumération attributes, elle a donc un effet de bord
    private static Map<String, Object> attributesToNative(NamingEnumeration<? extends Attribute> attributes) throws NamingException {
        Map<String, Object> output = new HashMap<>();
        while (attributes.hasMore()) {
            Attribute attribute = attributes.next();
            output.put(attribute.getID(), attributeValueToNative(attribute));
        }
        return output;
    }

    private static Object attributeValueToNative(Attribute attribute) throws NamingException {
        if (attribute.getID().equals("thumbnailPhoto")) { // integer array value, too long to display
            return "<OMITTED>";
        }
        if (attribute.size() == 1) {
            return attribute.get();
        }
        List<Object> attrs = new ArrayList<>();
        for (int i = 0; i < attribute.size(); i++) {
            attrs.add(attribute.get(i));
        }
        return attrs;
    }
}
