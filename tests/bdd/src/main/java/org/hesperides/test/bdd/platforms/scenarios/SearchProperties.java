/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java8.En;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.presentation.io.platforms.properties.PropertySearchResultOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

public class SearchProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;

    public SearchProperties() {

        When("^I( try to)? search for properties" +
                "(?: by name \"([^\"]+)\")?" +
                "(?: (?:by|and)? value \"([^\"]+)\")?$", (String tryTo,
                                                          String propertyName,
                                                          String propertyValue) -> {
            platformClient.searchProperties(propertyName, propertyValue, tryTo);
        });

        When("I try to search for properties without a name or a value", () -> {
            platformClient.searchProperties(null, null, "should-fail");
        });

        Then("the list of properties found is", (DataTable dataTable) -> {
            assertOK();
            List<SearchResult> expectedResults = dataTable.asList(SearchResult.class);
            List<SearchResult> actualResults = SearchResult.fromPropertySearchResultOutput(testContext.getResponseBodyAsList());
            assertThat(actualResults, containsInAnyOrder(expectedResults.toArray()));
        });

        Then("the list of properties found is empty", () -> {
            assertOK();
            List<PropertySearchResultOutput> results = testContext.getResponseBodyAsList();
            assertEquals(0, results.size());
        });
    }


    @DataTableType
    public SearchResult toSearchResult(Map<String, String> entry) {
        return new SearchResult(
                entry.get("propertyName"),
                entry.get("propertyValue"),
                entry.getOrDefault("applicationName", "test-application")
        );
    }

    @Value
    @AllArgsConstructor
    public static class SearchResult {
        String propertyName;
        String propertyValue;
        String applicationName;

        public SearchResult(PropertySearchResultOutput output) {
            propertyName = output.getPropertyName();
            propertyValue = output.getPropertyValue();
            applicationName = output.getApplicationName();
        }

        public static List<SearchResult> fromPropertySearchResultOutput(List<PropertySearchResultOutput> outputs) {
            return outputs.stream().map(SearchResult::new).collect(toList());
        }
    }
}
