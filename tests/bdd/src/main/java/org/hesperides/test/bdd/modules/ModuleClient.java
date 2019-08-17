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
import org.hesperides.core.presentation.io.ModuleKeyOutput;
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
public class ModuleClient {

    private final CustomRestTemplate restTemplate;

    @Autowired
    public ModuleClient(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CustomRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createModule(ModuleIO moduleInput, String tryTo) {
        restTemplate.postForEntity("/modules", moduleInput, getResponseType(tryTo, ModuleIO.class));
    }

    public void searchModules(String searchInput) {
        searchModules(searchInput, 0, null);
    }

    public void searchModules(String searchInput, Integer size, String tryTo) {
        restTemplate.getForEntity("/modules/perform_search?terms=" + searchInput + "&size=" + size, getResponseType(tryTo, ModuleIO[].class));
    }

    public void searchSingle(String searchInput) {
        restTemplate.getForEntity("/modules/search?terms=" + searchInput, ModuleIO.class);
    }

    public void getModule(ModuleIO moduleInput, String tryTo) {
        getModule(moduleInput, VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()), tryTo);
    }

    public void getModule(ModuleIO moduleInput, String versionType, String tryTo) {
        restTemplate.getForEntity("/modules/{name}/{version}/{type}",
                getResponseType(tryTo, ModuleIO.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                versionType);
    }

    public void releaseModule(ModuleIO moduleInput, String releaseVersion, String tryTo) {
        restTemplate.postForEntity("/modules/create_release?module_name={name}&module_version={version}&release_version={release_version}",
                null,
                getResponseType(tryTo, ModuleIO.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                releaseVersion);
    }

    public void deleteModule(ModuleIO moduleInput, String tryTo) {
        restTemplate.deleteForEntity("/modules/{name}/{version}/{type}",
                getResponseType(tryTo, ResponseEntity.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    public void copyModule(ModuleIO existingModuleInput, ModuleIO newModuleInput, String tryTo) {
        restTemplate.postForEntity("/modules?from_module_name={name}&from_module_version={version}&from_is_working_copy={isWorkingCopy}",
                newModuleInput,
                getResponseType(tryTo, ModuleIO.class),
                existingModuleInput.getName(),
                existingModuleInput.getVersion(),
                existingModuleInput.getIsWorkingCopy());
    }

    public void getModel(ModuleIO moduleInput) {
        getModel(moduleInput, null);
    }

    public void getModel(ModuleIO moduleInput, String tryTo) {
        restTemplate.getForEntity("/modules/{name}/{version}/{type}/model",
                getResponseType(tryTo, ModelOutput.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    public void addTemplate(TemplateIO templateInput, ModuleIO moduleInput) {
        addTemplate(templateInput, moduleInput, null);
    }

    public void addTemplate(TemplateIO templateInput, ModuleIO moduleInput, String tryTo) {
        restTemplate.postForEntity(
                "/modules/{name}/{version}/{type}/templates",
                templateInput,
                getResponseType(tryTo, TemplateIO.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    public void updateTemplate(TemplateIO templateInput, ModuleIO moduleInput) {
        updateTemplate(templateInput, moduleInput, null);
    }

    public void updateTemplate(TemplateIO templateInput, ModuleIO moduleInput, String tryTo) {
        restTemplate.putForEntity("/modules/{name}/{version}/{type}/templates",
                templateInput,
                getResponseType(tryTo, TemplateIO.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    public void getTemplates(ModuleIO moduleInput) {
        getTemplates(moduleInput, null);
    }

    public void getTemplates(ModuleIO moduleInput, String tryTo) {
        restTemplate.getForEntity("/modules/{name}/{version}/{type}/templates",
                getResponseType(tryTo, PartialTemplateIO[].class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()));
    }

    public void getTemplate(String templateName, ModuleIO moduleInput, String tryTo) {
        restTemplate.getForEntity("/modules/{name}/{version}/{type}/templates/{template_name}",
                getResponseType(tryTo, TemplateIO.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()),
                templateName);
    }

    public void deleteTemplate(String templateName, ModuleIO moduleInput, String tryTo) {
        restTemplate.deleteForEntity("/modules/{name}/{version}/{type}/templates/{template_name}",
                getResponseType(tryTo, ResponseEntity.class),
                moduleInput.getName(),
                moduleInput.getVersion(),
                VersionType.fromIsWorkingCopy(moduleInput.getIsWorkingCopy()),
                templateName);
    }

    public void getModuleNames() {
        restTemplate.getForEntity("/modules", String[].class);
    }

    public void getModuleVersions(String name) {
        restTemplate.getForEntity("/modules/{name}", String[].class, name);
    }

    public void getModuleTypes(String name, String version) {
        restTemplate.getForEntity("/modules/{name}/{version}", String[].class, name, version);
    }

    public void getModulesUsingTechno(TechnoIO technoInput) {
        restTemplate.getForEntity(
                "/modules/using_techno/{techno_name}/{techno_version}/{techno_type}",
                ModuleKeyOutput[].class,
                technoInput.getName(),
                technoInput.getVersion(),
                VersionType.fromIsWorkingCopy(technoInput.getIsWorkingCopy()));
    }

    public void updateModule(ModuleIO moduleInput, String tryTo) {
        restTemplate.putForEntity("/modules", moduleInput, getResponseType(tryTo, ModuleIO.class));
    }
}
