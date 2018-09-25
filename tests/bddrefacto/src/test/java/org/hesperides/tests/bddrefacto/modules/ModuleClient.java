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
package org.hesperides.tests.bddrefacto.modules;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class ModuleClient {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity create(ModuleIO moduleInput, Class responseType) {
        return restTemplate.postForEntity("/modules", moduleInput, responseType);
    }

    public ResponseEntity search(String terms) {
        return restTemplate.postForEntity("/modules/perform_search?terms=" + terms, null, ModuleIO[].class);
    }

    public ResponseEntity get(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()));
    }

    public ResponseEntity releaseModule(ModuleIO moduleInput, Class responseType) {
        return restTemplate.postForEntity("/modules/create_release?module_name={name}&module_version={version}",
                null,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion());
    }

    public ResponseEntity delete(ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules/{name}/{version}/{type}",
                HttpMethod.DELETE,
                null,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()));
    }

    public ResponseEntity copy(ModuleIO existingModuleInput, ModuleIO newModuleInput, Class responseType) {
        return restTemplate.postForEntity("/modules?from_module_name={name}&from_module_version={version}&from_is_working_copy={isWorkingCopy}",
                newModuleInput,
                responseType,
                existingModuleInput.getName(),
                existingModuleInput.getVersion(),
                existingModuleInput.isWorkingCopy());
    }

    public ResponseEntity getModel(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}/model",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()));

    }

    public ResponseEntity addTemplate(TemplateIO templateInput, ModuleIO moduleInput, Class responseType) {
        return restTemplate.postForEntity(
                "/modules/{name}/{version}/{type}/templates",
                templateInput,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()));
    }

    public ResponseEntity updateTemplate(TemplateIO templateInput, ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules/{name}/{version}/{type}/templates",
                HttpMethod.PUT,
                new HttpEntity<>(templateInput),
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()));
    }

    public ResponseEntity getTemplates(ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}/templates",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()));
    }

    public List<PartialTemplateIO> getTemplates(ModuleIO moduleInput) {
        ResponseEntity<PartialTemplateIO[]> responseEntity = getTemplates(moduleInput, PartialTemplateIO[].class);
        return Arrays.asList(responseEntity.getBody());
    }

    public ResponseEntity getTemplate(String templateName, ModuleIO moduleInput, Class responseType) {
        return restTemplate.getForEntity("/modules/{name}/{version}/{type}/templates/{template_name}",
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()),
                templateName);
    }

    private String getVersionType(boolean isWorkingCopy) {
        return isWorkingCopy ? "workingcopy" : "release";
    }

    public ResponseEntity deleteTemplate(String templateName, ModuleIO moduleInput, Class responseType) {
        return restTemplate.exchange("/modules/{name}/{version}/{type}/templates/{template_name}",
                HttpMethod.DELETE,
                null,
                responseType,
                moduleInput.getName(),
                moduleInput.getVersion(),
                getVersionType(moduleInput.isWorkingCopy()),
                templateName);
    }
}
