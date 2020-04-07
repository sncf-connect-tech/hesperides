package org.hesperides.core.domain.platforms.queries.views;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.platforms.PlatformModulePropertiesUpdatedEvent;
import org.hesperides.core.domain.platforms.PlatformPropertiesUpdatedEvent;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView.OBFUSCATED_PASSWORD_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

@Value
@AllArgsConstructor
public class PropertiesEventView {
    Instant timestamp;
    String author;
    String comment;
    List<ValuedPropertyView> addedProperties;
    List<UpdatedPropertyView> updatedProperties;
    List<ValuedPropertyView> removedProperties;

    public PropertiesEventView(EventView event,
                               List<ValuedPropertyView> addedProperties,
                               List<UpdatedPropertyView> updatedProperties,
                               List<ValuedPropertyView> removedProperties) {
        timestamp = event.getTimestamp();
        author = event.getData().getUser();
        comment = event.getData() instanceof PlatformModulePropertiesUpdatedEvent ? ((PlatformModulePropertiesUpdatedEvent) event.getData()).getUserComment() : null;
        this.addedProperties = addedProperties;
        this.updatedProperties = updatedProperties;
        this.removedProperties = removedProperties;
    }

    public static List<PropertiesEventView> buildPropertiesEvents(List<EventView> providedEvents, boolean isModuleProperties, boolean containsFirstEvent) {
        List<PropertiesEventView> propertiesEvents = new ArrayList<>();

        if (!isEmpty(providedEvents)) {
            // L'algorithme dépend du tri dans l'ordre chronologique
            providedEvents.sort(Comparator.comparing(EventView::getTimestamp));
            Iterator<EventView> eventsIterator = providedEvents.iterator();

            EventView previousEvent = eventsIterator.next(); // On a besoin de conserver `currentEvent` pour récupérer le timestamp

            List<ValuedProperty> simpleValuedProperties = isModuleProperties
                    ? extractSimpleValuedProperties(((PlatformModulePropertiesUpdatedEvent) previousEvent.getData()).getValuedProperties())
                    : ((PlatformPropertiesUpdatedEvent) previousEvent.getData()).getValuedProperties();

            if (containsFirstEvent) {
                // Premier évènement
                List<ValuedPropertyView> firstAddedProperties = simpleValuedProperties.stream()
                        .map(ValuedPropertyView::new)
                        .collect(toList());
                propertiesEvents.add(new PropertiesEventView(previousEvent, firstAddedProperties, emptyList(), emptyList()));
            }

            while (eventsIterator.hasNext()) {
                // On a besoin de conserver `currentEvent` pour le timestamp
                EventView currentEvent = eventsIterator.next();
                // Si une propriété est dans l'évènement précédent mais pas dans l'évènement
                // courant, c'est une propriété supprimée. Si elle n'est pas dans l'évènement
                // précédent mais dans l'évènement courant, c'est une nouvelle propriété. Si
                // elle est présente dans l'évènement précédent et l'évènement courant mais
                // que sa valeur est différente, c'est une propriété mise à jour.
                List<ValuedPropertyView> addedProperties = new ArrayList<>();
                List<UpdatedPropertyView> updatedProperties = new ArrayList<>();
                List<ValuedPropertyView> removedProperties = new ArrayList<>();

                // Pour l'instant on ne traite que les propriétés simples
                List<ValuedProperty> previousValuedProperties = isModuleProperties
                        ? extractSimpleValuedProperties(((PlatformModulePropertiesUpdatedEvent) previousEvent.getData()).getValuedProperties())
                        : ((PlatformPropertiesUpdatedEvent) previousEvent.getData()).getValuedProperties();
                List<ValuedProperty> currentValuedProperties = isModuleProperties
                        ? extractSimpleValuedProperties(((PlatformModulePropertiesUpdatedEvent) currentEvent.getData()).getValuedProperties())
                        : ((PlatformPropertiesUpdatedEvent) currentEvent.getData()).getValuedProperties();

                Map<String, ValuedProperty> previousPropertiesByName = simpleValuedPropertiesByName(previousValuedProperties);
                Map<String, ValuedProperty> currentPropertiesByName = simpleValuedPropertiesByName(currentValuedProperties);

                previousPropertiesByName.values().forEach(previousProperty -> {
                    ValuedProperty remainingProperty = currentPropertiesByName.getOrDefault(previousProperty.getName(), null);
                    if (remainingProperty == null) {
                        // Propriété supprimée
                        removedProperties.add(new ValuedPropertyView(previousProperty));
                    } else if (!Objects.equals(previousProperty.getValue(), remainingProperty.getValue())) {
                        // Propriété modifiée
                        updatedProperties.add(new UpdatedPropertyView(previousProperty, remainingProperty));
                    }
                });

                currentPropertiesByName.values().forEach(currentProperty -> {
                    if (!previousPropertiesByName.containsKey(currentProperty.getName())) {
                        // Nouvelle propriété
                        addedProperties.add(new ValuedPropertyView(currentProperty));
                    }
                });

                if (!isEmpty(addedProperties) || !isEmpty(updatedProperties) || !isEmpty(removedProperties)) {
                    propertiesEvents.add(new PropertiesEventView(currentEvent, addedProperties, updatedProperties, removedProperties));
                }

                previousEvent = currentEvent;
            }
        }
        return propertiesEvents;
    }

