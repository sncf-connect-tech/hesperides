package com.vsct.dt.hesperides.templating.modules.template.event;

import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.template.Template;

import java.util.Set;

/**
 * Created by emeric_martineau on 27/01/2016.
 */
public interface TemplateContainerInterface {
    Template getTemplate(String name);

    void addTemplate(Template template);

    void removeTemplate(String name);

    /**
     * Return list of template.
     *
     * @return set of template
     */
    Set<Template> loadAllTemplate();

    void setModule(Module m);

    Module getModule();

    /**
     * Clear module for reuse object.
     */
    void clear();
}
