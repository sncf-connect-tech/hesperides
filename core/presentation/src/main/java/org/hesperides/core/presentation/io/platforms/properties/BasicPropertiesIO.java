package org.hesperides.core.presentation.io.platforms.properties;

import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BasicPropertiesIO extends PropertiesIO<ValuedPropertyIO> {

    public BasicPropertiesIO(@Valid Long propertiesVersionId, @Valid List<AbstractValuedPropertyView> abstractValuedPropertyViews) {
        super(propertiesVersionId, abstractValuedPropertyViews);
        final List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, IterableValuedPropertyView.class);
        this.iterableValuedProperties = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
        final List<ValuedPropertyView> valuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, ValuedPropertyView.class);
        this.valuedProperties = ValuedPropertyIO.fromValuedPropertyViews(valuedPropertyViews);
    }

    public BasicPropertiesIO(@Valid Long propertiesVersionId, @Valid  Set<ValuedPropertyIO> valuedProperties, @Valid Set<IterableValuedPropertyIO> iterableValuedProperties) {
        super(propertiesVersionId, iterableValuedProperties);
        this.valuedProperties = valuedProperties;
    }

    public List<AbstractValuedProperty> toDomainInstances() {
        final List<ValuedProperty> valuedProperties = ValuedPropertyIO.toDomainInstances(this.valuedProperties);
        final List<IterableValuedProperty> iterableValuedProperties = IterableValuedPropertyIO.toDomainInstances(this.iterableValuedProperties);
        final List<AbstractValuedProperty> properties = new ArrayList<>();
        properties.addAll(valuedProperties);
        properties.addAll(iterableValuedProperties);
        return properties;
    }
}
