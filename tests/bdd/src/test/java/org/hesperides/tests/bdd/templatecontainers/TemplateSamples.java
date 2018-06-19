package org.hesperides.tests.bdd.templatecontainers;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;

public class TemplateSamples extends CucumberSpringBean implements En {

    public static final String DEFAULT_NAME = "template";
    public static final String DEFAULT_FILENAME = "template.json";
    public static final String DEFAULT_LOCATION = "/location";
    public static final String DEFAULT_CONTENT = "content";
    public static final long DEFAULT_VERSION_ID = 0;

    public static TemplateIO getTemplateInputWithDefaultValues() {
        return new TemplateIO(DEFAULT_NAME, null, DEFAULT_FILENAME, DEFAULT_LOCATION, DEFAULT_CONTENT, getRightsInput(), DEFAULT_VERSION_ID);
    }

    public static TemplateIO getTemplateInputWithName(String name) {
        return new TemplateIO(name, null, DEFAULT_FILENAME, DEFAULT_LOCATION, DEFAULT_CONTENT, getRightsInput(), DEFAULT_VERSION_ID);
    }

    public static TemplateIO getTemplateInputWithVersionId(long versionId) {
        return new TemplateIO(DEFAULT_NAME, null, DEFAULT_FILENAME, DEFAULT_LOCATION, DEFAULT_CONTENT, getRightsInput(), versionId);
    }

    public static TemplateIO getTemplateInputWithNameAndContent(String name, String content) {
        return new TemplateIO(name, null, DEFAULT_FILENAME, DEFAULT_LOCATION, content, getRightsInput(), DEFAULT_VERSION_ID);
    }

    public static TemplateIO getTemplateInputWithNameFilenameLocationAndContent(String name, String filename, String location, String content) {
        return new TemplateIO(name, null, filename, location, content, getRightsInput(), DEFAULT_VERSION_ID);
    }

    public static TemplateIO.RightsIO getRightsInput() {
        TemplateIO.FileRightsIO fileRights = new TemplateIO.FileRightsIO(true, true, true);
        return new TemplateIO.RightsIO(fileRights, fileRights, fileRights);
    }

    public static TemplateIO getTemplateInputWithNameContentContentAndVersionId(String name, String content, long versionId) {
        return new TemplateIO(name, null, DEFAULT_FILENAME, DEFAULT_LOCATION, content, getRightsInput(), versionId);
    }

    public static TemplateIO getTemplateInputWithContent(String content) {
        return new TemplateIO(DEFAULT_NAME, null, DEFAULT_FILENAME, DEFAULT_LOCATION, content, getRightsInput(), DEFAULT_VERSION_ID);
    }
}
