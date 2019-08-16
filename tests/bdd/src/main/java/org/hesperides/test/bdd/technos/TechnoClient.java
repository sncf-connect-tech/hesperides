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
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.commons.CustomRestTemplate;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.hesperides.test.bdd.commons.HesperidesScenario.getResponseType;

@Component
public class TechnoClient {

    private final CustomRestTemplate restTemplate;

    @Autowired
    public TechnoClient(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CustomRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createTechno(TemplateIO templateInput, TechnoIO technoInput, String tryTo) {
        restTemplate.postForEntity(
                "/templates/packages/{name}/{version}/{type}/templates",
                templateInput,
                getResponseType(tryTo, TemplateIO.class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void searchTechnos(String terms) {
        searchTechnos(terms, 0);
    }

    public void searchTechnos(String terms, Integer size) {
        restTemplate.getForEntity("/templates/packages/perform_search?terms=" + terms + "&size=" + size, TechnoIO[].class);
    }

    public void getTechno(TechnoIO technoInput, String tryTo) {
        getTechno(technoInput, VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()), tryTo);
    }

    public void getTechno(TechnoIO technoInput, String versionType, String tryTo) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}",
                getResponseType(tryTo, TechnoIO.class),
                technoInput.getName(),
                technoInput.getVersion(),
                versionType);
    }

    public void releaseTechno(TechnoIO technoInput) {
        releaseTechno(technoInput, null);
    }

    public void releaseTechno(TechnoIO technoInput, String tryTo) {
        restTemplate.postForEntity("/templates/packages/create_release?techno_name={name}&techno_version={version}",
                null,
                getResponseType(tryTo, TechnoIO.class),
                technoInput.getName(),
                technoInput.getVersion());
    }

    public void deleteTechno(TechnoIO technoInput, String tryTo) {
        restTemplate.deleteForEntity("/templates/packages/{name}/{version}/{type}",
                getResponseType(tryTo, ResponseEntity.class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void copyTechno(TechnoIO existingTechnoInput, TechnoIO newTechnoInput, String tryTo) {
        restTemplate.postForEntity("/templates/packages?from_package_name={name}&from_package_version={version}&from_is_working_copy={isWorkingCopy}",
                newTechnoInput,
                getResponseType(tryTo, TechnoIO.class),
                existingTechnoInput.getName(),
                existingTechnoInput.getVersion(),
                existingTechnoInput.getIsWorkingCopy());
    }

    public void getModel(TechnoIO technoInput) {
        getModel(technoInput, null);
    }

    public void getModel(TechnoIO technoInput, String tryTo) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/model",
                getResponseType(tryTo, ModelOutput.class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void addTemplate(TemplateIO templateInput, TechnoIO technoInput) {
        addTemplate(templateInput, technoInput, null);
    }

    public void addTemplate(TemplateIO templateInput, TechnoIO technoInput, String tryTo) {
        // L'appel est le même que pour la création
        createTechno(templateInput, technoInput, tryTo);
    }

    public void updateTemplate(TemplateIO templateInput, TechnoIO technoInput) {
        updateTemplate(templateInput, technoInput, null);
    }

    public void updateTemplate(TemplateIO templateInput, TechnoIO technoInput, String tryTo) {
        restTemplate.putForEntity("/templates/packages/{name}/{version}/{type}/templates",
                templateInput,
                getResponseType(tryTo, TemplateIO.class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void getTemplates(TechnoIO technoInput) {
        getTemplates(technoInput, null);
    }

    public void getTemplates(TechnoIO technoInput, String tryTo) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates",
                getResponseType(tryTo, PartialTemplateIO[].class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void getTemplate(String templateName, TechnoIO technoInput, String tryTo) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                getResponseType(tryTo, TemplateIO.class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()),
                templateName);
    }

    public void deleteTemplate(String templateName, TechnoIO technoInput, String tryTo) {
        restTemplate.deleteForEntity("/templates/packages/{name}/{version}/{type}/templates/{template_name}",
                getResponseType(tryTo, ResponseEntity.class),
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()),
                templateName);
    }

    public void getTechnoNames() {
        restTemplate.getForEntity("/templates/packages", String[].class);
    }

    public void getTechnoVersions(String name) {
        restTemplate.getForEntity("/templates/packages/{name}", String[].class, name);
    }

    public void getTechnoTypes(String name, String version) {
        restTemplate.getForEntity("/templates/packages/{name}/{version}", String[].class, name, version);
    }
}
