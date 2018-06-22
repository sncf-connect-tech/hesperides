package org.hesperides.presentation.io;

import lombok.Value;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
public class WorkshopPropertyInput {

    @NotNull
    @NotEmpty
    String key;
    @NotNull
    @NotEmpty
    String value;

    public WorkshopProperty toDomainInstance() {
        return new WorkshopProperty(key, value, null);
    }
}
