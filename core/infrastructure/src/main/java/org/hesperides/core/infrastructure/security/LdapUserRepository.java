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

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.security.GetUserQuery;
import org.hesperides.core.domain.security.ResolveDirectoryGroupCNsQuery;
import org.hesperides.core.domain.security.UserRepository;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.queries.views.DirectoryGroupDNsView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import javax.naming.directory.DirContext;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.SpringProfiles.NOLDAP;

@Repository
public class LdapUserRepository implements UserRepository {

    @Autowired(required = false)
    private LdapAuthenticationProvider ldapAuthenticationProvider;

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<User> onGetUserQuery(GetUserQuery query) {
        if (ldapAuthenticationProvider == null) {
            throw new RuntimeException("This functionality is not available with profile " + NOLDAP);
        }
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        DirContext dirContext = ldapAuthenticationProvider.buildSearchContext(auth);
        try {
            DirContextOperations userData = ldapAuthenticationProvider.searchCN(dirContext, query.getUsername());
            Collection<? extends GrantedAuthority> springAuthorities = ldapAuthenticationProvider.loadUserAuthorities(userData, auth.getName(), (String) auth.getCredentials());
            return Optional.of(new User(query.getUsername(), springAuthorities));
        } catch (IncorrectResultSizeDataAccessException incorrectResultSizeException) {
            if (incorrectResultSizeException.getActualSize() == 0) {
                return Optional.empty();
            }
            throw incorrectResultSizeException;

        } finally {
            LdapUtils.closeContext(dirContext); // implique la suppression de l'env créé dans .buildSearchContext
        }
    }

    @QueryHandler
    @Override
    public DirectoryGroupDNsView onResolveDirectoryGroupCNsQuery(ResolveDirectoryGroupCNsQuery query) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        DirContext dirContext = ldapAuthenticationProvider.buildSearchContext(auth);
        try {
            List<String> directoryGroupDNs = query.getDirectoryGroupCNs().stream().map(groupCN -> {
                // On doit bénéficier du cache durant cet appel :
                DirContextOperations dirContextOperations = ldapAuthenticationProvider.searchCN(dirContext, groupCN);
                return dirContextOperations.getNameInNamespace();
            }).collect(Collectors.toList());
            return new DirectoryGroupDNsView(directoryGroupDNs);
        } finally {
            LdapUtils.closeContext(dirContext); // implique la suppression de l'env créé dans .buildSearchContext
        }
    }
}
