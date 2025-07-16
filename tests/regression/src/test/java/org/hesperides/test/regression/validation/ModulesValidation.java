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
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class ModulesValidation extends AbstractValidation {

    public static final String MODULE_KEY_PREFIX = "module";
    private static final String GET_MODULE_NAMES = "modules";
    private static final String GET_MODULE_VERSIONS = "modules/{name}";
    private static final String GET_MODULE_TYPES = "modules/{name}/{version}";
    private static final String GET_MODULE_DETAIL = "modules/{name}/{version}/{type}";
    private static final String GET_MODULE_MODEL = "modules/{name}/{version}/{type}";
    private static final String GET_MODULE_TEMPLATES = "modules/{name}/{version}/{type}/templates";
    private static final String GET_MODULE_TEMPLATE_DETAIL = "modules/{name}/{version}/{type}/templates/{template_name}";
    private static final String GET_PLATFORMS_USING_MODULE = "applications/using_module/{name}/{version}/{type}";

    public void validate() {
        // On commence par récupérer la liste des noms de modules pour récupérer leur versions,
        // puis leur type de version (working copy ou release) afin de consituer leur clé
        // et appeler les endpoints relatifs à chaque module.
        testModuleNames();
    }

    private void testModuleNames() {
        testEndpointAndGetResult("module_names", GET_MODULE_NAMES, String[].class).ifPresent(moduleNames ->
                Stream.of(moduleNames)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(this::testModuleVersions));
    }

    private void testModuleVersions(String moduleName) {
        testEndpointAndGetResult("module_versions", GET_MODULE_VERSIONS, String[].class, moduleName).ifPresent(moduleVersions ->
                Stream.of(moduleVersions)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(moduleVersion -> testModuleTypes(moduleName, moduleVersion)));
    }

    private void testModuleTypes(String moduleName, String moduleVersion) {
        testEndpointAndGetResult("module_types", GET_MODULE_TYPES, String[].class, moduleName, moduleVersion).ifPresent(moduleTypes ->
                Stream.of(moduleTypes)
                        .distinct()
                        .filter(StringUtils::isNotEmpty)
                        .forEach(moduleType -> testModuleEndpoints(moduleName, moduleVersion, moduleType)));
    }

    private void testModuleEndpoints(String moduleName, String moduleVersion, String moduleType) {
        String moduleKey = String.format("%s-%s-%s-%s", MODULE_KEY_PREFIX, moduleName, moduleVersion, moduleType);

        testEndpoint(moduleKey, GET_MODULE_DETAIL, ModuleIO.class, moduleName, moduleVersion, moduleType);
        testEndpoint(moduleKey, GET_MODULE_MODEL, ModelOutput.class, moduleName, moduleVersion, moduleType);
        testModuleTemplates(moduleKey, moduleName, moduleVersion, moduleType);
        testEndpoint(moduleKey, GET_PLATFORMS_USING_MODULE, ModulePlatformsOutput[].class, moduleName, moduleVersion, moduleType);
    }

    private void testModuleTemplates(String moduleKey, String moduleName, String moduleVersion, String moduleType) {
        testEndpointAndGetResult(moduleKey, GET_MODULE_TEMPLATES, PartialTemplateIO[].class, moduleName, moduleVersion, moduleType).ifPresent(templates ->
                Stream.of(templates)
                        .map(PartialTemplateIO::getName)
                        .filter(StringUtils::isNotEmpty)
                        .forEach(templateName ->
                                testEndpoint(moduleKey, GET_MODULE_TEMPLATE_DETAIL, TemplateIO.class, moduleName, moduleVersion, moduleType, templateName)));
    }
}
