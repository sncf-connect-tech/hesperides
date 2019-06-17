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
import org.hesperides.core.domain.security.UserRole;
import org.hesperides.core.infrastructure.security.groups.CachedParentLdapGroupAuthorityRetriever;
import org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hesperides.commons.spring.SpringProfiles.LDAP;
import static org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority.containDN;
import static org.hesperides.core.infrastructure.security.groups.ParentGroupsDNRetrieverFromLdap.extractDirectParentGroupsDN;

@Profile(LDAP)
@Component
@Slf4j
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider implements AuthenticationProvider {

    private static final String USERS_AUTHENTICATION_CACHE_NAME = "users-authentication";
    private static final String AUTHORIZATION_GROUPS_TREE_CACHE_NAME = "authorization-groups-tree";

    @Autowired
    private Gson gson; // nécessaire uniquement pour les logs DEBUG
    @Autowired
    private LdapConfiguration ldapConfiguration;
    @Autowired
    private CacheManager cacheManager;
    private CachedParentLdapGroupAuthorityRetriever cachedParentLdapGroupAuthorityRetriever; // Pourquoi pas Autowired ?
    @Autowired
    private AuthorizationProjectionRepository authorizationProjectionRepository;

    @PostConstruct
    void init() {
        cachedParentLdapGroupAuthorityRetriever = new CachedParentLdapGroupAuthorityRetriever(cacheManager.getCache(AUTHORIZATION_GROUPS_TREE_CACHE_NAME));
    }

    @Override
    @Cacheable(cacheNames = USERS_AUTHENTICATION_CACHE_NAME, key = "#authentication.principal")
    public Authentication authenticate(Authentication authentication)
            throws org.springframework.security.core.AuthenticationException {
        return super.authenticate(authentication);
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        DirContext dirContext = buildSearchContext(auth.getName(), (String) auth.getCredentials());
        return searchUser(dirContext, auth.getName());
    }

    private DirContextOperations searchUser(final DirContext dirContext, final String username) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = ldapConfiguration.getSearchFilterForUsername(username);
        try {
            // L'objet retourné est directement passé à loadUserAuthorities par la classe parente:
            // Durant cet appel SpringSecurityLdapTemplate logue parfois des "Ignoring PartialResultException"
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(dirContext,
                    searchControls, ldapConfiguration.getUserSearchBase(), searchFilter,
                    new Object[]{username});
        } catch (NamingException cause) {
            throw new BadCredentialsException(messages.getMessage(
                    "LdapAuthenticationProvider.badCredentials", "Bad credentials"), cause);
        } finally {
            LdapUtils.closeContext(dirContext);
        }
    }

    private DirContext buildSearchContext(final String username, final String password) {
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
        Set<LdapGroupAuthority> groupAuthorities = extractGroupAuthoritiesRecursivelyWithCache((DirContextAdapter) userData);

        // TODO: call MongoDB to find what PROD_APP authorities match those groups + add corresponding SimpleGrantedAuthority
        authorizationProjectionRepository.getApplicationsForAuthorities(groupAuthorities.stream().map(LdapGroupAuthority::getAuthority).collect(Collectors.toList()));

        Set<GrantedAuthority> authorities = new HashSet<>(groupAuthorities);
        String prodGroupDN = ldapConfiguration.getProdGroupDN();
        if (!isBlank(prodGroupDN) && containDN(groupAuthorities, prodGroupDN)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.GLOBAL_IS_PROD));
        }
        String techGroupDN = ldapConfiguration.getTechGroupDN();
        if (!isBlank(techGroupDN) && containDN(groupAuthorities, techGroupDN)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.GLOBAL_IS_TECH));
        }
        return authorities;
    }

    private Set<LdapGroupAuthority> extractGroupAuthoritiesRecursivelyWithCache(DirContextAdapter userData) {
        cachedParentLdapGroupAuthorityRetriever.setParentGroupsDNRetriever(new ParentGroupsDNRetrieverFromLdap(userData));
        Set<LdapGroupAuthority> groupAuthorities = new HashSet<>();
        Attributes attributes;
        try {
            attributes = userData.getAttributes("");
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
        HashSet<String> parentGroupsDN = extractDirectParentGroupsDN(attributes);
        for (String groupDN : parentGroupsDN) {
            groupAuthorities.addAll(cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(groupDN));
        }
        return groupAuthorities;
    }

    // Public for testing
    public HashSet<String> getUserGroupsDN(String username, String password) {
        DirContext dirContext = this.buildSearchContext(username, password);
        DirContextAdapter dirContextAdapter = (DirContextAdapter) searchUser(dirContext, username);
        Attributes attributes;
        try {
            attributes = dirContextAdapter.getAttributes("");
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
        return extractDirectParentGroupsDN(attributes);
    }

    // Public for testing
    public HashSet<String> getParentGroupsDN(String username, String password, String dn) {
        DirContext dirContext = this.buildSearchContext(username, password);
        DirContextAdapter dirContextAdapter;
        try {
            dirContextAdapter = new DirContextAdapter(dirContext.getAttributes(""), new LdapName(dn),
                    new LdapName(dirContext.getNameInNamespace()));
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
        ParentGroupsDNRetrieverFromLdap parentGroupsDNRetriever = new ParentGroupsDNRetrieverFromLdap(dirContextAdapter);
        return parentGroupsDNRetriever.retrieveParentGroupsDN(dn);
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
