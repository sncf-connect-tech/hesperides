package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.properties.diff.AbstractDifferingPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class GetPropertiesDiff extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private PlatformHistory platformHistory;

    public GetPropertiesDiff() {
        When("^I get the( global)? properties diff on (stored|final) values between platforms \"([^\"]+)\" and \"([^\"]+)\"$", (
                String global, String storedOrFinal, String fromPlatformName, String toPlatformName) -> {
            String propertiesPath = StringUtils.isNotEmpty(global) ? "#" : moduleBuilder.getPropertiesPath();
            testContext.setResponseEntity(platformClient.getPropertiesDiff(
                    platformHistory.getPlatformByName(fromPlatformName),
                    propertiesPath,
                    platformHistory.getPlatformByName(toPlatformName),
                    propertiesPath,
                    storedOrFinal.equals("stored"),
                    null,
                    PropertiesDiffOutput.class
            ));
        });

        Then("the diff is successfully retrieved", this::assertOK);

        And("the resulting diff match these values", (DataTable data) -> {
            PropertiesDiffOutput actualPropertiesDiff = testContext.getResponseBody(PropertiesDiffOutput.class);
            List<Diff> expectedPropertiesDiff = data.asList(Diff.class);

            List<String> onlyLeftPropertiesName = getPropertiesName(actualPropertiesDiff.getOnlyLeft());
            List<String> onlyLeftPropertiesNameExpected = Diff.getOnlyLeft(expectedPropertiesDiff);
            assertEquals("only_left", onlyLeftPropertiesNameExpected, onlyLeftPropertiesName);

            List<String> onlyRightPropertiesName = getPropertiesName(actualPropertiesDiff.getOnlyRight());
            List<String> onlyRightPropertiesNameExpected = Diff.getOnlyRight(expectedPropertiesDiff);
            assertEquals("only_right", onlyRightPropertiesNameExpected, onlyRightPropertiesName);

            List<String> commonPropertiesName = getPropertiesName(actualPropertiesDiff.getCommon());
            List<String> commonPropertiesNameExpected = Diff.getCommon(expectedPropertiesDiff);
            assertEquals("common", commonPropertiesNameExpected, commonPropertiesName);

            List<String> differingPropertiesName = getPropertiesName(actualPropertiesDiff.getDiffering());
            List<String> differingPropertiesNameExpected = Diff.getDiffering(expectedPropertiesDiff);
            assertEquals("differing", differingPropertiesNameExpected, differingPropertiesName);
        });
    }

    private List<String> getPropertiesName(Set<AbstractDifferingPropertyOutput> propertiesDiff) {
        return propertiesDiff.stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
    }

    @Value
    private static class Diff {
        String only_left;
        String only_right;
        String common;
        String differing;

        private static List<String> getOnlyLeft(List<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getOnly_left);
        }

        private static List<String> getOnlyRight(List<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getOnly_right);
        }

        private static List<String> getCommon(List<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getCommon);
        }

        private static List<String> getDiffering(List<Diff> diffs) {
            return getTypeOfDiff(diffs, Diff::getDiffering);
        }

        private static List<String> getTypeOfDiff(List<Diff> diffs, Function<Diff, String> mapper) {
            return diffs.stream().map(mapper).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        }
    }
}
