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
package org.hesperides.infrastructure.security;

import org.hesperides.domain.security.AuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

@Component
public class LdapAuthenticationProvider extends AbstractLdapAuthenticationProvider implements AuthenticationProvider {
    private LdapConfiguration ldapConfiguration;

    /**
     * TODO: refacto du code => plus compréhensible
     * TODO: Ranger les properties
     */

    @Autowired
    public LdapAuthenticationProvider(final LdapConfiguration ldapConfiguration) {
        this.ldapConfiguration = ldapConfiguration;
    }

    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
        DirContextOperations dirContextOperations = null;
        try {
            String username = auth.getName();
            String password = (String) auth.getCredentials();

            DirContext ctx = buildContext(username, password);

            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchFilter = String.format("(%s=%s)", ldapConfiguration.getUsernameAttribute(), username);

            dirContextOperations = SpringSecurityLdapTemplate.searchForSingleEntryInternal(ctx,
                    searchControls, ldapConfiguration.getUserSearchBase(), searchFilter,
                    new Object[]{username});
        } catch (NamingException e) {
            throw badCredentials(e);
        }
        return dirContextOperations;
    }

    protected BadCredentialsException badCredentials(Throwable cause) {
        return (BadCredentialsException) badCredentials().initCause(cause);
    }

    private BadCredentialsException badCredentials() {
        return new BadCredentialsException(messages.getMessage(
                "LdapAuthenticationProvider.badCredentials", "Bad credentials"));
    }

    private DirContext buildContext(final String username, final String password) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConfiguration.getUrl());
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());
        env.put("com.sun.jndi.ldap.connect.timeout", ldapConfiguration.getConnectTimeout());
        env.put("com.sun.jndi.ldap.read.timeout", ldapConfiguration.getReadTimeout());
        env.put(Context.SECURITY_PRINCIPAL, String.format("%s\\%s", ldapConfiguration.getDomain(), username));
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialLdapContext(env, null);
    }

    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        return new ArrayList<GrantedAuthority>();
        /*DirContext ctx = null;
        try {
            ctx = buildContext(username, password);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        List<String> groupsDN = null;
        try {
            groupsDN = searchForUserGroups(ctx, userData);
        } catch (NamingException e) {
            logger.error("Failed to locate directory entry for authenticated user: " + username, e);
            throw new RuntimeException(e);
//            throw badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }

        if (groupsDN == null) {
            logger.debug("No values for user's member of chain.");

            return AuthorityUtils.NO_AUTHORITIES;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("'chaining memberOf' values: " + groupsDN);
        }

        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(groupsDN.size());

        for (String group : groupsDN) {
            authorities.add(new SimpleGrantedAuthority(new DistinguishedName(group)
                    .removeLast().getValue()));
        }

        return authorities;*/
    }

    private static final String LDAP_MATCHING_RULE_IN_CHAIN_OID = "1.2.840.113556.1.4.1941";

    private List<String> searchForUserGroups(DirContext ctx, DirContextOperations userData) throws NamingException {
        List<String> groupsDN = new ArrayList<>();

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchFilter = MessageFormat.format(LDAP_MATCHING_RULE_IN_CHAIN_OID, userData.getDn());
        Name searchBase = userData.getDn().getPrefix(2); // returns domain name like: DC=my_domain,DC=com

        NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter, searchCtls);
        while (answer.hasMoreElements()) {
            SearchResult sr = (SearchResult) answer.next();
            groupsDN.add(sr.getNameInNamespace());
        }

        return groupsDN;
    }
}
