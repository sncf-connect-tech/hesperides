package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.core.domain.workshopproperties.queries.views.WorkshopPropertyView;

@Value
public class WorkshopPropertyOutput {
    String key;
    String value;
    @SerializedName("key_value")
    String keyValue;

    public static WorkshopPropertyOutput fromWorkshopPropertyView(WorkshopPropertyView workshopPropertyView) {
        return new WorkshopPropertyOutput(
                workshopPropertyView.getKey(),
                workshopPropertyView.getValue(),
                workshopPropertyView.getKeyValue()
        );
    }
}
