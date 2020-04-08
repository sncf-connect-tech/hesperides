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

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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

    public static List<PropertiesEventView> buildPropertiesEvents(List<EventView> providedEvents, boolean isModuleProperties, boolean shouldExtractCreationEvent) {
        List<PropertiesEventView> propertiesEvents = new ArrayList<>();

        if (!isEmpty(providedEvents)) {
            // L'algorithme dépend du tri dans l'ordre chronologique
            providedEvents.sort(Comparator.comparing(EventView::getTimestamp));
            Iterator<EventView> eventsIterator = providedEvents.iterator();
            EventView previousEvent = eventsIterator.next();

            if (shouldExtractCreationEvent) {
                // Si la liste des évènements contient le tout premier évènement créé,
                // alors on ajoute un évènement contenant la création des propriétés
                // à notre propre liste
                List<ValuedPropertyView> firstAddedProperties = extractSimpleValuedPropertiesFromEvent(isModuleProperties, previousEvent)
                        .stream()
                        .map(ValuedPropertyView::new)
                        .collect(toList());
                propertiesEvents.add(new PropertiesEventView(previousEvent, firstAddedProperties, emptyList(), emptyList()));
            }

            Map<String, ValuedProperty> previousPropertiesByName = null;
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

                // Propriétés potentiellement déjà extraites au tour précédent et récupérées dans `currentPropertiesByName`
                if (previousPropertiesByName == null) {
                    previousPropertiesByName = extractSimpleValuedPropertiesByName(isModuleProperties, previousEvent);
                }
                // Pour l'instant on ne traite que les propriétés simples
                Map<String, ValuedProperty> currentPropertiesByName = extractSimpleValuedPropertiesByName(isModuleProperties, currentEvent);

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

                for (ValuedProperty currentProperty : currentPropertiesByName.values()) {
                    if (!previousPropertiesByName.containsKey(currentProperty.getName())) {
                        // Nouvelle propriété
                        addedProperties.add(new ValuedPropertyView(currentProperty));
                    }
                }

                if (!isEmpty(addedProperties) || !isEmpty(updatedProperties) || !isEmpty(removedProperties)) {
                    // Si aucune propriété n'est ajoutée, modifiée ou supprimée, on ne crée pas d'évènement.
                    // Cela peut se produire notamment dans les pipelines qui sauvegardent parfois les propriétés
                    // telles qu'elles ont été récupérées. Cela pose problème au niveau de la pagination d'ailleurs...
                    propertiesEvents.add(new PropertiesEventView(currentEvent, addedProperties, updatedProperties, removedProperties));
                }

                previousEvent = currentEvent;
                previousPropertiesByName = currentPropertiesByName;
            }
        }
        return propertiesEvents;
    }

    private static List<ValuedProperty> extractSimpleValuedPropertiesFromEvent(boolean isModuleProperties, EventView event) {
        return isModuleProperties
                ? extractSimpleValuedProperties(((PlatformModulePropertiesUpdatedEvent) event.getData()).getValuedProperties())
                : ((PlatformPropertiesUpdatedEvent) event.getData()).getValuedProperties();
    }

    private static Map<String, ValuedProperty> extractSimpleValuedPropertiesByName(boolean isModuleProperties, EventView event) {
        return extractSimpleValuedPropertiesFromEvent(isModuleProperties, event)
                .stream()
                .collect(toMap(ValuedProperty::getName, identity()));
    }

    private static List<ValuedProperty> extractSimpleValuedProperties(List<AbstractValuedProperty> valuedProperties) {
        return valuedProperties.stream()
                .filter(ValuedProperty.class::isInstance)
                .map(ValuedProperty.class::cast)
                .collect(toList());
    }

    public PropertiesEventView hidePasswords(Set<String> passwordPropertyNames) {
        List<ValuedPropertyView> addedProperties = hideValuedPasswordProperties(passwordPropertyNames, getAddedProperties());
        List<UpdatedPropertyView> updatedProperties = hideUpdatedPasswordProperties(passwordPropertyNames, getUpdatedProperties());
        List<ValuedPropertyView> removedProperties = hideValuedPasswordProperties(passwordPropertyNames, getRemovedProperties());
        return new PropertiesEventView(timestamp, author, comment, addedProperties, updatedProperties, removedProperties);
    }

    private static List<ValuedPropertyView> hideValuedPasswordProperties(Set<String> passwordPropertyNames, List<ValuedPropertyView> valuedProperties) {
        return valuedProperties.stream().map(valuedProperty -> passwordPropertyNames.contains(valuedProperty.getName())
                ? new ValuedPropertyView(valuedProperty.getName(), OBFUSCATED_PASSWORD_VALUE)
                : valuedProperty
        ).collect(toList());
    }

    private static List<UpdatedPropertyView> hideUpdatedPasswordProperties(Set<String> passwordPropertyNames, List<UpdatedPropertyView> updatedProperties) {
        return updatedProperties.stream().map(updatedProperty -> passwordPropertyNames.contains(updatedProperty.getName())
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
