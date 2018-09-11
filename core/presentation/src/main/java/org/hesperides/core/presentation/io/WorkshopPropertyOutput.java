package org.hesperides.core.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.core.domain.workshopproperties.queries.views.WorkshopPropertyView;

@Value
public class WorkshopPropertyOutput {
    String key;
    String value;
    @SerializedName("key_value")
    String keyValue;

    public WorkshopPropertyOutput(WorkshopPropertyView workshopPropertyView) {
        this.key = workshopPropertyView.getKey();
        this.value = workshopPropertyView.getValue();
        this.keyValue = workshopPropertyView.getKeyValue();
    }
}
