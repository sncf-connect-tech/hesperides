package org.hesperides.core.infrastructure.security;

import org.springframework.ldap.core.DirContextOperations;

import javax.naming.directory.DirContext;

public interface LdapCNSearcher {

    DirContextOperations searchCN(String username, String password);
}
