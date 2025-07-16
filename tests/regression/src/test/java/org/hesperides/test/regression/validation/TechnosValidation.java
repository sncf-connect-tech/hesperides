/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleKeyOutput;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class TechnosValidation extends AbstractValidation {

    public static final String TECHNO_KEY_PREFIX = "techno";
    private static final String GET_TECHNO_NAMES = "technos";
    private static final String GET_TECHNO_VERSIONS = "technos/{name}";
    private static final String GET_TECHNO_TYPES = "technos/{name}/{version}";
    private static final String GET_TECHNO_DETAIL = "technos/{name}/{version}/{type}";
    private static final String GET_TECHNO_MODEL = "technos/{name}/{version}/{type}";
    private static final String GET_TECHNO_TEMPLATES = "technos/{name}/{version}/{type}/templates";
    private static final String GET_TECHNO_TEMPLATE_DETAIL = "technos/{name}/{version}/{type}/templates/{template_name}";
    private static final String GET_MODULES_USING_TECHNO = "modules/using_techno/{name}/{version}/{type}";

    public void validate() {
        // On commence par récupérer la liste des noms de technos pour récupérer leur versions,
        // puis leur type de version (working copy ou release) afin de consituer leur clé
        // et appeler les endpoints relatifs à chaque techno.
        testTechnoNames();
    }

    private void testTechnoNames() {
        testEndpointAndGetResult("techno_names", GET_TECHNO_NAMES, String[].class).ifPresent(technoNames ->
                Stream.of(technoNames)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(this::testTechnoVersions));
    }

    private void testTechnoVersions(String technoName) {
        testEndpointAndGetResult("techno_versions", GET_TECHNO_VERSIONS, String[].class, technoName).ifPresent(technoVersions ->
                Stream.of(technoVersions)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(technoVersion -> testTechnoTypes(technoName, technoVersion)));
    }

    private void testTechnoTypes(String technoName, String technoVersion) {
        testEndpointAndGetResult("techno_types", GET_TECHNO_TYPES, String[].class, technoName, technoVersion).ifPresent(technoTypes ->
                Stream.of(technoTypes)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(technoType -> testTechnoEndpoints(technoName, technoVersion, technoType)));
    }

    private void testTechnoEndpoints(String technoName, String technoVersion, String technoType) {
        String technoKey = String.format("%s-%s-%s-%s", TECHNO_KEY_PREFIX, technoName, technoVersion, technoType);
        testEndpoint(technoKey, GET_TECHNO_DETAIL, TechnoIO.class, technoName, technoVersion, technoType);
        testEndpoint(technoKey, GET_TECHNO_MODEL, ModelOutput.class, technoName, technoVersion, technoType);
        testTechnoTemplates(technoKey, technoName, technoVersion, technoType);
        testEndpoint(technoKey, GET_MODULES_USING_TECHNO, ModuleKeyOutput[].class, technoName, technoVersion, technoType);
    }

    private void testTechnoTemplates(String technoKey, String technoName, String technoVersion, String technoType) {
        testEndpointAndGetResult(technoKey, GET_TECHNO_TEMPLATES, PartialTemplateIO[].class, technoName, technoVersion, technoType).ifPresent(templates ->
                Stream.of(templates)
                        .map(PartialTemplateIO::getName)
                        .filter(StringUtils::isNotEmpty)
                        .forEach(templateName ->
                                testEndpoint(technoKey, GET_TECHNO_TEMPLATE_DETAIL, TemplateIO.class, technoName, technoVersion, technoType, templateName)));
    }
}
