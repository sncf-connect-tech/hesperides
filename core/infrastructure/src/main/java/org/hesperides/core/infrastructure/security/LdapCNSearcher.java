package org.hesperides.core.infrastructure.security;

import org.springframework.ldap.core.DirContextOperations;

/*
 * Cette interface permet de "passer par un attribut" pour que le cache fonctionne,
 * cf. https://stackoverflow.com/a/48867068/636849
 */
public interface LdapCNSearcher {

    DirContextOperations searchCN(String username, String password);
}
