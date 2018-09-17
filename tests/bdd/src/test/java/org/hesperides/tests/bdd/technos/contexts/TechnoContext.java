package org.hesperides.tests.bdd.technos.contexts;

import cucumber.api.java8.En;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class TechnoContext implements En {

    private TemplateContainer.Key technoKey;

    @Autowired
    private HesperidesTestRestTemplate rest;

    public TechnoContext() {
        Given("^an existing techno$", () -> {
            TemplateIO templateInput = new TemplateBuilder().build();
            TechnoIO technoInput = new TechnoBuilder().build();
            createTechno(technoInput, templateInput);
        });

    }

    public TemplateContainer.Key getTechnoKey() {
        return technoKey;
    }

    public String getTechnoURI() {
        return getTechnoURI(technoKey);
    }

    public String getTechnoURI(TemplateContainer.Key technoKey) {
        return String.format("/templates/packages/%s/%s/%s", technoKey.getName(), technoKey.getVersion(), technoKey.getVersionType());
    }

    public String getTemplatesURI() {
        return getTechnoURI() + "/templates";
    }

    public String getTemplateURI(String templateName) {
        return getTemplatesURI() + "/" + templateName;
    }

    public ResponseEntity<TemplateIO> retrieveExistingTemplate() {
        return retrieveExistingTemplate(TemplateBuilder.DEFAULT_NAME);
    }

    private ResponseEntity<TemplateIO> retrieveExistingTemplate(String name) {
        return rest.getTestRest().getForEntity(getTemplateURI(name), TemplateIO.class);
    }

    public ResponseEntity<TemplateIO> createTechno(TechnoIO technoInput, TemplateIO templateInput) {
        ResponseEntity<TemplateIO> response = rest.getTestRest().postForEntity(
                "/templates/packages/{technoName}/{technoVersion}/workingcopy/templates",
                templateInput,
                TemplateIO.class,
                technoInput.getName(),
                technoInput.getVersion());
        technoKey = new Techno.Key(technoInput.getName(), technoInput.getVersion(), technoInput.isWorkingCopy() ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release);
        return response;
    }

    public ResponseEntity<TemplateIO> addTemplateToExistingTechno(String templateName) {
        TemplateIO templateInput = new TemplateBuilder().withName(templateName).build();
        TechnoIO technoInput = new TechnoBuilder().withKey(technoKey).build();
        return createTechno(technoInput, templateInput);
    }

    public ResponseEntity<TemplateIO> addTemplateToExistingTechno(TemplateIO templateInput) {
        TechnoIO technoInput = new TechnoBuilder().withKey(technoKey).build();
        return createTechno(technoInput, templateInput);
    }

    public ResponseEntity<TechnoIO> releaseTechno() {
        return rest.getTestRest().postForEntity("/templates/packages/create_release?techno_name={technoName}&techno_version={technoVersion}",
                null, TechnoIO.class, technoKey.getName(), technoKey.getVersion());
    }

    public ResponseEntity updateTemplate(TemplateIO templateInput) {
        return rest.putForEntity(getTemplatesURI(), templateInput, TemplateIO.class);
    }

    public String getNamespace() {
        return "packages#" + technoKey.getName() + "#" + technoKey.getVersion() + "#" + technoKey.getVersionType().name().toUpperCase();
    }

    public void deleteTemplate(String templateName) {
        rest.getTestRest().delete(getTemplateURI(templateName));
    }

    public void deleteTechno() {
        rest.getTestRest().delete(getTechnoURI());
    }
}
