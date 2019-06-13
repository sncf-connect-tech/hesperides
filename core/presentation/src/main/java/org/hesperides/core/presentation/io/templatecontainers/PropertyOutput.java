package org.hesperides.core.presentation.io.templatecontainers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class PropertyOutput {

    String name;
    @SerializedName("required")
    @JsonProperty("required")
    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    @SerializedName("password")
    @JsonProperty("password")
    boolean isPassword;
    @SerializedName("fields")
    @JsonProperty("fields")
    Set<PropertyOutput> properties;


    /**
     * Comme par défaut on sérialise les champs à null pour être iso-legacy
     * mais que le legacy exclue le champ `fields` lorsqu'il n'est pas renseigné,
     * on l'exclue ici manuellement.
     *
     * De plus, le serializer ne gère pas la récursivité nativement
     * donc on la gère à la main.
     */
    public static class Serializer implements JsonSerializer<PropertyOutput> {
        @Override
        public JsonElement serialize(PropertyOutput src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            JsonObject jsonObject = (JsonObject) gson.toJsonTree(src);
            jsonObject.remove("fields");
            if (src.getProperties() != null) {
                // Cas d'une propriété itérable : seuls les champs "name" & "fields" ont du sens
                JsonArray jsonArray = new JsonArray();
                for (PropertyOutput propertyOutput : src.getProperties()) {
                    jsonArray.add(context.serialize(propertyOutput, PropertyOutput.class));
                }
                jsonObject.add("fields", jsonArray);
                jsonObject.remove("required");
                jsonObject.remove("comment");
                jsonObject.remove("defaultValue");
                jsonObject.remove("pattern");
                jsonObject.remove("password");
            }
            return jsonObject;
        }
    }

    public PropertyOutput(AbstractPropertyView abstractPropertyView) {
        this.name = abstractPropertyView.getName();

        if (abstractPropertyView instanceof PropertyView) {
            final PropertyView propertyView = (PropertyView) abstractPropertyView;
            this.isRequired = propertyView.isRequired();
            this.comment = propertyView.getComment();
            this.defaultValue = propertyView.getDefaultValue();
            this.pattern = propertyView.getPattern();
            this.isPassword = propertyView.isPassword();
            this.properties = null;

        } else if (abstractPropertyView instanceof IterablePropertyView) {
            // Valeurs par défaut
            this.isRequired = false;
            this.comment = "";
            this.defaultValue = "";
            this.pattern = "";
            this.isPassword = false;

            final IterablePropertyView iterablePropertyView = (IterablePropertyView) abstractPropertyView;
            this.properties = PropertyOutput.fromAbstractPropertyViews(iterablePropertyView.getProperties());

        } else {
            throw new IllegalArgumentException("Can't instanciate a PropertyOutput based on an AbstractPropertyView that is not a PropertyView or an IterablePropertyView");
        }
    }

    private static Set<PropertyOutput> fromAbstractPropertyViews(List<AbstractPropertyView> abstractPropertyViews) {
        return Optional.ofNullable(abstractPropertyViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PropertyOutput::new)
                .collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName(), this.getComment());
    }

    @Override
    public boolean equals(Object object) {
        boolean equals;
        if (this == object) {
            equals = true;
        } else if (object == null || getClass() != object.getClass()) {
            equals = false;
        } else {
            final PropertyOutput otherProperty = (PropertyOutput) object;
            equals = Objects.equals(this.getName(), otherProperty.getName()) &&
                    Objects.equals(this.getComment(), otherProperty.getComment());
        }
        return equals;
    }
}
