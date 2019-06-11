package org.hesperides.core.infrastructure.security.groups;

import lombok.Value;
import org.springframework.security.core.GrantedAuthority;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.Set;

@Value
public class LdapGroupAuthority implements GrantedAuthority {
    private static final long serialVersionUID = 56874654321489L;
    // On marque comme "transient" les champs qu'on ne veut pas inclure dans la sortie de /users/auth
    transient String groupDN;
    transient int ancestorLevel; // 1 => parent, 2 => grand-parent, etc.
    String groupCN;

    LdapGroupAuthority(String groupDN, int ancestorLevel) {
        this.groupDN = groupDN;
        this.ancestorLevel = ancestorLevel;
        this.groupCN = extractCN(groupDN);
    }

    @Override
    public String getAuthority() {
        return this.groupCN;
    }

    public static boolean containDN(Set<LdapGroupAuthority> groupDNs, String dn) {
        return groupDNs.stream().anyMatch(group -> group.getGroupDN().equals(dn));
    }

    // public for testing
    public static String extractCN(String dn) {
        try {
            LdapName ln = new LdapName(dn);
            for (Rdn rdn : ln.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return (String) rdn.getValue();
                }
            }
            throw new IllegalArgumentException("No CN found in provided DN");
        } catch (InvalidNameException e) {
            throw new IllegalArgumentException("Invalid DN provided", e);
        }
    }
}
