package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.properties.diff.AbstractDifferingPropertyOutput;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            final Map<String, String> dataMap = data.asMap(String.class, String.class);

            List<String> onlyLeftPropertiesName = actualPropertiesDiff.getOnlyLeft().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> onlyLeftPropertiesNameExpected = Stream.of(dataMap.get("only_left")).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            assertEquals(onlyLeftPropertiesNameExpected, onlyLeftPropertiesName);

            List<String> onlyRightPropertiesName = actualPropertiesDiff.getOnlyRight().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> onlyRightPropertiesNameExpected = Stream.of(dataMap.get("only_right")).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            assertEquals(onlyRightPropertiesNameExpected, onlyRightPropertiesName);

            List<String> commonPropertiesName = actualPropertiesDiff.getCommon().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> commonPropertiesNameExpected = Stream.of(dataMap.get("common")).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            assertEquals(commonPropertiesNameExpected, commonPropertiesName);

            List<String> differingPropertiesName = actualPropertiesDiff.getDiffering().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> differingPropertiesNameExpected = Stream.of(dataMap.get("differing")).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            assertEquals(differingPropertiesNameExpected, differingPropertiesName);
        });
    }

}
