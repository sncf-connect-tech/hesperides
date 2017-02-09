/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.files;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import com.github.mustachejava.util.Wrapper;
import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.templating.Template;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.IterablePropertyModel;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import com.vsct.dt.hesperides.templating.TemplateFileRights;
import com.vsct.dt.hesperides.templating.TemplateRights;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.modules.Techno;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.platform.*;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.TemplateContentGenerator;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by william_montaz on 06/01/2015.
 */
public class Files {

    private final Applications              applications;
    private final ModulesAggregate          modules;
    private final TemplatePackagesAggregate templatePackages;

    private final DefaultMustacheFactory mustacheFactory;

    public Files(Applications applications, ModulesAggregate modules, TemplatePackagesAggregate templatePackages) {
        this.applications = applications;
        this.modules = modules;
        this.templatePackages = templatePackages;
        mustacheFactory = new NoHTMLEscapingMustacheFactory();
        mustacheFactory.setObjectHandler(new PropertiesWithDotHandler());
    }

    /**
     * List the files location for an instance of a specific platform
     * @param applicationName
     * @param platformName
     * @param path
     * @param moduleName
     * @param moduleVersion
     * @param instanceName
     * @return
     */
    public Set<HesperidesFile> getLocations(String applicationName, String platformName, String path, String moduleName, String moduleVersion, boolean isModuleWorkingCopy, String instanceName, Boolean simulate) {

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();


        PlatformData platform = applications.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("There is no platform " + applicationName + "/" + platformName));
        PropertiesData properties = applications.getProperties(platformKey, generatePropertiesPath(path, moduleName, moduleVersion, isModuleWorkingCopy));

        //Find the module and the related templates
        //Check that the module is defined in the platform and then get the real module representing it
        ModuleKey moduleKey = new ModuleKey(
                moduleName,
                new HesperidesVersion(moduleVersion, isModuleWorkingCopy)
        );

        ApplicationModuleData applicationModule = platform.findModule(moduleName, moduleVersion, isModuleWorkingCopy, path).orElseThrow(() -> new MissingResourceException("There is no module "+moduleName+"/"+moduleVersion+"/"+(isModuleWorkingCopy ? "WorkingCopy":"Release" + " defined for platform "+applicationName+"/"+platformName +" at path "+path)));

        Module module = modules.getModule(moduleKey).orElseThrow(() -> new MissingResourceException("There is no module " + moduleName + "/" + moduleVersion + "/" + (isModuleWorkingCopy ? "WorkingCopy" : "Release")));

        List<Template> templates = modules.getAllTemplates(moduleKey);

        //Dont forget the technos
        for(Techno techno : module.getTechnos()){

            TemplatePackageKey packageInfo = new TemplatePackageKey(
                    techno.getName(),
                    new HesperidesVersion(techno.getVersion(), techno.isWorkingCopy())
            );

            Set<Template> technoTemplates = templatePackages.getAllTemplates(packageInfo);
            templates.addAll(technoTemplates);
        }

        //Get the instance
        InstanceData instance = applicationModule.getInstance(instanceName, simulate).orElseThrow(() -> new MissingResourceException("There is no instance " + instanceName + " in platform " + applicationName + "/" + platformName));

        PropertiesData platformGlobalProperties = applications.getProperties(platformKey, "#");

        Set<KeyValueValorisationData> hesperidesPlatformPredefinedScope = platform.generateHesperidesPredefinedScope();
        hesperidesPlatformPredefinedScope.addAll(platformGlobalProperties.getKeyValueProperties());

        Set<KeyValueValorisationData> hesperidesModulePredefinedScope = applicationModule.generateHesperidesPredefinedScope();
        hesperidesPlatformPredefinedScope.addAll(hesperidesModulePredefinedScope);

        hesperidesPlatformPredefinedScope.addAll(instance.generatePredefinedScope());

        //Generate the mustache scope correpsonding to the instance chosen and the properties path of the platform
        MustacheScope mustacheScope = properties.toMustacheScope(instance.getKeyValues(),
                hesperidesPlatformPredefinedScope);

