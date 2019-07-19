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
package org.hesperides.core.domain.security.entities;

import lombok.Value;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value
public class ApplicationDirectoryGroups {
    String applicationName;
    List<String> directoryGroupDNs;

    public static String getCnFromDn(String dn) throws InvalidNameException {
        for (Rdn rdn : new LdapName(dn).getRdns()) {
            if (rdn.getType().equalsIgnoreCase("CN")) {
                return (String) rdn.getValue();
            }
        }
        throw new RuntimeException("Can't find CN in DN: " + dn);
    }

    public static Optional<String> extractCnFromDn(String dn) {
        try {
            return Optional.ofNullable(getCnFromDn(dn));
        } catch (InvalidNameException e) {
            return Optional.empty();
        }
    }
}
