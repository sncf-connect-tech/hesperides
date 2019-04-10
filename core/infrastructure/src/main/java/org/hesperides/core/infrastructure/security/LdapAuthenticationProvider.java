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

import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.domain.security.AuthenticationProvider;
import org.hesperides.core.domain.security.UserRole;
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
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

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

    private LdapConfiguration ldapConfiguration;

    public LdapAuthenticationProvider(final LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    @Cacheable(cacheNames = "users", key = "#authentication.principal")
    public Authentication authenticate(Authentication authentication)
            throws org.springframework.security.core.AuthenticationException {
        return super.authenticate(authentication);
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        String password = (String) auth.getCredentials();
        DirContext ctx = buildSearchContext(username, password);
        return searchUser(ctx, username);
    }

    private DirContext buildSearchContext(final String username, final String password) {
        DirContext context;

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
            context = new InitialLdapContext(env, null);
        } catch (AuthenticationException | OperationNotSupportedException e) {
//            handleBindException(bindPrincipal, e);
            throw badCredentials(e);
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
        return context;
    }

    private DirContextOperations searchUser(final DirContext ctx, final String username) {
        DirContextOperations dirContextOperations;
        try {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String searchFilter = String.format("(%s=%s)", ldapConfiguration.getUsernameAttribute(), username);

            // Durant cet appel SpringSecurityLdapTemplate logue parfois des "Ignoring PartialResultException"
            dirContextOperations = SpringSecurityLdapTemplate.searchForSingleEntryInternal(ctx,
                    searchControls, ldapConfiguration.getUserSearchBase(), searchFilter,
                    new Object[]{username});
        } catch (NamingException e) {
            throw badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
        return dirContextOperations;
    }

    private BadCredentialsException badCredentials(Throwable cause) {
        return (BadCredentialsException) badCredentials().initCause(cause);
    }

    private BadCredentialsException badCredentials() {
        return new BadCredentialsException(messages.getMessage(
                "LdapAuthenticationProvider.badCredentials", "Bad credentials"));
    }

    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        DirContext context = buildSearchContext(username, password);
        if (!isBlank(ldapConfiguration.getProdGroupName()) && hasGroup(context, ldapConfiguration.getProdGroupName(), userData.getNameInNamespace())) {
            authorities.add(new SimpleGrantedAuthority(UserRole.PROD));
        }
        if (!isBlank(ldapConfiguration.getTechGroupName()) && hasGroup(context, ldapConfiguration.getTechGroupName(), userData.getNameInNamespace())) {
            authorities.add(new SimpleGrantedAuthority(UserRole.TECH));
        }
        return authorities;
    }

    /**
     * Méthode reprise telle qu'elle du legacy pour reproduire à l'identique la gestion des rôles.
     */
    private boolean hasGroup(DirContext context, String groupName, String userDN) {
        try {
            String groupSearch = String.format("(CN=%s)", groupName);
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> groupResults = context.search(ldapConfiguration.getRoleSearchBase(), groupSearch, searchControls);

            SearchResult groupSearchResult;
            if (groupResults.hasMoreElements()) {
                groupSearchResult = groupResults.nextElement();
                if (groupResults.hasMoreElements()) {
                    log.error("Expected to find only one group for " + ldapConfiguration.getProdGroupName() + " but found more results");
                    return false;
                }

            } else {
                log.error("Unable to find group {}", ldapConfiguration.getProdGroupName());
                return false;
            }

            //Search recursively to see if user is member of this group
            //We search memberOf the prod group using user DN as base DN
            //We should have one result if the user belongs to the group -> the user itself
            String groupDN = groupSearchResult.getNameInNamespace();
            searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
            String memberOfSearch = String.format("(memberOf:%s:=%s)", LDAP_MATCHING_RULE_IN_CHAIN_OID, groupDN);

            NamingEnumeration<SearchResult> memberOfSearchResults = context.search(userDN, memberOfSearch, searchControls);

            if (memberOfSearchResults.hasMore()) {
                return true;
            } else {
                return false;
            }

        } catch (NamingException e) {
            log.error(e.getExplanation());
        }
        return false;
    }
}
