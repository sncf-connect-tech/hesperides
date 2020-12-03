package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.platforms.PropertiesEventOutput;
import org.hesperides.core.presentation.io.platforms.PropertiesEventOutput.UpdatedPropertyOutput;
import org.hesperides.core.presentation.io.platforms.PropertiesEventOutput.ValuedPropertyOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hesperides.test.bdd.commons.DataTableHelper.decodeValue;

public class GetPropertiesEvents extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;

    public GetPropertiesEvents() {

        When("^I get the( global)? properties events(?: for this module)?(?: with page (\\d) and size (\\d))?$", (
                String globalProperties, Integer page, Integer size) -> {
            String propertiesPath = isEmpty(globalProperties) ? deployedModuleBuilder.buildPropertiesPath() : "#";
            platformClient.getPropertiesEvents(platformBuilder.buildInput(), propertiesPath, page, size);
        });

        Then("^the properties event at index (\\d) has these (added|updated|removed) properties$", (
                Integer eventIndex, String changeNature, DataTable dataTable) -> {

            PropertiesEventOutput propertiesEvent = testContext.getResponseBody(PropertiesEventOutput[].class)[eventIndex];
            switch (changeNature) {
                case "added":
                    List<ValuedPropertyOutput> expectedAddedProperties = dataTable.asList(ValuedPropertyOutput.class);
                    assertThat(propertiesEvent.getAddedProperties(), containsInAnyOrder(expectedAddedProperties.toArray()));
                    break;
                case "updated":
                    List<UpdatedPropertyOutput> expectedUpdatedProperties = dataTable.asList(UpdatedPropertyOutput.class);
                    assertThat(propertiesEvent.getUpdatedProperties(), containsInAnyOrder(expectedUpdatedProperties.toArray()));
                    break;
                case "removed":
                    List<ValuedPropertyOutput> expectedRemovedProperties = dataTable.asList(ValuedPropertyOutput.class);
                    assertThat(propertiesEvent.getRemovedProperties(), containsInAnyOrder(expectedRemovedProperties.toArray()));
                    break;
                default:
                    throw new RuntimeException("Wrong type of properties change nature: ${changeNature}");
            }
        });
    }

    @DataTableType
    public ValuedPropertyOutput valuedPropertyOutput(Map<String, String> entry) {
        return new ValuedPropertyOutput(
                decodeValue(entry.get("name")),
                decodeValue(entry.get("value"))
        );
    }

    @DataTableType
    public UpdatedPropertyOutput updatedPropertyOutput(Map<String, String> entry) {
        return new UpdatedPropertyOutput(
                decodeValue(entry.get("name")),
                decodeValue(entry.get("old_value")),
                decodeValue(entry.get("new_value"))
        );
    }
}
