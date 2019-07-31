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

import java.util.Collections;
import java.util.List;
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
        When("^I get the (stored|final)( global)? properties diff(?: of this module)? between platforms \"([^\"]+)\" and \"([^\"]+)\"$", (
                String storedOrFinal, String global, String fromPlatformName, String toPlatformName) -> {
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

            List<String> onlyLeftPropertiesName = actualPropertiesDiff.getOnlyLeft().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> onlyLeftPropertiesNameExpected = Collections.singletonList(data.asMap(String.class, String.class).get("only_left"));
            assertEquals(onlyLeftPropertiesNameExpected, onlyLeftPropertiesName);

            List<String> onlyRightPropertiesName = actualPropertiesDiff.getOnlyRight().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> onlyRightPropertiesNameExpected = Collections.singletonList(data.asMap(String.class, String.class).get("only_right"));
            assertEquals(onlyRightPropertiesNameExpected, onlyRightPropertiesName);

            List<String> commonPropertiesName = actualPropertiesDiff.getCommon().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> commonPropertiesNameExpected = Collections.singletonList(data.asMap(String.class, String.class).get("common"));
            assertEquals(commonPropertiesNameExpected, commonPropertiesName);

            List<String> differingPropertiesName = actualPropertiesDiff.getDiffering().stream().map(AbstractDifferingPropertyOutput::getName).collect(Collectors.toList());
            List<String> differingPropertiesNameExpected = Collections.singletonList(data.asMap(String.class, String.class).get("differing"));
            assertEquals(differingPropertiesNameExpected, differingPropertiesName);
        });
    }

}
