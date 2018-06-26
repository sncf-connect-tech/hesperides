package org.hesperides.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.domain.templatecontainers.queries.PropertyView;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.method.P;

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

    public PropertyOutput(AbstractPropertyView abstractPropertyView) {
        this.name = abstractPropertyView.getName();

        if (abstractPropertyView instanceof PropertyView) {
            final PropertyView propertyView = (PropertyView) abstractPropertyView;
            this.required = propertyView.isRequired();
            this.comment = propertyView.getComment();
            this.defaultValue = propertyView.getDefaultValue();
            this.pattern = propertyView.getPattern();
            this.password = propertyView.isPassword();
            this.properties = null;

        } else if (abstractPropertyView instanceof IterablePropertyView) {
            // Valeurs par défaut
            this.required = false;
            this.comment = "";
            this.defaultValue = "";
            this.pattern = "";
            this.password = false;

            final IterablePropertyView iterablePropertyView = (IterablePropertyView) abstractPropertyView;
            this.properties = PropertyOutput.fromAbstractPropertyViews(iterablePropertyView.getProperties());

        } else {
            throw new IllegalArgumentException("Can't instanciate a PropertyOutput based on an AbstractPropertyView that is not a PropertyView or an IterablePropertyView");
        }
    }

    private static List<PropertyOutput> fromAbstractPropertyViews(List<AbstractPropertyView> abstractPropertyViews) {
        List<PropertyOutput> propertyOutputs = new ArrayList<>();
        if (abstractPropertyViews != null) {
            for (AbstractPropertyView abstractPropertyView : abstractPropertyViews) {
                propertyOutputs.add(new PropertyOutput(abstractPropertyView));
            }
        }
        return propertyOutputs;
    }

    @Override
    public int compareTo(@NotNull PropertyOutput o) {
        return this.name.compareTo(o.name);
    }
}
