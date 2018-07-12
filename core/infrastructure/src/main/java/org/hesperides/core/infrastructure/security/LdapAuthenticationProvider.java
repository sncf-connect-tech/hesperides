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

import org.hesperides.core.domain.security.AuthenticationProvider;
import org.hesperides.core.domain.security.UserRole;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

@Component
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider implements AuthenticationProvider {
    private LdapConfiguration ldapConfiguration;

    /**
     * TODO: Gestion des erreurs
     */

    public LdapAuthenticationProvider(final LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        String username = auth.getName();
        /**
         * TODO
         * Tester un mot de passe avec une lettre accentuée
         * Il paraît que ça plante => Gérer le cas
         */
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
        String[] groups = userData.getStringAttributes("memberOf");
        if (hasGroup(groups, ldapConfiguration.getProdGroupName())) {
            authorities.add(new SimpleGrantedAuthority(UserRole.PROD));
        }
        if (hasGroup(groups, ldapConfiguration.getTechGroupName())) {
            authorities.add(new SimpleGrantedAuthority(UserRole.TECH));
        }
        return authorities;
    }

    private boolean hasGroup(final String[] groups, final String groupName) {
        boolean hasRole = false;
        if (groups != null && StringUtils.hasText(groupName)) {
            for (String group : groups) {
                String commonName = getCommonName(group);
                if (groupName.equalsIgnoreCase(commonName)) {
                    hasRole = true;
                    break;
                }
            }
        }
        return hasRole;
    }

    /**
     * Get the CN out of the DN
     *
     * @param distinguishedName
     * @return
     */
    private String getCommonName(final String distinguishedName) {
        String commonName = null;
        LdapName ldapName = LdapUtils.newLdapName(distinguishedName);
        Rdn rdn = LdapUtils.getRdn(ldapName, "cn");
        if (rdn != null && rdn.getValue() != null) {
            commonName = rdn.getValue().toString();
        }
        return commonName;
    }

//    private static final String LDAP_MATCHING_RULE_IN_CHAIN_OID = "1.2.840.113556.1.4.1941";
//
//    private List<String> searchForUserGroups(DirContext ctx, DirContextOperations userData) throws NamingException {
//        List<String> groupsDN = new ArrayList<>();
//
//        SearchControls searchCtls = new SearchControls();
//        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//        String searchFilter = MessageFormat.format(LDAP_MATCHING_RULE_IN_CHAIN_OID, userData.getDn());
//        Name searchBase = userData.getDn().getPrefix(2); // returns domain name like: DC=my_domain,DC=com
//
//        NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter, searchCtls);
//        while (answer.hasMoreElements()) {
//            SearchResult sr = (SearchResult) answer.next();
//            groupsDN.add(sr.getNameInNamespace());
//        }
//
//        return groupsDN;
//    }
}
