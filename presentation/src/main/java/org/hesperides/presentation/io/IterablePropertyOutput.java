package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainer.queries.IterablePropertyView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class IterablePropertyOutput extends PropertyOutput {

    @SerializedName("fields")
    List<PropertyOutput> properties;

    public IterablePropertyOutput(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword, List<PropertyOutput> properties) {
        super(name, isRequired, comment, defaultValue, pattern, isPassword);
        this.properties = properties;
    }

    public static List<IterablePropertyOutput> fromIterablePropertyViews(List<IterablePropertyView> iterablePropertyViews) {
        List<IterablePropertyOutput> iterablePropertyOutputs = null;
        if (iterablePropertyViews != null) {
            iterablePropertyOutputs = iterablePropertyViews.stream().map(IterablePropertyOutput::fromIterablePropertyView).collect(Collectors.toList());
        }
        return iterablePropertyOutputs;
    }

    public static IterablePropertyOutput fromIterablePropertyView(IterablePropertyView iterablePropertyView) {
        IterablePropertyOutput iterablePropertyOutput = null;
        if (iterablePropertyView != null) {
            iterablePropertyOutput = new IterablePropertyOutput(
                    iterablePropertyView.getName(),
                    iterablePropertyView.isRequired(),
                    iterablePropertyView.getComment(),
                    iterablePropertyView.getDefaultValue(),
                    iterablePropertyView.getPattern(),
                    iterablePropertyView.isPassword(),
                    PropertyOutput.fromPropertyViews(iterablePropertyView.getProperties())
            );
        }
        return iterablePropertyOutput;
    }
}
