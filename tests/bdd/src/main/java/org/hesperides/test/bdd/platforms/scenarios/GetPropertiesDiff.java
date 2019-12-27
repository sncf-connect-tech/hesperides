package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.java.en.Then;
import io.cucumber.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.diff.AbstractDifferingPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetPropertiesDiff extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private PlatformBuilder platformBuilder;

    public GetPropertiesDiff() {

        When("^I get the( global)?( instance)? properties diff on (stored|final) values between platforms \"([^\"]+)\" and \"([^\"]+)\"$", (
                String globalProperties, String instanceProperties, String storedOrFinal, String fromPlatformName, String toPlatformName) -> {

            PlatformBuilder fromPlatform = platformHistory.getPlatformByName(fromPlatformName);
            PlatformBuilder toPlatform = platformHistory.getPlatformByName(toPlatformName);

            String fromPropertiesPath = getPropertiesPath(globalProperties, fromPlatform);
            String toPropertiesPath = getPropertiesPath(globalProperties, toPlatform);

            String fromInstance = getInstance(instanceProperties, fromPlatform);
            String toInstance = getInstance(instanceProperties, toPlatform);

            platformClient.getPropertiesDiff(
                    fromPlatform.buildInput(),
                    fromPropertiesPath,
                    fromInstance,
                    toPlatform.buildInput(),
                    toPropertiesPath,
                    toInstance,
                    storedOrFinal.equals("stored"),
                    null);
        });

        When("^I get the( global)?( instance)? properties diff on (stored|final) values between the first and second version of the platform values$", (
                String globalProperties, String instanceProperties, String storedOrFinal) -> {

            PlatformIO platform = platformBuilder.buildInput();
            String propertiesPath = getPropertiesPath(globalProperties, platformBuilder);
            String instance = getInstance(instanceProperties, platformBuilder);
            Long timestamp = platformHistory.getPenultimatePlatformTimestamp(platformBuilder.getApplicationName(), platformBuilder.getPlatformName());

            platformClient.getPropertiesDiff(
                    platform,
                    propertiesPath,
                    instance,
                    platform,
                    propertiesPath,
                    instance,
                    storedOrFinal.equals("stored"),
                    timestamp);
        });

        When("^I get the properties diff on (stored|final) values between the currently deployed modules$", (String storedOrFinal) -> {

            PlatformIO platform = platformBuilder.buildInput();
            String fromPropertiesPath = platformBuilder.getDeployedModuleBuilders().get(0).buildPropertiesPath();
            String toPropertiesPath = platformBuilder.getDeployedModuleBuilders().get(1).buildPropertiesPath();

            platformClient.getPropertiesDiff(
                    platform,
                    fromPropertiesPath,
                    null,
                    platform,
                    toPropertiesPath,
                    null,
                    storedOrFinal.equals("stored"),
                    null);
        });

        When("^I get the properties diff on final values of this platform between module versions \"([^\"]+)\" and \"([^\"]+)\"$", (
                String fromModuleVersion, String toModuleVersion) -> {

            String fromPropertiesPath = platformBuilder.findDeployedModuleBuilderByVersion(fromModuleVersion).buildPropertiesPath();
            String toPropertiesPath = platformBuilder.findDeployedModuleBuilderByVersion(toModuleVersion).buildPropertiesPath();

            PlatformIO platform = platformBuilder.buildInput();
            String instanceName = null;

            platformClient.getPropertiesDiff(
                    platform,
                    fromPropertiesPath,
                    instanceName,
                    platform,
                    toPropertiesPath,
                    instanceName,
                    false,
                    null);
        });

        Then("the diff is empty", () -> {
            assertOK();
            PropertiesDiffOutput actualPropertiesDiff = testContext.getResponseBody(PropertiesDiffOutput.class);
            assertThat(actualPropertiesDiff.getCommon(), is(empty()));
            assertThat(actualPropertiesDiff.getDiffering(), is(empty()));
            assertThat(actualPropertiesDiff.getOnlyLeft(), is(empty()));
            assertThat(actualPropertiesDiff.getOnlyRight(), is(empty()));
        });
    }

    @Then("the resulting diff matches")
    public void theResultingDiffMatches(List<Map<String, String>> data) {
        assertOK();
        PropertiesDiffOutput actualPropertiesDiff = testContext.getResponseBody(PropertiesDiffOutput.class);

        Set<String> onlyLeftPropertyNames = getPropertyNames(actualPropertiesDiff.getOnlyLeft());
        Set<String> onlyLeftPropertyNamesExpected = expectedPropertyNamesFromDatatableLines(data, "onlyLeft");
        assertEquals("only_left", onlyLeftPropertyNamesExpected, onlyLeftPropertyNames);

        Set<String> onlyRightPropertyNames = getPropertyNames(actualPropertiesDiff.getOnlyRight());
        Set<String> onlyRightPropertyNamesExpected = expectedPropertyNamesFromDatatableLines(data, "onlyRight");
        assertEquals("only_right", onlyRightPropertyNamesExpected, onlyRightPropertyNames);

        Set<String> commonPropertyNames = getPropertyNames(actualPropertiesDiff.getCommon());
        Set<String> commonPropertyNamesExpected = expectedPropertyNamesFromDatatableLines(data, "common");
        assertEquals("common", commonPropertyNamesExpected, commonPropertyNames);

        Set<String> differingPropertyNames = getPropertyNames(actualPropertiesDiff.getDiffering());
        Set<String> differingPropertyNamesExpected = expectedPropertyNamesFromDatatableLines(data, "differing");
        assertEquals("differing", differingPropertyNamesExpected, differingPropertyNames);
    }

    private Set<String> expectedPropertyNamesFromDatatableLines(List<Map<String, String>> data, String columnName) {
        return data.stream()
                .map(line -> line.get(columnName))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
    }

    private String getPropertiesPath(String globalProperties, PlatformBuilder platform) {
        return isNotEmpty(globalProperties) ? "#" : platform.getDeployedModuleBuilders().get(0).buildPropertiesPath();
    }

    private String getInstance(String instanceProperties, PlatformBuilder platform) {
        return isNotEmpty(instanceProperties) ? platform.getDeployedModuleBuilders().get(0).getInstanceBuilders().get(0).getName() : null;
    }

    private Set<String> getPropertyNames(Set<AbstractDifferingPropertyOutput> propertiesDiff) {
        return propertiesDiff.stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toSet());
    }
}
