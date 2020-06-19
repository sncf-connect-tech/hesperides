package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.platforms.PlatformEventOutput;
import org.hesperides.core.presentation.io.platforms.PlatformEventOutput.*;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetPlatformEvents extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;

    public GetPlatformEvents() {

        When("^I get this platform events(?: with page (\\d) and size (\\d))?$", (Integer page, Integer size) -> {
            platformClient.getPlatformEvents(platformBuilder.buildInput(), page, size);
        });

        Then("^the platform event at index (\\d) contains \"([^\"]*)\"(?: with old version \"([^\"]*)\" and new version \"([^\"]*)\")?$", (
                Integer eventIndex, String changeName, String oldVersion, String newVersion) -> {
            List<PlatformEventOutput> platformEvents = testContext.getResponseBodyAsList();
            List<PlatformChangeOutput> changes = platformEvents.get(eventIndex).getChanges();
            List<String> changesName = changes.stream().map(PlatformChangeOutput::getChangeName).collect(toList());
            assertTrue(changesName.contains(changeName));

            for (PlatformChangeOutput change : changes) {
                if (change instanceof PlatformVersionUpdatedOutput && "platform_version_updated".equals(changeName)) {
                    PlatformVersionUpdatedOutput platformVersionUpdated = (PlatformVersionUpdatedOutput) change;
                    assertEquals(oldVersion, platformVersionUpdated.getOldVersion());
                    assertEquals(newVersion, platformVersionUpdated.getNewVersion());

                } else if (change instanceof DeployedModuleUpdatedOutput && "deployed_module_updated".equals(changeName)) {
                    DeployedModuleUpdatedOutput deployedModuleVersionUpdated = (DeployedModuleUpdatedOutput) change;
                    String currentVersion = deployedModuleBuilder.getVersion();
                    deployedModuleBuilder.withVersion(oldVersion).buildPropertiesPath();
                    assertEquals(deployedModuleBuilder.withVersion(oldVersion).buildPropertiesPath(), deployedModuleVersionUpdated.getOldPropertiesPath());
                    assertEquals(deployedModuleBuilder.withVersion(newVersion).buildPropertiesPath(), deployedModuleVersionUpdated.getNewPropertiesPath());
                    deployedModuleBuilder.withVersion(currentVersion).buildPropertiesPath();

                } else if (change instanceof DeployedModuleAddedOutput && "deployed_module_added".equals(changeName)) {
                    DeployedModuleAddedOutput deployedModuleAdded = (DeployedModuleAddedOutput) change;
                    assertEquals(deployedModuleBuilder.buildPropertiesPath(), deployedModuleAdded.getPropertiesPath());

                } else if (change instanceof DeployedModuleRemovedOutput && "deployed_module_removed".equals(changeName)) {
                    DeployedModuleRemovedOutput deployedModuleRemoved = (DeployedModuleRemovedOutput) change;
                    assertEquals(deployedModuleBuilder.buildPropertiesPath(), deployedModuleRemoved.getPropertiesPath());
                }
            }
        });
    }
}
