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
import org.hesperides.core.domain.security.AuthenticationProvider;
import org.hesperides.core.domain.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
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

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hesperides.commons.spring.SpringProfiles.LDAP;

@Profile(LDAP)
@Component
@Slf4j
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider implements AuthenticationProvider {
    /**
     * AD matching rule.
     *
     * @link https://msdn.microsoft.com/en-us/library/aa746475(v=vs.85).aspx
     */
    private static final String LDAP_MATCHING_RULE_IN_CHAIN_OID = "1.2.840.113556.1.4.1941";

    @Autowired
    private Gson gson;
    @Autowired
    private LdapConfiguration ldapConfiguration;

    @Override
    @Cacheable(cacheNames = "users", key = "#authentication.principal")
    public Authentication authenticate(Authentication authentication)
            throws org.springframework.security.core.AuthenticationException {
        return super.authenticate(authentication);
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        DirContext ctx = buildSearchContext(auth.getName(), (String) auth.getCredentials());
        return searchUser(ctx, auth.getName());
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

    private DirContextOperations searchUser(final DirContext ctx, final String username) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = ldapConfiguration.getSearchFilterForUsername(username);
        try {
            // L'objet retourné est directement passé à loadUserAuthorities par la classe parente:
            // Durant cet appel SpringSecurityLdapTemplate logue parfois des "Ignoring PartialResultException"
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(ctx,
                    searchControls, ldapConfiguration.getUserSearchBase(), searchFilter,
                    new Object[]{username});
        } catch (NamingException cause) {
            throw new BadCredentialsException(messages.getMessage(
                    "LdapAuthenticationProvider.badCredentials", "Bad credentials"), cause);
        } finally {
            LdapUtils.closeContext(ctx);
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
        List<GrantedAuthority> authorities = extractGroupAuthoritiesRecursivelyWithCache(userData);
        // TODO: call MongoDB to find what PROD_APP authorities match those groups + add corresponding SimpleGrantedAuthority
        // TODO: remove the calls to hasGroup() below and just check the groupAuthorities for GLOBAL_IS_PROD / GLOBAL_IS_TECH
        DirContext context = buildSearchContext(username, password);
        if (!isBlank(ldapConfiguration.getProdGroupDN()) && hasGroup(context, ldapConfiguration.getProdGroupDN(), userDN)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.GLOBAL_IS_PROD));
        }
        if (!isBlank(ldapConfiguration.getTechGroupDN()) && hasGroup(context, ldapConfiguration.getTechGroupDN(), userDN)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.GLOBAL_IS_TECH));
        }
        return authorities;
    }

    private boolean hasGroup(DirContext context, String groupDN, String userDN) {
        log.debug("[hasGroup] groupDN: {}", groupDN);
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        String memberOfSearch = String.format("(memberOf:%s:=%s)", LDAP_MATCHING_RULE_IN_CHAIN_OID, groupDN);
        try {
            // Search recursively to see if user is member of this group
            // We search memberOf the prod group using user DN as base DN
            // We should have one result if the user belongs to the group -> the user itself
            NamingEnumeration<SearchResult> searchResults = context.search(userDN, memberOfSearch, searchControls);
            boolean hasResults = searchResults.hasMore();
            if (log.isDebugEnabled()) { // on évite ce traitement si ce n'est pas nécessaire
                log.debug("[hasGroup] searchResults: {}", gson.toJson(searchResultToNative(searchResults)));
            }
            return hasResults;
        } catch (NamingException e) {
            log.error(e.getExplanation() + (e.getCause() != null ? (" : " + e.getCause().getMessage()) : ""));
            return false;
        }
    }

    private static List<GrantedAuthority> extractGroupAuthoritiesRecursivelyWithCache(DirContextOperations userData) {
        List<GrantedAuthority> groupAuthorities = new ArrayList<>();
        for (String groupDN : extractDirectParentGroups(userData)) {
            groupAuthorities.add(new LdapGroupGrantedAuthority(groupDN, 1));
        }
        // TODO: recurse with cache
        return groupAuthorities;
    }

    private static List<String> extractDirectParentGroups(DirContextOperations userData) {
        try {
            Attribute memberOf = userData.getAttributes("").get("memberOf");
            List<String> groupsDNs = new ArrayList<>();
            for (int i = 0; i < memberOf.size(); i++) {
                groupsDNs.add((String) memberOf.get(i));
            }
            return groupsDNs;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
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
