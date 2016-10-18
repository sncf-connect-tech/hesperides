/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by william_montaz on 23/02/2015.
 */
public class LdapConfiguration {

    @NotEmpty
    @JsonProperty
    private String userNameAttribute;

    @NotEmpty
    @JsonProperty
    private String uri;

    @JsonProperty
    private Duration connectTimeout;

    @JsonProperty
    private Duration readTimeout;

    @NotEmpty
    @JsonProperty
    private String userSearchBase;

    @NotEmpty
    @JsonProperty
    private String roleSearchBase;

    @NotEmpty
    @JsonProperty
    private String prodGroupName;

    @NotEmpty
    @JsonProperty
    private String adDomain;

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getRoleSearchBase() {
        return roleSearchBase;
    }

    public void setRoleSearchBase(String roleSearchBase) {
        this.roleSearchBase = roleSearchBase;
    }

    public String getProdGroupName() {
        return prodGroupName;
    }

    public void setProdGroupName(String prodGroupName) {
        this.prodGroupName = prodGroupName;
    }

    public String getAdDomain() {
        return adDomain;
    }

    public void setAdDomain(String adDomain) {
        this.adDomain = adDomain;
    }
}
