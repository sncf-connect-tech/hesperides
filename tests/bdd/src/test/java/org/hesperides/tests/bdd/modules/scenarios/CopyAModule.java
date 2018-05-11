package org.hesperides.tests.bdd.modules.scenarios;

import com.google.common.collect.ImmutableList;
import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CopyAModule extends CucumberSpringBean implements En {

    private ModuleIO moduleInput;
    private ResponseEntity<ModuleIO> response;

    @Autowired
    private ExistingModuleContext existingModule;
    @Autowired
    private ExistingTemplateContext existingTemplate;

    public CopyAModule() {
        When("^creating a copy of this module$", () -> {
            moduleInput = new ModuleIO("test", "1.0.1", true, ImmutableList.of(), 0L);
            TemplateContainer.Key moduleKey = existingModule.getModuleKey();
            response = rest.getTestRest().postForEntity("/modules?from_module_name={moduleName}&from_module_version={moduleVersion}&from_is_working_copy={isWorkingCopy}",
                    moduleInput, ModuleIO.class,
                    moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());
        });

        Then("^the module is successfully and completely duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleIO moduleOutput = response.getBody();
            assertEquals(moduleInput.getName(), moduleOutput.getName());
            assertEquals(moduleInput.getVersion(), moduleOutput.getVersion());
            assertEquals(moduleInput.isWorkingCopy(), moduleOutput.isWorkingCopy());
            assertTemplate();
            assertTechno(moduleOutput.getTechnos());
            assertEquals(1L, moduleOutput.getVersionId().longValue());
        });
    }

    private void assertTechno(List<TechnoIO> technos) {
        TemplateContainer.Key existingTechnoKey = existingModule.getExistingTechno().getTechnoKey();
        TechnoIO moduleTechno = technos.get(0);
        //TODO Récupérer la techno via un appel rest ?
        assertEquals(existingTechnoKey.getName(), moduleTechno.getName());
        assertEquals(existingTechnoKey.getVersion(), moduleTechno.getVersion());
        assertEquals(existingTechnoKey.getVersionType(), TemplateContainer.getVersionType(moduleTechno.isWorkingCopy()));
    }

    private void assertTemplate() {
        PartialTemplateIO template = GetTemplates.getTemplates(rest, existingModule.getModuleKey()).getBody()[0];
        assertEquals(existingTemplate.getTemplateInput().getName(), template.getName());
    }
}
