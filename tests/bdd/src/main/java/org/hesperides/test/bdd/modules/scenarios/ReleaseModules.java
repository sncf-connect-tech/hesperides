package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class ReleaseModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    public ReleaseModules() {

        When("^I( try to)? release this module" +
                "(?: in version \"(.*)\")?" +
                "( without specifying its version)?$", (
                String tryTo,
                String releasedVersion,
                String withoutVersion) -> {

            if (isNotEmpty(withoutVersion)) {
                moduleBuilder.withVersion(null);
            }
            releaseModule(moduleClient, moduleBuilder, moduleHistory, releasedVersion, tryTo);
        });

        Then("^the module release is rejected with a not found error$", this::assertNotFound);

        Then("^the module release is rejected with a conflict error$", this::assertConflict);

        Then("^the module release is rejected with a bad request error$", this::assertBadRequest);
    }

    static void releaseModule(ModuleClient moduleClient, ModuleBuilder moduleBuilder, ModuleHistory moduleHistory, String releaseVersion, String tryTo) {
        moduleClient.releaseModule(moduleBuilder.build(), releaseVersion, tryTo);
        if (isNotEmpty(releaseVersion)) {
            moduleBuilder.withVersion(releaseVersion);
        }
        moduleBuilder.withVersionType(VersionType.RELEASE);
        moduleBuilder.updateTemplatesNamespace();
        moduleHistory.addModuleBuilder(moduleBuilder);
    }
}
