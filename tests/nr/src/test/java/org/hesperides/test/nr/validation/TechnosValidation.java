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
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.TechnoModulesOutput;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;

import java.util.stream.Stream;

public class TechnosValidation extends AbstractValidation {

    private static final String GET_TECHNO_NAMES = "technos";
    private static final String GET_TECHNO_VERSIONS = "technos/{name}";
    private static final String GET_TECHNO_TYPES = "technos/{name}/{version}";
    private static final String GET_TECHNO_DETAIL = "technos/{name}/{version}/{type}";
    private static final String GET_TECHNO_MODEL = "technos/{name}/{version}/{type}";
    private static final String GET_TECHNO_TEMPLATES = "technos/{name}/{version}/{type}/templates";
    private static final String GET_TECHNO_TEMPLATE_DETAIL = "technos/{name}/{version}/{type}/templates/{template_name}";
    private static final String GET_MODULES_USING_TECHNO = "modules/using_techno/{name}/{version}/{type}";

    public void validate() {
        testAndGetResult(GET_TECHNO_NAMES, String[].class).ifPresent(technoNames -> {
            Stream.of(technoNames).filter(StringUtils::isNotEmpty).forEach(technoName -> {
                testAndGetResult(GET_TECHNO_VERSIONS, String[].class, technoName).ifPresent(technoVersions -> {
                    Stream.of(technoVersions).filter(StringUtils::isNotEmpty).forEach(technoVersion -> {
                        testAndGetResult(GET_TECHNO_TYPES, String[].class, technoName, technoVersion).ifPresent(technoTypes -> {
                            Stream.of(technoTypes).filter(StringUtils::isNotEmpty).forEach(technoType -> {
                                test(GET_TECHNO_DETAIL, TechnoIO.class, technoName, technoVersion, technoType);
                                test(GET_TECHNO_MODEL, ModelOutput.class, technoName, technoVersion, technoType);
                                testAndGetResult(GET_TECHNO_TEMPLATES, PartialTemplateIO[].class, technoName, technoVersion, technoType).ifPresent(templates -> {
                                    Stream.of(templates).map(PartialTemplateIO::getName).filter(StringUtils::isNotEmpty).forEach(templateName ->
                                            test(GET_TECHNO_TEMPLATE_DETAIL, TemplateIO.class, technoName, technoVersion, technoType, templateName));
                                });
                                test(GET_MODULES_USING_TECHNO, TechnoModulesOutput[].class, technoName, technoVersion, technoType);
                            });
                        });
                    });
                });
            });
        });
    }
}
