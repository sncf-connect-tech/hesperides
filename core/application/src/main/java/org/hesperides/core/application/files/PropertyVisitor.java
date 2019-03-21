package org.hesperides.core.application.files;

import java.util.function.*;

/* Cette interface a 2 rôles :
 - coupler les modèles de propriétés avec leurs valorisations
 - permettre le parcours récursif de l'arbre de ces propriétés
 */
interface PropertyVisitor {

    String getName();

    void acceptSimplesRecursive(Consumer<SimplePropertyVisitor> consumer);

    void acceptEither(Consumer<SimplePropertyVisitor> simpleConsumer, Consumer<IterablePropertyVisitor> iterableConsumer);

    boolean testSimplesRecursive(Predicate<SimplePropertyVisitor> predicate);

    PropertyVisitor mapSimplesRecursive(Function<SimplePropertyVisitor, PropertyVisitor> mapper);

    PropertyVisitor mapSequencesRecursive(Function<PropertyVisitorsSequence, PropertyVisitorsSequence> mapper);
}
