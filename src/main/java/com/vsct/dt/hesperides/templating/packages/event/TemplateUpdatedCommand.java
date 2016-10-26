package com.vsct.dt.hesperides.templating.packages.event;

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent;

import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class TemplateUpdatedCommand implements HesperidesCommand<TemplateUpdatedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final TemplatePackageKey packageKey;
    private final TemplateData templateData;

    /**
     * Platform to update.
     */
    private Template updated;

    public TemplateUpdatedCommand(final TemplateRegistryInterface templateRegistry, final TemplatePackageKey packageKey,
                                  final TemplateData templateData) {
        this.templateRegistry = templateRegistry;
        this.packageKey = packageKey;
        this.templateData = templateData;
    }

    @Override
    public TemplateUpdatedEvent apply() {
        final Optional<Template> template = templateRegistry.getTemplate(packageKey.getNamespace(), templateData.getName());

        if(template.isPresent()){

            template.get().tryCompareVersionID(templateData.getVersionID());

            updated = new Template(
                    packageKey.getNamespace(),
                    templateData.getName(),
                    templateData.getFilename(),
                    templateData.getLocation(),
                    templateData.getContent(),
                    templateData.getRights(),
                    templateData.getVersionID() + 1
            );

            return new TemplateUpdatedEvent(updated);
        } else {
            throw new MissingResourceException("Cannot update template " + templateData + " because it does not exists");
        }
    }

    @Override
    public void complete() {
        templateRegistry.createOrUpdateTemplate(updated);
    }
}
