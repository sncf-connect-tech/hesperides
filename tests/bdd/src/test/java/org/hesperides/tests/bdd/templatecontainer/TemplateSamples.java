package org.hesperides.tests.bdd.templatecontainer;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;

public class TemplateSamples extends CucumberSpringBean implements En {

    public static final String DEFAULT_TEMPLATE_NAME = "template";
    public static final long DEFAULT_VERSION_ID = 0;

    public static TemplateIO getTemplateInputWithDefaultValues() {
        return getTemplateInputWithNameAndVersionId(DEFAULT_TEMPLATE_NAME, DEFAULT_VERSION_ID);
    }

    public static TemplateIO getTemplateInputWithName(String name) {
        return getTemplateInputWithNameAndVersionId(name, DEFAULT_VERSION_ID);
    }

    public static TemplateIO getTemplateInputWithVersionId(long versionId) {
        return getTemplateInputWithNameAndVersionId(DEFAULT_TEMPLATE_NAME, versionId);
    }

    public static TemplateIO getTemplateInputWithNameAndVersionId(String name, long versionId) {
        TemplateIO.FileRightsIO fileRights = new TemplateIO.FileRightsIO(true, true, true);
        TemplateIO.RightsIO rights = new TemplateIO.RightsIO(fileRights, fileRights, fileRights);
        return new TemplateIO(name, null, "template.json", "/location", "content", rights, versionId);
    }
}
