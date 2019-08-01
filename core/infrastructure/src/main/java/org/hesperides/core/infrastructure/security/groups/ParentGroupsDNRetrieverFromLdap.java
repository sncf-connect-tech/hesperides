package org.hesperides.core.infrastructure.security.groups;

import io.micrometer.core.annotation.Timed;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;
import org.hesperides.core.infrastructure.security.LdapConfiguration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import java.util.HashSet;

public class ParentGroupsDNRetrieverFromLdap implements ParentGroupsDNRetriever {

    private final DirContext dirContext;
    private final LdapConfiguration ldapConfiguration;

    public ParentGroupsDNRetrieverFromLdap(DirContext dirContext, LdapConfiguration ldapConfiguration) {
        this.dirContext = dirContext;
        this.ldapConfiguration = ldapConfiguration;
    }

    public HashSet<String> retrieveParentGroupDNs(String dn) {
        HashSet<String> parentGroupDNs = new HashSet<>();
        try {
            String cn = DirectoryGroupDN.extractCnFromDn(dn);
            String base = getBaseFrom(cn, dn);
            String searchFilter = ldapConfiguration.getSearchFilterForCN(cn);
            DirContextOperations dirContextOperations = searchCN(dirContext, cn, base, searchFilter);
            parentGroupDNs = extractDirectParentGroupDNs(dirContextOperations.getAttributes(""));
        } catch (IncorrectResultSizeDataAccessException e) {
            // On accepte que la recherche ne retourne aucun résultat
        } catch (NamingException exception) {
            throw LdapUtils.convertLdapException(exception);
        }
        return parentGroupDNs;
    }

    @Timed // Il s'agit du seul endroit du code d'où sont véritablement effectués les appels LDAPS
    public static DirContextOperations searchCN(DirContext dirContext, String cn, String base, String searchFilter) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            // Durant cet appel, SpringSecurityLdapTemplate logue parfois des "Ignoring PartialResultException"
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(dirContext, searchControls, base, searchFilter, new Object[]{cn});
        } catch (NamingException exception) {
            throw LdapUtils.convertLdapException(exception);
        }
    }

    /**
     * Retourne le DN amputé de son CN
     */
    private static String getBaseFrom(String cn, String dn) {
        return dn.substring(("cn" + cn + ",").length() + 1);
    }

    public static HashSet<String> extractDirectParentGroupDNs(Attributes attributes) {
        try {
            Attribute memberOf = attributes.get("memberOf");
            HashSet<String> groupsDNs = new HashSet<>();
            if (memberOf != null) {
                for (int i = 0; i < memberOf.size(); i++) {
                    groupsDNs.add((String) memberOf.get(i));
                }
            }
            return groupsDNs;
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
    }
}
