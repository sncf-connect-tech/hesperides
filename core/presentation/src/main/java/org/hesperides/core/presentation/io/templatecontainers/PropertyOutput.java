package org.hesperides.core.presentation.io.templatecontainers;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class PropertyOutput implements Comparable<PropertyOutput> {

    String name;
    @SerializedName("required")
    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    @SerializedName("password")
    boolean isPassword;
    @SerializedName("fields")
    List<PropertyOutput> properties;



    /**
     * Permet d'exclure le champs properties de la sérialisation
     * lorsque celui-ci est null. Ce sérialiseur est enregistré
     * dans la classe PresentationConfiguration.
     * PS : Ce serait tellement plus simple avec une annotation
     * "ExcludeIfNull".
     */
    public static class Serializer implements JsonSerializer<PropertyOutput> {
        @Override
        public JsonElement serialize(PropertyOutput src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            JsonObject jsonObject = (JsonObject) gson.toJsonTree(src);
            if (src.getProperties() == null) {
                jsonObject.remove("fields");
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

    private static List<PropertyOutput> fromAbstractPropertyViews(List<AbstractPropertyView> abstractPropertyViews) {
        return Optional.ofNullable(abstractPropertyViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(PropertyOutput::new)
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(@NotNull PropertyOutput o) {
        //TODO Supprimer
        return this.name.compareTo(o.name);
    }
}
