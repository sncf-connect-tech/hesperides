package org.hesperides.core.infrastructure.security;

import lombok.Value;
import org.springframework.security.core.GrantedAuthority;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

@Value
public class LdapGroupGrantedAuthority implements GrantedAuthority {
    private static final long serialVersionUID = 56874654321489L;
    // On marque comme "transient" les champs qu'on ne veut pas inclure dans la sortie de /users/auth
    private final transient String groupDN;
    private final transient int ancestorLevel; // 1 => parent, 2 => grand-parent, etc.
    private final String groupCN;

    LdapGroupGrantedAuthority(String groupDN, int ancestorLevel) {
        this.groupDN = groupDN;
        this.ancestorLevel = ancestorLevel;
        this.groupCN = extractCN(groupDN);
    }

    @Override
    public String getAuthority() {
        return this.groupCN;
    }

    private static String extractCN(String dn) {
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
