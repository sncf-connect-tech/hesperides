package org.hesperides.core.infrastructure.security;

import org.springframework.ldap.core.DirContextOperations;

import javax.naming.directory.DirContext;

public interface LDAPUserSearcher {

    DirContextOperations searchUser(final DirContext dirContext, final String username);
}
