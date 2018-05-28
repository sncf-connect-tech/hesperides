package org.hesperides.tests.bdd.templatecontainer.tools;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;

import static org.junit.Assert.assertEquals;

public class TemplateSample extends CucumberSpringBean implements En {

    public static final String TEMPLATE_NAME = "templateName";

    /**
     * Instance of TemplateInput with default values and versionId = 0
     */
    public static TemplateIO getTemplateInput() {
        return getTemplateInput(TEMPLATE_NAME, 0);
    }

    /**
     * Instance of TemplateInput with given name and versionId = 0
     */
    public static  TemplateIO getTemplateInput(String name) {
        return getTemplateInput(name, 0);
    }

    /**
     * Instance of TemplateInput with given versionId
     */
    public static TemplateIO getTemplateInput(long versionId) {
        return getTemplateInput(TEMPLATE_NAME, versionId);
    }

    /**
     * Instance of TemplateInput with given name and versionId
     */
    public static TemplateIO getTemplateInput(String name, long versionId) {
        TemplateIO.FileRightsIO fileRights = new TemplateIO.FileRightsIO(true, true, true);
        TemplateIO.RightsIO rights = new TemplateIO.RightsIO(fileRights, fileRights, fileRights);
        return new TemplateIO(name, null, "template.json", "/location", "content", rights, versionId);
    }
}
