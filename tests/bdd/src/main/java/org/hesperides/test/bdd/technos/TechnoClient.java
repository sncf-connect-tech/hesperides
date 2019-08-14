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
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.CustomRestTemplate;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TechnoClient {

    private final CustomRestTemplate restTemplate;

    @Autowired
    public TechnoClient(CustomRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void create(TemplateIO templateInput, TechnoIO technoInput) {
        create(templateInput, technoInput, TemplateIO.class);
    }

    public void create(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        restTemplate.postForEntity(
                "/templates/packages/{name}/{version}/{type}/templates",
                templateInput,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void search(String terms) {
        search(terms, 0);
    }

    public void search(String terms, Integer size) {
        restTemplate.getForEntity("/templates/packages/perform_search?terms=" + terms + "&size=" + size, TechnoIO[].class);
    }

    public void get(TechnoIO technoInput, String versionType, Class responseType) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                versionType);
    }

    public void release(TechnoIO technoInput) {
        release(technoInput, TechnoIO.class);
    }

    public void release(TechnoIO technoInput, Class responseType) {
        restTemplate.postForEntity("/templates/packages/create_release?techno_name={name}&techno_version={version}",
                null,
                responseType,
                technoInput.getName(),
                technoInput.getVersion());
    }

    public void delete(TechnoIO technoInput, Class responseType) {
        restTemplate.deleteForEntity("/templates/packages/{name}/{version}/{type}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void copy(TechnoIO existingTechnoInput, TechnoIO newTechnoInput, Class responseType) {
        restTemplate.postForEntity("/templates/packages?from_package_name={name}&from_package_version={version}&from_is_working_copy={isWorkingCopy}",
                newTechnoInput,
                responseType,
                existingTechnoInput.getName(),
                existingTechnoInput.getVersion(),
                existingTechnoInput.getIsWorkingCopy());
    }

    public void getModel(TechnoIO technoInput, Class responseType) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/model",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
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
        restTemplate.putForEntity("/templates/packages/{name}/{version}/{type}/templates",
                templateInput,
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void getTemplates(TechnoIO technoInput, Class responseType) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void getTemplate(String templateName, TechnoIO technoInput, Class responseType) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()),
                templateName);
    }

    public void deleteTemplate(String templateName, TechnoIO technoInput, Class responseType) {
        restTemplate.deleteForEntity("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                responseType,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()),
                templateName);
    }

    public void getNames() {
        restTemplate.getForEntity("/templates/packages", String[].class);
    }

    public void getVersions(String name) {
        restTemplate.getForEntity("/templates/packages/{name}", String[].class, name);
    }

    public void getTypes(String name, String version) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}", String[].class, name, version);
    }
}
