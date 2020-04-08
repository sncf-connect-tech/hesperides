package org.hesperides.core.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.PropertiesEventView;
import org.hesperides.core.domain.platforms.queries.views.PropertiesEventView.UpdatedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.PropertiesEventView.ValuedPropertyView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class PropertiesEventOutput {
    Long timestamp;
    String author;
    String comment;
    @SerializedName("added_properties")
    List<ValuedPropertyOutput> addedProperties;
    @SerializedName("updated_properties")
    List<UpdatedPropertyOutput> updatedProperties;
    @SerializedName("removed_properties")
    List<ValuedPropertyOutput> removedProperties;

    public static List<PropertiesEventOutput> fromViews(List<PropertiesEventView> views) {
        return views.stream()
                .map(PropertiesEventOutput::new)
                .collect(Collectors.toList());
    }

    public PropertiesEventOutput(PropertiesEventView view) {
        timestamp = view.getTimestamp().getEpochSecond();
        author = view.getAuthor();
        comment = view.getComment();
        addedProperties = ValuedPropertyOutput.fromViews(view.getAddedProperties());
        updatedProperties = UpdatedPropertyOutput.fromViews(view.getUpdatedProperties());
        removedProperties = ValuedPropertyOutput.fromViews(view.getRemovedProperties());
    }

    @Value
    @AllArgsConstructor
    public static class ValuedPropertyOutput {
        String name;
        String value;

        public static List<ValuedPropertyOutput> fromViews(List<ValuedPropertyView> views) {
            return views.stream()
                    .map(ValuedPropertyOutput::new)
                    .collect(Collectors.toList());
        }

        public ValuedPropertyOutput(ValuedPropertyView view) {
            this.name = view.getName();
            this.value = view.getValue();
        }
    }

    @Value
    @AllArgsConstructor
    public static class UpdatedPropertyOutput {
        String name;
        @SerializedName("old_value")
        String oldValue;
        @SerializedName("new_value")
        String newValue;

        public static List<UpdatedPropertyOutput> fromViews(List<UpdatedPropertyView> views) {
            return views.stream()
                    .map(UpdatedPropertyOutput::new)
                    .collect(Collectors.toList());
        }

        public UpdatedPropertyOutput(UpdatedPropertyView view) {
            this.name = view.getName();
            this.oldValue = view.getOldValue();
            this.newValue = view.getNewValue();
        }
    }
}
