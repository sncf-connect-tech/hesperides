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
package org.hesperides.tests.bdd.workshopproperties.contexts;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.WorkshopPropertyInput;
import org.hesperides.core.presentation.io.WorkshopPropertyOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.workshopproperties.samples.WorkshopPropertySamples;
import org.springframework.http.ResponseEntity;

public class WorkshopPropertyContext extends CucumberSpringBean implements En {

    private String key;

    public WorkshopPropertyContext() {
        Given("^an existing workshop property", () -> {
            createWorkshopProperty();
        });
    }

    private void createWorkshopProperty() {
        WorkshopPropertyInput workshopPropertyInput = WorkshopPropertySamples.getWorkshopPropertyInputWithDefaultValues();
        createWorkshopProperty(workshopPropertyInput);
    }

    public ResponseEntity<WorkshopPropertyOutput> createWorkshopProperty(WorkshopPropertyInput workshopPropertyInput) {
        ResponseEntity<WorkshopPropertyOutput> response = rest.getTestRest().postForEntity(
                "/workshop/properties", workshopPropertyInput, WorkshopPropertyOutput.class);
        WorkshopPropertyOutput workshopPropertyOutput = response.getBody();
        key = workshopPropertyOutput.getKey();
        return response;
    }

    public ResponseEntity<WorkshopPropertyOutput> updateWorkshopProperty(WorkshopPropertyInput workshopPropertyInput) {
        return rest.putForEntity("/workshop/properties", workshopPropertyInput, WorkshopPropertyOutput.class);
    }

    public ResponseEntity<WorkshopPropertyOutput> retrieveExistingWorkshopProperty() {
        return rest.getTestRest().getForEntity("/workshop/properties/{key}",
                WorkshopPropertyOutput.class, key);
    }
}
