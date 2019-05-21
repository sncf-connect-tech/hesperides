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

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;

import java.util.stream.Stream;

public class ModulesValidation extends AbstractValidation {

    private static final String GET_MODULE_NAMES = "modules";
    private static final String GET_MODULE_VERSIONS = "modules/{name}";
    private static final String GET_MODULE_TYPES = "modules/{name}/{version}";
    private static final String GET_MODULE_DETAIL = "modules/{name}/{version}/{type}";
    private static final String GET_MODULE_MODEL = "modules/{name}/{version}/{type}";
    private static final String GET_MODULE_TEMPLATES = "modules/{name}/{version}/{type}/templates";
    private static final String GET_MODULE_TEMPLATE_DETAIL = "modules/{name}/{version}/{type}/templates/{template_name}";
    private static final String GET_PLATFORMS_USING_MODULE = "applications/using_module/{name}/{version}/{type}";

    public void validate() {
        testAndGetResult(GET_MODULE_NAMES, String[].class).ifPresent(moduleNames -> {
            Stream.of(moduleNames).filter(StringUtils::isNotEmpty).forEach(moduleName -> {
                testAndGetResult(GET_MODULE_VERSIONS, String[].class, moduleName).ifPresent(moduleVersions -> {
                    Stream.of(moduleVersions).filter(StringUtils::isNotEmpty).forEach(moduleVersion -> {
                        testAndGetResult(GET_MODULE_TYPES, String[].class, moduleName, moduleVersion).ifPresent(moduleTypes -> {
                            Stream.of(moduleTypes).filter(StringUtils::isNotEmpty).forEach(moduleType -> {
                                test(GET_MODULE_DETAIL, ModuleIO.class, moduleName, moduleVersion, moduleType);
                                test(GET_MODULE_MODEL, ModelOutput.class, moduleName, moduleVersion, moduleType);
                                testAndGetResult(GET_MODULE_TEMPLATES, PartialTemplateIO[].class, moduleName, moduleVersion, moduleType)
                                        .ifPresent(templates -> {
                                            Stream.of(templates).map(PartialTemplateIO::getName).filter(StringUtils::isNotEmpty).forEach(templateName ->
                                                    test(GET_MODULE_TEMPLATE_DETAIL, TemplateIO.class, moduleName, moduleVersion, moduleType, templateName));
                                        });
                                test(GET_PLATFORMS_USING_MODULE, String[].class, moduleName, moduleVersion, moduleType);
                            });
                        });
                    });
                });
            });
        });
    }
}
