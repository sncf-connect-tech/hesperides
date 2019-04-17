/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
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
package org.hesperides.test.bdd.modules;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.TechnoModulesOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.templatecontainers.TemplateContainerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Arrays;
import java.util.List;

import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.urlEncodeUtf8;
import static org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode.NONE;

@Component
public class ModuleClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DefaultUriBuilderFactory defaultUriBuilderFactory;

    public ResponseEntity create(ModuleIO moduleInput) {
        return create(moduleInput, ModuleIO.class);
    }

    public ResponseEntity create(ModuleIO moduleInput, Class responseType) {
        return restTemplate.postForEntity("/modules", moduleInput, responseType);
    }

    public ResponseEntity search(String terms) {
        return this.search(terms, ModuleIO[].class);
    }

    public ResponseEntity search(String terms, Class responseType) {
        return restTemplate.postForEntity("/modules/perform_search?terms=" + terms, null, responseType);
    }

    public ResponseEntity singleSearch(String terms, Class responseType) {
        return restTemplate.postForEntity("/modules/search?terms=" + terms, null, responseType);
    }

    public ResponseEntity get(ModuleIO moduleInput, String versionType, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                versionType.toLowerCase());
    }

    public ResponseEntity release(ModuleIO moduleInput, Class responseType) {
        return this.release(moduleInput, "", responseType);
    }

    public ResponseEntity release(ModuleIO moduleInput, String releasedModuleVersion, Class responseType) {
        return restTemplate.postForEntity("/modules/create_release?module_name={name}&module_version={module_version}&release_version={release_version}",
                null,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                releasedModuleVersion);
    }

    public ResponseEntity delete(ModuleIO moduleInput) {
        return delete(moduleInput, ResponseEntity.class);
    }

    public ResponseEntity delete(ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules/{name}/{version}/{type}",
                HttpMethod.DELETE,
                null,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));
    }

    public ResponseEntity copy(ModuleIO existingModuleInput, ModuleIO newModuleInput, Class responseType) {
        String url = "/modules?from_module_name={name}&from_module_version={version}";
        if (existingModuleInput.getIsWorkingCopy() != null) {
            url += "&from_is_working_copy={isWorkingCopy}";
        }
        return restTemplate.postForEntity(url,
                newModuleInput,
                responseType,
                existingModuleInput.getName(),
                existingModuleInput.getVersion(),
                existingModuleInput.getIsWorkingCopy());
    }

    public ResponseEntity getModel(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}/model",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));

    }

    public ResponseEntity addTemplate(TemplateIO templateInput, ModuleIO moduleInput) {
        return addTemplate(templateInput, moduleInput, TemplateIO.class);
    }

    public ResponseEntity addTemplate(TemplateIO templateInput, ModuleIO moduleInput, Class responseType) {
        return restTemplate.postForEntity(
                "/modules/{name}/{version}/{type}/templates",
                templateInput,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));
    }

    public ResponseEntity updateTemplate(TemplateIO templateInput, ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules/{name}/{version}/{type}/templates",
                HttpMethod.PUT,
                new HttpEntity<>(templateInput),
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));
    }

    public ResponseEntity getTemplates(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}/templates",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));
    }

    public List<PartialTemplateIO> getTemplates(ModuleIO moduleInput) {
        ResponseEntity<PartialTemplateIO[]> responseEntity = getTemplates(moduleInput, PartialTemplateIO[].class);
        return Arrays.asList(responseEntity.getBody());
    }

    public ResponseEntity getTemplate(String templateName, ModuleIO moduleInput, Class responseType) {
        return getTemplate(templateName, moduleInput, responseType, false);
    }

    public ResponseEntity getTemplate(String templateName, ModuleIO moduleInput, Class responseType, boolean urlEncodeTemplateName) {
        DefaultUriBuilderFactory.EncodingMode defaultEncodingMode = defaultUriBuilderFactory.getEncodingMode();
        try {
            if (!urlEncodeTemplateName) {
                defaultUriBuilderFactory.setEncodingMode(NONE);
            }
            return restTemplate.getForEntity("/modules/{name}/{version}/{type}/templates/" + templateName,
                    responseType,
                    moduleInput.getName(),
                    moduleInput.getVersion(),
                    TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));
        } finally {
            defaultUriBuilderFactory.setEncodingMode(defaultEncodingMode);
        }
    }

    public ResponseEntity deleteTemplate(String templateName, ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules/{name}/{version}/{type}/templates/" + templateName,
                HttpMethod.DELETE,
                null,
                responseType,
                urlEncodeUtf8(moduleInput.getName()),
                urlEncodeUtf8(moduleInput.getVersion()),
                TemplateContainerHelper.getVersionType(moduleInput.getIsWorkingCopy()));
    }

    public ResponseEntity<String[]> getNames() {
        return restTemplate.getForEntity("/modules", String[].class);
    }

    public ResponseEntity<String[]> getVersions(String name) {
        return restTemplate.getForEntity("/modules/{name}", String[].class, urlEncodeUtf8(name));
    }

    public ResponseEntity<String[]> getTypes(String name, String version) {
        return restTemplate.getForEntity("/modules/{name}/{version}", String[].class, urlEncodeUtf8(name), urlEncodeUtf8(version));
    }

    public ResponseEntity update(ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules",
                HttpMethod.PUT,
                new HttpEntity<>(moduleInput),
                responseType);
    }

    public ResponseEntity<TechnoModulesOutput[]> getModulesUsingTechno(TechnoIO techno) {
        return restTemplate.getForEntity(
                "/modules/using_techno/{techno_name}/{techno_version}/{techno_type}",
                TechnoModulesOutput[].class,
                techno.getName(),
                techno.getVersion(),
                techno.getIsWorkingCopy() ? "workingcopy" : "release");
    }
}
