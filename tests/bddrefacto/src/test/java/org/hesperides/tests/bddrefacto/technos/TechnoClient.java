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
package org.hesperides.tests.bddrefacto.technos;

import org.hesperides.core.presentation.io.TechnoIO;
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
public class TechnoClient {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity create(TemplateIO templateInput, TechnoIO technoInput) {
        return create(templateInput, technoInput, TemplateIO.class);
    }

    public ResponseEntity create(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        return restTemplate.postForEntity(
                "/templates/packages/{name}/{version}/{type}/templates",
                templateInput,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()));
    }

    public ResponseEntity search(String terms) {
        return restTemplate.postForEntity("/templates/packages/perform_search?terms=" + terms, null, TechnoIO[].class);
    }

    public ResponseEntity get(TechnoIO technoInput, Class responseType) {
        return restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()));
    }

    public ResponseEntity release(TechnoIO technoInput) {
        return release(technoInput, TechnoIO.class);
    }

    public ResponseEntity release(TechnoIO technoInput, Class responseType) {
        return restTemplate.postForEntity("/templates/packages/create_release?techno_name={name}&techno_version={version}",
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion());
    }

    public ResponseEntity delete(TechnoIO technoInput, Class responseType) {
        return restTemplate.exchange("/templates/packages/{name}/{version}/{type}",
                HttpMethod.DELETE,
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()));
    }

    public ResponseEntity copy(TechnoIO existingTechnoInput, TechnoIO newTechnoInput, Class responseType) {
        return restTemplate.postForEntity("/templates/packages?from_package_name={name}&from_package_version={version}&from_is_working_copy={isWorkingCopy}",
                newTechnoInput,
                responseType,
                existingTechnoInput.getName(),
                existingTechnoInput.getVersion(),
                existingTechnoInput.isWorkingCopy());
    }

    public ResponseEntity getModel(TechnoIO technoInput, Class responseType) {
        return restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/model",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()));

    }

    public ResponseEntity addTemplate(TemplateIO templateInput, TechnoIO technoInput) {
        return addTemplate(templateInput, technoInput, TemplateIO.class);
    }

    public ResponseEntity addTemplate(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        // L'appel est le même que pour la création
        return create(templateInput, technoInput, responseType);
    }

    public ResponseEntity updateTemplate(TemplateIO templateInput, TechnoIO technoInput) {
        return updateTemplate(templateInput, technoInput, TemplateIO.class);
    }

    public ResponseEntity updateTemplate(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        return restTemplate.exchange("/templates/packages/{name}/{version}/{type}/templates",
                HttpMethod.PUT,
                new HttpEntity<>(templateInput),
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()));
    }

    public ResponseEntity getTemplates(TechnoIO technoInput, Class responseType) {
        return restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()));
    }

    public List<PartialTemplateIO> getTemplates(TechnoIO technoInput) {
        ResponseEntity<PartialTemplateIO[]> responseEntity = getTemplates(technoInput, PartialTemplateIO[].class);
        return Arrays.asList(responseEntity.getBody());
    }

    public ResponseEntity getTemplate(String templateName, TechnoIO technoInput, Class responseType) {
        return restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()),
                templateName);
    }

    private String getVersionType(boolean isWorkingCopy) {
        return isWorkingCopy ? "workingcopy" : "release";
    }

    public ResponseEntity deleteTemplate(String templateName, TechnoIO technoInput, Class responseType) {
        return restTemplate.exchange("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                HttpMethod.DELETE,
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                getVersionType(technoInput.isWorkingCopy()),
                templateName);
    }
}
