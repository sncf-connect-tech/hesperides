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
package org.hesperides.tests.bddrefacto.technos;

import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TechnoClient {

    @Autowired
    private TestRestTemplate testRestTemplate;

    public ResponseEntity create(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        return testRestTemplate.postForEntity(
                "/templates/packages/{name}/{version}/workingcopy/templates",
                templateInput,
                responseType,
                technoInput.getName(),
                technoInput.getVersion());
    }

    public ResponseEntity search(String terms) {
        return testRestTemplate.postForEntity("/templates/packages/perform_search?terms=" + terms, null, TechnoIO[].class);
    }

    public ResponseEntity get(TemplateContainer.Key technoKey, Class responseType) {
        return testRestTemplate.getForEntity("/templates/packages/{name}/{version}/{type}",
                responseType,
                technoKey.getName(),
                technoKey.getVersion(),
                technoKey.getVersionType(),
                TechnoIO.class);
    }

    public ResponseEntity releaseTechno(TemplateContainer.Key technoKey, Class responseType) {
        return testRestTemplate.postForEntity("/templates/packages/create_release?techno_name={name}&techno_version={version}",
                null,
                responseType,
                technoKey.getName(),
                technoKey.getVersion());
    }

    public ResponseEntity delete(TemplateContainer.Key technoKey, Class responseType) {
        return testRestTemplate.exchange("/templates/packages/{name}/{version}/{type}",
                HttpMethod.DELETE,
                null,
                responseType,
                technoKey.getName(),
                technoKey.getVersion(),
                technoKey.getVersionType());
    }
}
