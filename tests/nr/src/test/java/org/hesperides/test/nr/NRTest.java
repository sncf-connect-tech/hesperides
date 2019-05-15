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
package org.hesperides.test.nr;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.PresentationConfiguration;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.nr.errors.Diff;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {NRConfiguration.class})
@Slf4j
public class NRTest {

    private static List<Diff> diffs = new ArrayList<>();
    private static List<Diff> exceptions = new ArrayList<>();

    @Autowired
    private NRConfiguration nrConfiguration;
    @Autowired
    private RestTemplate latestRestTemplate;
    @Autowired
    private RestTemplate testingRestTemplate;

    @Test
    public void launch() {
        testAndGetResult("technos", String[].class).ifPresent(technoNames -> {
            Stream.of(technoNames).filter(StringUtils::isNotEmpty).forEach(technoName -> {
                testAndGetResult("technos/{name}", String[].class, technoName).ifPresent(technoVersions -> {
                    Stream.of(technoVersions).filter(StringUtils::isNotEmpty).forEach(technoVersion -> {
                        testAndGetResult("technos/{name}/{version}", String[].class, technoName, technoVersion).ifPresent(technoTypes -> {
                            Stream.of(technoTypes).filter(StringUtils::isNotEmpty).forEach(technoType -> {
                                test("technos/{name}/{version}/{type}", TechnoIO.class, technoName, technoVersion, technoType);
                                test("technos/{name}/{version}/{type}/model", ModelOutput.class, technoName, technoVersion, technoType);
                                testAndGetResult("technos/{name}/{version}/{type}/templates", PartialTemplateIO[].class, technoName, technoVersion, technoType)
                                        .ifPresent(templates -> {
                                            Stream.of(templates).map(PartialTemplateIO::getName).filter(StringUtils::isNotEmpty).forEach(templateName ->
                                                    test("technos/{name}/{version}/{type}/templates/{template_name}",
                                                            TemplateIO.class, technoName, technoVersion, technoType, templateName));
                                        });
                            });
                        });
                    });
                });
            });
        });

        /**
         * ****** Diff ******
         * Latest url: http://ciaobaby:52700/rest/modules/transilien-siteweb-vhost//5.53.0/templates
         * Testing url: http://ciaobaby:52700/rest/modules/transilien-siteweb-vhost//5.53.0/templates
         * null
         */

        testAndGetResult("modules", String[].class).ifPresent(moduleNames -> {
            Stream.of(moduleNames).filter(StringUtils::isNotEmpty).forEach(moduleName -> {
                testAndGetResult("modules/{name}", String[].class, moduleName).ifPresent(moduleVersions -> {
                    Stream.of(moduleVersions).filter(StringUtils::isNotEmpty).forEach(moduleVersion -> {
                        testAndGetResult("modules/{name}/{version}", String[].class, moduleName, moduleVersion).ifPresent(moduleTypes -> {
                            Stream.of(moduleTypes).filter(StringUtils::isNotEmpty).forEach(moduleType -> {
                                test("modules/{name}/{version}/{type}", ModuleIO.class, moduleName, moduleVersion, moduleType);
                                test("modules/{name}/{version}/{type}/model", ModelOutput.class, moduleName, moduleVersion, moduleType);
                                testAndGetResult("modules/{name}/{version}/{type}/templates", PartialTemplateIO[].class, moduleName, moduleVersion, moduleType)
                                        .ifPresent(templates -> {
                                            Stream.of(templates).map(PartialTemplateIO::getName).filter(StringUtils::isNotEmpty).forEach(templateName ->
                                                    test("modules/{name}/{version}/{type}/templates/{template_name}",
                                                            TemplateIO.class, moduleName, moduleVersion, moduleType, templateName));
                                        });
                            });
                        });
                    });
                });
            });
        });

        logStats();
        logDiffs();
    }

    private void logDiffs() {
        diffs.forEach(NRTest::logError);
    }

    private void logStats() {
        log.warn("{} endpoint diffs", diffs.size());
    }

    private void test(String restEndpoint, Object... endpointVariables) {
        test(restEndpoint, String.class, endpointVariables);
    }

    private void test(String restEndpoint, Class responseType, Object... endpointVariables) {
        testAndGetResult(restEndpoint, responseType, endpointVariables);
    }

    private <T> Optional<T> testAndGetResult(String restEndpoint, Class<T> responseType, Object... endpointVariables) {
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
            // Appelle la version de prod et la prochaine version
            ResponseEntity<String> latestResult = getResult(latestRestTemplate, isFileUrl, String.class, latestUri, endpointVariables);
            ResponseEntity<String> testingResult = getResult(testingRestTemplate, isFileUrl, String.class, testingUri, endpointVariables);
            // Compare le code de retour et le contenu
//            Assert.assertEquals(latestResult, testingResult);
            try {
                Assert.assertEquals(latestResult.getStatusCode(), testingResult.getStatusCode());
                Assert.assertEquals(latestResult.getBody(), testingResult.getBody());
                // Convertit
                result = Optional.of(PresentationConfiguration.gson().fromJson(latestResult.getBody(), responseType));
            } catch (Throwable t) {
                logAndSaveDiff(new Diff(readableLatestUri, readableTestingUri, t.getMessage()));
            }

        } catch (Throwable t) {
            logAndSaveException(new Diff(readableLatestUri, readableTestingUri, t.getMessage()));
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
        logError(diff, "Diff");
        diffs.add(diff);
    }

    private static void logAndSaveException(Diff diff) {
        logError(diff, "Exception");
        exceptions.add(diff);
    }

    private static void logError(Diff diff, String errorType) {
        log.warn("");
        log.warn("****** {} ******", errorType);
        log.warn("Latest url: " + diff.getLatestUri());
        log.warn("Testing url: " + diff.getTestingUri());
        log.warn(diff.getMessage());
        log.warn("");
    }
}