        return templates.stream()
                .map(template -> getLocationFromMustacheScopeEvaluation(template, mustacheScope))
                .collect(Collectors.toSet());
    }

    private String generatePropertiesPath(String path, String moduleName, String moduleVersion, boolean isModuleWorkingCopy) {
        StringBuilder propertiesPath = new StringBuilder();
        return propertiesPath.append(path).append("#").append(moduleName).append("#").append(moduleVersion).append("#").append(isModuleWorkingCopy ? "WORKINGCOPY" : "RELEASE").toString();
    }

    /**
     * Helper method to evaluate the filename and location.
     * That implementation uses mustache to parse thoose simple fields,
     * we might consider to be more efficient by string replacement,
     * or guessing if we nedd replacement in the first place....
     *
     * @param template
     * @param mustacheScope
     * @return
     */
    private HesperidesFile getLocationFromMustacheScopeEvaluation(Template template, MustacheScope mustacheScope) {
        /* NOT EFFICIENT */
        Mustache mustacheFilename = mustacheFactory.compile(new StringReader(template.getFilename()), "something");
        String filename = TemplateContentGenerator.from(mustacheFilename).withScope(mustacheScope).generate();

        /* NOT EFFICIENT */
        Mustache mustacheLocation = mustacheFactory.compile(new StringReader(template.getLocation()), "something");
        String location = TemplateContentGenerator.from(mustacheLocation).withScope(mustacheScope).generate();

        return new HesperidesFile(template.getNamespace(), template.getName(), location,filename,
                convertRights(template.getRights()));
    }

    /**
     * Convert TemplateRights to HesperidesFileRights
     * @param rights
     * @return
     */
    private static HesperidesFileRights convertRights(TemplateRights rights) {
        HesperidesFileRights hfr = null;

        if (rights != null) {
            HesperidesRight user = null;
            HesperidesRight group = null;
            HesperidesRight other = null;

            if (rights.getUser() != null) {
                user = convertRight(rights.getUser());
            }

            if (rights.getGroup() != null) {
                group = convertRight(rights.getGroup());
            }

            if (rights.getOther() != null) {
                other = convertRight(rights.getOther());
            }

            hfr = new HesperidesFileRights(user, group, other);
        }

        return hfr;
    }

    /**
     * Convert TemplateFileRights to HesperidesRight
     * @param rights TemplateFileRights
     * @return HesperidesRight
     */
    private static HesperidesRight convertRight(final TemplateFileRights rights) {
        HesperidesRight group;
        group = new HesperidesRight(rights.isRead(), rights.isWrite(), rights.isExecute());

        return group;
    }

    /**
     * Evaluate a template with properties
     * @param applicationName
     * @param platformName
     * @param path
     * @param moduleName
     * @param moduleVersion
     * @param isModuleWorkingCopy
     * @param instanceName
     * @return
     */
    public String getFile(String applicationName,
                          String platformName,
                          String path,
                          String moduleName,
                          String moduleVersion,
                          boolean isModuleWorkingCopy,
                          String instanceName,
                          String templateNamespace,
                          String templateName, HesperidesPropertiesModel model,
                          Boolean simulate) {

        PlatformKey platformKey = PlatformKey.withName(platformName)
                .withApplicationName(applicationName)
                .build();

        PlatformData platform = applications.getPlatform(platformKey).orElseThrow(() -> new MissingResourceException("There is no platform " + applicationName + "/" + platformName));
        PropertiesData properties = applications.getSecuredProperties(platformKey, generatePropertiesPath(path, moduleName, moduleVersion, isModuleWorkingCopy), model);

        PropertiesData platformGlobalProperties = applications.getProperties(platformKey, "#");

        Set<KeyValueValorisationData> hesperidesPlatformPredefinedScope = platform.generateHesperidesPredefinedScope();

        hesperidesPlatformPredefinedScope.addAll(platformGlobalProperties.getKeyValueProperties());

        ApplicationModuleData applicationModule = platform.findModule(moduleName, moduleVersion, isModuleWorkingCopy, path).orElseThrow(() -> new MissingResourceException("There is no module "+moduleName+"/"+moduleVersion+"/"+(isModuleWorkingCopy ? "WorkingCopy":"Release" + " defined for platform "+applicationName+"/"+platformName +" at path "+path)));

        //Get the instance
        InstanceData instance = applicationModule.getInstance(instanceName, simulate).orElseThrow(() -> new MissingResourceException("There is no instance " + instanceName + " in platform " + applicationName + "/" + platformName));

        Set<KeyValueValorisationData> hesperidesModulePredefinedScope = applicationModule.generateHesperidesPredefinedScope();
        hesperidesPlatformPredefinedScope.addAll(hesperidesModulePredefinedScope);

        hesperidesPlatformPredefinedScope.addAll(instance.generatePredefinedScope());

        //Generate the mustache scope correpsonding to the instance chosen and the properties path of the platform
        MustacheScope mustacheScope = properties.toMustacheScope(instance.getKeyValues(), hesperidesPlatformPredefinedScope, true);

        //Find the template
        ModuleKey moduleKey = new ModuleKey(
                moduleName,
                new HesperidesVersion(moduleVersion, isModuleWorkingCopy)
        );
        
        Template template = manageModule(moduleName, moduleVersion, isModuleWorkingCopy, templateNamespace,
                templateName, mustacheScope, moduleKey);

        //Test template is null
        if(template == null){
            throw new MissingResourceException("Could not find template "+templateNamespace+"/"+templateName);
        }

        Mustache mustacheTemplate = mustacheFactory.compile(new StringReader(template.getContent()), "something");
        return TemplateContentGenerator.from(mustacheTemplate).withScope(mustacheScope).generate();
    }

    /**
     * Manage module template.
     *
     * @param moduleName
     * @param moduleVersion
     * @param isModuleWorkingCopy
     * @param templateNamespace
     * @param templateName
     * @param mustacheScope
     * @param moduleKey
     *
     * @return
     */
    private Template manageModule(final String moduleName, final String moduleVersion,
                                  final boolean isModuleWorkingCopy, final String templateNamespace,
                                  final String templateName, final MustacheScope mustacheScope,
                                  final ModuleKey moduleKey) {
        Template template = null;

        if(templateNamespace.startsWith("modules")){
            template = modules.getTemplate(moduleKey, templateName).orElseThrow(()
                    -> new MissingResourceException("Could not find template " + templateName + " in module "
                    + moduleName + "/" + moduleVersion + "/" + (isModuleWorkingCopy ? "WorkingCopy" : "Release")));
        } else if(templateNamespace.startsWith("packages")) {
            template = templatePackages.getTemplate(templateNamespace, templateName).orElseThrow(() -> new MissingResourceException("Could not find template "+templateNamespace+"/"+templateName));
        }

        HesperidesPropertiesModel templateModel
                = modules.getModel(moduleKey).orElseThrow(() -> new MissingResourceException("Could not find module " + moduleKey));
        boolean exists;

        // Taking care of key-value properties
        for (KeyValuePropertyModel kvpm : templateModel.getKeyValueProperties()) {
            exists = isInScope(kvpm.getName(), mustacheScope);

            if (kvpm.isRequired() && !exists) {
                throw new MissingResourceException(String.format("Property '%s' in template '%s/%s' must be set.",
                        kvpm.getName(), templateNamespace, templateName));
            }

            if (StringUtils.isNotEmpty(kvpm.getDefaultValue()) && !exists) {
                mustacheScope.put(kvpm.getName(), kvpm.getDefaultValue());
            }

            if (StringUtils.isNotEmpty(kvpm.getPattern())) {
                String propVal = findProperty(kvpm.getName(), mustacheScope);

                if (propVal != null) {
                    Pattern p = Pattern.compile(kvpm.getPattern());
                    Matcher m = p.matcher(propVal);

                    if (!m.matches()) {
                        throw new MissingResourceException(String.format(
                                "Property '%s' in template '%s/%s' not match regular expression '%s'.",
                                kvpm.getName(), templateNamespace, templateName, kvpm.getPattern()));
                    }
                }
            }
        }

        // Taking care of iterable properties
        // TODO : Update this to make multiple level implementation available.
        for (IterablePropertyModel itpm : templateModel.getIterableProperties()){
            // Get the valuation for this
            if (mustacheScope.keySet().contains(itpm.getName())){
                ArrayList<MustacheScope> scopes = (ArrayList<MustacheScope>) mustacheScope.get(itpm.getName());

                scopes.forEach(scope -> {

                    itpm.getFields().forEach(p -> {
                        // check if the property exists in scope.
                        boolean _exists = isInScope(p.getName(), scope);

                        // required but not existing
                        if (!_exists && p.isRequired()){
                            throw new MissingResourceException(String.format("Property '%s' in template '%s/%s' must be set.",
                                    p.getName(), templateNamespace, templateName));
                        }

                        // has default value and not existing
                        if (!_exists && StringUtils.isNotEmpty(p.getDefaultValue())){
                            scope.put(p.getName(), p.getDefaultValue());
                        }

                        // has pattern
                        if (StringUtils.isNotEmpty(p.getPattern())){
                            // get the value
                            String value = findProperty(p.getName(), scope);
                            Pattern pattern = Pattern.compile(p.getPattern());
                            Matcher matcher = pattern.matcher(value);
                            if (!matcher.matches()){
                                throw new MissingResourceException(String.format(
                                        "Property '%s' in template '%s/%s' not match regular expression '%s'.",
                                        p.getName(), templateNamespace, templateName, p.getPattern()));
                            }
                        }
                    });
                });
            }
        }

        return template;
    }

    /**
     * Check if found property in scope.
     *
     * @param name property name.
     * @param mustacheScope scope
     *
     * @return true if found else otherwise.
     */
    private static boolean isInScope(final String name, final MustacheScope mustacheScope) {
        boolean found = false;

        for (String kvv : mustacheScope.keySet()) {

            if (name.equals(kvv)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Found property in scope.
     *
     * @param name property name.
     * @param mustacheScope scope
     *
     * @return true if not found.
     */
    private static String findProperty(final String name, final MustacheScope mustacheScope) {
        String result = null;

        for (Map.Entry<String, Object> kvv : mustacheScope.entrySet()) {
            if (name.equals(kvv.getKey())) {
                result = kvv.getValue().toString();
                break;
            }
        }

        return result;
    }

    /* This handlers helps using properties with dotted name like com.vsct.hesperides */
    /* it actually looks for the entire dotted property instead of trying to go inside nested objects */
    private static class PropertiesWithDotHandler extends ReflectionObjectHandler {

        @Override
        public Wrapper find(final String name, final List<Object> scopes) {
            Object scope;
            String real_name = name.split("[|]")[0].trim();
            int scope_index = scopes.size() - 1;

            final ListIterator<Object> scopeIterator = scopes.listIterator(scopes.size());

            // We must search from local scope to global scope in case of iterable properties.
            while (scopeIterator.hasPrevious()) {
                scope = scopeIterator.previous();

                if (scope instanceof Map && ((Map) scope).containsKey(real_name)) {
                    final int index = scope_index;
                    return scopes1 -> ((Map) scopes1.get(index)).get(real_name);
                }

                scope_index--;
            }

            return super.find(name, scopes);
        }

    }

    private static class NoHTMLEscapingMustacheFactory extends DefaultMustacheFactory {

        @Override
        public void encode(String value, Writer writer) {
            try {
                writer.write(value);
            } catch (IOException e) {
                //Should never be here
                e.printStackTrace();
                throw new MustacheException("Impossible to execute encode method properly");
            }
        }
    }
}
