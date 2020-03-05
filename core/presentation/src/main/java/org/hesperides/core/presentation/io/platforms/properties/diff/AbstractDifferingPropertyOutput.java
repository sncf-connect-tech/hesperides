package org.hesperides.core.presentation.io.platforms.properties.diff;

import com.google.gson.*;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.entities.properties.diff.AbstractDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.IterableDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.SimpleDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.visitors.IterablePropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.PropertyVisitor;
import org.hesperides.core.domain.platforms.entities.properties.visitors.SimplePropertyVisitor;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@NonFinal
public abstract class AbstractDifferingPropertyOutput {

    String name;

    private static AbstractDifferingPropertyOutput fromAbstractDifferingProperty(AbstractDifferingProperty abstractDifferingProperty) {
        AbstractDifferingPropertyOutput abstractDifferingPropertyOutput;
        if (abstractDifferingProperty instanceof SimpleDifferingProperty) {
            abstractDifferingPropertyOutput = new DualDifferingPropertyOutput((SimpleDifferingProperty)abstractDifferingProperty);
        } else {
            abstractDifferingPropertyOutput = new IterableDifferingPropertyOutput((IterableDifferingProperty)abstractDifferingProperty);
        }
        return abstractDifferingPropertyOutput;
    }

    static Set<AbstractDifferingPropertyOutput> fromAbstractDifferingProperties(Set<AbstractDifferingProperty> abstractDifferingProperties) {
        return Optional.ofNullable(abstractDifferingProperties)
                .orElseGet(Collections::emptySet)
                .stream()
                .map(AbstractDifferingPropertyOutput::fromAbstractDifferingProperty)
                .collect(Collectors.toSet());
    }

    static AbstractDifferingPropertyOutput nonDifferingFromPropertyVisitor(PropertyVisitor propertyVisitor) {
        AbstractDifferingPropertyOutput abstractDifferingPropertyOutput;
        if (propertyVisitor instanceof SimplePropertyVisitor) {
            abstractDifferingPropertyOutput = new NonDifferingPropertyOutput((SimplePropertyVisitor)propertyVisitor);
        } else {
            abstractDifferingPropertyOutput = IterableDifferingPropertyOutput.onlyCommon((IterablePropertyVisitor)propertyVisitor);
        }
        return abstractDifferingPropertyOutput;
    }

    public static class Adapter implements JsonDeserializer<AbstractDifferingPropertyOutput>, JsonSerializer<AbstractDifferingPropertyOutput> {

        @Override
        public AbstractDifferingPropertyOutput deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Class<? extends AbstractDifferingPropertyOutput> subClassType;
            if (jsonObject.has("items")) {
                subClassType = IterableDifferingPropertyOutput.class;
            } else if (jsonObject.has("value")) {
                subClassType = NonDifferingPropertyOutput.class;
            } else {
                subClassType = DualDifferingPropertyOutput.class;
            }
            return context.deserialize(json, subClassType);
        }

        @Override
        public JsonElement serialize(AbstractDifferingPropertyOutput src, Type typeOfSrc, JsonSerializationContext context) {
            Class<? extends AbstractDifferingPropertyOutput> subClassType;
            if (src instanceof IterableDifferingPropertyOutput) {
                subClassType = IterableDifferingPropertyOutput.class;
            } else if (src instanceof NonDifferingPropertyOutput) {
                subClassType = NonDifferingPropertyOutput.class;
            } else {
                subClassType = DualDifferingPropertyOutput.class;
            }
            return context.serialize(src, subClassType);
        }
    }
}
