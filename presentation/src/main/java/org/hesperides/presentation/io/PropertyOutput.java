package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.domain.templatecontainer.queries.PropertyView;

import java.util.List;
import java.util.stream.Collectors;

@Value
@NonFinal
public class PropertyOutput {

    String name;
    @SerializedName("required")
    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    @SerializedName("password")
    boolean isPassword;

    public static List<PropertyOutput> fromPropertyViews(List<PropertyView> propertyViews) {
        List<PropertyOutput> propertyOutputs = null;
        if (propertyViews != null) {
            propertyOutputs = propertyViews.stream().map(PropertyOutput::fromPropertyView).collect(Collectors.toList());
        }
        return propertyOutputs;
    }

    public static PropertyOutput fromPropertyView(PropertyView propertyView) {
        PropertyOutput propertyOutput = null;
        if (propertyView != null) {
            propertyOutput = new PropertyOutput(
                    propertyView.getName(),
                    propertyView.isRequired(),
                    propertyView.getComment(),
                    propertyView.getDefaultValue(),
                    propertyView.getPattern(),
                    propertyView.isPassword()
            );
        }
        return propertyOutput;
    }
}
