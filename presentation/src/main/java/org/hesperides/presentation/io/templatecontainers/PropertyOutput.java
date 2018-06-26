package org.hesperides.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.domain.templatecontainers.queries.PropertyView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Value
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

    public static PropertyOutput fromPropertyView(PropertyView propertyView) {
        return new PropertyOutput(
                propertyView.getName(),
                propertyView.isRequired(),
                propertyView.getComment(),
                propertyView.getDefaultValue(),
                propertyView.getPattern(),
                propertyView.isPassword(),
                null
        );
    }

    public static PropertyOutput fromIterablePropertyView(IterablePropertyView iterablePropertyView) {
        return new PropertyOutput(
                iterablePropertyView.getName(),
                false,
                "",
                "",
                "",
                false,
                PropertyOutput.fromAbstractPropertyViews(iterablePropertyView.getProperties())
        );
    }

    private static List<PropertyOutput> fromAbstractPropertyViews(List<AbstractPropertyView> abstractPropertyViews) {
        List<PropertyOutput> propertyOutputs = new ArrayList<>();

        if (abstractPropertyViews != null) {
            for (AbstractPropertyView abstractPropertyView : abstractPropertyViews) {
                PropertyOutput propertyOutput = fromAbstractPropertyView(abstractPropertyView);
                if (propertyOutput != null) {
                    propertyOutputs.add(propertyOutput);
                }
            }
        }

        return propertyOutputs;
    }

    public static PropertyOutput fromAbstractPropertyView(AbstractPropertyView abstractPropertyView) {
        PropertyOutput propertyOutput = null;
        if (abstractPropertyView instanceof PropertyView) {
            PropertyView propertyView = (PropertyView) abstractPropertyView;
            propertyOutput = PropertyOutput.fromPropertyView(propertyView);
        } else if (abstractPropertyView instanceof IterablePropertyView) {
            IterablePropertyView iterablePropertyView = (IterablePropertyView) abstractPropertyView;
            propertyOutput = PropertyOutput.fromIterablePropertyView(iterablePropertyView);
        }
        return propertyOutput;
    }

    @Override
    public int compareTo(@NotNull PropertyOutput o) {
        return this.name.compareTo(o.name);
    }
}
