package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class CreateModules implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public CreateModules() {

        Given("^an existing module( with a template)?( with this template)?( with properties)?( (?:and|with) global properties)?( (?:and|with) this techno)?$", (
                String withATemplate, String withThisTemplate, String withProperties, String withGlobalProperties, String withThisTechno) -> {

            if (StringUtils.isEmpty(withThisTemplate)) {
                templateBuilder.reset();
            }

            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }

            moduleClient.create(moduleBuilder.build());
            moduleBuilder.withVersionId(1);

            if (StringUtils.isNotEmpty(withProperties)) {
                addPropertyToBuilders("module-foo");
                addPropertyToBuilders("module-bar");
            }

            if (StringUtils.isNotEmpty(withGlobalProperties)) {
                addPropertyToBuilders("global-module-foo");
                addPropertyToBuilders("global-module-bar");
            }
            if (StringUtils.isNotEmpty(withATemplate) || StringUtils.isNotEmpty(withThisTemplate) || StringUtils.isNotEmpty(withProperties) || StringUtils.isNotEmpty(withGlobalProperties)) {
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
            }
        });

        Given("^a module to create(?: with the same name and version)?( with this techno)?$", (String withThisTechno) -> {
            moduleBuilder.reset();
            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }
        });

        When("^I( try to)? create this module$", (String tryTo) -> {
            responseEntity = moduleClient.create(moduleBuilder.build(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module is successfully created$", () -> {
            assertCreated(responseEntity);
            ModuleIO excpectedModule = moduleBuilder.withVersionId(1).build();
            ModuleIO actualModule = (ModuleIO) responseEntity.getBody();
            assertEquals(excpectedModule, actualModule);
        });

        Then("^the module creation is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
        });

        Then("^the module creation is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });
    }

    private void addPropertyToBuilders(String name) {
        propertyBuilder.reset().withName(name);
        modelBuilder.withProperty(propertyBuilder.build());
        templateBuilder.withContent(propertyBuilder.toString());
    }
}
