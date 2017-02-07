/*
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
 */
package com.vsct.dt.hesperides.security.jersey;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.DefaultUnauthorizedHandler;
import io.dropwizard.auth.UnauthorizedHandler;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;

/**
 * Created by emeric_martineau on 02/02/2017.
 */
public class HesperidesBasicAuthFactory<T> extends AuthFactory<BasicCredentials, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthFactory.class);

    private final boolean required;
    private final Class<T> generatedClass;
    private final String realm;
    private String prefix = "Basic";
    private UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

    @Context
    private HttpServletRequest request;

    public HesperidesBasicAuthFactory(final Authenticator<BasicCredentials, T> authenticator,
            final String realm,
            final Class<T> generatedClass) {
        super(authenticator);
        this.required = false;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }

    private HesperidesBasicAuthFactory(final boolean required,
            final Authenticator<BasicCredentials, T> authenticator,
            final String realm,
            final Class<T> generatedClass) {
        super(authenticator);
        this.required = required;
        this.realm = realm;
        this.generatedClass = generatedClass;
    }

    public HesperidesBasicAuthFactory<T> prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public HesperidesBasicAuthFactory<T> responseBuilder(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
        return this;
    }

    @Override
    public AuthFactory<BasicCredentials, T> clone(boolean required) {
        return new HesperidesBasicAuthFactory<>(required, authenticator(), this.realm, this.generatedClass).prefix(prefix).responseBuilder(unauthorizedHandler);
    }

    @Override
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public T provide() {
        if (request != null) {
            final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            try {
                if (header != null) {
                    final int space = header.indexOf(' ');
                    if (space > 0) {
                        final String method = header.substring(0, space);
                        if (prefix.equalsIgnoreCase(method)) {
                            final String decoded = new String(
                                    BaseEncoding.base64().decode(header.substring(space + 1)),
                                    StandardCharsets.UTF_8);
                            final int i = decoded.indexOf(':');
                            if (i > 0) {
                                final String username = decoded.substring(0, i);
                                final String password = decoded.substring(i + 1);
                                final BasicCredentials credentials = new BasicCredentials(username, password);
                                final Optional<T> result = authenticator().authenticate(credentials);
                                if (result.isPresent()) {
                                    return result.get();
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Error decoding credentials", e);
            } catch (AuthenticationException e) {
                LOGGER.warn("Error authenticating credentials", e);
                throw new InternalServerErrorException();
            }
        }

        if (required) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
        }

        return null;
    }

    @Override
    public Class<T> getGeneratedClass() {
        return generatedClass;
    }
}
