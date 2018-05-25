package org.hesperides.tests.bdd.templatecontainer.contexts;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;

import static org.junit.Assert.assertEquals;

public class TemplateSample extends CucumberSpringBean implements En {

    public static final String TEMPLATE_NAME = "templateName";

    public TemplateIO getTemplateInput() {
        return getTemplateInput(TEMPLATE_NAME, 0);
    }

    public TemplateIO getTemplateInput(String name) {
        return getTemplateInput(name, 0);
    }

    public TemplateIO getTemplateInput(long versionId) {
        return getTemplateInput(TEMPLATE_NAME, versionId);
    }

    public TemplateIO getTemplateInput(String name, long versionId) {
        TemplateIO.FileRightsIO fileRights = new TemplateIO.FileRightsIO(true, true, true);
        TemplateIO.RightsIO rights = new TemplateIO.RightsIO(fileRights, fileRights, fileRights);
        return new TemplateIO(name, null, "template.json", "/location", "content", rights, versionId);
    }

    public void assertTemplateProperties(TemplateIO templateOutput, String expectedNamespace, long expectedVersionId) {
        TemplateIO templateInput = getTemplateInput();
        assertEquals(expectedNamespace, templateOutput.getNamespace());
        assertEquals(templateInput.getName(), templateOutput.getName());
        assertEquals(templateInput.getFilename(), templateOutput.getFilename());
        assertEquals(templateInput.getLocation(), templateOutput.getLocation());
        assertEquals(templateInput.getContent(), templateOutput.getContent());
        assertRights(templateInput.getRights(), templateOutput.getRights());
        assertEquals(expectedVersionId, templateOutput.getVersionId().longValue());
    }

    private void assertRights(TemplateIO.RightsIO rightsInput, TemplateIO.RightsIO rightsOutput) {
        assertEquals(rightsInput.getUser().getRead(), rightsOutput.getUser().getRead());
        assertEquals(rightsInput.getUser().getWrite(), rightsOutput.getUser().getWrite());
        assertEquals(rightsInput.getUser().getExecute(), rightsOutput.getUser().getExecute());
        assertEquals(rightsInput.getGroup().getRead(), rightsOutput.getGroup().getRead());
        assertEquals(rightsInput.getGroup().getWrite(), rightsOutput.getGroup().getWrite());
        assertEquals(rightsInput.getGroup().getExecute(), rightsOutput.getGroup().getExecute());
        assertEquals(rightsInput.getOther().getRead(), rightsOutput.getOther().getRead());
        assertEquals(rightsInput.getOther().getWrite(), rightsOutput.getOther().getWrite());
        assertEquals(rightsInput.getOther().getExecute(), rightsOutput.getOther().getExecute());
    }
}
