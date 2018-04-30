package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import javafx.scene.control.cell.MapValueFactory;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GetModuleInfo extends CucumberSpringBean implements En {

    private ResponseEntity<ModuleView> response;

    public GetModuleInfo() {

        When("^retrieving the module's info$", () -> {
            response = rest.getTestRest().getForEntity("/modules/test/1.0.0/workingcopy", ModuleView.class);
        });

        Then("^the module's info is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ModuleView module = response.getBody();
            assertEquals("test", module.getName());
            assertEquals("1.0.0", module.getVersion());
            assertEquals(true, module.isWorkingCopy());
            assertEquals(1, module.getVersionId().longValue());
            //TODO technos
            //TODO released
        });
    }
}
