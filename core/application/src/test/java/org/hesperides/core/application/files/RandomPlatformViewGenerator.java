package org.hesperides.core.application.files;

import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.hesperides.core.domain.platforms.queries.views.DeployedModuleView;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;

public class RandomPlatformViewGenerator {

    static PlatformView genPlatformView(int avgGlobalsCount, DeployedModuleProfile... dpProfils) {
        return new PlatformView(
                UUID.randomUUID().toString(),
                genTrigram(),
                genTrigram(),
                genVersion(),
                false,
                genDeployedModules(dpProfils),
                1L,
                genValuedProperties(avgGlobalsCount, 0)
        );
    }

    static List<AbstractPropertyView> genModulePropertiesModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> genPropertyView())
                .collect(Collectors.toList());
    }

    static PropertyView genPropertyView() {
        String name = genNameOrValue();
        return new PropertyView(name, name, false, "", "", "", false);
    }

    static List<DeployedModuleView> genDeployedModules(DeployedModuleProfile... dpProfiles) {
        return Arrays.stream(dpProfiles)
                .map(dpProfile -> IntStream.range(0, dpProfile.times)
                        .mapToObj(i -> genDeployedModule(dpProfile))
                )
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    static DeployedModuleView genDeployedModule(DeployedModuleProfile dpProfile) {
        String name = genNameOrValue();
        String version = genVersion();
        String modulePath = genModulePath();
        List<InstanceView> instances = genInstances(dpProfile.avgInstanceCount, dpProfile.avgInstancePropertiesCount);
        return new DeployedModuleView(
                nextLong(),
                name,
                version,
                true,
                modulePath,
                modulePath + "#" + name + "#" + version + "#WORKINGCOPY",
                instances,
                genAbstractValuedProperties(dpProfile.avgValuedPropertiesCount, dpProfile.avgInstancePropsInValuedPropsCount),
                instances.stream().map(InstanceView::getName).collect(Collectors.toList())
        );
    }

    static List<InstanceView> genInstances(int avgCount, int avgPropertiesCount) {
        return IntStream.range(0, nextInt(1, 2 * avgCount))
                .mapToObj(i -> new InstanceView(genNameOrValue(), genValuedProperties(avgPropertiesCount, 0)))
                .collect(Collectors.toList());
    }

    static List<AbstractValuedPropertyView> genAbstractValuedProperties(int avgCount, int avgInstancePropsInValuedPropsCount) {
        return genValuedProperties(avgCount, avgInstancePropsInValuedPropsCount).stream()
                .map(AbstractValuedPropertyView.class::cast)
                .collect(Collectors.toList());
    }

    static List<ValuedPropertyView> genValuedProperties(int avgCount, int avgInstancePropsInValuedPropsCount) {
        return IntStream.range(0, nextInt(1, 2 * avgCount))
                .mapToObj(i -> new ValuedPropertyView(genNameOrValue(), genNameOrValue()))
                .collect(Collectors.toList());
    }

    static String genModulePath() {
        return "#" + genTrigram() + "#" + genTrigram();
    }

    static String genTrigram() {
        return RandomStringUtils.random(3, true, false).toUpperCase();
    }

    static String genVersion() {
        return Integer.toString(nextInt(0, 10))
                + "." + Integer.toString(nextInt(0, 10))
                + "." + Integer.toString(nextInt(0, 10));
    }

    static String genNameOrValue() {
        return RandomStringUtils.random(10, true, false);
    }

    @Value
    static class DeployedModuleProfile {
        int times;
        int avgValuedPropertiesCount;
        int avgInstancePropsInValuedPropsCount;
        int avgInstanceCount = 4;
        int avgInstancePropertiesCount = 2;
    }

}
