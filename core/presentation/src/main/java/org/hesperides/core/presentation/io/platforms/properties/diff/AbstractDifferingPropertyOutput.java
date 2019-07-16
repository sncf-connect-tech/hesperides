package org.hesperides.core.presentation.io.platforms.properties.diff;

import com.google.gson.*;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.entities.properties.diff.AbstractDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.IterableDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.SimpleDifferingProperty;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@NonFinal
public abstract class AbstractDifferingPropertyOutput {

    String name;

    public static AbstractDifferingPropertyOutput fromAbstractDifferingProperty(AbstractDifferingProperty abstractDifferingProperty){
        if (abstractDifferingProperty instanceof SimpleDifferingProperty) {
            return new SimpleDifferingPropertyOutput((SimpleDifferingProperty)abstractDifferingProperty);
        } else {
            return new IterableDifferingPropertyOutput((IterableDifferingProperty)abstractDifferingProperty);
        }
    }

    public static Set<AbstractDifferingPropertyOutput> fromAbstractDifferingProperties(Set<AbstractDifferingProperty> abstractDifferingProperties) {
        return Optional.ofNullable(abstractDifferingProperties)
                .orElseGet(Collections::emptySet)
                .stream()
                .map(AbstractDifferingPropertyOutput::fromAbstractDifferingProperty)
                .collect(Collectors.toSet());
    }

    public static class Serializer implements JsonDeserializer<AbstractDifferingPropertyOutput>, JsonSerializer<AbstractDifferingPropertyOutput> {

        @Override
        public AbstractDifferingPropertyOutput deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement items = jsonObject.get("differing_items");
            Class<? extends AbstractDifferingPropertyOutput> subClass = items != null ? IterableDifferingPropertyOutput.class : SimpleDifferingPropertyOutput.class;
            return context.deserialize(json, subClass);
        }

        @Override
        public JsonElement serialize(AbstractDifferingPropertyOutput src, Type typeOfSrc, JsonSerializationContext context) {
            Class<? extends AbstractDifferingPropertyOutput> subClass = src instanceof SimpleDifferingPropertyOutput ? SimpleDifferingPropertyOutput.class : IterableDifferingPropertyOutput.class;
            return context.serialize(src, subClass);
        }
    }
}
