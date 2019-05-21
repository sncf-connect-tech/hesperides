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
package org.hesperides.test.nr.validation;

import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.presentation.PresentationConfiguration;
import org.hesperides.test.nr.NRConfiguration;
import org.hesperides.test.nr.errors.Diff;
import org.hesperides.test.nr.errors.UnexpectedException;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractValidation {

    private static List<Diff> diffs = new ArrayList<>();
    private static List<UnexpectedException> exceptions = new ArrayList<>();

    @Autowired
    private NRConfiguration nrConfiguration;
    @Autowired
    private RestTemplate latestRestTemplate;
    @Autowired
    private RestTemplate testingRestTemplate;

    private void logDiffs() {
        log.warn("{} endpoint diffs", diffs.size());
        diffs.forEach(Diff::log);
    }

    private void logUnexpectedExceptions() {
        log.warn("{} unexpected exceptions", exceptions.size());
        diffs.forEach(Diff::log);
    }

//    private void test(String restEndpoint, Object... endpointVariables) {
//        test(restEndpoint, String.class, endpointVariables);
//    }

    void test(String restEndpoint, Class responseType, Object... endpointVariables) {
        test(restEndpoint, false, responseType, endpointVariables);
    }

    void test(String restEndpoint, boolean isFileUrl, Class responseType, Object... endpointVariables) {
        testAndGetResult(restEndpoint, isFileUrl, responseType, endpointVariables);
    }

    <T> Optional<T> testAndGetResult(String restEndpoint, Class<T> responseType, Object... endpointVariables) {
        return testAndGetResult(restEndpoint, false, responseType, endpointVariables);
    }

    private <T> Optional<T> testAndGetResult(String restEndpoint, boolean isFileUrl, Class<T> responseType, Object... endpointVariables) {

        String latestUri = nrConfiguration.getLatestUri(restEndpoint);
        String testingUri = nrConfiguration.getTestingUri(restEndpoint);

        String readableLatestUri = getReadableUri(isFileUrl, latestUri, endpointVariables, latestRestTemplate);
        String readableTestingUri = getReadableUri(isFileUrl, testingUri, endpointVariables, testingRestTemplate);
        log.debug("Testing endpoint " + readableLatestUri);

        Optional<T> result = Optional.empty();
        try {
            ResponseEntity<String> latestResult = getResult(latestRestTemplate, isFileUrl, String.class, latestUri, endpointVariables);
            ResponseEntity<String> testingResult = getResult(testingRestTemplate, isFileUrl, String.class, testingUri, endpointVariables);

            try {
                Assert.assertEquals(latestResult.getStatusCode(), testingResult.getStatusCode());
                Assert.assertEquals(latestResult.getBody(), testingResult.getBody());
                result = Optional.of(PresentationConfiguration.gson().fromJson(latestResult.getBody(), responseType));

            } catch (Throwable t) {
                logAndSaveDiff(new Diff(readableLatestUri, readableTestingUri, t.getMessage()));
            }

        } catch (Throwable t) {
            logAndSaveException(new UnexpectedException(readableLatestUri, readableTestingUri, t.getMessage()));
        }
        return result;
    }

    private static String getReadableUri(boolean isFileUrl, String uri, Object[] endpointVariables, RestTemplate restTemplate) {
        return isFileUrl ? uri : restTemplate.getUriTemplateHandler().expand(uri, endpointVariables).toString();
    }

    private static <T> ResponseEntity<T> getResult(RestTemplate restTemplate, boolean isFileUrl, Class<T> responseType, String uri, Object[] endpointVariables) {
        return isFileUrl ? restTemplate.getForEntity(decode(uri), responseType) : restTemplate.getForEntity(uri, responseType, endpointVariables);
    }

    private static URI decode(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Impossible de convertir l'url encodée " + url);
        }
    }

    private static void logAndSaveDiff(Diff diff) {
        diff.log();
        diffs.add(diff);
    }

    private static void logAndSaveException(UnexpectedException exception) {
        exception.log();
        exceptions.add(exception);
    }
}
