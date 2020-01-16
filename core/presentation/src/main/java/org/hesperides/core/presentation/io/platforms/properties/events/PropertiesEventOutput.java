package org.hesperides.core.presentation.io.platforms.properties.events;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hesperides.core.domain.platforms.entities.properties.events.PropertiesEvent;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class PropertiesEventOutput {

    String author;
    
    String comment;

    Instant timestamp;

    List<String> addedProperties;

    List<String> removedProperties;

    List<PropertyEventUpdatedValueOuput> updatedProperties;


    public PropertiesEventOutput(PropertiesEvent propertyEvent) {
        comment = propertyEvent.getComment();
        author = propertyEvent.getAuthor();
        timestamp = propertyEvent.getTimestamp();
        addedProperties = propertyEvent.getAddedProperties();
        removedProperties = propertyEvent.getRemovedProperties();
        updatedProperties = Optional.ofNullable(propertyEvent.getUpdatedProperties())
                                        .orElseGet(Collections::emptyList)
                                        .stream()
                                        .map(PropertyEventUpdatedValueOuput::new)
                                        .collect(Collectors.toList());
    }

    /**
     * Conversion d'une liste d'objets domaine PropertiesEvent en une liste d'objets de présentation PropertiesEventOutput
     *
     * @param propertiesEventsDiffs La liste d'objets de domaine à convertir
     * @return La liste d'objets convertis
     */
    public static List<PropertiesEventOutput> fromPropertiesEventsDiffs(List<PropertiesEvent> propertiesEventsDiffs) {
        return Optional.ofNullable(propertiesEventsDiffs)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PropertiesEventOutput::new)
                .collect(Collectors.toList());
    }
}
