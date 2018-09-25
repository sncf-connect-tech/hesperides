package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.PropertyBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertConflict;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertCreated;
import static org.junit.Assert.assertEquals;

public class CreateModules implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public CreateModules() {

        Given("^an existing module( with a template)?( with properties)?$", (final String withATemplate, final String withProperties) -> {
            moduleClient.create(moduleBuilder.build(), ModuleIO.class);

            if (StringUtils.isNotEmpty(withProperties)) {
                propertyBuilder.reset().withName("foo");
                modelBuilder.withProperty(propertyBuilder.build());
                templateBuilder.withContent(propertyBuilder.toString());

                propertyBuilder.reset().withName("bar");
                modelBuilder.withProperty(propertyBuilder.build());
                templateBuilder.withContent(propertyBuilder.toString());

                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build(), TemplateIO.class);
            }

            if (StringUtils.isNotEmpty(withATemplate) && StringUtils.isEmpty(withProperties)) {
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build(), TemplateIO.class);
            }
        });

        Given("^a module to create(?: with the same name and version)?$", () -> {
            moduleBuilder.reset();
        });

        When("^I( try to)? create this module$", (final String tryTo) -> {
            responseEntity = moduleClient.create(moduleBuilder.build(), StepHelper.getResponseType(tryTo, ModuleIO.class));
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
    }
}
