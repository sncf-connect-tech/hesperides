/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.files;

import lombok.Getter;
import lombok.Setter;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileBuilder {

    @Getter
    @Setter
    private boolean simulate;

    private final PlatformBuilder platformBuilder;
    private final TechnoBuilder technoBuilder;
    private final ModuleBuilder moduleBuilder;

    @Autowired
    public FileBuilder(PlatformBuilder platformBuilder, TechnoBuilder technoBuilder, ModuleBuilder moduleBuilder) {
        this.platformBuilder = platformBuilder;
        this.technoBuilder = technoBuilder;
        this.moduleBuilder = moduleBuilder;
    }

    public void reset() {
        simulate = false;
    }

    private Optional<DeployedModuleBuilder> getDeployedModuleBuilder() {
        return platformBuilder.findMatchingDeployedModuleBuilder(moduleBuilder);
    }

    public String buildModulePath() {
        return getDeployedModuleBuilder().map(DeployedModuleBuilder::getModulePath).orElse("anything");
    }

    public String buildInstanceName() {
        return getDeployedModuleBuilder().isPresent() && !CollectionUtils.isEmpty(getDeployedModuleBuilder().get().getInstanceBuilders()) && !simulate
                ? getDeployedModuleBuilder().get().getInstanceBuilders().get(0).getName()
                : "anything";
    }

    public List<InstanceFileOutput> buildInstanceFileOutputs() {
        List<InstanceFileOutput> instanceFiles = new ArrayList<>();
        technoBuilder.getTemplateBuilders().stream().map(this::templateToInstanceFile).forEach(instanceFiles::add);
        moduleBuilder.getTemplateBuilders().stream().map(this::templateToInstanceFile).forEach(instanceFiles::add);
        return instanceFiles;
    }

    private InstanceFileOutput templateToInstanceFile(TemplateBuilder templateBuilder) {
        TemplateIO template = templateBuilder.build();
        return buildInstanceFileOutput(platformBuilder.buildInput(), moduleBuilder.build(), buildModulePath(), simulate, buildInstanceName(), template, template.getNamespace());
    }

    private InstanceFileOutput buildInstanceFileOutput(PlatformIO platform, ModuleIO module, String modulePath, boolean simulate, String instanceName, TemplateIO template, String templateNamespace) {
        String location = replacePropertiesWithValues(template.getLocation(), modulePath, instanceName);
        String filename = replacePropertiesWithValues(template.getFilename(), modulePath, instanceName);
        try {
            return new InstanceFileOutput(
                    template.getName(),
                    template.getFilename(),
                    location + "/" + filename,
                    "/rest/files"
                            + "/applications/" + platform.getApplicationName()
                            + "/platforms/" + platform.getPlatformName()
                            + "/" + URLEncoder.encode(modulePath, "UTF-8")
                            + "/" + module.getName()
                            + "/" + module.getVersion()
                            + "/instances/" + instanceName
                            + "/" + template.getName()
                            + "?isWorkingCopy=" + module.getIsWorkingCopy()
                            + "&template_namespace=" + URLEncoder.encode(templateNamespace, "UTF-8")
                            + "&simulate=" + simulate,
                    new InstanceFileOutput.Rights("rwx", "---", "   ")
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cf. org.hesperides.core.application.files.FileUseCases#valorizeWithModuleAndGlobalAndInstanceProperties
     * <p>
     * 1. Une propriété peut être valorisée au niveau de la platforme (propriété globale)
     * et au niveau du module (valorisation classique).
     * <p>
     * 2. La valorisation d'une propriété au niveau du module peut faire référence
     * à une propriété globale ou d'instance.
     * <p>
     * 3. La valorisation d'une propriété d'instance peut faire référence
     * à une propriété globale.
     */
    private String replacePropertiesWithValues(String input, String modulePath, String instanceName) {
        List<ValuedPropertyIO> predefinedProperties = getPredefinedProperties(modulePath, instanceName);
        List<ValuedPropertyIO> globalProperties = platformBuilder.getGlobalProperties();
        List<ValuedPropertyIO> moduleProperties = platformBuilder.getAllModuleProperties();
        List<ValuedPropertyIO> instanceProperties = platformBuilder.getAllInstanceProperties();

        List<ValuedPropertyIO> moduleAndGlobalProperties = Stream.concat(moduleProperties.stream(), globalProperties.stream()).collect(Collectors.toList());
        List<ValuedPropertyIO> globalAndInstanceProperties = Stream.concat(globalProperties.stream(), instanceProperties.stream()).collect(Collectors.toList());

        input = PropertyBuilder.replacePropertiesWithValues(input, predefinedProperties, moduleAndGlobalProperties);
        input = PropertyBuilder.replacePropertiesWithValues(input, predefinedProperties, globalAndInstanceProperties);
        return PropertyBuilder.replacePropertiesWithValues(input, predefinedProperties, globalProperties);
    }

    private List<ValuedPropertyIO> getPredefinedProperties(String modulePath, String instanceName) {
        PlatformIO platform = platformBuilder.buildInput();
        ModuleIO module = moduleBuilder.build();
        return Arrays.asList(
                new ValuedPropertyIO("hesperides.application.name", platform.getApplicationName()),
                new ValuedPropertyIO("hesperides.application.version", platform.getVersion()),
                new ValuedPropertyIO("hesperides.platform.name", platform.getPlatformName()),
                new ValuedPropertyIO("hesperides.module.name", module.getName()),
                new ValuedPropertyIO("hesperides.module.version", module.getVersion()),
                new ValuedPropertyIO("hesperides.module.path.full", modulePath.replace('#', '/')),
                new ValuedPropertyIO("hesperides.instance.name", instanceName));
    }
}
