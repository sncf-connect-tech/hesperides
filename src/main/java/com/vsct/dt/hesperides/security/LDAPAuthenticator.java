/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.security;

import java.util.Optional;

import com.vsct.dt.hesperides.security.model.LdapPoolConfiguration;

import com.vsct.dt.hesperides.security.model.User;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * Created by william_montaz on 12/11/2014.
 */
public final class LDAPAuthenticator implements Authenticator<BasicCredentials, User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDAPAuthenticator.class);

    /**
     * AD matching rule.
     * @link https://msdn.microsoft.com/en-us/library/aa746475(v=vs.85).aspx
     */
    private static final String LDAP_MATCHING_RULE_IN_CHAIN_OID = "1.2.840.113556.1.4.1941";

    private final LdapConfiguration configuration;

    public LDAPAuthenticator(LdapConfiguration configuration) {
        this.configuration = configuration;

        final LdapPoolConfiguration pool = configuration.getPool();

        if (pool != null) {
            System.setProperty("com.sun.jndi.ldap.connect.pool", "true");
            System.setProperty("com.sun.jndi.ldap.connect.pool.initsize", String.valueOf(pool.getInitsize()));
            System.setProperty("com.sun.jndi.ldap.connect.pool.maxsize", String.valueOf(pool.getMaxsize()));
            System.setProperty("com.sun.jndi.ldap.connect.pool.timeout", String.valueOf(pool.getIdleTimeout()));
        }
    }

    @Override
    public Optional<User> authenticate(final BasicCredentials credentials) throws AuthenticationException {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        try {
            try (AutoclosableDirContext context = buildContext(username, password)) {

                //Get the user DN
                SearchResult userSearched = searchUser(context, username);

                //Check if user is in the prod group
                final boolean prodUser = checkIfUserBelongsToGroup(context, userSearched.getNameInNamespace(), configuration.getProdGroupName());
                final boolean techUser = checkIfUserBelongsToGroup(context, userSearched.getNameInNamespace(), configuration.getTechGroupName());

                return Optional.of(new User(username, prodUser, techUser));
            }

        } catch (NamingException e) {
            LOGGER.debug("{} failed to authenticate {}", username);
        }

        return Optional.empty();
    }

    private boolean checkIfUserBelongsToGroup(final AutoclosableDirContext context, final String userDN, final String groupName) throws
            NamingException,
            AuthenticationException {
        String groupSearch = String.format("(CN=%s)", groupName);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> groupResults = context.search(configuration.getRoleSearchBase(), groupSearch, searchControls);

        SearchResult groupSearchResult;

        if (groupResults.hasMoreElements()) {
            groupSearchResult = groupResults.nextElement();

            if (groupResults.hasMoreElements()) {
                LOGGER.error("Expected to find only one group for " + configuration.getProdGroupName() + " but found more results");
                return false;
            }

        } else {
            LOGGER.error("Unable to find group {}", configuration.getProdGroupName());
            return false;
        }

        //Search recursively to see if user is member of this group
        //We search memberOf the prod group using user DN as base DN
        //We should have one result if the user belongs to the group -> the user itself
        String groupDN = groupSearchResult.getNameInNamespace();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        String memberOfSearch = String.format("(memberOf:%s:=%s)", LDAP_MATCHING_RULE_IN_CHAIN_OID, groupDN);

        NamingEnumeration<SearchResult> memberOfSearchResults = context.search(userDN, memberOfSearch, searchControls);

        if (memberOfSearchResults.hasMore()){
            return true;
        } else {
            return false;
        }
    }

    private SearchResult searchUser(final AutoclosableDirContext context, final String username) throws NamingException, AuthenticationException {
        String searchfilter = String.format("(%s=%s)", configuration.getUserNameAttribute(), username);

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = context.search(configuration.getUserSearchBase(), searchfilter, searchControls);

        SearchResult searchResult;

        if (results.hasMoreElements()) {
            searchResult = results.nextElement();

            if (results.hasMoreElements()){
                throw new AuthenticationException("Expected to find only one user for "+username+" but found more results");
            }

        } else {
            throw new AuthenticationException("Unable to authenticate user "+username);
        }

        return searchResult;
    }

    private AutoclosableDirContext buildContext(final String username, final String password) throws NamingException {
        Hashtable<String, String> env = contextConfiguration();

        env.put(Context.SECURITY_PRINCIPAL, configuration.getAdDomain() + "\\" + username);
        env.put(Context.SECURITY_CREDENTIALS, password);

        return new AutoclosableDirContext(env);
    }

    private Hashtable<String, String> contextConfiguration() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, configuration.getUri());
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(configuration.getConnectTimeout().toMilliseconds()));
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(configuration.getReadTimeout().toMilliseconds()));

        return env;
    }
}
