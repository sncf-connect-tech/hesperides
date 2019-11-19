package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.diff.AbstractDifferingPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
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

        Then("the resulting diff matches", (DataTable data) -> {
            assertOK();
            PropertiesDiffOutput actualPropertiesDiff = testContext.getResponseBody(PropertiesDiffOutput.class);
            Set<Diff> expectedPropertiesDiff = new HashSet<>(data.asList(Diff.class));

            Set<String> onlyLeftPropertiesName = getPropertiesName(actualPropertiesDiff.getOnlyLeft());
            Set<String> onlyLeftPropertiesNameExpected = Diff.getOnlyLeft(expectedPropertiesDiff);
            assertEquals("only_left", onlyLeftPropertiesNameExpected, onlyLeftPropertiesName);

            Set<String> onlyRightPropertiesName = getPropertiesName(actualPropertiesDiff.getOnlyRight());
            Set<String> onlyRightPropertiesNameExpected = Diff.getOnlyRight(expectedPropertiesDiff);
            assertEquals("only_right", onlyRightPropertiesNameExpected, onlyRightPropertiesName);

            Set<String> commonPropertiesName = getPropertiesName(actualPropertiesDiff.getCommon());
            Set<String> commonPropertiesNameExpected = Diff.getCommon(expectedPropertiesDiff);
            assertEquals("common", commonPropertiesNameExpected, commonPropertiesName);

            Set<String> differingPropertiesName = getPropertiesName(actualPropertiesDiff.getDiffering());
            Set<String> differingPropertiesNameExpected = Diff.getDiffering(expectedPropertiesDiff);
            assertEquals("differing", differingPropertiesNameExpected, differingPropertiesName);
        });
    }

    private String getPropertiesPath(String globalProperties, PlatformBuilder platform) {
        return isNotEmpty(globalProperties) ? "#" : platform.getDeployedModuleBuilders().get(0).buildPropertiesPath();
    }

    private String getInstance(String instanceProperties, PlatformBuilder platform) {
        return isNotEmpty(instanceProperties) ? platform.getDeployedModuleBuilders().get(0).getInstanceBuilders().get(0).getName() : null;
    }

    private Set<String> getPropertiesName(Set<AbstractDifferingPropertyOutput> propertiesDiff) {
        return propertiesDiff.stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toSet());
    }

    @Value
    private static class Diff {
        String onlyLeft;
        String onlyRight;
        String common;
        String differing;

        private static Set<String> getOnlyLeft(Set<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getOnlyLeft);
        }

        private static Set<String> getOnlyRight(Set<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getOnlyRight);
        }

        private static Set<String> getCommon(Set<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getCommon);
        }

        private static Set<String> getDiffering(Set<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getDiffering);
        }

        private static Set<String> getTypeOfDiff(Set<Diff> diffs, Function<Diff, String> mapper) {
            return diffs.stream().map(mapper).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        }
    }
}
