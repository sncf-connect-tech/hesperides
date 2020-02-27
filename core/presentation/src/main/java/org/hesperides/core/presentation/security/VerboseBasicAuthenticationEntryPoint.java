/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2020 VSCT.
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
package org.hesperides.core.presentation.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final class VerboseBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

	VerboseBasicAuthenticationEntryPoint() {
		setRealmName("Realm");
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) throws IOException {
		response.addHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + getRealmName() + "\"");
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		String errorMessage = authException.getMessage();
		if (authException.getCause() != null) {
			// LDAP error messages have been seen to contain \u0000 characters. We remove them:
			errorMessage += " : " + authException.getCause().getMessage().replace("\u0000", "");
		}
		response.getOutputStream().println(errorMessage);
	}
}