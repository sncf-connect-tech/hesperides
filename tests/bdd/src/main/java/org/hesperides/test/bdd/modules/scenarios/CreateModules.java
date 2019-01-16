package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.CommonSteps;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class CreateModules extends HesperidesScenario implements En {

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
    @Autowired
    private CommonSteps commonSteps;

    public CreateModules() {

        Given("^an existing module" +
                "(?: named \"([^\"]*)\")?" +
                "( with a template)?" +
                "( with this template)?" +
                "( with(?: password)? properties)?" +
                "( (?:and|with) global properties)?" +
                "( (?:and|with) this techno)?$", (
                String moduleName,
                String withATemplate,
                String withThisTemplate,
                String withProperties,
                String withGlobalProperties,
                String withThisTechno) -> {

            if (StringUtils.isNotEmpty(moduleName)) {
                moduleBuilder.withName(moduleName);
            }

            if (StringUtils.isEmpty(withThisTemplate)) {
                templateBuilder.reset();
            }

            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }

            commonSteps.ensureUserAuthIsSet();
            testContext.responseEntity = moduleClient.create(moduleBuilder.build());
            assertCreated();
            moduleBuilder.withVersionId(1);

            if (StringUtils.isNotEmpty(withProperties)) {
                addPropertyToBuilders("module-foo", withProperties.contains("password"));
                addPropertyToBuilders("module-bar", withProperties.contains("password"));
            }

            if (StringUtils.isNotEmpty(withGlobalProperties)) {
                addPropertyToBuilders("global-module-foo");
                addPropertyToBuilders("global-module-bar");
            }
            if (StringUtils.isNotEmpty(withATemplate) || StringUtils.isNotEmpty(withThisTemplate) || StringUtils.isNotEmpty(withProperties) || StringUtils.isNotEmpty(withGlobalProperties)) {
                moduleBuilder.withTemplate(templateBuilder.build());
                moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
            }
        });

        Given("^a module with (\\d+) versions$", (Integer nbVersions) -> {
            moduleBuilder.withName("new-module");
            for (int i = 0; i < nbVersions; i++) {
                moduleBuilder.withVersion("1." + i);
                moduleClient.create(moduleBuilder.build());
            }
        });

        Given("^a list of( \\d+)? modules( with different names)?(?: with the same name)?$", (String modulesCount, String withDifferentNames) -> {
            Integer modulesToCreateCount = StringUtils.isEmpty(modulesCount) ? 12 : Integer.valueOf(modulesCount.substring(1));
            for (int i = 0; i < modulesToCreateCount; i++) {
                if (StringUtils.isNotEmpty(withDifferentNames)) {
                    moduleBuilder.withName("new-module-" + i);
                } else {
                    moduleBuilder.withName("new-module");
                }
                moduleBuilder.withVersion("0.0." + i + 1);
                moduleClient.create(moduleBuilder.build());
            }
        });

        Given("^a module to create(?: with the same name and version)?( with this techno)?( but different letter case)?$", (String withThisTechno, String withDifferentCase) -> {
            moduleBuilder.reset();
            if (StringUtils.isNotEmpty(withDifferentCase)) {
                moduleBuilder.withName(moduleBuilder.getName().toUpperCase());
            }
            if (StringUtils.isNotEmpty(withThisTechno)) {
                moduleBuilder.withTechno(technoBuilder.build());
            }
        });

        Given("^an existing module with this template content?$", (String templateContent) -> {
            moduleClient.create(moduleBuilder.build());
            templateBuilder.setContent(templateContent);
            moduleClient.addTemplate(templateBuilder.build(), moduleBuilder.build());
        });

        When("^I( try to)? create this module$", (String tryTo) -> {
            testContext.responseEntity = moduleClient.create(moduleBuilder.build(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module is successfully created$", () -> {
            assertCreated();
            ModuleIO expectedModule = moduleBuilder.withVersionId(1).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module creation is rejected with a conflict error$", () -> {
            assertConflict();
        });

        Then("^the module creation is rejected with a not found error$", () -> {
            assertNotFound();
        });
    }

    private void addPropertyToBuilders(String name) {
        addPropertyToBuilders(name, false);
    }

    private void addPropertyToBuilders(String name, boolean isPassword) {
        propertyBuilder.reset().withName(name);
        if (isPassword) {
            propertyBuilder.withIsPassword();
        }
        modelBuilder.withProperty(propertyBuilder.build());
        templateBuilder.withContent(propertyBuilder.toString());
    }
}