    private static List<ValuedProperty> extractSimpleValuedProperties(List<AbstractValuedProperty> valuedProperties) {
        return valuedProperties.stream()
                .filter(ValuedProperty.class::isInstance)
                .map(ValuedProperty.class::cast)
                .collect(toList());
    }

    private static Map<String, ValuedProperty> simpleValuedPropertiesByName(List<ValuedProperty> simpleValuedProperties) {
        return simpleValuedProperties.stream()
                .collect(Collectors.toMap(ValuedProperty::getName, identity()));
    }

    public PropertiesEventView hidePasswords(List<String> passwordProperties) {
        List<ValuedPropertyView> addedProperties = hideValuedPasswordProperties(passwordProperties, getAddedProperties());
        List<UpdatedPropertyView> updatedProperties = hideUpdatedPasswordProperties(passwordProperties, getUpdatedProperties());
        List<ValuedPropertyView> removedProperties = hideValuedPasswordProperties(passwordProperties, getRemovedProperties());
        return new PropertiesEventView(timestamp, author, comment, addedProperties, updatedProperties, removedProperties);
    }

    private static List<ValuedPropertyView> hideValuedPasswordProperties(List<String> passwordProperties, List<ValuedPropertyView> valuedProperties) {
        return valuedProperties.stream().map(valuedProperty -> passwordProperties.contains(valuedProperty.getName())
                ? new ValuedPropertyView(valuedProperty.getName(), OBFUSCATED_PASSWORD_VALUE)
                : valuedProperty
        ).collect(toList());
    }

    private static List<UpdatedPropertyView> hideUpdatedPasswordProperties(List<String> passwordProperties, List<UpdatedPropertyView> updatedProperties) {
        return updatedProperties.stream().map(updatedProperty -> passwordProperties.contains(updatedProperty.getName())
                ? new UpdatedPropertyView(updatedProperty.getName(), OBFUSCATED_PASSWORD_VALUE, OBFUSCATED_PASSWORD_VALUE)
                : updatedProperty
        ).collect(toList());
    }

    @Value
    @AllArgsConstructor
    public static class ValuedPropertyView {
        String name;
        String value;

        public ValuedPropertyView(ValuedProperty valuedProperty) {
            name = valuedProperty.getName();
            value = valuedProperty.getValue();
        }
    }

    @Value
    @AllArgsConstructor
    public static class UpdatedPropertyView {
        String name;
        String oldValue;
        String newValue;

        public UpdatedPropertyView(ValuedProperty previousProperty, ValuedProperty remainingProperty) {
            name = previousProperty.getName();
            oldValue = previousProperty.getValue();
            newValue = remainingProperty.getValue();
        }
    }
}
