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
package org.hesperides.core.domain.security.entities.springauthorities;

import org.springframework.security.core.GrantedAuthority;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class DirectoryGroupDN implements GrantedAuthority {

    private final String authority;

    public DirectoryGroupDN(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public static String extractCnFromDn(String dn) {
        String cn = null;
        try {
            LdapName ldapName = new LdapName(dn);
            for (Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    cn = (String) rdn.getValue();
                }
            }
        } catch (InvalidNameException e) {
            throw new IllegalArgumentException("Invalid DN: " + dn, e);
        }
        if (cn == null) {
            throw new IllegalArgumentException("Can't find CN in DN: " + dn);
        }
        return cn;
    }
}
