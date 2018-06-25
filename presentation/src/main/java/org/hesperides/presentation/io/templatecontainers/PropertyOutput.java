package org.hesperides.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.domain.templatecontainers.queries.PropertyView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Value
@AllArgsConstructor
public class PropertyOutput implements Comparable<PropertyOutput> {

    String name;
    boolean required;
    String comment;
    String defaultValue;
    String pattern;
    boolean password;
    @SerializedName("fields")
    List<PropertyOutput> properties;

    public PropertyOutput(PropertyView propertyView) {
        this.name = propertyView.getName();
        this.required = propertyView.isRequired();
        this.comment = propertyView.getComment();
        this.defaultValue = propertyView.getDefaultValue();
        this.pattern = propertyView.getPattern();
        this.password = propertyView.isPassword();
        this.properties = null;
    }

    public PropertyOutput(IterablePropertyView iterablePropertyView) {
        this.name = iterablePropertyView.getName();
        this.required = false;
        this.comment = "";
        this.defaultValue = "";
        this.pattern = "";
        this.password = false;
        this.properties = PropertyOutput.fromAbstractPropertyViews(iterablePropertyView.getProperties());
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
        //TODO Est-ce que je peux/dois utiliser un constructeur ?
        PropertyOutput propertyOutput = null;
        if (abstractPropertyView instanceof PropertyView) {
            PropertyView propertyView = (PropertyView) abstractPropertyView;
            propertyOutput = new PropertyOutput(propertyView);
        } else if (abstractPropertyView instanceof IterablePropertyView) {
            IterablePropertyView iterablePropertyView = (IterablePropertyView) abstractPropertyView;
            propertyOutput = new PropertyOutput(iterablePropertyView);
        }
        return propertyOutput;
    }

    @Override
    public int compareTo(@NotNull PropertyOutput o) {
        return this.name.compareTo(o.name);
    }
}
