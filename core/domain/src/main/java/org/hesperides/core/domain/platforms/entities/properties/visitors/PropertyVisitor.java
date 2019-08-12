package org.hesperides.core.domain.platforms.entities.properties.visitors;

import java.util.function.Consumer;
import java.util.function.Function;

/* Cette interface a 2 rôles :
 - coupler les modèles de propriétés avec leurs valorisations
 - permettre le parcours récursif de l'arbre de ces propriétés
 */
public interface PropertyVisitor {

    String getName();

    void acceptSimplesRecursive(Consumer<SimplePropertyVisitor> consumer);

    PropertyVisitor mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper);

    PropertyVisitor mapSequencesRecursive(Function<PropertyVisitorsSequence, PropertyVisitorsSequence> mapper);

    boolean equals(PropertyVisitor propertyVisitor, boolean compareStoredValue);

    boolean isValued();
}
