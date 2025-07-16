/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.core.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static org.hesperides.commons.SpringProfiles.LDAP;

@Profile(LDAP)
@Component
@ConfigurationProperties(prefix = "ldap")
@Getter
@Setter
@Validated
public class LdapConfiguration {
    @NotEmpty
    private String url;
    
    private String domain;

    private String bindDn;

    private String bindPassword;
    @NotEmpty
    private String userSearchBase;
    @NotEmpty
    private String roleSearchBase;
    @NotEmpty
    private String usernameAttribute;
    @NotEmpty
    private String connectTimeout;
    @NotEmpty
    private String readTimeout;
    @NotEmpty
    private String prodGroupDN;
    @NotEmpty
    private String techGroupDN;

    public String getSearchFilterForCN(String username) {
        return String.format("(%s=%s)", usernameAttribute, LdapEncoder.nameEncode(username));
    }
}
