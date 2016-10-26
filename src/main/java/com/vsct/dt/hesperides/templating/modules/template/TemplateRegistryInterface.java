package com.vsct.dt.hesperides.templating.modules.template;

import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public interface TemplateRegistryInterface {
    Collection<Template> allTemplates();

    Optional<Template> getTemplate(String namespace, String name);

    Optional<Template> getTemplate(TemplatePackageKey packageKey, String name);

    boolean existsTemplate(String namespace, String name);

    void createOrUpdateTemplate(Template template);

    void deleteTemplate(String namespace, String name);

    Set<Template> getAllTemplatesForNamespace(String namespace);

    Set<Template> getAllTemplates(TemplatePackageKey packageKey);

    boolean templateHasNamespace(String namespace);

    /**
     * Remove item from cache.
     *
     * @param packageKey
     */
    void removeFromCache(TemplatePackageKey packageKey);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
