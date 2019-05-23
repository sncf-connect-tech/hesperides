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
package org.hesperides.test.regression.validation;

import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.presentation.PresentationConfiguration;
import org.hesperides.test.regression.RegressionLogs;
import org.hesperides.test.regression.RestConfiguration;
import org.hesperides.test.regression.errors.Diff;
import org.hesperides.test.regression.errors.UnexpectedException;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Slf4j
public abstract class AbstractValidation {

    @Autowired
    private RestConfiguration restConfiguration;
    @Autowired
    private RegressionLogs regressionLogs;
    @Autowired
    private RestTemplate restTemplate;

    protected void testEndpoint(String entityKey, String restEndpoint, Class responseType, Object... endpointVariables) {
        testEndpointAndGetResult(entityKey, restEndpoint, responseType, endpointVariables);
    }

    protected void testFileEndpoint(String entityKey, String restEndpoint) {
        testEndpointAndGetResult(entityKey, true, restEndpoint, String.class);
    }

    protected <T> Optional<T> testEndpointAndGetResult(String entityKey, String restEndpoint, Class<T> responseType, Object... endpointVariables) {
        return testEndpointAndGetResult(entityKey, false, restEndpoint, responseType, endpointVariables);
    }

    private <T> Optional<T> testEndpointAndGetResult(String entityKey, boolean isFileUrl, String restEndpoint, Class<T> responseType, Object... endpointVariables) {

        String latestUri = restConfiguration.getLatestUri(restEndpoint);
        String testingUri = restConfiguration.getTestingUri(restEndpoint);

        String readableLatestUri = getReadableUri(isFileUrl, latestUri, endpointVariables);
        String readableTestingUri = getReadableUri(isFileUrl, testingUri, endpointVariables);

        log.debug("Testing endpoint " + readableLatestUri);

        Optional<T> result = Optional.empty();
        try {
            ResponseEntity<String> latestResult = getResult(isFileUrl, latestUri, endpointVariables);
            ResponseEntity<String> testingResult = getResult(isFileUrl, testingUri, endpointVariables);

            Assert.assertEquals(latestResult.getStatusCode(), testingResult.getStatusCode());
            Assert.assertEquals(latestResult.getBody(), testingResult.getBody());

            result = responseType.equals(String.class)
                    ? (Optional<T>) Optional.ofNullable(latestResult.getBody())
                    : Optional.of(PresentationConfiguration.gson().fromJson(latestResult.getBody(), responseType));

        } catch (AssertionError e) {
            regressionLogs.logAndSaveDiff(new Diff(entityKey, readableLatestUri, readableTestingUri, e.getMessage()));
        } catch (Throwable t) {
            regressionLogs.logAndSaveException(new UnexpectedException(entityKey, readableLatestUri, readableTestingUri, t.getMessage()));
        }
        return result;
    }

    private String getReadableUri(boolean isFileUrl, String uri, Object[] endpointVariables) {
        return isFileUrl ? uri : restTemplate.getUriTemplateHandler().expand(uri, endpointVariables).toString();
    }

    private <T> ResponseEntity<T> getResult(boolean isFileUrl, String uri, Object[] endpointVariables) {
        return isFileUrl ? restTemplate.getForEntity(urlDecode(uri), (Class<T>) String.class) : restTemplate.getForEntity(uri, (Class<T>) String.class, endpointVariables);
    }

    private static URI urlDecode(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Impossible de convertir l'url encodée " + url);
        }
    }
}
