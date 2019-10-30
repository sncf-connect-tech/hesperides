package org.hesperides.core.presentation.io.platforms.properties;

import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BasicPropertiesIo extends  PropertiesIO<ValuedPropertyIO> {


    public BasicPropertiesIo(@Valid Long propertiesVersionId, @NotNull @Valid Set<ValuedPropertyIO> valuedProperties,
                             @NotNull @Valid Set<IterableValuedPropertyIO> iterableValuedProperties, @NotNull @Valid List<AbstractValuedPropertyView> abstractValuedPropertyViews) {
        super(propertiesVersionId, valuedProperties, iterableValuedProperties);

        final List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, IterableValuedPropertyView.class);
        this.iterableValuedProperties = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
        if(CollectionUtils.isEmpty( this.iterableValuedProperties)) {
           this.iterableValuedProperties = iterableValuedProperties;
        }

        final List<ValuedPropertyView> valuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, ValuedPropertyView.class);
        this.valuedProperties = ValuedPropertyIO.fromValuedPropertyViews(valuedPropertyViews);
        if(CollectionUtils.isEmpty(this.valuedProperties)) {
            this.valuedProperties = valuedProperties;
        }
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
