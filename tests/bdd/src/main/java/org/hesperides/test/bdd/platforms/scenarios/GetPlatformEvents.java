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
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
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
                Integer eventIndex, String expectedChangeName, String oldVersion, String newVersion) -> {
            List<PlatformEventOutput> actualPlatformEvents = testContext.getResponseBodyAsList();
            List<PlatformChangeOutput> actualEventChanges = actualPlatformEvents.get(eventIndex).getChanges();
            Map<String, PlatformChangeOutput> actualChangesByName = actualEventChanges.stream()
                    .collect(toMap(PlatformChangeOutput::getChangeName, Function.identity()));

            assertTrue(actualChangesByName.containsKey(expectedChangeName));
            PlatformChangeOutput actualChange = actualChangesByName.get(expectedChangeName);

            if ("platform_created".equals(expectedChangeName)) {
                assertTrue(actualChange instanceof PlatformCreatedOutput);

            } else if ("platform_version_updated".equals(expectedChangeName)) {
                assertTrue(actualChange instanceof PlatformVersionUpdatedOutput);
                PlatformVersionUpdatedOutput platformVersionUpdated = (PlatformVersionUpdatedOutput) actualChange;
                assertEquals(oldVersion, platformVersionUpdated.getOldVersion());
                assertEquals(newVersion, platformVersionUpdated.getNewVersion());

            } else if ("deployed_module_updated".equals(expectedChangeName)) {
                assertTrue(actualChange instanceof DeployedModuleUpdatedOutput);
                DeployedModuleUpdatedOutput deployedModuleVersionUpdated = (DeployedModuleUpdatedOutput) actualChange;
                String currentVersion = deployedModuleBuilder.getVersion();
                deployedModuleBuilder.withVersion(oldVersion).buildPropertiesPath();
                assertEquals(deployedModuleBuilder.withVersion(oldVersion).buildPropertiesPath(), deployedModuleVersionUpdated.getOldPropertiesPath());
                assertEquals(deployedModuleBuilder.withVersion(newVersion).buildPropertiesPath(), deployedModuleVersionUpdated.getNewPropertiesPath());
                deployedModuleBuilder.withVersion(currentVersion).buildPropertiesPath();

            } else if ("deployed_module_added".equals(expectedChangeName)) {
                assertTrue(actualChange instanceof DeployedModuleAddedOutput);
                DeployedModuleAddedOutput deployedModuleAdded = (DeployedModuleAddedOutput) actualChange;
                assertEquals(deployedModuleBuilder.buildPropertiesPath(), deployedModuleAdded.getPropertiesPath());

            } else if ("deployed_module_removed".equals(expectedChangeName)) {
                assertTrue(actualChange instanceof DeployedModuleRemovedOutput);
                DeployedModuleRemovedOutput deployedModuleRemoved = (DeployedModuleRemovedOutput) actualChange;
                assertEquals(deployedModuleBuilder.buildPropertiesPath(), deployedModuleRemoved.getPropertiesPath());

            } else {
                throw new IllegalArgumentException("Event at index " + eventIndex + " does not contain change \"" + expectedChangeName + "\"");
            }
        });
    }
}
