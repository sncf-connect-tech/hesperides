package org.hesperides.core.infrastructure.security.groups;

import org.hesperides.core.infrastructure.security.LdapConfiguration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.HashSet;

public class ParentGroupsDNRetrieverFromLdap implements ParentGroupsDNRetriever {

    private final DirContext context;
    private final LdapConfiguration ldapConfiguration;

    public ParentGroupsDNRetrieverFromLdap(DirContext context, LdapConfiguration ldapConfiguration) {
        this.context = context;
        this.ldapConfiguration = ldapConfiguration;
    }

    public HashSet<String> retrieveParentGroupsDN(String dn) {
        HashSet<String> parents = new HashSet<>();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            String cn = getCnFromDn(dn);
            String base = getBaseFrom(cn, dn);
            final String filter = ldapConfiguration.getSearchFilterForUsername(cn);

            final DirContextOperations dirContextOperations = SpringSecurityLdapTemplate.searchForSingleEntryInternal(
                    context, searchControls, base, filter, new Object[]{cn});
            final Attributes attributes = dirContextOperations.getAttributes("");
            parents = extractDirectParentGroupsDN(attributes);

        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        } catch (IncorrectResultSizeDataAccessException e) {
            // On accepte que la recherche ne retourne aucun résultat
        }
        return parents;
    }

    /**
     * Extrait le CN à partir du DN
     */
    private String getCnFromDn(String dn) throws InvalidNameException {
        for (Rdn rdn : new LdapName(dn).getRdns()) {
            if (rdn.getType().equalsIgnoreCase("CN")) {
                return (String) rdn.getValue();
            }
        }
        throw new RuntimeException("Can't find CN in DN: " + dn);
    }

    /**
     * Retourne le DN amputé de son CN
     */
    private String getBaseFrom(String cn, String dn) {
        return dn.substring(("cn" + cn + ",").length() + 1);
    }

    public static HashSet<String> extractDirectParentGroupsDN(Attributes attributes) {
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
