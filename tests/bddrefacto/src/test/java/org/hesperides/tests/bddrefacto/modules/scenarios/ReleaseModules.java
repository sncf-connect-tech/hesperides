package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.PropertyBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class ReleaseModules implements En {

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

    public ReleaseModules() {

        Given("^a released module( with a template)?( with properties)?( (?:and|with) this techno)?$", (
                final String withATemplate, final String withProperties, final String withThisTechno) -> {

            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }

            moduleClient.create(moduleBuilder.build());

            if (StringUtils.isNotEmpty(withProperties)) {
                propertyBuilder.reset().withName("foo");
                modelBuilder.withProperty(propertyBuilder.build());
                templateBuilder.withContent(propertyBuilder.toString());

                propertyBuilder.reset().withName("bar");
                modelBuilder.withProperty(propertyBuilder.build());
                templateBuilder.withContent(propertyBuilder.toString());

                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
            }

            if (StringUtils.isNotEmpty(withATemplate) && StringUtils.isEmpty(withProperties)) {
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
            }

            moduleClient.release(moduleBuilder.build(), ModuleIO.class);
            moduleBuilder.withVersionId(1).withIsWorkingCopy(false);
        });

        When("^I( try to)? release this module$", (final String tryTo) -> {
            responseEntity = moduleClient.release(moduleBuilder.build(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module is successfully released$", () -> {
            assertOK(responseEntity);
            ModuleBuilder expectedModuleBuilder = new ModuleBuilder().withTechno(technoBuilder.build()).withVersionId(1).withIsWorkingCopy(false);
            ModuleIO expectedModule = expectedModuleBuilder.build();
            ModuleIO actualModule = (ModuleIO) responseEntity.getBody();
            assertEquals(expectedModule, actualModule);

            // Compare les templates de la module d'origine avec ceux de la module en mode release
            // Seul le namespace est différent
            String expectedNamespace = expectedModuleBuilder.getNamespace();
            List<PartialTemplateIO> expectedTemplates = moduleClient.getTemplates(this.moduleBuilder.build())
                    .stream()
                    .map(expectedTemplate -> new PartialTemplateIO(expectedTemplate.getName(), expectedNamespace, expectedTemplate.getFilename(), expectedTemplate.getLocation()))
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = moduleClient.getTemplates(actualModule);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the module release is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }
}
