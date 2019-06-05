package org.hesperides.core.infrastructure.security.groups;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import java.util.HashSet;

public class ParentGroupsDNRetrieverFromLdap implements ParentGroupsDNRetriever {

    private DirContextAdapter dirContext;

    public ParentGroupsDNRetrieverFromLdap(DirContextAdapter dirContext) {
        dirContext.setUpdateMode(false);
        this.dirContext = dirContext;
    }

    public HashSet<String> retrieveParentGroupsDN(String dn) {
        try {
            dirContext.setDn(new LdapName(dn));
            return extractDirectParentGroupsDN(dirContext.getAttributes(""));
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
    }

    public static HashSet<String> extractDirectParentGroupsDN(Attributes attributes) {
        try {
            Attribute memberOf = attributes.get("memberOf");
            HashSet<String> groupsDNs = new HashSet<>();
            for (int i = 0; i < memberOf.size(); i++) {
                groupsDNs.add((String) memberOf.get(i));
            }
            return groupsDNs;
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
    }
}
