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
package org.hesperides.test.bdd.technos;

import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.TestContext;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class TechnoClient {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    protected TestContext testContext;

    public void create(TemplateIO templateInput, TechnoIO technoInput) {
        create(templateInput, technoInput, TemplateIO.class);
    }

    public void create(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.postForEntity(
                "/templates/packages/{name}/{version}/{type}/templates",
                templateInput,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
        testContext.setResponseEntity(responseEntity);
    }

    public void search(String terms) {
        search(terms, 0);
    }

    public void search(String terms, Integer size) {
        ResponseEntity<TechnoIO[]> responseEntity = restTemplate.getForEntity("/templates/packages/perform_search?terms=" + terms + "&size=" + size, TechnoIO[].class);
        testContext.setResponseEntity(responseEntity);
    }

    public void get(TechnoIO technoInput, String versionType, Class responseType) {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                versionType);
        testContext.setResponseEntity(responseEntity);
    }

    public void release(TechnoIO technoInput) {
        release(technoInput, TechnoIO.class);
    }

    public void release(TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.postForEntity("/templates/packages/create_release?techno_name={name}&techno_version={version}",
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion());
        testContext.setResponseEntity(responseEntity);
    }

    public void delete(TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.exchange("/templates/packages/{name}/{version}/{type}",
                HttpMethod.DELETE,
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
        testContext.setResponseEntity(responseEntity);
    }

    public void copy(TechnoIO existingTechnoInput, TechnoIO newTechnoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.postForEntity("/templates/packages?from_package_name={name}&from_package_version={version}&from_is_working_copy={isWorkingCopy}",
                newTechnoInput,
                responseType,
                existingTechnoInput.getName(),
                existingTechnoInput.getVersion(),
                existingTechnoInput.getIsWorkingCopy());
        testContext.setResponseEntity(responseEntity);
    }

    public void getModel(TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/model",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
        testContext.setResponseEntity(responseEntity);
    }

    public void addTemplate(TemplateIO templateInput, TechnoIO technoInput) {
        addTemplate(templateInput, technoInput, TemplateIO.class);
    }

    public void addTemplate(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        // L'appel est le même que pour la création
        create(templateInput, technoInput, responseType);
    }

    public void updateTemplate(TemplateIO templateInput, TechnoIO technoInput) {
        updateTemplate(templateInput, technoInput, TemplateIO.class);
    }

    public void updateTemplate(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.exchange("/templates/packages/{name}/{version}/{type}/templates",
                HttpMethod.PUT,
                new HttpEntity<>(templateInput),
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
        testContext.setResponseEntity(responseEntity);
    }

    public void getTemplates(TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
        testContext.setResponseEntity(responseEntity);
    }

    public List<PartialTemplateIO> getTemplates(TechnoIO technoInput) {
        getTemplates(technoInput, PartialTemplateIO[].class);
        return Arrays.asList(testContext.getResponseBody(PartialTemplateIO[].class));
    }

    public void getTemplate(String templateName, TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()),
                templateName);
        testContext.setResponseEntity(responseEntity);
    }

    public void deleteTemplate(String templateName, TechnoIO technoInput, Class responseType) {
        ResponseEntity responseEntity = restTemplate.exchange("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                HttpMethod.DELETE,
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()),
                templateName);
        testContext.setResponseEntity(responseEntity);
    }

    public void getNames() {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages", String[].class);
        testContext.setResponseEntity(responseEntity);
    }

    public void getVersions(String name) {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages/{name}", String[].class, name);
        testContext.setResponseEntity(responseEntity);
    }

    public void getTypes(String name, String version) {
        ResponseEntity responseEntity = restTemplate.getForEntity("/templates/packages/{name}/{version}", String[].class, name, version);
        testContext.setResponseEntity(responseEntity);
    }
}
