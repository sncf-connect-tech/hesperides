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
package org.hesperides.test.bdd.events;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.test.bdd.templatecontainers.VersionTypes;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.hesperides.test.bdd.commons.TestContext.getResponseType;

@Component
public class EventClient {

    private final RestTemplate restTemplate;

    public EventClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    void getModuleEvents(ModuleIO moduleInput, String tryTo) {
        restTemplate.getForEntity("/events/modules/{name}/{version}/{type}",
                getResponseType(tryTo, EventOutput[].class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionTypes.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    void getPlatformEvents(PlatformIO platformInput, String tryTo) {
        restTemplate.getForEntity("/events/platforms/{name}/{version}",
                getResponseType(tryTo, EventOutput[].class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }
}
