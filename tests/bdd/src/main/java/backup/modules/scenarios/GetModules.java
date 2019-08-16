package backup.modules.scenarios;

import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetModules extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    public GetModules() {

        Given("^a module that doesn't exist$", () -> {
            moduleBuilder.withName("nope");
        });

        When("^I( try to)? get the module detail(?: for a module type \"(.*)\")?( with the wrong letter case)?$", (String tryTo, String moduleType, String withWrongLetterCase) -> {
            if (StringUtils.isNotEmpty(moduleType)) {
                moduleBuilder.withVersionType(moduleType);
            }
            ModuleIO moduleInput = moduleBuilder.build();
            if (StringUtils.isNotEmpty(withWrongLetterCase)) {
                moduleInput = new OldModuleBuilder().withName(moduleBuilder.getName().toUpperCase()).build();
            }
            testContext.setResponseEntity(moduleClient.get(moduleInput, moduleBuilder.getVersionType(), getResponseType(tryTo, ModuleIO.class)));
        });

        Then("^the module detail is successfully retrieved$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionId(1).build();
            ModuleIO actualModule = testContext.getResponseBody(ModuleIO.class);
            assertEquals(expectedModule, actualModule);
        });
    }
}
