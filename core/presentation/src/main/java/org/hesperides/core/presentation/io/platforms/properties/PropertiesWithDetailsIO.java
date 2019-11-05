package org.hesperides.core.presentation.io.platforms.properties;

import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

public class PropertiesWithDetailsIO extends PropertiesIO<PropertyWithDetailsIO> {

    public PropertiesWithDetailsIO(@Valid Long propertiesVersionId, @Valid Set<PropertyWithDetailsIO> valuedProperties, @Valid List<AbstractValuedPropertyView> abstractValuedPropertyViews) {
        super(propertiesVersionId, abstractValuedPropertyViews);
        List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, IterableValuedPropertyView.class);
        this.iterableValuedProperties = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
        this.valuedProperties = valuedProperties;
    }
}
