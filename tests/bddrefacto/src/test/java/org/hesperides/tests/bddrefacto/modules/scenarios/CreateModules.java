package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.PropertyBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
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

        Given("^an existing module( with a template)?( with properties)?( (?:and|with) this techno)?$", (
                final String withATemplate, final String withProperties, final String withThisTechno) -> {

            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }

            moduleClient.create(moduleBuilder.build());
            moduleBuilder.withVersionId(1);

            if (StringUtils.isNotEmpty(withProperties)) {
                addPropertyToBuilders("module-foo");
                addPropertyToBuilders("module-bar");
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
            }

            if (StringUtils.isNotEmpty(withATemplate) && StringUtils.isEmpty(withProperties)) {
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
            }
        });

        Given("^a module to create(?: with the same name and version)?( with this techno)?$", (final String withThisTechno) -> {
            moduleBuilder.reset();
            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }
        });

        When("^I( try to)? create this module$", (final String tryTo) -> {
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
