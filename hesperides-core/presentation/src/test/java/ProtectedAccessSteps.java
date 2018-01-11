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

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtectedAccessSteps implements En {
    private Response response;

    public ProtectedAccessSteps() {
        When("^an (un)?authenticated user tries to retrieve the modules name$", (final String notAuthenticated) -> {
            boolean isAuthenticated = StringUtils.isEmpty(notAuthenticated);
            response = ProtectedAccessTest.query("/toto", isAuthenticated);
        });

        Then("^he should( not)? be authorized to get them$", (final String notAuthorized) -> {
            Response.Status expectedStatus = StringUtils.isBlank(notAuthorized) ? Response.Status.OK : Response.Status.UNAUTHORIZED;
            assertThat(response.getStatus()).isEqualTo(expectedStatus.getStatusCode());
        });
    }
}
