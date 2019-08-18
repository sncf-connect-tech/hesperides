package oldplatformscenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SearchPlatforms extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;

    public SearchPlatforms() {

        When("^I( try to)? search for the platform \"([^\"]*)\" in the application \"([^\"]*)\"$", (
                String tryTo, String platformName, String applicationName) -> {
            testContext.setResponseEntity(oldPlatformClient.search(applicationName, platformName, getResponseType(tryTo, SearchResultOutput[].class)));
        });

        Then("^the platform search result contains (\\d+) entr(?:y|ies)?$", (Integer nbEntries) -> {
            assertOK();
            List<SearchResultOutput> result = Arrays.asList(testContext.getResponseBody(SearchResultOutput[].class));
            assertEquals(nbEntries.intValue(), result.size());
        });

        Then("^the platform \"([^\"]*)\" is found$", (String platformName) -> {
            assertOK();
            List<SearchResultOutput> result = Arrays.asList(testContext.getResponseBody(SearchResultOutput[].class));
            assertEquals(platformName, result.get(0).getName());
        });

        Then("^the platform search is rejected with a bad request error$", () -> {
            assertBadRequest();
        });
    }

}
